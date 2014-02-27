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
import java.util.HashSet;
import java.util.List;

import relex.feature.FeatureNode;
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
	 * Checks whether a rule can be applied, but does not apply it.
	 * It does however register the values for found candidates that
	 * match the variables in the criteria.
	 * @param relexRule The rule which should be checked for applicability.
	 * @param rootNode The FeatureNode from which to start the application search.
	 * @param appliedRules The rules which have already been applied,
	 *        so that application of mutually exclusive rules is not attempted.
	 * @return Boolean indicating if the rule can be applied or not.
	 */
	public Boolean checkRuleApplicability(Rule relexRule, FeatureNode rootNode, List<String> appliedRules)
	{
		Boolean bResult = false;
		Boolean bNotMutuallyExclusive = true;
                Boolean flag = false;

		for (String appliedRule : appliedRules)
		{
			for (String mutuallyExclusiveRule: relexRule.getMutuallyExclusiveRuleNames())
			{
				if (appliedRule.equalsIgnoreCase(mutuallyExclusiveRule))
					bNotMutuallyExclusive = false;
			}
		}

		if (bNotMutuallyExclusive)
		{
			for (Criterium ruleCriterium: relexRule.getCriteria())
			{
				if (bVerboseMode)
					System.out.println("  Matching criterium: " + ruleCriterium.getCriteriumString() + "...");

				List<FeatureNode> criteriumFeatureNodes = findFeatureNodes(rootNode, ruleCriterium.getCriteriumLabel(), null, null);

				if (criteriumFeatureNodes != null)
				{
					for (FeatureNode foundNode : criteriumFeatureNodes)
					{
						if (bVerboseMode)
							System.out.println("   Found node '" + ruleCriterium.getCriteriumLabel() + "'");

						if (!foundNode.isValued())
						{
							if (foundNode.getFeatureNames().contains("name"))
							{
								if (bVerboseMode)
									System.out.println("   Its 'name' is '" + foundNode.get("name") + "'");
                                                                if(relexRule.getName().compareTo("maybe")==0)
                                                                    {
                                                                            
                                                                            ArrayList<String> sVar= new ArrayList<String>();
                                                                            ScopeVariables s = new ScopeVariables ();
                                                                            sVar=s.loadVarScope();
                                                                            int i =0;
                                                                            while(i<sVar.size() && !flag)
                                                                            {
                                                                                if((foundNode.get("name").getValue().compareTo(sVar.get(i)))!=0)
                                                                                i++;
                                                                                else
                                                                                flag=true;
                                                                                                                    
                                                                            }
                                                                            
                                                                           
                                                                       }

								String secondVariableName = ruleCriterium.getSecondVariableName();
								String secondVariableValue = foundNode.get("name").getValue();

								if (bVerboseMode)
									System.out.println("   I just recorded the value of '" + secondVariableName + "' to be '" + secondVariableValue + "'");

								String firstVariableName = ruleCriterium.getFirstVariableName();
								String firstVariableValue = getHeadNameValue(rootNode);

								List<FeatureNode> suitableParents = findFeatureNodeByChildLinkName(rootNode, ruleCriterium.getCriteriumLabel(), null, null);

								for (FeatureNode suitableParent : suitableParents)
								{
									firstVariableValue = suitableParent.get("name").getValue();
								}

								if (bVerboseMode)
									System.out.println("   I just recorded the value of '" + firstVariableName + "' to be '" + firstVariableValue + "'");

								ruleCriterium.setVariableValue(firstVariableName, firstVariableValue);
								ruleCriterium.setVariableValue(secondVariableName, secondVariableValue);
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
					}

				}
			}

			if (relexRule.getAllCriteriaSatisfied())
			{
				if (bVerboseMode)
					System.out.println("   All criteria for rule '" + relexRule.getName() + "' satisfied, scheme output: " + relexRule.getSchemeOutput());

				bResult = true;
                                if(relexRule.getName().compareTo("maybe")==0 && !flag)                               
                              		bResult = false; 
			}
			else
			{
				if (bVerboseMode)
					System.out.println("   Not all criteria for rule '" + relexRule.getName() + "' satisfied :(");
			}
		}
		else
		{
			System.out.println("   Cannot apply rule '" + relexRule.getName() + "' due to mutual exclusivity");
		}

		return bResult;
	}

	/**
	 * Retrieves the Scheme output from a rule whose applicability has
	 * been established, rewriting the variables in the rule to the
	 * values that have been determined in the verification process.
	 * @param ruleToApply The rule that has been determined applicable,
	 *         and contains the established values for the variable criteria.
	 * @param schemeBuilder A StringBuilder to which to append the Scheme output.
	 */
	private void applyRule(Rule ruleToApply, StringBuilder schemeBuilder)
	{
		String schemeOutput = ruleToApply.getSchemeOutput();
		schemeBuilder.append(schemeOutput);
		schemeBuilder.append("\n");
	}

	/**
	 * Applies the local ruleset to the dependency graph that starts with rootNode.
	 * @param rootNode The root of the dependency graph.
	 * @return
	 */
	public String applyRulesToParse(FeatureNode rootNode)
	{
		StringBuilder schemeBuilder = new StringBuilder();
		List<String> appliedRules = new ArrayList<String>();

		List<Rule> ruleSet = _relex2LogicRuleSet.getRulesByCriteriaCountDesc();

		for (Rule relexRule: ruleSet) {

			if (bVerboseMode)
				System.out.println("Matching rule '" + relexRule.getName() + "'...");

			if (checkRuleApplicability(relexRule, rootNode, appliedRules))
			{
				applyRule(relexRule, schemeBuilder);

				appliedRules.add(relexRule.getName());
			}
		}

		return schemeBuilder.toString();
	}

	/**
	 * Retries the value of the name feature of the head.
	 * @param rootNode The root of the dependency graph.
	 * @return
	 */
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
	 * Finds a node based on it having a node within it's feature 'links' that matches childLinkName.
	 * @param nodeToSearchThrough The FeatureNode from which to begin the search.
	 * @param childLinkName The name of the link that the returned nodes should posess.
	 * @param alreadyVisited HashSet to prevent revisiting of nodes that have already been visited.
	 * @param alreadyFound List to prevent adding of nodes that have already been added to the resultset.
	 * @return A List<FeatureNode> that match the provided criteria.
	 */
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
}
