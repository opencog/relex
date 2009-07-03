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

import java.util.Arrays;
import java.util.List;

public class DocSplitterFallbackImpl implements DocSplitter
{
	public boolean operational()
	{
		return true;
	}
	public boolean acceptableBreak(String s, int start, int end)
	{
		return false;
	}

	private StringBuilder sb = new StringBuilder();

	public void addText(String newText)
	{
		sb.append(newText);
	}

	public void clearBuffer()
	{
		sb.setLength(0);
	}

	public String getNextSentence()
	{
		String s = "";
		int nl = sb.indexOf("\n");
		if (0 < nl)
		{
			s = sb.substring(0,nl);
			sb = sb.delete(0,nl);
		}
		else
		{
			s = sb.toString();
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
