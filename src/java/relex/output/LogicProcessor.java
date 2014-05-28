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
 * @version     1.0                 (current version number of program)
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
			public String relation;
			public FeatureNode child;
			public FeatureNode parent;

			public ChildParentPair(String r, FeatureNode c, FeatureNode p)
			{
				relation = r;
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
		 * This method is not needed.
		 */
		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
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

			List<ChildParentPair> foundPairs = new ArrayList<ChildParentPair>();
			List<Criterium> foundCriteriums = new ArrayList<Criterium>();

			// find all criteriums that can be matched on this node and its immediate links,
			// and build the child/parent pairs
			matchCriteriums(parentNode, criteriums, foundPairs, foundCriteriums);

			// if no criteriums matched, maybe subsequent node will satisfy some criteria
			if (foundPairs.size() == 0)
			{
				if (!parentNode.isValued())
				{
					FeatureNode linksNode = parentNode.get("links");

					if (linksNode != null)
					{
						// follow deeper into the links
						for (String linkToName : linksNode.getFeatureNames())
						{
							FeatureNode subNode = linksNode.get(linkToName);
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
			visitedNodes.remove(parentNode);
		}

		/**
		 * Helper method to find criteriums that can be satisfied by the current node and its immediate links.
		 *
		 * @param parentNode       The parent node we are looking at
		 * @param criteriums       The list of criteriums to check
		 * @param foundPairs       Returns all child/parent nodes pair that are matched to a criterium
		 * @param foundCriteriums  Returns all criteriums that got matched
		 */
		private void matchCriteriums(FeatureNode parentNode, HashSet<Criterium> criteriums, List<ChildParentPair> foundPairs, List<Criterium> foundCriteriums)
		{
			// find all criteriums that can be satisfied by the current node and its immediate links
			for (Criterium thisCriterium : criteriums)
			{
				if (parentNode.isValued())
					break;

				String attrName = thisCriterium.getCriteriumLabel();
				FeatureNode node = parentNode.get(attrName);

				if (node != null)
				{
					foundPairs.add(new ChildParentPair(attrName, node, parentNode));
					foundCriteriums.add(thisCriterium);
				}

				FeatureNode linksNode = parentNode.get("links");

				if (linksNode != null)
				{
					if (linksNode.get(attrName) != null)
					{
						foundPairs.add(new ChildParentPair(attrName, linksNode.get(attrName), parentNode));
						foundCriteriums.add(thisCriterium);
					}
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
					ChildParentPair newPair = new ChildParentPair(pairs.get(depth).relation, memberNode, pairs.get(depth).parent);

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
					if (pair.relation.equals(thisCriterium.getCriteriumLabel()))
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
					if (!ruleResult.valuesMap.get(thisCriterium.getFirstVariableName()).equals(thisParent.get("name").getValue()))
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
					// the second value is at different place depends on whether the node is valued
					if (!(thisNode.isValued() && ruleResult.valuesMap.get(thisCriterium.getSecondVariableName()).equals(thisNode.getValue()))
							&& !(!thisNode.isValued() && ruleResult.valuesMap.get(thisCriterium.getSecondVariableName()).equals(thisNode.get("name").getValue())))
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
		List<Rule> ruleSet = _relex2LogicRuleSet.getRulesByCriteriaCountDesc();

		RuleChecker rc = new RuleChecker();
		rc.orderedRulesSet = ruleSet;

		RelationForeach.foreach(rootNode, rc);

		return rc.schemeBuilder.toString();
	}

	/**
	 * Returns the value of the name feature of the head.
	 * @param rootNode The root of the dependency graph.
	 * @return
	 */
	@Deprecated
	private String getHeadNameValue(FeatureNode rootNode)
	{
		String headNameValue = "";

		FeatureNode headNode = rootNode.get("head");

		if (headNode != null)
		{
			FeatureNode nameNode = headNode.get("name");

			if (nameNode != null)
			{
				if (nameNode.isValued())
					headNameValue = nameNode.getValue();
			}
		}
		return headNameValue;
	}

	/**
	 * Returns the value of the uuid feature of the nameSource Node.
	 * @param rootNode The root of the dependency graph.
	 * @return the uuid value
	 */
	@Deprecated
	private String getNameSourceUUIDValue(FeatureNode rootNode)
	{
		String uuidValue = "";

		FeatureNode nameSourceNode = rootNode.get("nameSource");

		if (nameSourceNode != null)
		{
			FeatureNode uuidNode = nameSourceNode.get("uuid");

			if (uuidNode != null)
			{
				if (uuidNode.isValued())
					uuidValue = uuidNode.getValue();
			}
		}
		return uuidValue;
	}

	/**
	 * Finds a node based on it having a node within it's feature 'links' that matches childLinkName.
	 * @param nodeToSearchThrough The FeatureNode from which to begin the search.
	 * @param childLinkName The name of the link that the returned nodes should posess.
	 * @param alreadyVisited HashSet to prevent revisiting of nodes that have already been visited.
	 * @param alreadyFound List to prevent adding of nodes that have already been added to the resultset.
	 * @return A List<FeatureNode> that match the provided criteria.
	 */
	@Deprecated
	public static List<FeatureNode> findFeatureNodeByChildLinkName(
		FeatureNode nodeToSearchThrough,
		String childLinkName,
		HashSet<FeatureNode> alreadyVisited,
		List<FeatureNode> alreadyFound)
	{
		if (alreadyFound == null)
			alreadyFound = new ArrayList<FeatureNode>();

		if (alreadyVisited == null)
			alreadyVisited = new HashSet<FeatureNode>();

		if (alreadyVisited.contains(nodeToSearchThrough)) {
			return alreadyFound;
		} else {
			alreadyVisited.add(nodeToSearchThrough);

			// Is any of your children the node I want?
			for (String key : nodeToSearchThrough.getFeatureNames()) {
				if (key.equals("links")) {
					for (String linkKey : nodeToSearchThrough.get("links").getFeatureNames()) {
						if (linkKey.equals(childLinkName)) {
							if (!alreadyFound.contains(nodeToSearchThrough)) {
								alreadyFound.add(nodeToSearchThrough);
							}
						}
					}
				}
			}

			// Ask the kids
			for (String key : nodeToSearchThrough.getFeatureNames()) {
				FeatureNode childNode = nodeToSearchThrough.get(key);

				if (!childNode.isValued()) {
					alreadyFound = findFeatureNodeByChildLinkName(childNode, childLinkName, alreadyVisited, alreadyFound);
				}
			}
		}
		return alreadyFound;
	}

	/**
	 * Searches the passed nodeToSearchThrough for one or more nodes with name 'name'.
	 * @param nodeToSearchThrough The FeatureNode from which to begin the search.
	 * @param name The name of the node to find.
	 * @param alreadyVisited A list of nodes that has already been visited to avoid infinite recursion.
	 * @param alreadyFound List to prevent adding of nodes that have already been added to the resultset.
	 * @return A List<FeatureNode> that match the provided criteria.
	 */
	@Deprecated
	public static List<FeatureNode> findFeatureNodes(
		FeatureNode nodeToSearchThrough,
		String name,
		HashSet<FeatureNode> alreadyVisited,
		List<FeatureNode> alreadyFound)
	{
		if (alreadyFound == null)
			alreadyFound = new ArrayList<FeatureNode>();

		if (alreadyVisited == null)
			alreadyVisited = new HashSet<FeatureNode>();

		if (alreadyVisited.contains(nodeToSearchThrough)) {
			return alreadyFound;
		} else {
			alreadyVisited.add(nodeToSearchThrough);

			// Is any of your children the node I want?
			for (String key : nodeToSearchThrough.getFeatureNames()) {
				if (key.equals(name)) {
					FeatureNode foundNode = nodeToSearchThrough.get(key);

					if (!alreadyFound.contains(foundNode)) {
						alreadyFound.add(foundNode);
					}
				}
			}

			// Ask the kids
			for (String key : nodeToSearchThrough.getFeatureNames()) {
				FeatureNode childNode = nodeToSearchThrough.get(key);

				if (!childNode.isValued()) {
					alreadyFound = findFeatureNodes(childNode, name, alreadyVisited, alreadyFound);
				}
			}
		}
		return alreadyFound;
	}

	/**
	 *
	 * @param nodeToSearchThrough The FeatureNode from which to begin the search.
	 * @param name The name of the node to find.
	 * @param alreadyVisited A list of nodes that has already been visited to avoid infinite recursion.
	 * @return The node, if it exists, that matches the provided name.
	 */
	@Deprecated
	public static FeatureNode findFeatureNode(
		FeatureNode nodeToSearchThrough,
		String name,
		HashSet<FeatureNode> alreadyVisited)
	{
		FeatureNode foundNode = null;
		if (alreadyVisited == null)
			alreadyVisited = new HashSet<FeatureNode>();

		if (alreadyVisited.contains(nodeToSearchThrough)) {
			return foundNode;
		} else {
			alreadyVisited.add(nodeToSearchThrough);

			// Is any of your children the node I want?
			for (String key : nodeToSearchThrough.getFeatureNames()) {
				if (foundNode == null) {
					if (key.equals(name)) {
						foundNode = nodeToSearchThrough.get(key);
					}
				} else {
					continue;
				}
			}

			if (foundNode == null) {
				// Ask the kids
				for (String key : nodeToSearchThrough.getFeatureNames()) {
					if (foundNode == null) {
						FeatureNode childNode = nodeToSearchThrough.get(key);

						if (!childNode.isValued()) {
							foundNode = findFeatureNode(childNode, name, alreadyVisited);
						}
					} else {
						continue;
					}
				}
			}
		}
		return foundNode;
	}

	/**
	 * Grounds the variable of the Criterium.
	 * @param rootNode The FeatureNode from which other FeatureNode are derived.
	 * @param foundNode One of the FeatureNodes that satisfies Citerium.
	 * @param relexRule The rule from which the Criterium is derived.
	 * @param ruleCriterium The Criterium whose variables are being grounded.
	 * @return A boolean value used to denote the existance of a restricted-scope variable in the rule.
	 */
	@Deprecated
	private Boolean groundRuleCriterium(
		FeatureNode rootNode,
		FeatureNode foundNode,
		Rule relexRule,
		Criterium ruleCriterium)
	{
		Boolean flag = false;
		if (!foundNode.isValued())
		{
			if (foundNode.getFeatureNames().contains("name"))
			{
				if (bVerboseMode)
					System.out.println("   Its 'name' is '" + foundNode.get("name") + "'");

				if(relexRule.getName().compareTo("MAYBE")==0)
				{
					ArrayList<String> sVar = new ArrayList<String>();
					ScopeVariables s = new ScopeVariables ();
					sVar = s.loadVarScope();
					int i =0;
					while(i < sVar.size() && !flag)
					{
						if((foundNode.get("name").getValue().compareTo(sVar.get(i)))!= 0)
						    i++;
						else
						    flag = true;
					}
				}

				String secondVariableName = ruleCriterium.getSecondVariableName();
				String secondVariableValue = new String();
				if(secondVariableName.substring(0, 1).equals("$"))
					secondVariableValue = foundNode.get("name").getValue();
				else
					secondVariableValue = secondVariableName;
				String secondVariableUUID = getNameSourceUUIDValue(foundNode);

				if (bVerboseMode)
					System.out.println("   I just recorded the value of '" + secondVariableName + "' to be '" + secondVariableValue + "'");

				String firstVariableName = ruleCriterium.getFirstVariableName();
				String firstVariableValue = getHeadNameValue(rootNode);
				String firstVariableUUID = getNameSourceUUIDValue(rootNode.get("head"));
				List<FeatureNode> suitableParents = findFeatureNodeByChildLinkName(rootNode, ruleCriterium.getCriteriumLabel(), null, null);

				System.out.println("Originally start with " + firstVariableValue + ", " + firstVariableUUID);

				for (FeatureNode suitableParent : suitableParents)
				{
					if(suitableParent.get("links").get(ruleCriterium.getCriteriumLabel()).get("name").getValue() == secondVariableValue)
					{
						firstVariableValue = suitableParent.get("name").getValue();
						firstVariableUUID = suitableParent.get("nameSource").get("uuid").getValue();

						System.out.println("Replacing with " + firstVariableValue + ", " + firstVariableUUID);
					}

				}

				if (bVerboseMode)
					System.out.println("   I just recorded the value of '" + firstVariableName + "' to be '" + firstVariableValue + "'");

				ruleCriterium.setVariableValue(firstVariableName, firstVariableValue);
				ruleCriterium.setVariableValueUUID(firstVariableName, firstVariableUUID);
				ruleCriterium.setVariableValue(secondVariableName, secondVariableValue);
				ruleCriterium.setVariableValueUUID(secondVariableName, secondVariableUUID);
			}
		}
		else
		{
			if (bVerboseMode)
				System.out.println("   It is valued, the value is '" + foundNode.getValue() + "'");

			if (ruleCriterium.getSecondVariableName().equals(foundNode.getValue()))
			{
				if (bVerboseMode)
					System.out.println("   This value matches the one specified in the rule!");

				ruleCriterium.setVariableValue(ruleCriterium.getFirstVariableName(), getHeadNameValue(rootNode));
				ruleCriterium.setVariableValue(foundNode.getValue(), foundNode.getValue());
			}
		}
		return flag;
	}

	/**
	 * Traverses the parse graph and return FeatureNodes that can help ground
	 * the criteria of the RelEx rule.
	 * @param rootNode The FeatureNode from which other FeatureNode are derived.
	 * @param relexRule The rule for which FeatureNodes are extracted for.
	 * @return The mapping from a Criterium-string to a list of FeatureNodes
	 *	that can ground the variables of a Criterium constructed from
	 *	the Criterium-string.
	 */
	@Deprecated
	public HashMap<String, List<FeatureNode>> getCriteriaFeatureNodes(
		FeatureNode rootNode,
		Rule relexRule)
	{
		HashMap<String, List<FeatureNode>> criteriaFeatureNodes = new HashMap<String, List<FeatureNode>>();

		for (Criterium ruleCriterium: relexRule.getCriteria())
		{
			List<FeatureNode> criteriumFeatureNodes = findFeatureNodes(rootNode, ruleCriterium.getCriteriumLabel(), null, null);
			List<FeatureNode> filteredFeatureNodes = new ArrayList<FeatureNode>();

			for(FeatureNode foundNode : criteriumFeatureNodes)
			{
				Criterium aCriterium = new Criterium(ruleCriterium.getCriteriumString());
				groundRuleCriterium(rootNode, foundNode, relexRule, aCriterium);

				if(aCriterium.getAllVariablesSatisfied())
					filteredFeatureNodes.add(foundNode);
			}

			criteriaFeatureNodes.put(ruleCriterium.getCriteriumString(), filteredFeatureNodes);
		}

		return criteriaFeatureNodes;
	}
}
