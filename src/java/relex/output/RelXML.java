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
import relex.feature.FeatureForeach;

/**
 * The RelXML object outputs a ParsedSentence in 
 * the Novamente OpenCog-style XML
 *
 * As the same sentence can have multiple parses, this
 * class only displays a single, particular parse.
 *
 * This class makes heavy use of String. If performance needs to be
 * improved, then a conversion to StringBuff should be considered.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
class RelXML
{
	// The sentence being examined.
	private ParsedSentence sent;

	private HashMap<String,String> id_map = null;

	/* ----------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public RelXML()
	{
		sent = null;
	}

	public void setParse(ParsedSentence s, HashMap<String,String> im)
	{
		sent = s;
		id_map = im;
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

			outstr += "<!-- " + attrName + " (" + srcName + ", " + value + ") -->\n";
			String guid = id_map.get(srcName);

			// Flags are assumed to be true, so value is the flag name.
			if (attrName.endsWith("-FLAG"))
				value = attrName.replaceAll("-FLAG","").toLowerCase();
			if (attrName.equals("HYP"))
				value = attrName.toLowerCase();

			outstr += "  <DefinedLinguisticConceptNode name=\"#" + value + "\"/>\n";

			outstr += "  <InheritanceLink>\n";
			outstr += "    <Element class=\"DefinedLinguisticConceptNode\" name=\"#" + value + "\"/>\n";
			outstr += "    <Element class=\"ConceptNode\" name=\"" + guid + "\"/>\n";
			outstr += "  </InheritanceLink>\n";

			// Make a note of the value, it is needed for frame printing.
			id_map.put(value, "#" + value);
			return false;
		}

		public Boolean BinaryRelationCB(String relName,
                                   FeatureNode srcNode, FeatureNode tgtNode)
		{
			FeatureNode srcName = srcNode.get("name");
			if (srcName == null) return false;
			FeatureNode tgtName = tgtNode.get("name");
			if (tgtName == null) return false;

			outstr += "<!-- " + relName + " (" + srcName + ", " + tgtName + ") -->\n";
			String src_guid = id_map.get(srcName.getValue());
			String tgt_guid = id_map.get(tgtName.getValue());


			outstr += "  <DefinedLinguisticRelationshipNode name=\"" + relName + "\"/>\n";

			outstr += "  <EvaluationLink>\n";
			outstr += "    <Element class=\"DefinedLinguisticRelationshipNode\" name=\"" + relName + "\"/>\n";
			outstr += "    <ListLink>\n";
			outstr += "      <Element class=\"ConceptNode\" name=\"" + src_guid + "\"/>\n";
			outstr += "      <Element class=\"ConceptNode\" name=\"" + tgt_guid + "\"/>\n";
			outstr += "    </ListLink>\n";
			outstr += "  </EvaluationLink>\n";

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
		FeatureForeach.foreach(sent.getLeft(), prt);
		return prt.outstr;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Print the word referents, Novamente-style.
	 */
	private String printWordRefs()
	{
		String refs = "";
		int numWords = sent.getNumWords();
		for (int i = 1; i < numWords; i++) {
			FeatureNode fn = sent.getWordAsNode(i);

			// There is no "name" for the given index, if it was
			// merged into an entity. For example "New York", the
			// word "New" will not have an orig_str, while that for
			// "York" will be "New_York".
			if (fn == null) continue;
			fn = fn.get("ref");
			if (fn == null) continue;
			fn = fn.get("name");
			if (fn == null) continue;

			String word = fn.getValue();

			// The word node proper, the concept for which it stands, and a link.
			refs += "  <WordNode name=\"" + word + "\"/>\n";
			refs += "  <ConceptNode name=\"" + word + "_1\"/>\n";
			refs += "  <WRLink>\n";
			refs += "    <Element class=\"WordNode\" name=\"" + word + "\"/>\n";
			refs += "    <Element class=\"ConceptNode\" name=\"" + word + "_1\"/>\n";
			refs += "  </WRLink>\n";

			// The word instance
			refs += word_instance(word);
		}

		return refs;
	}

	private String word_instance(String word)
	{
		// An instance of this concept. Assign a UUID to this instance.
		UUID guid = UUID.randomUUID();
		String guid_name = word + "_" + guid;

		// Remember the word-to guid map; we'll need it for later
		// in this sentence.
		id_map.put(word, guid_name);

		// Write out a unique instance of each general word.
		String refs = "";
		refs += "  <ConceptNode name=\"" + word + "_" + guid + "\"/>\n";
		refs += "  <InheritanceLink>\n";
		refs += "    <Element class=\"ConceptNode\" name=\"" + guid_name + "\"/>\n";
		refs += "    <Element class=\"ConceptNode\" name=\"" + word + "_1\"/>\n";
		refs += "  </InheritanceLink>\n";
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

} // end RelXML

