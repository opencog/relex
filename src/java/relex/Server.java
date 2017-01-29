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
 * Copyright (c) 2008, 2009, 2013 Linas Vepstas <linasvepstas@gmail.com>
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
import java.util.HashSet;
import java.util.Map;
import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.output.SimpleView;
import relex.output.OpenCogScheme;
import relex.Version;

/**
 * handler for a single coekct session.
 */
public class Session
{
	private Socket send_sock = null;
	private OutputStream outs = null;
	private PrintWriter out = null;

	private RelationExtractor re = null;
	private OpenCogScheme opencog = null;
	private DocSplitter ds = null;

	// Set up the parsers.
	public void setup(boolean relex_on, boolean link_on)
	{
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
			System.err.println("Info: Link grammar output on.");
			opencog.setShowLinkage(link_on);
		}
		if (relex_on)
		{
			System.err.println("Info: RelEx output on.");
			opencog.setShowRelex(relex_on);
		}
		if (!relex_on)
		{
			re.do_apply_algs = false;
		}
	}

	// -----------------------------------------------------------------
	// Run a single socket session, acceptinng input, and returning
	// responses.
	public void handle_session(Socket in_sock)
	{
		System.err.println("Info: Socket accept");
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

					System.err.println("Info: recv input: \"" + line + "\"");

					// If the free-text flag is set, then use the document
					// splitter to find sentence boundaries. Otherwise,
					// Assume one sentence per line.
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
					System.err.println("Error: Read of input failed:" + e.getMessage());
					break;
				}
			}

			// If the sentence is null; we've run out of input.
			if (null == sentence || sentence.equals(""))
				break;

			try
			{
				System.err.println("Info: sentence: \"" + sentence + "\"");
				Sentence sntc = re.processSentence(sentence);
				if (sntc.getParses().size() == 0)
				{
					System.err.println("Info: No parses!");
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
					System.err.println("Info: sent parse " + (pn + 1) + " of " + np);

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
				System.err.println("Error: Failed to parse: " + e.getMessage());
				e.printStackTrace();
				break;
			}

			// Only one sentence per connection in the non-free-text mode.
			if (!free_text) break;
		}

		try
		{
			in_sock.close();
			System.err.println("Info: Closed input socket");
		}
		catch (IOException e)
		{
			System.err.println("Error: Socket close failed: " + e.getMessage());
		}
	}
}

/**
 * The Server class provides a very simple socket-based parse server.
 * It will listen for plain-text input sentences on port 4444, and will
 * generate OpenCog output.
 *
 * It is intended that this server be used by OpenCog agents to process
 * text; the text is sent from opencog to this server, and the returned
 * parses are then further processed by OpenCog.
 */

public class Server
{
	// command-line arguments
	private int listen_port = 4444;
	private String host_name = null;
	private int host_port = 0;
	private int max_parses = 1;
	private String lang = "en";
	private boolean verbose = false;
	private boolean relex_on = false;
	private boolean link_on = false;
	private boolean free_text = false;

	// sockets
	private ServerSocket listen_sock = null;

	public Server()
	{
	}

	public void parse_args(String[] args)
	{
		String usageString = "RelEx server (designed for OpenCog interaction).\n" +
			"Given a sentence, it returns a parse in opencog-style scheme format.\n" +
			"Options:\n" +
			" -p number  \t Port number to listen on (default: 4444)\n" +
			" --port num \t Port number to listen on (default: 4444)\n" +
			" --host host:port\t Send output to indicated host:port (example: localhost:17001)\n" +
			" --lang lang\t Set langauge (default: en)\n" +
			" -n number  \t Max number of parses to return (default: 1)\n" +
			" --relex    \t Output RelEx relations (default)\n" +
			" --link     \t Output Link Grammar Linkages\n" +
			" --free-text\t Don't assume one sentence per line; look for !?. to end sentence.\n" +
			" --verbose  \t Print parse output to server stdout.\n";

		HashSet<String> flags = new HashSet<String>();
		flags.add("-h");
		flags.add("--help");
		flags.add("--link");
		flags.add("--relex");
		flags.add("--free-text");
		flags.add("--verbose");
		HashSet<String> opts = new HashSet<String>();
		opts.add("-n");
		opts.add("-p");
		opts.add("--host");
		opts.add("--lang");
		opts.add("--port");
		Map<String,String> commandMap = CommandLineArgParser.parse(args, opts, flags);

		try
		{
			String opt;

			opt = commandMap.get("-n");
			if (opt != null) max_parses = Integer.parseInt(opt);

			opt = commandMap.get("-p");
			if (opt != null) listen_port = Integer.parseInt(opt);

			opt = commandMap.get("--host");
			if (opt != null)
			{
				String[] hp = opt.split(":");
				host_name = hp[0];
				host_port = Integer.parseInt(hp[1]);
			}

			opt = commandMap.get("--lang");
			if (opt != null) lang = opt;

			opt = commandMap.get("--port");
			if (opt != null) listen_port = Integer.parseInt(opt);
		}
		catch (Exception e)
		{
			System.err.println("Unrecognized parameter.");
			System.err.println(usageString);
			System.exit(1);
		}

		if (commandMap.get("-h") != null ||
		    commandMap.get("--help") != null)
		{
			System.err.println(usageString);
			System.exit(0);
		}
		if (commandMap.get("--link") != null) link_on = true;
		if (commandMap.get("--relex") != null) relex_on = true;
		if (commandMap.get("--free-text") != null) free_text = true;

		if (commandMap.get("--verbose") != null)
		{
			System.err.println("Info: Verbose server mode set.");
			verbose = true;
		}
	}

	// -----------------------------------------------------------------
	// Socket setup
	public void socket_setup()
	{
		try
		{
			listen_sock = new ServerSocket(listen_port);
		}
		catch (IOException e)
		{
			System.err.println("Error: Listen failed on port " + listen_port);
			System.exit(-1);
		}
		System.err.println("Info: Listening on port " + listen_port);

		// Send output to an opencog server, instead of returning it on
		// the input socket.
		if (host_name != null)
		{
			try
			{
				send_sock = new Socket(host_name, host_port);
				send_sock.setKeepAlive(true);
				// send_sock.shutdownInput();
				outs = send_sock.getOutputStream();
				out = new PrintWriter(outs, true);

				// Assume we're talking to an opencog server.
				// Escape it into a scheme shell.
				out.println("scm hush");
				out.flush();
			}
			catch (Exception e)
			{
				System.err.println("Error: Unable to connect to " +
				   host_name + ":" + host_port + " : " + e.getMessage());
				System.exit(-1);
			}
			System.err.println("Info: Will send output to " + host_name + ":" + host_port);
		}
	}

	// -----------------------------------------------------------------
	public void run_server()
	{
		int loop_count = 0;
		System.err.println("===============================================");
		System.err.println("===============================================");
		System.err.println("===============================================");
		System.err.println("Info: Version: " + Version.getVersion());

		Session sess = new Session (relex_on, link_on);

		// -----------------------------------------------------------------
		// Main loop -- listen for connections, accept them, and process.
		while (true)
		{
			Socket in_sock = null;
			try {
				System.err.println("Info: Waiting for socket connection");
				in_sock = listen_sock.accept();

				// If no end-point, return data on same socket.
				if (send_sock == null)
				{
					outs = in_sock.getOutputStream();
					out = new PrintWriter(outs, true);
				}
			} catch (IOException e) {
				System.err.println("Error: Accept failed: " + e.getMessage());
				continue;
			}

			// Attempt to detect a dead socket. This could happen if the
			// remote end died. This should be easy to do, but for some
			// reason, the below fails ... programming in Java sucks. Oh well.
			if (send_sock != null)
			{
				try
				{
					// Send a lone newline char.
					outs.write(10);
				}
				catch (Exception e)
				{
					System.err.println("Error: Remote end has closed socket! " + e.getMessage());
				}
			}

			sess.handle_session(in_sock);

			// Something here is leaking memory ... 10GB a day ... can this help?
			System.gc();
			loop_count++;
			if (500 < loop_count) break;
		}
	}

	public static void main(String[] args)
	{
		Server srv = new Server();
		srv.parse_args(args);
		srv.socket_setup();

		// Every five-hundred sentences, the server will auto-exit,
		// and we will attempt a marathon garage collection.  I hope
		// that maybe this will fix the performance issues that
		// accumulate over time?  What's wrong with Java GC?
		//
		// CPU usage starts getting really heavy, and performance
		// starts dropping after 500 sentences, and totally collapses
		// after about 4 hours or run-time... WTF.
		//
		int restart_count = 0;
		while (true)
		{
			srv.run_server();

			// Java sucks rocks.
			System.gc();
			System.gc();
			System.gc();
			System.gc();
			System.gc();

			restart_count++;
			System.err.println("===============================================");
			System.err.println("Restart count = " + restart_count);
		}
	}
}
