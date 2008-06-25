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
package relex.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;

import relex.CommandLineArgParser;

/**
 * The DocSplitterTool class provides a command-line tool
 * for splitting up a generic corpus into sentences, outputing one
 * sentece per line.
 */
public class DocSplitterTool
{
	/**
	 * Main entry point
	 */
	public static void main(String[] args)
	{
		String callString = "DocSplitterTool" +
			" [-h (show help)]" +
			" [-n (use the OpenNLP-based splitter)]" +
			" [-q (use the Quotes-Parens splitter)]";
		HashSet<String> flags = new HashSet<String>();
		flags.add("-h");
		flags.add("-n");
		flags.add("-q");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, null, flags);

		String sentence = null;

		if (commandMap.get("-h") != null)
		{
			System.err.println(callString);
			return;
		}

		// Read text from standard input:
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		DocSplitter ds = DocSplitterFactory.create();

		// QuotesParens is currently broken, it fails to handle possesives.
		// QuotesParensSentenceDetector ds = QuotesParensSentenceDetector.create();

		int sentence_count = 0;
		int line_count = 0;
		while(true)
		{
			// Read lines from stdin until enough has been read to form
			// one (or more) entire sentence(s).
			while (sentence == null)
			{
				try
				{
					sentence = stdin.readLine();
					line_count++;
				}
				catch (IOException e)
				{
					System.err.println("Error reading sentence from the standard input!");
				}

				if (sentence == null) return;

				// Buffer up input text, and wait for a whole,
				// complete sentence before continuing.
				ds.addText(sentence + " ");
				sentence = ds.getNextSentence();
			}

			while (sentence != null)
			{
				sentence_count ++;
				System.out.println(sentence);

				sentence = ds.getNextSentence();
			}
		}
	}
}

/* ============================ END OF FILE ====================== */
