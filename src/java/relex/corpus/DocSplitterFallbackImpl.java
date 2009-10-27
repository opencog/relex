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
 */
package relex.corpus;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of the doc splitter, if OpenNLP is not
 * installed. This uses the standard Java text BreakIteror class
 * to accomplish the sentence splitting.
 */
public class DocSplitterFallbackImpl implements DocSplitter
{
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
	 * Returns false if break is unacceptable. Used to prevent overzelous
	 * sentence detectors which have recognizable idiosyncracies
	 */
	public boolean acceptableBreak(String s, int start, int end)
	{
		return false;
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
			start = end;
		}
		else
		{
			s = buffer.substring(start);
			clearBuffer();
		}

		if (s.equals("")) s = null;
		return s;
	}

	public List<TextInterval> process(String docText)
	{
		// XXX this is wrong, it fails to look for newlines!
		return Arrays.asList(new TextInterval(0,docText.length()-1));
	}

	public List<String> split(String docText)
	{
		// XXX this is wrong, it fails to look for newlines!
		return Arrays.asList(docText);
	}
}
