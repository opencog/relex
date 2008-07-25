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

import org.linkgrammar.LinkGrammar;

/**
 * This LinkGrammar.class serves as a wrapper to the C linkparser library.
 * The LinkGrammar.class is not internally organized in a very good OOP way.
 * This should be fixed.
 *
 * This class is for use with link-grammar versions 4.2.6 and later
 */

public class LinkParserJNINewClient extends LinkParserClient
{
	private static final int verbosity = 0;

	private static LinkParserJNINewClient singletonInstance = null;

	private LinkParserJNINewClient() {
		super();
	}

	static {
		singletonInstance = new LinkParserJNINewClient();
	}

	public static LinkParserJNINewClient getSingletonInstance() {
		return singletonInstance;
	}

	public String getVersion() {
		// This requires link-grammar version 4.3.6 or later.
		return LinkGrammar.getVersion();
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		super.setMaxParseSeconds(maxParseSeconds);
		LinkGrammar.setMaxParseSeconds(maxParseSeconds);
	}

	public void setMaxCost(int maxCost) {
		super.setMaxCost(maxCost);
		LinkGrammar.setMaxCost(maxCost);
	}

	public boolean isPastTenseForm(String word) {
		return LinkGrammar.isPastTenseForm(word);
	}

	public boolean isEntity(String word) {
		return LinkGrammar.isEntity(word);
	}

	public void close() {
		LinkGrammar.close();
	}

	public void init() {
		init(null);
	}

	public void init(String linkGrammarDictionariesPath) {
		super.init(linkGrammarDictionariesPath);
		if (verbosity > 3) System.err.println("Info: LinkParserJNINewClient: initializing.");
		LinkGrammar.init();
	}

	void execParse(String sentence)
	{
		Long starttime;
		if (verbosity > 0)
		{
			if (verbosity > 3) System.err.println("Info: parsing:" + sentence + "[end_sentence]");
			starttime = System.currentTimeMillis();
		}
		LinkGrammar.parse(sentence);
		if (verbosity > 0)
		{
			Long now = System.currentTimeMillis();
			Long elapsed = now - starttime;
			System.err.println("Info: Link parse time: " + elapsed + " milliseconds");
			if (verbosity > 3) System.err.println("Info: Parsing LinkGrammar.completed.");
		}
	}

	int getNumLinkages() {
		return LinkGrammar.getNumLinkages();
	}

	void makeLinkage(int i)
	{
		LinkGrammar.makeLinkage(i);
	}

	String getConstituentString() {
		return LinkGrammar.getConstituentString();
	}

	int getNumSkippedWords() {
		return LinkGrammar.getNumSkippedWords();
	}

	int getNumWords() {
		return LinkGrammar.getNumWords();
	}

	String getWord(int w) {
		return LinkGrammar.getWord(w);
	}

	String getLinkageWord(int w) {
		// XXX-- hack alert stub out until link-grammar 
		// version 4.3.7 is commonly available.
		// return LinkGrammar.getLinkageWord(w);
		return LinkGrammar.getWord(w);
	}

	int getLinkageAndCost() {
		return LinkGrammar.getLinkageAndCost();
	}

	int getLinkageDisjunctCost() {
		return LinkGrammar.getLinkageDisjunctCost();
	}

	int getLinkageLinkCost() {
		return LinkGrammar.getLinkageLinkCost();
	}

	int getLinkageNumViolations() {
		return LinkGrammar.getLinkageNumViolations();
	}

	int getNumLinks() {
		return LinkGrammar.getNumLinks();
	}

	String getLinkString() {
		return LinkGrammar.getLinkString();
	}

	int getLinkLWord(int i) {
		return LinkGrammar.getLinkLWord(i);
	}

	int getLinkRWord(int i) {
		return LinkGrammar.getLinkRWord(i);
	}

	String getLinkLabel(int i) {
		return LinkGrammar.getLinkLabel(i);
	}

	String getLinkLLabel(int i) {
		return LinkGrammar.getLinkLLabel(i);
	}

	String getLinkRLabel(int i) {
		return LinkGrammar.getLinkRLabel(i);
	}
}

