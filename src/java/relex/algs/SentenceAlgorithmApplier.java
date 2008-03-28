package relex.algs;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import relex.ParsedSentence;
import relex.parser.LinkParserClient;

/**
 * SentenceAlgorithmApplier is responsible for loading SentenceAlgorithms from a
 * file, and applying them to a ParsedSentence.
 */
public class SentenceAlgorithmApplier {

	/** a debug variable */
	private static int verbosity = 0;

	/** The list of algorithms to be applied */
	private ArrayList<SentenceAlgorithm> algs;

	/** The name of the algorithms package */
	private static String ALGS_PACKAGE = "relex.algs";

	/** The character in an algfile which preceeds a classname */
	private static char CLASS_CHAR = '#';

	/** The character in an algfile which preceeds a comment. */
	private static char COMMENT_CHAR = ';';

	private void addAlg(SentenceAlgorithm alg, String initString) {
		alg.init(initString); // init the algorithm
		algs.add(alg); // add it to algs vector
		if (verbosity > 0)
			System.out.println("Adding alg: " + alg.getSignature());
	}

	// The apply method!
	public void applyAlgs(ParsedSentence sentence, LinkParserClient lpc) {
		for (SentenceAlgorithm alg: algs){
			alg.apply(sentence, lpc);
		}
	}

	/**
	 *  Read in the set of SentenceAlgorithms
	 */
	public void read(File file) {
		algs = new ArrayList<SentenceAlgorithm>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			throw new RuntimeException("Problem reading file " + file);
		}
		SentenceAlgorithm curAlg = null;
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			while (br.ready()) {
				line = br.readLine();
				if (line.length() > 0) {
					if (line.charAt(0) == CLASS_CHAR) {
						// If the line begins with CLASS_CHAR, use the current
						// buffer to create a new algorithm
						// System.out.println(sb);
						if (curAlg != null) {
							addAlg(curAlg, sb.toString());
							sb.delete(0, sb.length()); // ready the buffer for next alg
						}
						Class<?> c = Class.forName(ALGS_PACKAGE + "." + line.substring(1));
						curAlg = (SentenceAlgorithm) c.newInstance();
					} else { // for normal lines
						if (line.charAt(0) != COMMENT_CHAR) // skip comments
							sb.append(line).append("\n");   // add to string buffer
					}
				}
			}
			if (curAlg != null) {
				addAlg(curAlg, sb.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException("Problem reading file " + file);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Invalid class: " + e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize class: " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access class: " + e);
		}
	}

}
