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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import junitparams.JUnitParamsRunner;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import relex.ParsedSentence;
import relex.RelationExtractor;
import relex.Sentence;
import relex.output.StanfordView;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@RunWith(JUnitParamsRunner.class)
public class TestStanford {
	private static RelationExtractor re;

	@BeforeClass
	public static void setUpClass() {
		re = new RelationExtractor();
		re.do_stanford = true;
	}
	
	/**
	 * First argument is the sentence.
	 * Second argument is a list of the relations that the 
	 * Stanford parser generates. 
	 * Return true if relex generates that same dependencies
	 * as the second argument.
	 */
	@Test
	@junitparams.Parameters(source=RelExCases.class, method="provideStanfordUntagged")
	public void untaggedSentence(String sent, Set<String> sf, Optional<String> description) {
		re.do_penn_tagging = false;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse, false);

		List<String> sfa = ImmutableList.copyOf(sf);
		List<String> rsa = Splitter.on("\n").omitEmptyStrings().splitToList(rs);
		assertThat("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, hasSize(rsa.size()));
		assertThat("Error: content miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, Matchers.containsInAnyOrder(rsa.toArray(new String[] {})));
	}

	@Test
	@junitparams.Parameters(source=RelExCases.class, method="provideStanfordTagged")
	public void taggedSentence(String sent, Set<String> sf, Optional<String> description) {
		re.do_penn_tagging = true;
		Sentence sntc = re.processSentence(sent);
		ParsedSentence parse = sntc.getParses().get(0);
		String rs = StanfordView.printRelations(parse, true);

		List<String> sfa = ImmutableList.copyOf(sf);
		List<String> rsa = Splitter.on("\n").omitEmptyStrings().splitToList(rs);
		assertThat("Error: size miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, hasSize(rsa.size()));
		assertThat("Error: content miscompare:\n" +
				"\tStanford = " + sfa + "\n" +
				"\tRelEx    = " + rsa + "\n" +
				"\tSentence = " + sent,
				sfa, Matchers.containsInAnyOrder(rsa.toArray(new String[] {})));
	}

}
