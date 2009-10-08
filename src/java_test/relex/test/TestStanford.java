/*
 * Copyright 2009 Linas Vepstas
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

package relex.test;

import relex.ParsedSentence;
import relex.RelationExtractor;
import relex.Sentence;
import relex.output.StanfordView;

public class TestStanford
{
	private RelationExtractor re;

	public TestStanford()
	{
		re = new RelationExtractor();
		re.do_stanford = true;
	}
	public boolean test_sentence (String sent, String sf)
	{
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		StanfordView.printRelations(parse);
		return true;
	}
	public static void main(String[] args)
	{

		TestStanford ts = new TestStanford();
		ts.test_sentence ("Who invented sliced bread?",
			"nsubj(invented-2, who-1)\n" +
			"amod(bread-4, sliced-3)\n" + 
			"dobj(invented-2, bread-4)");

		System.out.println("Test passed OK");
	}
}
