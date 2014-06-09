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

import relex.Document;
import relex.ParsedSentence;
import relex.Sentence;
import relex.anaphora.history.SentenceHistory;
import relex.feature.FeatureNode;

/**
 * The OpenCogScheme object outputs a ParsedSentence in the
 * OpenCog-style Scheme format. The actual format used, and its rationale,
 * is described in greater detail in the opencog wiki page
 * http://wiki.opencog.org/w/RelEx_OpenCog_format
 *
 * See also the README file in 
 * https://github.com/opencog/opencog/tree/master/opencog/nlp/wsd
 *
 * This class prints just one parse at a time.
 *
 * Copyright (c) 2007, 2008, 2013, 2014 Linas Vepstas <linas@linas.org>
 */
public class OpenCogScheme
{
	private ParsedSentence _parse = null;
	private String orig_sentence = null;

	private OpenCogSchemeLink link_scheme;
	private OpenCogSchemeRel rel_scheme;
	private OpenCogSchemeAnaphora anaphora_scheme;
	private boolean do_show_linkage = false;
	private boolean do_show_relex = false;
	private boolean do_show_anaphora = false;
	private int seqno = 1;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	public OpenCogScheme()
	{
		rel_scheme = new OpenCogSchemeRel();
		link_scheme = new OpenCogSchemeLink();
		anaphora_scheme = new OpenCogSchemeAnaphora();
		orig_sentence = "";
	}

	public void setShowLinkage(boolean t) { do_show_linkage = t; }
	public boolean getShowLinkage() { return do_show_linkage; }

	public void setShowRelex(boolean t) { do_show_relex = t; }
	public boolean getShowRelex() { return do_show_relex; }

	public void setShowAnaphora(boolean flag) { do_show_anaphora = flag; }
	public boolean getShowAnaphora() { return do_show_anaphora; }

	/**
	 * Set the parse that is to be printed. After setting this, call
	 * the toString() method to get a string representation of the
	 * sentence.
	 */
	public void setParse(ParsedSentence parse)
	{
		_parse = parse;

		orig_sentence += printWords();
		orig_sentence += printSentence();

		link_scheme.setParse(_parse);
		rel_scheme.setParse(_parse);

		anaphora_scheme.clear();
		anaphora_scheme.setSentence(_parse);
	}

	/* -------------------------------------------------------------------- */

	/**
	 * Return the the string representation of the parse, previously
	 * specified with setParse().
	 */
	public String toString()
	{
		String ret = "";

		ret += orig_sentence;

		if (do_show_linkage) ret += linkSchemeToString();
		if (do_show_relex) ret += relSchemeToString();
		if (do_show_anaphora) ret += anaphoraSchemeToString();

		// Don't repeat the orig sentence, until we get a new sentence.
		orig_sentence = "";
		return ret;
	}

	public String anaphoraSchemeToString() {
		return anaphora_scheme.toString();
	}

	public String relSchemeToString() {
		return rel_scheme.toString();
	}

	public String linkSchemeToString() {
		return link_scheme.toString();
	}

	/* -------------------------------------------------------------------- */

	/**
	 * Print the word instances of the original sentence, associating
	 * each instance to its WordNode.
	 */
	public String printWords()
	{
		String str = "";

		FeatureNode fn = _parse.getLeft();
		while (fn != null)
		{
			String word = fn.get("orig_str").getValue();
			String guid_word = fn.get("uuid").getValue();
			if (word.equals("LEFT-WALL"))
				word = "###LEFT-WALL###";

			// XXX FIXME -- this is a bit of a crazy hack that attempts
			// to handle numbers, but does it wrong.  Why is this wrong?
			// 1) because Europeans use a comma for a decimal, and a period
			//    for a thousands separator
			// 2) Because americans use a comma for a thousands separator,
			// 3) This fails to handle number words, like "one two three"
			// 4) This fails to handle time, dates, etc.
			// In my opinion, the below whould be removed, and higher
			// mathematics should be handled in Opencog, and not here...
			if (word.matches("[-+]?[0-9]*?\\.?[0-9]+"))
			{
				str += "(ReferenceLink (stv 1.0 1.0)\n" +
						"   (WordInstanceNode \"" + guid_word + "\")\n" +
						"   (NumberNode " + word + ")\n" +
						")\n";
			}
			else 
			{
				str += "(ReferenceLink (stv 1.0 1.0)\n" +
					"   (WordInstanceNode \"" + guid_word + "\")\n" +
					"   (WordNode \"" + word + "\")\n" +
					")\n"; 
			}

			str += "(WordInstanceLink (stv 1.0 1.0)\n" +
					"   (WordInstanceNode \"" + guid_word + "\")\n" +
					"   (ParseNode \"" + _parse.getIDString() + "\")\n" +
					")\n";

			str += "(WordSequenceLink (stv 1.0 1.0)\n" +
					"     (WordInstanceNode \"" + guid_word + "\")\n" +
					"     (NumberNode \"" + getSeqNo() + "\")\n" +
					")\n";

			fn = fn.get("NEXT");
		}
		return str;
	}

	/**
	 * Print a parseLink that attaches a specific parse to the sentence
	 * that its a part of.  Attach a truth value that represents the 
	 * parse ranking for the parse.
	 */
	public String printSentence()
	{
		Double confidence = _parse.getTruthValue().getConfidence();

		// Why the fuck is string formating so fucked in Java? WTF?
		String scf = confidence.toString();
		int strl = Math.min(6, scf.length());
		scf = scf.substring(0, strl);

		String str = "(ParseLink (stv 1 1)\n" +
					"   (ParseNode \"" + _parse.getIDString() +
					    "\"(stv 1.0 " + scf + "))\n" +
				   "   (SentenceNode \"" + _parse.getSentence().getID() + "\")\n" +
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

	/**
	 * Sets the history for the AnaphoraResolutionScheme
	 * @param history the history to be used for anaphora resolution
	 */
	public void setAnaphoraHistory(SentenceHistory history)
	{
		anaphora_scheme.setHistory(history);
	}

	public int getSeqNo()
	{
		return seqno++;
	}

} // end OpenCogScheme
