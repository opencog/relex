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

package relex.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import relex.ParsedSentence;
import relex.Sentence;
import relex.algs.SentenceAlgorithmApplier;

/**
 * Processes a sentence using the given LinkParserClient. When processing is
 * finished, returns the LPC to the pool.
 *
 * @author muriloq
 */
public class RelexTask implements Callable<RelexTaskResult>
{
	public static final int DEBUG = 0;
	// arguments
	private int index;
	private String sentence;

	// Reusable, shared processors
	private SentenceAlgorithmApplier sentenceAlgorithmApplier;

	// Used in mutual exclusion, must be returned to the pool
	private RelexContext context;
	private BlockingQueue<RelexContext> pool;

	public RelexTask(int index, String sentence,
			SentenceAlgorithmApplier sentenceAlgorithmApplier,
			RelexContext context, BlockingQueue<RelexContext> pool){
		this.index = index;
		this.sentenceAlgorithmApplier = sentenceAlgorithmApplier;
		this.context = context;
		this.pool = pool;
		this.sentence = sentence;
	}

	public RelexTaskResult call()
	{
		try
		{
			if (DEBUG > 0) System.err.println("[" + index + "] Start processing "+ sentence);
			Sentence sntc = null;
			try {
				sntc = context.getParser().parse(sentence);//, context.getLinkParserClient());
			} catch (RuntimeException ex) {
				sntc = new Sentence();
				sntc.setSentence(sentence);
			}

			if (DEBUG > 0) System.err.println("[" + index + "] End parsing");

			int i = 0;
			for (ParsedSentence parse : sntc.getParses())
			{
				try {
					// The actual relation extraction is done here.
					sentenceAlgorithmApplier.applyAlgs(parse, context);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (DEBUG > 0)
					System.err.println("[" + index+ "] end post-processing sentence " +
							(i++) + "/"+ sntc.getParses().size());
			}
			return new RelexTaskResult(index, sentence, sntc);
		}
		finally
		{
			if (DEBUG > 0)
				System.err.println("[" + index + "] End processing");
			try {
				pool.put(context);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (DEBUG > 0) System.err.println("[" + index + "] Release resources");
		}
	}

	public String toString()
	{
		return index + ": " + sentence;
	}
}
