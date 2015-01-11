/*
 * Copyright 2008 Novamente LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package relex.output;

import java.text.SimpleDateFormat;
import java.util.Date;

import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkForeach;
import relex.feature.RelationCallback;
import relex.ParsedSentence;
import relex.Sentence;

/**
 * Implements the so-called "compact view", as documented at
 * http://opencog.org/wiki/RelEx_compact_output
 *
 * Copyright (c) 2008 Linas Vepstas <linas@linas.org>
 */
public class CompactView
{
	private boolean do_show_constituents;
	private boolean do_show_metadata;
	private boolean do_show_links;
	private int sentence_count;
	private int parse_count;
	private int max_parses;
	private String sourceURL;
	private String version;

	private boolean notfirst;

	public CompactView()
	{
		do_show_constituents = true;
		do_show_metadata = true;
		do_show_links = true;
		sentence_count = 0;
		max_parses = 4;
		sourceURL = null;
		version = "relex";
	}

	public void showConstituents(boolean sc)
	{
		do_show_constituents = sc;
	}

	public void showMetadata(boolean sc)
	{
		do_show_metadata = sc;
	}

	public void showLinks(boolean sc)
	{
		do_show_links = sc;
	}

	public void setMaxParses(int max)
	{
		max_parses = max;
	}

	public void setSourceURL(String url)
	{
		sourceURL = url;
	}

	public void setVersion(String v)
	{
		version = v;
	}

	public String header()
	{
		sentence_count = 0;

		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		str += "<nlparse xmlns=\"http://opencog.org/RelEx/0.1.1\">\n";

		str += "  <parser>" + version + "</parser>\n";

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		str += "  <date>" + sdf.format(now) + "</date>\n";

		if (sourceURL != null)
		{
			str += "  <source url=\"" + sourceURL + "\" />";
		}
		return str;
	}

	public String footer()
	{
		return "</nlparse>";
	}

	public String toString(Sentence sntc)
	{
		sentence_count ++;
		parse_count = 0;

		String str = "  <sentence index=\"" + sentence_count + "\"";
		str += " parses=\"" + sntc.getNumParses() + "\">\n";
		str += "  " + sntc.getSentence() + "\n";
		for (ParsedSentence parse: sntc.getParses())
		{
			str += toString(parse);
			if (parse_count > max_parses) break;
		}
		str += "  </sentence>";
		return str;
	}

	public String toString(ParsedSentence parse)
	{
		parse_count ++;
		String str = "    <parse id=\"" + parse_count + "\">\n";

		// Print link-grammar's parse ranking.
		if (do_show_metadata)
		{
			str += "      <lg-rank ";
			str += "num_skipped_words=\"" + parse.getNumSkippedWords() + "\" ";
			str += "disjunct_cost=\"" + parse.getDisjunctCost() + "\" ";
			str += "link_cost=\"" + parse.getLinkCost() + "\" ";
			str += "/>\n";
		}

		// Show the HPSG-style (Penn tree-bank) constituent tree.
		if (do_show_constituents)
		{
			str += "      <constituents>" + parse.getPhraseString() +
			       "      </constituents>\n";
		}

		// Print the lists of features
		str += printFeatures(parse);

		// Print the dependency relations
		str += printRelations(parse);

		// Show the Link-grammar links
		if (do_show_links)
		{
			str += "      <links>\n" + printLinks(parse) +
			       "      </links>\n";
		}
		str += "    </parse>\n";
		return str;
	}

	// -----------------------------------------------------------------

	private String getfeat(FeatureNode f, String fn)
	{
		f = f.get(fn);
		if (f == null) return "";
		if (notfirst) return "|" + f.getValue();
		notfirst = true;
		return f.getValue();
	}

	private String getflag(FeatureNode f, String fn)
	{
		FeatureNode ff = f.get(fn);
		if (ff == null)
		{
			ff = f.get(fn.replaceAll("-FLAG","").toLowerCase() + "-FLAG");
		}
		if (ff == null) return "";

		fn = fn.replaceAll("-FLAG","").toLowerCase();
		if (notfirst) return "|" + fn;
		notfirst = true;
		return fn;
	}

	private String getstr(FeatureNode node)
	{
		if (null ==  node) {
			return "\t";
		} else {
			return node.getValue() + "\t";
		}
	}

	/**
	 *  Print the CoNLL-style lemmas, parts of speech, and feature lists
	 */
	private String printFeatures(ParsedSentence parse)
	{
		String str = "      <features>\n";

		FeatureNode node = parse.getLeft();
		node = node.get("NEXT");
		while (node != null)
		{
			str += node.get("index_in_sentence").getValue() + "\t";

			str += getstr(node.get("orig_str"));
			str += getstr(node.get("str"));

			str += node.get("POS").getValue() + "\t";

			FeatureNode ref = node.get("ref");
			if (ref != null)
			{
				str += getfeat(ref, "tense");
				str += getfeat(ref, "noun_number");
				str += getfeat(ref, "gender");
				str += getfeat(ref, "QUERY-TYPE");
				str += getfeat(ref, "subscript-TAG");

				str += getflag(ref, "date-FLAG");
				str += getflag(ref, "definite-FLAG");
				str += getflag(ref, "emoticon-FLAG");
				str += getflag(ref, "entity-FLAG");
				str += getflag(ref, "idiom-FLAG");
				str += getflag(ref, "location-FLAG");
				str += getflag(ref, "money-FLAG");
				str += getflag(ref, "organization-FLAG");
				str += getflag(ref, "person-FLAG");
				str += getflag(ref, "polyword-FLAG");
				str += getflag(ref, "pronoun-FLAG");
				str += getflag(ref, "time-FLAG");
			}

			notfirst = false;

			str += "\n";
			// Iterate to the next word
			node = node.get("NEXT");
		}
		str += "      </features>\n";
		return str;
	}

	// -----------------------------------------------------------------
	/**
	 *  Print the link-grammar links
	 *  LAB, F_L, F_R
	 */
	private String printLinks(ParsedSentence parse)
	{
		LinkCB cb = new LinkCB();
		cb.str = "";
		LinkForeach.foreach(parse.getLeft(), cb);
		return cb.str;
	}

	private class LinkCB implements FeatureNodeCallback
	{
		String str;
		public Boolean FNCallback(FeatureNode fn)
		{
			str += fn.get("LAB").getValue() + "(";
			FeatureNode fl = fn.get("F_L");
			str += fl.get("index_in_sentence").getValue();
			str += ", ";
			FeatureNode fr = fn.get("F_R");
			str += fr.get("index_in_sentence").getValue();
			str += ")\n";
			return false;
		}
	};

	// -----------------------------------------------------------------
	/**
	 * Print out RelEx relations. All relations shown
	 * in a binary form.
	 *
	 * Example:
	 *   _subj(throw, John)
	 *   _obj(throw, ball)
	 */
	private String printRelations(ParsedSentence parse)
	{
		Visit v = new Visit();
		v.str = "      <relations>\n";
		parse.foreach(v);
		v.str += "      </relations>\n";
		return v.str;
	}

	private static class Visit implements RelationCallback
	{
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }
		public Boolean BinaryRelationCB(String relName,
		                                FeatureNode srcNode,
		                                FeatureNode tgtNode)
		{
			String srcName = srcNode.get("name").getValue();
			String tgtName = "";
			FeatureNode tgt = tgtNode.get("name");
			if (tgt != null)
			{
				tgtName = tgt.getValue();
			}

			String srcIdx = "-1";
			srcNode = srcNode.get("nameSource");
			if (srcNode != null)
			{
				srcIdx = srcNode.get("index_in_sentence").getValue();
			}

			String tgtIdx = "-1";
			tgtNode = tgtNode.get("nameSource");
			if (tgtNode != null)
			{
				tgtIdx = tgtNode.get("index_in_sentence").getValue();
			}

			str += relName + "(" + srcName + "[" + srcIdx + "], " +
			       tgtName + "[" + tgtIdx + "])\n";
			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
