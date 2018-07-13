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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import org.linkgrammar.JSONUtils;
import relex.output.SimpleView;
import relex.output.StanfordView;
import relex.Version;


/**
 * The PlainTextServer class provides a very simple socket-based parse
 * server.  It will listen for plain-text input sentences on port 3333,
 * and will generate plain-text parse output.
 *
 * It is intended that this server be used for on-line web demos, and
 * nothing more.
 *
 * This class understans a quasi-JSON format. The followig parameters
 * are recognized:
 *
 * <ul>
 * <li><b>maxLinkages</b> - maximum number of parses to return in the
 *      result. Note that this does not affect the parser behavior which
 *      computes all parses anyway.</li>
 * <li><b>showLinkages</b> Return the link-grammar parse diagram</li>
 * <li><b>showPhrase</b> Return the Phrase Structure tree</li>
 * <li><b>showRelex</b> Return the RelEx dependency relations</li>
 * <li><b>showStanford</b> Return the Stanford Dependency relations</li>
 * </ul>
 *
 * Example usage:
 * echo "maxLinkages:1,showLink:true,text:this is a test" | netcat localhost 3333
 */

public class PlainTextServer
{
	private int listen_port;

	public PlainTextServer()
	{
		listen_port = 3333;
	}

	public static void main(String[] args)
	{
		int listen_port = 3333;
		boolean verbose = false;
		String lang = "en";
		String dict_path = null;

		String usageString = "Plain-text RelEx server.\n" +
			"Given a sentence, it returns a plain-output parse.\n" +
			" -p number  \t Port number to listen on (default: 3333)\n" +
			" --port num \t Port number to listen on (default: 3333)\n" +
			" --lang     \t Language (en, fr, de, ru) default: en\n" +
			" --dict     \t Dictionary location, default: data/en \n" +
			" --verbose  \t Print parse output to server stdout.\n";

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--help") || args[i].equals("-h"))
			{
				System.out.println(usageString);
				System.exit(0);
			}
			else if (args[i].equals("--lang"))
			{
				i++;
				if (i >= args.length) {
					System.err.println("Error: Expected a language after the --lang flag.");
					System.exit(1);
				}
				lang = args[i];
			}
			else if (args[i].equals("--dict"))
			{
				i++;
				if (i >= args.length) {
					System.err.println("Error: Expected a dictionary path after the --lang flag.");
					System.exit(1);
				}
				dict_path = args[i];
			}
			else if (args[i].equals("--port") || args[i].equals("-p"))
			{
				i++;
				if (i >= args.length) {
					System.err.println("Error: Expected a port number after the -p flag.");
					System.exit(1);
				}

				try {
					listen_port = Integer.parseInt(args[i]);
				} catch (NumberFormatException nfe) {
					System.err.println("Error: Expected a port number after the -p flag.");
					System.exit(1);
				}
			}
			else if (args[i].equals("--verbose") )
			{
				System.err.println("Info: Verbose server mode set.");
				verbose = true;
			}
			else
			{
				System.err.println("Error: Unknown option " + args[i]);
				System.err.println(usageString);
				System.exit(1);
			}
		}

		System.err.println("Info: Version: " + Version.getVersion());

		RelationExtractor r = new RelationExtractor();
		if (null != lang) r.setLanguage(lang);
		if (null != dict_path) r.setDictPath(dict_path);

		PlainTextServer s = new PlainTextServer();
		s.listen_port = listen_port;
		ServerSocket listen_sock = null;

		try
		{
			listen_sock = new ServerSocket(s.listen_port);
		}
		catch (IOException e)
		{
			System.err.println("Error: Listen failed on port " + s.listen_port);
			System.exit(-1);
		}
		System.err.println("Info: Listening on port " + s.listen_port);

		while(true)
		{
			Socket out_sock = null;
			OutputStream outs = null;
			InputStream ins = null;
			try {
				out_sock = listen_sock.accept();
				ins = out_sock.getInputStream();
				outs = out_sock.getOutputStream();
			} catch (IOException e) {
				System.err.println("Error: Socket accept failed");
				continue;
			}

			System.err.println("Info: Socket accept");
			BufferedReader in = new BufferedReader(new InputStreamReader(ins));
			PrintWriter out = new PrintWriter(outs, true);
			JSONUtils msgreader = new JSONUtils();

			try {

				String line = "";
				int num_show = 3;
				boolean show_link = false;
				boolean show_phrase = false;
				boolean show_relex = false;
				boolean show_stanford = false;
				try {
					Map<String, String> msg = msgreader.readMsg(in);
					line = msg.get("text");
					num_show = JSONUtils.getInt("maxLinkages", msg, num_show);
					show_link = JSONUtils.getBool("showLink", msg, show_link);
					show_phrase = JSONUtils.getBool("showPhrase", msg, show_phrase);
					show_relex = JSONUtils.getBool("showRelex", msg, show_relex);
					show_stanford = JSONUtils.getBool("showStanford", msg, show_stanford);
				} catch (RuntimeException e) {
					line = msgreader.getRawText();
					line = line.trim();
				}
				System.err.println("Info: recv input: \"" + line + "\"");

				if (!show_link && !show_phrase && !show_relex &&
				    !show_stanford)
				{
					// Turn everything on by default
					show_link = true;
					show_phrase = true;
					show_relex = true;
					show_stanford = true;
				}

				r.do_stanford = show_stanford;
				r.do_penn_tagging = show_stanford;

				Sentence sntc = r.processSentence(line);
				if (sntc.getParses().size() == 0)
				{
					out.println("==== NO PARSES ====");
					try
					{
						out_sock.close();
						System.err.println("Info: Closed socket");
					}
					catch (IOException e)
					{
						System.err.println("Error: Socket close failed");
					}
					continue;
				}

				int num_parses = sntc.getParses().size();
				if (num_parses < num_show) num_show = num_parses;

				for (int i=0; i < num_show; i++)
				{
					ParsedSentence parse = sntc.getParses().get(i);

					// Print the phrase string .. why ??

					int ialt = i+1;
					out.println("==== Parse alternative " + ialt + " ====\n");

					if (show_link)
					{
						out.println("Link Grammar parse diagram:");
						out.println(parse.getLinkString());
					}
					if (show_phrase)
					{
						out.println("Phrase Structure parse:\n");
						out.println("    " + parse.getPhraseString());
					}
					if (show_relex)
					{
						out.println("Dependency relations:\n");
						String fin = SimpleView.printRelations(parse);
						out.println(fin);
					}
					if (show_stanford)
					{
						out.println("Stanford-style dependency relations:\n");
						String fin = StanfordView.printRelations(parse, true, "    ");
						out.println(fin);
					}
					if (verbose)
						System.out.print(SimpleView.printRelations(parse));
				}
				out.println("==== END OF SENTENCE ====");

				out.close();
				System.err.println("Info: Closed printer");
			}
			catch (IOException e)
			{
				System.err.println("Error: Processing input failed");
				continue;
			}

			try
			{
				out_sock.close();
				System.err.println("Info: Closed socket");
			}
			catch (IOException e)
			{
				System.err.println("Error: Socket close failed");
				continue;
			}
		}
	}
}

