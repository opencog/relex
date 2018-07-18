/*
 * Copyright 2008 Novamente LLC
 * Copyright 2010, 2013 Linas Vepstas <linasvepstas@gmail.com>
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

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;
import relex.utils.ResourceUtils;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * DocSplitterOpenNLP15Impl -- split document into sentences.
 *
 * This is a convenience wrapper around the opennlp-1.5.x toolkit.
 */
// @SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class DocSplitterOpenNLP15Impl implements DocSplitter
{
	private static final int DEBUG = 0;
	private static final String DEFAULT_ENGLISH_DIR =
	        "data/opennlp/models-1.5";
	private static final String DEFAULT_ENGLISH_FILENAME =
	        "en-sent.bin";
	private static final String DEFAULT_ENGLISH_FILEPATH =
			DEFAULT_ENGLISH_DIR + "/" + DEFAULT_ENGLISH_FILENAME;

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

	private static SentenceDetectorME detector;

	// Returned values
	private ArrayList<TextInterval> lst;
	private ArrayList<String> snl;

	// Buffered text, for FIFO mode.
	private String buffer;

	// parameters
	private String englishModelFilename;

	/* --------------------------------------------------------------- */
	public DocSplitterOpenNLP15Impl()
	{
		buffer = null;
		initialize();
	}

	public boolean operational()
	{
		if (detector == null) return false;
		return true;
	}

	private void initialize()
	{
		if (detector == null)
		{
			englishModelFilename = System.getProperty("EnglishModelFilename");
			if (englishModelFilename == null || englishModelFilename.isEmpty())
				englishModelFilename = DEFAULT_ENGLISH_FILEPATH;

			try
			{
				InputStream modelIn = ResourceUtils.getResource(
						"EnglishModelFilename",
						DEFAULT_ENGLISH_FILENAME,
						DEFAULT_ENGLISH_DIR
				);
				SentenceModel model = new SentenceModel(modelIn);
				modelIn.close();
				detector = new SentenceDetectorME(model);
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
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
	private boolean acceptableBreak(String s, int start, int end)
	{
		// If the string ends with "Ms." preceeded by whitespace
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

		// OpenNLP-1.5 will fail on sentences split between multiple
		// lines. Therefore, make sure we actually have punctuation
		// at the end! Otherwise, its probably only half-a-sentence.
		char e = s.charAt(end-1);
		if(e == '.') return true;
		if(e == '?') return true;
		if(e == '!') return true;
		return false;
	}

	/* --------------------------------------------------------------- */
	/**
	 * Add more text to the buffer.
	 * This allows this class to be used in FIFO mode: text is added with
	 * this call, and sentences are extracted with the getNextSentence() call.
	 */
	public void addText(String newText)
	{
		if (null == buffer) buffer = "";
		buffer += newText;
	}

	/**
	 * Clear out the sentence buffer.
	 */
	public void clearBuffer()
	{
		buffer = null;
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
			buffer = null;
			return rc;
		}

		Span spans[] = detector.sentPosDetect(buffer);
		if (0 == spans.length) return null;

		start = 0;
		for (Span span : spans)
		{
			end = span.getEnd();
			if (foundSentence(buffer)) break;
		}
		if (!foundSentence(buffer)) return null;

		buffer = buffer.substring(trimmedEnd);
		return trimmedSentence;
	}

	public String getRemainder()
	{
		String s = buffer;
		buffer = null;
		return s;
	}

	/* --------------------------------------------------------------- */
	private int start;
	private int end;
	private int trimmedStart;
	private int trimmedEnd;
	private String trimmedSentence;

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

		Span spans[] = detector.sentPosDetect(buffer);

		start = 0;
		end = 0;
		for (Span span : spans)
		{
			int sentenceEnd = span.getEnd();
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
