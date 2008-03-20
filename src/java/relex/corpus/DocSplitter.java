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

import java.util.List;

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
public interface DocSplitter
{
	String DEFAULT_ENGLISH_FILENAME =
	        "data/sentence-detector/EnglishSD.bin.gz";

  /**
   * Sets up
   * @param emf for example, data/sentence-detector/EnglishSD.bin.gz
   * @see #DEFAULT_ENGLISH_FILENAME
   */
  void setEnglishModelFilename(String emf);

  /**
   * Gets the setting
   * @return file path
   */
  String getEnglishModelFilename();

	/**
	 * Used to prevent overzelous sentence detectors which have recognizable idiosyncracies
   * @param s ...
   * @param start ...
   * @param end ...
   * @return false if break is unacceptable
   */
  boolean acceptableBreak(String s, int start, int end);

	/**
	 * Adds more text to the buffer.
	 * This allows this class to be used in FIFO mode: text is added with
	 * this call, and sentences are extracted with the getNextSentence() call.
   * @param newText text to be added
   */
  void addText(String newText);

	/**
	 * Clears out the sentence buffer.
	 */
  void clearBuffer();

	/**
	 * Gets the next sentence out of the buffered text.
	 * @return null if there are no complete sentences in the buffer.
	 */
  String getNextSentence();

	/**
	 * Splits a document text string into sentences.
   * @param docText text to be split
   * @return a list of sentence start and end-points
   */
  List<TextInterval> process(String docText);

	/**
	 * Splits a document text string into sentences.
	 * @param docText text to be split
   * @return a list of sentence strings.
	 */
  List<String> split(String docText);
}
