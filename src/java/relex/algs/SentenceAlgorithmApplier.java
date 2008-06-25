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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import relex.ParsedSentence;
import relex.concurrent.RelexContext;

/**
 * SentenceAlgorithmApplier is responsible for loading SentenceAlgorithms from a
 * file, and applying them to a ParsedSentence.
 */
public class SentenceAlgorithmApplier
{
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

	public SentenceAlgorithmApplier()
	{
		read();
	}
	
	private void addAlg(SentenceAlgorithm alg, String initString)
	{
		alg.init(initString); // init the algorithm
		algs.add(alg); // add it to algs vector
		if (verbosity > 1)
			System.err.println("Info: Adding alg: " + alg.getSignature());
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
	public void read()
	{
		InputStream in = getAlgorithmsFile();
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
			throw new RuntimeException("Problem reading relex semantic algorithms file.");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Invalid class: " + e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize class: " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access class: " + e);
		}
	}

	/**
	 * Determine the relex algorithms file will be used. 
	 * 
	 * First try to load the the file defined by the system property
	 * relex.algpath. Then try to load the file as a resource in the
	 * jar file.  Finally, tries the default location (equivalent to
	 * -Drelex.algpath=./data/relex-semantic-algs.txt)
	 * 
	 * @return
	 */
	public static InputStream getAlgorithmsFile()
	{
		try
		{
			InputStream in = null; 
			String algsFileName = System.getProperty("relex.algpath");
			if (algsFileName!=null)
			{
				in = new FileInputStream(algsFileName);
				if (in != null)
				{
					if (verbosity > 0)
						System.err.println("Info: Using relex algorithms file defined in relex.algpath:" + algsFileName);
					return in;
				}
			}
			
			in = SentenceAlgorithmApplier.class.getResourceAsStream("/relex-semantic-algs.txt");
			if (in != null)
			{
				if (verbosity > 0)
					System.err.println("Info: Using relex algorithms file defined as a resource.");
				return in;
			}
	
			String defaultRelexSemanticAlgsFile = "./data/relex-semantic-algs.txt";
			in = new FileInputStream(defaultRelexSemanticAlgsFile);
			if (in != null)
			{
				if (verbosity > 0)
					System.err.println("Info: Using default relex algorithms file "+defaultRelexSemanticAlgsFile);
				return in;
			}
	
			throw new RuntimeException("Error reading semantic algorithms file.");
		}
		catch (FileNotFoundException exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	public static void main(String[] args)
	{
		new SentenceAlgorithmApplier();
	}
}
