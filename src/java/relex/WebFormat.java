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
import java.util.HashSet;
import java.util.Map;

import relex.corpus.EntityMaintainerFactory;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.entity.EntityMaintainer;
import relex.frame.Frame;
import relex.output.CompactView;
import relex.output.SimpleView;

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
public class WebFormat extends RelationExtractor
{
	/**
	 * Main entry point
	 */
	public static void main(String[] args)
	{
		String callString = "WebFormat" +
			" [-f (show frame output)]" +
			" [-g (do not use GATE entity detector)]" +
			" [-h (show this help)]" +
			" [-l (do not show parse links)]" +
			" [-m (do not show parse metadata)]" +
			" [-n max number of parses to display]" +
			" [-t (do not show constituent tree)]" +
			" [--url source URL]" +
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
		opts.add("--url");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		String url = null;
		String sentence = null;
		int maxParses = 30;
		int maxParseSeconds = 60;

		CompactView cv = new CompactView();

		if (commandMap.get("-l") != null) cv.showLinks(false);
		if (commandMap.get("-m") != null) cv.showMetadata(false);
		if (commandMap.get("-t") != null) cv.showConstituents(false);

		// Check for optional command line arguments.
		try
		{
			maxParses = commandMap.get("-n") != null ?
				Integer.parseInt(commandMap.get("-n").toString()) : 1;

			maxParseSeconds = commandMap.get("--maxParseSeconds") != null ?
				Integer.parseInt(commandMap.get("--maxParseSeconds").toString()) : 60;
			url = commandMap.get("--url");
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
		cv.setSourceURL(url);

		WebFormat re = new WebFormat();
		re.setAllowSkippedWords(true);
		re.setMaxParses(maxParses);
		re.setMaxParseSeconds(maxParseSeconds);

		// XXX hack alert -- we should be getting the relex
		// version string from the build environment somehow,
		// instead of hard-coding it.
		String version = re.getVersion();
		version += "\trelex-0.9.0";
		cv.setVersion(version);

		EntityMaintainerFactory gem = null;
		if (commandMap.get("-g") == null)
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
				for (ParsedSentence parse: ri.getParses())
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
