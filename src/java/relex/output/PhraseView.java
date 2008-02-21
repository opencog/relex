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

/**
 * A PhraseView object displays a ParesedSentence in
 * terms of its consitituent phrases.  The output
 * format can be choosen to be "plain" human-readable
 * output, and XML output.
 *
 * This class is a part of the Cerego graph visualization
 * project.
 *
 * This class makes heavy use of String. If performance needs to be
 * improved, then a conversion to StringBuff should be considered.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */
class PhraseView {

	// The sentence being examined.
	private ParsedSentence sent;

	// Choice of output: plain or XML
	private Boolean do_show_XML;

	// Used for recursive enumeration of the phrases
	private Integer idx;

	RelationView relview;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public PhraseView()
	{
		sent = null;
		relview = new RelationView();
		showXML(false);
	}

	public void setParse(ParsedSentence s) {
		sent = s;
	}

	/**
	 * If argument is set to true, then this class will
	 * output the XML-style markup.
	 */
	public void showXML(Boolean flag)
	{
		do_show_XML = flag;
		if (do_show_XML)
			XMLMarkup();
		else
			PlainMarkup();
	}

	private String phr_markup_start;
	private String phr_markup_end;
	private HashMap<String,String> phr_type_map;

	/*
	 * Do setup for generating XML output
	 */
	private void XMLMarkup()
	{
		relview.showWord(false);
		phr_markup_start = "   <phrases>\n";
		phr_markup_end = "   </phrases>\n\n";

		phr_type_map = new HashMap<String,String>();
		phr_type_map.put("ADJP",   "adjective");
		phr_type_map.put("ADVP",   "adverb");
		phr_type_map.put("NP",     "noun");
		phr_type_map.put("PP",     "prepositional");
		phr_type_map.put("PRT",    "particle");
		phr_type_map.put("QP",     "quantifier");
		phr_type_map.put("S",      "clause");
		phr_type_map.put("SBAR",   "subordinate");
		phr_type_map.put("SINV",   "inverted");
		phr_type_map.put("TOP",    "root");
		phr_type_map.put("VP",     "verb");
		phr_type_map.put("WHADVP", "wh-adverb");
		phr_type_map.put("WHNP",   "wh-noun");
		phr_type_map.put("WHPP",   "wh-prep");
	}

	/*
	 * Do setup for generating human-readable output
	 */
	private void PlainMarkup()
	{
		relview.showWord(true);
		phr_markup_start = "Phrase Structure:\n";
		phr_markup_end = "\n";

		phr_type_map = new HashMap<String,String>();
		phr_type_map.put("ADJP",   "Adjectival Phrase");
		phr_type_map.put("ADVP",   "Adverbial Phrase");
		phr_type_map.put("NP",     "Noun Phrase");
		phr_type_map.put("PP",     "Prepositional Phrase");
		phr_type_map.put("PRT",    "Particle");
		phr_type_map.put("QP",     "Quantifier Phrase");
		phr_type_map.put("S",      "Clause");
		phr_type_map.put("SBAR",   "Subordinate Clause");
		phr_type_map.put("SINV",   "Subject Inverted");
		phr_type_map.put("TOP",    "Sentence");
		phr_type_map.put("VP",     "Verb Phrase");
		phr_type_map.put("WHADVP", "Wh-Adverb Phrase");
		phr_type_map.put("WHNP",   "Wh-Noun Phrase");
		phr_type_map.put("WHPP",   "Wh-Prepositional Phrase");
	}

	/* -------------------------------------------------------------------- */
	/**
	 * assignIndex() -- add a "phr-idx" feature node to graph.
	 *
	 * The routine adds a unique index id to each node
	 * of the phrase tree.  Needed for identifying the nodes.
	 */
	private void assignIndex (FeatureNode head)
	{
		FeatureNode fn = head.get("phr-head");
		Integer cidx = idx;
		idx ++;

		fn.set("phr-idx", new FeatureNode(cidx.toString()));

		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) {
				assignIndex(fn);
			}
			fn = fn.get("phr-next");
		}
	}

	/* -------------------------------------------------------------------- */

	protected String __prtIndex (FeatureNode head, Boolean hide_word)
	{
		String str = "";
		String slv = "";
		FeatureNode fn = head.get("phr-head");

		if (hide_word) relview.showWordOnly(false);
		else relview.showWordOnly(true);

		Boolean first = true;
		while (fn != null)
		{
			FeatureNode word = fn.get("phr-word");
			if (word != null)
			{
				// Add spaces between words
				if (first) first = false;
				else str += " ";

				str += relview.get_one_orig_word(word);
			}
			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				// Add spaces between words
				if (first) first = false;
				else str += " ";

				// Get the phrase index
				if (hide_word) {
					FeatureNode fidx = subf.get("phr-idx");
					str += fidx.getValue();
				} else {
					str += __prtIndex(fn, false);
				}
			}
			fn = fn.get("phr-next");
		}

		return slv + str;
	}

	private String _prtOnePhrase (FeatureNode head, Boolean top)
	{
		String str = "";
		FeatureNode fn = head.get("phr-head");

		FeatureNode phrt = fn.get("phr-type");
		String spt = phrt.getValue();

		if (top && spt.equals("S")) spt = "TOP";
		spt = phr_type_map.get(spt);

		FeatureNode phri = fn.get("phr-idx");
		String spi = phri.getValue();

		FeatureNode phrl = fn.get("phr-leader");

		if (do_show_XML)
			relview.showWord(false);
		else
			relview.showWord(true);

		String leader = relview.get_orig_word(phrl);

		if (do_show_XML)
		{
			str += "      <phrase type=\"" + spt + "\" id=\"" + spi;
			if (phrl != null) {
				str += "\" head=\"" + leader;
			}
			str += "\" parts=\"";
			str += __prtIndex(head, true);
			str += "\">";
			str += __prtIndex(head, false);
			str += "</phrase>\n";
		}
		else
		{
			str += "   " + spi + " = " + spt + " (";
			str += __prtIndex(head, false) + ")";
			if (phrl != null) {
				str += "  -- Head=" + leader;
			}
			str += "\n";
		}
		return str;
	}

	/**
	 * Traverse, and print all phrases
	 */
	private String prtPhrases (FeatureNode head, Boolean top)
	{
		String str = "";
		String slv = "";
		FeatureNode fn = head.get("phr-head");

		str += _prtOnePhrase (head, top);
		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				// Queue up output from leaves and lower parts of the tree.
				slv += prtPhrases(fn, false);
			}
			fn = fn.get("phr-next");
		}
		return slv + str;
	}

	/**
	 * Assign an id number to each subphrase
	 * of a sentence.
	 */
	public String printPhraseIndex()
	{
		FeatureNode head = sent.getLeft();
		idx = 1000;

		assignIndex(head);
		String str = phr_markup_start;
		str += prtPhrases (head, true);
		str += phr_markup_end;
		return str;
	}

} // end PhraseView

