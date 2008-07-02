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

/**
 * A ParseView object displays a ParsedSentence in either
 * a plain-text markup, or as XML output.
 * As the same sentence can have multiple parses, this
 * class only displays a single, particular parse.
 *
 * The name of this class is likely to change to some better name,
 * suggestions welcome.
 *
 * This class makes heavy use of String. If performance needs to be
 * improved, then a conversion to StringBuff should be considered.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
public class ParseView {

	// The sentence being examined.
	private ParsedSentence sent;

	// Choice of output: plain or XML
	private Boolean do_show_XML;

	RelationView relview;

	private HashMap<String,String> id_map;
	private Integer string_id;

	private HashMap<String,String> pos_map;
	private HashMap<String,String> def_map;
	private HashMap<String,String> tns_map;
	private HashMap<String,String> num_map;
	private HashMap<String,String> gen_map;
	private HashMap<String,String> qry_map;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public ParseView()
	{
		sent = null;

		relview = new RelationView();

		initMaps();
		showXML(false);
	}

	private void initMaps()
	{
		string_id = 0;
		id_map = null;

		// Part of Speech remapping
		pos_map = new HashMap<String,String>();
		pos_map.put("adj",  "adjective");
		pos_map.put("adv",  "adverb");
		pos_map.put("det",  "determiner");
		pos_map.put("prep", "preposition");
		pos_map.put("WORD", "word");

		// some things don't change in ascii, but do get ids
		pos_map.put("noun",        "noun");
		pos_map.put("verb",        "verb");
		pos_map.put("particle",    "particle");
		pos_map.put("punctuation", "punctuation");

		// Quantification remapping
		def_map = new HashMap<String,String>();
		def_map.put("T", "definite");

		// Tense remapping
		tns_map = new HashMap<String,String>();
		tns_map.put("future",              "future");
		tns_map.put("future_progressive",  "future progressive");
		tns_map.put("imperative",          "imperative");
		tns_map.put("infinitive",          "infinitive");
		tns_map.put("past",                "past");
		tns_map.put("past_infinitive",     "past infinitive");
		tns_map.put("past_progressive",    "past progressive");
		tns_map.put("perfect",             "perfect");
		tns_map.put("progressive",         "progressive");
		tns_map.put("present",             "present");
		tns_map.put("present_progressive", "present progressive");

		// Number remapping
		num_map = new HashMap<String,String>();
		num_map.put("singular",    "singular");
		num_map.put("plural",      "plural");
		num_map.put("uncountable", "uncountable");

		// Gender remapping
		gen_map = new HashMap<String,String>();
		gen_map.put("masculine", "masculine");
		gen_map.put("feminine",  "feminine");
		gen_map.put("person",    "person");
		gen_map.put("neuter",    "neuter");

		// Query remapping
		qry_map = new HashMap<String,String>();
		qry_map.put("how",   "how");
		qry_map.put("what",  "what");
		qry_map.put("when",  "when");
		qry_map.put("where", "where");
		qry_map.put("which", "which");
		qry_map.put("who",   "who");
		qry_map.put("why",   "why");
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

	/* -------------------------------------------------------------------- */
	/* Word-index printing format */
	private String sentence_start;
	private String sentence_end;
	private String word_idx_start;
	private String word_idx_middle;
	private String word_idx_end;

	/* Relation printing format */
	private String rel_markup_start;
	private String rel_markup_end;
	private String rel_fmt;
	private HashMap<String,String> rel_map;
	private HashMap<String,HashMap<String,String>> rel_implode;

	/* Attribute printing format */
	private String attr_fmt;
	private HashMap<String,String> attr_map;
	private HashMap<String,HashMap<String,String>> attr_val;
	private HashMap<String,HashMap<String,String>> attr_id;

	private String wattr_fmt;
	private HashMap<String,String> wattr_map;
	private HashMap<String,HashMap<String,String>> wattr_val;
	private HashMap<String,HashMap<String,String>> wattr_id;

	/* Infinitive printing */
	private String inf_fmt;

	/*
	 * Do setup for generating XML output
	 */
	private void XMLMarkup()
	{
		relview.showWord(false);
		id_map = null;

		// Index of word in sentence
		sentence_start = "   <words>\n";
		sentence_end = "   </words>\n\n";
		word_idx_start = "      <word id=\"";
		word_idx_middle = "\">";
		word_idx_end = "</word>\n";

		rel_markup_start = "   <relations>\n";
		rel_markup_end = "   </relations>\n\n";

		// Infinitive format
		inf_fmt = "%i      <relation id=\"%I\" label=\"infinitive\"><id1>%W</id1><id2>%R</id2></relation>\n";

		// Simple word attributes
		attr_fmt = "%a      <relation id=\"%I\" label=\"%N\"><id1>%W</id1><id2>%V</id2></relation>\n";
		attr_map = new HashMap<String,String>();
		attr_val = new HashMap<String,HashMap<String,String>>();
		attr_id = null;

		declAttr("gender",        "gender",         gen_map);
		declAttr("noun_number",   "number",         num_map);
		declAttr("tense",         "tense",          tns_map);
		declAttr("QUERY-TYPE",    "query",          qry_map);
		declAttr("DEFINITE-FLAG", "quantification", def_map);

		// One-off word attributes
		wattr_fmt = "%w      <relation id=\"%I\" label=\"%N\"><id1>%W</id1><id2>%V</id2></relation>\n";
		wattr_map = new HashMap<String,String>();
		wattr_val = new HashMap<String,HashMap<String,String>>();
		wattr_id = null;

		declWdAt("POS", "pos", pos_map);

		// Binary relations
		rel_fmt = "%r      <relation id=\"%I\" label=\"%N\"><id1>%F</id1><id2>%T</id2></relation>\n";
		rel_map = new HashMap<String,String>();
		rel_implode = new HashMap<String,HashMap<String,String>>();

		// Implodables
		declRel("_appo",         "appostive",    true);
		declRel("_obj",          "object",       true);
		declRel("_iobj",         "ind-obj",      true);
		declRel("_subj",         "subject",      true);
		declRel("_prepObj",      "prep-object",  true);
		declRel("_prepSubj",     "prep-subject", true);

		// Non-Implodables
		declRel("_to-do",         "action",         false);
		declRel("_amod",          "adjective",      false);
		declRel("_advmod",        "adverb",         false);
		declRel("_%because",      "cause",          false);
		declRel("_more",          "comparison",     false);
		declRel("_%atLocation",   "location",       false);
		declRel("_nn",            "noun-mod",       false);
		declRel("_%quantity_mult","quant-mult",     false);
		declRel("_poss",          "possesion",      false);
		declRel("_%quantity_mod", "quantifier",     false);
		declRel("_to-be",         "property",       false);
		declRel("_%quantity",     "quantity",       false);
		declRel("_that",          "that",           false);
		declRel("_%atTime",       "time",           false);
	}

	/*
	 * Do setup for generating human-readable output
	 */
	private void PlainMarkup()
	{
		relview.showWord(true);
		id_map = null;

		// Index of word in sentence
		sentence_start = "Word Indexes:\n";
		sentence_end = "\n";
		word_idx_start = "   ";
		word_idx_middle = " = ";
		word_idx_end = "\n";

		rel_markup_start = "Relations:\n";
		rel_markup_end = "\n";

		// Infinitive of a verb
		inf_fmt = "%i   Infinitive (%W, %R)\n";

		// Simple word attributes
		attr_fmt = "%a   %N (%W, %V)\n";
		attr_map = new HashMap<String,String>();
		attr_val = new HashMap<String,HashMap<String,String>>();
		attr_id = null;

		declAttr("gender",        "Gender",         gen_map);
		declAttr("noun_number",   "Number",         num_map);
		declAttr("tense",         "Tense",          tns_map);
		declAttr("QUERY-TYPE",    "Query",          qry_map);
		declAttr("DEFINITE-FLAG", "Quantification", def_map);

		// One-off word attributes
		wattr_fmt = "%w   %N (%W, %V)\n";
		wattr_map = new HashMap<String,String>();
		wattr_val = new HashMap<String,HashMap<String,String>>();
		wattr_id = null;

		declWdAt("POS", "PartOfSpeech", pos_map);

		// Binary relations
		rel_fmt = "%r   %N (%F, %T)\n";
		rel_map = new HashMap<String,String>();
		rel_implode = new HashMap<String,HashMap<String,String>>();

		// Implodables
		declRel("_appo",         "Appositive",           true);
		declRel("_obj",          "Object",               true);
		declRel("_iobj",         "IndirectObject",       true);
		declRel("_subj",         "Subject",              true);
		declRel("_prepObj",      "PrepositionalObject",  true);
		declRel("_prepSubj",     "PrepositionalSubject", true);

		// Non-Implodables
		declRel("_to-do",         "Action",         false);
		declRel("_amod",          "Adjective",      false);
		declRel("_advmod",        "Adverb",         false);
		declRel("_%because",      "Cause",          false);
		declRel("_more",          "Comparison",     false);
		declRel("_%atLocation",   "Location",       false);
		declRel("_%quantity_mult","Multiplication", false);
		declRel("_nn",            "NounModifier",   false);
		declRel("_poss",          "Possesion",      false);
		declRel("_%quantity_mod", "Quantifier",     false);
		declRel("_to-be",         "Property",       false);
		declRel("_%quantity",     "Quantity",       false);
		declRel("_that",          "That",           false);
		declRel("_%atTime",       "Time",           false);
	}

	private void declRel(String feat_name, String prt_name, Boolean implode)
	{
		rel_map.put(feat_name, prt_name);
		if (!implode) rel_implode.put(feat_name, rel_map);
	}

	private void declAttr(String attr_name, String prt_name,
	                      HashMap<String,String> map)
	{
		attr_map.put(attr_name, prt_name);
		attr_val.put(attr_name, map);
	}

	private void declWdAt(String attr_name, String prt_name,
	                      HashMap<String,String> map)
	{
		wattr_map.put(attr_name, prt_name);
		wattr_val.put(attr_name, map);
	}

	/* -------------------------------------------------------------------- */

	private String getPartOfSpeech(FeatureNode fn)
	{
		if (fn == null) return "";

		FeatureNode ns = fn.get("nameSource");
		if (ns == null) return "";

		FeatureNode pos = ns.get("POS");
		if (pos == null) return "";
		
		String pname = pos.getValue();
		return pname;
	}

	/**
	 * getInfinitive() -- get the infinitive form of a verb.
	 */
	private String getInfinitive(FeatureNode fn)
	{
		String pname = getPartOfSpeech(fn);
		if (!pname.equals("verb")) return "";

		String str = relview.get_root_word_form(fn);
		return str;
	}

	private String getAttr(FeatureNode fn, String attr_name)
	{
		if (fn == null) return "";

		FeatureNode df = fn.get(attr_name);
		if (df == null) return "";
		
		String def = df.getValue();
		return def;
	}

	/* -------------------------------------------------------------------- */

	private Boolean skip_left_wall(FeatureNode f)
	{
		FeatureNode fname = f.get("name");
		if (fname == null) return true;
		String name = fname.getValue();
		if (name.equals("LEFT-WALL")) return true;
		return false;
	}

	/* -------------------------------------------------------------------- */

	private void _map_id(String str, HashMap<String,String> map)
	{
		if (str.equals("")) return;
		String nstr = map.get(str);
		if (nstr != null) str = nstr;
		if (do_show_XML) {
			id_map.put(str, string_id.toString());
			string_id ++;
		} else {
			id_map.put(str, str);
		}
	}

	/**
	 * Walk the graph, looking for ID'able words.
	 */
	private class findIDs implements FeatureNodeCallback
	{
		public Boolean FNCallback(FeatureNode f)
		{
			if (skip_left_wall(f)) return false;

			// Assign an ID number to all verb infinitives.
			String inf = getInfinitive(f);
			if (!inf.equals("")) {
				id_map.put(inf, string_id.toString());
				string_id ++;
			}

			// Assign an ID number to all parts of speech.
			String pos = getPartOfSpeech(f);
			_map_id(pos, pos_map);

			// Assign an ID number to quantifier.
			String def = getAttr(f, "DEFINITE-FLAG");
			_map_id(def, def_map);

			// Assign an ID number to tenses.
			String tns = getAttr(f, "tense");
			_map_id(tns, tns_map);

			// Assign an ID number to noun numbers.
			String num = getAttr(f, "noun_number");
			_map_id(num, num_map);

			// Assign an ID number to gender.
			String gen = getAttr(f, "gender");
			_map_id(gen, gen_map);

			// Assign an ID number to queries.
			String qry = getAttr(f, "QUERY-TYPE");
			_map_id(qry, qry_map);

			return false;
		}
	}

	/**
	 * Print a term-to-ID number table for the various terms that can occur.
	 */
	public String printIDs()
	{
		id_map = new HashMap<String,String>();
		string_id = 2001;
		sent.foreach(new findIDs());

		String outstr = "   <relex_words>\n";

		for (String key : id_map.keySet()) {
			String val = id_map.get(key);
			outstr += "      <word id=\"" + val + "\">" + key + "</word>\n";
		}

		outstr += "   </relex_words>\n\n";
		return outstr;
	}

	private void 
	_remap_ids(HashMap<String,HashMap<String,String>> aid,
	             HashMap<String,HashMap<String,String>> aval)
	{
		for (String key : aval.keySet()) {
			HashMap<String,String> val_map = aval.get(key);
			if (val_map != null) {
				HashMap<String,String> remap = new HashMap<String,String>();
				aid.put(key, remap);

				for (String rawval : val_map.keySet()) {
					String val = val_map.get(rawval);
					String id = val;
					if (id_map != null) {
						id = id_map.get(val);
						if (id == null) id = val;
					}
					remap.put(rawval, id);
				}
			}
		}
	}

	/**
	 * Map attribute values to ID numbers. This allows for
	 * a straight lookup from the "raw" value, via the
	 * intermediate "pretty-looking" value, to a numeric id.
	 */
	private void mapValsToIDs()
	{
		attr_id = new HashMap<String,HashMap<String,String>>();
		_remap_ids (attr_id, attr_val);

		wattr_id = new HashMap<String,HashMap<String,String>>();
		_remap_ids (wattr_id, wattr_val);
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Walk the graph, extracting semantic relationships, and word attributes.
	 */
	private class prtRelation implements FeatureNodeCallback
	{
		public String outstr;
		public prtRelation()
		{
			outstr = "";
		}
		public Boolean FNCallback(FeatureNode f)
		{
			if (skip_left_wall(f)) return false;

			outstr += relview.scanner(f, inf_fmt,   id_map,    null);
			outstr += relview.scanner(f, rel_fmt,   rel_map,   rel_implode);
			outstr += relview.scanner(f, attr_fmt,  attr_map,  attr_id);
			outstr += relview.scanner(f, wattr_fmt, wattr_map, wattr_id);

	// outstr += rv.print_bool_attr(f, "TRUTH-QUERY-FLAG","Humpf",          "Query");
	// outstr += rv.print_bool_attr(f, "QUERY-FLAG",    "Humpf",          "Query");
	// outstr += rv.print_bool_attr(f, "HYP",           "Hypothesis",     "xxx");

	// XXX to-do:
	// booleans not handled:
	// HYP -- Kim knows Mike is hungry.  hypothesis is that "mike is hungry"
	// TRUTH-QUERY-FLAG  -- Is John alive ?  -- should be a query type...
	//
	// XXX Deprecated: QUERY-FLAG: this should not really be used for anything.
			return false;
		}
	}

	public String printRelations()
	{
		mapValsToIDs(); 
		relview.setIndex(3001);
		String outstr = rel_markup_start;
		prtRelation prt = new prtRelation();
		sent.foreach(prt);
		outstr += prt.outstr;
		outstr += rel_markup_end;
		return outstr;
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Print out the original sentence by walking the graph NEXT nodes.
	 * Handy for debugging.
	 */
	private void _nextCrawl(FeatureNode f, String[] blob)
	{
		FeatureNode fn = f.get("orig_str");
		if (fn != null) blob[0] += fn.getValue() + " ";
		fn = f.get("NEXT");
		if (fn != null)
			_nextCrawl (fn, blob);
	}

	public void nextCrawl(String[] blob)
	{
		_nextCrawl(sent.getLeft(), blob);
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Print the index of the word in the sentence
	 */
	private String printWordIndex()
	{
		// Print the XML word-to-index key table.
		String word_idx = sentence_start;
		int numWords = sent.getNumWords();
		for (int i = 1; i < numWords; i++) {
			FeatureNode fn = sent.getWordAsNode(i);
			FeatureNode word = fn.get("orig_str");
			if (word == null) {
				// There is no orig_str for the given index, if it was
				// merged into an entity. For example "New York", the
				// word "New" will not have an orig_str, while that for
				// "York" will be "New_York".
				// word_idx += "ohh nooo! " + i + "\n";
			} else {
				FeatureNode fidx = fn.get("index_in_sentence");
				String idx = fidx.getValue();
				String ostr = word.getValue();
				word_idx += word_idx_start + idx
				            + word_idx_middle + ostr + word_idx_end;
			}
		}
		word_idx += sentence_end;

		return word_idx;
	}

	// Subject to change; provisional output right now.
	public String printCerego()
	{
		String ret = "";
		if (do_show_XML) 
			ret += "<relex_xml>\n";

		// Print the word-to-index key table.
		ret += printWordIndex();

		// Create list of ID's for certain strings
		if (do_show_XML) ret += printIDs();

		// Print the phrase-to-index key table.
		PhraseView ph = new PhraseView();
		ph.setParse(sent);
		ph.showXML(do_show_XML);
		ret += ph.printPhraseIndex();

		ret += printRelations();

		if (do_show_XML) 
			ret += "</relex_xml>\n";

		return ret;
	}

} // end ParseView

