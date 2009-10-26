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

package relex;

import java.io.Serializable;
import java.util.ArrayList;
import java.lang.Comparable;
import java.util.List;
import java.util.UUID;

import relex.feature.Atom;
import relex.feature.FeatureForeach;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkableView;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;
import relex.stats.SimpleTruthValue;
import relex.tree.PhraseTree;

/**
 * A ParsedSentence object stores all of the syntactic and semantic
 * information about a sentence parse. The data in the Object is
 * gradually built up by RelationExtractor.
 *
 * ParsedSentence contains:
 * 1. A FeatureNode with metaData about the parse (i.e., the number
 *    of conjunctions)
 * 2. An ArrayList of FeatureNodes (leafConstituents) representing each
 *    word in the sentence. -- the parse data can be found by checking
 *    the links in these words.
 * 3. Strings representing the original sentence, and representations
 *    of its parses
 * 4. Sets of relations, with the semantic data from the sentence.
 * 5. A TruthValue (inherited from Atom) that ranks the relative
 *    likelihood of this parse of being a correct (meaningful) parse
 *    of the sentence.
 */
public class ParsedSentence
	extends Atom
	implements Serializable, Comparable<ParsedSentence>
{
	private static final long serialVersionUID = -5518792541801263127L;

	// Unique ID string identifying this parse.
	private String idString;

	// Back-pointer to collection of other parses for this sentence
	private Sentence sentence;

	// String containing the original sentence
	private String original;

	// String containing the ascii-art tree output by the link grammar parser.
	private String linkString;

	// A string containing the Penn tree-bank style markup,
	// aka "phrase structure" markup, for example
	// (S (NP I) (VP am (NP a big robot)) .)
	private String phraseString;

	private String errorString;

	// Metadata about the sentence; primarily, this consists of diagnostic
	// info returned by the link grammar parser.
	private FeatureNode metaData;

	// An ArrayList of FeatureNodes, each one representing a word in the
	// sentence.  If there are no "link islands", each can be reached by
	// following arcs from the others.
	private ArrayList<FeatureNode> leafConstituents;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	// Constructor.
	public ParsedSentence(String originalString)
	{
		original = originalString;
		linkString = null;
		errorString = "";
		phraseString = null;
		leafConstituents = new ArrayList<FeatureNode>();
	}

	public void setMetaData(FeatureNode f) {
		metaData = f;
	}

	public FeatureNode getMetaData() {
		return metaData;
	}

	public String getOriginalSentence() {
		return original;
	}

	public String getIDString() {
		return idString;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence s) {
		sentence = s;
	}

	public void setIDString(String str) {
		idString = str;
	}

	public String getLinkString() {
		return linkString;
	}

	public void setLinkString(String str) {
		linkString = str;
	}

	public String getPhraseString() {
		return phraseString;
	}

	public void setPhraseString(String str) {
		phraseString = str;
	}

	public void setErrorString(String eString) {
		errorString = eString;
	}

	public String getErrorString() {
		return errorString;
	}

	/* -------------------------------------------------------------------- */
	public int getNumWords()
	{
		return leafConstituents.size();
	}

	/**
	 * Return the i'th word in the sentence, as a feature node
	 */
	public FeatureNode getWordAsNode(int i)
	{
		return leafConstituents.get(i);
	}

	/**
	 * Return the i'th lemmatized word in the sentence, as a string.
	 * This is the "root form" of the word, and not the original word.
	 */
	public String getWord(int i)
	{
		return LinkableView.getWordString(getWordAsNode(i));
	}

	/**
	 * Return the i'th word in the sentence, as a string
	 * This is the original form of the word, and not its lemma.
	 */
	public String getOrigWord(int i)
	{
		return LinkableView.getOrigWordString(getWordAsNode(i));
	}

	/**
	 * Return the part-of-speech of the i'th word in the sentence
	 */
	public String getPOS(int i)
	{
		return LinkableView.getPOS(getWordAsNode(i));
	}

	/**
	 * Return the offset, in the original sentence, to the first
	 * character of the i'th word in the sentence.
	 */
	public int getStartChar(int i)
	{
		return LinkableView.getStartChar(getWordAsNode(i));
	}

	public void addWord(FeatureNode w)
	{
		leafConstituents.add(w);
	}

	/**
	 * Return feature node for the indicated word. Return null
	 * if the word cannot be found in the sentence.  The input
	 * word may be either the word as it appears in the sentence,
	 * or its morphological root.
	 *
	 * If there are multiple occurances of a word in a sentence,
	 * this will return only the left-most such occurance.
	 */
	public FeatureNode findWord(String word)
	{
		class word_cb implements FeatureNodeCallback
		{
			String match_word;
			FeatureNode found;
			word_cb(String mw)
			{
				match_word = mw;
				found = null;
			}

			Boolean test(FeatureNode fn, FeatureNode fstr)
			{
				if (null == fstr) return false;
				String w = fstr.getValue();
				if (match_word.equals(w))
				{
					found = fn;
					return true;
				}
				return false;
			}
			public Boolean FNCallback(FeatureNode fn)
			{
				Boolean rc = test(fn, fn.get("orig_str"));
				if (rc) return rc;
				rc = test(fn, fn.get("str"));
				if (rc) return rc;
				return false;
			}
		}
		word_cb cb = new word_cb(word);
		FeatureForeach.foreachWord(getLeft(), cb);
		return cb.found;
	}

	/**
	 * assign_id -- Add UUID tags to all word nodes in the parse. 
	 * These UUID tags are used by opencog, and by other parts
	 * (e.g. the anaphora resolution code) to uniquely identify
	 * individual word instances across multiple parses of 
	 * multiple sentences of multiple documents or conversations.
	 */
	public void assign_id()
	{
		FeatureNode fn = getLeft();
		fn.add("uuid", "LEFT-WALL@" + getIDString());
		fn = fn.get("NEXT");
		while (fn != null)
		{
			String word = fn.get("orig_str").getValue();
			UUID guid = UUID.randomUUID();
			String guid_word = word + "@" + guid;
			fn.add("uuid", guid_word);
			fn = fn.get("NEXT");
		}
	}

	/* -------------------------------------------------------------------- */
	/* Various different views of the parsed sentence */

	/**
	 * Shows the full feature structure of the parse as it can be found by
	 * tracing links from the left-most word. Islands will be missed.
	 */
	public String fullParseString()
	{
		if (getLeft() != null)
			return getLeft().toString();
		return "";
	}

	/**
	 * Returns a list of the words in the sentence, marked up according to
	 * which "part of speech" they are.  Thus, for example:
	 * "The big red baloon floated away." becomes
	 * LEFT-WALL The.det big.adj red.adj balloon.noun float.verb away.prep .
	 *
	 * @deprecated -- please make use of the output systems in the 
	 * output directory, or create one, if you really need it!
	 */
	@Deprecated
	public String printPartsOfSpeech()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < leafConstituents.size(); i++) {
			sb.append(getWord(i));
			LinkableView w = new LinkableView(getWordAsNode(i));
			String pos = w.getPOS();
			if (pos != null && !pos.equals("WORD"))
				sb.append("." + pos);
			if (i < leafConstituents.size() - 1)
				sb.append(" ");
			// else
			// sb.append(".");
		}
		return sb.toString();
	}

	public String toString()
	{
		return original;
	}

	/* ---------------------------------------------------------------- */
	/**
	 * Call the callback on each relation in the sentence
	 */
	public Boolean foreach(RelationCallback cb)
	{
		return RelationForeach.foreach(getLeft(), cb);
	}

	public Boolean foreach(FeatureNodeCallback cb)
	{
		return RelationForeach.foreach(getLeft(), cb);
	}

	public List<FeatureNode> getLeafConstituents()
	{
	    return this.leafConstituents;
	}
	
	/* ---------------------------------------------------------------- */
	/**
	 * @return the FeatureNode representing the left-most word in the sentence.
	 */
	public FeatureNode getLeft()
	{
		return leafConstituents.get(0);
	}

	/**
	 * @return the phrase tree associated with this parse
	 */
	public PhraseTree getPhraseTree()
	{
		return new PhraseTree(getLeft());
	}

	/* ---------------------------------------------------------------- */
	/* Return unpacked meta information about parse, and ranking too */

	public int getAndCost()
	{
		return getMeta("and_cost");
	}

	public int getDisjunctCost()
	{
		return getMeta("disjunct_cost");
	}

	public int getLinkCost()
	{
		return getMeta("link_cost");
	}

	public int getNumSkippedWords()
	{
		return getMeta("num_skipped_words");
	}

	private int getMeta(String str)
	{
		FeatureNode fn = metaData.get(str);
		if (fn == null) return -1;
		String val = fn.getValue();
		return Integer.parseInt(val);
	}

	/**
	 * Perform a crude parse-ranking based on Link-grammar output.
	 * The ranking will be stored as the "confidence" of the
	 * TruthValue associated with this parse.
	 *
	 * @returns the score that was assigned.
	 *
	 * A classic example of competing parses for a sentence is:
	 * (S (NP I) (VP saw (NP the man) (PP with (NP the binoculars))) .)
	 * (S (NP I) (VP saw (NP (NP the man) (PP with (NP the binoculars)))) .)
	 * The ranker below gives both about equal scores.
	 *
	 */
	public double simpleRankParse()
	{
		SimpleTruthValue stv = new SimpleTruthValue();
		truth_value = stv;
		stv.setMean(1.0);  // 1.0 == true -- this is a parse.

		// The weights used here are rather ad-hoc; but the
		// basic idea is that we want to penalize skipped words
		// strongly, but disjunct costs not as much. Low link
		// costs are the tiebreaker.
		double weight = 0.4 * getNumSkippedWords();
		weight += 0.2 * getDisjunctCost();
		weight += 0.06 * getAndCost();
		weight += 0.012 * getLinkCost();

		weight = Math.exp(-weight);

		stv.setConfidence(weight);
		return weight;
	}

	/**
	 * Take the current parse confidence, and rescale it by the
	 * indicated amount.  The method simpleRankParse() must have
	 * been previously called to perform the initial ranking.
	 */
	public void rescaleRank(double weight)
	{
		SimpleTruthValue stv = (SimpleTruthValue) truth_value;
		double confidence = stv.getConfidence();
		confidence *= weight;
		stv.setConfidence(confidence);
	}

	public double getRank()
	{
		SimpleTruthValue stv = (SimpleTruthValue) truth_value;
		return stv.getConfidence();
	}

	public int compareTo(ParsedSentence that)
	{
		if (this.getRank() < that.getRank()) return +1;
		return -1;
	}

	public int hashCode()
	{
		if (original == null)
			return 0;
		return original.hashCode() | leafConstituents.size();
	}

	public boolean equals(Object x)
	{
		if (! (x instanceof ParsedSentence))
			return false;
		ParsedSentence p = (ParsedSentence)x;
		if (original == null)
			return p.original == null;
		else
			return original.equals(p.original) && this.leafConstituents.equals(p.leafConstituents);
	}

} // end ParsedSentence
