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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;

import junitparams.JUnitParamsRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import relex.ParsedSentence;
import relex.RelationExtractor;
import relex.Sentence;
import relex.output.SimpleView;

@RunWith(JUnitParamsRunner.class)
public class RelExTest {
	
	private static final Logger log = LoggerFactory.getLogger(RelExTest.class);
	private static RelationExtractor re;
	
	@BeforeClass
	public static void setUpClass() {
		log.info("Initializing RelEx...");
		re = new RelationExtractor();
	}

	public ArrayList<String> split(String a) {
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
	public void test_sentence(String sent, String sf) {
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
			if(!brgot.contains(exp.get(i)))
			{
				assertThat("content miscompare:\n" +
					    "\tExpected = " + exp + "\n" +
					    "\tGot Binary Relations = " + brgot + "\n" +
					    "\tGot Unary Relations = " + urgot + "\n" +
					    "\tSentence = " + sent,
					    urgot, hasItem(exp.get(i)));
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
		assertThat("size miscompare:\n" +
			    "\tExpected = " + exp + "\n" +
			    "\tGot Binary Relations = " + brgot + "\n" +
			    "\tGot Unary Relations = " + urgot + "\n" +
			    "\tSentence = " + sent,
			    exp, hasSize(greaterThanOrEqualTo(sizeOfGotRelations)));
	}

	@Test
	@junitparams.Parameters(source=ComparativesCases.class)
	public void comparatives(String sentence, String expected) {
		test_sentence(sentence, expected);
	}
	
	@Test
	@junitparams.Parameters(source=ConjunctionCases.class)
    public void conjunction(String sentence, String expected) {
		test_sentence(sentence, expected);
	}
	
	@Test
	@junitparams.Parameters(source=ExtrapositionCases.class)
	public void extraposition(String sentence, String expected) {
		test_sentence(sentence, expected);
	}

}
