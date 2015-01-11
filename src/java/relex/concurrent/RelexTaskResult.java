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

package relex.concurrent;

import java.io.Serializable;

import relex.Sentence;

public class RelexTaskResult implements Comparable<RelexTaskResult>, Serializable
{
	private static final long serialVersionUID = -3231030217056826602L;

	public Integer index;
	public String sentence;
	public Sentence result;

	public RelexTaskResult(int index, String sentence,
	                       Sentence sntc)
	{
		this.index = index;
		this.sentence = sentence;
		this.result = sntc;
	}

	public int compareTo(RelexTaskResult that)
	{
		return this.index.compareTo(that.index);
	}

	public String toString()
	{
		return index+": "+sentence+"\n"+result+"\n";
	}

	public int hashCode()
	{
		return sentence == null ? 0 : sentence.hashCode();
	}

	/**
	 * Very superficial compare, doesn't compare actual parses, just the sentence
	 * string.
	 */
	public boolean equals(Object other)
	{
		if (! (other instanceof RelexTaskResult))
			return false;
		RelexTaskResult x = (RelexTaskResult)other;
		if (sentence == null)
			return x.sentence == null;
		else if (!this.sentence.equals(x.sentence))
			return false;
		else
			return true;
	}
}
