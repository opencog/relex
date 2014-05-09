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
	private int subpass;
	private int subfail;

	public TestRelEx()
	{
		re = new RelationExtractor();
		pass = 0;
		fail = 0;
		subpass = 0;
		subfail = 0;
	}

	public ArrayList<String> split(String a)
	{
		String[] sa = a.split("\n");
		ArrayList<String> saa = new ArrayList<String>();
		for (String s : sa) {
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
			subfail ++;
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
				subfail ++;
				fail ++;
				return false;
			}
		}

		subpass ++;
		pass ++;
		return true;
	}

	public void report(boolean rc, String subsys)
	{
		if (rc) {
			System.err.println(subsys + ": Tested " + pass + " sentences, test passed OK");
		} else {
			System.err.println(subsys + ": Test failed\n\t" +
			                   fail + " sentences failed\n\t" +
			                   pass + " sentences passed");
		}
		subpass = 0;
		subfail = 0;
	}

	public boolean test_comparatives()
	{
		boolean rc = true;
		rc &= test_sentence ("Some people like pigs less than dogs.",
		                     "_advmod(like, less)\n" +
		                     "_obj(like, pig)\n" +
		                     "_quantity(people, some)\n" +
		                     "_subj(like, people)\n" +
		                     "than(pig, dog)\n");

		rc &= test_sentence ("Some people like pigs more than dogs.",
		                     "_advmod(like, more)\n" +
		                     "_obj(like, pig)\n" +
		                     "_quantity(people, some)\n" +
		                     "_subj(like, people)\n" +
		                     "than(pig, dog)\n");
		report(rc, "Comparatives");
		return rc;
	}

	public boolean test_extraposition()
	{
		boolean rc = true;
		rc &= test_sentence ("The woman who lives next door is a registered nurse.",
		                        "_obj(be, nurse)\n" +
		                        "_subj(be, woman)\n" +
		                        "_amod(nurse, registered)\n" +
		                        "_advmod(live, next_door)\n" +
		                        "_subj(live, woman)\n" +
		                        "who(woman, live)\n");

		rc &= test_sentence ("A player who is injured has to leave the field.",
		                        "_to-do(have, leave)\n" +
		                        "_subj(have, player)\n" +
		                        "_obj(leave, field)\n" +
		                        "_predadj(player, injured)\n" +
		                        "who(player, injured)\n" );

		rc &= test_sentence ("Pizza, which most people love, is not very healthy.",
		                        "_advmod(very, not)\n" +
		                        "_advmod(healthy, very)\n" +
		                        "_obj(love, pizza)\n" +
		                        "_quantity(people, most)\n" +
		                        "which(pizza, love)\n" +
		                        "_subj(love, people)\n" +
		                        "_predadj(pizza, healthy)\n" );

		rc &= test_sentence ("The restaurant which belongs to my aunt is very famous.",
		                        "_advmod(famous, very)\n" +
		                        "to(belong, aunt)\n" +
		                        "_subj(belong, restaurant)\n" +
		                        "_poss(aunt, me)\n" +
		                        "which(restaurant, belong)\n" +
		                        "_predadj(restaurant, famous)\n");

		rc &= test_sentence ("The books which I read in the library were written by Charles Dickens.",
		                        "_obj(write, book)\n" +
		                        "by(write, Charles_Dickens)\n" +
		                        "_obj(read, book)\n" +
		                        "in(read, library)\n" +
		                        "_subj(read, I)\n" +
		                        "which(book, read)\n");

		rc &= test_sentence("This is the book whose author I met in a library.",
		                       "_obj(be, book)\n" +
		                       "_subj(be, this)\n" +
		                       "_obj(meet, author)\n" +
		                       "in(meet, library)\n" +
		                       "_subj(meet, I)\n" +
		                       "whose(book, author)\n");

		rc &= test_sentence("The book that Jack lent me is very boring.",
		                       "_advmod(boring, very)\n" +
		                       "_iobj(lend, book)\n" +
		                       "_obj(lend, me)\n" +
		                       "_subj(lend, Jack)\n" +
		                       "that_adj(book, lend)\n" +
		                       "_predadj(book, boring)\n");

		rc &= test_sentence("They ate a special curry which was recommended by the restaurant’s owner.",
		                       "_obj(eat, curry)\n" +
		                       "_subj(eat, they)\n" +
		                       "_obj(recommend, curry)\n" +
		                       "by(recommend, owner)\n" +
		                       "_poss(owner, restaurant)\n" +
		                       "which(curry, recommend)\n" +
		                       "_amod(curry, special)\n");

		rc &= test_sentence("The dog who Jack said chased me was black.",
		                       "_obj(chase, me)\n" +
		                       "_subj(chase, dog)\n" +
		                       "_subj(say, Jack)\n" +
		                       "_predadj(dog, black)\n" +
		                       "who(dog, chase)\n");

		rc &= test_sentence("Jack, who hosted the party, is my cousin.",
		                       "_obj(be, cousin)\n" +
		                       "_subj(be, Jack)\n" +
		                       "_poss(cousin, me)\n" +
		                       "_obj(host, party)\n" +
		                       "_subj(host, Jack)\n" +
		                       "who(Jack, host)\n");

		rc &= test_sentence("Jack, whose name is in that book, is the student near the window.",
		                       "near(be, window)\n" +
		                       "_obj(be, student)\n" +
		                       "_subj(be, Jack)\n" +
		                       "_obj(near, window)\n" +
		                       "_pobj(in, book)\n" +
		                       "_psubj(in, name)\n" +
		                       "_det(book, that)\n" +
		                       "whose(Jack, name)\n");

		rc &= test_sentence("Jack stopped the police car that was driving fast.",
		                       "_obj(stop, car)\n" +
		                       "_subj(stop, Jack)\n" +
		                       "_advmod(drive, fast)\n" +
		                       "_subj(drive, car)\n" +
		                       "that_adj(car, drive)\n" +
		                       "_nn(car, police)\n");

		rc &= test_sentence("Just before the crossroads, the car was stopped by a traffic sign that stood on the street.",
		                       "_obj(stop, car)\n" +
		                       "by(stop, sign)\n" +
		                       "_advmod(stop, just)\n" +
		                       "on(stand, street)\n" +
		                       "_subj(stand, sign)\n" +
		                       "that_adj(sign, stand)\n" +
		                       "_nn(sign, traffic)\n" +
		                       "before(just, crossroads)\n");

		report(rc, "Extrapostion");
		return rc;
	}


	public static void main(String[] args)
	{
		TestRelEx ts = new TestRelEx();
		boolean rc = true;

		rc &= ts.test_comparatives();
		rc &= ts.test_extraposition();

		if (rc) {
			System.err.println("Tested " + ts.pass + " sentences, test passed OK");
		} else {
			System.err.println("Test failed\n\t" +
			                   ts.fail + " sentences failed\n\t" +
			                   ts.pass + " sentences passed");
		}
	}
}
