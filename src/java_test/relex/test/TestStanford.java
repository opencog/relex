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

import org.junit.Test;
import org.junit.BeforeClass;

public class TestStanford
{
	private static RelationExtractor re;
	private int pass;
	private int fail;
	private static ArrayList<String> sentfail= new ArrayList<String>();
	private static ArrayList<String> sentfailpostag= new ArrayList<String>();

	@BeforeClass
	public static void setUpClass() {
		re = new RelationExtractor();
	}

	public TestStanford()
	{
		re.do_stanford = true;
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
		String rs = StanfordView.printRelations(parse, false);

		ArrayList<String> sfa = split(sf);
		ArrayList<String> rsa = split(rs);
		if (sfa.size() != rsa.size())
		{
			System.err.println("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent);
			fail ++;
			sentfail.add(sent);
			return false;
		}
		for (int i=0; i< sfa.size(); i++)
		{
			if (!sfa.get(i).equals (rsa.get(i)))
			{
				System.err.println("Error: content miscompare:\n" +
					"\tStanford = " + sfa + "\n" +
					"\tRelEx    = " + rsa + "\n" +
					"\tSentence = " + sent);
				fail ++;
				sentfail.add(sent);
				return false;
			}
		}

		pass ++;
		return true;
	}

	public boolean test_tagged_sentence (String sent, String sf)
	{
		re.do_penn_tagging = true;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse, true);

		ArrayList<String> sfa = split(sf);
		ArrayList<String> rsa = split(rs);
		if (sfa.size() != rsa.size())
		{
			System.err.println("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent);
			fail ++;
			sentfailpostag.add(sent);
			return false;
		}
		for (int i=0; i< sfa.size(); i++)
		{
			if (!sfa.get(i).equals (rsa.get(i)))
			{
				System.err.println("Error: content miscompare:\n" +
					"\tStanford = " + sfa + "\n" +
					"\tRelEx    = " + rsa + "\n" +
					"\tSentence = " + sent);
				fail ++;
				sentfailpostag.add(sent);
				return false;
			}
		}

		pass ++;
		return true;
	}

	public static void main(String[] args)
	{
		setUpClass();
		TestStanford ts = new TestStanford();
		ts.runTests();
	}

	@Test
	public void runTests()
	{
		TestStanford ts = this;
		boolean rc = true;

		// The parses below were compared to the Stanford parser, circa
		// 2009.  Since then, it is likely that Stanford has changed.
		// The tests below should not be changed, unless a) they are
		// changed to be comaptible with current-day Stanford, and b) they
		// pass.
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
			"aux(go-7, can-6)");
			// wtf ?? dep is not documented .. not sure what to do here ...
			// "dep(is-2, go-7)");

		rc &= ts.test_sentence ("The linebacker gave the quarterback a push.",
			"det(linebacker-2, the-1)\n" +
			"nsubj(gave-3, linebacker-2)\n" +
			"det(quarterback-5, the-4)\n" +
			"iobj(gave-3, quarterback-5)\n" +
			"det(push-7, a-6)\n" +
			"dobj(gave-3, push-7)\n");

		rc &= ts.test_sentence ("He stood at the goal line.",
			"nsubj(stood-2, he-1)\n" +
			"det(line-6, the-4)\n" +
			"nn(line-6, goal-5)\n" +
			"prep_at(stood-2, line-6)");

		// acomp example from Stanford docs
		rc &= ts.test_sentence ("She looks very beautiful.",
			"nsubj(looks-2, she-1)\n" +
			"advmod(beautiful-4, very-3)\n" +
			"acomp(looks-2, beautiful-4)");

		// advcl example from Stanford docs
		rc &= ts.test_sentence ("The accident happened as the night was falling.",
			"det(accident-2, the-1)\n" +
			"nsubj(happened-3, accident-2)\n" +
			"mark(falling-8, as-4)\n" +
			"det(night-6, the-5)\n" +
			"nsubj(falling-8, night-6)\n" +
			"aux(falling-8, was-7)\n" +
			"advcl(happened-3, falling-8)");

		// advcl example from Stanford docs
		rc &= ts.test_sentence ("If you know who did it, you should tell the teacher.",
			"mark(know-3, if-1)\n" +
			"nsubj(know-3, you-2)\n" +
			"advcl(tell-10, know-3)\n" +
			"nsubj(did-5, who-4)\n" +
			"ccomp(know-3, did-5)\n" +
			"dobj(did-5, it-6)\n" +
			"nsubj(tell-10, you-8)\n" +
			"aux(tell-10, should-9)\n" +
			"det(teacher-12, the-11)\n" +
			"dobj(tell-10, teacher-12)");

		// agent example from Stanford docs
		rc &= ts.test_sentence ("The man has been killed by the police.",
			"det(man-2, the-1)\n" +
			"nsubjpass(killed-5, man-2)\n" +
			"aux(killed-5, has-3)\n" +
			"auxpass(killed-5, been-4)\n" +
			"det(police-8, the-7)\n" +
			"agent(killed-5, police-8)");

		rc &= ts.test_sentence ("Effects caused by the protein are important.",
			"nsubj(important-7, effects-1)\n" +
			"partmod(effects-1, caused-2)\n" +
			"det(protein-5, the-4)\n" +
			"agent(caused-2, protein-5)\n" +
			"cop(important-7, are-6)");

		rc &= ts.test_sentence ("Sam, my brother, has arrived.",
			"nsubj(arrived-7, Sam-1)\n" +
			"poss(brother-4, my-3)\n" +
			"appos(Sam-1, brother-4)\n" +
			"aux(arrived-7, has-6)");

		rc &= ts.test_sentence ("What is that?",
			"attr(is-2, what-1)\n" +
			"nsubj(is-2, that-3)");

		rc &= ts.test_sentence ("Reagan has died.",
			"nsubj(died-3, Reagan-1)\n" +
			"aux(died-3, has-2)");

		rc &= ts.test_sentence ("He should leave.",
			"nsubj(leave-3, he-1)\n" +
			"aux(leave-3, should-2)");

		rc &= ts.test_sentence ("Kennedy has been killed.",
			"nsubjpass(killed-4, Kennedy-1)\n" +
			"aux(killed-4, has-2)\n" +
			"auxpass(killed-4, been-3)");

		rc &= ts.test_sentence ("Kennedy was killed.",
			"nsubjpass(killed-3, Kennedy-1)\n" +
			"auxpass(killed-3, was-2)");

		rc &= ts.test_sentence ("Kennedy got killed.",
			"nsubjpass(killed-3, Kennedy-1)\n" +
			"auxpass(killed-3, got-2)");

		rc &= ts.test_sentence ("Bill is big.",
			"nsubj(big-3, Bill-1)\n" +
			"cop(big-3, is-2)\n");

		rc &= ts.test_sentence ("Bill is an honest man.",
			"nsubj(man-5, Bill-1)\n" +
			"cop(man-5, is-2)\n" +
			"det(man-5, an-3)\n" +
			"amod(man-5, honest-4)");

		rc &= ts.test_sentence ("What she said makes sense.",
			"dobj(said-3, what-1)\n" +
			"nsubj(said-3, she-2)\n" +
			"csubj(makes-4, said-3)\n" +
			"dobj(makes-4, sense-5)");

		rc &= ts.test_sentence ("What she said is not true.",
			"dobj(said-3, what-1)\n" +
			"nsubj(said-3, she-2)\n" +
			"csubj(true-6, said-3)\n" +
			"cop(true-6, is-4)\n" +
			"neg(true-6, not-5)");

		rc &= ts.test_sentence ("Which book do you prefer?",
			"det(book-2, which-1)\n" +
			"dobj(prefer-5, book-2)\n" +
			"aux(prefer-5, do-3)\n" +
			"nsubj(prefer-5, you-4)");

		rc &= ts.test_sentence ("There is a ghost in the room.",
			"expl(is-2, there-1)\n" +
			"det(ghost-4, a-3)\n" +
			"nsubj(is-2, ghost-4)\n" +
			"det(room-7, the-6)\n" +
			"prep_in(is-2, room-7)");

		rc &= ts.test_sentence ("She gave me a raise.",
			"nsubj(gave-2, she-1)\n" +
			"iobj(gave-2, me-3)\n" +
			"det(raise-5, a-4)\n" +
			"dobj(gave-2, raise-5)");

		rc &= ts.test_sentence ("The director is 65 years old.",
			"det(director-2, the-1)\n" +
			"nsubj(old-6, director-2)\n" +
			"cop(old-6, is-3)\n" +
			"num(years-5, 65-4)\n" +
			"measure(old-6, years-5)");

		rc &= ts.test_sentence ("Sam eats 3 sheep.",
			"nsubj(eats-2, Sam-1)\n" +
			"num(sheep-4, 3-3)\n" +
			"dobj(eats-2, sheep-4)");

/****************
 * I don't get it. Stanford makes a num/number distinction I can't grok.
		rc &= ts.test_sentence ("I lost $ 3.2 billion.",
			"nsubj(lost-2, I-1)\n" +
			"dobj(lost-2, $-3)\n" +
			"number($-3, 3.2-4)\n" +
			"number($-3, billion-5)");
***********/

		rc &= ts.test_sentence ("Truffles picked during the spring are tasty.",
			"nsubj(tasty-7, truffles-1)\n" +
			"partmod(truffles-1, picked-2)\n" +
			"det(spring-5, the-4)\n" +
			"prep_during(picked-2, spring-5)\n" +
			"cop(tasty-7, are-6)");

/****************
 * Currently fails due to xcomp generation problems
 *
		rc &= ts.test_sentence ("We went to their offices to get Bill's clothes.",
			"nsubj(went-2, we-1)\n" +
			"xsubj(get-7, we-1)\n" +
			"poss(offices-5, their-4)\n" +
			"prep_to(went-2, offices-5)\n" +
			"aux(get-7, to-6)\n" +
			"xcomp(went-2, get-7)\n" +
			"poss(clothes-10, Bill-8)\n" +
			"dobj(get-7, clothes-10)");
***********/

/****************
 * See README-Stanford for details.
		rc &= ts.test_sentence ("All the boys are here.",
			"predet(boys-3, all-1)\n" +
			"det(boys-3, the-2)\n" +
			"nsubj(are-4, boys-3)\n" +
			"advmod(are-4, here-5)");
***********/

/****************
 * These are ambiguous parses.
 * Stanford picks the opposite choice from Relex.
 * See the README-Stanford for a discussion.
		rc &= ts.test_sentence ("I saw a cat in a hat.",
			"nsubj(saw-2, I-1)\n" +
			"det(cat-4, a-3)\n" +
			"dobj(saw-2, cat-4)\n" +
			"det(hat-7, a-6)\n" +
			"prep_in(cat-4, hat-7)");

		rc &= ts.test_sentence ("I saw a cat with a telescope.",
			"nsubj(saw-2, I-1)\n" +
			"det(cat-4, a-3)\n" +
			"dobj(saw-2, cat-4)\n" +
			"det(telescope-7, a-6)\n" +
			"prep_with(cat-4, telescope-7)");
***********/

		rc &= ts.test_sentence ("He is responsible for meals.",
			"nsubj(responsible-3, he-1)\n" +
			"cop(responsible-3, is-2)\n" +
			"prep_for(responsible-3, meals-5)\n");

		rc &= ts.test_sentence ("They shut down the station.",
			"nsubj(shut-2, they-1)\n" +
			"prt(shut-2, down-3)\n" +
			"det(station-5, the-4)\n" +
			"dobj(shut-2, station-5)");

		rc &= ts.test_sentence ("About 200 people came to the party.",
			"quantmod(200-2, about-1)\n" +
			"num(people-3, 200-2)\n" +
			"nsubj(came-4, people-3)\n" +
			"det(party-7, the-6)\n" +
			"prep_to(came-4, party-7)");

		rc &= ts.test_sentence ("I saw the man who you love.",
			"nsubj(saw-2, I-1)\n" +
			"det(man-4, the-3)\n" +
			"dobj(saw-2, man-4)\n" +
			"dobj(love-7, man-4)\n" +
			"rel(love-7, who-5)\n" +
			"nsubj(love-7, you-6)\n" +
			"rcmod(man-4, love-7)");


/****************
 *
 * relex is failing to generate teh following:
 * Almost got it w/the B** rules but not quite ...

rel(love-8, wife-6)
rcmod(man-4, love-8)

		rc &= ts.test_sentence ("I saw the man whose wife you love.",
			"nsubj(saw-2, I-1)\n" +
			"det(man-4, the-3)\n" +
			"dobj(saw-2, man-4)\n" +
			"poss(wife-6, whose-5)\n" +
			"dobj(love-8, wife-6)\n" +
			"rel(love-8, wife-6)\n" +
			"nsubj(love-8, you-7)\n" +
			"rcmod(man-4, love-8)");
***********/

		rc &= ts.test_sentence ("I am ready to leave.",
			"nsubj(ready-3, I-1)\n" +
			"cop(ready-3, am-2)\n" +
			"aux(leave-5, to-4)\n" +
			"xcomp(ready-3, leave-5)");

		rc &= ts.test_sentence ("Tom likes to eat fish.",
			"nsubj(likes-2, Tom-1)\n" +
			"xsubj(eat-4, Tom-1)\n" +
			"aux(eat-4, to-3)\n" +
			"xcomp(likes-2, eat-4)\n" +
			"dobj(eat-4, fish-5)");


/****************
		rc &= ts.test_sentence ("He says that you like to swim.",
			"nsubj(says-2, he-1)\n" +
			"complm(like-5, that-3)\n" +
			"nsubj(like-5, you-4)\n" +
			"ccomp(says-2, like-5)\n" +
			"nsubj(swim-7, to-6)\n" +   // NFW that this can't be right.
			"ccomp(like-5, swim-7)");
**************/


		rc &= ts.test_sentence ("The garage is next to the house.",
			"det(garage-2, the-1)\n" +
			"nsubj(next-4, garage-2)\n" +
			"cop(next-4, is-3)\n" +
			"det(house-7, the-6)\n" +
			"prep_to(next-4, house-7)");

		// =========================================================
		// PENN PART_OF_SPEECH TAGGING
		// =========================================================
		//
		rc &= ts.test_tagged_sentence ("Truffles picked during the spring are tasty.",
			"nsubj(tasty-7-JJ, truffles-1-NNS)\n" +
			"partmod(truffles-1-NNS, picked-2-VBN)\n" +
			"det(spring-5-NN, the-4-DT)\n" +
			"prep_during(picked-2-VBN, spring-5-NN)\n" +
			"cop(tasty-7-JJ, are-6-VBP)");

		rc &= ts.test_tagged_sentence ("I ate twelve truffles.",
			"nsubj(ate-2-VBD, I-1-PRP)\n" +
			"num(truffles-4-NNS, twelve-3-CD)\n" +
			"dobj(ate-2-VBD, truffles-4-NNS)");

		rc &= ts.test_tagged_sentence ("I have eaten twelve truffles.",
			"nsubj(eaten-3-VBN, I-1-PRP)\n" +
			"aux(eaten-3-VBN, have-2-VBP)\n" +
			"num(truffles-5-NNS, twelve-4-CD)\n" +
			"dobj(eaten-3-VBN, truffles-5-NNS)");

		rc &= ts.test_tagged_sentence ("I had eaten twelve truffles.",
			"nsubj(eaten-3-VBN, I-1-PRP)\n" +
			"aux(eaten-3-VBN, had-2-VBD)\n" +
			"num(truffles-5-NNS, twelve-4-CD)\n" +
			"dobj(eaten-3-VBN, truffles-5-NNS)");

		rc &= ts.test_tagged_sentence ("The truffles were eaten.",
			"det(truffles-2-NNS, the-1-DT)\n" +
			"nsubjpass(eaten-4-VBN, truffles-2-NNS)\n" +
			"auxpass(eaten-4-VBN, were-3-VBD)");


		// Full disclosure:  Stanford currently generates
		// dep(time-4-NN, young-8-JJ) which just means it doesn't know
		// the right answer (which is advcl, right?).
		// It also generates advmod(young-8-JJ, when-5-WRB) in addition
		// to rel(young-8-JJ, when-5-WRB) which is not quite right
		// either.
		rc &= ts.test_tagged_sentence ("There was a time when we were young.",
			"expl(was-2-VBD, there-1-EX)\n" +
			"det(time-4-NN, a-3-DT)\n" +
			"nsubj(was-2-VBD, time-4-NN)\n" +
			"rel(young-8-JJ, when-5-WRB)\n" +
			"nsubj(young-8-JJ, we-6-PRP)\n" +
			"cop(young-8-JJ, were-7-VBD)\n" +
			"advcl(time-4-NN, young-8-JJ)");

		rc &= ts.test_tagged_sentence ("Is there a better way?",
			"expl(is-1-VBZ, there-2-EX)\n" +
			"det(way-5-NN, a-3-DT)\n" +
			"amod(way-5-NN, better-4-JJR)\n" +
			"nsubj(is-1-VBZ, way-5-NN)");

		rc &= ts.test_tagged_sentence ("Is this the largest you can find?",
			"cop(largest-4-JJS, is-1-VBZ)\n" +
			"nsubj(largest-4-JJS, this-2-DT)\n" +
			"det(largest-4-JJS, the-3-DT)\n" +
			"nsubj(find-7-VB, you-5-PRP)\n" +
			"aux(find-7-VB, can-6-MD)\n" +
			"rcmod(largest-4-JJS, find-7-VB)");

		rc &= ts.test_tagged_sentence ("But my efforts to win his heart have failed.",
			"poss(efforts-3-NNS, my-2-PRP$)\n" +
			"nsubj(failed-9-VBN, efforts-3-NNS)\n" +
			"aux(win-5-VB, to-4-TO)\n" +
			"infmod(efforts-3-NNS, win-5-VB)\n" +
			"poss(heart-7-NN, his-6-PRP$)\n" +
			"dobj(win-5-VB, heart-7-NN)\n" +
			"aux(failed-9-VBN, have-8-VBP)");

		rc &= ts.test_tagged_sentence ("The undergrads are occasionally late.",
			"det(undergrads-2-NNS, the-1-DT)\n" +
			"nsubj(late-5-JJ, undergrads-2-NNS)\n" +
			"cop(late-5-JJ, are-3-VBP)\n" +
			"advmod(late-5-JJ, occasionally-4-RB)");

		rc &= ts.test_tagged_sentence ("The height of Mount Everest is 8,848 metres.",
			"det(height-2-NN, the-1-DT)\n" +
			"nsubj(metres-8-NNS, height-2-NN)\n" +
			"nn(Everest-5-NNP, Mount-4-NNP)\n" +
			"prep_of(height-2-NN, Everest-5-NNP)\n" +
			"cop(metres-8-NNS, is-6-VBZ)\n" +
			"num(metres-8-NNS, 8,848-7-CD)");

		rc &= ts.test_tagged_sentence ("It happened on December 3rd, 1990.",
			"nsubj(happened-2-VBD, it-1-PRP)\n" +
			"prep_on(happened-2-VBD, December-4-NNP)\n" +
			"num(December-4-NNP, 3rd-5-CD)\n" +
			"num(December-4-NNP, 1990-7-CD)");


		if (rc)
		{
			System.err.println("Stanford compatibility: Tested " + ts.pass + " sentences, test passed OK");
		}
		else
		{
			int total = ts.pass + ts.fail;
			System.err.println("Stanford compatibility: Test failed; out of " +
				total + " sentences total,\n\t" + 
				ts.fail + " sentences failed\n\t" +
				ts.pass + " sentences passed");
		}


		if (!sentfail.isEmpty())
		{
			System.err.println("********************************************************");
			System.err.println("Stanford compat: Failed sentences with POS tagging FALSE");
			System.err.println("********************************************************");

			for (String temp : sentfail)
			{
				System.err.println(temp);
			}
			System.err.println("********************************************************\n");
		}


		if (!sentfailpostag.isEmpty())
		{
			System.err.println("********************************************************");
			System.err.println("Stanford compat: Failed sentences with POS tagging TRUE");
			System.err.println("********************************************************");

			for (String temp : sentfailpostag)
			{
				System.err.println(temp);
			}
			System.err.println("********************************************************\n");
		}
	}
}
