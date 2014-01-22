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
import relex.output.SimpleView;

public class TestRelEx
{
	private RelationExtractor re;
	private int pass;
	private int fail;

	public TestRelEx()
	{
		re = new RelationExtractor();
		pass = 0;
		fail = 0;
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

	/**
	 * First argument is the sentence.
	 * Second argument is a list of the relations that the 
	 * Stanford parser generates. 
	 * Return true if relex generates that same dependencies
	 * as the second argument.
	 */
	public boolean test_sentence (String sent, String sf)
	{
		re.do_penn_tagging = false;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = SimpleView.printBinaryRelations(parse);

		ArrayList<String> exp = split(sf);
		ArrayList<String> got = split(rs);
		if (exp.size() != got.size())
		{
			System.err.println("Error: size miscompare:\n" +
				"\tExpected = " + exp + "\n" +
				"\tGot      = " + got + "\n" +
				"\tSentence = " + sent);
			fail ++;
			return false;
		}
		for (int i=0; i< exp.size(); i++)
		{
			if (!exp.get(i).equals (got.get(i)))
			{
				System.err.println("Error: content miscompare:\n" +
					"\tExpected = " + exp + "\n" +
					"\tGot      = " + got + "\n" +
					"\tSentence = " + sent);
				fail ++;
				return false;
			}
		}

		pass ++;
		return true;
	}


	public static void main(String[] args)
	{
		TestRelEx ts = new TestRelEx();
		boolean rc = true;

		rc &= ts.test_sentence ("Some people like pigs less than dogs.",
			"_advmod(like, less)\n" +
			"_obj(like, pig)\n" +
			"_quantity(people, some)\n" +
			"_subj(like, people)\n" +
			"than(pig, dog)\n");

		rc &= ts.test_sentence ("Some people like pigs more than dogs.",
			"_advmod(like, more)\n" +
			"_obj(like, pig)\n" +
			"_quantity(people, some)\n" +
			"_subj(like, people)\n" +
			"than(pig, dog)\n");
		rc &= ts.test_sentence ("I like the man who gave me chocolate.",
   			"_obj(like, man)\n" +
   			"_subj(like, I)\n" +
   			"_iobj(give, me)\n" +
    			"_obj(give, chocolate)\n" +
    			"_subj(give, man)\n" +
    			"who(man, give)\n");


		if (rc)
		{
			System.err.println("Tested " + ts.pass + " sentences, test passed OK");
		}
		else
		{
			System.err.println("Test failed\n\t" + 
				ts.fail + " sentences failed\n\t" +
				ts.pass + " sentences passed");
		}
	}
}
