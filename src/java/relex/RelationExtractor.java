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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;

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
// import relex.corpus.QuotesParensSentenceDetector;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.entity.EntityMaintainer;
import relex.entity.EntityTagger;
import relex.entity.EntityTaggerFactory;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.frame.Frame;
import relex.morphy.Morphy;
import relex.morphy.MorphyFactory;
import relex.output.OpenCogScheme;
import relex.output.ParseView;
import relex.output.PrologList;
import relex.output.RawView;
import relex.output.SimpleView;
import relex.output.StanfordView;
import relex.parser.LGParser;
import relex.parser.LocalLGParser;
import relex.parser.RemoteLGParser;
import relex.stats.TruthValue;
import relex.stats.SimpleTruthValue;
import relex.tree.PhraseMarkup;
import relex.tree.PhraseTree;

/**
 * The RelationExtractor class provides the central processing
 * point for parsing sentences and extracting dependency
 * relationships from them.  The main() proceedure is usable
 * as a stand-alone document analyzer; it supports several
 * flags modifying the displayed output.
 *
 * The primary interface is the processSentence() method,
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

	/** The LinkParserClient to be used - this class isn't thread safe! */
	private RelexContext context;

	/** Syntax processing */
	private LGParser parser;

	/** Dependency processing */
	private SentenceAlgorithmApplier sentenceAlgorithmApplier;

	/** Penn tree-bank style phrase structure markup. */
	private PhraseMarkup phraseMarkup;
	public boolean do_tree_markup;

	/** Anaphora resolution */
	// XXX these should probably be moved to class Document!
	public Antecedents antecedents;
	private Hobbs hobbs;
	public boolean do_anaphora_resolution;

	/** Document - holder of sentences */
	Document doco;

	/** Stanford parser compatibility mode */
	public boolean do_stanford;

	/** Penn tagset compatibility mode */
	public boolean do_penn_tagging;

	/** Expand preposition markup to two dependencies. */
	public boolean do_expand_preps;

	/** Perform entity substitution before parsing */
	public boolean do_pre_entity_tagging;

	/** Perform entity tagging after parse */
	public boolean do_post_entity_tagging;
	public EntityTagger tagger;

	/** Statistics */
	private ParseStats stats;

	/* ---------------------------------------------------------- */
	/* Constructors, etc. */

	public RelationExtractor()
	{
		init(false);
	}
	public RelationExtractor(boolean useSocket)
	{
		init(useSocket);
	}
	private void init(boolean useSocket)
	{
		parser = useSocket ? new RemoteLGParser() : new LocalLGParser();
		parser.getConfig().setStoreConstituentString(true);
		parser.getConfig().setLoadSense(true);
		Morphy morphy = MorphyFactory.getImplementation(MorphyFactory.DEFAULT_SINGLE_THREAD_IMPLEMENTATION);
		context = new RelexContext(parser, morphy);

		sentenceAlgorithmApplier = new SentenceAlgorithmApplier();

		setMaxParses(DEFAULT_MAX_PARSES);
		setMaxParseSeconds(DEFAULT_MAX_PARSE_SECONDS);
		setMaxCost(DEFAULT_MAX_PARSE_COST);

		phraseMarkup = new PhraseMarkup();
		antecedents = new Antecedents();
		hobbs = new Hobbs(antecedents);
		do_anaphora_resolution = false;

		doco = new Document();

		do_tree_markup = false;

		do_stanford = false;
		do_penn_tagging = false;
		do_expand_preps = false;
		do_pre_entity_tagging = false;
		do_post_entity_tagging = false;
		tagger = null;

		stats = new ParseStats();
		sumtime = new TreeMap<String,Long>();
		cnttime = new TreeMap<String,Long>(); 
	}

	public String getVersion()
	{
		return parser.getVersion() + "\t" + Version.getVersion();
	}

	/* ---------------------------------------------------------- */
	/* Control parameters, etc. */
	/**
	 * Set the max number of parses.
	 * This will NOT reduce processing time; all parses are still computed,
	 * but only this many are returned.
	 */
	public void setMaxParses(int maxParses)
	{
		parser.getConfig().setMaxLinkages(maxParses);
	}

	public void setMaxCost(int maxCost) {
		parser.getConfig().setMaxCost(maxCost);
	}

	public void setAllowSkippedWords(boolean allow) {
		parser.getConfig().setAllowSkippedWords(allow);
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		parser.getConfig().setMaxParseSeconds(maxParseSeconds);
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

	public Sentence processSentence(String sentence)
	{
		return processSentence(sentence, null);
	}

	public Sentence processSentence(String sentence,
	                                 EntityMaintainer entityMaintainer)
	{
		starttime = System.currentTimeMillis();
		if (entityMaintainer == null)
		{
			entityMaintainer = new EntityMaintainer();
		}

		Sentence sntc = null;
		try
		{
			if (verbosity > 0) starttime = System.currentTimeMillis();
			sntc = parseSentence(sentence, entityMaintainer);
			if (verbosity > 0) reportTime("Link-parsing: ");

			for (ParsedSentence parse : sntc.getParses())
			{
				if (do_expand_preps) 
				{
					parse.getLeft().set("expand-preps", new FeatureNode("T"));
				}

				if (do_post_entity_tagging && (tagger != null))
				{
					// Markup feature node graph with entity info,
					// so that the relex algs (next step) can see them.
					tagger.tagEntities(sentence);
					tagger.tagParse(parse);
				}

				// Markup feature node graph with entity info,
				// so that the relex algs (next step) can see them.
				entityMaintainer.tagConvertedSentence(parse);

				// The actual relation extraction is done here.
				sentenceAlgorithmApplier.applyAlgs(parse, context);
				if (do_stanford) sentenceAlgorithmApplier.extractStanford(parse, context);
				if (do_penn_tagging) sentenceAlgorithmApplier.pennTag(parse, context);

				// Strip out the entity markup, so that when the
				// sentence is printed, we don't print gunk.
				entityMaintainer.repairSentence(parse.getLeft());

				// Also do a Penn tree-bank style phrase structure markup.
				if (do_tree_markup)
				{
					phraseMarkup.markup(parse);

					// Repair the entity-mangled tree-bank string.
					PhraseTree pt = new PhraseTree(parse.getLeft());
					parse.setPhraseString(pt.toString());
				}

			}

			// Assign a simple parse-ranking score, based on LinkGrammar data.
			sntc.simpleParseRank();

			// Perform anaphora resolution
			if (do_anaphora_resolution)
			{
				hobbs.addParse(sntc);
				hobbs.resolve(sntc);
			}
		}
		catch(Exception e)
		{
			System.err.println("Error: Failed to process sentence: " + sentence);
			e.printStackTrace();
		}
		if (verbosity > 0) reportTime("RelEx processing: ");
		return sntc;
	}

	/**
	 * Parses a sentence, using the parser. The private ArrayList of
	 * currentParses is filled with the ParsedSentences Uses an optional
	 * EntityMaintainer to work on a converted sentence.
	 */
	private Sentence
	parseSentence(String sentence, EntityMaintainer entityMaintainer)
	{
		if (entityMaintainer != null)
		{
			entityMaintainer.convertSentence(sentence,null);
			sentence = entityMaintainer.getConvertedSentence();
		}
		if (sentence == null) return null;

		String orig_sentence = entityMaintainer.getOriginalSentence();
		Sentence sent = null;
		if (sentence.length() < DEFAULT_MAX_SENTENCE_LENGTH) {
			sent = parser.parse(sentence);
		} else {
			System.err.println("Sentence too long!: " + sentence);
			sent = new Sentence();
		}
		sent.setSentence(orig_sentence);
		return sent;
	}

	/* ---------------------------------------------------------- */
	// Provide some basic timing info
	Long starttime;
	TreeMap<String,Long> sumtime; 
	TreeMap<String,Long> cnttime; 

	private void reportTime(String msg)
	{
		Long now = System.currentTimeMillis();
		Long elapsed = now - starttime;
		starttime = now;

		Long sum = sumtime.get(msg);
		Long cnt = cnttime.get(msg);
		if (sum == null)
		{
			sum = 0L;
			cnt = 0L;
		}
		cnt ++;
		sum += elapsed;
		sumtime.put(msg, sum);
		cnttime.put(msg, cnt);

		Long avg = sum / cnt;
		System.err.println(msg + elapsed + " milliseconds (avg=" 
			+ avg + " millisecs, cnt=" + cnt + ")");
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

	// Punish chunks whose length is other than 3.
	private static void discriminate(ChunkRanker ranker)
	{
		ArrayList<LexChunk> chunks = ranker.getChunks();
		for (LexChunk ch : chunks)
		{
			int sz = ch.size();
			double weight = sz-3;
			if (weight < 0) weight = - weight;
			weight = 1.0 - 0.2 * weight;

			// twiddle the confidence of the chunk
			TruthValue tv = ch.getTruthValue();
			SimpleTruthValue stv = (SimpleTruthValue) tv;
			double confidence = stv.getConfidence();
			confidence *= weight;
			stv.setConfidence(confidence);
		}
	}

	/* ---------------------------------------------------------- */
	/**
	 * Main entry point
	 */
	public static void main(String[] args)
	{
		String callString = "RelationExtractor" +
			" [-a (perform anaphora resolution)]" +
			" [-c (show plain output)]" +
			" [--expand-preps (show expanded prepositions)]" +
			" [-f (show frame output)]" +
			" [-g (pre-process with GATE entity detector)]" +
			" [--g-post (post-process with GATE entity detector)]" +
			" [-h (show this help)]" +
			" [-l (show parse links)]" +
			" [-m (show parse metadata)]" +
			" [--maxParseSeconds N]" +
			" [-n max number of parses to display]" +
			" [-o (show opencog scheme output)]" +
			" [--pa (show phrase-based lexical chunks)]" +
			" [--pb (show pattern-based lexical chunks)]" +
			" [--pc (show relational lexical chunks)]" +
			" [--penn (generate Penn treebank-style POS tags)]" +
			" [--prolog (show prolog output)]" +
			" [-q (do NOT show relations)]" +
			" [-r (show raw output)]" +
			" [-s Sentence (in quotes)]" +
			" [--stanford (generate stanford-compatible output)]" +
			" [-t (show parse tree)]" +
			" [-v (verbose, full graph output)]" +
			" [-x (show cerego XML output)]" +
			" [--html filename (output HTML to file)]"
				;
		HashSet<String> flags = new HashSet<String>();
		flags.add("-a");
		flags.add("-c");
		flags.add("--expand-preps");
		flags.add("-f");
		flags.add("-g");
		flags.add("--g-post");
		flags.add("-h");
		flags.add("-l");
		flags.add("-m");
		flags.add("-o");
		flags.add("--pa");
		flags.add("--pb");
		flags.add("--pc");
		flags.add("--penn");
		flags.add("--prolog");
		flags.add("-q");
		flags.add("-r");
		flags.add("--stanford");
		flags.add("-t");
		flags.add("-v");
		flags.add("-x");
		HashSet<String> opts = new HashSet<String>();
		opts.add("-n");
		opts.add("-s");
		opts.add("--maxParseSeconds");
		opts.add("--html");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		String sentence = null;
		int maxParses = 1;
		int maxParseSeconds = 6;
		PrintWriter html = null;

		// Check for optional command line arguments.
		try
		{
			String opt = commandMap.get("-s");
			if (opt != null) sentence = opt;

			opt = commandMap.get("-n");
			if (opt != null) maxParses = Integer.parseInt(opt);

			opt = commandMap.get("--maxParseSeconds");
			if (opt != null) maxParseSeconds = Integer.parseInt(opt);

			opt = commandMap.get("--html");
			if (opt != null) html = new PrintWriter(new FileWriter(opt));
		}
		catch (Exception e)
		{
			System.err.println("Unrecognized parameter.");
			System.err.println(callString);
			e.printStackTrace();
			return;
		}

		if (commandMap.get("-h") != null)
		{
			System.err.println(callString);
			return;
		}

		// If generating OpenCog Scheme, delimit output.
		if (commandMap.get("-o") != null)
			System.out.print("scm\n");

		if (html != null) html.println("<html>");

		RelationExtractor re = new RelationExtractor(false);
		re.setAllowSkippedWords(true);
		re.setMaxParses(maxParses);
		re.setMaxParseSeconds(maxParseSeconds);
		System.out.println("; Version: " + re.getVersion());

		// Don't run anaphora if -o is set, this will be done in a
		// distinct stage that wipes out the first run.
		if ((commandMap.get("-a") != null) &&
		    (commandMap.get("-o") == null))
		{
			re.do_anaphora_resolution = true;
			re.do_tree_markup = true;
		}

		if ((commandMap.get("-t") != null) ||
		    (commandMap.get("--pa") != null) ||
		    (commandMap.get("--pb") != null) ||
		    (commandMap.get("--pc") != null))
		{
			re.do_tree_markup = true;
		}

		if (commandMap.get("--stanford") != null)
		{
			re.do_stanford = true;
		}

		if (commandMap.get("--penn") != null)
		{
			re.do_penn_tagging = true;
		}

		if (commandMap.get("--expand-preps") != null)
		{
			re.do_expand_preps = true;
		}

		EntityTagger gent = null;
		if ((commandMap.get("-g") != null) ||
		    (commandMap.get("--g-post") != null))
		{
			re.starttime = System.currentTimeMillis();
			gent = EntityTaggerFactory.get();
			gent.tagEntities(""); // force initialization to measure initialization time
			re.tagger = gent;
			re.reportTime("Entity Detector Initialization: ");
		}
		if (commandMap.get("-g") != null) re.do_pre_entity_tagging = true;
		if (commandMap.get("--g-post") != null) re.do_post_entity_tagging = true;

		// If sentence is not passed at command line, read from standard input:
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		DocSplitter ds = DocSplitterFactory.create();

		// QuotesParens is currently broken, it fails to handle possesives.
		// QuotesParensSentenceDetector ds = QuotesParensSentenceDetector.create();

		ParseView ceregoView = new ParseView();
		OpenCogScheme opencog = null;
		if (commandMap.get("-o") != null)
		{
			opencog = new OpenCogScheme();
			if (commandMap.get("-l") != null)
			{
				opencog.setShowLinkage(true);
			}
			opencog.setShowRelex(true);
			if (commandMap.get("-q") != null)
			{
				opencog.setShowRelex(false);
			}
			if (commandMap.get("-f") != null)
			{
				opencog.setShowFrames(true);
			}
			if (commandMap.get("-a") != null)
			{
				opencog.setShowAnaphora(true);
			}
		}

		// Print normal frames, but only if -o has not been invoked.
		Frame frame = null;
		if ((commandMap.get("-f") != null) &&
		    (commandMap.get("-o") == null))
		{
			frame = new Frame();
		}

		int sentence_count = 0;
		boolean more_input = true;
		while(more_input)
		{
			// If no sentence specified on the command line
			// (with the "-s" flag), then read it from stdin.
			while (sentence == null)
			{
				try {
					sentence = stdin.readLine();
					if ((sentence == null) || "END.".equals(sentence))
					{
						more_input = false;
						sentence = null;
						break;
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
				System.out.println("; SENTENCE: ["+sentence+"]");
				EntityMaintainer em = null;
				if (re.do_pre_entity_tagging)
				{
					re.starttime = System.currentTimeMillis();
					em = new EntityMaintainer();
					gent.reset();
					em.set(gent);
					// gent.makeEntityTagger(sentence));
					re.reportTime("Gate processing: ");
				}
				Sentence sntc = re.processSentence(sentence,em);
				re.doco.addSentence(sntc);

				if (html != null)
					html.printf("<div id='relex-%d'><table><tr><td>%d: %s</td></tr><tr>\n",
						sentence_count, sentence_count, escape(sentence));

				sentence_count ++;
				re.stats.bin(sntc);

				int np = sntc.getParses().size();
				if (np > maxParses) np = maxParses;

				// chunk ranking stuff
				ChunkRanker ranker = new ChunkRanker();
				double parse_weight = 1.0 / ((double) np);
				double votes = 1.0e-20;
				if (commandMap.get("--pa") != null) votes += 1.0;
				if (commandMap.get("--pb") != null) votes += 2.0;
				if (commandMap.get("--pc") != null) votes += 1.0;
				votes = 1.0 / votes;
				votes *= parse_weight;

				// Print output
				int numParses = 0;
				for (ParsedSentence parse: sntc.getParses())
				{
					if (commandMap.get("-o") == null)
					{
						System.out.println(sentence);
						System.out.println("\n====\n");
						System.out.println("Parse " + (numParses+1) +
					             	" of " + sntc.getParses().size());
					}

					if (commandMap.get("-r") != null)
					{
						System.out.println("\n====\n");
						System.out.println("Dependency graph:\n");
						System.out.println(RawView.printZHeads(parse.getLeft()));
						System.out.println("\n======\n");
					}

					if (commandMap.get("-t") != null) {
						System.out.println("\n" + parse.getPhraseString());
						if (html != null)
							html.printf("<td colspan='2'>%s</td></tr><tr>", escape(parse.getPhraseString()));
					}

					// Don't print the link string if xml output is enabled.
					// XML parsers choke on it.
					if ((commandMap.get("-l") != null) &&
					    (commandMap.get("-o") == null))
						System.out.println("\n" + parse.getLinkString());

					if (commandMap.get("-m") != null)
					{
						System.out.println(parse.getMetaData().toString() + "\n");
					}

					if (commandMap.get("-o") == null)
					{
						// Print simple parse ranking
						Double confidence = parse.getTruthValue().getConfidence();
						String pconfidence = confidence.toString().substring(0,6);
						System.out.println("Parse confidence: " + pconfidence);
						System.out.println(
							"cost vector = (UNUSED=" + parse.getNumSkippedWords() +
							" DIS=" + parse.getDisjunctCost() +
							" AND=" + parse.getAndCost() +
							" LEN=" + parse.getLinkCost() + ")");
					}

					// Verbose graph.
					if (commandMap.get("-v") != null)
						// System.out.println("\n" + parse.fullParseString());
						System.out.println("\n" + 
						 	parse.getLeft().toString(LinkView.getFilter()));

					if ((commandMap.get("-q") == null) &&
					    (commandMap.get("-o") == null))
					{
						System.out.println("\n======\n");
						System.out.println("Dependency relations:\n");
						System.out.println(SimpleView.printRelations(parse));
						System.out.println("\n======\n");

						if (html != null)
							html.printf("<td valign='top'><pre>%s</pre></td>\n", escape(SimpleView.printRelations(parse)));
					}

					if (commandMap.get("--pa") != null)
					{
						System.out.println("Phrase tree-based lexical chunks:");
						LexicalChunker chunker = new PhraseChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						ranker.add(chunker.getChunks(), parse.getTruthValue(), votes);
					}
					if (commandMap.get("--pb") != null)
					{
						System.out.println("Pattern-matching lexical chunks:");
						LexicalChunker chunker = new PatternChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						ranker.add(chunker.getChunks(), parse.getTruthValue(), 2.0*votes);
					}
					if (commandMap.get("--pc") != null)
					{
						System.out.println("Relation-based lexical chunks:");
						LexicalChunker chunker = new RelationChunker();
						chunker.findChunks(parse);
						prt_chunks(chunker.getChunks());
						ranker.add(chunker.getChunks(), parse.getTruthValue(), votes);
					}

					if (commandMap.get("--prolog") != null)
					{
						PrologList pl = new PrologList();
						System.out.println(
							pl.toPrologList(parse.getLeft(), 
								PrologList.getDefaultFilter(),
								true));
						System.out.println("\n======\n");
					}

					if (commandMap.get("--stanford") != null)
					{
						System.out.println(
							StanfordView.printRelations(parse, re.do_penn_tagging));
						System.out.println("\n======\n");
					}

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
					if ((commandMap.get("-f") != null) &&
					    (commandMap.get("-o") == null))
					{
						re.starttime = System.currentTimeMillis();
						String fin = SimpleView.printRelationsAlt(parse);
						String[] fout = frame.process(fin);
						re.reportTime("Frame processing: ");

						Arrays.sort(fout);

						for (int i=0; i < fout.length; i++) {
							System.out.println(fout[i]);
						}

						if (html != null) {
							html.print("<td valign='top'><pre>");
							for (String f : fout) {
								html.println(escape(f));
							}
							html.println("</pre></td>");
						}

						System.out.println("\nFraming rules applied:\n");
						System.out.println(frame.printAppliedRules());
					}
					if (commandMap.get("-o") != null)
					{
						opencog.setParse(parse);
						System.out.println(opencog.toString());
					}

					if (html != null) html.println("</tr></table></div>");

					if (++numParses >= maxParses) break;
				}

				if (0 < ranker.getChunks().size())
				{
					discriminate(ranker);
					System.out.println("\nLexical Chunks:\n" +
					             ranker.toString());
				}

				if (re.do_anaphora_resolution &&
				    (commandMap.get("-o") == null))
				{
					System.out.println("\nAntecedent candidates:\n"
					                   + re.antecedents.toString());
				}

				// Print out the stats every now and then.
				if (sentence_count%5 == 0)
				{
					System.err.println ("\n" + re.stats.toString());
				}

				if (commandMap.get("-s") != null) break;
				sentence = ds.getNextSentence();
			}
			if (commandMap.get("-s") != null) break;
		}

		if (html != null) {
			html.println("</html>");
			html.close();
		}

		// Dump the list of document sentences
		if (commandMap.get("-o") != null)
		{
			System.out.println(opencog.printDocument(re.doco));
		}
		System.out.println("; Bye.");
		if (commandMap.get("-o") != null)
		{
			System.out.println(".\nexit");
		}
		System.exit(0);
	}

	private static String escape(String sent)
	{
		sent = sent.replace("&", "&amp;");
		sent = sent.replace("<", "&lt;");
		sent = sent.replace(">", "&rt;");
		return sent;
	}

}

/* ============================ END OF FILE ====================== */
