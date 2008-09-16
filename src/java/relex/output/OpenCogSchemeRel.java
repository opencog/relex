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
import java.util.UUID;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.RelationCallback;

/**
 * The OpenCogSchemeRel object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its rationale,
 * is described in greater detail in the README file in the opencog
 * source code directory src/nlp/wsd/README.
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

	// Map associating a feature-node to a unique ID string.
	private HashMap<FeatureNode,String> word_id_map = null;
	private HashMap<FeatureNode,String> lemma_id_map = null;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public OpenCogSchemeRel()
	{
		parse = null;
	}

	public void setParse(ParsedSentence s, HashMap<FeatureNode,String> im)
	{
		parse = s;
		lemma_id_map = im;
		word_id_map = new  HashMap<FeatureNode,String>(); // XXXX
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
			String guid = lemma_id_map.get(srcNode);

			// Flags are assumed to be true, so value is the flag name.
			if (attrName.endsWith("-FLAG"))
				value = attrName.replaceAll("-FLAG","").toLowerCase();
			if (attrName.equals("HYP"))
				value = attrName.toLowerCase();

			// Special treatment for part-of-speech.
			String link_start = "(InheritanceLink\n";
			String link_end   = ")\n";
			if (attrName.equals("pos"))
			{
				link_start = "(PartOfSpeechLink\n";
				link_end   = ")\n";
			}

			// All of the other cases.
			outstr += link_start;
			outstr += "   (ConceptNode \"" + guid + "\")\n";
			outstr += "   (DefinedLinguisticConceptNode \"" + value + "\")\n";
			outstr += link_end;

			return false;
		}

		public Boolean BinaryRelationCB(String relName,
                                   FeatureNode srcNode, FeatureNode tgtNode)
		{
			FeatureNode srcName = srcNode.get("name");
			if (srcName == null) return false;
			FeatureNode tgtName = tgtNode.get("name");
			if (tgtName == null) return false;

			outstr += "; " + relName + " (" + srcName + ", " + tgtName + ") \n";
			String src_guid = lemma_id_map.get(srcNode);
			String tgt_guid = lemma_id_map.get(tgtNode);

			outstr += "(EvaluationLink\n";
			outstr += "   (DefinedLinguisticRelationshipNode \"" + relName + "\")\n";
			outstr += "   (ListLink\n";
			outstr += "      (ConceptNode \"" + src_guid + "\")\n";
			outstr += "      (ConceptNode \"" + tgt_guid + "\")\n";
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
	private String getstr(FeatureNode node)
	{
		if (null ==  node) {
			return "";
		} else {
			return node.getValue();
		}
	}

	/**
	 * Print the word referents.
	 */
	private String printWordRefs()
	{
		String parse_id = parse.getIDString();
		String refs = "";
		FeatureNode fn = parse.getLeft();
		fn = fn.get("NEXT");
		while (fn != null)
		{
			String word = getstr(fn.get("orig_str"));
			String lemma = getstr(fn.get("str"));

			// A unique UUID for each word instance.
			UUID guid = UUID.randomUUID();
			String guid_word = word + "@" + guid;
			guid = UUID.randomUUID();
			String guid_lemma = lemma + "@" + guid;

			// Remember the word-to guid map; we'll need it for later
			// in this sentence.
			word_id_map.put(fn, guid_word);

			FeatureNode refNode = fn.get("ref");
			if (refNode != null)
			{
				lemma_id_map.put(refNode, guid_lemma);
			}

			// The word node proper, the concept for which it stands, and a link.
			refs += "(ReferenceLink\n";
			refs += "   (ConceptNode \"" + guid_lemma + "\")\n";
			refs += "   (WordNode \"" + lemma + "\")\n";
			refs += ")\n";

			refs += "(LemmaLink\n";
			refs += "   (ConceptNode \"" + guid_word + "\")\n";
			refs += "   (ConceptNode \"" + guid_lemma + "\")\n";
			refs += ")\n";

			refs += "(ParseInstanceLink\n";
			refs += "   (ConceptNode \"" + guid_lemma + "\")\n";
			refs += "   (ConceptNode \"" + parse_id + "\")\n";
			refs += ")\n";

			fn = fn.get("NEXT");
		}

		return refs;
	}

	/* ----------------------------------------------------------- */

	private String printRank()
	{
		String ret = 
		"(ParseLink\n" +
		"   (ConceptNode \"" + parse.getIDString() + "\" (stv 1.0 "; 

		Double confidence = parse.getTruthValue().getConfidence();
		ret += confidence.toString().substring(0,6) + "))\n" +
		"   (SentenceNode \"" + parse.getRI().getID() + "\")\n)\n";
		return ret;
	}

	/* ----------------------------------------------------------- */

	public String toString()
	{
		String ret = "";
		ret += printRank();
		ret += printWordRefs();
		ret += printRelations();
		return ret;
	}

} // end RelScheme

