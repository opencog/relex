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
import relex.anaphora.Antecedents;
import relex.anaphora.Hobbs;
import relex.chunk.ChunkRanker;
import relex.chunk.LexChunk;
import relex.chunk.LexicalChunker;
import relex.chunk.PatternChunker;
import relex.chunk.PhraseChunker;
import relex.chunk.RelationChunker;
import relex.concurrent.RelexContext;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.corpus.GateEntityMaintainer;
import relex.corpus.QuotesParensSentenceDetector;
import relex.entity.EntityInfo;
import relex.entity.EntityMaintainer;
import relex.feature.LinkView;
import relex.frame.Frame;
import relex.morphy.Morphy;
import relex.morphy.MorphyFactory;
import relex.output.OpenCogXML;
import relex.output.ParseView;
import relex.output.RawView;
import relex.output.SimpleView;
import relex.parser.LinkParser;
import relex.parser.LinkParserClient;
import relex.parser.LinkParserJNINewClient;
import relex.parser.LinkParserSocketClient;
import relex.tree.PhraseMarkup;

/**
 * The RelationExtractor class provides the central processing
 * point for parsing sentences and extracting semantic 
 * relationships from them.  The main() proceedure is usable
 * as a stand-alone document analyzer; it supports several
 * flags modifying the displayed output.
 *
 * The primarey interface is the processSentence() method,
 * which accepts one sentence at a time, parses it, and extracts
 * relationships from it. This method is stateful: it also 
 * performs anaphora resolution.
 */
public class RelationExtractor
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

	/** Anaphora resolution */
	public Antecedents antecedents;
	private Hobbs hobbs;

	/* ---------------------------------------------------------- */
	/* Constructors, etc. */

	public RelationExtractor(boolean useSocket)
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
		antecedents = new Antecedents();
		hobbs = new Hobbs(antecedents);
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

	/**
	 * Clear out the cache of old sentences.
	 *
	 * The Anaphora resolver keeps a list of sentences previously seen,
	 * so that anaphora resolution can be done. When starting the parse
	 * of a new text, this cache needs to be cleaned out. This is the
	 * way to do so.
	 */
	public void clear()
	{
		antecedents.clear();
		hobbs = new Hobbs(antecedents);
	}

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

			// Assign a simple parse-ranking score, based on LinkGrammar data.
			parse.simpleRankParse();
		}

		// Perform anaphora resolution
		hobbs.addParse(ri);
		hobbs.resolve(ri);
		if (verbosity > 0) reportTime("Relex processing: ");
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

	private void reportTime(String msg)
	{
		Long now = System.currentTimeMillis();
		Long elapsed = now - starttime;
		starttime = now;
		System.out.println(msg + elapsed + " millseconds");
	}

	/* --------------------------------------------------------- */

	private static void prt_chunks(ArrayList<LexChunk> chunks)
	{
		for (LexChunk ch : chunks)
		{
			System.out.println(ch.toString());
		}
		System.out.println("\n======\n");
	}

	/* ---------------------------------------------------------- */
	/**
	 * Main entry point
	 */
	public static void main(String[] args) 
	{
		String callString = "RelationExtractor" + 
			" [-c (show plain output)]" +
			" [-f (show frame output)]" +
			" [-g (use GATE entity detector)]" +
			" [-h (show this help)]" +
			" [-l (show parse links)]" +
			" [-m (show parse metadata)]" +
			" [-n parse-number]" +
			" [-o (show opencog XML output)]" +
			" [--pa (show phrase-based lexical chunks)]" +
			" [--pb (show pattern-based lexical chunks)]" +
			" [--pc (show relational lexical chunks)]" +
			" [-r (show raw output)]" +
			" [-s Sentence (in quotes)]" +
			" [-t (show parse tree)]" +
			" [-v verbose]" +
			" [-x (show cerego XML output)]" +
			" [--maxParseSeconds N]";
		HashSet<String> flags = new HashSet<String>();
		flags.add("-c");
		flags.add("-f");
		flags.add("-g");
		flags.add("-h");
		flags.add("-l");
		flags.add("-m");
		flags.add("-o");
		flags.add("--pa");
		flags.add("--pb");
		flags.add("--pc");
		flags.add("-r");
		flags.add("-t");
		flags.add("-v");
		flags.add("-x");
		HashSet<String> opts = new HashSet<String>();
		opts.add("-n");
		opts.add("-s");
		opts.add("--maxParseSeconds");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		String sentence = null;
		int maxParses = 10;
		int maxParseSeconds = 60;

		// Check for optional command line arguments.
		try
		{
			maxParses = commandMap.get("-n") != null ? 
				Integer.parseInt(commandMap.get("-n").toString()) : 1;

			sentence = commandMap.get("-s") != null ? 
				commandMap.get("-s").toString() : null;

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

		// If generating OpenCog XML, delimit non-xml output.
		if (commandMap.get("-o") != null)
			System.out.print("data\n<!-- ");

		RelationExtractor re = new RelationExtractor(false);
		re.setAllowSkippedWords(true);
		re.setMaxParses(maxParses);
		re.setMaxParseSeconds(maxParseSeconds);

		GateEntityMaintainer gem = null;
		if (commandMap.get("-g") != null)
		{
			re.starttime = System.currentTimeMillis();
			gem = new GateEntityMaintainer();
			gem.initialize();
			re.reportTime("Gate initialization: ");
		}

		// If sentence is not passed at command line, read from standard input:
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		// DocSplitter ds = DocSplitterFactory.create();
		QuotesParensSentenceDetector ds = QuotesParensSentenceDetector.create();

		ParseView ceregoView = new ParseView();
		OpenCogXML opencog = new OpenCogXML();

		Frame frame = null;
		if (commandMap.get("-f") != null) frame = new Frame();

		while(true)
		{
			// If no sentence specified on the command line 
			// (with the "-s" flag), then read it from stdin.
			while (sentence == null)
			{
				System.out.print("% ");
				try {
					sentence = stdin.readLine();
					if ((sentence == null) || "END.".equals(sentence)) 
					{
						System.out.println("Bye.");
						if (commandMap.get("-o") != null)
						{
							System.out.print("-->\n");
							char[] eot = new char[1];
							eot[0] = 0x4;
							System.out.println(new String (eot));
						}
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
				System.out.println("SENTENCE: ["+sentence+"]");
				EntityMaintainer em = null;
				if (gem != null)
				{
					re.starttime = System.currentTimeMillis();
					em = gem.process(sentence);
					re.reportTime("Gate processing: ");
				}

				RelexInfo ri = re.processSentence(sentence,em);
	
				int np = ri.parsedSentences.size();
				if (np > maxParses) np = maxParses;
				ChunkRanker ranker = new ChunkRanker(np);

				// Print output
				int numParses = 0;
				for (ParsedSentence parse: ri.parsedSentences)
				{
					ChunkRanker rollup = new ChunkRanker(1);

					System.out.println(sentence);
					System.out.println("\n====\n");
					System.out.println("Parse " + (numParses+1) + 
					             " of " + ri.parsedSentences.size());

					if (commandMap.get("-r") != null)
					{
						System.out.println("\n====\n");
						System.out.println(RawView.printZHeads(parse.getLeft()));
						System.out.println("\n======\n");
					}
	
					if (commandMap.get("-t") != null)
						System.out.println("\n" + parse.getPhraseString());

					// Don't print the link string if xml output is enabled.
					// XML parsers choke on it.
					if ((commandMap.get("-l") != null) &&
					    (commandMap.get("-o") == null))
						System.out.println("\n" + parse.getLinkString());

					if (commandMap.get("-m") != null)
					{
						System.out.println(parse.getMetaData().toString() + "\n");
					}

					// Print simple parse ranking
					Double confidence = parse.getTruthValue().getConfidence();
					String pconfidence = confidence.toString().substring(0,6);
					System.out.println("Parse confidence: " + pconfidence);
					System.out.println(
						"cost vector = (UNUSED=" + parse.getNumSkippedWords() +
						" DIS=" + parse.getDisjunctCost() +
						" AND=" + parse.getAndCost() +
						" LEN=" + parse.getLinkCost() + ")");

					// Verbose graph.
					if (commandMap.get("-v") != null)
						System.out.println("\n" + parse.getLeft().toString(LinkView.getFilter()));

					System.out.println("\n======\n");
					System.out.println(SimpleView.printRelations(parse));
					System.out.println("\n======\n");
	
					if (commandMap.get("--pa") != null)
					{
						System.out.println("Phrase tree-based lexical chunks:");
						LexicalChunker chunker = new PhraseChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						rollup.add(chunker.getChunks(), parse.getTruthValue());
					}
					if (commandMap.get("--pb") != null)
					{
						System.out.println("Pattern-matching lexical chunks:");
						LexicalChunker chunker = new PatternChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						rollup.add(chunker.getChunks(), parse.getTruthValue());
					}
					if (commandMap.get("--pc") != null)
					{
						System.out.println("Relation-based lexical chunks:");
						LexicalChunker chunker = new RelationChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						rollup.add(chunker.getChunks(), parse.getTruthValue());
					}

					// Now roll them up. Need to do this in two stages
					// to get the probability calculations correct.
					ranker.add(rollup.getChunks(), parse.getTruthValue());

					if (commandMap.get("-c") != null)
					{
						ceregoView.setParse(parse);
						ceregoView.showXML(false);
						System.out.println(ceregoView.printCerego());
						System.out.println("\n======\n");
					}
					if (commandMap.get("-x") != null)
					{
						System.out.print("-->\n");
						ceregoView.setParse(parse);
						ceregoView.showXML(true);
						System.out.println(ceregoView.printCerego());
						System.out.println("\n<!-- ======\n");
					}
					if (commandMap.get("-f") != null)
					{
						re.starttime = System.currentTimeMillis();
						String fin = SimpleView.printRelationsAlt(parse);
						String[] fout = frame.process(fin);
						re.reportTime("Frame processing: ");
						for (int i=0; i < fout.length; i++) {
							System.out.println(fout[i]);
						}

						System.out.println("\nFraming rules applied:\n");
						System.out.println(frame.printAppliedRules());
					}
					if (commandMap.get("-o") != null)
					{
						System.out.print("-->\n");
						opencog.setParse(parse);
						System.out.println(opencog.toString());

						char[] eot = new char[1];
						eot[0] = 0x4;
						System.out.println(new String (eot));
						System.out.println("data\n<!-- ======\n");
					}

					if (++numParses >= maxParses) break;
				}
				if (0 < ranker.getChunks().size())
				{
					System.out.println("\nLexical Chunks:\n" +
					             ranker.toString());
				}
				System.out.println("\nAntecedent candidates:\n"
				                   + re.antecedents.toString());
	
				sentence = ds.getNextSentence();
			}
			if (commandMap.get("-s") != null) break;
		}
	}
}

/* ============================ END OF FILE ====================== */
