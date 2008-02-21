package relex;
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	private int listen_port;

	public Server()
	{
		listen_port = 4444;
	}

	public static void main(String[] args)
	{
		Server s = new Server();
		ServerSocket listen_sock = null;
		try {
			listen_sock = new ServerSocket(s.listen_port);
		} catch (IOException e) {
			System.out.println("Listen failed on port " + s.listen_port);
			System.exit(-1);
		}

		while(true)
		{
			Socket out_sock = null;
			OutputStream outs = null;
			try {
				out_sock = listen_sock.accept();
				outs = out_sock.getOutputStream();
			} catch (IOException e) {
				System.out.println("Accept failed");
				continue;
			}

System.out.println("duude got accept");
			PrintWriter out = new PrintWriter(outs, true);
	
			// doc = new StringTextDocument("Hello world");
			// s.rf.process(doc);

			out.close();
			try {
				out_sock.close();
			} catch (IOException e) {
				System.out.println("Socket close failed");
				continue;
			}
		}
	}
}


