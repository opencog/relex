package relex.parser;
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

import java.util.ArrayList;
import java.util.HashMap;

import relex.ParsedSentence;
import relex.RelexProperties;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

/**
 * This class provides another level of abstraction over the linkparser client,
 * allowing communication with ParsedSentence objects.
 */
public class LinkParser extends Parser
{
	private static final int verbosity = 0;

	private static final boolean storePhraseString = true;

	private static LinkParser singleton = null;

	private LinkParserClient lpc = null;

	public void setMaxParses(int maxParses) {
		lpc.setMaxParses(maxParses);
	}

	public void setAllowSkippedWords(boolean val) {
		lpc.setAllowSkippedWords(val);
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		lpc.setMaxParseSeconds(maxParseSeconds);
	}

	public void setMaxCost(int maxCost) {
		lpc.setMaxCost(maxCost);
	}

	public boolean isPastTenseForm(String word) {
		return lpc.isPastTenseForm(word);
	}

	public boolean isEntity(String word) {
		return lpc.isEntity(word);
	}

	public String simpleParse(String sentence) {
		System.out.println("Calling cParse for sentence: " + sentence);
		lpc.execParse(sentence);
		if (lpc.getNumLinkages() > 0) {
			lpc.makeLinkage(0);
			return lpc.getConstituentString() + "\n";
		}
		return null;
	}

	public String cleanSentenceString(String sentence) {
		String sen = sentence.replace('[', '(');
		sen = sen.replace(']', ')');
		sen = sen.replace('"', ' ');
		return sen;
	}

	/**
	 * Creates a ParsedSentence from a String representation of the sentence. 
	 * The ParsedSentence has ConstituentNodes for every tokenized word. It 
	 * does not have a Tree structure or Linkage structure.
	 */
	private ArrayList<ParsedSentence> tokenizeAndParseSentence(String sentence)
	{
		boolean ignoreFirst = false; // true if first word is LEFT-WALL
		boolean ignoreLast = false;  // true if first word is RIGHT_WALL
		if (verbosity >= 5) System.out.println("about to parse [" + sentence + "]");
		lpc.execParse(cleanSentenceString(sentence));
		if (verbosity >= 5) System.out.println("parsed [" + sentence + "]");
		
		int numParses = lpc.getNumLinkages();
		if (verbosity >= 5) System.out.println("found " + numParses + " parse(s)");
		
		ArrayList<ParsedSentence> parsedSentences = new ArrayList<ParsedSentence>();

		if ((numParses < 1) || 
		    (!lpc.getAllowSkippedWords() && lpc.getNumSkippedWords() > 0))
		{
			System.err.println("Warning: No parses found for:\n" +
			     sentence);
			return parsedSentences;
		}

		for (int i = 0; i < numParses && i < lpc.getMaxParses(); i++) {
			if (verbosity >= 5) System.out.println("making linkage for parse " + i);
			lpc.makeLinkage(i);
			
			if (verbosity >= 5) System.out.println("making sentence for parse " + i);
			ParsedSentence s = new ParsedSentence(sentence);
			
			// add words
			int numWords = lpc.getNumWords();
			FeatureNode lastFN = null;
			FeatureNode leftWall = null;
			/*
			 * Note that we should adjust cNumWords to count the words in the
			 * sentence -- this will have other consequences when we try to do
			 * partial parses -- must take this action carefully
			 */
			
			/*
			 * In order to find the start character of each word/token, we need
			 * to keep track of previous instances of the word (in case the same
			 * word appears more than once in the sentence). The variables
			 * "index" and "timesTokenSeen" are used for this:
			 */
			int startChar = 0;
			HashMap<String,Integer> timesTokenSeen = new HashMap<String,Integer>();
			// System.out.println("LINKPARSER NUMWORDS=" + numWords + "
			// SKIPPED=" + cNumSkippedWords());
			for (int w = 0; w < numWords; w++) {
				String wordString = lpc.getWord(w);
				if (verbosity >= 5) System.out.println(" Processing Word " + wordString);

				if (wordString.equals("RIGHT-WALL")){
					ignoreLast = true;
				} else {
					LinkableView fnv = new LinkableView(new FeatureNode());
					if (wordString.equals("LEFT-WALL")) leftWall = fnv.fn();
					// LEFT-WALL should always be first word, so throw an exception if it was not
					if (leftWall == null) throw new RuntimeException("Invalid parse: first word is not left wall");
					
					// set the word and part-of-speach
					fnv.setWordAndPos(lpc.getWord(w));
					
					// create a feature "this" which points to the linkable
					fnv.fn().set("this", fnv.fn());
					
					// set "wall" to point to the left wall
					fnv.fn().set("wall", leftWall);
					if (lastFN != null) {
						LinkableView.setNext(lastFN, fnv.fn());
						fnv.setPrev(lastFN);
					}
					s.addWord(fnv.fn());

					// Add char-index information to the feature node
					String tokenString = lpc.getWord(w).toLowerCase(); // normalize cases
					
					// System.out.println("DOING INFO FOR " + tokenString);
					String sentenceString = sentence.toLowerCase();
					Integer timesSeenInt = timesTokenSeen.get(tokenString);
					int timesSeen = (timesSeenInt == null ? 0 : timesSeenInt.intValue());
					for (int x = 0; x <= timesSeen; x++) { // "x<=" means we will do at least once
						startChar = sentenceString.indexOf(tokenString,startChar);
					}
					timesTokenSeen.put(tokenString, new Integer(timesSeen + 1));
					int endChar = (startChar >= 0 ? startChar + tokenString.length() : -1);
					// System.out.println("INFO IS " + startChar + "," + endChar);
					fnv.setCharIndices(startChar, endChar, w);
					// Increment index to start looking for next tokenString after the current one.
					// Use "max" to prevent decreasing index in the case the tokenString end is -1
					startChar = Math.max(startChar, endChar);
					lastFN = fnv.fn();
				}

			}
			if (verbosity >= 5) System.out.println("Done with parse " + i);
			// set meta data
			FeatureNode meta = new FeatureNode();
			meta.set("skipped", new FeatureNode(Integer.toString(lpc.getNumSkippedWords())));
			meta.set("disj_cost", new FeatureNode(Integer.toString(lpc.getLinkageDisjunctCost())));
			meta.set("num_violations", new FeatureNode(Integer.toString(lpc.getLinkageNumViolations())));
			s.setMetaData(meta);
			// add linkage and tree structure
			if (verbosity >= 5) System.out.println("Adding Linkage Structure");
			addLinkageStructure(s, ignoreFirst, ignoreLast);
			if (verbosity >= 5) System.out.println("Adding Tree Structure");
			if (storePhraseString) addTreeStructure(s);
			if (verbosity >= 5) System.out.println("Ready To Finish");
			
			// add to return list
			parsedSentences.add(s);
		}
		
		if (verbosity >= 5) System.out.println("Done with tokenizeAndParse");
		return parsedSentences;
	}

	private void addLinkageStructure(ParsedSentence s, boolean ignoreFirst, boolean ignoreLast) {
		int length = lpc.getNumWords();
		int numLinks = lpc.getNumLinks();
		s.setLinkString(lpc.getLinkString());
		
		for (int i = 0; i < numLinks; i++) {
			boolean bad = false;
			int left = lpc.getLinkLWord(i);
			int right = lpc.getLinkRWord(i);
			if (ignoreLast && (right == length - 1)) {
				bad = true;
			}
			if (ignoreFirst) {
				if (left == 0) bad = true;
				--left;
				--right;
			}
			if (!bad) {
				/*
				 * System.out.println("ADDING LINK " + left + "," + right);
				 * System.out.println("labels: " + cLinkLLabel(i) + ":" +
				 * cLinkRLabel(i) + ":" + cLinkLabel(i) + ":");
				 */
				new LinkView(new FeatureNode()).setLinkFeatures(
						lpc.getLinkLLabel(i), 
						lpc.getLinkRLabel(i), 
						lpc.getLinkLabel(i), 
						s.getWordAsNode(left), 
						s.getWordAsNode(right)
				);
			}
		}
	}

	/**
	 * Uses the Penn-tree-bank style "phrase structure".
	 */
	private void addTreeStructure(ParsedSentence s) {
		String treeString = lpc.getConstituentString();
		s.setPhraseString(treeString);
	}

	/**
	 * Given a sentence, returns a vector of parses, ordered by likelihood.
	 * 
	 * @param sentence
	 * @return only a single parse for now. Need to add more parses by modifying calls to tokenizeAndParseSentence
	 */
	public ArrayList<ParsedSentence> parse(String sentence) {
		return tokenizeAndParseSentence(sentence);
	}

	/*
	 * (see bug #48 at kmi's bugzilla //frees the C memory space public void
	 * finalize() { close(); }
	 */

	public void close() {
		lpc.close();
	}

	public static String retrievePathName() {
		return RelexProperties.getProperty("relex.parser.LinkParser.pathname");
	}

	public void reset() {
		close();
		lpc.init(retrievePathName());
	}

	private LinkParser(LinkParserClient _lpc) {
		lpc = _lpc;
		if (verbosity > 3) System.out.println("LinkParser: initializing client with pathname:" + retrievePathName());
		lpc.init(retrievePathName());
	}

	public static LinkParser createSingletonInstance(LinkParserClient _lpc) {
		if (singleton != null) throw new RuntimeException("LinkParser Singleton has already been created");
		singleton = new LinkParser(_lpc);
		return singleton;
	}

	public static boolean isSingletonCreated() {
		return singleton != null;
	}

	// defaults to JNINewClient
	public static LinkParser getSingletonInstance()
	{
		if (singleton == null)
			singleton = LinkParser.createSingletonInstance(LinkParserJNINewClient.getSingletonInstance());
		return singleton;
	}

	// Main is for unit testing only.
	public static void main(String[] args)
	{
		LinkParser lp = LinkParser.createSingletonInstance(LinkParserJNINewClient.getSingletonInstance());
		ArrayList<ParsedSentence> v = lp.parse(
			"After the signing, the ambassadors affirmed both sides' readiness for full-scale development of bilateral relations.");
		System.out.println("FOUND " + v.size() + " sentence(s)");
		System.out.println("HAD is past: " + lp.isPastTenseForm("had"));
		System.out.println("HAVE is past: " + lp.isPastTenseForm("have"));
		v = lp.parse("Mike saw the man with the telescope.");
		if (v.size() > 0) {
			ParsedSentence sentence = v.get(0);
			System.out.println("ParsedSentence.getLinkString():\n"+ sentence.getLinkString());
			System.out.println("ParsedSentence.getErrorString():\n"+ sentence.getErrorString());
		} else {
			System.out.println("No parse found for sentence");
		}
		if (args.length > 0)
			System.out.println(args[0] + " is past: "+ lp.isPastTenseForm(args[0]));
		lp.close();
	}
}

// =============================== End of File =========================================
