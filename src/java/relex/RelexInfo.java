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
package relex;

import java.util.ArrayList;

/* Obsolete interface name, do not use; use Sentence instead. */
/*
 * Java, the programing language, is lame. The very existance
 * of the utter stupidity in this file demonstrates one central
 * problem with the lameness of Java langauge design. I really
 * don't understand why so many people think that Java is cool!
 * Why is that?
 */

public class RelexInfo extends Sentence
{
	private static final long serialVersionUID = -2047552550351161106L;
	private Sentence s;
	RelexInfo(Sentence _s)
	{
		s = _s;
	}

	public String getID()
	{
		return s.getID();
	}

	public void setSentence(String snt)
	{
		s.setSentence(snt);
	}
	
	public String getSentence()
	{
		return s.getSentence();
	}
	
	public void setParses (ArrayList<ParsedSentence> pl)
	{
		s.setParses(pl);
	}

	public ArrayList<ParsedSentence> getParses()
	{
		return s.getParses();
	}

	public void setNumParses(int np)
	{
		s.setNumParses(np);
	}

	public int getNumParses()
	{
		return s.getNumParses();
	}

	public String[] getWords()
	{
		return s.getWords();
	}

	public void simpleParseRank()
	{
		s.simpleParseRank();
	}

	public void normalizeParseRank()
	{
		s.normalizeParseRank();
	}

	public String toString()
	{
		return s.toString();
	}
}

