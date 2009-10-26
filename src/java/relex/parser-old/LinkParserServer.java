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

package relex.parser_old;

import java.io.IOException;

import relex.parser_old.util.socket.ProcessServer;

/**
 * @deprecated
 */
public class LinkParserServer
{
	public static int verbosity = 0;

	public static void main(String[] args) throws IOException {
		ProcessServer ps = new ProcessServer(Integer.parseInt(args[0]),
				new LinkParserProtocol(LinkParserJNINewClient.getSingletonInstance()));
		if (verbosity > 0) System.out.println("ProcessServer Running.  Waiting for client.");
		while (true) {
			ps.init();
			ps.run();
			ps.close();
		}
	}
}
