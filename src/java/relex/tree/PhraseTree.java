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

import java.util.ArrayList;
import java.util.Iterator;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;

/**
 * The PhraseTree class visualizes the Penn tree-bank style
 * phrase structure markup in a FeatureNode graph.
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
 * This class provides *two* sets of interfaces: a traditional
 * static function interface, and an object-oriented interface.
 * The OO interface is a lot easier to use, and its type-safe,
 * but its a *lot* more computationally intensive. The static
 * function interface is a lot faster, but will act unpredicably
 * if it is passed FeatureNodes that aren't appropriate.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */

public class PhraseTree
{
	FeatureNode phr;
	FeatureNode cur; /* cursor into phrase */

	protected PhraseTree() {}

	public PhraseTree(FeatureNode fn)
	{
		if (fn.get("phr-head") != null) phr = fn;
		else phr = fn.get("phr-root");
		if (phr != null) cur = phr.get("phr-head");
		else cur = null;
	}

	public String toString()
	{
		return toString(phr);
	}

	public FeatureNode getCursor()
	{
		return cur;
	}
	public void setCursor(FeatureNode c)
	{
		cur = c;
	}
	public void resetCursor()
	{
		cur = phr.get("phr-head");
	}

	public String getPhraseType()
	{
		return getPhraseType(phr);
	}
	public FeatureNode getPhraseLeader()
	{
		return getPhraseLeader(phr);
	}

	public Boolean contains(PhraseTree child)
	{
		if (child == null) return false;
		return contains(phr, child.phr);
	}

	public PhraseTree up()
	{
		if (phr == null) return null;
		FeatureNode ph = up(phr);
		if (ph == null) return null;
		return new PhraseTree(ph);
	}

	public Iterator<PhraseTree> iterator()
	{
		if (phr == null) return null;
		ArrayList<PhraseTree> list = new ArrayList<PhraseTree>();

		FeatureNode ph = phr.get("phr-head");
		ph = ph.get("phr-next");
		while (ph != null)
		{
			FeatureNode h = ph.get("phr-head");
			if (h != null)
			{
				list.add(new PhraseTree(ph));
			}
			ph = ph.get("phr-next");
		}
		return list.iterator();
	}

	public Boolean foreach(FeatureNodeCallback cb)
	{
		return foreach(phr, cb);
	}

	public Boolean equiv(PhraseTree pt)
	{
		if ((phr == null) && (pt == null)) return true;
		if (pt == null) return false;
		return equiv(phr, pt.phr);
	}

	public FeatureNode get(String str)
	{
		if (phr == null) return null;
		return phr.get(str);
	}

	public FeatureNode getNode()
	{
		return phr;
	}

	public void setMark(String str)
	{
		FeatureNode he = phr.get("phr-head");
		FeatureNode ph = he.get("phr-mark");
		if (ph == null) ph = he.add("phr-mark");
		ph.set(str, new FeatureNode("T"));
	}

	public Boolean getMark(String str)
	{
		FeatureNode ph = phr.get("phr-head");
		ph = ph.get("phr-mark");
		if (ph == null) return false;
		ph = ph.get(str);
		if (ph == null) return false;
		return true;
	}

	public Boolean isLeaf()
	{
		return isLeaf(phr);
	}

	public Boolean hasWord()
	{
		return hasWord(phr);
	}

	public Boolean startsWithWord()
	{
		return startsWithWord(phr);
	}

	public int getDegree()
	{
		return getDegree(phr);
	}

	public int getDepth()
	{
		return getDepth(phr);
	}

	public int getBreadth()
	{
		return getBreadth(phr);
	}

	public ArrayList<FeatureNode> getWordList()
	{
		ArrayList<FeatureNode> words = new ArrayList<FeatureNode>();
		getWordList(phr, words);
		return words;
	}

	public FeatureNode getFirstWord()
	{
		return getFirstWord(phr);
	}

	/* ------------------------------------------------------------ */
	/* equivalent static interfaces below */

	private static String _toString(FeatureNode fn, String str)
	{
		str += "(";
		FeatureNode fn_type = fn.get("phr-type");
		str += fn_type.getValue();

		fn = fn.get("phr-next");
		while (fn != null)
		{
			fn_type = fn.get("phr-type");
			if (fn_type != null)
			{
				str += " " + fn_type.getValue();
			}

			FeatureNode fn_word = fn.get("phr-word");
			if (fn_word != null)
			{
				fn_word = fn_word.get("orig_str");
				// fn_word can be null, when entity names are united.
				// For example, "New York" -> null + "New_York"
				if (fn_word != null)
				{
					// Must not allow words that are parenthesis in the constituent string.
					str += " " + fn_word.getValue().replace('(','{').replace(')','}');
				}
			}

			FeatureNode head = fn.get("phr-head");
			if (head != null)
			{
				str += " ";
				str = _toString(head, str);
			}
			fn = fn.get("phr-next");
		}

		str += ")";
		return str;
	}

	/**
	 * Regenerate the phrase structure tree from the
	 * feature node graph.
	 */
	public static String toString(FeatureNode head)
	{
		if (head == null) return "";
		FeatureNode fn = head.get("phr-head");
		if (fn == null)
		{
			fn = head.get("phr-type");
			if (fn != null) return fn.getValue();
			fn = head.get("phr-word");
			if (fn != null)
			{
				fn = fn.get("orig_str");
				if (fn != null) return fn.getValue();
			}
			return "";
		}
		String str = "";
		return _toString(fn, str);
	}

	/**
	 * Return true, if this word is part of a leaf word phrase
	 * (i.e. the phrase does not itself contain any sub-phrases).
	 */
	public static Boolean isLeaf(FeatureNode word)
	{
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) return false;
			fn = fn.get("phr-next");
		}
		return true;
	}

	/**
	 * Return true, if this phrase has at least one word in it
	 * (as opposed to consisting entirely of subphrases)
	 */
	public static Boolean hasWord(FeatureNode phr)
	{
		FeatureNode fn = phr.get("phr-head");
		while (fn != null)
		{
			FeatureNode word = fn.get("phr-word");
			if (word != null) return true;
			fn = fn.get("phr-next");
		}
		return false;
	}

	/**
	 * Return true, if this phrase starts with a word
	 * (as opposed to starting with a subphrases)
	 */
	public static Boolean startsWithWord(FeatureNode phr)
	{
		FeatureNode fn = phr.get("phr-head");
		while (fn != null)
		{
			FeatureNode word = fn.get("phr-word");
			if (word != null) return true;
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) return false;
			fn = fn.get("phr-next");
		}
		return false;
	}

	/**
	 * Return the degree of a node in the tree
	 */
	public static int getDegree(FeatureNode word)
	{
		int cnt = 0;
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			cnt++;
			fn = fn.get("phr-next");
		}
		return cnt;
	}

	/**
	 * Return the depth of the tree
	 */
	public static int getDepth(FeatureNode word)
	{
		if (null == word) return 0;
		int maxdepth = 0;
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				int depth = getDepth(fn);
				if (maxdepth < depth) maxdepth = depth;
			}
			fn = fn.get("phr-next");
		}
		return maxdepth+1;
	}

	/**
	 * Return true, if this word is part of a leaf word phrase
	 * and the leaf word phrase has two or more words in it.
	 * To be a "leaf phrase", the phrase itself must not
	 * contain any sub-phrases.
	 */
	public static Boolean isCompoundLeaf(FeatureNode word)
	{
		int cnt = 0;
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			cnt ++;
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) return false;
			fn = fn.get("phr-next");
		}
		if (cnt > 2) return true;
		return false;
	}

	/**
	 * Return the breadth of the tree. This is equal to the number of
	 * number of words in the tree.
	 */
	public static int getBreadth(FeatureNode word)
	{
		if (word == null) return 0;

		int cnt = -1;  // don't count the phrase type as part of the breadth
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				cnt += getBreadth(fn);
			}
			else
			{
				cnt ++;
			}
			fn = fn.get("phr-next");
		}
		return cnt;
	}

	/**
	 * Return the leaf node, if this tree has only a
	 * single branch all the way down to its leaf.
	 * i.e. the branch itself is either a leaf,
	 * or there is only one subphrase.
	 */
	public static FeatureNode getOneLeafOnly(FeatureNode word)
	{
		int cnt = 0;
		FeatureNode rc = word;
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word"); // count only words
			if (wd != null) cnt ++;
			if (cnt > 1) return null;
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) {
				rc = getOneLeafOnly(fn);
				if (rc == null) return null;
			}
			fn = fn.get("phr-next");
		}
		return rc;
	}

	/**
	 * getPhraseType -- Returns the type of the node
	 *
	 * Assumes that in input node is pointing at
	 * head of the phrase.  Returns an empty string
	 * if its not pointing at the phrase head.
	 */
	public static String getPhraseType(FeatureNode fn)
	{
		if (fn == null) return "";
		fn = fn.get("phr-head");
		if (fn == null) return "";
      fn = fn.get("phr-type");
		if (fn == null) return "";
      String type = fn.getValue();
		return  type;
	}

	/**
	 * getPhraseLeader -- returns the leading word of the phrase
	 *
	 * For example, in the phrase "(the big red balloon)", the
	 * leader is "balloon".
	 */

	public static FeatureNode getPhraseLeader(FeatureNode fn)
	{
		if (fn == null) return null;
		fn = fn.get("phr-head");
		if (fn == null) return null;
		fn = fn.get("phr-leader");
		if (fn == null) return null;
		fn = fn.get("nameSource");
		return fn;
	}

	/**
	 * Return a list of feature nodes of the words in the phrase.
	 */
	public static void getWordList(FeatureNode word, ArrayList<FeatureNode> words)
	{
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) words.add (wd);

			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				getWordList(fn, words);
			}
			fn = fn.get("phr-next");
		}
	}

	/**
	 * Return the feature node of the first word of the phrase.
	 */
	public static FeatureNode getFirstWord(FeatureNode word)
	{
		FeatureNode fn = word.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) return wd;

			FeatureNode subf = fn.get("phr-head");
			if (subf != null)
			{
				wd = getFirstWord(fn);
				if (wd != null) return wd;
			}
			fn = fn.get("phr-next");
		}
		return null;
	}

	/**
	 * up -- go up the tree structure, if possible
	 */
	public static FeatureNode up(FeatureNode fn)
	{
		if (fn == null) return null;
		fn = fn.get("phr-head");
		if (fn == null) return null;
		fn = fn.get("phr-root");
		return fn;
	}

	/**
	 * getNextPhrase -- iterator, return next phrase head.
	 *
	 * If the input argument is pointing into a list of
	 * phrases, this iterator will return the next phrase
	 * in the list.  It will skip over any words in the list.
	 *
	 * Sample usage of recursive walk:
	 *     void doStuff (FeatureNode head) {
	 *         FeatureNode fn = PhraseTree.getFirstPhrase(head);
	 *         while (fn != null) {
	 *             doStuff (fn);
	 *             fn = PhraseTree.getNextPhrase(fn);
	 *         }
	 *     }
	 */
	public static FeatureNode getFirstPhrase(FeatureNode ph)
	{
		if (ph == null) return ph;
		ph = ph.get("phr-head");
		return getNextPhrase(ph);
	}

	public static FeatureNode getNextPhrase(FeatureNode ph)
	{
		if (ph == null) return ph;

		ph = ph.get("phr-next");
		while (ph != null)
		{
			FeatureNode h = ph.get("phr-head");
			if (h != null) return ph;
			ph = ph.get("phr-next");
		}
		return null;
	}

	/**
	 * foreach() -- called for every phrase in the tree
	 */
	public static Boolean foreach(FeatureNode fn, FeatureNodeCallback cb)
	{
		if (fn == null) return false;

		Boolean stop = cb.FNCallback(fn);
		if (stop) return stop;

		fn = fn.get("phr-head");
		while (fn != null)
		{
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) {
				stop = foreach(fn, cb);
				if (stop) return stop;
			}
			fn = fn.get("phr-next");
		}
		return false;
	}

	/**
	 * equiv -- Returns true if two phrases are the same object
	 *
	 * Returns true if two feature nodes are pointing at the same
	 * object.  Note that this is not the same as "equals":
	 * two different phrases are equal when they match up
	 * node-for-node; however, the are not equivalent unless
	 * they are the same object.
	 *
	 * This routine returns false if the two phrases are not
	 * the same object.
	 */
	public static Boolean equiv(FeatureNode a, FeatureNode b)
	{
		if ((a == null) && (b == null)) return true;
		if ((a == null) || (b == null)) return false;
		if (a.equiv(b)) return true;
		a = a.get("phr-head");
		b = b.get("phr-head");
		if ((a == null) || (b == null)) return false;
		if (a.equiv(b)) return true;

		return false;
	}

	/**
	 * Return true if "child" is is in the tree "head"
	 */
	public static Boolean contains(FeatureNode parent, FeatureNode child)
	{
		if (equiv(parent, child)) return true;

		FeatureNode p = parent.get("phr-head");
		p = p.get("phr-next"); // skip type while we're here.
		while (p != null)
		{
			FeatureNode h = p.get("phr-head");
			if ((h != null) && contains(p, child)) return true;
			p = p.get("phr-next");
		}
		return false;
	}

};

/* =========================== END OF FILE ================== */
