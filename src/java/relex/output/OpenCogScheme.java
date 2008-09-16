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
import java.util.UUID;

import relex.ParsedSentence;
import relex.RelexInfo;
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
	private RelexInfo ri = null;
	private ParsedSentence parse = null;
	private String orig_sentence = null;

	private ArrayList<String> word_list = null;
	private HashMap<FeatureNode,String> id_map = null;
	private OpenCogSchemeLink link_scheme;
	private OpenCogSchemeRel rel_scheme;
	private OpenCogSchemeFrame frame_scheme;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	public OpenCogScheme()
	{
		rel_scheme = new OpenCogSchemeRel();
		link_scheme = new OpenCogSchemeLink();
		frame_scheme = new OpenCogSchemeFrame();
	}

	public void setParse(ParsedSentence _parse)
	{
		parse = _parse;
		if (parse.getRI() != ri)
		{
			ri = parse.getRI();
			orig_sentence = printWords();
			orig_sentence += printSentence();
		}

		link_scheme.setParse(parse, word_list);
		id_map = new HashMap<FeatureNode,String>();
		rel_scheme.setParse(parse, word_list, id_map);
		frame_scheme.setParse(parse, id_map);
	}

	/* -------------------------------------------------------------------- */
	public String toString()
	{
		String ret = "";

		ret += orig_sentence;
		ret += link_scheme.toString();
		ret += rel_scheme.toString();
		// ret += frame_scheme.toString();

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
			UUID guid = UUID.randomUUID();
			String guid_word = word + "@" + guid;
			word_list.add(guid_word);

			str += "(ReferenceLink\n" +
			       "   (ConceptNode \"" + guid_word + "\")\n" +
			       "   (WordNode \"" + word + "\")\n" +
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
		String str = "(SentenceLink\n" +
		             "   (SentenceNode \"" + ri.getID() + "\")\n"; 

		for (int i=1; i<word_list.size(); i++)
		{
			str += "   (ConceptNode \"" + word_list.get(i) + "\")\n"; 
		}

		str += ")\n";
		return str;
	}

} // end OpenCogScheme
