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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import relex.algs.SentenceAlgorithmApplier;
import relex.concurrent.RelexContext;
import relex.corpus.EntityMaintainerFactory;
// import relex.corpus.QuotesParensSentenceDetector;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.entity.EntityInfo;
import relex.entity.EntityMaintainer;
import relex.frame.Frame;
import relex.morphy.Morphy;
import relex.morphy.MorphyFactory;
import relex.output.CompactView;
import relex.output.SimpleView;
import relex.parser.LinkParser;
import relex.parser.LinkParserClient;
import relex.parser.LinkParserJNINewClient;
import relex.parser.LinkParserSocketClient;
import relex.tree.PhraseMarkup;

/**
 * The WebFormat class provides the central processing
 * point for parsing sentences and extracting semantic
 * relationships from them.  The main() proceedure is usable
 * as a stand-alone document analyzer; it supports several
 * flags modifying the displayed output.
 *
 * The primarey interface is the processSentence() method,
 * which accepts one sentence at a time, parses it, and extracts
 * relationships from it.
 */
public class WebFormat
{
	public static final int verbosity = 1;

	public static final int DEFAULT_MAX_PARSES = 100;
	public static final int DEFAULT_MAX_SENTENCE_LENGTH = 1024;
	public static final int DEFAULT_MAX_PARSE_SECONDS = 30;
	public static final int DEFAULT_MAX_PARSE_COST = 1000;
	public static final String DEFAULT_ALGS_FILE = "./data/relex-semantic-algs.txt";

	/** The LinkParserClient to be used - this class isn't thread safe! */
	private RelexContext context;

	/** Syntax processing */
	private LinkParser parser;

	/** Semantic processing */
	private SentenceAlgorithmApplier sentenceAlgorithmApplier;

	/** Penn tree-bank style phrase structure markup. */
	private PhraseMarkup phraseMarkup;

	/* ---------------------------------------------------------- */
	/* Constructors, etc. */

	public WebFormat(boolean useSocket)
	{
		parser = new LinkParser();

		LinkParserClient lpc = (useSocket) ? new LinkParserSocketClient() : LinkParserJNINewClient.getSingletonInstance();
		lpc.init();
		Morphy morphy = MorphyFactory.getImplementation(MorphyFactory.DEFAULT_SINGLE_THREAD_IMPLEMENTATION);
		context = new RelexContext(lpc, morphy);

		sentenceAlgorithmApplier = new SentenceAlgorithmApplier();

		setMaxParses(DEFAULT_MAX_PARSES);
		setMaxParseSeconds(DEFAULT_MAX_PARSE_SECONDS);
		setMaxCost(DEFAULT_MAX_PARSE_COST);

		phraseMarkup = new PhraseMarkup();
	}

	/* ---------------------------------------------------------- */
	/* Control parameters, etc. */
	/**
	 * Set the max number of parses.
	 * This will NOT reduce processing time; all parses are still computed,
	 * but only this many are returned.
	 */
	public void setMaxParses(int maxParses) {
		context.getLinkParserClient().setMaxParses(maxParses);
	}

	public void setMaxCost(int maxCost) {
		context.getLinkParserClient().setMaxCost(maxCost);
	}

	public void setAllowSkippedWords(boolean allow) {
		context.getLinkParserClient().setAllowSkippedWords(allow);
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		context.getLinkParserClient().setMaxParseSeconds(maxParseSeconds);
	}

	/* ---------------------------------------------------------- */

	public RelexInfo processSentence(String sentence)
	{
		return processSentence(sentence, null);
	}

	public RelexInfo processSentence(String sentence,
	                                 EntityMaintainer entityMaintainer)
	{
		starttime = System.currentTimeMillis();
		if (entityMaintainer == null)
		{
			entityMaintainer = new EntityMaintainer(sentence,
		                               new ArrayList<EntityInfo>());
		}

		RelexInfo ri = parseSentence(sentence, entityMaintainer);

		for (ParsedSentence parse : ri.parsedSentences)
		{
			// Markup feature node graph with entity info,
			// so that the relex algs (next step) can see them.
			entityMaintainer.prepareSentence(parse.getLeft());

			// The actual relation extraction is done here.
			sentenceAlgorithmApplier.applyAlgs(parse, context);

			// Strip out the entity markup, so that when the
			// sentence is printed, we don't print gunk.
			entityMaintainer.repairSentence(parse.getLeft());

			// Also do a Penn tree-bank style phrase structure markup.
			phraseMarkup.markup(parse);
		}

		return ri;
	}

	/**
	 * Parses a sentence, using the parser. The private ArrayList of
	 * currentParses is filled with the ParsedSentences Uses an optional
	 * EntityMaintainer to work on a converted sentence.
	 */
	private RelexInfo
	parseSentence(String sentence, EntityMaintainer entityMaintainer)
	{
		if (entityMaintainer != null) {
			sentence = entityMaintainer.getConvertedSentence();
		}
		if (sentence == null) return null;

		ArrayList<ParsedSentence> parses = null;
		if (sentence.length() < DEFAULT_MAX_SENTENCE_LENGTH) {
			parses = parser.parse(sentence, context.getLinkParserClient());
		} else {
			System.err.println("Sentence too long!: " + sentence);
			parses = new ArrayList<ParsedSentence>();
		}
		RelexInfo ri = new RelexInfo(sentence, parses);
		return ri;
	}

	/* ---------------------------------------------------------- */
	// Provide some basic timing info
	Long starttime;

	/* ---------------------------------------------------------- */
	/**
	 * Main entry point
	 */
	public static void main(String[] args)
	{
		String callString = "WebFormat" +
			" [-f (show frame output)]" +
			" [-g (use GATE entity detector)]" +
			" [-h (show this help)]" +
			" [-l (show parse links)]" +
			" [-m (do not show parse metadata)]" +
			" [-n max number of parses to display]" +
			" [-t (do not show constituent tree)]" +
			" [--maxParseSeconds N]";
		HashSet<String> flags = new HashSet<String>();
		flags.add("-f");
		flags.add("-g");
		flags.add("-h");
		flags.add("-l");
		flags.add("-m");
		flags.add("-t");
		HashSet<String> opts = new HashSet<String>();
		opts.add("-n");
		opts.add("--maxParseSeconds");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		String sentence = null;
		int maxParses = 30;
		int maxParseSeconds = 60;

		CompactView cv = new CompactView();

		if (commandMap.get("-m") != null) cv.showMetadata(false);
		if (commandMap.get("-t") != null) cv.showConstituents(false);

		// Check for optional command line arguments.
		try
		{
			maxParses = commandMap.get("-n") != null ?
				Integer.parseInt(commandMap.get("-n").toString()) : 1;

			maxParseSeconds = commandMap.get("--maxParseSeconds") != null ?
				Integer.parseInt(commandMap.get("--maxParseSeconds").toString()) : 60;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Unrecognized parameter.");
			System.err.println(callString);
			return;
		}

		if (commandMap.get("-h") != null)
		{
			System.err.println(callString);
			return;
		}

		cv.setMaxParses(maxParses);

		WebFormat re = new WebFormat(false);
		re.setAllowSkippedWords(true);
		re.setMaxParses(maxParses);
		re.setMaxParseSeconds(maxParseSeconds);

		EntityMaintainerFactory gem = null;
		if (commandMap.get("-g") != null)
		{
			re.starttime = System.currentTimeMillis();
			gem = EntityMaintainerFactory.get();
			gem.makeEntityMaintainer(""); // force initialization to measure initialization time
		}

		// If sentence is not passed at command line, read from standard input:
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		DocSplitter ds = DocSplitterFactory.create();

		Frame frame = null;
		if (commandMap.get("-f") != null) frame = new Frame();

		System.out.println(cv.header());

		while(true)
		{
			// Read text from stdin.
			while (sentence == null)
			{
				try {
					sentence = stdin.readLine();
					if ((sentence == null) || "END.".equals(sentence))
					{
						System.out.println(cv.footer());
						return;
					}
				} catch (IOException e) {
					System.err.println("Error reading sentence from the standard input!");
				}

				// Buffer up input text, and wait for a whole,
				// complete sentence before continuing.
				ds.addText(sentence + " ");
				sentence = ds.getNextSentence();
			}

			while (sentence != null)
			{
				EntityMaintainer em = null;
				if (gem != null)
				{
					re.starttime = System.currentTimeMillis();
					em = gem.makeEntityMaintainer(sentence);
				}

				RelexInfo ri = re.processSentence(sentence,em);

				System.out.println (cv.toString(ri));

				// Print output
				for (ParsedSentence parse: ri.parsedSentences)
				{
					if (commandMap.get("-f") != null)
					{
						String fin = SimpleView.printRelationsAlt(parse);
						String[] fout = frame.process(fin);
						for (int i=0; i < fout.length; i++) {
							System.out.println(fout[i]);
						}

						System.out.println("\nFraming rules applied:\n");
						System.out.println(frame.printAppliedRules());
					}
				}

				sentence = ds.getNextSentence();
			}
		}
	}
}

/* ============================ END OF FILE ====================== */
