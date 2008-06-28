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
import java.util.HashMap;

import relex.feature.FeatureNode;
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
	private int parse_count;
	private int max_parses;

	public CompactView()
	{
		do_show_constituents = true;
		max_parses = 4;
	}

	public void showConstituents(boolean sc)
	{
		do_show_constituents = sc;
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
      str += "      <lg-rank ";
		str += "num_skipped_words=\"" + parse.getNumSkippedWords() + "\" ";
		str += "disjunct_cost=\"" + parse.getDisjunctCost() + "\" ";
		str += "and_cost=\"" + parse.getAndCost() + "\" ";
		str += "link_cost=\"" + parse.getLinkCost() + "\" ";
		str += "/>\n";
		if (do_show_constituents)
		{
      	str += "      <constituents>" + parse.getPhraseString() +
			       "      </constituents>\n";
		}
		str += "    </parse>\n";
		return str;
   }

	/**
	 * Print out RelEx relations. All relations shown
	 * in a binary form.
	 *
	 * Example:
	 *   _subj(throw, John)
	 *   _obj(throw, ball)
	 *   tense(throw, past)
	 *   DEFINITE-FLAG(ball, T)
	 *   noun_number(ball, singular)
	 */
	public static String printRelations(ParsedSentence parse)
	{
		return printRelations(parse, null);
	}
	public static String printRelations(ParsedSentence parse,
	                                    HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.str = "";
		parse.foreach(v);
		return v.str;
	}

	/**
	 * Print out RelEx relations, alternate format.
	 * Unary relations, including booleans, doen't show
	 * the attribute name.
	 *
	 * Example:
	 *   _subj(throw, John)
	 *   _obj(throw, ball)
	 *   past(throw)
	 *   definite(ball)
	 *   singular(ball)
	 */
	public static String printRelationsAlt(ParsedSentence parse)
	{
		return printRelationsAlt(parse, null);
	}
	public static String printRelationsAlt(ParsedSentence parse,
	                                    HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.unaryStyle = true;
		v.str = "";
		parse.foreach(v);
		return v.str;
	}

	private static class Visit implements RelationCallback
	{
		// Map associating a feature-node to a unique ID string.
		public HashMap<FeatureNode,String> id_map = null;

		public Boolean unaryStyle = false;
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }
		public Boolean BinaryRelationCB(String relName,
		                                FeatureNode srcNode,
		                                FeatureNode tgtNode)
		{
			String srcName = srcNode.get("name").getValue();
			FeatureNode tgt = tgtNode.get("name");
			if (tgt == null)
			{
				System.out.println("Error: No target! rel=" + relName +
				                   " and src=" + srcName);
				return false;
			}
			String tgtName = tgt.getValue();

			if (id_map != null)
			{
				srcName = id_map.get(srcNode);
				tgtName = id_map.get(tgtNode);
			}
			str += relName + "(" + srcName + ", " + tgtName + ")\n";

			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();
			String srcName = srcNode.get("name").getValue();

			if (id_map != null)
			{
				srcName = id_map.get(srcNode);
			}
			if (unaryStyle)
			{
				if (attrName.endsWith("-FLAG"))
					value = attrName.replaceAll("-FLAG","").toLowerCase();

				if (attrName.equals("HYP"))
					value = attrName.toLowerCase();

				str += value + "(" + srcName + ")\n";
			}
			else
			{
				str += attrName + "(" + srcName + ", " + value + ")\n";
			}

			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
