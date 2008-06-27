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

import java.util.HashMap;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.ParsedSentence;

/**
 * Implements the so-called "compact view", as documented at
 * http://opencog.org/wiki/RelEx_compact_output
 *
 * Copyright (c) 2008 Linas Vepstas <linas@linas.org>
 */
public class CompactView
{
	public static String toString(ParsedSentence parse)
   {
		return "";
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
