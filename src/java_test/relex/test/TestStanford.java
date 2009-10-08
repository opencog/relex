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

import java.util.ArrayList;
import java.util.Collections;

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

	public ArrayList<String> split(String a)
	{
		String[] sa = a.split("\n");
		ArrayList<String> saa = new ArrayList<String>();
		for (String s : sa)
		{
			saa.add(s);
		}
		Collections.sort (saa);
		return saa;
	}

	public boolean test_sentence (String sent, String sf)
	{
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse);

		ArrayList<String> sfa = split(sf);
		ArrayList<String> rsa = split(rs);
		for (int i=0; i< sfa.size(); i++)
		{
			if (!sfa.get(i).equals (rsa.get(i)))
			{
				System.err.println("Error: miscompare:\n" +
					"\tStanford = " + sfa + "\n" +
					"\tRelEx    = " + rsa );
				return false;
			}
		}

		return true;
	}
	public static void main(String[] args)
	{
		TestStanford ts = new TestStanford();
		boolean rc = true;

		rc &= ts.test_sentence ("Who invented sliced bread?",
			"nsubj(invented-2, who-1)\n" +
			"amod(bread-4, sliced-3)\n" + 
			"dobj(invented-2, bread-4)");

		rc &= ts.test_sentence ("Jim runs quickly.",
			"nsubj(runs-2, Jim-1)\n" +
			"advmod(runs-2, quickly-3)");

		rc &= ts.test_sentence ("The bird, a robin, sang sweetly.",
			"det(bird-2, the-1)\n" +
			"nsubj(sang-7, bird-2)\n" +
			"det(robin-5, a-4)\n" +
			"appos(bird-2, robin-5)\n" +
			"advmod(sang-7, sweetly-8)");

		rc &= ts.test_sentence ("There is a place we can go.",
			"expl(is-2, there-1)\n" +
			"det(place-4, a-3)\n" +
			"nsubj(is-2, place-4)\n" +
			"nsubj(go-7, we-5)\n" +
			"aux(go-7, can-6)\n" +
			"dep(is-2, go-7)");


		if (rc)
		{
			System.err.println("Test passed OK");
		}
		else
		{
			System.err.println("Test failed");
		}
	}
}
