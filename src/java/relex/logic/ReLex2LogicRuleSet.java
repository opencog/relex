package relex.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Which is managed and queried through a class called Relex2SchemeRuleSet

public class ReLex2LogicRuleSet {
	// Lowest and highest priority encountered in loading the rule file
	private int _minPriority = 0;
	private int _maxPriority = 0;

	// The rules in this rule set.
	private List<ReLex2LogicRule> _relex2SchemeRules = new ArrayList<ReLex2LogicRule>();

	// Summary: Adds a rule to the rule set. Also updated the minimum and
	// maximum priority withint he ruleSet.
	public void addRule(ReLex2LogicRule reLex2LogicRule) {
		_relex2SchemeRules.add(reLex2LogicRule);

		_minPriority = Math.min(_minPriority, reLex2LogicRule.getPriority());
		_maxPriority = Math.max(_maxPriority, reLex2LogicRule.getPriority());
	}

	// Summary: Returns the list of rules in this ruleset.
	public List<ReLex2LogicRule> getRules() {
		return _relex2SchemeRules;
	}
	
	// Summary: Returns the rules in this ruleset sorted by the amount of
		// criteria they have.
	public List<ReLex2LogicRule> getRulesByCriteriaCountDesc() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.CRITERIA_COUNT_DESC));
		
		return _relex2SchemeRules;
	}
	
	public List<ReLex2LogicRule> getRulesByPriority() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.PRIORITY));
		
		return _relex2SchemeRules;
	}
	

	// Summary: Returns the rules in this ruleset sorted first by priority and
	// then by criteria count
	public List<ReLex2LogicRule> getRulesByPriorityAndCriteriaCountDesc() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.PRIORITY, RuleComparator.CRITERIA_COUNT_DESC));
		
		return _relex2SchemeRules;
	}

	public enum RuleComparator implements Comparator<ReLex2LogicRule> {
		PRIORITY {
			public int compare(ReLex2LogicRule o1, ReLex2LogicRule o2) {
				return o1.getPriority().compareTo(o2.getPriority());
			}
		},
		PRIORITY_DESC {
			public int compare(ReLex2LogicRule o1, ReLex2LogicRule o2) {
				return o1.getPriority().compareTo(o2.getPriority()) * -1;
			}
		},
		CRITERIA_COUNT {
			public int compare(ReLex2LogicRule o1, ReLex2LogicRule o2) {
				return o1.getCriteriaCount().compareTo(o2.getCriteriaCount());
			}
		},
		CRITERIA_COUNT_DESC {
			public int compare(ReLex2LogicRule o1, ReLex2LogicRule o2) {
				return o1.getCriteriaCount().compareTo(o2.getCriteriaCount()) * -1;
			}
		};

		public static Comparator<ReLex2LogicRule> getComparator(
				final RuleComparator... multipleOptions) {
			return new Comparator<ReLex2LogicRule>() {
				public int compare(ReLex2LogicRule o1, ReLex2LogicRule o2) {
					for (RuleComparator option : multipleOptions) {
						int result = option.compare(o1, o2);
						if (result != 0) {
							return result;
						}
					}
					return 0;
				}
			};
		}
	}
	

	/*public List<ReLex2LogicRule> getRulesByCriteriaCountDescOld() {

		Collections.sort(_relex2SchemeRules, new Comparator<ReLex2LogicRule>() {
			public int compare(final ReLex2LogicRule a, final ReLex2LogicRule b) {
				return a.getCriteriaCount().compareTo(b.getCriteriaCount());
			}
		});

		return _relex2SchemeRules;
	}

	// Summary: Returns the rules in this ruleset sorted by their priority
	public List<ReLex2LogicRule> getRulesByPriorityDescOld() {

		Collections.sort(_relex2SchemeRules, new Comparator<ReLex2LogicRule>() {
			public int compare(final ReLex2LogicRule a, final ReLex2LogicRule b) {
				return a.getPriority().compareTo(b.getPriority());
			}
		});

		return _relex2SchemeRules;
	}

	public List<ReLex2LogicRule> getRulesByPriorityAndCriteriaCountDescOld() {

		Collections.sort(_relex2SchemeRules, new Comparator<ReLex2LogicRule>() {
			public int compare(final ReLex2LogicRule a, final ReLex2LogicRule b) {
				return a.getPriority().compareTo(b.getPriority());
			}
		});

		return _relex2SchemeRules;
	}*/
}