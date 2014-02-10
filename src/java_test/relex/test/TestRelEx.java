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
              rc &= ts.test_sentence ("The woman who lives next door is a registered nurse.",
			"_obj(be, nurse)\n" +
    			"_subj(be, woman)\n" +
    			"_amod(nurse, registered)\n" +
    			"_advmod(live, next_door)\n" +
   			"_subj(live, woman)\n" +
    			"who(woman, live)\n");
   	      rc &= ts.test_sentence ("A player who is injured has to leave the field.",
			"_to-do(have, leave)\n" +
    			"_subj(have, player)\n" +
    			"_obj(leave, field)\n" +
    			"_predadj(player, injured)\n" +
   			"who(player, injured)\n" ); 
 	     rc &= ts.test_sentence ("Pizza, which most people love, is not very healthy.",
			"_advmod(very, not)\n" +
    			"_advmod(healthy, very)\n" +
    			"_obj(love, Pizza)\n" +
    			"_quantity(people, most)\n" +
			"which(Pizza, love)\n" +
                        "_subj(love, people)\n" +
   			"_predadj(Pizza, healthy)\n" );   
	    rc &= ts.test_sentence ("The restaurant  which belongs to my aunt is very famous.",
    			"_advmod(famous, very)\n" +
    			"to(belong, aunt)\n" +
    			"_subj(belong, restaurant)\n" +
    			"_poss(aunt, me)\n" +
    			"which(restaurant, belong)\n" +
    			"_predadj(restaurant, famous)\n");
	     rc &= ts.test_sentence ("I like the trees which have coffee beans.",
   			"_obj(like, tree)\n" +
   			"_subj(like, I)\n" +
    			"_obj(have, bean)\n" +
    			"_subj(have, tree)\n" +
   			"_nn(bean, coffee)\n" +
 			"which(tree, have)\n");
	     rc &= ts.test_sentence ("The books which I read in the library were written by Charles Dickens.",     
   			 "_obj(write, book)\n" +
    			 "by(write, Charles_Dickens)\n" +
   			 "_obj(read, book)\n" +
   			 "in(read, library)\n" +
    			 "_subj(read, I)\n" +
   			 "which(book, read)\n");
            rc &= ts.test_sentence(" Anyone who is afraid of doing too much will end up doing too little.",
   			"_to-do(end_up, do)\n" +
    			"_obj(do, much)\n" +
    			"_subj(do, of)\n" +
    			"_advmod(much, too)\n" +
    			"_predadj(anyone, afraid)\n" +
    			"who(anyone, afraid)\n" +
    			"_advmod(little, too)\n" +
    			"_obj(do, little)\n" +
    			"_subj(do, anyone)\n");

	   rc &= ts.test_sentence("The woman who hosted the party is my cousin.",
   			"_obj(be, cousin)\n" +
    			"_subj(be, woman)\n" +
   			"_poss(cousin, me)\n" +
    			"_obj(host, party)\n" +
    			"_subj(host, woman)\n" +
    			"who(woman, host)\n");
	   rc &= ts.test_sentence("Yesterday we were visited by a man who wanted to repair our coffee machine.",
   			"_obj(visit, we)\n" +
    			"by(visit, man)\n" +
    			"_advmod(visit, yesterday)\n" +
    			"_nn(machine, coffee)\n" +
    			"_poss(machine, us)\n" +
    			"_obj(repair, machine)\n" +
    			"_to-do(want, repair)\n" +
    			"_subj(want, man)\n" +
    			"who(man, want)\n");
 	  rc &= ts.test_sentence("The gentleman who is speaking German is my Uncle.",
    			"_obj(be, Uncle)\n" +
    			"_subj(be, gentleman)\n" +
    			"_poss(Uncle, me)\n" +
   			"_obj(speak, German)\n" +
    			"_subj(speak, gentleman)\n" +
   			"who(gentleman, speak)\n");
	  rc &= ts.test_sentence("Mr. Emric is the man who owns the School.",
    			"_obj(be, man)\n" +
    			"_subj(be, Mr._Emric)\n" +
   			"_obj(own, School)\n" +
    			"_subj(own, man)\n" +
    			"who(man, own)\n");
	 rc &= ts.test_sentence("The woman who hosted the party is my cousin",
  			"_obj(be, cousin)\n" +
    			"_subj(be, woman)\n" +
   			"_poss(cousin, me)\n" +
    			"_obj(host, party)\n" +
    			"_subj(host, woman)\n" +
   			"who(woman, host)\n");
	rc &= ts.test_sentence("They passed some shops whose windows were decorated for Independence’s day.",
			"_obj(pass, shop)\n" +
    			"_subj(pass, they)\n" +
    			"for(decorate, day)\n" +
    			"_obj(decorate, windows)\n" +
    			"_poss(day, Independence)\n" +
    			"whose(shop, windows)\n");
	rc &= ts.test_sentence("This is the book  whose author I met in a library.",
     			"_obj(be, book)\n" +
    			"_subj(be, this)\n" +
    			"_obj(meet, author)\n" +
    			"in(meet, library)\n" +
    			"_subj(meet, I)\n" +
    			"whose(book, author)\n");
       rc &= ts.test_sentence("The girl whose boyfriend works as a teacher is a great author.",
     			"_obj(be, author)\n" +
    			"_subj(be, girl)\n" +
    			"_amod(author, great)\n" +
    			"as(work, teacher)\n" +
    			"_subj(work, boyfriend)\n" +
   			 "whose(girl, boyfriend)\n");
      rc &= ts.test_sentence("The book that she lent me is very boring.",
   			"_advmod(boring, very)\n" +
    			"_iobj(lend, book)\n" +
    			"_obj(lend, me)\n" +
    			"_subj(lend, she)\n" +
    			"_which(book, lend)\n" +
    			"_predadj(book, boring)\n");
      rc &= ts.test_sentence("Over there is the school that I attended",
   			"_obj(attend, school)\n" +
    			"_subj(attend, I)\n" +
    			"_which(school, attend)\n" +
   			 "_subj(be, school)\n");
     rc &= ts.test_sentence("They ate a special curry which was recommended by the restaurant’s owner.",
       			"_obj(eat, curry)\n" +
    			"_subj(eat, they)\n" +
   			 "_obj(recommend, curry)\n" +
    			"by(recommend, owner)\n" +
   			"_poss(owner, restaurant)\n" +
   			"which(curry, recommend)\n" +
    			"_amod(curry, special)\n"); 
    
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
