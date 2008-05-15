package relex.concurrent;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import relex.ParsedSentence;
import relex.RelexInfo;
import relex.algs.SentenceAlgorithmApplier;
import relex.entity.EntityMaintainer;
import relex.parser.LinkParser;
import relex.tree.PhraseMarkup;

/**
 * Processes a sentence using the given LinkParserClient. When processing is
 * finished, returns the LPC to the pool. 
 *  
 * @author muriloq
 */
public class RelexTask implements Callable<RelexTaskResult> {
	public static final int DEBUG = 0;
	// arguments 
	private int index;
	private String sentence;
	private EntityMaintainer entityMaintainer;
	
	// Reusable, shared processors
	private LinkParser lp; 
	private SentenceAlgorithmApplier sentenceAlgorithmApplier;
	private PhraseMarkup phraseMarkup;

	// Used in mutual exclusion, must be returned to the pool
	private RelexContext context; 
	private BlockingQueue<RelexContext> pool;
	
	public RelexTask(int index, String sentence,
			EntityMaintainer entityMaintainer,
			LinkParser lp, 
			SentenceAlgorithmApplier sentenceAlgorithmApplier,
			PhraseMarkup phraseMarkup,
			RelexContext context, BlockingQueue<RelexContext> pool){
		this.index = index;
		this.entityMaintainer = entityMaintainer;
		this.lp = lp; 
		this.sentenceAlgorithmApplier = sentenceAlgorithmApplier;
		this.phraseMarkup = phraseMarkup;
		this.context = context;
		this.pool = pool;
		this.sentence = sentence;
	}
	
	public RelexTaskResult call() {
		try {
			if (DEBUG > 0) System.out.println("[" + index + "] Start processing "+ sentence);
			String convertedSentence = entityMaintainer.getConvertedSentence();
			if (DEBUG > 0) System.out.println("[" + index + "] End entity detection");
			ArrayList<ParsedSentence> parses = lp.parse(convertedSentence, context.getLinkParserClient());
			RelexInfo ri = new RelexInfo(sentence, parses);
			if (DEBUG > 0) System.out.println("[" + index + "] End parsing");

			int i = 0;
			for (ParsedSentence parse : parses) {
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (DEBUG > 0) 
					System.out.println("[" + index+ "] end post-processing sentence " + 
							(i++) + "/"+ parses.size());
			}
			return new RelexTaskResult(index, sentence, entityMaintainer, ri);
		} finally {
			if (DEBUG > 0)
				System.out.println("[" + index + "] End processing");
			try {
				pool.put(context);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (DEBUG > 0) System.out.println("[" + index + "] Release resources");
		}
	}
	
	public String toString(){
		return index+": "+sentence;
	}
}