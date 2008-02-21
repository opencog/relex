package relex.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessServer {
	private static final int verbosity = 1;

	PrintWriter out = null;

	BufferedReader in = null;

	ProcessProtocol pp = null;

	ServerSocket serverSocket = null;

	Socket clientSocket = null;

	int port;

	public ProcessServer(int _port, ProcessProtocol _pp) {
		port = _port;
		pp = _pp;
	}

	public void init() throws IOException {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port + ".");
			System.exit(1);
		}

		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}

		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket
				.getInputStream()));

		String outputLine = pp.processInput(ProcessProtocol.HANDSHAKE_REQUEST);
		if (!outputLine.equals(ProcessProtocol.HANDSHAKE_RESPONSE)) throw new RuntimeException("Bad Connection");
		if (verbosity > 1) out.println(outputLine);
		if (verbosity > 0) System.out.println("ProcessServer connection established.  Ready to receive messages");
	}

	public void run() throws IOException {
		String inputLine = null;
		String outputLine = null;
		while ((inputLine = in.readLine()) != null) {
			if (verbosity > 3)
				System.out.println("ProcessServer requesting:" + inputLine);
			outputLine = pp.processInput(inputLine);
			if (verbosity > 3)
				System.out.println("ProcessServer response:" + outputLine);
			out.println(outputLine);
			if (outputLine.equals("Bye."))
				break;
		}
	}

	public void close() throws IOException {
		if (out != null)
			out.close();
		if (in != null)
			in.close();
		if (clientSocket != null)
			clientSocket.close();
		if (serverSocket != null)
			serverSocket.close();
	}

}
