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
package relex.morphy;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

public class MapMorphy implements Morphy
{
	public static final boolean DEBUG = true;
	public static final String NOUN_F = "noun";
	public static final String VERB_F = "verb";
	public static final String ADJ_F = "adj";
	public static final String ADV_F = "adv";
	public static final String ROOT_F = "root";
	public static final String TYPE_F = "type";
	public static final String NEG_F = "neg";

	private static Map<String, String> irregularVerbContractions = new TreeMap<String, String>();
	private static Map<String, String> possessiveAdjRoots = new TreeMap<String, String>();
	private static Map<String, String> possessiveNounRoots = new TreeMap<String, String>();
	private static Map<String, String> standardContractions = new TreeMap<String, String>();

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

	private static Map<POS, Stemmer> stemmers = new HashMap<POS, Stemmer>();
	static {
		stemmers.put(POS.NOUN,
				SuffixStemmer.makeSuffixStemmer("|ses=s|xes=x|zes=z|ches=ch|shes=sh|men=man|ies=y|s=|"));
		stemmers.put(POS.VERB,
				SuffixStemmer.makeSuffixStemmer("|ies=y|es=e|es=|ed=e|ed=|ing=e|ing=|s=|"));
		stemmers.put(POS.ADJECTIVE,
				SuffixStemmer.makeSuffixStemmer("|er=|est=|er=e|est=e|"));
	}

	final POS[] pos = new POS[] { POS.NOUN, POS.VERB, POS.ADVERB, POS.ADJECTIVE };

	Map<POS, Map<String, IndexWord>> partsOfSpeech = new HashMap<POS, Map<String,IndexWord>>();

	Map<POS, Map<String, TreeSet<String>>> exceptions = new HashMap<POS, Map<String, TreeSet<String>>>();

	@SuppressWarnings("unchecked")
	public void initialize() {
		MorphyFactory.initializeJWNL();

		Dictionary d = Dictionary.getInstance();
		for(int i = 0; i < pos.length; i++) {
			Map<String, TreeSet<String>> posExceptions = exceptions.get(pos[i]);
			if (posExceptions == null){
				posExceptions = new TreeMap<String, TreeSet<String>>();
				exceptions.put(pos[i], posExceptions);
			}
			try {
				for (Iterator it = d.getExceptionIterator(pos[i]); it.hasNext(); ){
					Exc exc = (Exc) it.next();
					String word = exc.getLemma();
					TreeSet<String> wordExceptions = posExceptions.get(word);
					if (wordExceptions==null){
						wordExceptions = new TreeSet<String>();
						posExceptions.put(word, wordExceptions);
					}
					wordExceptions.addAll(exc.getExceptions());
				}
				Map<String, IndexWord> indexWords = partsOfSpeech.get(pos[i]);
				if (indexWords == null){
					indexWords = new TreeMap<String, IndexWord>();
					partsOfSpeech.put(pos[i], indexWords);
				}
				for (Iterator it = d.getIndexWordIterator(pos[i]); it.hasNext(); ){
					IndexWord word = (IndexWord) it.next();
					String lemma = word.getLemma();
					indexWords.put(lemma, word);
				}
			} catch (JWNLException e) {
				e.printStackTrace();
			}
		}

	}

	public Morphed morph(String word) {
		Morphed m = new Morphed(word);
		load(m);
		return m;
	}

	private IndexWord lookup(POS pos, String word){
		String lookup = word.trim().toLowerCase();
		IndexWord indexWord = partsOfSpeech.get(pos).get(lookup);
		if (indexWord != null) return indexWord;

		IndexWord exception = lookupExceptions(pos, lookup);
		if (exception!=null) return exception;

		Stemmer stemmer = stemmers.get(pos);
		if (stemmer != null){
			for (String stemmed : stemmer.stemIt(word)){
				IndexWord stemmedResult = partsOfSpeech.get(pos).get(stemmed.toLowerCase());
				if (stemmedResult != null) return stemmedResult;
				else {
					IndexWord stemmedException = lookupExceptions(pos, stemmed.toLowerCase());
					if (stemmedException!=null) return stemmedException;
				}
			}
		}
		return null;
	}

	/**
	 * @param pos
	 * @param lookup
	 */
	private IndexWord lookupExceptions(POS pos, String lookup) {
		TreeSet<String> wordExceptions = exceptions.get(pos).get(lookup);
		if ( (wordExceptions!=null) && (wordExceptions.size()>0) ){
			for(String exception: wordExceptions){
				IndexWord exceptionResult = partsOfSpeech.get(pos).get(exception.toLowerCase());
				if (exceptionResult != null) return exceptionResult;
			}
		}
		return null;
	}

	private void load(Morphed m)
	{
		String word = m.getOriginal();

 		// If it is a common possessive form of personal pronoun.
		if (loadPossessive(word, m))
			return;

		word = stripNegativeContraction(word);
		boolean negativeVerb = !word.equals(m.getOriginal());

		word = convertStandardContraction(word);

		IndexWord verb = lookup(POS.VERB, word);

			// Dont check for non-verb roots if we already
			// know its a negative verb.
			if (negativeVerb && verb == null) {
				return;
			}

			IndexWord noun = lookup(POS.NOUN, word);
			IndexWord adj = lookup(POS.ADJECTIVE, word);
			IndexWord adv = lookup(POS.ADVERB, word);

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


		// In case root wasn't in wordnet, add it here.
		if ((negativeVerb) && m.getVerbString() == null) {
			m.putRootNegative("verb", word);
		}
	}

	private String convertStandardContraction(String word)
	{
		String s = standardContractions.get(word);
		if (s!=null)
			return s;
		return word;
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

	protected String maybeChangeFirstLetter(String originalString, String modifiedString) {
		if (Character.isUpperCase(originalString.charAt(0))) {
			if (modifiedString.length() > 1) {
				return Character.toUpperCase(modifiedString.charAt(0)) + modifiedString.substring(1);
			}
			return modifiedString.toUpperCase();
		}
		return modifiedString;
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

	public static void main(String[] args) throws FileNotFoundException, JWNLException
   {
		System.out.println("Initializing MapMorphy...");
		long t = System.currentTimeMillis();
		MapMorphy morphy = new MapMorphy();
		morphy.initialize();
		System.out.println("Elapsed time: "+((System.currentTimeMillis() - t)/1000)+" s");

		String[] test = new String[]{"abaci", "cat", "kills", "Kill", "bill", "slowly", "fast", "Barbra Streisand"};
		for (int i=0; i < test.length; i++){
			Morphed m = morphy.morph(test[i]);
			System.out.println(m);
		}
	}

}
