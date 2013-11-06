/*
 * Copyright 2008 Evgenii Philippov
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

import java.util.*;

/**
 * Separate sentences containing quotes into
 * individual phrases. So, for example,
 *     She yelled, "Go to hell!"
 * will be split up into "She yelled, BLOCK1"
 * and "Go to hell!"
 *
 * XXX This is currently broken as it fails to handle possesives correctly,
 * e.g.
 *    John's sleeve was dirty.
 * breaks up into
 *     John BLOCK
 * and
 *     s sleeve was dirty.
 * This needs to be fixed.
 *
 * This class also doesn't handle non-ASCII UTF8 quotes.
 */
public class QuotesParensSentenceDetector
{
	private final DocSplitter mainSentenceSplitter=
		DocSplitterFactory.create();

	private QuotesParensSentenceDetector(){}

	public static QuotesParensSentenceDetector create()
	{
		return new QuotesParensSentenceDetector();
	}

	/**
	 * Add more text to the buffer.
	 * This allows this class to be used in FIFO mode: text is added with
	 * this call, and sentences are extracted with the getNextSentence() call.
	 */
	public void addText(String newText)
	{
		mainSentenceSplitter.addText(newText);
	}

	private final List<String> subsentences=new LinkedList<String>();

	/**
	 * Clear out the sentence buffer.
	 */
	public void clearBuffer()
	{
		mainSentenceSplitter.clearBuffer();
		subsentences.clear();
	}

	/**
	 * Get the next sentence out of the buffered text.
	 * Return null if there are no complete sentences in the buffer.
	 */
	public String getNextSentence()
	{
		if (subsentences.isEmpty())
		{
			String supersentence = mainSentenceSplitter.getNextSentence();
			if (supersentence == null) return null;
			resetBlockId();
			parse(supersentence);
		}
		if (subsentences.isEmpty())
			return null;
		return subsentences.remove(0);
	}

	private int nextId;
	private int generateBlockId() {return ++nextId;}
	private void resetBlockId() {nextId = 0;}
	private static class QuotedBlock
	{
		private final int blockId;
		private final String rawString;
		private QuotedBlock(int blockId,String rawString)
		{
			this.blockId=blockId;
			this.rawString=rawString;
		}
	}

	public static final String BLOCK = "BLOCK";
	public static final String EMPTY_BLOCK = "EMPTY_BLOCK";
	private void parse(String supersentence)
	{
		List<QuotedBlock> quotedBlocks = new LinkedList<QuotedBlock>();
		int[] quoteIndex = new int[1];
		int[] quoteId = new int[1];
		int[] endQuoteIndex = new int[1];
		while (true)
		{
			if (!findQuote(supersentence, quoteIndex, quoteId))
				break;
			if (!findClosingQuote(supersentence,
					quoteIndex[0]+1, quoteId[0], endQuoteIndex))
				endQuoteIndex[0] = supersentence.length();

			// Found both starting and closing
			if (quoteIndex[0]+1 >= supersentence.length())
			{
				// Dangling opening quote
				supersentence = supersentence.substring(0,supersentence.length()-1);
				break;
			}

			String rawQuotedString = supersentence.substring(quoteIndex[0]+1,endQuoteIndex[0]);
			QuotedBlock qb = new QuotedBlock(generateBlockId(),rawQuotedString);
			quotedBlocks.add(qb);
			supersentence=
				supersentence.substring(0, quoteIndex[0])+' '+
				BLOCK+qb.blockId+
				(endQuoteIndex[0]+1>=supersentence.length()?
						"":' '+supersentence.substring(endQuoteIndex[0]+1));
		}
		supersentence = supersentence.trim();
		if (supersentence.length() == 0) supersentence = EMPTY_BLOCK;
		subsentences.add(supersentence);
		for (QuotedBlock qb:quotedBlocks)
		{
			parse(qb.rawString);
		}
	}

	private static final String QUOTES="\"\"`''()({}{[][";
	private boolean findQuote(
			String s,
			int[] openingQuoteIndex,
			int[] openingQuoteId)
	{
		char[] ca = s.toCharArray();
		for (int i=0; i<ca.length;i++)
		{
			char c = ca[i];
			int pos = QUOTES.indexOf(c);
			if (pos<0) continue;
			// char openingQuote = QUOTES.charAt(pos);
			openingQuoteIndex[0] = i;
			openingQuoteId[0] = pos;
			return true;
		}
		return false;
	}

	private boolean findClosingQuote(
			String s,
			int openingQuoteIndexPlus1,
			int openingQuoteId,
			int[] endQuoteIndex)
	{
		char closingQuote = QUOTES.charAt(openingQuoteId+1);
		int pos=s.substring(openingQuoteIndexPlus1).lastIndexOf(closingQuote);
		if (pos<0)
			return false;
		pos += openingQuoteIndexPlus1;
		endQuoteIndex[0] = pos;
		return true;
	}

	/**
	 * Split a document text string into sentences.
	 * Returns a list of sentence strings.
	 */
	public List<String> split(String docText)
	{
		clearBuffer();
		addText(docText);
		List<String> sentences=new LinkedList<String>();
		while (true)
		{
			String sent = getNextSentence();
			if (sent==null) break;
			sentences.add(sent);
		}
		return sentences;
	}
}
