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
package relex.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Iterator;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;
import relex.logic.Rule;
import relex.logic.Criterium;
import relex.logic.RuleSet;

/** LogicProcessor, for applying a relex.logic.RuleSet to a sentence parse.
 * @author      Alex van der Peet <alex.van.der.peet@gmail.com>
 * @author      William Ma <https://github.com/williampma>
 * @version     2.0                 (current version number of program)
 * @since       2013-11-08          (the version of the package this class was first added to)
 */
public class LogicProcessor
{
	/**
	 * Relex2LogicRuleSet that contains a fresh rule set.
	 */
	private RuleSet _relex2LogicRuleSet;

	/**
	 * Boolean to control verbose output
	 */
	private Boolean bVerboseMode = false;

	/**
	 * Constructor, receives a fresh rule set.
	 * @param relex2LogicRuleSet a RuleSet to be applied.
	 */
	public LogicProcessor(RuleSet relex2LogicRuleSet)
	{
		_relex2LogicRuleSet = relex2LogicRuleSet;
	}

	/**
	 * Implement the RelationCallback to crawl the Feature Structure.
	 */
	private static class RuleChecker implements RelationCallback
	{
		// input
		public List<Rule> orderedRulesSet;

		// output
		public StringBuilder schemeBuilder;

		private List<Rule> allAppliedRules;		// keep track of all the rules that has been applied

		/**
		 * Helper class for returning both child and parent nodes.
		 */
		private static class ChildParentPair
		{
			public Criterium criterium;
			public FeatureNode child;
			public FeatureNode parent;

			public ChildParentPair(Criterium r, FeatureNode c, FeatureNode p)
			{
				criterium = r;
				child = c;
				parent = p;
			}
		}

		/**
		 * Helper class for storing the result of applying a rule.
		 */
		private static class RuleResult
		{
			public Boolean passed;
			public Boolean maybeCheck;
			public HashMap<String, String> valuesMap;
			public HashMap<String, String> uuidsMap;

			public RuleResult()
			{
				passed = false;
				maybeCheck = false;
				valuesMap = new HashMap<String, String>();
				uuidsMap = new HashMap<String, String>();
			}
		}

		public RuleChecker()
		{
			schemeBuilder = new StringBuilder();
			allAppliedRules = new ArrayList<Rule>();
		}

		/**
		 * The main callback for binary relations.
		 */
		public Boolean BinaryHeadCB(FeatureNode node)
		{
			applyRules(node);

			return false;
		}

		/**
		 * This method is not needed.
		 */
		public Boolean BinaryRelationCB(String relName,	FeatureNode srcNode, FeatureNode tgtNode)
		{
			return false;
		}

		/**
		 * For applying rules on nodes with no outgoing links.
		 */
		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			// to avoid processing the same srcNode multiple times
			if (!attrName.equals("nameSource"))
				return false;

			// skip binary relations as that is already processed
			if (srcNode.get("links") != null)
				return false;

			applyRules(srcNode);

			return false;
		}


		/**
		 * Checks which rules can be applied to the linked relations under this path, and applys them.
		 *
		 * @param startNode   The start node to begin path searching.
		 */
		public void applyRules(FeatureNode startNode)
		{
			// clone the rules set before applying because data will be written into the rule
			// and we want a clean set of rules that can be re-applied
			List<Rule> clonedRulesSet = new ArrayList<Rule>();

			for (Rule thisRule : orderedRulesSet)
			{
				clonedRulesSet.add(new Rule(thisRule.getRuleString()));
			}

			List<Rule> appliedRules = new ArrayList<Rule>();

			for (Rule thisRule: clonedRulesSet)
			{
				Boolean bNotMutuallyExclusive = true;

				for (Rule appliedRule : appliedRules)
				{
					bNotMutuallyExclusive = !thisRule.isMutuallyExclusive(appliedRule);

					if (!bNotMutuallyExclusive)
						break;
				}

				if (!bNotMutuallyExclusive)
					continue;

				// create structure to store all the times rules can be applied to this node
				List<RuleResult> results = new ArrayList<RuleResult>();

				// search all possible path starting from this node
				recursiveMatchAndApply(thisRule, startNode, new HashSet<FeatureNode>(), new HashSet<Criterium>(thisRule.getCriteria()), new Stack<ChildParentPair>(), results);

				for (RuleResult ruleResult : results)
				{
					// create another copy again since the same rule can be applied multiple times
					// even to the same node (by choosing different sub-node)
					Rule tempRule = new Rule(thisRule.getRuleString());

					if (ruleResult.passed)
					{
						// actually write the matched values into the rule
						for (Criterium thisCriterium : tempRule.getCriteria())
						{
							String name1 = thisCriterium.getFirstVariableName();
							String name2 = thisCriterium.getSecondVariableName();

							thisCriterium.setVariableValue(name1, ruleResult.valuesMap.get(name1));
							thisCriterium.setVariableValueUUID(name1, ruleResult.uuidsMap.get(name1));
							thisCriterium.setVariableValue(name2, ruleResult.valuesMap.get(name2));
							thisCriterium.setVariableValueUUID(name2, ruleResult.uuidsMap.get(name2));
						}

						if (tempRule.getAllCriteriaSatisfied())
						{
							if (tempRule.getName().compareTo("MAYBE") != 0 || ruleResult.maybeCheck)
							{
								Boolean applied = false;

								// dumb way to check this rule against all others and make sure the exact same one was not created
								for (Rule otherRule : allAppliedRules)
								{
									if (tempRule.getSchemeOutput().equals(otherRule.getSchemeOutput()))
									{
										applied = true;
										break;
									}
								}

								if (!applied)
								{
									// apply the rule
									String schemeOutput = tempRule.getSchemeOutput();
									schemeBuilder.append(schemeOutput);
									schemeBuilder.append("\n");

									allAppliedRules.add(tempRule);
								}
							}

							appliedRules.add(tempRule);
						}
					}
				}
			}
		}

		/**
		 * Recursive method to examine all possible linked path under a node, and check/apply the rule
		 * as many times as possible.
		 *
		 * eg. [word1 [links [rel1 [member0 word2 [links [rel5 ...
		 *                         [member1 ...
		 *                         ....
		 *                   [rel2 [member0 ...
		 *                         ....
		 *                   [rel3 [member0 ...
		 *                         ....
		 *                   [rel4 wordN [links ...
		 *
		 * We would want to examine paths such as:
		 *
		 *     word1->rel1->member0->word2->rel5->...
		 *     word1->rel1->member1->...
		 *     word1->rel2->member0->...
		 *     word1->rel3->member0->...
		 *     word1->rel4->...
		 *
		 * If we have 2 rules:
		 *
		 *     rule1: (rel1($a, $b) & rel2($a, $c) & rel3($a, $d) & rel4($a, $e)
		 *     rule2: (rel1($a, $b) & rel2($a, $c) & rel3($a, $d) & rel4($a, $e) & rel5($b, $f)
		 *
		 * we want to make sure all possible combination of member# are checked for matches and also
		 * all possible path formed by choosing each combination of members.
		 *
		 * @param rule           The current rule we are checking
		 * @param parentNode     The parent node at the current recursion level
		 * @param visitedNodes   Parent nodes visited from previous recursion level
		 * @param criteriums     The remaining criteriums that has not been matched yet
		 * @param matchedPairs   The matched nodes collected from previous recursion level
		 * @param results        All possible results set
		 */
		private void recursiveMatchAndApply(
				Rule rule,
				FeatureNode parentNode,
				HashSet<FeatureNode> visitedNodes,
				HashSet<Criterium> criteriums,
				Stack<ChildParentPair> matchedPairs,
				List<RuleResult> results)
		{
			if (visitedNodes.contains(parentNode))
				return;

			visitedNodes.add(parentNode);

			// base case of the recursion, all criteriums matched
			if (criteriums.isEmpty())
			{
				RuleResult newResult = checkValues(rule, matchedPairs);
				results.add(newResult);
				visitedNodes.remove(parentNode);
				return;
			}

			List<List<ChildParentPair>> foundPairsList = new ArrayList<List<ChildParentPair>>();
			List<List<Criterium>> foundCriteriumsList = new ArrayList<List<Criterium>>();

			// find all criteriums that can be matched on this node and its immediate links,
			// and build the child/parent pairs, in all possible combinations
			matchCriteriums(parentNode, criteriums, foundPairsList, foundCriteriumsList);

			// if no criteriums matched, maybe subsequent node will satisfy some criteria
			if (foundPairsList.size() == 0)
			{
				// only continue if the initial parent node satisfies some criteria
				if (matchedPairs.size() > 0 && !parentNode.isValued())
				{
					FeatureNode linksNode = parentNode.get("links");

					if (linksNode != null)
					{
						// follow deeper into the links
						for (String linkToName : linksNode.getFeatureNames())
						{
							FeatureNode subNode = linksNode.get(linkToName);

							// to handle valued node like SIG in links (which appears for that-clause)
							if (subNode.isValued())
								continue;

							FeatureNode memberNode = subNode.get("member0");

							if (memberNode != null)
							{
								Integer n = 0;
								while (memberNode != null)
								{
									recursiveMatchAndApply(rule, memberNode, visitedNodes, criteriums, matchedPairs, results);

									n++;
									String memberName = "member" + n.toString();
									memberNode = subNode.get(memberName);
								}
							}
							else
							{
								recursiveMatchAndApply(rule, subNode, visitedNodes, criteriums, matchedPairs, results);
							}
						}
					}
				}

				visitedNodes.remove(parentNode);
				return;
			}

			Iterator<List<ChildParentPair>> pairsListIter = foundPairsList.iterator();
			Iterator<List<Criterium>> criteriumsListIter = foundCriteriumsList.iterator();

			// check all combinations of unique match generated from matchCriteriums
			while (pairsListIter.hasNext() && criteriumsListIter.hasNext())
			{
				List<ChildParentPair> foundPairs = pairsListIter.next();
				List<Criterium> foundCriteriums = criteriumsListIter.next();

				// criteriums matched, remove them all for the next level
				criteriums.removeAll(foundCriteriums);

				// generate all combinations between members of different matched links
				// ie. [word1 [links [rel1 [member0 word2 [links [rel5 ...
				//                         [member1 ...
				//                         ....
				//                   [rel2 [member0 ...
				//                         ....
				//                   [rel3 [member0 ...
				//                         ....
				//                   [rel4 wordN [links ...
				// needs <rel1 member0, rel2 member0, rel3 member0, rel4>,
				//       <rel1 member0, rel2 member0, rel3 member1, rel4> ... etc.
				List<List<ChildParentPair>> allComb = new ArrayList<List<ChildParentPair>>();
				generateAllComb(foundPairs, 0, allComb, new Stack<ChildParentPair>());

				// for each combination, search deeper for more criteriums
				for (List<ChildParentPair> pairs : allComb)
				{
					for (ChildParentPair pair : pairs)
						matchedPairs.push(pair);

					// pick one node and search deeper, and do this for all nodes in this combination
					for (ChildParentPair pair : pairs)
						recursiveMatchAndApply(rule, pair.child, visitedNodes, criteriums, matchedPairs, results);

					for (int i = 0; i < pairs.size(); i++)
						matchedPairs.pop();
				}

				// add the criteriums back in, since the previous level might match
				// the same criteriums with a different path
				criteriums.addAll(foundCriteriums);
			}

			visitedNodes.remove(parentNode);
		}

		/**
		 * Helper method to find criteriums that can be satisfied by the current node and its immediate links.
		 *
		 * @param parentNode           The parent node we are looking at
		 * @param criteriums           The list of criteriums to check
		 * @param foundPairsList       Returns all child/parent nodes pair that are matched to a criterium, in various combinations
		 * @param foundCriteriumsList  Returns all criteriums that got matched, correspond to the child/parent list
		 */
		private void matchCriteriums(FeatureNode parentNode, HashSet<Criterium> criteriums, List<List<ChildParentPair>> foundPairsList, List<List<Criterium>> foundCriteriumsList)
		{
			HashSet<String> nameSet = new HashSet<String>();

			List<ChildParentPair> foundPairs = new ArrayList<ChildParentPair>();

			// find all criteriums that can be satisfied by the current node and its immediate links
			for (Criterium thisCriterium : criteriums)
			{
				if (parentNode.isValued())
					break;

				String attrName = thisCriterium.getCriteriumLabel();
				FeatureNode node = parentNode.get(attrName);	// check local stuff such as "tense"

				if (node != null)
				{
					nameSet.add(attrName);
					foundPairs.add(new ChildParentPair(thisCriterium, node, parentNode));
				}

				FeatureNode linksNode = parentNode.get("links");

				if (linksNode != null)
				{
					if (linksNode.get(attrName) != null)
					{
						nameSet.add(attrName);
						foundPairs.add(new ChildParentPair(thisCriterium, linksNode.get(attrName), parentNode));
					}
				}
			}

			// since some criteriums with same name might get matched, need to find all combinations of
			// different sizes
			for (int i = 0; i < nameSet.size(); i++)
			{
				List<List<ChildParentPair>> tempPairsList = new ArrayList<List<ChildParentPair>>();

				generateAllComb(nameSet, foundPairs, tempPairsList, i + 1, new Stack<ChildParentPair>());
				foundPairsList.addAll(tempPairsList);
			}

			//  get the corresponding criterium
			for (Iterator<List<ChildParentPair>> iter1 = foundPairsList.iterator(); iter1.hasNext(); )
			{
				List<ChildParentPair> thisPairsComb = iter1.next();
				List<Criterium> thisCriteriumsComb = new ArrayList<Criterium>();

				for (Iterator<ChildParentPair> iter2 = thisPairsComb.iterator(); iter2.hasNext(); )
				{
					ChildParentPair thisPair = iter2.next();
					thisCriteriumsComb.add(thisPair.criterium);
				}

				foundCriteriumsList.add(thisCriteriumsComb);
			}
		}

		/**
		 * When we have several criteriums with the same name (eg. that($A,$B) & pos($A,$C) & pos($B,$D))
		 * and a node can satisfy it, we generate all possible combination of how this node can satisfy the rule.
		 *
		 * @param nameSet      The set of unique criteriums name matched
		 * @param foundPairs   All criteriums that can be matched
		 * @param allComb      Returns all possible combination where the criterium names will be unique
		 * @param currLevel    Current level, needed to make it possible to generate combinations that doesn't involve all matched criterium
		 * @param currComb     Store the current combination for recursion
		 */
		private void generateAllComb(HashSet<String> nameSet, List<ChildParentPair> foundPairs, List<List<ChildParentPair>> allComb, int currLevel, Stack<ChildParentPair> currComb)
		{
			// base case, all criterium names checked or
			if (currLevel == 0 || nameSet.size() == 0)
			{
				// do nothing if we have not create any combination
				if (currComb.empty())
					return;

				List<ChildParentPair> newComb = new ArrayList<ChildParentPair>();
				newComb.addAll(currComb);

				allComb.add(newComb);
				return;
			}

			// create a new nameMap, so not to interfere with loop
			HashSet<String> newSet = new HashSet<String>();
			newSet.addAll(nameSet);

			for (String name : nameSet)
			{
				// get the first key and find all Pair with that name
				//String name = nameSet.iterator().next();
				List<ChildParentPair> namePairs = new ArrayList<ChildParentPair>();

				for (ChildParentPair pair : foundPairs)
				{
					if (pair.criterium.getCriteriumLabel().equals(name))
						namePairs.add(pair);
				}

				// remove this name from future consideration
				newSet.remove(name);

				for (ChildParentPair pair : namePairs)
				{
					currComb.push(pair);
					generateAllComb(newSet, foundPairs, allComb, currLevel - 1, currComb);
					currComb.pop();
				}
			}
		}

		/**
		 * Given several nodes "links" to a parent node, each with its own set of member# nodes, generate
		 * all possible combination.
		 *
		 * ie. [word1 [links [rel1 [member0 word2 [links [rel5 ...
		 *                         [member1 ...
		 *                         ....
		 *                   [rel2 [member0 ...
		 *                         ....
		 *                   [rel3 [member0 ...
		 *                         ....
		 *                   [rel4 wordN [links ...
		 * needs <rel1 member0, rel2 member0, rel3 member0, rel4>,
		 *       <rel1 member0, rel2 member0, rel3 member1, rel4> ... etc.
		 *
		 * So this is essentially a "combination of items in a list of lists problem".
		 *
		 * @param pairs      A list of child/parent pair, each child could have multiple member#
		 * @param depth      Current depth of recursion
		 * @param allComb    Returns a list of all possible combination
		 * @param currComb   The current combination in the making
		 */
		private void generateAllComb(List<ChildParentPair> pairs, int depth, List<List<ChildParentPair>> allComb, Stack<ChildParentPair> currComb)
		{
			// base case reached
			if (depth == pairs.size())
			{
				// build a copy for currComb, since it is a reference and will get "popped"
				List<ChildParentPair> newComb = new ArrayList<ChildParentPair>();
				newComb.addAll(currComb);

				allComb.add(newComb);
				return;
			}

			// find how many members there are
			Integer numMembers = 0;

			if (!pairs.get(depth).child.isValued())
			{
				FeatureNode member = pairs.get(depth).child.get("member0");

				while (member != null)
				{
					numMembers++;
					String memberName = "member" + numMembers.toString();
					member = pairs.get(depth).child.get(memberName);
				}
			}

			// if only the lone member
			if (numMembers == 0)
			{
				currComb.push(pairs.get(depth));
				generateAllComb(pairs, depth + 1, allComb, currComb);
				currComb.pop();
			}
			else
			{
				for (Integer i = 0; i < numMembers; i++)
				{
					String memberName = "member" + i.toString();
					FeatureNode memberNode = pairs.get(depth).child.get(memberName);
					ChildParentPair newPair = new ChildParentPair(pairs.get(depth).criterium, memberNode, pairs.get(depth).parent);

					currComb.push(newPair);
					generateAllComb(pairs, depth + 1, allComb, currComb);
					currComb.pop();
				}
			}
		}

		/**
		 * Method to assign and match variables and constants in the rule.
		 *
		 * @param thisRule      The current rule we are looking at
		 * @param matchedPairs  The list of matched child/parent pairs we are considering for this rule
		 * @return              A RuleResult object
		 */
		private RuleResult checkValues(Rule thisRule, Stack<ChildParentPair> matchedPairs)
		{
			RuleResult ruleResult = new RuleResult();

			// build the list of variables
			for (Criterium thisCriterium : thisRule.getCriteria())
			{
				// check and add first variable name, and handle constants
				if (!ruleResult.valuesMap.containsKey(thisCriterium.getFirstVariableName()))
				{
					ruleResult.uuidsMap.put(thisCriterium.getFirstVariableName(), null);

					if (thisCriterium.getFirstVariableName().charAt(0) == '$')
						ruleResult.valuesMap.put(thisCriterium.getFirstVariableName(), null);
					else
						ruleResult.valuesMap.put(thisCriterium.getFirstVariableName(), thisCriterium.getFirstVariableName());
				}

				// check and add second variable name, and handle constants
				if (!ruleResult.valuesMap.containsKey(thisCriterium.getSecondVariableName()))
				{
					ruleResult.uuidsMap.put(thisCriterium.getSecondVariableName(),  null);

					if (thisCriterium.getSecondVariableName().charAt(0) == '$')
						ruleResult.valuesMap.put(thisCriterium.getSecondVariableName(), null);
					else
						ruleResult.valuesMap.put(thisCriterium.getSecondVariableName(), thisCriterium.getSecondVariableName());
				}
			}

			Boolean allMatched = true;

			for (Criterium thisCriterium : thisRule.getCriteria())
			{
				// find the pair that matched this criterium
				ChildParentPair thisPair = null;

				for (ChildParentPair pair : matchedPairs)
				{
					// reference check; find the correct pair
					if (pair.criterium == thisCriterium)
					{
						thisPair = pair;
						break;
					}
				}

				FeatureNode thisNode = thisPair.child;
				FeatureNode thisParent = thisPair.parent;

				// special treatment for maybe-rule where different words can be matched
				if (!ruleResult.maybeCheck && thisRule.getName().compareTo("MAYBE") == 0)
				{
					ArrayList<String> sVar = new ArrayList<String>();
					ScopeVariables s = new ScopeVariables ();
					sVar = s.loadVarScope();
					int i = 0;

					while (i < sVar.size() && !ruleResult.maybeCheck)
					{
						if (!thisNode.isValued() && thisNode.get("name").getValue().compareTo(sVar.get(i)) !=  0)
						    i++;
						else
							ruleResult.maybeCheck = true;
					}
				}

				// if a variable already has a value, check it against the value at the node, else assign it
				if (ruleResult.valuesMap.get(thisCriterium.getFirstVariableName()) != null)
				{
					// the first variable will always be the 'name' of the parent node
					// XXX can also add checks to UUID, but would requires more logic for constants
					if (!thisParent.get("name").getValue().matches(ruleResult.valuesMap.get(thisCriterium.getFirstVariableName())))
					{
						allMatched = false;
						break;
					}
				}
				else
				{
					ruleResult.valuesMap.put(thisCriterium.getFirstVariableName(), thisParent.get("name").getValue());
					ruleResult.uuidsMap.put(thisCriterium.getFirstVariableName(), thisParent.get("nameSource").get("uuid").getValue());
				}

				// check the 2nd variable of this criterium
				if (ruleResult.valuesMap.get(thisCriterium.getSecondVariableName()) != null)
				{
					String secondValue;

					// the second value is at different place depends on whether the node is valued
					if (thisNode.isValued())
						secondValue = thisNode.getValue();
					else
						secondValue = thisNode.get("name").getValue();

					if (!secondValue.matches(ruleResult.valuesMap.get(thisCriterium.getSecondVariableName())))
					{
						allMatched = false;
						break;
					}
				}
				else
				{
					if (thisNode.isValued())
						ruleResult.valuesMap.put(thisCriterium.getSecondVariableName(), thisNode.getValue());
					else
					{
						ruleResult.valuesMap.put(thisCriterium.getSecondVariableName(), thisNode.get("name").getValue());
						ruleResult.uuidsMap.put(thisCriterium.getSecondVariableName(), thisNode.get("nameSource").get("uuid").getValue());
					}
				}
			}

			if (allMatched)
			{
				ruleResult.passed = true;
				return ruleResult;
			}

			ruleResult.passed = false;
			return ruleResult;
		}
	}


	/**
	 * Applies the local ruleset to the dependency graph that starts with rootNode.
	 * @param rootNode The root of the dependency graph.
	 * @return
	 */
	public String applyRulesToParse(FeatureNode rootNode)
	{
		List<Rule> ruleSet = _relex2LogicRuleSet.getRulesByPriority();

		RuleChecker rc = new RuleChecker();
		rc.orderedRulesSet = ruleSet;

		RelationForeach.foreach(rootNode, rc);

		return rc.schemeBuilder.toString();
	}
}
