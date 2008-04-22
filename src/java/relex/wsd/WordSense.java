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

package relex.wsd;

import relex.RelationExtractor;
import relex.RelexInfo;
import relex.stats.SimpleTruthValue;

/**
 * Experimental word-sense disambiguation code.
 */
public class WordSense
{
	public SimpleTruthValue wordSenseMatch(String word,
	                                       RelexInfo example,
	                                       RelexInfo target)
	{
		SimpleTruthValue stv = new SimpleTruthValue();
System.out.println("hello world");

		return stv;
	}

	/**
	 * Test/sample usage
	 */

	public static void main(String[] args)
	{
		RelationExtractor re = new RelationExtractor(false);

		String example_sentence = "I was fishing for an answer";
		String target_sentence = "We went on a fishing expedition.";

		RelexInfo ri_example = re.processSentence(example_sentence);
		RelexInfo ri_target = re.processSentence(target_sentence);

		WordSense ws = new WordSense();
		ws.wordSenseMatch("fishing", ri_example, ri_target);
	}

} // end WordSense

