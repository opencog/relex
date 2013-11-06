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
import relex.output.SimpleView;
import relex.output.OpenCogScheme;


/**
 * The PlainTextServer class provides a very simple socket-based parse server.
 * It will listen for plain-text input sentences on port 3333, and will
 * generate plain-text parse output.
 *
 * It is intended that this server be used for on-line web demos, and nothing more.
 */

public class PlainTextServer
{
	private int listen_port;

	public Server()
	{
		listen_port = 3333;
	}

	public static void main(String[] args)
	{
		int listen_port = 3333;
		boolean relex_on = false;
		boolean link_on = false;
		boolean anaphora_on = false;
		boolean verbose = false;
		String usageString = "Plain-text RelEx server.\n" +
			"Given a sentence, it returns a parse in opencog-style scheme format.\n" +
			" -p number  \t Port number to listen on (default: 3333)\n" +
			" --port num \t Port number to listen on (default: 3333)\n" +
			" --relex    \t Output RelEx relations (default)\n" +
			" --link     \t Output Link Grammar Linkages\n" +
			" --anaphora \t Output anaphore references\n" +
			" --verbose  \t Print parse output to server stdout.\n";

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--anaphora"))
			{
				anaphora_on = true;
			}
			else if (args[i].equals("--help") || args[i].equals("-h"))
			{
				System.out.println(usageString);
				System.exit(0);
			}
			else if (args[i].equals("--link"))
			{
				link_on = true;
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
			else if (args[i].equals("--relex"))
			{
				relex_on = true;
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
		RelationExtractor r = new RelationExtractor(false);
		OpenCogScheme opencog = new OpenCogScheme();
		Server s = new Server();
		s.listen_port = listen_port;
		ServerSocket listen_sock = null;

		if (!relex_on && !link_on)
		{
			// By default just export RelEx output.
			relex_on = true;
		}
		if (anaphora_on)
		{
			System.err.println("Info: Anaphora output on.");
			opencog.setShowAnaphora(anaphora_on);
		}
		if (link_on)
		{
			System.err.println("Info: Link grammar output on.");
			opencog.setShowLinkage(link_on);
		}
		if (relex_on)
		{
			System.err.println("Info: RelEx output on.");
			opencog.setShowRelex(relex_on);
		}

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
				System.err.println("Error: Accept failed");
				continue;
			}

			System.err.println("Info: Socket accept");
			BufferedReader in = new BufferedReader(new InputStreamReader(ins));
			PrintWriter out = new PrintWriter(outs, true);

			try {
				String line = in.readLine();
				if (line == null)
					continue;
				System.err.println("Info: recv input: \"" + line + "\"");
				Sentence sntc = r.processSentence(line);
				if (sntc.getParses().size() == 0)
				{
					out.println("; NO PARSES");
					continue;
				}
				ParsedSentence parse = sntc.getParses().get(0);

				// Print the phrase string .. why ??
				out.println("; " + parse.getPhraseString());

				if (verbose)
				{
					String fin = SimpleView.printRelationsAlt(parse);
					System.out.print(fin);
				}
				opencog.setParse(parse);
				out.println(opencog.toString());

				// Add a special tag to tell the cog server that it's
				// just recieved a brand new sentence. The OpenCog scheme
				// code depends on this being visible, in order to find
				// the new sentence.
				out.println("(ListLink (stv 1 1)");				
				out.println("   (AnchorNode \"# New Parsed Sentence\")");
				out.println("   (SentenceNode \"" + sntc.getID() + "\")");
				out.println(")");

				out.println("; END OF SENTENCE");

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

