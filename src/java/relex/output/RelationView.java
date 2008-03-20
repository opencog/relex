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
import java.util.Iterator;

import relex.feature.FeatureNode;
import relex.tree.PhraseTree;

/**
 * A RelationView object displays a word relationship in the
 * Cerego-style format.
 *
 * The name of this class is likely to change to some better name,
 * suggestions welcome.
 *
 * This class makes heavy use of String. If performance needs to be
 * improved, then a conversion to StringBuff should be considered.
 *
 * This class doesn't inherit from relex.feature::View because there
 * doesn't seem to be any strong, direct reason for doing so.
 *
 * Copyright (C) 2007 Linas Vepstas <linas@linas.org>
 */
class RelationView
{
	private Integer idx;

	// Output modifiers.
	private Boolean do_print_orig_word;
	private Boolean do_print_index;

	// Constructor.
	public RelationView()
	{
		do_print_orig_word =  true;
		do_print_index =  true;
		idx = 0;
	}

	public void showWord(Boolean flag)
	{
		do_print_orig_word = flag;
		do_print_index = true;
	}

	public void showWordOnly(Boolean flag)
	{
		do_print_orig_word = flag;
		do_print_index = !flag;
	}

	public void setIndex(Integer val)
	{
		idx = val;
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Return the index of the word in the original sentence
	 */
	public String
	get_word_index(FeatureNode fn)
	{
		if (fn == null) return "";

		FeatureNode fn_name_source = fn.get("nameSource");
		// XXX fix-me: a missing fn_name_source is due to a relex algs bug.
		// We should notify someone about this in some way.
		if (fn_name_source == null) return "";

		// Print the index in the sentence
		FeatureNode fn_index = fn_name_source.get("index_in_sentence");
		String index = fn_index.getValue();
		return index;
	}

	public String
	get_one_orig_word(FeatureNode word)
	{
		if (word == null) return "";

		// Print the index in the sentence
		FeatureNode fn_index = word.get("index_in_sentence");
		if (fn_index == null) return "";
		String index = fn_index.getValue();

		// Print the word in the sentence, just to be clear
		if (do_print_orig_word) {
			String str = "";
			FeatureNode fn_orig_str = word.get("orig_str");
			if (fn_orig_str != null) {
				str += fn_orig_str.getValue();
			}
			if (do_print_index) str += "[" + index + "]";
			return str;
		} else {
			return index;
		}
	}

	/**
	 * Print out a word from a sentence, as referenced through the
	 * "nameSource" node. The original word is in "orig_str", and
	 * its location in the sentence is in "index_in_sentence".
	 */
	public String
	get_orig_word(FeatureNode fn)
	{
		return get_orig_word(fn, false);
	}

	private String
	get_orig_word(FeatureNode fn, Boolean do_implode)
	{
		if (fn == null) return "";

		FeatureNode fn_name_source = fn.get("nameSource");
		// XXX fix-me: a missing fn_name_source is due to a relex algs bug.
		// We should notify someone about this in some way.
		if (fn_name_source == null) return "";

		// If implosions are disabled, just print the single word.
		// If implosions are enabled, and the word is a part of a
		// leaf phrase (contains no subphrases) then print the phrase.
		String orig_str = "";
		Boolean simple_prt = true;
		if (do_implode) {
			simple_prt = PhraseTree.isCompoundLeaf(fn_name_source);
			simple_prt = !simple_prt;
		}

		if (simple_prt) {
			orig_str += get_one_orig_word (fn_name_source);
		} else {
			FeatureNode phr = fn_name_source.get("phr-head");
			orig_str += "(";
			while (phr != null) {
				FeatureNode word = phr.get("phr-word");
				if (word != null) {
					orig_str += get_one_orig_word(word);
				}
				phr = phr.get("phr-next");
				if (phr != null && word != null) orig_str += " ";
			}
			orig_str += ")";
		}
		return orig_str;
	}

	private String
	get_orig_phrase(FeatureNode fn)
	{
		if (fn == null) return "";

		FeatureNode fn_name_source = fn.get("nameSource");
		// XXX fix-me: a missing fn_name_source is due to a relex algs bug.
		// We should notify someone about this in some way.
		if (fn_name_source == null) return "";

		// If the word is a part of a leaf phrase (contains no subphrases)
		// then return the id of the leaf phrase
		Boolean simple_prt = PhraseTree.isCompoundLeaf(fn_name_source);
		if (!simple_prt) return "";

		FeatureNode phr = fn_name_source.get("phr-head");
		FeatureNode pidx = phr.get("phr-idx");

		String orig_str = "";
		if (do_print_orig_word)
		{
			PhraseView pv = new PhraseView();
			orig_str += pv.__prtIndex(fn_name_source, false);
		}
		if (do_print_index)
		{
			if (do_print_orig_word) orig_str += "[";
			orig_str += pidx.getValue();
			if (do_print_orig_word) orig_str += "]";
		}
		return orig_str;
	}

	public String
	get_root_word_form(FeatureNode word)
	{
		if (word == null) return "";

		String str = "";
		FeatureNode fn_root = word.get("name");
		if (fn_root != null) {
			str += fn_root.getValue();
		}
		return str;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Look for the word attribute "match_attr_name", and, if found,
	 * print it out, using "prt_attr_name" as the translated thing to print.
	 */
	public String
	print_attribute(FeatureNode fn,
                   String match_attr_name,
	                String prt_attr_name)
	{
		if (fn == null) return "";

		// If the attribute is there, print it
		FeatureNode fn_attr = fn.get(match_attr_name);
		if (fn_attr == null) return "";

		String attr = fn_attr.getValue();
		String outstr = prt_attr_name + " (" + get_orig_word(fn, false) + ", " + attr + ")\n";
		return outstr;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Look for the word attribute "match_attr_name", and, if found,
	 * print it out, using "prt_attr_name" as the translated thing to print.
	 */
	public String
	print_word_attribute(FeatureNode fn,
                   String match_attr_name,
	                String prt_attr_name)
	{
		return print_word_attribute(fn, match_attr_name, prt_attr_name, null);
	}

	public String
	print_word_attribute(FeatureNode fn,
                   String match_attr_name,
	                String prt_attr_name,
	                HashMap<String,String> attr_map)
	{
		if (fn == null) return "";
		FeatureNode fn_src = fn.get("nameSource");
		if (fn_src == null) return "";

		// If the attribute is there, print it
		FeatureNode fn_attr = fn_src.get(match_attr_name);
		if (fn_attr == null) return "";

		String attr = fn_attr.getValue();
		if (attr_map != null) {
			String map = attr_map.get(attr);
			if (map != null) attr = map;
		}
		String outstr = prt_attr_name + " (" + get_orig_word(fn, false) + ", " + attr + ")\n";
		return outstr;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Look for the word attribute "match_attr_name", and, if found,
	 * check to see if it is true.  If so, then print "prt_attr_name",
	 * with value prt_value. For example, "DEFINITE-FLAG", becomes
	 * "Quantification (The[1], Definite);"
	 */
	public String
	print_bool_attr(FeatureNode fn,
	                String match_attr_name,
	                String prt_attr_name,
	                String prt_attr_value)
	{
		if (fn == null) return "";

		// If the attribute is there, print it
		FeatureNode fn_attr = fn.get(match_attr_name);
		if (fn_attr == null) return "";

		String attr = fn_attr.getValue();
		if (!attr.equals("T")) return "";

		String outstr = prt_attr_name + " (" + get_orig_word(fn, false)
		                + ", " + prt_attr_value + ")\n";
		return outstr;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Look for the relation "match_relation_name", and, if found,
	 * print it out, using "prt_relation_name" as the print header.
	 * The structure of a relation is always
	 * [name <<from-val>> links [relation [name <<to-val>>]]]
	 * So, for example,
	 * print_relation (f, "_%atLocation", "Location", true);
	 */
	public String
	print_relation(FeatureNode fn_link_from,
	               String match_relation_name,
	               String prt_relation_name,
	               Boolean do_implode)
	{
		if (fn_link_from == null) return "";

		FeatureNode fn_link = fn_link_from.get("links");
		if (fn_link == null) return "";

		FeatureNode fn_link_to = fn_link.get(match_relation_name);
		if (fn_link_to == null) return "";

		String outstr = "";
		// There may be multiple outgoing links;
		// if there are, print all of them.
		// outstr += "XXX link-to >> " + fn_link_to._prt_vals();
		FeatureNode multi = fn_link_to.get("member0");
		if (multi != null) {
			Integer n = 0;
			while (multi != null) {
				outstr += prt_relation_name + " ("
				          + get_orig_word(fn_link_from, do_implode) + ", ";
				outstr += get_orig_word(multi, do_implode) + ")\n";

				n++;
				String member_name = "member" + n.toString();
				multi = fn_link_to.get(member_name);
			}
		} else {
			outstr += prt_relation_name + " ("
			          + get_orig_word(fn_link_from, do_implode) + ", ";

			// The fn_link_to_name should be same as the printed name.
			// Validate this manually.
			// FeatureNode fn_link_to_name = fn_link_to.get("name");
			// String link_to_name = fn_link_to_name.getValue();
			// outstr += link_to_name + "\n";

			// Find the other end, the source of what is being linked together
			outstr += get_orig_word(fn_link_to, do_implode) + ")\n";
		}
		return outstr;
	}

	/* ----------------------------------------------------------- */
	/* Attribute scanning */

	private String orig_word;   // %W
	private String attr_name;   // %N
	private String attr_val;    // %V
	
	private Boolean
	scan_for_attribute(FeatureNode fn,
	                   HashMap<String,String> map)
	{
		return scan_for_attribute(fn, fn, map);
	}

	private Boolean
	scan_for_word_attribute(FeatureNode fn,
	                        HashMap<String,String> map)
	{
		if (fn == null) return false;
		FeatureNode fn_src = fn.get("nameSource");
		if (fn_src == null) return false;

		return scan_for_attribute(fn, fn_src, map);
	}

	private Iterator<String> attr_iterator;
	private FeatureNode attr_fn_src;

	private Boolean
	scan_for_attribute(FeatureNode fn,
	                   FeatureNode fn_src,
	                   HashMap<String,String> map)
	{
		orig_word = "";
		attr_name = "";
		attr_val = "";

		if (fn == null) return false;
		if (fn_src == null) return false;

		attr_fn_src = fn_src;
		attr_iterator = map.keySet().iterator();
		return true;
	}

	/**
	 * Look for the word attribute "match_attr_name", and, if found,
	 * print it out, using "attr_name" as the translated thing to print.
	 */
	private Boolean
	attribute_iterate(FeatureNode fn,
	                  HashMap<String,String> map,
	                  HashMap<String,HashMap<String,String>> value_map)
	{
		// Loop over the list of attributes, and see if
		// the attr_fn_src carries any of these attributes.
		// If not, return the empty string.
		FeatureNode fn_attr = null;
		String match_attr_name = null;

		while (attr_iterator.hasNext()) {
			match_attr_name = attr_iterator.next();

			fn_attr = attr_fn_src.get(match_attr_name);
			if (fn_attr != null) break;
		}

		// If this node doesn't have any of the attrs, give up
		if (fn_attr == null) return false;

		// If the attribute value is to be renamed, then rename it
		attr_val = fn_attr.getValue();
		if (value_map != null) {
			HashMap<String,String> attr_val_map = value_map.get(match_attr_name);
			if (attr_val_map != null) {
				String remap = attr_val_map.get(attr_val);
				if (remap != null) attr_val = remap;
			}
		}

		orig_word = get_orig_word(fn, false);
		if (orig_word.equals("")) return false;

		attr_name = map.get(match_attr_name);

		return true;
	}

	/* ----------------------------------------------------------- */

	private String root_word;   // %R

	private Boolean
	scan_for_infinitive(FeatureNode fn,
	                    HashMap<String,String> map,
	                    HashMap<String,HashMap<String,String>> value_map)
	{
		root_word = null;
		if (fn == null) return false;

		FeatureNode ns = fn.get("nameSource");
		if (ns == null) return false;

		FeatureNode pos = ns.get("POS");
		if (pos == null) return false;
		
		String pname = pos.getValue();
		if (!pname.equals("verb")) return false;

		orig_word = get_one_orig_word (ns);
		root_word = get_root_word_form(fn);

		if (map != null) {
			String id = map.get(root_word);
			if (id != null) root_word = id;
		}
		return true;
	}

	/* ----------------------------------------------------------- */
	/**
	 * Loop over all links extending from a starting
	 * feature node. When found, print the name of the link,
	 * and the from- and to- nodes connected by the link.
	 *
	 * Optionally, rename the relation name via the map.
	 * Optionally, use implosions.
	 */

	private Integer member_count;
	private String orig_from_word;   // %F
	private String orig_to_word;     // %T (word, or phrase if implodable....)
	private String orig_to_phrase;   // %P

	private Boolean
	scan_for_one_relation(FeatureNode fn_link_from,
	               FeatureNode fn_link_to)
	{
		// Find the other end, the source of what is being linked together
		orig_from_word = get_orig_word(fn_link_from, false);
		if (orig_from_word.equals("")) return false;

		orig_to_word = get_orig_word(fn_link_to, false);
		if (orig_to_word.equals("")) return false;

		orig_to_phrase = get_orig_phrase(fn_link_to);
		if (orig_to_phrase.equals("")) orig_to_phrase = orig_to_word;

		if (rel_do_implode) orig_to_word = orig_to_phrase;

		return true;
	}

	private Boolean
	scan_for_relation_part(FeatureNode fn_link_from,
	               FeatureNode fn_link_to)
	{
		// There may be multiple outgoing links;
		// if there are, print all of them.
		// outstr += "XXX link-to >> " + fn_link_to._prt_vals();

		String member_name = "member" + member_count;
		member_count ++;
		FeatureNode multi = fn_link_to.get(member_name);
		if (multi != null) {
			return scan_for_one_relation(fn_link_from, multi);
		} else {
			if (member_count != 1) return false;
			return scan_for_one_relation(fn_link_from, fn_link_to);
		}
	}

	private Iterator<String> rel_iterator;
	private FeatureNode rel_fn_link;
	private FeatureNode rel_fn_link_to;
	private Boolean rel_do_implode;

	private Boolean
	scan_for_relation(FeatureNode fn_link_from)
	{
		orig_from_word = "";
		orig_to_word = "";
		orig_to_phrase = "";
		rel_fn_link = null;
		rel_fn_link_to = null;
		rel_do_implode = true;
		member_count = 0;

		if (fn_link_from == null) return false;

		rel_fn_link = fn_link_from.get("links");
		if (rel_fn_link == null) return false;

		// Loop over all of the links. Core assumption
		// is that *all* links are valid relations that
		// should be displayed.
		rel_iterator = rel_fn_link.getFeatureNames().iterator();
		return true;
	}

	private Boolean
	relation_iterate(FeatureNode fn_link_from, 
	                  HashMap<String,String> map,
	                  HashMap<String,HashMap<String,String>> value_map)
	{
		Boolean got_one = false;

		if (member_count !=0) {
			got_one = scan_for_relation_part(fn_link_from, rel_fn_link_to);
			if (got_one) return true;
		}
		member_count = 0;
		
		// Loop over all of the links. Core assumption
		// is that *all* links are valid relations that
		// should be displayed.
		while(rel_iterator.hasNext())
		{
			String relation_name = rel_iterator.next();
			rel_fn_link_to = rel_fn_link.get(relation_name);

			if (rel_fn_link_to != null && !rel_fn_link_to.isValued()) {

				// Does the relation need to be imploded ?
				rel_do_implode = true;
				HashMap<String,String> imp = value_map.get(relation_name);
				if (imp != null) rel_do_implode = false;

				// Does the relation need to be re-named?
				String rename = map.get(relation_name);
				if (rename != null)
					relation_name = rename;

				attr_name = relation_name;
				got_one = scan_for_relation_part(fn_link_from, rel_fn_link_to);
				if (got_one) return true;
			}
		}

		return false;
	}

	/* ============================================================= */

	private String
	_scanner(FeatureNode fn, String format_string,
	        HashMap<String,String> map,
	        HashMap<String,HashMap<String,String>> value_map,
	        Boolean recursion_top)
	{
		Boolean found = false;
		String outstr = "";
		int m = 0;
		int n = format_string.indexOf('%');
		while (-1 < n)
		{
			outstr += format_string.substring(m,n);
			char fmt = format_string.charAt(n+1);
			switch (fmt) {
				case 'I': outstr += idx; idx++; break;

				case 'a':
					if (recursion_top) {
						found = scan_for_attribute(fn, map);
						if (!found) return "";
						while(true) {
							found = attribute_iterate(fn, map, value_map);
							if (found == false) return outstr;
							outstr += _scanner(fn, format_string, map, value_map, false);
						}
					}
					break;

				case 'w':
					if (recursion_top) {
						found = scan_for_word_attribute(fn, map);
						if (!found) return "";
						while(true) {
							found = attribute_iterate(fn, map, value_map);
							if (found == false) return outstr;
							outstr += _scanner(fn, format_string, map, value_map, false);
						}
					}
					break;

				case 'i':
					found = scan_for_infinitive(fn, map, value_map);
					if (found == false) return "";
					break;

				case 'r':
					if (recursion_top) {
						found = scan_for_relation(fn);
						if (!found) return "";
						while(true) {
							found = relation_iterate(fn, map, value_map);
							if (found == false) return outstr;
							outstr += _scanner(fn, format_string, map, value_map, false);
						}
					}
					break;

				case 'W': outstr += orig_word; break;
				case 'N': outstr += attr_name; break;
				case 'V': outstr += attr_val; break;
				case 'F': outstr += orig_from_word; break;
				case 'T': outstr += orig_to_word; break;
				case 'P': outstr += orig_to_phrase; break;
				case 'R': outstr += root_word; break;
				default: break;
			}

			m = n+2;
			n = format_string.indexOf('%', m);
		}
		outstr += format_string.substring(m);

		return outstr;
	}

	public String
	scanner(FeatureNode fn, String format_string,
	        HashMap<String,String> map,
	        HashMap<String,HashMap<String,String>> value_map)
	{
		return _scanner(fn, format_string, map, value_map, true);
	}

} // end RelationView

