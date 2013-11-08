package relex.logic;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Description: Class that takes a ReLex2Logic rule file and parses these into a Rule & Criterium structure that can be applied to the
// root FeatureNode of a dependency graph.

// File format example:
/*
[SVO]  {2} <SV, SVP> _subj($y, $x) & _obj($y, $z) => (SVO-rule $x (get_instance_name $x word_index sentence_index) $y (get_instance_name $y word_index sentence_index) $z (get_instance_name $z word_index sentence_index))
[AMOD] {3} <> _amod($N, $A) => (amod-rule $N (get_instance_name $N word_index sentence_index) $A (get_instance_name $A word_index sentence_index))
[ADVMOD] {4} <> _advmod($V, $ADV) => (advmod-rule $V (get_instance_name $V word_index sentence_index) $ADV (get_instance_name $ADV word_index sentence_index))
[TENSEPOS] {5} <> tense($W, $Tense) & pos($W, verb) => (tense-rule $W (get_instance_name $W word_index sentence_index) $Tense)
[DET] {6} <> _det($W, those) => (those-rule $W (get_instance_name $W word_index sentence_index) choose_var_name)
[NEGFLAG] {7} <> NEGATIVE-FLAG($V, T) => (negative-rule $V (get_instance_name $V word_index sentence_index))
[POSS1A] {8} <POSS1B, POSS2> _poss($N, $W) & pos($W, adj) => (possesive-rule $N (get_instance_name $N word_index sentence_index) $W (get_instance_name $W word_index sentence_index))
[POSS1B] {8} <POSS1A, POSS2> _poss($N, $W) & pos($W, noun) & person-FLAG($W, T) => (possesive-rule $N (get_instance_name $N word_index sentence_index) $W (get_instance_name $W word_index sentence_index))
[POSS2] {8} <POSS1A, POSS1B> _poss($N, $W) & pos($W, noun) => (possesive-rule $N (get_instance_name $V word_index sentence_index) $W (get_instance_name $W word_index sentence_index))
*/

public class ReLex2LogicRuleLoader
{
	// The rules once they are loaded
	private ReLex2LogicRuleSet _relex2SchemeRuleSet = new ReLex2LogicRuleSet();
	
	// Summary: Processes a rule file and loads them into _relex2SchemeRuleSet
	public Boolean loadRules(String ruleFile)
	{
		Boolean loadSuccesful = false;
		
		File file = new File(ruleFile);
		Scanner input = null;
		
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (input != null) {
			// Parse file
			int iRules = 0;
			
			while(input.hasNext()) {
				// Get line
			    String nextLine = input.nextLine();
			    
			    if (!nextLine.equals(""))
			    {
			    	if (!nextLine.substring(0, 1).equals("#"))
		    		{
					    ReLex2LogicRule newRule = new ReLex2LogicRule(nextLine);

					    _relex2SchemeRuleSet.addRule(newRule);
					    
					    iRules++;		    		
		    		}
			    	
			    }
			    // Parse line
			    
			    // (SVO-rule $x (get_instance_name $x word_index sentence_index) $y (get_instance_name $y word_index sentence_index) $z (get_instance_name $z word_index sentence_index))
			}

			input.close();
			
			System.out.println("Loaded " + iRules + " rule(s) succesfully.");
			
			loadSuccesful = true;
		}
		
		return loadSuccesful;
	}
	
	// Summary: Get an ‘unused’ set of the rules, could be used later for batch processing
	public ReLex2LogicRuleSet getFreshRuleSet()
	{
		ReLex2LogicRuleSet freshRuleSet = new ReLex2LogicRuleSet();
		
		for (ReLex2LogicRule rule: _relex2SchemeRuleSet.getRules()) {
			freshRuleSet.addRule(new ReLex2LogicRule(rule.getRuleString()));
		}
		
		return freshRuleSet;
	}
	
	
}
