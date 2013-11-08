package relex.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// Description: Class to store the details of a rule, namely its name, priority, mutually exclusive rules (if any), criteria (in the form of ReLex2LogicCriterium).

// Textual definition of a rule from the rule file: 
// [SVO]  {2} <SV, SVP> _subj($y, $x) & _obj($y, $z) => (SVO-rule $x (get_instance_name $x word_index sentence_index) $y (get_instance_name $y word_index sentence_index) $z (get_instance_name $z word_index sentence_index))

public class ReLex2LogicRule {
	// Full rule definition in textual format as provided through the
	// constructor.
	private String _ruleString;

	// Name of the rule (in the example above, 'SVO')
	private String _name;
	// The priority of the rule (in the example above, 2)
	private int _priority = -1;
	// The criteria in the rule, as strings (in the example above _subj($y, $x)
	// & _obj($y, $z)
	private List<String> _criteriaStrings;
	// The criteria in the rule, as objects
	private List<ReLex2LogicRuleCriterium> _criteria;

	// Summary: Constructor to build a ReLex2LogicRule from a string in the rule
	// file.
	public ReLex2LogicRule(String ruleString) {
		_ruleString = ruleString;
	}

	// Summary: Checks all criteria in this rule to check whether their
	// variables have been satisfied. Once all criteria of a rule have been
	// satisfied, the output can be written out.
	public Boolean getAllCriteriaSatisfied() {
		Boolean allSatisfied = true;

		for (ReLex2LogicRuleCriterium criterium : _criteria) {
			if (!criterium.getAllVariablesSatisfied())
				allSatisfied = false;
		}

		return allSatisfied;
	}

	// Summary: Returns the name of this rule, in the example above this would
	// be SVO
	public String getName() {
		if (_name == null) {
			_name = getStringSection("[", "]");
		}

		return _name;
	}

	// Summary: Return the priority of this rule, in the example above this
	// would be 2
	public Integer getPriority() {
		if (_priority == -1) {
			String strPriority = getStringSection("{", "}");

			if (tryParseInt(strPriority))
				_priority = Integer.parseInt(strPriority);
		}

		return _priority;
	}

	// Summary: Returns the output part of the rule in its original form, so
	// with the original variable string still in place. To get the string with
	// its variables replaced, use getSchemeOutput
	public String getOutputString() {
		String outputString = _ruleString
				.substring(_ruleString.indexOf("=>") + 3);

		return outputString;
	}

	// Summary: Returns the Scheme output as defined by the rule, with the
	// variables replaced by the values identified by the matching process in
	// OpenCogRelExToLogicSchemeView
	public String getSchemeOutput() {
		String schemeOutput = getOutputString();

		for (ReLex2LogicRuleCriterium criterium : _criteria) {
			for (String variableName : criterium.getVariables()) {
				String variableValue = criterium.getVariableValue(variableName);

				schemeOutput = schemeOutput.replaceAll(
						Pattern.quote(variableName), variableValue);
			}
		}

		return schemeOutput;
	}

	// Summary: Returns a list of Strings containing the criteria in the form
	// they were supplied to the rule on constructions. In the example above,
	// two strings, _subj($y, $x) and _obj($y, $z)
	public List<String> getCriteriaStrings() {
		if (_criteriaStrings == null) {
			String criteriaString = getStringSection(">", "=>").trim();

			String[] criteriaStrings = criteriaString.split(" & ");

			_criteriaStrings = Arrays.asList(criteriaStrings);
		}

		return _criteriaStrings;
	}

	// Summary: Returns the criteria of this rule as a list of
	// ReLex2LogicRuleCriterium objects.
	public List<ReLex2LogicRuleCriterium> getCriteria() {
		if (_criteria == null) {
			_criteria = new ArrayList<ReLex2LogicRuleCriterium>();

			for (String criteriumString : getCriteriaStrings()) {
				_criteria.add(new ReLex2LogicRuleCriterium(criteriumString));
			}
		}

		return _criteria;
	}

	// Summary: Returns the number of criteria in this rule.
	public Integer getCriteriaCount() {
		return getCriteriaStrings().size();
	}

	// Summary: Returns a <List>String of the names of rules that are mutually
	// exclusive to this rule. In the example above, <SV, SVP>
	public List<String> getMutuallyExclusiveRuleNames() {
		String mutuallyExclusiveRuleSection = getStringSection("<", ">");

		String[] mutuallyExclusiveRuleNames = mutuallyExclusiveRuleSection
				.split(", ");

		return Arrays.asList(mutuallyExclusiveRuleNames);
	}

	// Summary: Return the string part of the mutually exclusive rule section of
	// the original rule string provided in the constructor. In the example
	// above <SV, SVP>
	public String getMutuallyExclusiveRuleNamesString() {
		String mutuallyExclusiveRuleSection = getStringSection("<", ">");

		return mutuallyExclusiveRuleSection;
	}

	// Summary: Helper function to attempt to parse an int.
	private boolean tryParseInt(String value) {
		try {
			Integer.parseInt(value);

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	// Summary: Helper function to retrieve a string section from the original
	// rule string based on a left and right side bounding character.
	private String getStringSection(String leftBoundingChar,
			String rightBoundingChar) {
		int left = _ruleString.indexOf(leftBoundingChar);
		int right = _ruleString.indexOf(rightBoundingChar);

		return _ruleString.substring(left + 1, right);
	}

	// Summary: Returns the original rule string provided in the constructor of
	// this rule.
	public String getRuleString() {
		return _ruleString;
	}

	/*
	 * public void updateAvailability(String appliedRule) { Boolean
	 * appliedRuleIsMutuallyExclusive = false;
	 * 
	 * for (String mutuallyExlusiveWithMe : _mutuallyExclusiveRules) { if
	 * (appliedRule.equalsIgnoreCase(mutuallyExlusiveWithMe))
	 * appliedRuleIsMutuallyExclusive = true; }
	 * 
	 * if (appliedRuleIsMutuallyExclusive) _isAvailable = false; }
	 */

	/*
	 * public void setIsAvailable(Boolean isAvailable) { _isAvailable =
	 * isAvailable; }
	 */

	// Summary: Returns the output part of the rule in its original form, so
	// with the original variable strings in place. To get the string with its
	// variables replaced, use getSchemeOutput.
	/*
	 * public List<String> getOutput() { if (_output == null) { String
	 * outputString = getOutputString();
	 * 
	 * String[] outputStrings = outputString.split(" & ");
	 * 
	 * _output = Arrays.asList(outputStrings); }
	 * 
	 * return _output; }
	 */
}