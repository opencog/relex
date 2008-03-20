package relex.corpus;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.ClassLoader;

import opennlp.tools.sentdetect.EnglishSentenceDetectorME;
import opennlp.tools.sentdetect.SentenceDetector;

/**
 * DocSplitter -- split document into sentences.
 * Sentences are blocks of text ending with a period, exclamation
 * mark, or question mark. Unpunctuated blocks of text are not
 * recognized as sentences.
 *
 * This class can operate in one of two modes: batch mode and fifo mode.
 *
 * In batch mode, a large block of text is submitted, and the
 * process() or split() methods are used to get arrays of sentences.
 *
 * In fifo mode, it can be fed anything from large blocks of text
 * to sentence fragments or even a few letters at a time, via the
 * addText() method. It can then be polled to see if there is a
 * complete sentence yet, by calling getNextSentence().  Calls
 * to each of these routines can be arbitrarily alternated, with
 * sentence fragments buffered up.
 *
 * This is a convenience wrapper around the opennlp toolkit;
 * it fixes some bugs encountered there, and provides a more
 * convenient interface.
 *
 * Refactored, expanded version of ord.disco.taca.SentenceAnnotator
 *
 * XXX known bugs:
 * fails to recognize exclamation mark as end of sentence, unless
 * it is preceeded by white space.
 */
public class DocSplitter
{
	public static final int DEBUG = 0;
	public static final String DEFAULT_ENGLISH_FILENAME =
	        "./data/sentence-detector/EnglishSD.bin.gz";

	private static HashSet<String> capitalizedUnacceptableSentenceEnds;

	static
	{
		// The NLP toolkit seems to work fine with Dr., Mrs. etc.
		// but chokes on Ms.
		capitalizedUnacceptableSentenceEnds = new HashSet<String>();
		capitalizedUnacceptableSentenceEnds.add("MS.");
		capitalizedUnacceptableSentenceEnds.add("MR.");
	}

	private static SentenceDetector detector = null;

	// Returned values
	private ArrayList<TextInterval> lst = null;
	private ArrayList<String> snl = null;

	// Buffered text, for FIFO mode.
	private String buffer;

	// parameters
	private String englishModelFilename = null;

	/* --------------------------------------------------------------- */
	// private static intializer -- Manually verify the user's installation.
	static {
		// XXX Strange ... this sometimes works, and sometimes doesn't ...
		try
		{
			Class.forName("opennlp.tools.sentdetect.EnglishSentenceDetectorME");
		}
		catch(Throwable t)
		{
			System.out.println(
				"\nWARNING:\n" +
				"\tIt appears the the OpenNLP tools are not installed\n" +
				"\tor are not correctly specified in the java classpath.\n" +
				"\tThe OpenNLP tools are used to perform sentence detection,\n" +
				"\tand RelEx will have trouble handling multiple sentences.\n" +
				"\tPlease see the README file for install info.\n");
		}
	}

	/* --------------------------------------------------------------- */
	public DocSplitter()
	{
		buffer = "";
		initialize();
	}

	private void initialize()
	{
		if (detector == null)
		{
			// Manually verify the user's installation.
			// XXX Strange ... this sometimes works, and sometimes doesn't ...
			try
			{
				ClassLoader ld = Thread.currentThread().getContextClassLoader();
				ld.loadClass("opennlp.tools.sentdetect.EnglishSentenceDetectorME");
			}
			catch(Throwable t)
			{
				System.out.println(
					"\nWARNING:\n" +
					"\tIt appears the the OpenNLP tools are not installed\n" +
					"\tor are not correctly specified in the java classpath.\n" +
					"\tThe OpenNLP tools are used to perform sentence detection,\n" +
					"\tand RelEx will have trouble handling multiple sentences.\n" +
					"\tPlease see the README file for install info.\n");
				return;
			}
			try
			{
				if (englishModelFilename == null)
					englishModelFilename = System.getProperty("EnglishModelFilename");
				if ((englishModelFilename == null) || (englishModelFilename.length() == 0))
					englishModelFilename = DEFAULT_ENGLISH_FILENAME;

				detector = new EnglishSentenceDetectorME(englishModelFilename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/* --------------------------------------------------------------- */
	public void setEnglishModelFilename(String emf)
	{
		englishModelFilename = emf;
	}

	public String getEnglishModelFilename()
	{
		return englishModelFilename;
	}

	/* --------------------------------------------------------------- */
	/**
	 * Returns false if break is unacceptable. Used to prevent overzelous
	 * sentence detectors which have recognizable idiosyncracies
	 */
	public boolean acceptableBreak(String s, int start, int end)
	{
		// if the string ends with "Ms." preceeded by whitespace
		Iterator<String> i = capitalizedUnacceptableSentenceEnds.iterator();
		while (i.hasNext()) {
			String endString = i.next();
			int len = endString.length();
			if (end >= start + len && s.substring(end - len, end).toUpperCase().equals(endString)
					&& (end == start + len || Character.isWhitespace(s.charAt(end - len - 1)))) {
				return false;
			}
		}
		return true;
	}

	/* --------------------------------------------------------------- */
	/**
	 * Add more text to the buffer.
	 * This allows this class to be used in FIFO mode: text is added with
	 * this call, and sentences are extracted with the getNextSentence() call.
	 */
	public void addText(String newText)
	{
		buffer += newText;
	}

	/**
	 * Clear out the sentence buffer.
	 */
	public void clearBuffer()
	{
		buffer = "";
	}

	/**
	 * Get the next sentence out of the buffered text.
	 * Return null if there are no complete sentences in the buffer.
	 */
	public String getNextSentence()
	{
		// punt if no sentence detector
		if (detector == null)
		{
			String rc = buffer;
			buffer = "";
			return rc;
		}

		int[] sentenceEnds = detector.sentPosDetect(buffer);
		if (0 == sentenceEnds.length) return null;

		start = 0;
		end = sentenceEnds[0];
		if (!foundSentence(buffer)) return null;

		buffer = buffer.substring(trimmedEnd);
		return trimmedSentence;
	}

	/* --------------------------------------------------------------- */
	int start;
	int end;
	int trimmedStart;
	int trimmedEnd;
	String trimmedSentence;

	private Boolean foundSentence(String docText)
	{
		trimmedSentence = docText.substring(start, end).trim();
		if (trimmedSentence == null || trimmedSentence.length() == 0)
			return false;

		trimmedStart = docText.indexOf(trimmedSentence.charAt(0), start);
		trimmedEnd = trimmedStart + trimmedSentence.length();
		if (!acceptableBreak(docText, trimmedStart, trimmedEnd))
			return false;
		return true;
	}

	/* --------------------------------------------------------------- */
	/**
	 * Split a document text string into sentences.
	 * Returns a list of sentence start and end-points.
	 */
	public ArrayList<TextInterval> process(String docText)
	{
		_process(docText);
		return lst;
	}

	/**
	 * Split a document text string into sentences.
	 * Returns a list of sentence strings.
	 */
	public ArrayList<String> split(String docText)
	{
		_process(docText);
		return snl;
	}

	private void _process(String docText)
	{
		lst = new ArrayList<TextInterval>();
		snl = new ArrayList<String>();
		if (docText == null) return;

		int[] sentenceEnds = detector.sentPosDetect(docText);

		start = 0;
		end = 0;
		for (int i = 0; i < sentenceEnds.length; i++) {
			int prevstart = start;
			start = end; // from previous loop iteration
			end = sentenceEnds[i];

			if (!foundSentence(docText))
			{
				// go back to previous start
				start = prevstart;
				end = prevstart;
				continue;
			}

			if (DEBUG > 0) System.out.println(start + "," + end + ": " + trimmedSentence);
			lst.add(new TextInterval(trimmedStart, trimmedEnd));
			snl.add(trimmedSentence);
		}
	}
}
