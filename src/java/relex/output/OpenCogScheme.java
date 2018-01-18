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
import java.util.HashSet;

import relex.Document;
import relex.ParsedSentence;
import relex.Sentence;
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
	private boolean do_show_linkage = false;
	private boolean do_show_relex = false;
	private int seqno = 1;
	private HashSet<String> previous_words;
	private HashSet<String> previous_sents;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	public OpenCogScheme()
	{
		rel_scheme = new OpenCogSchemeRel();
		link_scheme = new OpenCogSchemeLink();
		orig_sentence = "";
		previous_words = new HashSet<String>();
		previous_sents = new HashSet<String>();
	}

	public void setShowLinkage(boolean t) { do_show_linkage = t; }
	public boolean getShowLinkage() { return do_show_linkage; }

	public void setShowRelex(boolean t) { do_show_relex = t; }
	public boolean getShowRelex() { return do_show_relex; }

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

		// Don't repeat the orig sentence, until we get a new sentence.
		orig_sentence = "";
		return ret;
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

			// We MUST escape all quotation marks! If we don't, then
			// we are just generating bad scheme.
			word = word.replaceAll("\"", "\\\\\"");
			String guid_word = fn.get("uuid").getValue();
			guid_word = guid_word.replaceAll("\"", "\\\\\"");

			if (word.equals("LEFT-WALL"))
				word = "###LEFT-WALL###";

			if (word.equals("RIGHT-WALL"))
				word = "###RIGHT-WALL###";

			str += "(ReferenceLink (stv 1.0 1.0)\n" +
			       "   (WordInstanceNode \"" + guid_word + "\")\n" +
			       "   (WordNode \"" + word + "\")\n" +
			       ")\n";

			str += "(WordInstanceLink (stv 1.0 1.0)\n" +
			       "   (WordInstanceNode \"" + guid_word + "\")\n" +
			       "   (ParseNode \"" + _parse.getIDString() + "\")\n" +
			       ")\n";

			// If we've never printed the sequence number for this word
			// before, do so now.  Opencog uses this to determine the
			// order in which the words appear in a sentence.
			if (!previous_words.contains(guid_word))
			{
				str += "(WordSequenceLink (stv 1.0 1.0)\n" +
				       "     (WordInstanceNode \"" + guid_word + "\")\n" +
				       "     (NumberNode \"" + getSeqNo() + "\")\n" +
				       ")\n";
				previous_words.add(guid_word);
			}

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

		String sent_id = _parse.getSentence().getID();

		String str = "(ParseLink (stv 1 1)\n" +
		             "   (ParseNode \"" + _parse.getIDString() +
			          "\"(stv 1.0 " + scf + "))\n" +
		             "   (SentenceNode \"" + sent_id + "\")\n" +
		             ")\n";

		// If we haven't seen this sentence before, then issue a
		// sequence number for this sentence. This is used by opencog
		// to determine teh order in which sentences were seen.
		if (!previous_sents.contains(sent_id))
		{
			str += "(SentenceSequenceLink (stv 1 1)\n" +
			       "	(SentenceNode \"" + sent_id + "\")\n" +
			       "	(NumberNode \"" + getSeqNo() + "\")\n" +
			       ")\n";
			previous_sents.add(sent_id);
		}
		return str;
	}

	/**
	 * Print the original document, as made up out of sentences,
	 * maintaining the proper sentence order in the document.
	 */
	public String printDocument(Document doco)
	{
		String doco_id = doco.getID();
		String str = "";

		ArrayList<Sentence> sentence_list = doco.getSentences();
		for (int i=0; i<sentence_list.size(); i++)
		{
			String sent_id = sentence_list.get(i).getID();
			str += "(SentenceLink (stv 1 1)\n" +
			       "   (SentenceNode \"" + sent_id + "\")\n" +
			       "   (DocumentNode \"" + doco_id + "\")\n" +
			       ")\n";

			// If we haven't seen this sentence before, then issue a
			// sequence number for this sentence. This is used by opencog
			// to determine teh order in which sentences were seen.
			if (!previous_sents.contains(sent_id))
			{
				str += "(SentenceSequenceLink (stv 1 1)\n" +
				       "	(SentenceNode \"" + sent_id + "\")\n" +
				       "	(NumberNode \"" + getSeqNo() + "\")\n" +
				       ")\n";
				previous_sents.add(sent_id);
			}
		}

		return str;
	}

	public int getSeqNo()
	{
		return seqno++;
	}

} // end OpenCogScheme
