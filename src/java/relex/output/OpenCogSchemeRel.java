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

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.RelationCallback;

/**
 * The OpenCogSchemeRel object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its
 * rationale, is described in greater detail in the opencog wiki, at
 * http://wiki.opencog.org/w/RelEx_OpenCog_format
 * See also the opencog source code directory opencog/nlp/wsd/README.
 *
 * As the same sentence can have multiple parses, this class only
 * displays a single, particular parse.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
class OpenCogSchemeRel
{
	// The sentence being examined.
	private ParsedSentence parse;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public OpenCogSchemeRel()
	{
		parse = null;
	}

	public void setParse(ParsedSentence s)
	{
		parse = s;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Walk the graph, extracting semantic relationships, and word
	 * attributes.
	 */
	private class prtRelation implements RelationCallback
	{
		public String outstr;
		public prtRelation()
		{
			outstr = "";
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			FeatureNode src = srcNode.get("name");
			if (src == null) return false;
			String srcName = src.getValue();
			if (srcName == null) return false;

			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();

			outstr += "; " + attrName + " (" + srcName + ", " + value + ")\n";
			String guid = srcNode.get("nameSource").get("uuid").getValue();
			guid = guid.replaceAll("\"", "\\\\\"");

			// Flags are assumed to be true, so value is the flag name.
			if (attrName.endsWith("-FLAG"))
				value = attrName.replaceAll("-FLAG","").toLowerCase();
			if (attrName.equals("HYP"))
				value = attrName.toLowerCase();

			// Special treatment for part-of-speech.
			if (attrName.equals("pos"))
			{
				outstr += "(PartOfSpeechLink (stv 1.0 1.0)\n";
				outstr += "   (WordInstanceNode \"" + guid + "\")\n";
				outstr += "   (DefinedLinguisticConceptNode \"" + value + "\")\n";
				outstr += ")\n";
			}

			// Special treatment for tense
			else if (attrName.equals("tense"))
			{
				outstr += "(TenseLink (stv 1.0 1.0)\n";
				outstr += "   (WordInstanceNode \"" + guid + "\")\n";
				outstr += "   (DefinedLinguisticConceptNode \"" + value + "\")\n";
				outstr += ")\n";
			}

			// All of the other cases.
			else
			{
				outstr += "(InheritanceLink (stv 1.0 1.0)\n";
				outstr += "   (WordInstanceNode \"" + guid + "\")\n";
				outstr += "   (DefinedLinguisticConceptNode \"" + value + "\")\n";
				outstr += ")\n";
			}

			return false;
		}

		public Boolean BinaryRelationCB(String relName,
                                   FeatureNode srcNode, FeatureNode tgtNode)
		{
			FeatureNode srcName = srcNode.get("name");
			if (srcName == null) return false;
			FeatureNode tgtName = tgtNode.get("name");
			if (tgtName == null)
			{
				outstr += "; Error: No target! rel=" + relName +
				          " and src=" + srcName + "\n";
				return false;
			}

			// nameSource might be NULL if theres a bug in an algs file.
			// Currently, "She likes more pasta." triggers this, due to
			// a bug in compartive processing (no target for _$crVar)
			FeatureNode srcRaw = srcNode.get("nameSource");
			if (null == srcRaw) return false;
			FeatureNode tgtRaw = tgtNode.get("nameSource");
			if (null == tgtRaw) return false;

			outstr += "; " + relName + " (" + srcName + ", " + tgtName + ") \n";
			String src_guid = srcRaw.get("uuid").getValue();
			String tgt_guid = tgtRaw.get("uuid").getValue();
			src_guid = src_guid.replaceAll("\"", "\\\\\"");
			tgt_guid = tgt_guid.replaceAll("\"", "\\\\\"");

			char underscore = relName.charAt(0);

			outstr += "(EvaluationLink (stv 1.0 1.0)\n";
			if ('_' == underscore)
			{
				outstr += "   (DefinedLinguisticRelationshipNode \"";
			}
			else
			{
				outstr += "   (PrepositionalRelationshipNode \"";
			}
			outstr += relName + "\")\n";
			outstr += "   (ListLink\n";
			outstr += "      (WordInstanceNode \"" + src_guid + "\")\n";
			outstr += "      (WordInstanceNode \"" + tgt_guid + "\")\n";
			outstr += "   )\n";
			outstr += ")\n";

			return false;
		}
		public Boolean BinaryHeadCB(FeatureNode from)
		{
			return false;
		}
	}

	public String printRelations()
	{
		prtRelation prt = new prtRelation();
		parse.foreach(prt);
		return prt.outstr;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Print the word referents.
	 */
	private String printWordRefs()
	{
		String refs = "";
		FeatureNode fn = parse.getLeft();
		fn = fn.get("NEXT");
		int word_index = 1;
		while (fn != null)
		{
			// Some words don't have a lemma form.  This occurs when
			// the algs have concatenated multiple words into one:
			// for example: "I bank at Sun Trust Bank" -- the G links
			// result in "Bank" having the lemma "Sun_Trust_Bank" while
			// neither Sun nor Trust have a lemma form.
			FeatureNode lemode = fn.get("str");
			if (null != lemode)
			{
				String lemma = lemode.getValue();
				// Remember the word-to guid map; we'll need it for
				// printing relations, and printing frames,
				String guid_word = fn.get("uuid").getValue();
				guid_word = guid_word.replaceAll("\"", "\\\\\"");

				// The word instance, and its associated lemma form
				refs += "(LemmaLink (stv 1.0 1.0)\n";
				refs += "   (WordInstanceNode \"" + guid_word + "\")\n";
				refs += "   (WordNode \"" + lemma + "\"))\n";
			}
			fn = fn.get("NEXT");
			word_index ++;
		}

		return refs;
	}

	/* ----------------------------------------------------------- */

	public String toString()
	{
		String ret = "";
		ret += printWordRefs();
		ret += printRelations();
		return ret;
	}

} // end RelScheme
