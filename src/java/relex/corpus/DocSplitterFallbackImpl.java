/*
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
 *
 * Copyright (c) 2010, 2013 Linas Vepstas <linasvepstas@gmail.com>
 */
package relex.corpus;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of the doc splitter, if OpenNLP is not
 * installed. This uses the standard Java text BreakIteror class
 * to accomplish the sentence splitting.
 */
public class DocSplitterFallbackImpl implements DocSplitter
{
	// Buffered text, for FIFO mode.
	private String buffer;
	private BreakIterator bdry;
	private int start;

	public DocSplitterFallbackImpl()
	{
		buffer = "";
		bdry = BreakIterator.getSentenceInstance(Locale.US);
		bdry.setText("");
		start = bdry.first();
	}

	public boolean operational()
	{
		return true;
	}

	/**
	 * Add more text to the buffer.
	 * This allows this class to be used in FIFO mode: text is added with
	 * this call, and sentences are extracted with the getNextSentence() call.
	 */
	public void addText(String newText)
	{
		// First, trim off up to old start.
		buffer = buffer.substring(start);
		buffer += newText;
		bdry.setText(buffer);
		start = bdry.first();
	}

	public void clearBuffer()
	{
		buffer = "";
		bdry.setText(buffer);
		start = bdry.first();
	}

	public String getNextSentence()
	{
		int end = bdry.next();
		String s = "";
		if (BreakIterator.DONE != end)
		{
			s = buffer.substring(start,end);
		}
		else
		{
			s = buffer.substring(start);
		}

		s = s.trim();
		int len = s.length();
		if (len < 1)
		{
			clearBuffer();
			return null;
		}

		// Verify that we've got a complete sentence. If so, return it,
		// else return null, and wait for more input.
		char c = s.charAt(len-1);
		if ('.' == c || '!' == c || '?' == c)
		{
			start = end;
			if (BreakIterator.DONE == end) clearBuffer();
			return s;
		}
		return null;
	}

	public String getRemainder()
	{
		// Trim away previously issued text.
		String s = buffer.substring(start);
		s = s.trim();
		if (s == "") s = null;

		// Reset the buffer state.
		clearBuffer();
		return s;
	}

	/* --------------------------------------------------------------- */
	// Bulk mode returned values
	private ArrayList<TextInterval> lst;
	private ArrayList<String> snl;

	/**
	 * Split a document text string into sentences.
	 * Returns a list of sentence start and end-points.
	 */
	public List<TextInterval> process(String docText)
	{
		_process(docText);
		return lst;
	}

	/**
	 * Split a document text string into sentences.
	 * Returns a list of sentence strings.
	 */
	public List<String> split(String docText)
	{
		_process(docText);
		return snl;
	}

	private void _process(String docText)
	{
		lst = new ArrayList<TextInterval>();
		snl = new ArrayList<String>();
		if (docText == null) return;

		bdry.setText(docText);
		start = bdry.first();
		int end = bdry.next();
		while (BreakIterator.DONE != end)
		{
			lst.add(new TextInterval(start, end));
			snl.add(docText.substring(start, end));
			start = end;
			end = bdry.next();
		}
	}
}
