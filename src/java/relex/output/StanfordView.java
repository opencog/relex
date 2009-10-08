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

import java.util.HashMap;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;
import relex.ParsedSentence;

/**
 * Prototype implementation of Stanford-parser compatible output.
 */
public class SFView
{
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
		Visit v = new Visit();
		v.str = "";
		RelationForeach.foreach(parse.getLeft(), v, "sf-links");
		return v.str;
	}

	private static class Visit implements RelationCallback
	{
		public boolean unaryStyle = false;
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }
		public Boolean BinaryRelationCB(String relName,
		                                FeatureNode srcNode,
		                                FeatureNode tgtNode)
		{
			FeatureNode tgt = tgtNode.get("name");
			if (tgt == null)
			{
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

			FeatureNode srcN = srcNode.get("nameSource");
			FeatureNode tgtN = tgtNode.get("nameSource");

			String srcName = srcN.get("orig_str").getValue();
			String tgtName = tgtN.get("orig_str").getValue();
			String srcIdx = srcN.get("index_in_sentence").getValue();
			String tgtIdx = tgtN.get("index_in_sentence").getValue();

			str += relName + "(" + srcName + "-" + srcIdx + ", " + 
				tgtName + "-" + tgtIdx + ")\n";

			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
/**********************
			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();
			String srcName = srcNode.get("name").getValue();

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
******************/

			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
