/*
 * Copyright 2008 Novamente LLC
 * Copyright (c) 2008,2009 Linas Vepstas <linasvepstas@gmail.com>
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

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;
import relex.ParsedSentence;

/**
 * Prototype implementation of Stanford-parser compatible output.
 */
public class StanfordView
{
	/**
	 * Print out Stanford-parser-style dependency relations.
	 *
	 * Example:
	 *   nsubj(throw, John)
	 *   dobj(throw, ball)
	 */
	public static String printRelations(ParsedSentence parse,
	                                    boolean show_tags)
	{
		return printRelations(parse, show_tags, "");
	}
	public static String printRelations(ParsedSentence parse,
	                                    boolean show_tags,
	                                    String indent)
	{
		Visit v = new Visit();
		v.str = "";
		v.show_penn_tags = show_tags;
		v.indent = indent;
		RelationForeach.foreach(parse.getLeft(), v, "sf-links");
		return v.str;
	}

	private static class Visit implements RelationCallback
	{
		public String str;
		public String indent;
		public boolean show_penn_tags;

		public Boolean BinaryHeadCB(FeatureNode node) { return false; }
		public Boolean BinaryRelationCB(String relName,
		                                FeatureNode srcNode,
		                                FeatureNode tgtNode)
		{
			FeatureNode srcN = srcNode.get("nameSource");
			FeatureNode tgtN = tgtNode.get("nameSource");
			if (tgtN == null)
			{
				// Such errors can arise sometimes -- ignore them for now.
				// e.g. parsing: "Be sure to check."
				String srcName = srcNode.get("name").getValue();
				System.out.println("Error: No target! rel=" + relName +
				                  " and src=" + srcName);
				return false;
			}

			// Trim leading underscores from the relation names.
			char underscore = relName.charAt(0);
			if ('_' == underscore)
			{
				relName = relName.substring(1);
			}
			else
			{
				relName = "prep_" + relName;
			}

			String srcName = srcN.get("orig_str").getValue();
			String tgtName = tgtN.get("orig_str").getValue();
			String srcIdx = srcN.get("index_in_sentence").getValue();
			String tgtIdx = tgtN.get("index_in_sentence").getValue();

			String srcPosTag = "";
			String tgtPosTag = "";
			if (show_penn_tags)
			{
				FeatureNode srcPN = srcNode.get("penn-POS");
				if (null != srcPN) srcPosTag = "-" + srcPN.getValue();
				FeatureNode tgtPN = tgtNode.get("penn-POS");
				if (null != tgtPN) tgtPosTag = "-" + tgtPN.getValue();
			}

			str += indent + relName + "(" + srcName + "-" + srcIdx + srcPosTag + ", " +
				tgtName + "-" + tgtIdx + tgtPosTag + ")\n";

			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
