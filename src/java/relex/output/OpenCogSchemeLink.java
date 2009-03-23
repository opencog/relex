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

import java.util.ArrayList;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkForeach;

/**
 * The OpenCogSchemeLink object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its rationale,
 * is described in greater detail in the README file in the opencog
 * source code directory src/nlp/wsd/README.
 *
 * As the same sentence can have multiple parses, this class only
 * displays a single, particular parse.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
class OpenCogSchemeLink
{
	// The sentence being examined.
	private ParsedSentence parse;

	// Map associating a feature-node to a unique ID string.
	private ArrayList<String> word_list = null;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public OpenCogSchemeLink()
	{
		parse = null;
	}

	public void setParse(ParsedSentence s, ArrayList<String> wl)
	{
		parse = s;
		word_list = wl;
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
				"      (WordInstanceNode \"";

			FeatureNode fl = fn.get("F_L");

			String li = fl.get("index_in_sentence").getValue();
			int lindex = Integer.parseInt(li);
			str += word_list.get(lindex) + "\")\n" +
				"      (WordInstanceNode \"";


			FeatureNode fr = fn.get("F_R");
			String ri = fr.get("index_in_sentence").getValue();
			int rindex = Integer.parseInt(ri);
			str += word_list.get(rindex) + "\")\n" +
				"   )\n)\n";
			return false;
		}
	};

	/* ----------------------------------------------------------- */

	public String toString()
	{
		String ret = "";
		ret += printLinks();
		return ret;
	}

} // end RelScheme

