/*
 * Copyright 2008 Novamente LLC
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
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

import java.util.UUID;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkForeach;

/**
 * The OpenCogSchemeLink object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its rationale,
 * is described in greater detail in the README file in the opencog
 * source code directory opencog/nlp/wsd/README.
 *
 * As the same sentence can have multiple parses, this class only
 * displays a single, particular parse.
 */
class OpenCogSchemeLink
{
	// The sentence being examined.
	private ParsedSentence parse;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public OpenCogSchemeLink()
	{
		parse = null;
	}

	public void setParse(ParsedSentence s)
	{
		parse = s;
	}

	// -----------------------------------------------------------------
	/**
	 *  Print the link-grammar links
	 *  LAB, F_L, F_R
	 */
	private String printLinks()
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
			String lab = fn.get("LAB").getValue();
			String lab_inst = fn.get("LAB").getValue() + "@" + UUID.randomUUID();
			String fl_inst = fn.get("F_L").get("uuid").getValue();
			String fr_inst = fn.get("F_R").get("uuid").getValue();
			fl_inst = fl_inst.replaceAll("\"", "\\\\\"");
			fr_inst = fr_inst.replaceAll("\"", "\\\\\"");
			
			str +=
				"(EvaluationLink (stv 1.0 1.0)\n" +
				"   (LinkGrammarRelationshipNode \"" + lab + "\")\n" +
				"   (ListLink\n" +
				"      (WordInstanceNode \"" + fl_inst + "\")\n" +
				"      (WordInstanceNode \"" + fr_inst + "\")\n" +
				"   )\n)\n";

			str +=
				"(EvaluationLink (stv 1.0 1.0)\n" +
				"   (LgLinkInstanceNode \"" + lab_inst + "\")\n" +
				"   (ListLink\n" +
				"      (WordInstanceNode \"" + fl_inst + "\")\n" +
				"      (WordInstanceNode \"" + fr_inst + "\")\n" +
				"   )\n)\n";

			FeatureNode labl = fn.get("lab_L");
			FeatureNode labr = fn.get("lab_R");
			str +=
				"(LgLinkInstanceLink \n" +
				"   (LgLinkInstanceNode \"" + lab_inst + "\")\n" +
				"   (LgConnector\n" +
				"      (LgConnNode \"" + labl.getValue() + "\")\n" +
				"      (LgConnDirNode \"+\")\n" +
				"   )\n" +
				"   (LgConnector\n" +
				"      (LgConnNode \"" + labr.getValue() + "\")\n" +
				"      (LgConnDirNode \"-\")\n" +
				"   )\n)\n";

			str +=
				"(ReferenceLink\n" +
				"   (LgLinkInstanceNode \"" + lab_inst + "\")\n" +
				"   (LinkGrammarRelationshipNode \"" + lab + "\")\n" +
				")\n";
			return false;
		}
	};
        
	/**
	 * Print the link-grammar disjuncts
	 * DISJUNCT
	 */
	private String printDisjuncts()
	{
		DisjunctCB cb = new DisjunctCB();
		cb.str = "";
		FeatureNode fn = parse.getLeft();
		while (fn != null)
		{
			cb.FNCallback(fn);
			fn = fn.get("NEXT");
		}
		return cb.str;
	}

	private class DisjunctCB implements FeatureNodeCallback
	{
		String str;

		public Boolean FNCallback(FeatureNode srcNode)
		{
			FeatureNode attr = srcNode.get("DISJUNCT");

			if (attr == null || !attr.isValued())
				return false;

			String value = attr.getValue();

			// handle bad sentences where a word can have no connections
			if (value.length() == 0)
				return false;

			// split the value into different connectors
			String[] connectors = value.split(" ");

			String guid_word = srcNode.get("uuid").getValue();
			guid_word = guid_word.replaceAll("\"", "\\\\\"");

			str += "(LgWordCset \n";
			str += "    (WordInstanceNode \"" + guid_word + "\")\n";
			str += "    (LgAnd \n";

			// connectors should already be sorted with - before +
			for (String conn : connectors)
			{
				String name;
				String direction;
				Boolean multi;

				if (conn.charAt(0) == '@')
				{
					name = conn.substring(1, conn.length() - 1);
					direction = conn.substring(conn.length() - 1);
					multi = true;
				}
				else
				{
					name = conn.substring(0, conn.length() - 1);
					direction = conn.substring(conn.length() - 1);
					multi = false;
				}

				str += "        (LgConnector \n";
				str += "            (LgConnNode \"" + name + "\")\n";
				str += "            (LgConnDirNode \"" + direction + "\")\n";

				if (multi)
					str += "            (LgConnMultiNode \"@\")\n";

				str += "        )\n";
			}

			str += "    )\n";
			str += ")\n";

			return false;
		}
	};

	/* ----------------------------------------------------------- */

	public String toString()
	{
		String ret = "";
		ret += printLinks();
		ret += printDisjuncts();
		return ret;
	}

} // end RelScheme

