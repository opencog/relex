/*
 * Copyright 2013 OpenCog Foundation
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
 *
 * Alex van der Peet <alex.van.der.peet@gmail.com>
 */
package relex.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// Description: Class to store the details of a rule, namely its name, priority, mutually exclusive rules (if any), criteria (in the form of ReLex2LogicCriterium).

// Textual definition of a rule from the rule file:
// [SVO]  {2} <SV, SVP> _subj($y, $x) & _obj($y, $z) => (SVO-rule $x (get_instance_name $x word_index sentence_index) $y (get_instance_name $y word_index sentence_index) $z (get_instance_name $z word_index sentence_index))

/** Stores Rule details and Criteria
 * @author      Alex van der Peet <alex.van.der.peet@gmail.com>
 * @version     1.0                 (current version number of program)
 * @since       2013-11-08          (the version of the package this class was first added to)
 */
public class Rule {
	/**
	 * Full rule definition in textual format as provided through the constructor.
	 */
	private String _ruleString;
	/**
	 * Name of the rule (in the example above, 'SVO')
	 */
	private String _name;
	/**
	 * The priority of the rule (in the example above, 2)
	 */
	private int _priority = -1;
	/**
	 * The criteria in the rule, as strings (in the example above _subj($y, $x) & _obj($y, $z)
	 */
	private List<String> _criteriaStrings;
	/**
	 * The criteria in the rule, as objects
	 */
	private List<Criterium> _criteria;

	private List<String> _exclusionList;


	/**
	 * Constructor to build a ReLex2LogicRule from a string in the rule file.
	 * @param ruleString A rule string
	 * @see relex.logic.Loader
	 */
	public Rule(String ruleString) {
		_ruleString = ruleString;

		// build the required data immediately
		getCriteria();
		getMutuallyExclusiveRuleNames();
	}

	/**
	 * Checks all criteria in this rule to check whether their variables have been satisfied.
	 * Once all criteria of a rule have been satisfied, the output can be written out.
	 * @return Boolean indicating whether all criteria have been satisfied.
	 */
	public Boolean getAllCriteriaSatisfied() {
		Boolean allSatisfied = true;

		for (Criterium criterium : _criteria) {
			if (!criterium.getAllVariablesSatisfied())
				allSatisfied = false;
		}

		return allSatisfied;
	}

	/**
	 * Returns the name of this rule, in the example above this would be SVO
	 * @return The name of this rule.
	 */
	public String getName() {
		if (_name == null) {
			_name = getStringSection("[", "]");
		}

		return _name;
	}

	/**
	 * Returns the priority of this rule, in the example above this would be 2
	 * @return The priority (Integer) of this rule
	 */
	public Integer getPriority() {
		if (_priority == -1) {
			String strPriority = getStringSection("{", "}");

			if (tryParseInt(strPriority))
				_priority = Integer.parseInt(strPriority);
		}

		return _priority;
	}

	/**
	 * Returns the output part of the rule in its original form, so with the original variable string still in place. To get the string with its variables replaced, use getSchemeOutput
	 * @return The output part of the rule in its original form
	 */
	public String getOutputString() {
		String outputString = _ruleString
				.substring(_ruleString.indexOf("=>") + 3);

		return outputString;
	}

	/**
	 * Returns the Scheme output as defined by the rule, with the variables replaced by the values identified by the matching process in LogicSchemeView
	 * @return Scheme output.
	 */
	public String getSchemeOutput() {
		String schemeOutput = getOutputString();

		for (Criterium criterium : _criteria) {
			for (String variableName : criterium.getVariables()) {
				String variableValue = criterium.getVariableValue(variableName);
				String variableValueUUID = criterium.getVariableValueUUID(variableName);
				if(variableName.substring(0, 1).equals("$"))
				{
					schemeOutput = schemeOutput.replaceAll(
											Pattern.quote(variableName), Matcher.quoteReplacement("\"" + variableValue + "\""));
					schemeOutput = schemeOutput.replaceAll(
											Pattern.quote("\"" + variableValue + "\"" + " (get-instance-name " + "\"" + variableValue + "\"" + " word_index"),
											Matcher.quoteReplacement("\"" + variableValue + "\"" + " (get-instance-name " + "\"" + variableValue + "\" " +"\"" + variableValueUUID + "\""));
				}
			}
		}

		return schemeOutput;
	}

	/**
	 * Returns a List<String> containing the criteria in the form they were supplied to the rule on constructions. In the example above, two strings, _subj($y, $x) and _obj($y, $z)
	 * @return  A List<String> containing the original criteria strings.
	 */
	public List<String> getCriteriaStrings() {
		if (_criteriaStrings == null) {
			String criteriaString = getStringSection(">", "=>").trim();

			String[] criteriaStrings = criteriaString.split(" & ");

			_criteriaStrings = Arrays.asList(criteriaStrings);
		}

		return _criteriaStrings;
	}

	/**
	 * @return The criteria of this rule as a list of ReLex2LogicRuleCriterium objects.
	 */
	public List<Criterium> getCriteria() {
		if (_criteria == null) {
			_criteria = new ArrayList<Criterium>();

			for (String criteriumString : getCriteriaStrings()) {
				_criteria.add(new Criterium(criteriumString));
			}
		}

		return _criteria;
	}

	/**
	 * @return The number of criteria in this rule.
	 */
	public Integer getCriteriaCount() {
		return getCriteriaStrings().size();
	}


	/**
	 * Check if a rule name matches one in the exclusion list.  Regular Expression
	 * @param otherName   The rule name to check
	 * @return            True if excluded, false otherwise
	 */
	public Boolean isRuleMutuallyExclusive(String otherName)
	{
		for (String ruleRegex : _exclusionList)
		{
			if (otherName.matches(ruleRegex))
				return true;
		}

		return false;
	}


	/**
	 * @return A <List>String of the names of rules that are mutually exclusive to this rule
	 */
	public List<String> getMutuallyExclusiveRuleNames() {
		if (_exclusionList == null)
		{
			String mutuallyExclusiveRuleSection = getStringSection("<", ">");

			String[] mutuallyExclusiveRuleNames = mutuallyExclusiveRuleSection
					.split(", ");
			_exclusionList = Arrays.asList(mutuallyExclusiveRuleNames);
		}

		// return a copy, don't really want a reference to private variable to leave this object
		return new ArrayList<String>(_exclusionList);
	}

	/**
	 * @return The string part of the mutually exclusive rule section of the original rule string provided in the constructor
	 */
	public String getMutuallyExclusiveRuleNamesString() {
		String mutuallyExclusiveRuleSection = getStringSection("<", ">");

		return mutuallyExclusiveRuleSection;
	}

	/**
	 * Helper function to attempt to parse a String to an Integer.
	 * @param value The String value of the Integer to attempt parsing on.
	 * @return Boolean indicating whether the String can be parsed to an Integer.
	 */
	private boolean tryParseInt(String value) {
		try {
			Integer.parseInt(value);

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	/**
	 * Helper function to retrieve a string section from the original rule string based on a left and right side bounding character.
	 * @param leftBoundingChar The character before the first character of the string that is to be retrieved.
	 * @param rightBoundingChar The character after the last character of the string that is to be retrieved.
	 * @return A String starting one index after the first occurence of leftBoundingChar and ending one index before the first occurence of rightBoundingChar
	 */
	private String getStringSection(String leftBoundingChar,
			String rightBoundingChar) {
		int left = _ruleString.indexOf(leftBoundingChar);
		int right = _ruleString.indexOf(rightBoundingChar);

		return _ruleString.substring(left + 1, right);
	}

	/**
	 * Returns the original rule string provided in the constructor of this rule.
	 * @return The original rule string provided to create this rule.
	 */
	public String getRuleString() {
		return _ruleString;
	}

	/**
	 * Maps the Variable names  of a Criterium to the Criterium that use the variable.
	 * @return The mapping from a variable name to a list of Ctirerium-strings.
	 */
	public HashMap<String, List<String>> mapVariableNameToCriterium()
	{
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();

		for (Criterium ruleCriterium: getCriteria())
		{
			for(String variable : ruleCriterium.getVariables())
			{
				if(map.containsKey(variable))
					map.get(variable).add(ruleCriterium.getCriteriumString());
				else
				{
					List aList = new ArrayList();
					aList.add(ruleCriterium.getCriteriumString());
					map.put(variable, aList);
				}
			}
		}

		return map;
	}
}
