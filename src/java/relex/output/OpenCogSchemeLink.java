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
			str +=
				"(EvaluationLink (stv 1.0 1.0)\n" +
				"   (LinkGrammarRelationshipNode \"" +
				fn.get("LAB").getValue() + "\")\n" +
				"   (ListLink\n" +
				"      (LgWordConn\n" +
				"         (WordInstanceNode \"";

			FeatureNode fl = fn.get("F_L");
			FeatureNode labl = fn.get("lab_L");
			str += fl.get("uuid").getValue() + "\")\n" +
				"         (LgConnector\n" +
				"            (LgConnectorNode \"" + labl.getValue() + "\")\n" +
				"            (LgConnDirNode \"+\")\n" +
				"         )\n" +
				"      )\n" +
				"      (LgWordConn\n" +
				"         (WordInstanceNode \"";

			FeatureNode fr = fn.get("F_R");
			FeatureNode labr = fn.get("lab_R");
			str += fr.get("uuid").getValue() + "\")\n" +
				"         (LgConnector\n" +
				"            (LgConnectorNode \"" + labr.getValue() + "\")\n" +
				"            (LgConnDirNode \"-\")\n" +
				"         )\n" +
				"      )\n" +
				"   )\n)\n";
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
			if (!attr.isValued())
				return false;
			
			String value = attr.getValue();
			
			// split the value into different connectors
			String[] connectors = value.split(" ");

			str += "(LgWordCset \n";
			str += "    (WordInstanceNode \"" + srcNode.get("uuid").getValue() + "\")\n";
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
				str += "            (LgConnectorNode \"" + name + "\")\n";
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

