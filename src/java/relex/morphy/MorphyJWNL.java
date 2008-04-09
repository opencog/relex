package relex.morphy;
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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import relex.morphy.TextVacuum;

/**
 * Return information about the base form (lemma) of a word.
 * Uses Wordnet to get this info, plus special rules for contractions
 * such as "didn't" , "they're", "I'm", etc.
 *
 * The interface is straightforward: specify the word in the
 * constructor (or use the factory). Then use one (or more) of the
 * methods getNounString(), getVerbString(), getAdjString(), or
 * getAdvString() to get the base form.
 *
 * Example:
 * System.out.println(Morphy.getMorphy("dogs").getNounString());
 * should print "dog".
 *
 * The system attempts to load locally installed wordnet databases
 * as specified in the "file_properties.xml" config file. This
 * file can be specified with the java system -Dwordnet.configfile
 * option. If the config file cannot be found, then it attempts to
 * use the command-line wordnet command instead. It assumes the
 * command-line command is called "wn" and can be found in the
 * system executable file search path.
 */
public class MorphyJWNL implements Morphy
{
	public static final boolean DEBUG = false;
	// STATIC ATTRIBUTES

	private static HashMap<String, String> irregularVerbContractions = new HashMap<String, String>();
	private static HashMap<String, String> possessiveAdjRoots = new HashMap<String, String>();
	private static HashMap<String, String> possessiveNounRoots = new HashMap<String, String>();
	private static HashMap<String, String> standardContractions = new HashMap<String, String>();

	/* static private initializer */
	static {
		irregularVerbContractions.put("won't", "will");
		irregularVerbContractions.put("can't", "can");
		irregularVerbContractions.put("cannot", "can");

		// Hmm .. ain't ain't conjugated. 
		// I ain't, you ain't, we ain't, they ain't changing at all.
		irregularVerbContractions.put("ain't", "am");

		possessiveAdjRoots.put("my", "me");
		possessiveNounRoots.put("mine", "me");

		possessiveAdjRoots.put("his", "him");
		possessiveNounRoots.put("his", "him");

		possessiveNounRoots.put("her", "her");
		possessiveAdjRoots.put("hers", "her");

		possessiveAdjRoots.put("our", "us");
		possessiveNounRoots.put("ours", "us");

		possessiveAdjRoots.put("their", "them");
		possessiveNounRoots.put("theirs", "them");

		possessiveAdjRoots.put("your", "you");
		possessiveNounRoots.put("yours", "you");

		standardContractions.put( "'re", "are" );
		standardContractions.put( "'m", "am" );
	}


	/* ============================================================== */
	private static final String DEFAULT_WORDNET_CONFIG_FILE =
	      "data"+File.separator+"wordnet"+File.separator+"file_properties.xml";

	private boolean javaWordnetFound = false;

	/**
	 * Attempts to load the wordnet data files, assuming
	 * that thier location is given in a file called
	 * "file_properties.xml"
	 */
	private void initWordNet()
	{
		String wordNetConfigFile = null;
		try
		{
			wordNetConfigFile = System.getProperty("wordnet.configfile");
			if (wordNetConfigFile == null)
			{
				if (DEBUG)
					System.out.println(
                   "WordNet config file not set(wordnet.configfile).\n" +
				       "Trying default path: " + DEFAULT_WORDNET_CONFIG_FILE);
				wordNetConfigFile = DEFAULT_WORDNET_CONFIG_FILE;
			}
			if (DEBUG)
				System.out.print("Reading Wordnet Java API...");
			JWNL.initialize(new FileInputStream(wordNetConfigFile));
			if (DEBUG)
				System.out.println("ok");
			javaWordnetFound = true;
		}
		catch (Exception ex)
		{
			System.err.println(
			       "Unable to load Java Wordnet files from config file: " +
					 wordNetConfigFile + " .\n" +
			       "Will use command-line interface; this will negatively " + 
			       "impact performance.\n" + 
			       ex.getMessage());
		}
	}

	/* ============================================================== */

	public MorphyJWNL()
	{
	}

	/* (non-Javadoc)
	 * @see relex.morphy.Morphy#initialize()
	 */
	public void initialize()
	{
		initWordNet();
	}

	public Morphed morph(String word)
	{
		Morphed m = new Morphed(word);
		if (javaWordnetFound)
			loadLocal(m);
		else
			load(m);
		return m;
	}

	/* ============================================================== */

	private String convertStandardContraction(String word)
	{
		String s = standardContractions.get(word);
		if (s!=null)
			return s;
		return word;
	}

	/**
	 * Attempts to strip a negative contraction off the word.
	 */
	protected String stripNegativeContraction(String word)
	{
		String root = irregularVerbContractions.get(word);
		if (root == null) {
			int x = word.length() - 3;
			if (x > 0 && word.substring(x).equals("n't")) {
				root = word.substring(0, x);
			}
		}
		if (root != null)
			return root;
		return word;
	}

	protected String maybeChangeFirstLetter(String originalString, String modifiedString) {
		if (Character.isUpperCase(originalString.charAt(0))) {
			if (modifiedString.length() > 1) {
				return Character.toUpperCase(modifiedString.charAt(0)) + modifiedString.substring(1);
			}
			return modifiedString.toUpperCase();
		}
		return modifiedString;
	}

	protected boolean loadPossessive(String word, Morphed m)
	{
		boolean found = false;
		String root = possessiveNounRoots.get(word);
		if (root != null) {
			found = true;
			m.putRoot(NOUN_F, root);
		}
		root = possessiveAdjRoots.get(word);
		if (root != null) {
			found = true;
			m.putRoot(ADJ_F, root);
		}
		return found;
	}

	/**
	 * Use the command-line version of wordnet to obtain the morphology.
	 * Uses the "wn" shell command (e.g. /usr/bin/wn) to get the desired
	 * form.
	 */
	private void load(Morphed m)
	{
		String word = m.getOriginal();

 		// If it is a common possessive form of personal pronoun.
		if (loadPossessive(word, m))
			return;

		word = stripNegativeContraction(word);
		boolean negativeVerb = !word.equals(m.getOriginal());

		word = convertStandardContraction(word);

		String[] command = new String[2];
		command[0] = "wn"; // must be in operating system file path!
		command[1] = word;
		try {
			Process ss = Runtime.getRuntime().exec(command);
			TextVacuum vac = new TextVacuum(ss);
			vac.start();
			vac.join();
			Iterator<String> i = vac.iterator();
			while (i.hasNext()) {
				parseWordnetCommandOutput(i.next(), negativeVerb, m);
			}
		} catch (Exception e) {
			System.out.println("Error calling wordnet command " + e);
			e.printStackTrace();
		}
		// In case root wasn't in wordnet, add it here.
		if ((negativeVerb) && m.getVerbString() == null) {
			m.putRootNegative("verb", word);
		}
	}

	/**
	 * Look through the wordnet command-line ouput for a line
	 * that looks like e.g. "Information available for noun dog"
	 * which tells us that "dog" is the singular for "dogs" and
	 * its a noun.
	 */
	private void parseWordnetCommandOutput(String line, 
	                                       boolean negativeVerb,
	                                       Morphed m)
	{
		String template = "Information available for ";
		if (line.indexOf(template) != 0)
			return;
		String rest = line.substring(template.length());
		int space = rest.indexOf(" ");
		String cat = rest.substring(0, space);
		if (negativeVerb && !cat.equals("verb"))
			return; // dont check for non-verb roots if we already know its a negative verb
		String root = rest.substring(space + 1);
		if (Character.isUpperCase(m.getOriginal().charAt(0))) {
			char[] chars = root.toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			root = new String(chars);
		}
		if (cat.equals("noun"))
			m.putRoot(NOUN_F, root);
		else if (cat.equals("verb")) {
			if (negativeVerb)
				m.putRootNegative(VERB_F, root);
			else
				m.putRoot(VERB_F, root);
		} else if (cat.equals("adj"))
			m.putRoot(ADJ_F, root);
		else if (cat.equals("adv"))
			m.putRoot(ADV_F, root);
		else
			throw new RuntimeException("Unknown WordNet category: [" + cat + "] with root [" + root + "]");
	}

	/**
	 * Use the jwnl interfaces to look up a word, straight from
	 * the locally-installed wordnet databases.
	 */
	private void loadLocal(Morphed m)
	{
		String word = m.getOriginal();

 		// If it is a common possessive form of personal pronoun.
		if (loadPossessive(word, m))
			return;

		word = stripNegativeContraction(word);
		boolean negativeVerb = !word.equals(m.getOriginal());

		word = convertStandardContraction(word);

		try {
			Dictionary dict = Dictionary.getInstance();
			IndexWord verb = dict.lookupIndexWord(POS.VERB, word);

			// Dont check for non-verb roots if we already 
			// know its a negative verb.
			if (negativeVerb && verb == null) {
				return;
			}

			IndexWord noun = dict.lookupIndexWord(POS.NOUN, word);
			IndexWord adj = dict.lookupIndexWord(POS.ADJECTIVE, word);
			IndexWord adv = dict.lookupIndexWord(POS.ADVERB, word);
			if (noun != null) {
				m.putRoot(NOUN_F, maybeChangeFirstLetter(m.getOriginal(), noun.getLemma()));
			}
			if (verb != null) {
				if (negativeVerb) {
					m.putRootNegative(VERB_F, maybeChangeFirstLetter(m.getOriginal(), verb.getLemma()));
				} else {
					m.putRoot(VERB_F, maybeChangeFirstLetter(m.getOriginal(), verb.getLemma()));
				}
			}
			if (adj != null) {
				m.putRoot(ADJ_F, maybeChangeFirstLetter(m.getOriginal(), adj.getLemma()));
			}
			if (adv != null) {
				m.putRoot(ADV_F, maybeChangeFirstLetter(m.getOriginal(), adv.getLemma()));
			}

		} catch (JWNLException ex) {
			System.out.println("Error finding wordnet dictionaries" + ex);
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

		// In case root wasn't in wordnet, add it here.
		if ((negativeVerb) && m.getVerbString() == null) {
			m.putRootNegative("verb", word);
		}
	}

	public static void main(String[] args)
	{
//		Morphy morphy = getInstance();
		Morphy morphy = new MorphyJWNL();
		System.out.println(morphy.morph(args[0]));
	}
}
