/*
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
 *
 * Copyright (c) 2008 Novamente LLC
 * Copyright (c) 2008, 2009, 2014 Linas Vepstas <linasvepstas@gmail.com>
 */

package relex;

import java.io.Serializable;
import java.util.ArrayList;
import java.lang.Comparable;
import java.util.List;
import java.util.UUID;

import relex.feature.Atom;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkableView;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;
import relex.stats.SimpleTruthValue;

/**
 * A ParsedSentence object stores all of the syntactic and semantic
 * information about a sentence parse. The data in the Object is
 * gradually built up by RelationExtractor.
 *
 * ParsedSentence contains:
 * 1. A FeatureNode with metaData about the parse (e.g. the total costs
 *    returned by LG.)
 * 2. An ArrayList of FeatureNodes (leafConstituents) representing each
 *    word in the sentence. -- the parse data can be found by checking
 *    the links in these words.
 * 3. Strings representing the original sentence, and representations
 *    of its parses
 * 4. Sets of relations, with the semantic data from the sentence.
 * 5. A TruthValue (inherited from Atom) that ranks the relative
 *    likelihood of this parse of being a correct (meaningful) parse
 *    of the sentence.
 *
 * XXX Most or all of this data should probably be anchored in the
 * feature-node graph.
 */
public class ParsedSentence
	extends Atom
	implements Serializable, Comparable<ParsedSentence>
{
	private static final long serialVersionUID = -5518792541801263127L;

	// Unique ID string identifying this parse.
	private String idString;

	// Back-pointer to collection of other parses for this sentence
	private Sentence _sentence;

	// String containing the original sentence
	private String original;

	// String containing the ascii-art tree output by the link grammar parser.
	private String linkString;

	// A string containing the HPSG-style (Penn tree-bank) markup,
	// aka "phrase structure" markup, for example
	// (S (NP We) (VP are (NP show-room dummies)) .)
	private String phraseString;

	private String errorString;

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

	public void setMetaData(FeatureNode f)
	{
		// Get the left wall, and anchor the meta-data there.
		getLeft().get("wall").set("meta", f);
	}

	/**
	 * Return metadata about the sentence; primarily, this consists
	 * of diagnostic info returned by the link grammar parser.
	 */
	public FeatureNode getMetaData()
	{
		return getLeft().get("wall").get("meta");
	}

	public String getOriginalSentence() {
		return original;
	}

	public String getIDString() {
		return idString;
	}

	public Sentence getSentence() {
		return _sentence;
	}

	public void setSentence(Sentence s) {
		_sentence = s;
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
	 * assign_id -- Add UUID tags to all word nodes in the parse.
	 * These UUID tags are used by opencog to uniquely identify
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

	/* ---------------------------------------------------------------- */
	/* Return unpacked meta information about parse, and ranking too */

	public double getDisjunctCost()
	{
		return getMeta("disjunct_cost");
	}

	public double getLinkCost()
	{
		return getMeta("link_cost");
	}

	public double getNumSkippedWords()
	{
		return getMeta("num_skipped_words");
	}

	private double getMeta(String str)
	{
		FeatureNode fn = getMetaData().get(str);
		if (fn == null) return -1;
		String val = fn.getValue();
		return Double.parseDouble(val);
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
		//
		// The weights: Having num-skipped-words >0 is terrible, unless
		// these are commas or bad punctuation.  So we punish this strongly.
		// A disjunct cost of 5 should about balance out one skipped
		// word or so. Disjunct costs above 3 or 4 are bad news, at
		// least for short sentences (bad scores are more common for long
		// sentences).  Thus, weights of 1.0 and 0.2 seem about right.
		//
		// The current link-grammar commonly uses small differences in
		// the disjunct cost, such as 0.1 and even 0.05, to break ties.
		// Such a disjunct cost should overwhelm the link cost of 2 or
		// 3 or 4, and so link-costs are given a very low weight. The
		// problem is that the new link-grammar now uses long-distance
		// links much more freely, e.g. WV. And of course, the Xp links
		// are always very high link cost. We almost shouldn't count
		// these ...  Anyway, this suggests that 0.2*0.05 ~ 3*0.003 is a
		// reasonable choice. Or maybe even 0.004 or 0.005 for the link
		// cost weight...
		//
		// This is all a manula balancing act, until automatic weighting
		// is implemented in link-grammar.
		double weight = 1.0 * getNumSkippedWords();
		weight += 0.2 * getDisjunctCost();
		weight += 0.003 * getLinkCost();

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
