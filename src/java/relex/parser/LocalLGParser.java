package relex.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.linkgrammar.LGService;
import org.linkgrammar.LinkGrammar;

import relex.ParsedSentence;
import relex.Sentence;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

public class LocalLGParser extends LGParser
{
	private static final int verbosity = 0;
	
	private ThreadLocal<Boolean> initialized = new ThreadLocal<Boolean>()
	{
		protected Boolean initialValue()
		{
			return Boolean.FALSE;
		}
	};
	
	public void init()
	{
		if (!initialized.get())
			LinkGrammar.init();
		LGService.configure(config);
		initialized.set(Boolean.TRUE);
	}

	public void close()
	{
		LinkGrammar.close();
		initialized.set(Boolean.FALSE);
	}
	
	public Sentence parse(String sentence) throws ParseException
	{
		if (!initialized.get())
			init();
		Long starttime;
		if (verbosity > 0) starttime = System.currentTimeMillis();

		Sentence sntc = new Sentence();

		boolean ignoreFirst = false; // true if first word is LEFT-WALL
		boolean ignoreLast = false;  // true if first word is RIGHT_WALL
		if (verbosity >= 5) System.out.println("about to parse [" + sentence + "]");
		LinkGrammar.parse(sentence);
		if (verbosity >= 5) System.out.println("parsed [" + sentence + "]");
		
		int numParses = LinkGrammar.getNumLinkages();
		if (verbosity >= 5) System.out.println("found " + numParses + " parse(s)");
		
		ArrayList<ParsedSentence> parses = new ArrayList<ParsedSentence>();

		if ((numParses < 1) || 
		    (!config.isAllowSkippedWords() && LinkGrammar.getNumSkippedWords() > 0))
		{
			System.err.println("Warning: No parses found for:\n" +
			     sentence);
			return sntc;
		}

		for (int i = 0; i < numParses && i < config.getMaxLinkages(); i++)
		{
			if (verbosity >= 5) System.out.println("making linkage for parse " + i);
			LinkGrammar.makeLinkage(i);
			
			if (verbosity >= 5) System.out.println("making sentence for parse " + i);
			ParsedSentence s = new ParsedSentence(sentence);
			
			// add words
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
			// System.out.println("LINKPARSER NUMWORDS=" + numWords + "
			// SKIPPED=" + cNumSkippedWords());
			for (int w = 0; w < numWords; w++)
			{
				String wordString = LinkGrammar.getLinkageWord(w);
				if (verbosity >= 5) System.out.println(" Processing Word " + wordString);

				if (wordString.equals("RIGHT-WALL"))
				{
					ignoreLast = true;
				}
				else
				{
					LinkableView fnv = new LinkableView(new FeatureNode());
					if (wordString.equals("LEFT-WALL")) leftWall = fnv.fn();
					// LEFT-WALL should always be first word, so throw an 
					// exception if it was not.
					if (leftWall == null)
						throw new RuntimeException("Invalid parse: " +
							"first word is not left wall");
					
					// set the word and part-of-speach
					fnv.setWordAndPos(wordString);
					
					// create a feature "this" which points to the linkable
					fnv.fn().set("this", fnv.fn());
					
					// set "wall" to point to the left wall
					fnv.fn().set("wall", leftWall);
					if (lastFN != null)
					{
						LinkableView.setNext(lastFN, fnv.fn());
						fnv.setPrev(lastFN);
					}
					
					if (LinkGrammar.isEntity(wordString) || Character.isUpperCase(wordString.charAt(0)))
						fnv.setEntityFlag();
					if (LinkGrammar.isPastTenseForm(wordString))
						fnv.setTenseVal("past");				
					
					s.addWord(fnv.fn());

					// Add char-index information to the feature node
					String tokenString = LinkGrammar.getWord(w).toLowerCase(); // normalize cases
					
					// System.out.println("DOING INFO FOR " + tokenString);
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
					// System.out.println("INFO IS " + startChar + "," + endChar);
					fnv.setCharIndices(startChar, endChar, w);

					// Increment index to start looking for next tokenString
					// after the current one. Use "max" to prevent decreasing
					// index in the case the tokenString end is -1
					startChar = Math.max(startChar, endChar);
					lastFN = fnv.fn();
				}
			}

			if (verbosity >= 5) System.out.println("Done with parse " + i);

			// set meta data
			FeatureNode meta = new FeatureNode();
			meta.set("num_skipped_words", new FeatureNode(Integer.toString(
					LinkGrammar.getNumSkippedWords())));
			meta.set("and_cost", new FeatureNode(Integer.toString(
					LinkGrammar.getLinkageAndCost())));
			meta.set("disjunct_cost", new FeatureNode(Integer.toString(
					LinkGrammar.getLinkageDisjunctCost())));
			meta.set("link_cost", new FeatureNode(Integer.toString(
					LinkGrammar.getLinkageLinkCost())));
			meta.set("num_violations", new FeatureNode(Integer.toString(
					LinkGrammar.getLinkageNumViolations())));
			s.setMetaData(meta);

			// add linkage and tree structure
			if (verbosity >= 5) System.out.println("Adding Linkage Structure");
			addLinkageStructure(s, ignoreFirst, ignoreLast);			
			if (config.isStoreConstituentString()) 
			{
				if (verbosity >= 5) System.out.println("Adding Tree Structure");
				s.setPhraseString(LinkGrammar.getConstituentString());
			}
			if (verbosity >= 5) System.out.println("Ready To Finish");
			
			// add to return list
			parses.add(s);
		}

		sntc.setParses(parses);
		sntc.setNumParses(LinkGrammar.getNumLinkages());
		
		if (verbosity > 0)
		{
			Long now = System.currentTimeMillis();
			Long elapsed = now - starttime;
			System.out.println("Parse setup time: " + elapsed + " milliseconds");
		}
		if (verbosity >= 5) System.out.println("Done with parse");
		
		return sntc;
	}
	
	private void addLinkageStructure(ParsedSentence s, 
									 boolean ignoreFirst, 
									 boolean ignoreLast)
	{
		int length = LinkGrammar.getNumWords();
		int numLinks = LinkGrammar.getNumLinks();
		s.setLinkString(LinkGrammar.getLinkString());	
		for (int i = 0; i < numLinks; i++)
		{
			boolean bad = false;
			int left = LinkGrammar.getLinkLWord(i);
			int right = LinkGrammar.getLinkRWord(i);
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
				 * System.out.println("ADDING LINK " + left + "," + right);
				 * System.out.println("labels: " + cLinkLLabel(i) + ":" +
				 * cLinkRLabel(i) + ":" + cLinkLabel(i) + ":");
				 */
				new LinkView(new FeatureNode()).setLinkFeatures(
						LinkGrammar.getLinkLLabel(i), 
						LinkGrammar.getLinkRLabel(i), 
						LinkGrammar.getLinkLabel(i), 
						s.getWordAsNode(left), 
						s.getWordAsNode(right)
				);
			}
		}
	}
	
	public static void main(String[] args)
	{
		LocalLGParser lp = new LocalLGParser();
		Sentence sntc = lp.parse(
			"After the signing, the ambassadors affirmed both sides' readiness for full-scale development of bilateral relations.");
		System.out.println("FOUND " + sntc.getParses().size() + " sentence(s)");
		System.out.println("HAD is past: " + LinkGrammar.isPastTenseForm("had"));
		System.out.println("HAVE is past: " + LinkGrammar.isPastTenseForm("have"));
		sntc = lp.parse("Mike saw the man with the telescope.");
		if (sntc.getParses().size() > 0) {
			ParsedSentence sentence = sntc.getParses().get(0);
			System.out.println("ParsedSentence.getLinkString():\n"+ sentence.getLinkString());
			System.out.println("ParsedSentence.getErrorString():\n"+ sentence.getErrorString());
		} else {
			System.out.println("No parse found for sentence");
		}
		if (args.length > 0)
			System.out.println(args[0] + " is past: "+ LinkGrammar.isPastTenseForm(args[0]));
		lp.close();
	}		
}
