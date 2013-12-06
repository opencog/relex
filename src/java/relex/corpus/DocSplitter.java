/*
 * Copyright 2008 Novamente LLC
 * Copyright 2013 Linas Vepstas <linasvepstas@gmail.com>
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
 */
public interface DocSplitter
{
	/**
 	 * Return true if the sentence detector initialized correctly.
 	 * This is used to fallback to simpler detectors if initialization
 	 * failed.
 	 */
	boolean operational();

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
	 * Gets the remainder of any text in the buffer. Empties the buffer.
	 * Returns something, even if it's not a complete sentence.
	 * @return entire contents of buffer.
	 */
	String getRemainder();

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
