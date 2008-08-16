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

package relex.parser;

import relex.util.socket.ProcessProtocol;

public class LinkParserProtocol extends ProcessProtocol
{
	private static final int verbosity = 1;

	// All MSG Strings must end with END_CHAR, and cannot have it anywhere else
	// in the string
	// public static final String END_CHAR = "]";
	public static final String END_CHAR = "\0";

	// VERBOSE version
	public static final String MSG_GET_VERSION = "[getVersion]\0";

	public static final String MSG_SET_MAX_PARSE_SECONDS = "[setMaxParseSeconds]\0";

	public static final String MSG_SET_MAX_COST = "[setMaxCost]\0";

	public static final String MSG_IS_PAST_TENSE_FORM = "[isPastTenseForm]\0";

	public static final String MSG_IS_ENTITY = "[isEntity]\0";

	public static final String MSG_PARSE = "[parse]\0";

	public static final String MSG_GET_NUM_LINKAGES = "[getNumLinkages]\0";

	public static final String MSG_MAKE_LINKAGE = "[makeLinkage]\0";

	public static final String MSG_GET_CONSTITUENT_STRING = "[getConstituentString]\0";

	public static final String MSG_GET_NUM_SKIPPED_WORDS = "[getNumSkippedWords]\0";

	public static final String MSG_GET_NUM_WORDS = "[getNumWords]\0";

	public static final String MSG_GET_WORD = "[getWord]\0";

	public static final String MSG_GET_LINKAGE_WORD = "[getLinkageWord]\0";

	public static final String MSG_GET_LINKAGE_NUM_VIOLATIONS = "[getLinkageNumViolations]\0";

	public static final String MSG_GET_LINKAGE_AND_COST = "[getLinkageAndCost]\0";
	public static final String MSG_GET_LINKAGE_DISJUNCT_COST = "[getLinkageDisjunctCost]\0";
	public static final String MSG_GET_LINKAGE_LINK_COST = "[getLinkageLinkCost]\0";

	public static final String MSG_GET_NUM_LINKS = "[getNumLinks]\0";

	public static final String MSG_GET_LINK_STRING = "[getLinkString]\0";

	public static final String MSG_GET_LINK_L_WORD = "[getLinkLWord]\0";

	public static final String MSG_GET_LINK_R_WORD = "[getLinkRWord]\0";

	public static final String MSG_GET_LINK_LABEL = "[getLinkLabel]\0";

	public static final String MSG_GET_LINK_L_LABEL = "[getLinkLLabel]\0";

	public static final String MSG_GET_LINK_R_LABEL = "[getLinkRLabel]\0";

	public static final String MSG_INIT = "[init]\0";

	public static final String MSG_CLOSE = "[close]\0";

	public static final String MSG_RETURN_SUCCESS = "[success]\0";

	public static final String MSG_KILL_SYSTEM = "[killSystem]\0";

	// public static final String MSG_SET_MAX_PARSE_SECONDS = "a]";
	//
	// public static final String MSG_SET_MAX_COST = "b]";
	//
	// public static final String MSG_IS_PAST_TENSE_FORM = "c]";
	//
	// public static final String MSG_IS_ENTITY = "d]";
	//
	// public static final String MSG_PARSE = "e]";
	//
	// public static final String MSG_GET_NUM_LINKAGES = "f]";
	//
	// public static final String MSG_MAKE_LINKAGE = "g]";
	//
	// public static final String MSG_GET_CONSTITUENT_STRING = "h]";
	//
	// public static final String MSG_GET_NUM_SKIPPED_WORDS = "i]";
	//
	// public static final String MSG_GET_NUM_WORDS = "j]";
	//
	// public static final String MSG_GET_WORD = "k]";
	//
	// public static final String MSG_GET_LINKAGE_NUM_VIOLATIONS = "l]";
	//
	// public static final String MSG_GET_LINKAGE_DISJUNCT_COST = "m]";
	//
	// public static final String MSG_GET_NUM_LINKS = "n]";
	//
	// public static final String MSG_GET_LINK_STRING = "o]";
	//
	// public static final String MSG_GET_LINK_L_WORD = "p]";
	//
	// public static final String MSG_GET_LINK_R_WORD = "q]";
	//
	// public static final String MSG_GET_LINK_LABEL = "r]";
	//
	// public static final String MSG_GET_LINK_L_LABEL = "s]";
	//
	// public static final String MSG_GET_LINK_R_LABEL = "t]";
	//
	// public static final String MSG_INIT = "u]";
	//
	// public static final String MSG_CLOSE = "v]";
	//
	// public static final String MSG_RETURN_SUCCESS = "w]";
	//
	// public static final String MSG_KILL_SYSTEM = "x]";

	private LinkParserClient client;

	@SuppressWarnings("unused")
	private LinkParserProtocol() {
	}

	public LinkParserProtocol(LinkParserClient _client) {
		client = _client;
	}

	public String processInput(String input) {
		if (input.equals(ProcessProtocol.HANDSHAKE_REQUEST))
			return ProcessProtocol.HANDSHAKE_RESPONSE;
		// find the message and optional argument
		int end = input.indexOf(END_CHAR) + 1;
		if (end == 0)
			throw new RuntimeException("LinkParserProtocol: Malformed Message:" + input);
		String message = input.substring(0, end);
		String arg = null;
		if (end < input.length())
			arg = input.substring(end);

		// process the message
		if (message.equals(MSG_SET_MAX_PARSE_SECONDS)) {
			client.setMaxParseSeconds(Integer.parseInt(arg));
		} else if (message.equals(MSG_SET_MAX_COST)) {
			client.setMaxCost(Integer.parseInt(arg));
		} else if (message.equals(MSG_IS_PAST_TENSE_FORM)) {
			return makeMessage(Boolean.toString(client.isPastTenseForm(arg)));
		} else if (message.equals(MSG_IS_ENTITY)) {
			return makeMessage(Boolean.toString(client.isEntity(arg)));
		} else if (message.equals(MSG_PARSE)) {
			System.out.println("Parsing "+arg);
			client.execParse(arg);
		} else if (message.equals(MSG_GET_NUM_LINKAGES)) {
			return makeMessage(Integer.toString(client.getNumLinkages()));
		} else if (message.equals(MSG_MAKE_LINKAGE)) {
			client.makeLinkage(Integer.parseInt(arg));
		} else if (message.equals(MSG_GET_CONSTITUENT_STRING)) {
			return makeMessage(client.getConstituentString());
		} else if (message.equals(MSG_GET_NUM_SKIPPED_WORDS)) {
			return makeMessage(Integer.toString(client.getNumSkippedWords()));
		} else if (message.equals(MSG_GET_NUM_WORDS)) {
			return makeMessage(Integer.toString(client.getNumWords()));
		} else if (message.equals(MSG_GET_WORD)) {
			return makeMessage(client.getWord(Integer.parseInt(arg)));
		} else if (message.equals(MSG_GET_LINKAGE_WORD)) {
			return makeMessage(client.getLinkageWord(Integer.parseInt(arg)));
		} else if (message.equals(MSG_GET_LINKAGE_NUM_VIOLATIONS)) {
			return makeMessage(Integer.toString(client.getLinkageNumViolations()));
		} else if (message.equals(MSG_GET_LINKAGE_AND_COST)) {
			return makeMessage(Integer.toString(client.getLinkageAndCost()));
		} else if (message.equals(MSG_GET_LINKAGE_DISJUNCT_COST)) {
			return makeMessage(Integer.toString(client.getLinkageDisjunctCost()));
		} else if (message.equals(MSG_GET_LINKAGE_LINK_COST)) {
			return makeMessage(Integer.toString(client.getLinkageLinkCost()));
		} else if (message.equals(MSG_GET_NUM_LINKS)) {
			return makeMessage(Integer.toString(client.getNumLinks()));
		} else if (message.equals(MSG_GET_LINK_STRING)) {
			return makeMessage(client.getLinkString());
		} else if (message.equals(MSG_GET_LINK_L_WORD)) {
			return makeMessage(Integer.toString(client.getLinkLWord(Integer.parseInt(arg))));
		} else if (message.equals(MSG_GET_LINK_R_WORD)) {
			return makeMessage(Integer.toString(client.getLinkRWord(Integer.parseInt(arg))));
		} else if (message.equals(MSG_GET_LINK_LABEL)) {
			return makeMessage(client.getLinkLabel(Integer.parseInt(arg)));
		} else if (message.equals(MSG_GET_LINK_L_LABEL)) {
			return makeMessage(client.getLinkLLabel(Integer.parseInt(arg)));
		} else if (message.equals(MSG_GET_LINK_R_LABEL)) {
			return makeMessage(client.getLinkRLabel(Integer.parseInt(arg)));
		} else if (message.equals(MSG_GET_VERSION)) {
			return makeMessage(client.getVersion());
		} else if (message.equals(MSG_INIT)) {
			if (null == arg || "".equals(arg) || "null".equals(arg))
				client.init();
			else
				client.init(arg);				
		} else if (message.equals(MSG_CLOSE)) {
			client.close();
		} else if (message.equals(MSG_KILL_SYSTEM)) {
			// if (verbosity >= 1)
			// System.out.println("Preparing to shut down JVM");
			// client.close(); commented
			if (verbosity >= 1)
				System.out.println("Shutting down JVM.");
			System.exit(0);
		} else { // ERROR throws exception
			throw new RuntimeException("LinkParserProtocol: Unknown Message");
		}
		// For messages that successfully processed but return makeMessage(no
		// value, return makeMessage(MSG_SUCCESS)
		return makeMessage(MSG_RETURN_SUCCESS);
	}

}
