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

import static org.hamcrest.Matchers.equalTo;
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
import relex.output.StanfordView;

@RunWith(JUnitParamsRunner.class)
public class StanfordTest {
	
	private static final Logger log = LoggerFactory
			.getLogger(StanfordTest.class);
	private static RelationExtractor re;

	@BeforeClass
	public static void setUpClass() {
		log.info("Initializing RelEx...");
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

	/**
	 * First argument is the sentence.
	 * Second argument is a list of the relations that the 
	 * Stanford parser generates. 
	 * Return true if relex generates that same dependencies
	 * as the second argument.
	 */
	@Test
	@junitparams.Parameters(source=StanfordCases.class, method="provideUntagged")
	public void untaggedSentence(String sent, String sf) {
		re.do_penn_tagging = false;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse, false);

		ArrayList<String> sfa = split(sf);
		ArrayList<String> rsa = split(rs);
		assertThat("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, hasSize(rsa.size()));
		assertThat("Error: content miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, equalTo(rsa));
	}

	@Test
	@junitparams.Parameters(source=StanfordCases.class, method="provideTagged")
	public void taggedSentence(String sent, String sf) {
		re.do_penn_tagging = true;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse, true);

		ArrayList<String> sfa = split(sf);
		ArrayList<String> rsa = split(rs);
		assertThat("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, hasSize(rsa.size()));
		assertThat("Error: content miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, equalTo(rsa));
	}

}
