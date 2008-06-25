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

/**
 * The DocSplitterTool class provides the central processing
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
public class DocSplitterTool
{
	public static final int verbosity = 1;

	/* ---------------------------------------------------------- */
	/* Constructors, etc. */

	public DocSplitterTool()
	{
	}

	/* ---------------------------------------------------------- */
	/**
	 * Main entry point
	 */
	public static void main(String[] args)
	{
		String callString = "DocSplitterTool" +
			" [-n (use the OpenNLP-based splitter)]";
		HashSet<String> flags = new HashSet<String>();
		flags.add("-n");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		String sentence = null;

		if (commandMap.get("-h") != null)
		{
			System.err.println(callString);
			return;
		}

		// If sentence is not passed at command line, read from standard input:
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		DocSplitter ds = DocSplitterFactory.create();

		// QuotesParens is currently broken, it fails to handle possesives.
		// QuotesParensSentenceDetector ds = QuotesParensSentenceDetector.create();

		int sentence_count = 0;
		while(true)
		{
			// If no sentence specified on the command line
			// (with the "-s" flag), then read it from stdin.
			while (sentence == null)
			{
				System.out.print("% ");
				try {
					sentence = stdin.readLine();
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

				sentence = ds.getNextSentence();
			}
		}
	}
}

/* ============================ END OF FILE ====================== */
