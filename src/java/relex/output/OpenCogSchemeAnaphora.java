/*
 * Copyright 2009 Novamente LLC
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

package relex.output;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import relex.ParsedSentence;
import relex.Sentence;
import relex.anaphora.Antecedents;
import relex.anaphora.Hobbs;
import relex.anaphora.history.SentenceHistoryFactory;
import relex.anaphora.history.SentenceHistoryFactory.HistoryEnum;

/**
 * Implements opencog scheme output from hobbs anaphora resolution.
 * 
 * @author fabricio <fabricio@vettalabs.com>
 * 
 */
public class OpenCogSchemeAnaphora {

	// The sentence being examined.
	private Sentence sentence;
	
	private static final String anaphoraStr="possible_anaphora";
	
	private Antecedents antecedents;
	private Hobbs hobbs;
	
	public OpenCogSchemeAnaphora(){
		antecedents = new Antecedents();
		hobbs = new Hobbs(antecedents);
		hobbs.setHistory(SentenceHistoryFactory.create(HistoryEnum.DEFAULT));
	}
	
	public void setSentence(ParsedSentence s, ArrayList<String> word_list){
		s.setWordList(word_list);
		this.sentence=s.getSentence();
	}
	
	public void clear()
	{
		antecedents.clear();//cleans the old anaphora candidates
	}
	
	public String toString(){
		return printAnaphora();
	}

	private String printAnaphora() {
		hobbs.addParse(sentence);
		hobbs.resolve(sentence);
		String candidates = antecedents.toString();		
		return convertCandidatesToOpencogScheme(candidates);
	}

	/**
	 * converts the hobbs output (like: 
	 * _ante_candidate(it_1, apple) {0}
	 * _ante_candidate(it_1, mushroom) {1}
	 * to scheme output, assuming that the anaphora candidates are
	 * separate by a line break (\n)
	 */
	private String convertCandidatesToOpencogScheme(String candidates) {
		StringTokenizer tokens = new StringTokenizer(candidates,"\n");
		String schemeOutput="";
		while(tokens.hasMoreTokens()){
			String token = tokens.nextToken();
			schemeOutput += convertCandidate(token);
		}
		return schemeOutput;
	}

	/**
	 * 
	 * @param the line to be converted, assumed to be like: _ante_candidate(it_1, apple) {0}
	 * @return scheme output according to Linas suggestion
	 */
	private String convertCandidate(String token) {
		String patternStr = "_ante_candidate\\((.*),\\s+(.*)\\)";
		Pattern pattern = Pattern.compile(patternStr);
	    Matcher matcher = pattern.matcher(token);
	    boolean matchFound = matcher.find();
	    
	    if (matchFound) {
	    	//the first term (the pronoun)
	    	String item1 =  matcher.group(1);
	    	//the second term (the term that the first one apparently refers to)
	    	String item2 = matcher.group(2);
	    		
			String str="";

			str += "(EvaluationLink \n" +
		       	   "    (ConceptNode \"" + anaphoraStr + "\" \n" +
		       	   "        (ListLink \n" +
		       	   "             (WordInstanceNode \""+item1+"\" )\n"+
		       	   "             (WordInstanceNode \""+item2+"\" )\n"+
		       	   "         )\n" +
		       	   "     )\n" +
		       	   ")\n";
			
			return str;
	    }
		return "";
	}
}
