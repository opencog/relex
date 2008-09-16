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
	private ParsedSentence sent;

	// Map associating a feature-node to a unique ID string.
	private HashMap<FeatureNode,String> id_map = null;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public OpenCogSchemeLink()
	{
		sent = null;
	}

	public void setParse(ParsedSentence s, HashMap<FeatureNode,String> im)
	{
		sent = s;
		id_map = im;
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
		LinkForeach.foreach(sent.getLeft(), cb);
		return cb.str;
	}

	private class LinkCB implements FeatureNodeCallback
	{
		String str;
		public Boolean FNCallback(FeatureNode fn)
		{
			str +=
				"(EvaluationLink\n" + 
				"   (LinkGrammarRelationshipNode " +
				fn.get("LAB").getValue() + ")\n" +
				"   (ListLink\n" +
				"      (ConceptNode ";

			FeatureNode fl = fn.get("F_L");

			// A unique UUID for each word instance.
			// fn = fn.get("ref");
			// if (fn == null) continue;
			// FeatureNode refNode = fn;
			// String guid_name = id_map.get(refNode);

			str += fl.get("index_in_sentence").getValue();
			str += ", ";
			FeatureNode fr = fn.get("F_R");
			str += fr.get("index_in_sentence").getValue();
			str += ")\n";
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

