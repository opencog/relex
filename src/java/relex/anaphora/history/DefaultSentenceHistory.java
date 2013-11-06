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
package relex.anaphora.history;

import java.util.ArrayList;
import relex.Sentence;

/**
 *
 * Sentence history that keeps just a max number of sentences
 * in the buffer.
 *
 * @author fabricio <fabricio@vettalabs.com>
 *
 */
public class DefaultSentenceHistory implements SentenceHistory
{

	ArrayList<Sentence> sentences=null;
	// The max number of sentences to keep in the history buffer.
	private static int max_sentences = 20;

	public DefaultSentenceHistory()
	{
		sentences = new ArrayList<Sentence>();
	}

	public void addSentence(Sentence sentence)
	{
		sentences.add(0, sentence);
		int size = sentences.size();
		if (size > max_sentences) sentences.remove(max_sentences);
	}

	public ArrayList<Sentence> getSentenceList()
	{
		return sentences;
	}
}
