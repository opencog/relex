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

import java.util.ArrayList;

import relex.RelexProperties;
import relex.util.socket.ProcessClient;

/**
 * This class serves as a wrapper to the C linkparser library.
 * Because it is using C, the class is not internally organized
 * in a very good OOP way. -- Someday, this should be fixed.
 * 
 * @deprecated
 */
public class LinkParserSocketClient extends LinkParserClient
{
	private static final int DEFAULT_PORT = 9000;

	private static final String DEFAULT_HOST = "127.0.0.1";

	private static final int verbosity = 1;

	// Properties set from RelexProperties
	private int parseCountBetweenKills;

	private int parseCount;

	private ProcessClient client;

	private ArrayList<String> restoreCommands;

	public LinkParserSocketClient() {
		this(DEFAULT_HOST,DEFAULT_PORT);
	}

	public LinkParserSocketClient(String host, int port) {
		super();
		setProperties();
		parseCount = 0;
		client = new ProcessClient(
				host,
				port,
				RelexProperties.getIntProperty("relex.parser.LinkParserSocketClient.millisecondsBetweenConnectionAttempts"),
				RelexProperties.getIntProperty("relex.parser.LinkParserSocketClient.failedRequestRepeatLimit")
				);
		restoreCommands = new ArrayList<String>();
		resetRestoreCommands();
	}

	private void setProperties() {
		parseCountBetweenKills = RelexProperties.getIntProperty("relex.parser.LinkParserSocketClient.parseCountBetweenKills");
	}

	private void resetRestoreCommands() {
		restoreCommands.clear();
		restoreCommands.add(LinkParserProtocol.MSG_INIT + serverParams.pathName);
		restoreCommands.add(LinkParserProtocol.MSG_SET_MAX_PARSE_SECONDS+ serverParams.maxParseSeconds);
		if (serverParams.maxCost != null)
			restoreCommands.add(LinkParserProtocol.MSG_SET_MAX_PARSE_SECONDS+ serverParams.maxCost);
	}

	private String exec(String command)
	{
		if (verbosity > 3) System.out.println("LinkParserSocketClient Executing:" + command);
		String response = client.process(command, restoreCommands);
		if (verbosity > 3) System.out.println("LinkParserSocketClient Received:" + response);

		// Avoid infinite loop, if link parser crashes.
		if (response != null)
			restoreCommands.add(command);
		return response;
	}

	public String getVersion() {
		return exec(LinkParserProtocol.MSG_GET_VERSION);
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		super.setMaxParseSeconds(maxParseSeconds);
		exec(LinkParserProtocol.MSG_SET_MAX_PARSE_SECONDS + maxParseSeconds);
	}

	public void setMaxCost(int maxCost) {
		super.setMaxCost(maxCost);
		exec(LinkParserProtocol.MSG_SET_MAX_COST + maxCost);
	}

	
	public void init() {
		// we send the empty string to the link parser server so it will use its local relex.linkparserpath
		init(""); 
	}

	// These are not added to restorecommands, so we do not use exec
	public void init(String linkGrammarDictionariesPath) {
		client.process(LinkParserProtocol.MSG_INIT + linkGrammarDictionariesPath, null);
	}

	public boolean isPastTenseForm(String word) {
		return Boolean.valueOf(exec(LinkParserProtocol.MSG_IS_PAST_TENSE_FORM + word)).booleanValue();
	}

	public boolean isEntity(String word) {
		return Boolean.valueOf(exec(LinkParserProtocol.MSG_IS_ENTITY + word)).booleanValue();
	}

	public void killServer() {
		client.process(LinkParserProtocol.MSG_KILL_SYSTEM, null);
	}

	public void close()	{
		try {
			client.closeConnection();
		}
		catch (java.io.IOException ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	void execParse(String sentence) {
		parseCount++;
		if ((parseCountBetweenKills != 0) && parseCount >= parseCountBetweenKills) {
			parseCount = 0;
			if (verbosity >= 1)
				System.out
						.println("LinkparserSocketClient kill limit exceeded.  Killing Server...");
			client.process(LinkParserProtocol.MSG_KILL_SYSTEM, null);
		}
		// when a new sentence is parsed, we reinitialize the restore commands
		resetRestoreCommands();
		if (exec(LinkParserProtocol.MSG_PARSE + sentence) == null) {
			if (verbosity >= 1)
				System.out
						.println("LinkparserSocketClient giving up on sentence:"
								+ sentence);
			throw new RuntimeException("Linkparser Failure for sentence:"
					+ sentence);
		}
	}

	int getNumLinkages() {
		String result=null;
		int num = 0;
		try{
			result = exec(LinkParserProtocol.MSG_GET_NUM_LINKAGES);
			num = Integer.parseInt(result);
		} catch(NullPointerException npe){
			System.err.println("\nLinkParserProtocol.MSG_GET_NUM_LINKAGES returned null!");
			return 0;
		} catch(NumberFormatException nfe){
			System.err.println("\nLinkParserProtocol.MSG_GET_NUM_LINKAGES returned an invalid number: "+result);
			return 0;
		}
		return num;
	}

	void makeLinkage(int i) {
		exec(LinkParserProtocol.MSG_MAKE_LINKAGE + i);
	}

	String getConstituentString() {
		return exec(LinkParserProtocol.MSG_GET_CONSTITUENT_STRING);
	}

	int getNumSkippedWords() {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_NUM_SKIPPED_WORDS));
	}

	int getNumWords() {
		return Integer.parseInt(exec(LinkParserProtocol.MSG_GET_NUM_WORDS));
	}

	String getWord(int w) {
		return exec(LinkParserProtocol.MSG_GET_WORD + w);
	}

	String getLinkageWord(int w) {
		return exec(LinkParserProtocol.MSG_GET_LINKAGE_WORD + w);
	}

	String getLinkageDisjunct(int w) {
		return exec(LinkParserProtocol.MSG_GET_LINKAGE_DISJUNCT + w);
	}

	int getLinkageAndCost() {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINKAGE_AND_COST));
	}

	int getLinkageDisjunctCost() {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINKAGE_DISJUNCT_COST));
	}

	int getLinkageLinkCost() {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINKAGE_LINK_COST));
	}

	int getLinkageNumViolations() {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINKAGE_NUM_VIOLATIONS));
	}

	int getNumLinks() {
		return Integer.parseInt(exec(LinkParserProtocol.MSG_GET_NUM_LINKS));
	}

	String getLinkString() {
		return exec(LinkParserProtocol.MSG_GET_LINK_STRING);
	}

	int getLinkLWord(int i) {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINK_L_WORD + i));
	}

	int getLinkRWord(int i) {
		return Integer
				.parseInt(exec(LinkParserProtocol.MSG_GET_LINK_R_WORD + i));
	}

	String getLinkLabel(int i) {
		return exec(LinkParserProtocol.MSG_GET_LINK_LABEL + i);
	}

	String getLinkLLabel(int i) {
		return exec(LinkParserProtocol.MSG_GET_LINK_L_LABEL + i);
	}

	String getLinkRLabel(int i) {
		return exec(LinkParserProtocol.MSG_GET_LINK_R_LABEL + i);
	}

}
