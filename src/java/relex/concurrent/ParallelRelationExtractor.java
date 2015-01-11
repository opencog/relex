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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import relex.algs.SentenceAlgorithmApplier;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.morphy.Morphy;
import relex.morphy.MorphyFactory;
import relex.parser.RemoteLGParser;

public class ParallelRelationExtractor {

	private static final String DEFAULT_HOST = "localhost";
	public static final int FIRST_PORT = 9000;
	public static final int CLIENT_POOL_SIZE = 1;

    private ExecutorService exec;

	private BlockingQueue<RelexContext> pool;

	private LinkedBlockingQueue<Future<RelexTaskResult>> results;

	public int count = 0;

	private boolean stop;

	// Thread-safe processors
	/** Syntactic processing */
//	private LinkParser linkParser;

	/** Semantic (RelEx) processing */
	private SentenceAlgorithmApplier sentenceAlgorithmApplier;

	public ParallelRelationExtractor()
	{
		initializePool();
		results = new LinkedBlockingQueue<Future<RelexTaskResult>>();
//		linkParser = new LinkParser();
		sentenceAlgorithmApplier = new SentenceAlgorithmApplier();
	}

	/**
	 * Initialize the pool of LinkParserClients, creating
	 * CLIENT_POOL_SIZE instances, which connects to ports FIRST_PORT,
	 * FIRST_PORT+1, ..., FIRST_PORT+(CLIENT_POOL_SIZE-1)
	 */
	private void initializePool()
	{
		exec = Executors.newFixedThreadPool(CLIENT_POOL_SIZE); // thread pool
		pool = new ArrayBlockingQueue<RelexContext>(CLIENT_POOL_SIZE);
		Morphy morphy = MorphyFactory.getImplementation(MorphyFactory.DEFAULT_MULTI_THREAD_IMPLEMENTATION);
		morphy.initialize();

		for (int i = 0 ; i < CLIENT_POOL_SIZE; i++)
		{
//			LinkParserClient lpc = new LinkParserSocketClient(DEFAULT_HOST, FIRST_PORT+i);
//			lpc.setAllowSkippedWords(true);
			RemoteLGParser parser = new RemoteLGParser();
			parser.getLinkGrammarClient().setHostname(DEFAULT_HOST);
			parser.getLinkGrammarClient().setPort(FIRST_PORT+i);
			RelexContext context = new RelexContext(parser, morphy);
			try
			{
				pool.put(context);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	/**
	 * Submit a new sentence to be processed, blocking if no resources
	 * are available. Results are obtained calling take(), and are
	 * returned in order of submission.
	 *
	 * @param sentence The sentence to be processed.
	 * @throws InterruptedException
	 */
	public void push(String sentence) throws InterruptedException
	{
		RelexContext context = pool.take();
		Callable<RelexTaskResult> callable =
			new RelexTask(count++, sentence,
					sentenceAlgorithmApplier, context, pool);
		Future<RelexTaskResult> submit = exec.submit(callable);
        results.add(submit);
	}

	/**
	 * Return the next result, in order of submission, or blocks until
	 * it's ready
	 *
	 * @return The next result
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected RelexTaskResult take() throws InterruptedException, ExecutionException
	{
		Future<RelexTaskResult> first = results.take();
		RelexTaskResult taskResult = first.get();

		return taskResult;
	}

	/**
	 * Stop accepting requests, and shutdown the thread pool after all
	 * remaining requests are done.
	 */
	public void shutdown()
	{
		stop = true;
		exec.shutdown();
	}

	/**
	 * @return true is no more sentences are accepted (i.e., shutdown()
	 * was called) and there are no pending results.
	 */
	protected boolean isRunning()
	{
		return !stop || !results.isEmpty();
	}

	/**
	 * Unit test. Read a text file and process its sentences in parallel.
	 * Assumes link-grammar servers running on DEFAULT_HOST,
	 * listening to ports FIRST_PORT, FIRST_PORT+1, ..., FIRST_PORT+(CLIENT_POOL_SIZE-1)
	 *
	 * @param args The text file to be read
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(final String[] args)
		throws IOException, InterruptedException
	{
		long t = System.currentTimeMillis();
		final ParallelRelationExtractor pre = new ParallelRelationExtractor();
		System.err.println("Initialization time: "+((System.currentTimeMillis() - t)/1000)+" s");

		final long xt = System.currentTimeMillis();
		// Producer - submits sentences from a file
		new Thread(new Runnable()
		{
			public void run()
			{
				DocSplitter ds = DocSplitterFactory.create();
				try
				{
					// Read entire file
					StringBuilder sb = new StringBuilder();
					BufferedReader in = new BufferedReader(new FileReader(args[0]));
					String line = in.readLine();
					while (line!=null){
						sb.append(" "+line+" ");
						line = in.readLine();
					}
					in.close();

					// Break text into sentences and submit
					ds.addText(sb.toString());
					sb = null;

					String sentence = ds.getNextSentence();
					while (sentence!=null)
					{
						pre.push(sentence);
						sentence = ds.getNextSentence();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				pre.shutdown(); // end all threads in the pool after finishing all requests
		}}).start();

		// Consumer - print the results, in the original order
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					while (pre.isRunning())
					{
						System.err.println(pre.take());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				System.err.println("Elapsed time: "+((System.currentTimeMillis() - xt)/1000)+" s");
			}
		}).start();
	}
}
