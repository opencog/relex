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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Map;
import org.linkgrammar.LinkGrammar;
import relex.ServerSession;
import relex.Version;

/**
 * The Server class provides a multi-threaded socket-based parse server.
 * It will listen for plain-text input sentences on port 4444, and will
 * generate OpenCog Atomese output.
 *
 * It is intended that this server be used by OpenCog agents to process
 * text; the text is sent from opencog to this server, and the returned
 * parses are then further processed by OpenCog.
 */

public class Server
{
	// command-line arguments
	static private int NTHREADS = 8;
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
	private Socket send_sock = null;
	private OutputStream outs = null;
	private PrintWriter out = null;

	// stats
	static int restart_count = 0;

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
	private static class ConnHandler implements Runnable
	{
		public ArrayBlockingQueue<ServerSession> sessq = null;
		public ServerSession sess = null;
		Socket in_sock = null;
		PrintWriter out = null;
		public void run()
		{
			System.err.println("Info: Enter thread with handler " + sess.id);
			try {
				sess.handle_session(in_sock, out);
			} catch (IOException e) {
				System.err.println("Error: Cannot handle: " + e.getMessage());
			}
			out.flush();
			out.close();
			out = null;

			try {
				in_sock.close();
			} catch (IOException e) {
				System.err.println("Error: Cannot close: " + e.getMessage());
			}
			in_sock = null;

			sessq.add(sess);
			sess = null;
			sessq = null;

			// Something here is leaking memory ... 10GB in 10 minutes.
			// System.gc();
		}
	}

	// Block until one copy of this is running in each thread.
	// When this finally happens, perform link-grammar cleanup
	// in each thread.
	private static class ConnCleanup implements Runnable
	{
		public ServerSession sess = null;
		public static AtomicInteger count = new AtomicInteger(0);
		public void run()
		{
			count.addAndGet(1);
			System.err.println("Info: Waiting cleanup thread. cnt=" + count.get());

			// Stall until all threads are running.
			try {
				while (count.get() < NTHREADS) { TimeUnit.SECONDS.sleep(1); }
			} catch (InterruptedException e) {
				System.err.println("Error: Shtutdown interrupted: " + e.getMessage());
			}
			sess.sess_close();
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

		{
			// Force link-grammar initialization, before anything else.
			ServerSession tmp = new ServerSession();
			tmp.sess_setup(relex_on, link_on, free_text, max_parses, lang);
		}

		ArrayBlockingQueue<ServerSession> sessq = null;
		ExecutorService tpool = null;
		ServerSession sess = null;

		// If the end-point is null, we can (and will) use threads.
		if (send_sock == null)
		{
			sessq = new ArrayBlockingQueue<ServerSession>(NTHREADS);
			for (int i=0; i<NTHREADS; i++)
			{
				sess = new ServerSession();
				sess.id = i+1;
				sess.sess_setup(relex_on, link_on, free_text, max_parses, lang);
				sess.verbose = verbose;
				sessq.add(sess);
			}
			tpool = Executors.newFixedThreadPool(NTHREADS);
		}
		else
		{
			sess = new ServerSession();
			sess.sess_setup(relex_on, link_on, free_text, max_parses, lang);
			sess.verbose = verbose;
		}

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
					out = new PrintWriter(in_sock.getOutputStream(), true);
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
				try {
					sess.handle_session(in_sock, out);
				} catch (IOException e) {
					System.err.println("Error: Cannot connect: " + e.getMessage());
				}
			}
			else
			{
				try {
					// Take will block; can throw InterruptedException
					sess = sessq.take();
					ConnHandler cha = new ConnHandler();
					cha.sess = sess;
					cha.sessq = sessq;
					cha.in_sock = in_sock;
					cha.out = out;
					tpool.execute(cha);
				} catch (InterruptedException e) {
					System.err.println("Error: Queue interrupted: " + e.getMessage());
				}
			}
			loop_count++;
			System.err.println("Loop count=" + loop_count +
			                   " Restart count=" + restart_count);

			if (loop_count%100 == 0) System.gc();
			// Basically, don't ever break ...
			// if (2147483600 < loop_count) break;

			// Sometime after about 1000 parses, Java gets old and
			// slow and tired. Try to work around this.
			if (1000 < loop_count) break;
		}

		System.err.println("Info: Main loop shutting down");

		// Invoke the Relex close handler in each thread.
		for (int i=0; i<NTHREADS; i++)
		{
			try {
				// Take will block; can throw InterruptedException
				sess = sessq.take();
				ConnCleanup cle = new ConnCleanup();
				cle.sess = sess;
				tpool.execute(cle);
			} catch (InterruptedException e) {
				System.err.println("Error: Cleanup interrupted: " + e.getMessage());
			}
		}
		sessq = null;

		// Wait until each thread has been cleaned up.
		try {
			tpool.shutdown();
			tpool.awaitTermination(600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("Error: Shutdown interrupted: " + e.getMessage());
		}
		tpool = null;

		// Now perform Relex finalization.
		LinkGrammar.doFinalize();
	}

	public static void main(String[] args)
	{
		Server srv = new Server();
		srv.parse_args(args);
		srv.socket_setup();

		// Every thousand sentences, the server will auto-exit,
		// and we will attempt a marathon garage collection.
		// This appears to (mostly, but not completely?) fix the
		// performance issues that accumulate over time.
		//
		// The core problem seems to be related to very large
		// parse results being returned -- sometimes ten or
		// twenty megabytes.  I think that these cause Java memory
		// fragmentation, and the Java GC is unable to deal with
		// this or resolve it in any way.  The symptoms are that
		// this server gets slower and slower, suffering after
		// about 500 maybe 1000 sentences, and coming to a near-total
		// grinding halt after about 4 hours of run-time, with the
		// processing rate dropping to about 5% of the fresh-restart
		// rate. The periodic clean-sweap done here seems to fix the
		// issue.
		//
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
