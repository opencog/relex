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
package relex.tree;

import relex.feature.FeatureNode;

/**
 * Implements phrase pattern matching.
 *
 *  Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class PatternMatch
{

	/**
	 * Phrase pattern matching.
	 * Given a string pattern, e.g. (NP (NP a) (PP a (NP r)))
	 * and a phrase-tree structure, it will determine
	 * if the phrase-tree structure matches the pattern, 
	 * and then if will call the callback for each match.
	 *
	 * So, for example, if the PhraseTree looks like:
	 * (NP (NP a couple) (PP of (NP clicks)))
	 * the callback will make the following calls:
	 *    ("a", (NP a couple))
	 *    ("a", (PP of (NP clicks)))
	 *    ("r", (NP clicks))
	 */
	public static Boolean match (String pattern, PhraseTree pt, PatternCallback cb)
	{
		return _match(pattern, pt, cb);
	}

	/**
	 * Return true to indicate mismatch
	 */
	private static Boolean _match (String pattern, PhraseTree pt, PatternCallback cb)
	{
System.out.println("enter match pat= " + pattern + " tree=" + pt.toString());
		int open = pattern.indexOf('(');
		if (open < 0) return true;  // no opening paren was found.

		int close = pattern.lastIndexOf(')');
		if (close < 0) return true;  // no closing paren was found.

		pattern = pattern.substring(open+1, close).trim();

		int white = pattern.indexOf(' ');
		if (white < 0) return true;  // no whitespace after token!

		// ptype is the phrase type (S, NP, VP, ADVP, etc.)
		String ptype = pattern.substring(0, white);

		// phrase types must match.
		String phtype = pt.getPhraseType();
		// System.out.println ("ptype="+ptype + " phrasetype=" + phtype);
		if (!ptype.equals(phtype)) return true;

		// skip over the type in the pattern string.
		pattern = pattern.substring(white).trim();
		boolean pat_starts_with_word = !pattern.startsWith("(");

		// Now start walking the thing.
		FeatureNode fn = pt.get("phr-head");
		boolean saw_word = false;
System.out.println("duude so far so good with " + pt.toString() + " and pat=" + pattern);
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null)
			{
				if (!pat_starts_with_word) return true; // no match
FeatureNode fu= wd.get("orig_str");
String fus="";
if (fu != null) fus=fu.getValue();
System.out.println("duude got match word "+ fus);
				saw_word = true;
			}
			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				if (pat_starts_with_word)
				{
					if (saw_word == false) return true; // no match
					String wat = pattern.substring(0, pattern.indexOf('(')).trim();
System.out.println("breakout pat=" + wat);
					Boolean rc = cb.PMCallback(wat, pt);
					if (rc) return rc;
				}

				// extract a sub-tree from the pattern.
				open = pattern.indexOf('(');
				close = get_closing_paren (pattern, open);

				String subpat = pattern.substring(open, close+1);
System.out.println("duude got subf");
				PhraseTree subt = new PhraseTree(fn);
				boolean rc = _match(subpat, subt, cb);
				if (rc) return rc;

				pattern = pattern.substring(close+1).trim();
				pat_starts_with_word = !pattern.startsWith("(");
				saw_word = false;
			}
			fn = fn.get("phr-next");
		}

		if (0 == pattern.length()) return false;
		if (pat_starts_with_word && !saw_word) return true;
System.out.println("at the end but patt=" + pattern + "=");
		Boolean rc = cb.PMCallback(pattern, pt);
		return rc;
	}

	static private int get_closing_paren(String str, int open)
	{
		int cnt = 1;
		int paren = open;
		while (cnt != 0)
		{
			int next_clos = str.indexOf(')', paren+1);
			if (next_clos < 0) return -1;
			int next_open = str.indexOf('(', paren+1);
			if (next_clos < next_open)
			{
				cnt --;
				paren = next_clos;
			} 
			else if (0 < next_open) 
			{
				cnt ++;
				paren = next_open;
			}
			else // if we are here, then next_open = -1 and there's no opener.
			{
				cnt --;
				paren = next_clos;
			}
		}
		return paren;
	}
}
