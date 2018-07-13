/*
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
 *
 * Copyright (c) 2008, 2009, 2013, 2017 Linas Vepstas <linasvepstas@gmail.com>
 */

package relex;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.output.SimpleView;
import relex.output.OpenCogScheme;
import relex.Version;

/**
 * Handler for a single socket session.
 * Accepts input text string on input, delivers relex-parsed
 * syntax tree on output, in the Opencog Atomese format.
 */
public class ServerSession
{
	public boolean verbose = false;
	public int id = 0;

	private RelationExtractor re = null;
	private OpenCogScheme opencog = null;
	private DocSplitter ds = null;
	private boolean free_text = false;
	private int max_parses;

	// Set up the parsers.
	public void sess_setup(boolean relex_on, boolean link_on,
	                       boolean ft, int mp, String lang)
	{
		free_text = ft;
		max_parses = mp;

		// -----------------------------------------------------------------
		// After parsing the commmand arguments, set up the assorted classes.
		re = new RelationExtractor(false);
		re.setLanguage(lang);
		re.setMaxParses(max_parses);
		if (1000 < max_parses) re.setMaxLinkages(max_parses+100);
		opencog = new OpenCogScheme();
		ds = DocSplitterFactory.create();

		if (!relex_on && !link_on)
		{
			// By default just export RelEx output.
			relex_on = true;
		}
		if (link_on)
		{
			System.err.println("Info: hndlr=" + id + " Link grammar output on.");
			opencog.setShowLinkage(link_on);
		}
		if (relex_on)
		{
			System.err.println("Info: hndlr=" + id + " RelEx output on.");
			opencog.setShowRelex(relex_on);
		}
		if (!relex_on)
		{
			re.do_apply_algs = false;
		}
	}

	// Need to close in the same thread in which work was being done,
	// so as to release allocated memory associated with LinkGrammar,
	// with that processing thread. The java bindings do a per-thread
	// malloc.
	public void sess_close()
	{
		ds = null;
		opencog = null;
		re.close();
		re = null;
	}

	// -----------------------------------------------------------------
	// Run a single socket session, acceptinng input, and returning
	// responses.
	public void handle_session(Socket in_sock, PrintWriter out)
		throws IOException
	{
		// System.err.println("Info: Socket accept");
		InputStream ins = in_sock.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));

		// Loop over multiple sentences that may be present in input.
		while (true)
		{
			// Loop over multiple input lines, looking for one complete sentence.
			String sentence = null;
			while (null == sentence)
			{
				try {
					// Break if EOF encountered.  This should have been easy
					// to figure out, but its not. Java sux rox. What is wrong
					// with these people? Are they all stupid, or what? Arghhhh.
					int one_char = in.read();
					// 0x4 is ASCII EOT aka ctrl-D via telnet.
					if (-1 == one_char || 4 == one_char)
					{
						sentence = ds.getRemainder();
						sentence = sentence.trim();
						break;
					}
					if ('\r' == one_char)
						continue;
					if ('\n' == one_char)
						continue;

					// Another bright shining example of more java idiocy.
					char junk[] = {(char)one_char};
					String line = new String(junk);
					line += in.readLine();

					System.err.println("Info: hndlr=" + id + " recv input: \"" + line + "\"");

					// If the free-text flag is set, then use the document
					// splitter to find sentence boundaries. Otherwise,
					// assume one sentence per line.
					if (free_text)
					{
						ds.addText(line + " ");
						sentence = ds.getNextSentence();
					}
					else
					{
						sentence = line;
					}
				}
				catch (Exception e)
				{
					System.err.println("Error: hndlr=" + id + " Read of input failed:" + e.getMessage());
					break;
				}
			}

			// If the sentence is null; we've run out of input.
			if (null == sentence || sentence.equals(""))
				break;

			try
			{
				System.err.println("Info: hndlr=" + id + " sentence: \"" + sentence + "\"");
				Sentence sntc = re.processSentence(sentence);
				if (sntc.getParses().size() == 0)
				{
					System.err.println("Info: hndlr=" + id + " No parses!");
					out.println("; NO PARSES");

					// Only one sentence per connection in the non-free-text mode.
					if (!free_text) break;
					continue;
				}
				int np = Math.min(max_parses, sntc.getParses().size());
				int pn;
				for (pn = 0; pn < np; pn++)
				{
					ParsedSentence parse = sntc.getParses().get(pn);

					// Print the phrase string ... handy for debugging.
					out.println("; " + parse.getPhraseString());

					if (verbose)
					{
						String fin = SimpleView.printRelationsAlt(parse);
						System.out.print(fin);
					}
					opencog.setParse(parse);
					out.println(opencog.toString());
					out.flush();
					System.err.println("Info: hndlr=" + id + " sent parse " + (pn + 1) + " of " + np);

					// This is for simplifying pre-processing of scheme string
					// before evaluating it in opencog.
					out.println("; ##### END OF A PARSE #####");
					out.flush();
				}

				// Add a special tag to tell the cog server that it's
				// just recieved a brand new sentence. The OpenCog scheme
				// code depends on this being visible, in order to find
				// the new sentence.
				out.println("(ListLink (stv 1 1)");
				out.println("   (AnchorNode \"# New Parsed Sentence\")");
				out.println("   (SentenceNode \"" + sntc.getID() + "\")");
				out.println(")");

				out.println("; END OF SENTENCE");
				out.flush();
			}
			catch (Exception e)
			{
				System.err.println("Error: hndlr=" + id + " Failed to parse: " + e.getMessage());
				e.printStackTrace();
				break;
			}

			// Only one sentence per connection in the non-free-text mode.
			if (!free_text) break;
		}

		try
		{
			in_sock.close();
			System.err.println("Info: hndlr=" + id + " Closed input socket");
		}
		catch (IOException e)
		{
			System.err.println("Error: hndlr=" + id + " Socket close failed: " + e.getMessage());
		}
	}
}
