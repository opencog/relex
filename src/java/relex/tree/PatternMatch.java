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
		int open = pattern.indexOf('(');
		if (open < 0) return false;  // no opening paren was found.

		int close = pattern.lastIndexOf(')');
		if (close < 0) return false;  // no closing paren was found.

		pattern = pattern.substring(open+1, close).trim();

		int white = pattern.indexOf(' ');
		if (white < 0) return false;  // no whitespace after token!

		// ptype is the phrase type (S, NP, VP, ADVP, etc.)
		String ptype = pattern.substring(0, white);

		String phtype = pt.getPhraseType();
System.out.println ("duude ptype="+ptype + " ph=" + phtype);

		if (!ptype.equals(phtype)) return false;

		Boolean rc = cb.PMCallback(pattern, pt);
		return rc;
	}
}
