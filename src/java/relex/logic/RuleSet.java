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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** RuleSet, for managing a collection of Rule objects. 
 * @author      Alex van der Peet <alex.van.der.peet@gmail.com>
 * @version     1.0                 (current version number of program)
 * @since       2013-11-08          (the version of the package this class was first added to)
 */
public class RuleSet {
	/**
	 * The rules in this rule set.
	 */
	private List<Rule> _relex2SchemeRules = new ArrayList<Rule>();

	/**
	 * Adds a Rule to this RuleSet. 
	 * @param rule
	 */
	public void addRule(Rule rule) {
		_relex2SchemeRules.add(rule);
	}

	/**
	 * @return Returns the list of rules in this ruleset.
	 */
	public List<Rule> getRules() {
		return _relex2SchemeRules;
	}
	
	/**
	 * @return The rules in this ruleset sorted by the number of criteria they have, in descending order.
	 */
	public List<Rule> getRulesByCriteriaCountDesc() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.CRITERIA_COUNT_DESC));
		
		return _relex2SchemeRules;
	}
	
	/**
	 * @return The rules in this ruleset sorted by their priority they have.
	 */
	public List<Rule> getRulesByPriority() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.PRIORITY));
		
		return _relex2SchemeRules;
	}

	/**
	 * @return The rules in this ruleset sorted first by priority and then by criteria count
	 */
	public List<Rule> getRulesByPriorityAndCriteriaCountDesc() {
		Collections.sort(_relex2SchemeRules, RuleComparator.getComparator(RuleComparator.PRIORITY, RuleComparator.CRITERIA_COUNT_DESC));
		
		return _relex2SchemeRules;
	}

	public enum RuleComparator implements Comparator<Rule> {
		PRIORITY {
			public int compare(Rule o1, Rule o2) {
				return o1.getPriority().compareTo(o2.getPriority());
			}
		},
		PRIORITY_DESC {
			public int compare(Rule o1, Rule o2) {
				return o1.getPriority().compareTo(o2.getPriority()) * -1;
			}
		},
		CRITERIA_COUNT {
			public int compare(Rule o1, Rule o2) {
				return o1.getCriteriaCount().compareTo(o2.getCriteriaCount());
			}
		},
		CRITERIA_COUNT_DESC {
			public int compare(Rule o1, Rule o2) {
				return o1.getCriteriaCount().compareTo(o2.getCriteriaCount()) * -1;
			}
		};

		public static Comparator<Rule> getComparator(
				final RuleComparator... multipleOptions) {
			return new Comparator<Rule>() {
				public int compare(Rule o1, Rule o2) {
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
}