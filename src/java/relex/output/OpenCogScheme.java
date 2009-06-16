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
import java.util.HashMap;

import relex.Document;
import relex.ParsedSentence;
import relex.Sentence;
import relex.feature.FeatureNode;

/**
 * The OpenCogScheme object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its rationale,
 * is described in greater detail in the README file in the opencog
 * source code directory src/nlp/wsd/README.
 *
 * As the same sentence can have multiple parses, this class only
 * displays a single, particular parse.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
public class OpenCogScheme
{
	private ParsedSentence parse = null;
	private String orig_sentence = null;

	private ArrayList<String> word_list = null;
	private HashMap<FeatureNode,String> id_map = null;
	private HashMap<String,String> uuid_to_base_map = null;
	private OpenCogSchemeLink link_scheme;
	private OpenCogSchemeRel rel_scheme;
	private OpenCogSchemeFrame frame_scheme;
	private OpenCogSchemeAnaphora anaphora_scheme;
	private boolean link_on = false;
	private boolean relex_on = false;
	private boolean frame_on = false;
	private boolean anaphora_on = false;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	public OpenCogScheme()
	{
		rel_scheme = new OpenCogSchemeRel();
		link_scheme = new OpenCogSchemeLink();
		frame_scheme = new OpenCogSchemeFrame();
		anaphora_scheme = new OpenCogSchemeAnaphora();
		orig_sentence = "";
	}

	public void setFrameOn(boolean t) { frame_on = t; }
	public boolean getFrameOn() { return frame_on; }

	public void setLinkOn(boolean t) { link_on = t; }
	public boolean getLinkOn() { return link_on; }

	public void setRelExOn(boolean t) { relex_on = t; }
	public boolean getRelExOn() { return relex_on; }

	public void setAnaphoraOn(boolean flag) { anaphora_on = flag; }
	public boolean getAnaphoraOn() { return anaphora_on; }

	public void setParse(ParsedSentence _parse)
	{
		parse = _parse;

		parse.addWordUUIDs();

		orig_sentence += printWords();
		orig_sentence += printSentence();

		link_scheme.setParse(parse, word_list);

		id_map = new HashMap<FeatureNode,String>();
		uuid_to_base_map = new HashMap<String,String>();
		rel_scheme.setParse(parse, word_list, id_map, uuid_to_base_map);
		frame_scheme.setParse(parse, id_map, uuid_to_base_map);

		anaphora_scheme.clear();
		anaphora_scheme.setSentence(parse, word_list);
	}

	/* -------------------------------------------------------------------- */
	public String toString()
	{
		String ret = "";

		ret += orig_sentence;

		if (link_on) ret += link_scheme.toString();
		if (relex_on) ret += rel_scheme.toString();
		if (frame_on) ret += frame_scheme.toString();
		if (anaphora_on) ret += anaphora_scheme.toString(); 
		
		// Don't repeat the orig sentence, until we get a new sentence.
		orig_sentence = "";
		return ret;
	}

	/* -------------------------------------------------------------------- */

	/**
	 * Print the word instances of the original sentence, associating
	 * each instance to its WordNode.
	 */
	public String printWords()
	{
		String str = "";
		word_list = new ArrayList<String>();
		word_list.add("LEFT-WALL");

		FeatureNode fn = parse.getLeft();
		fn = fn.get("NEXT");
		while (fn != null)
		{
			String word = fn.get("orig_str").getValue();
			String guid_word = fn.get("uuid").getValue();
			word_list.add(guid_word);

			str += "(ReferenceLink (stv 1.0 1.0)\n" +
			       "   (WordInstanceNode \"" + guid_word + "\")\n" +
			       "   (WordNode \"" + word + "\")\n" +
			       ")\n";

			str += "(WordInstanceLink (stv 1.0 1.0)\n" +
			       "   (WordInstanceNode \"" + guid_word + "\")\n" +
			       "   (ParseNode \"" + parse.getIDString() + "\")\n" +
			       ")\n";

			fn = fn.get("NEXT");
		}
		return str;
	}

	/**
	 * Print the original sentence, as made up out of word instances,
	 * maintaining the proper word order in the sentence.
	 */
	public String printSentence()
	{
		String str = "(ReferenceLink (stv 1.0 1.0)\n" +
		             "   (ParseNode \"" + parse.getIDString() + "\")\n" +
		             "   (ListLink\n";

		// Loop starts at 1, since we skip LEFT-WALL
		for (int i=1; i<word_list.size(); i++)
		{
			str += "      (WordInstanceNode \"" + word_list.get(i) + "\")\n";
		}

		str += "   )\n" +
		       ")\n";
		return str;
	}

	/**
	 * Print the original document, as made up out of sentences,
	 * maintaining the proper sentence order in the document.
	 */
	public String printDocument(Document doco)
	{
		String str = "(ReferenceLink (stv 1.0 1.0)\n" +
		             "   (DocumentNode \"" + doco.getID() + "\")\n" +
		             "   (ListLink\n";

		ArrayList<Sentence> sentence_list = doco.getSentences();
		for (int i=0; i<sentence_list.size(); i++)
		{
			str += "      (SentenceNode \"" + 
			       sentence_list.get(i).getID() + "\")\n";
		}

		str += "   )\n" +
		       ")\n";
		return str;
	}

} // end OpenCogScheme
