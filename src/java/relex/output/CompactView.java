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
import relex.RelexInfo;

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
	private int parse_count;
	private int max_parses;

	private boolean notfirst;

	public CompactView()
	{
		do_show_constituents = true;
		do_show_metadata = true;
		do_show_links = true;
		max_parses = 4;
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

	public String header()
	{
		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		str += "<nlparse xmlns=\"http://opencog.org/RelEx/0.1\">\n";
		// hack alert -- get the real version number!
		str += "  <parser>link-grammar-4.3.5\trelex-0.9.0</parser>\n";

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		str += "  <date>" + sdf.format(now) + "</date>\n";

		str += "  <source url=\"xxx broken fixme\">";
		return str;
	}

	public String footer()
	{
		return "</nlparse>";
	}

	public String toString(RelexInfo ri)
   {
		parse_count = 0;

		String str = "  <sentence id=\"xxx fix me\"";
		str += " parses=\"" + ri.parsedSentences.size() + "\">\n";
      str += "  " + ri.getSentence() + "\n";
		for (ParsedSentence parse: ri.parsedSentences)
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
			str += "and_cost=\"" + parse.getAndCost() + "\" ";
			str += "link_cost=\"" + parse.getLinkCost() + "\" ";
			str += "/>\n";
		}

		// Show the Penn tree-bank style constituent tree.
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
		f = f.get(fn);
		if (f == null) return "";

		fn = fn.replaceAll("-FLAG","").toLowerCase();
		if (notfirst) return "|" + fn;
		notfirst = true;	
		return fn;
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
			str += node.get("orig_str").getValue() + "\t";
			str += node.get("str").getValue() + "\t";
			str += node.get("POS").getValue() + "\t";

			FeatureNode ref = node.get("ref");
			str += getfeat(ref, "tense");
			str += getfeat(ref, "noun_number");
			str += getfeat(ref, "gender");
			str += getfeat(ref, "QUERY-TYPE");

			str += getflag(ref, "DATE-FLAG");
			str += getflag(ref, "DEFINITE-FLAG");
			str += getflag(ref, "EMOTICON-FLAG");
			str += getflag(ref, "ENTITY-FLAG");
			str += getflag(ref, "IDIOM-FLAG");
			str += getflag(ref, "LOCATION-FLAG");
			str += getflag(ref, "MONEY-FLAG");
			str += getflag(ref, "ORGANIZATION-FLAG");
			str += getflag(ref, "PERSON-FLAG");
			str += getflag(ref, "POLYWORD-FLAG");

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
		public Boolean unaryStyle = false;
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

			srcNode = srcNode.get("nameSource");
			String srcIdx = srcNode.get("index_in_sentence").getValue();

			tgtNode = tgtNode.get("nameSource");
			String tgtIdx = tgtNode.get("index_in_sentence").getValue();

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
