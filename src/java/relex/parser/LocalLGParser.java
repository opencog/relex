/*
 * Copyright 2009 Borislav Iordanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.linkgrammar.LGService;
import org.linkgrammar.LinkGrammar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import relex.ParsedSentence;
import relex.Sentence;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;
import relex.stats.SimpleTruthValue;

public class LocalLGParser extends LGParser
{
	private static final Logger logger = LoggerFactory.getLogger(LocalLGParser.class);
	private static final double min_score = -0.001;
	private static final double score_bump = 0.001;

	private AtomicBoolean initialized = new AtomicBoolean(false);

	public void init()
	{
		if (initialized.getAndSet(true)) return;

		// Must set language or dictionary path BEFORE initializing!
		if (_lang != null)
			LinkGrammar.setLanguage(_lang);

		if (_dict_path != null)
			LinkGrammar.setDictionariesPath(_dict_path);

		try
		{
			LinkGrammar.init();
		}
     	catch (Exception e)
		{
			String msg = "Error: LinkGrammar initialization error";
			if (_lang != null)
				msg = "Error: LinkGrammar: Unknown language \"" + _lang + "\"";
			if (_dict_path != null)
				msg = "Error: LinkGrammar: Invalid dictionary path \"" + _dict_path + "\"";
			throw new RuntimeException(msg);
		}

		LinkGrammar.setMaxLinkages(_max_linkages);
		LGService.configure(_config);
	}

	public void close()
	{
		LinkGrammar.close();
		initialized.set(Boolean.FALSE);
	}

	public void doFinalize()
	{
		LinkGrammar.doFinalize();
	}

	public Sentence parse(String sentence) throws ParseException
	{
		Long starttime;
		starttime = System.currentTimeMillis();

		Sentence sntc = new Sentence();

		boolean ignoreFirst = false; // true if first word is LEFT-WALL
		boolean ignoreLast = false;  // true if first word is RIGHT_WALL
		logger.trace("about to parse [{}]", sentence);
		LinkGrammar.parse(sentence);
		logger.trace("parsed [{}]", sentence);

		int numParses = LinkGrammar.getNumLinkages();
		logger.trace("found {} parse(s)", numParses);

		ArrayList<ParsedSentence> parses = new ArrayList<ParsedSentence>();

		if (numParses < 1)
		{
			logger.warn("Warning: No parses found for:\n{}",
			     sentence);
			return sntc;
		}

		for (int i = 0; i < numParses && i < _config.getMaxLinkages(); i++)
		{
			logger.trace("making linkage for parse {}", i);
			LinkGrammar.makeLinkage(i);

			logger.trace("making sentence for parse {}", i);
			ParsedSentence s = new ParsedSentence(sentence);

			// Add words
			int numWords = LinkGrammar.getNumWords();
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

			int skip_count = 0;
			int[] skip_map = new int[numWords];
			for (int w = 0; w < numWords; w++)
			{
				String wordString = LinkGrammar.getLinkageWord(w);
				logger.trace(" Processing Word {}", wordString);

				// If link-grammar guesses a word, it will add [?] to the
				// end of it. The regex guess will add [!] and the spell
				// guesser will add [~]. The run-on tokenizer adds [&].
				// Remove these.
				int idx = wordString.indexOf("[");
				if (0 < idx)
					wordString = wordString.substring(0,idx);

				// If link-grammar cannot use a word, it will put square
				// brackets around it. We really do not want these, they
				// garble up processing for us.
				wordString = wordString.replace("[","");
				wordString = wordString.replace("]","");

				// In Russian, some words (suffixes) can be zero-length.
				if (0 == wordString.length())
				{
					skip_count ++;
					skip_map[w] = w-skip_count;
					continue;
				}
				skip_map[w] = w-skip_count;

				if (wordString.equals("RIGHT-WALL"))
				{
					ignoreLast = true;
				}
				else
				{
					FeatureNode fn = new FeatureNode();
					if (wordString.equals("LEFT-WALL")) leftWall = fn;
					// LEFT-WALL should always be first word, so throw an
					// exception if it was not.
					if (leftWall == null)
						throw new RuntimeException("Invalid parse: " +
							"first word is not left wall");

					// Set the word
					LinkableView.setWord(fn, wordString);

					// Create a feature "this" which points to the linkable
					fn.set("this", fn);

					// set "wall" to point to the left wall
					fn.set("wall", leftWall);
					if (lastFN != null)
					{
						LinkableView.setNext(lastFN, fn);
						LinkableView.setPrev(fn, lastFN);
					}

					// XXX This should be removed, it really doesn't belong here.
					if (Character.isUpperCase(wordString.charAt(0)))
						LinkableView.setEntityFlag(fn);

					s.addWord(fn);

					// Add char-index information to the feature node
					// FYI, the JNI call (*env)->NewStringUTF(env, str);
					// will return NULL if str is utf8-encoded Japanese or Chinese. Go figure.
					String tokenString = wordString;
					if (null != tokenString) tokenString = tokenString.toLowerCase(); // normalize cases
					else tokenString = "";

					// System.err.println("DOING INFO FOR " + tokenString);
					String sentenceString = sentence.toLowerCase();
					Integer timesSeenInt = timesTokenSeen.get(tokenString);
					int timesSeen = (timesSeenInt == null ? 0 : timesSeenInt.intValue());

					// "x<=" means we will do at least once
					for (int x = 0; x <= timesSeen; x++)
					{
						startChar = sentenceString.indexOf(tokenString,startChar);
					}

					timesTokenSeen.put(tokenString, new Integer(timesSeen + 1));
					int endChar = (startChar >= 0 ? startChar + tokenString.length() : -1);
					// System.err.println("INFO IS " + startChar + "," + endChar);
					LinkableView.setCharIndices(fn, startChar, endChar, w-skip_count);

					// Increment index to start looking for next tokenString
					// after the current one. Use "max" to prevent decreasing
					// index in the case the tokenString end is -1
					startChar = Math.max(startChar, endChar);
					lastFN = fn;
				}
			}

			logger.trace("Done with parse {}", i);

			// set meta data
			FeatureNode meta = new FeatureNode();
			meta.set("num_skipped_words", new FeatureNode(Integer.toString(
					LinkGrammar.getNumSkippedWords())));
			meta.set("disjunct_cost", new FeatureNode(Double.toString(
					LinkGrammar.getLinkageDisjunctCost())));
			meta.set("link_cost", new FeatureNode(Double.toString(
					LinkGrammar.getLinkageLinkCost())));
			meta.set("num_violations", new FeatureNode(Integer.toString(
					LinkGrammar.getLinkageNumViolations())));
			s.setMetaData(meta);

			// add linkage and tree structure
			logger.trace("Adding Linkage Structure");
			addLinkageStructure(s, ignoreFirst, ignoreLast, skip_map);
			if (_config.isStoreConstituentString())
			{
				logger.trace("Adding Tree Structure");
				s.setPhraseString(LinkGrammar.getConstituentString());
			}
			logger.trace("Ready To Finish");

			// add to return list
			parses.add(s);
		}

		sntc.setParses(parses);
		sntc.setNumParses(LinkGrammar.getNumLinkages());

		Long now = System.currentTimeMillis();
		Long elapsed = now - starttime;
		logger.debug("Parse setup time: {} milliseconds", elapsed);
		logger.trace("Done with parse");

		return sntc;
	}

	private void addLinkageStructure(ParsedSentence s,
	                                 boolean ignoreFirst,
	                                 boolean ignoreLast,
	                                 int[] skip_map)
	{
		// Russian sentences can have 'blank words' in them which we skip.
		// That means that the count of actual non-empty words no longer
		// matches the link-parser count.  Technically, this is a link-parser
		// bug, but fixing it would require a major re-write of the parser.
		// int length = LinkGrammar.getNumWords();
		int length = skip_map[skip_map.length-1] + 1;
		int numLinks = LinkGrammar.getNumLinks();
		s.setLinkString(LinkGrammar.getLinkString());
		for (int i = 0; i < numLinks; i++)
		{
			boolean bad = false;
			int left = LinkGrammar.getLinkLWord(i);
			int right = LinkGrammar.getLinkRWord(i);
			left = skip_map[left];
			right = skip_map[right];
			if (ignoreLast && (right == length - 1))
			{
				bad = true;
			}
			if (ignoreFirst)
			{
				if (left == 0) bad = true;
				--left;
				--right;
			}
			if (!bad)
			{
				/*
				 * System.err.println("ADDING LINK " + left + "," + right);
				 * System.err.println("labels: " + cLinkLLabel(i) + ":" +
				 * cLinkRLabel(i) + ":" + cLinkLabel(i) + ":");
				 */
				FeatureNode f = new FeatureNode();
				LinkView.setLinkFeatures(f,
						LinkGrammar.getLinkLLabel(i),
						LinkGrammar.getLinkRLabel(i),
						LinkGrammar.getLinkLabel(i),
						s.getWordAsNode(left),
						s.getWordAsNode(right)
				);
			}
		}

		// Ignore the last word (RIGHT-WALL) if this is set.
		if (ignoreLast) length--;

		for (int i = 0; i < length; i++)
		{
			// We'll hang the disjunct right off the word node.
			FeatureNode f = s.getWordAsNode(i);
			String dj = LinkGrammar.getLinkageDisjunct(i);
			if (dj != null)
			{
				f.set("DISJUNCT", new FeatureNode(dj));
			}
		}
	}

	public String getVersion()
	{
		return LinkGrammar.getVersion();
	}

	public static void main(String[] args)
	{
		LocalLGParser lp = new LocalLGParser();
		Sentence sntc = lp.parse(
			"After the signing, the ambassadors affirmed both sides' readiness for full-scale development of bilateral relations.");
		System.err.println("FOUND " + sntc.getParses().size() + " sentence(s)");
		sntc = lp.parse("Mike saw the man with the telescope.");
		if (sntc.getParses().size() > 0) {
			ParsedSentence sentence = sntc.getParses().get(0);
			System.err.println("ParsedSentence.getLinkString():\n"+ sentence.getLinkString());
			System.err.println("ParsedSentence.getErrorString():\n"+ sentence.getErrorString());
		} else {
			System.err.println("No parse found for sentence");
		}
		lp.close();
		lp.doFinalize();
	}
}
