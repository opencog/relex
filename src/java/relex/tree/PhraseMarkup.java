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

import relex.ParsedSentence;
import relex.feature.FeatureNode;

/**
 * The PhraseMarkup class adds Penn tree-bank style phrase structure
 * markup to the relex ParsedSentence/FeatureNode graph.
 *
 * An example Penn tree phrase structure is
 * (S (NP I) (VP am (NP a big robot)) .)
 *
 * The head of the phrase structure is located with the "phr-head"
 * key on the left wall (ParsedSentence.getLeft()) of the sentence.
 *
 * The phrase structure is given the same LISP-like nested list
 * sructure as the ascii above. Thus, the next element in the
 * list is given by "phr-next" (cdr) while subphrases can be found
 * by looking for "phr-head" (car).
 *
 * Each node always holds a pair of keys. One key is always
 * "phr-next" (cdr). The other (car) is one (and only one) of
 * the following three feature nodes:
 *
 * "phr-type": can be S, VP, NP, ADJP, etc., or absent
 * "phr-word": pointer to the feature node for the word.
 * "phr-head": pointer to a subphrase. So, for example,
 *             (NP a big robot) is a sub-sub-phrase of the whole.
 *
 * The mapping is such that CDR is always given by "phr-next",
 * while CAR is either "phr-type", "phr-word" or "phr-head".
 * Here, CAR and CDR are the standard LISP pair primitves.
 *
 * The standard recipe for walking the tree would then be:
 *     void doStuff (FeatureNode head) {
 *         FeatureNode fn = head.get("phr-head");
 *         while (fn != null) {
 *             FeatureNode subf = fn.get("phr-head");
 *             if (subf != null) doStuff (fn);
 *             fn = fn.get("phr-next");
 *         }
 *     }
 *
 * This structure can be found by looking up the "phr-head"
 * pointer on the LEFT-WALL.  In addition, each word will
 * point back at the smallest subphrase that contains it (via
 * a "phr-head" in that word).
 *
 * The tree can also be navigated backwards, from leaves to
 * trunk, by following a chain of "phr-root".
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */

public class PhraseMarkup
{
	static final int DEBUG = 0;
	int word_index;
	ParsedSentence sentence;

	public PhraseMarkup()
	{
		word_index = 0;
		sentence = null;
	}

	/**
	 * Take the indicated sentence, and add FeatureNodes to
	 * it, corresponding to the phrase structure of the sentence.
	 */
	public void markup(ParsedSentence sent)
	{
		word_index = 1;
		sentence = sent;
		String phraseString = sent.getPhraseString();

		// The phraseString might be null if the sentence contains Japanese 
		// or Chinese UTF8 characters.  It seems that Java does not support
		// Japanese/Chinese automatically(!!??) and returns nulls for such
		// strings.
		if (null == phraseString) return;
		FeatureNode head = sent.getLeft();

		doMarkup(head, null, phraseString);
		// System.err.println("Debug: phrase markup:\n" + sent.getLeft());

		// Now, mark up the phrase leaders as well. 
		// This assumes that the relex algos have already run.
		PhraseLeader.markup(head);
	}

	/**
	 * Find and return the index of matching parenthesis
	 */
	static private int closer (String str, int idx)
	{
		int cnt = 0;
		int len = str.length();
		while (idx < len)
		{
			if (str.charAt(idx) == '(') cnt++;
			else if (str.charAt(idx) == ')') cnt--;
			if (cnt == 0) return idx;
			idx ++;
		}
		return idx;
	}

	/**
	 * The Java String class is missing basic string handling functions
	 * such as strsep() and strpbrk(). So hack around this inadequacy :-/
	 * and search for white-space by hand.  These two functions are
	 * crude equivalaents of the standard C library strpbrk() and strspn()
	 * routines.
	 */
	static private int strpbrk (String str, int idx, String accept)
	{
		int we = str.length();
		int n = accept.length();
		for (int i=0; i<n; i++)
		{
			int wh = str.indexOf(accept.charAt(i), idx);
			if (wh>0 && wh < we) we = wh;
		}
		return we;
	}

	static private int strspn (String str, int idx, String accept)
	{
		int len = str.length();
		int n = accept.length();
		for (int j = idx; j<len; j++)
		{
			char ch = str.charAt(j);
			int i=0;
			for (i=0; i<n; i++)
			{
				if (ch == accept.charAt(i)) break;
			}
			if (i==n) return j;
		}
		return len;
	}

	private void doMarkup(FeatureNode top, FeatureNode root, String str)
	{
		if (str.charAt(0) != '(') return;

		if (0 < DEBUG) System.err.println("Debug: doMarkup: " + str);

		// Mark the head of the phrase
		FeatureNode head = top.add("phr-head");
		if (root != null) head.set("phr-root", root);
		FeatureNode fn = head;

		// Find trailing whitespace, extract substring
		int idx = strpbrk (str, 0, " \t\r\n");
		String phrase_type = str.substring(1, idx);
		fn.set("phr-type", new FeatureNode(phrase_type));

		// Now get the word, or get the next subphrase
		idx++;
		int len = str.length();
		while(idx < len)
		{
			// If the first char is '(', then find the matching ')',
			// These two demark a sub-phrase. Hand off the subphrase
			// for recursive treatment.
			if (str.charAt(idx) == '(')
			{
				// Remove the trailing paren
				int lidx = closer(str, idx) + 1;
				if (len < lidx) {
					throw new RuntimeException("Malformed phrase structure string: " + str);
				} else {
					String subphrase = str.substring(idx,lidx);
					fn = fn.add("phr-next");
					doMarkup(fn, top, subphrase);
					idx += subphrase.length() + 1;
					idx = strspn(str, idx, " \r\n\t");
				}
			}
			else
			{
				// If we are here, the next elt is a word.
				// We can handle this word in one of three ways:
				//
				// 1) Increment the word index, and hope that we are pointing
				//    at the right thing.
				//
				// 2) Extract the string. Unfortunately, this doesn't tell
				//    us what the corresponding feature node is.
				//
				// 3) Compare the orig_str from 1) with the string from 2) and
				//    make sure they match. This almost works, except that
				//    entities goof things up. For example, "New York" becomes
				//    null + "New_York", making direct word comparison hard.
				//
				// Manual testing shows that 1) works. So just do part 1).
				//
				// Sooo .. do step 1)
				FeatureNode wn = sentence.getWordAsNode(word_index);

				// Now, for Step 2).  The word will be followed either by a
				// blank, a tab, a closing paren, or a newline or carriage return.
				int we = strpbrk(str, idx, " \r\n\t)");

				// The following commented-out lines attempt to
				// implement step 3) described above
				// String word = str.substring(idx, we);
				// FeatureNode fo = wn.get("orig_str");
				// String sword = "";
				// if (fo != null) sword = fo.getValue();
				// System.out.println("idx=" + idx + " we=" + we + " word=" + word + " sw=" + sword);

				fn = fn.add("phr-next");
				fn.set("phr-word", wn);
				wn.set("phr-head", head);
				word_index ++;

				// If a closing paren was found, then we are done.
				int wh = strpbrk(str, idx, ")");
				if (we == wh) return;

				// Advance to the next word
				idx = strspn(str, we, " \r\n\t");
			}
		}
	}
};

/* =========================== END OF FILE ================== */
