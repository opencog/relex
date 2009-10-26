/*
 * Copyright 2008 Novamente LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.parser_old;

import java.io.File;

import org.linkgrammar.LinkGrammar;

/**
 * The abstract interface to the C linkparser. Extensions of this class can link
 * directly to the linkparser through JNI, or through some other call method
 * such as Sockets
 * 
 * @deprecated
 */
public abstract class LinkParserClient
{
	// ServerParams are parameters that are used by the link parser c code
	class ServerParams
	{
		String pathName = LinkParser.retrievePathName();
		int maxParseSeconds = 20;
		Integer maxCost = null;
	}

	// ClientParams are parameters that are used only by the client code
	// to determine how it returns values
	class ClientParams
	{
		int max_parses = 25;
		boolean ALLOW_SKIPPED_WORDS = false;
	}

	ServerParams serverParams = new ServerParams();

	ClientParams clientParams = new ClientParams();

	// PUBLIC METHODS
	public void setMaxParses(int maxParses) {
		clientParams.max_parses = maxParses;
	}

	public int getMaxParses() {
		return clientParams.max_parses;
	}

	public void setAllowSkippedWords(boolean val) {
		clientParams.ALLOW_SKIPPED_WORDS = val;
	}

	public boolean getAllowSkippedWords() {
		return clientParams.ALLOW_SKIPPED_WORDS;
	}

	public void setMaxParseSeconds(int maxParseSeconds) {
		serverParams.maxParseSeconds = maxParseSeconds;
		// must extend this!
	}

	public void setMaxCost(int maxCost) {
		serverParams.maxCost = new Integer(maxCost);
		// must extend this!
	}

	public void init(){
		init(System.getProperty("relex.linkparserpath"));
	}

	public void init(String linkGrammarDictionariesPath) {
		if (linkGrammarDictionariesPath==null) {
			System.err.println("Info: Using default relex.linkparserpath");
		} else {
			File dir = new File(linkGrammarDictionariesPath);
			if (!dir.isDirectory() || !dir.canRead()) 
				throw new IllegalArgumentException(linkGrammarDictionariesPath == null ? 
						"Error reading default relex.linkparserpath" :
						"Error reading relex.linkparserpath: "+linkGrammarDictionariesPath);
			try {
				System.err.println("Info: Using relex.linkparserpath: "+dir.getCanonicalPath());
				LinkGrammar.setDictionariesPath(linkGrammarDictionariesPath);
			} catch (Throwable t){
				throw new IllegalArgumentException("Invalid relex.linkparserpath: "+linkGrammarDictionariesPath);			
			}
		}
	}

	abstract public String getVersion();

	abstract public boolean isPastTenseForm(String word);

	abstract public boolean isEntity(String word);

	abstract public void close();

	// DEFAULT METHODS
	abstract void execParse(String sentence);

	abstract int getNumLinkages();

	abstract void makeLinkage(int i);

	abstract String getConstituentString();

	abstract int getNumSkippedWords();

	abstract int getNumWords();

	abstract String getWord(int w);

	abstract String getLinkageDisjunct(int w);

	abstract String getLinkageWord(int w);

	abstract int getLinkageAndCost();

	abstract int getLinkageDisjunctCost();

	abstract int getLinkageLinkCost();

	abstract int getLinkageNumViolations();

	abstract int getNumLinks();

	abstract String getLinkString();

	abstract int getLinkLWord(int i);

	abstract int getLinkRWord(int i);

	abstract String getLinkLabel(int i);

	abstract String getLinkLLabel(int i);

	abstract String getLinkRLabel(int i);

}
