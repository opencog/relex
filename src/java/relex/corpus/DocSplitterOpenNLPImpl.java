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

package relex.corpus;

import opennlp.tools.sentdetect.SentenceDetector;

import java.util.ArrayList;
import java.util.HashSet;

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
// @SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class DocSplitterOpenNLPImpl implements DocSplitter
{
	private static final int DEBUG = 0;
	private static final String DEFAULT_ENGLISH_FILENAME =
	        "data/sentence-detector/EnglishSD.bin.gz";

	private static HashSet<String> unacceptableSentenceEnds;

	static
	{
		// The NLP toolkit seems to work fine with Dr., Mrs. etc.
		// but chokes on Ms.
		// Unfortunately, MS can mean "Multiple Sclerosis", and
		// sentences can end with MS.
		unacceptableSentenceEnds = new HashSet<String>();
		unacceptableSentenceEnds.add("Ms.");
		// unacceptableSentenceEnds.add("MS.");
		unacceptableSentenceEnds.add("Mr.");
	}

	private static SentenceDetector detector;

	// Returned values
	private ArrayList<TextInterval> lst;
	private ArrayList<String> snl;

	// Buffered text, for FIFO mode.
	private String buffer;

	// parameters
	private String englishModelFilename;

	/* --------------------------------------------------------------- */
	public DocSplitterOpenNLPImpl()
	{
		buffer = "";
		initialize();
	}

	private void initialize()
	{
		if (detector == null)
		{
			try
			{
				if (englishModelFilename == null)
					englishModelFilename = System.getProperty("EnglishModelFilename");
				if (englishModelFilename == null || englishModelFilename.length() == 0)
					englishModelFilename = DEFAULT_ENGLISH_FILENAME;

				detector = new opennlp.tools.lang.english.SentenceDetector(englishModelFilename);
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
		for (String endString : unacceptableSentenceEnds)
		{
			int len = endString.length();
			if (end >= start + len && 
			    s.substring(end - len, end).equals(endString) &&
			    (end == start + len || Character.isWhitespace(s.charAt(end - len - 1)))) 
			{
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
		return acceptableBreak(docText, trimmedStart, trimmedEnd);
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
		for (int sentenceEnd : sentenceEnds)
		{
			int prevstart = start;
			start = end; // from previous loop iteration
			end = sentenceEnd;

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
