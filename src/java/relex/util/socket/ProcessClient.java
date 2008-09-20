package relex.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class ProcessClient {
	private static final int verbosity = 0;

	private int millisecondsBetweenConnectionAttempts;

	private int failedRequestRepeatLimit;

	private String HOST;

	private int PORT;

	private Socket pSocket = null;

	private PrintWriter out = null;

	private BufferedReader in = null;

	private BufferedReader stdIn = null;

	public ProcessClient(String _HOST, int _PORT,
			int _millisecondsBetweenConnectionAttempts,
			int _failedRequestRepeatLimit) {
		stdIn = new BufferedReader(new InputStreamReader(System.in));
		HOST = _HOST;
		PORT = _PORT;
		millisecondsBetweenConnectionAttempts = _millisecondsBetweenConnectionAttempts;
		failedRequestRepeatLimit = _failedRequestRepeatLimit;
		if (verbosity > 1) {
			System.out.println("ProcessClient initialized with HOST=" + HOST
					+ " PORT=" + PORT);
		}
	}

	public ProcessClient() {
		throw new RuntimeException("ProcessClient empty constructor not allowed");
	}

	private void waitForInitConnection() {
		while (true) {
			try {
				if (verbosity > 0) System.out.println("CLOSING ANY REMAINING PARTS OF CONNECTION");
				closeConnection();
				if (verbosity > 0) System.out.println("OPENING CONNECTION");
				pSocket = new Socket(HOST, PORT);
				out = new PrintWriter(pSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(pSocket.getInputStream()));
				return;
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: " + HOST + ":"+ PORT);
			} catch (IOException e) {
				System.err.println("\nCouldn't get I/O for the connection to: "+ HOST + ":" + PORT);
			}
			pause(millisecondsBetweenConnectionAttempts);
		}
	}

	public void closeConnection() throws IOException {
		if (verbosity > 0)
			System.out.println("CLOSING CONNECTION");
		if (out != null)
			out.close();
		if (in != null)
			try { in.close(); }
			catch (IOException e) { /* swallow exception so that we absolutely call pSocket.close below */}		
		if (pSocket != null)
			pSocket.close();
	}

	/**
	 * Wait for an initialized connection and then runs every restoreCommand in the given list.
	 * 
	 * Will not return until server has been initialized and all restoreCommands successfully entered.
	 * 
	 * @param restoreCommands The list of restoreCommands to be executed
	 */
	private void restoreServerState(ArrayList<String> restoreCommands) {
		while (true) {
			// wait for an initialized connection
			waitForInitConnection();
			// We prevent infinite recursion by passing "null" as the second argument to process
			Iterator<String> coms = restoreCommands.iterator();
			while (coms.hasNext() && process(coms.next(), null) != null)
				;
			// if we made it through all restore requests, then we are done restoring.
			if (!coms.hasNext()) return;
		}
	}

	/**
	 * Process the given request and returns the response. If a response isn't received,
	 * uses the restoreCommands to rollback server state.
	 *  
	 * This method will hold until a response is received. It works with a
	 * single recursion. Here is the sequence.
	 * 
	 * -- A request is received with a list of restoreCommands. 
	 *    The restoreCommands are a set of commands that should be executed 
	 *    in order to return the server side to the same state
	 *
	 * -- If the request receives a response, the response is returned.
	 * -- Otherwise, the server state is restored (which also holds) and the command is re-executed
	 */
	public String process(String request, ArrayList<String> restoreCommands) {
		if (restoreCommands != null) {
			if (verbosity > 3) System.out.println("ProcessClient processing:" + request);
		} else {
			if (verbosity > 3) System.out.println("ProcessClient restoring:" + request);
		}
		String response = null;
		// loops forever until it returns a response or until
		// failedRequestRepeatLimit is past
		int requestFailures = 0;
		String lastFailedRequest = null;
		while (true) {
			try {
				// Send the request:
				if (verbosity > 4) System.out.println("Sending request: "+request);
				out.println(request);
				// if the server is working, return the response
				if ((response = in.readLine()) != null && response != null) {
					if (verbosity > 3)
						System.out.println("ProcessClient received:" + response);
					String addendum = null;
					while ((addendum = in.readLine()) != null
							&& addendum != null
							&& !addendum.equals(ProcessProtocol.RESPONSE_SEPERATOR)) {
						response += "\n" + addendum;
					}
					// A response was received...
					lastFailedRequest = null;
					requestFailures = 0;
					return response;
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
			// The server is not working.
			// If there are no restore commands, return null.
			if (restoreCommands == null) {
				if (verbosity > 0) System.out.println("ProcessClient failed: returning null.");
				return null;
			}
			// otherwise....
			if (!request.equals(lastFailedRequest))
				requestFailures = 0;
			requestFailures++;
			lastFailedRequest = request;
			// ...if we have past the threshold, give up, and return null
			if (requestFailures >= failedRequestRepeatLimit) {
				if (verbosity > 0) System.out.println("ProcessClient failed on attempt "+ requestFailures + ".  Giving up!");
				return null;
			}
			// ...if we have not past the threshold, restore self, and try again.
			if (verbosity > 0) System.out.println("ProcessClient failed: restoring state.  Attempt "+ requestFailures);
			
			restoreServerState(restoreCommands);
			
			if (verbosity > 0) System.out.println("ProcessClient state restored.  Reprocessing:"+ request);
		}
	}

	private void run() throws IOException {
		String fromServer;
		String fromUser;

		while ((fromServer = in.readLine()) != null) {
			System.out.println("Server: " + fromServer);
			if (fromServer.equals("Bye.")) {
				closeConnection();
				return;
			}
			fromUser = stdIn.readLine();
			if (fromUser != null) {
				System.out.println("Client: " + fromUser);
				out.println(fromUser);
			}
		}
		closeConnection();
	}

	private void pause(int amount) {
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() < time + amount)
			;
	}

	public static void main(String[] args) throws IOException {
		while (true) {
			ProcessClient pc = new ProcessClient();
			pc.waitForInitConnection();
			pc.run();
			System.out.println("FINISHED");
		}
	}
}
