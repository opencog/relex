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

package relex.algs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import relex.ParsedSentence;
import relex.concurrent.RelexContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static relex.utils.ResourceUtils.getResource;

/**
 * AlgorithmApplier is responsible for loading SentenceAlgorithms from a
 * file, and applying them to a ParsedSentence.
 */
public class AlgorithmApplier
{
	private static final Logger logger = LoggerFactory.getLogger(AlgorithmApplier.class);

	/** The list of algorithms to be applied */
	private ArrayList<SentenceAlgorithm> algs;

	/** The name of the algorithms package */
	private static final String ALGS_PACKAGE = "relex.algs";

	/** The character in an algfile which preceeds a classname */
	private static final char CLASS_CHAR = '#';

	/** The character in an algfile which preceeds a comment. */
	private static final char COMMENT_CHAR = ';';

	public AlgorithmApplier(String prop, String filename)
	{
		read(prop, filename);
	}

	private void addAlg(SentenceAlgorithm alg, String initString)
	{
		alg.init(initString); // init the algorithm
		algs.add(alg); // add it to algs vector
		logger.debug("Info: Adding alg: " + alg.getSignature());
	}

	// The apply method!
	public void applyAlgs(ParsedSentence sentence, RelexContext context)
	{
		for (SentenceAlgorithm alg: algs)
		{
			alg.apply(sentence, context);
		}
	}

	/**
	 *  Read in the set of SentenceAlgorithms
	 */
	public void read(String prop, String filename)
	{
		InputStream in = getResource(prop, filename, "data");
		algs = new ArrayList<SentenceAlgorithm>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

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
						// System.err.println(sb);
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
			throw new RuntimeException("Problem reading relex semantic algorithms file.");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Invalid class: " + e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize class: " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access class: " + e);
		}
	}
}
