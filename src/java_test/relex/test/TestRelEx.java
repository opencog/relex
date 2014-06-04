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
	private static ArrayList<String> sentfail= new ArrayList<String>();

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
	 * Second argument is a list of the relations that RelEx
	 * should be generating.
	 * Return true if RelEx generates the same dependencies
	 * as the second argument.
	 */
	public boolean test_sentence (String sent, String sf)
	{
		re.do_penn_tagging = false;
		re.setMaxParses(1);
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = SimpleView.printBinaryRelations(parse);
		String urs = SimpleView.printUnaryRelations(parse);

		ArrayList<String> exp = split(sf);
		ArrayList<String> brgot = split(rs);
		ArrayList<String> urgot = split(urs);
		
		//add number of binary relations from parser-output, to total number of relationships got
		int sizeOfGotRelations= brgot.size();
		//check expected binary and unary relations
		//the below for-loop checks whether all expected binary relations are
		//contained in the parser-binary-relation-output arrayList "brgot".
		//if any unary relations are expected in the output it checks the 
		//parser-unary-relation-output arrayList "urgot" for unary relationships
		for (int i=0; i< exp.size(); i++)
		{	
			if(!brgot.contains((String)exp.get(i)))
			{
				if(!urgot.contains(exp.get(i)))
				{
					System.err.println("Error: content miscompare:\n" +
						    "\tExpected = " + exp + "\n" +
						    "\tGot Binary Relations = " + brgot + "\n" +
						    "\tGot Unary Relations = " + urgot + "\n" +
						    "\tSentence = " + sent);
					subfail ++;
					fail ++;
					sentfail.add(sent);
					return false;
				}
				//add the unary relation, count to totoal number of binary relations
				sizeOfGotRelations++;
			}
			
		}
		//The size checking of the expected relationships vs output relationships
		//is done here purposefully, to accommodate if there is any unary relationships present 
		//in the expected output(see above for-loop also).
		//However it only checks whether parser-output resulted more relationships(binary+unary) than expected relations
		//If the parser-output resulted less relationships(binary+unary) than expected it would 
		//catch that in the above for-loop
		if (exp.size() < sizeOfGotRelations)
		{
			System.err.println("Error: size miscompare:\n" +
					    "\tExpected = " + exp + "\n" +
					    "\tGot Binary Relations = " + brgot + "\n" +
					    "\tGot Unary Relations = " + urgot + "\n" +
					    "\tSentence = " + sent);
			subfail ++;
			fail ++;
			sentfail.add(sent);
			return false;
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
		//Non-equal Gradable : Two entities one feature "more/less"	
		
		rc &= test_sentence ("He is more intelligent than John.",
				    "than(he, John)\n" +
				    "more(intelligent, he)\n" +
				    "degree(intelligent, comparative)\n"+
				    "_predadj(he, intelligent)\n");
		
		rc &= test_sentence ("He is less intelligent than John.",
				    "than(he, John)\n" +
				    "_more(intelligent, he)\n" +
				    "degree(intelligent, comparative)\n"+
				    "_advmod(intelligent, less)\n"+
				    "_predadj(he, intelligent)\n");
				
		rc &= test_sentence ("He runs more quickly than John.",
				    "_advmod(run, quickly)\n" +
				    "_subj(run, he)\n" +
				    "than(he, John)\n" +
				    "more(quickly, run)\n" +
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs less quickly than John.",
				    "_advmod(run, quickly)\n" +
				    "_subj(run, he)\n" +
				    "_advmod(quickly, less)\n"+
				    "than(he, John)\n" +
				    "_more(quickly, run)\n" +
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs more quickly than John does.",
				    "_advmod(run, quickly)\n" +
				    "_subj(run, he)\n" +
				    "_subj(do, John)\n"+
				    "than(he, John)\n" +
				    "more(quickly, run)\n" +
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs less quickly than John does.",
				    "_advmod(run, quickly)\n" +
				    "_subj(run, he)\n" +
				    "_subj(do, John)\n"+
				    "_advmod(quickly, less)\n"+
				    "than(he, John)\n" +
				    "_more(quickly, run)\n" +
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs more than John.",
				    "_obj(run, more)\n" +
				    "_subj(run, he)\n" +
				    "than(he, John)\n"+
				    "more(more, run)\n"+
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs less than John.",
				    "_obj(run, less)\n" +
				    "_subj(run, he)\n" +
				    "than(he, John)\n"+
				    "_more(more, run)\n"+
				    "degree(quickly, comparative)\n");
		
		rc &= test_sentence ("He runs faster than John.",
				    "than(He, John)\n" +
				    "_more(fast, run)\n" +
				    "_subj(run, He)\n"+
				    "_advmod(run, fast)\n"+
				    "degree(fast, comparative)\n");
		
		rc &= test_sentence ("He runs more slowly than John.",
				    "than(He, John)\n" +
				    "_subj(run, He)\n" +
				    "more(slowly, run)\n"+
				    "_advmod(run, slowly)\n"+
				    "degree(slowly, comparative)\n");
		
		rc &= test_sentence ("He runs less slowly than John.",
				    "than(He, John)\n" +
				    "_subj(run, He)\n" +
				    "_more(slowly, run)\n"+
				    "_advmod(run, slowly)\n"+
				    "_advmod(slowly, less)\n"+
				    "degree(slowly, comparative)\n");
		
		rc &= test_sentence ("He runs more miles than John does.",
				    "than(he, John)\n" +
				    "_subj(run, He)\n" +
				    "_subj(do, John)\n"+
				    "_obj(run, mile)\n"+
				    "more(mile, run)\n"+
				    "_quantity(mile, many)\n"+
				    "degree(much, comparative)\n");
		
		rc &= test_sentence ("He runs less miles than John does.",
				    "than(he, John)\n" +
				    "_subj(run, He)\n" +
				    "_subj(do, John)\n"+
				    "_obj(run, mile)\n"+
				    "_more(mile, run)\n"+
				    "_quantity(mile, little)\n"+
				    "degree(little, comparative)\n");
		
		rc &= test_sentence ("He runs many more miles than John does.",
				    "than(he, John)\n" +
				    "more(mile, run)\n"+
				    "_obj(run, mile)\n"+
				    "_subj(run, he)\n" +
				    "_subj(do, John)\n" +		    
				    "_quantity(mile, many)\n"+
				    "degree(many, comparative)\n");
		report(rc, "Comparatives");
		return rc;
	}
        public boolean test_Conjunction()
	{
		boolean rc = true;
                //conjoined verbs
		rc &= test_sentence ("Scientists make observations and ask questions.",
		                     "_obj(make, observation)\n" +
		                     "_obj(ask, question)\n" +
		                     "_subj(make, scientist)\n" +
		                     "_subj(ask, scientist)\n" +
		                     "conj_and(make, ask)\n");
                //conjoined nouns              
		rc &= test_sentence ("She is a student and an employee.",
		                     "_obj(be, student)\n" +
		                     "_obj(be, employee)\n" +
		                     "_subj(be, she)\n" +
        	                     "conj_and(student, employee)\n");
                //conjoined adjectives
		rc &= test_sentence ("I hailed a black and white taxi.",
		                     "_obj(hail, taxi)\n" +
		                     "_subj(hail, I)\n" +
		                     "_amod(taxi, black)\n" +
                                     "_amod(taxi, white)\n" +
        	                     "conj_and(black, white)\n");
                //conjoined adverbs
		rc &= test_sentence ("She ran quickly and quietly.",
		                     "_advmod(run, quickly)\n" +
		                     "_advmod(run, quietly)\n" +
		                     "_subj(run, she)\n" +
        	                     "conj_and(quickly, quietly)\n");
                //adjectival modifiers on conjoined subject          
		rc &= test_sentence ("The big truck and the little car collided.",
		                     "_amod(car, little)\n" +
		                     "_amod(truck, big)\n" +
		                     "_subj(collide, truck)\n" +
                                     "_subj(collide, car)\n" +
        	                     "conj_and(truck, car)\n");
                //verbs with modifiers
                rc &= test_sentence ( "We ate dinner at home and went to the movies.",
		                     "_obj(eat, dinner)\n" +
		                     "conj_and(eat, go)\n" +
		                     "at(eat, home)\n" +
                                     "_subj(eat, we)\n" +
                                     "to(go, movie)\n" +
        	                     "_subj(go, we)\n");
                //verb with more modifiers
                rc &= test_sentence ("We ate a late dinner at home and went out to the movies afterwards.",
		                     "_obj(eat, dinner)\n" +
		                     "conj_and(eat, go_out)\n" +
		                     "at(eat, home)\n" +
                                     "_subj(eat, we)\n" +
                                     "to(go_out, movie)\n" +
                                     "_advmod(go_out, afterwards)\n" +
                                     "_subj(go_out, we)\n" +
        	                     "_amod(dinner, late)\n");

                //conjoined ditransitive verbs 
                rc &= test_sentence ("She baked him a cake and sang him a song.",
		                     "_iobj(sing, him)\n" +
		                     "_obj(sing, song)\n" +
		                     "_subj(sing, she)\n" +
                                     "_iobj(bake, him)\n" +
                                     "_obj(bake, cake)\n" +
                                     "conj_and(bake, sing)\n" +
           	                     "_subj(bake, she)\n"); 
                //conjoined adverbs with modifiers
                rc &= test_sentence ("she ran very quickly and extremely quietly.",
		                     "_advmod(run, quickly)\n" +
		                     "_advmod(run, quietly)\n" +
		                     "_subj(run, she)\n" +
                                     "_advmod(quietly, extremely)\n" +
                                     "conj_and(quickly, quietly)\n" +
              	                     "_advmod(quickly, very)\n"); 
               //conjoined adverbs with out modifiers
                rc &= test_sentence ("She handled it quickly and gracefully.",
		                     "_obj(handle, quickly)\n" +
		                     "_obj(handle, gracefully)\n" +
		                     "_advmod(handle, quickly)\n" +
                                     "_advmod(handle, gracefully)\n" +
                                     "_subj(handle, she)\n" +
              	                     "conj_and(quickly, gracefully)\n"); 
               //modifiers on conjoined adjectives
                rc &= test_sentence ("He had very long and very white hair.",
		                     "_obj(have, hair)\n" +
		                     "_subj(have, he)\n" +
		                     "_amod(hair, long)\n" +
                                     "_amod(hair, white)\n" +
                                     "_advmod(white, very)\n" +
                                     "conj_and(long, white)\n" +
              	                     "_advmod(long, very)\n");    
               //adjectival modifiers on conjoined object
                rc &= test_sentence ("The collision was between the little car and the big truck.",
		                     "_pobj(between, car)\n" +
		                     "_pobj(between, truck)\n" +
		                     "_psubj(between, collision)\n" +
                                     "_amod(truck, big)\n" +
                                     "_amod(car, little)\n" +
                                     "conj_and(car, truck)\n");
                //Names Modifiers  and conjunction
                rc &= test_sentence ("Big Tom and Angry Sue went to the movies.",
		                     "to(go, movie)\n" +
		                     "_subj(go, Big_Tom)\n" +
		                     "_subj(go, Angry_Sue)\n" +
                                     "conj_and(Big_Tom, Angry_Sue)\n");  

                report(rc, "Conjunction");
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
		                        "_obj(love, Pizza)\n" +
		                        "_quantity(people, most)\n" +
		                        "which(Pizza, love)\n" +
		                        "_subj(love, people)\n" +
		                        "_predadj(Pizza, healthy)\n" );

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
                rc &= ts.test_Conjunction();

		if (rc) {
			System.err.println("Tested " + ts.pass + " sentences, test passed OK");
		} else {
			System.err.println("Test failed\n\t" +
			                   ts.fail + " sentences failed\n\t" +
			                   ts.pass + " sentences passed");
		}

		System.err.println("******************************");
		System.err.println("Failed test sentences on Relex");
		System.err.println("******************************");
		if(sentfail.isEmpty())
			System.err.println("All test sentences passed");
		for(String temp : sentfail){
			System.err.println(temp);
		}
		System.err.println("******************************\n");
	}
}
