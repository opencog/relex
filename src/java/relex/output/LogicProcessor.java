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
		public String ruleName;
		public List<Criterium> criteriums;
		
		// output
		public HashMap<String, String> valuesMap;
		public HashMap<String, String> uuidsMap;
		public Boolean maybeCheck;
				
		/**
		 * Helper class for returning both child and parent nodes.
		 */
		private static class ChildParentPair
		{
			public FeatureNode child;
			public FeatureNode parent;	
			
			public ChildParentPair(FeatureNode c, FeatureNode p)
			{
				child = c;
				parent = p;
			}
		}
		
		
		/**
		 * The main callback for binary relations.
		 */
		public Boolean BinaryHeadCB(FeatureNode parentNode) 
		{
			return ApplyToLink(parentNode, "links");
		}
		
		/**
		 * This method is not needed.
		 */
		public Boolean BinaryRelationCB(String relName,	FeatureNode srcNode, FeatureNode tgtNode)
		{
			return false;
		}

		/**
		 * This method is used for "sf-links", which is needed for Stanford-relations such as _det.
		 */
		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			if (attrName.equals("sf-links"))
				return ApplyToLink(srcNode, "sf-links");
			
			return false;
		}
		

		/**
		 * Check whether the list of criteriums can be applied to this linked relations,
		 * and store how it can be applied.
		 * @param parentNode	The parent Feature Node of the linked relations
		 * @param linkName		The type of link we are using
		 * @return				True if rule can be applied, false otherwise.
		 */
		public Boolean ApplyToLink(FeatureNode parentNode, String linkName) 
		{
			valuesMap = new HashMap<String, String>();
			uuidsMap = new HashMap<String, String>();
			maybeCheck = false;
			
			List<FeatureNode> nodes = new ArrayList<FeatureNode>();
			List<FeatureNode> parents = new ArrayList<FeatureNode>();
			
			for (Criterium thisCriterium : criteriums)
			{
				ChildParentPair foundPair = FindMatchingNode(parentNode, thisCriterium.getCriteriumLabel(), linkName, new HashSet<FeatureNode>());
				
				// if following this node will not satisfy all criteriums, give up on this node
				if (foundPair == null)
				{
					valuesMap.clear();
					uuidsMap.clear();
					return false;
				}
				
				// FIXME this check is OK now since each rule can only be applied once with the
				// current algorithm, but once that is fixed this needs to be changed too
				// XXX what if multiple nodes of same name link together and only one satisfy rule?
				if (!foundPair.child.isValued() && foundPair.child.get("member0") != null)
					nodes.add(foundPair.child.get("member0"));
				else
					nodes.add(foundPair.child);
				
				parents.add(foundPair.parent);
				
				// special treatment for maybe-rule where different words can be matched
				if (ruleName.compareTo("MAYBE") == 0)
				{
					ArrayList<String> sVar = new ArrayList<String>();
					ScopeVariables s = new ScopeVariables ();
					sVar = s.loadVarScope();
					int i = 0;
					
					FeatureNode justAdded = nodes.get(nodes.size() - 1);
					
					while (i < sVar.size() && !maybeCheck)
					{
						if (!justAdded.isValued() && justAdded.get("name").getValue().compareTo(sVar.get(i)) !=  0)
						    i++;
						else
							maybeCheck = true;
					}
				}
				
				// check and add first variable name, and handle constants
				if (!valuesMap.containsKey(thisCriterium.getFirstVariableName()))
				{
					uuidsMap.put(thisCriterium.getFirstVariableName(), null);
					
					if (thisCriterium.getFirstVariableName().charAt(0) == '$')
						valuesMap.put(thisCriterium.getFirstVariableName(), null);
					else
						valuesMap.put(thisCriterium.getFirstVariableName(), thisCriterium.getFirstVariableName());
				}
				
				// check and add second variable name, and handle constants
				if (!valuesMap.containsKey(thisCriterium.getSecondVariableName()))
				{
					uuidsMap.put(thisCriterium.getSecondVariableName(),  null);
					
					if (thisCriterium.getSecondVariableName().charAt(0) == '$')
						valuesMap.put(thisCriterium.getSecondVariableName(), null);
					else
						valuesMap.put(thisCriterium.getSecondVariableName(), thisCriterium.getSecondVariableName());
				}
			}

			Iterator<Criterium> iterCriteriums = criteriums.iterator();
			Iterator<FeatureNode> iterNodes = nodes.iterator();
			Iterator<FeatureNode> iterParents = parents.iterator();
			Boolean allMatched = true;
			
			// check the actual values
			while (iterCriteriums.hasNext() && iterNodes.hasNext() && iterParents.hasNext())
			{
				Criterium thisCriterium = iterCriteriums.next();
				FeatureNode thisNode = iterNodes.next();
				FeatureNode thisParent = iterParents.next();
								
				// if a variable already has a value, check it against the value at the node, else assign it
				if (valuesMap.get(thisCriterium.getFirstVariableName()) != null)
				{
					// the first variable will always be the 'name' of the parent node
					// XXX can also add checks to UUID, but would requires more logic for constants
					if (!valuesMap.get(thisCriterium.getFirstVariableName()).equals(thisParent.get("name").getValue()))
					{
						allMatched = false;
						break;
					}
				}
				else
				{
					valuesMap.put(thisCriterium.getFirstVariableName(), thisParent.get("name").getValue());
					uuidsMap.put(thisCriterium.getFirstVariableName(), thisParent.get("nameSource").get("uuid").getValue());
				}
				
				// check the 2nd variable of this criterium
				if (valuesMap.get(thisCriterium.getSecondVariableName()) != null)
				{
					// the second value is at different place depends on whether the node is valued
					if (!(thisNode.isValued() && valuesMap.get(thisCriterium.getSecondVariableName()).equals(thisNode.getValue()))
							&& !(!thisNode.isValued() && valuesMap.get(thisCriterium.getSecondVariableName()).equals(thisNode.get("name").getValue())))
					{
						allMatched = false;
						break;
					}
				}
				else
				{
					if (thisNode.isValued())
						valuesMap.put(thisCriterium.getSecondVariableName(), thisNode.getValue());
					else
					{
						valuesMap.put(thisCriterium.getSecondVariableName(), thisNode.get("name").getValue());
						uuidsMap.put(thisCriterium.getSecondVariableName(), thisNode.get("nameSource").get("uuid").getValue());
					}
				}
			}
			
			if (allMatched)
				return true;
			
			valuesMap.clear();
			uuidsMap.clear();
			
			return false;
		}
		
		
		/**
		 * Recursive method for finding the child/parent pair following the links node.
		 * 
		 * XXX RelationForeach is already going through the Feature Structure, so it might
		 * be better to let it do the job instead of writing another recursive walker here.
		 * 
		 * @param parentNode		The parent of the current recursion level.
		 * @param name				The name of the node we want to find.
		 * @param linkName			The name of the link node we follow.
		 * @param alreadyVisited	For complex sentence it is possible to have loops.
		 * @return					The child/parent FeatureNode pair.
		 */
		private ChildParentPair FindMatchingNode(FeatureNode parentNode, String name, String linkName, HashSet<FeatureNode> alreadyVisited)
		{
			if (alreadyVisited.contains(parentNode))
				return null;

			alreadyVisited.add(parentNode);
			
			if (parentNode.isValued())
				return null;
			
			FeatureNode node = parentNode.get(name);
			
			if (node != null)
				return new ChildParentPair(node, parentNode);
			
			FeatureNode linksNode = parentNode.get(linkName);
			
			if (linksNode != null)
			{
				if (linksNode.get(name) != null)
					return new ChildParentPair(linksNode.get(name), parentNode);
				
				// follow deeper into the links
				for (String linkToName : linksNode.getFeatureNames())
				{
					FeatureNode subNode = linksNode.get(linkToName);
					ChildParentPair pair = FindMatchingNode(subNode, name, linkName, alreadyVisited);
					
					if (pair != null)
						return pair;
				}
			}
			
			return null;
		}
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
			RuleChecker rcheck = new RuleChecker();
			rcheck.ruleName = relexRule.getName();
			rcheck.criteriums = relexRule.getCriteria();

			// if rule can be applied
			if (RelationForeach.foreach(rootNode, rcheck))
			{
				if (bVerboseMode)
				{
					System.out.println("  All criteriums satisfied.");
				
					for (String key: rcheck.valuesMap.keySet())
					{
			            String value = rcheck.valuesMap.get(key);
			            String uuid = rcheck.uuidsMap.get(key);
			            System.out.println("    " + key + ", " + value + ", " + uuid);  
			        }
				}
				
				// actually write the matched values into the rule
				for (Criterium thisCriterium : relexRule.getCriteria())
				{
					String name1 = thisCriterium.getFirstVariableName();
					String name2 = thisCriterium.getSecondVariableName();
							
					thisCriterium.setVariableValue(name1, rcheck.valuesMap.get(name1));
					thisCriterium.setVariableValueUUID(name1, rcheck.uuidsMap.get(name1));
					thisCriterium.setVariableValue(name2, rcheck.valuesMap.get(name2));
					thisCriterium.setVariableValueUUID(name2, rcheck.uuidsMap.get(name2));
				}
			}			
			

			if (relexRule.getAllCriteriaSatisfied())
			{
				if (bVerboseMode)
					System.out.println("   All criteria for rule '" + relexRule.getName() + "' satisfied, scheme output: " + relexRule.getSchemeOutput());

				bResult = true;
				if (relexRule.getName().compareTo("MAYBE") == 0 && !rcheck.maybeCheck)
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
	 *    and contains the established values for the variable criteria.
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


		// FIXME This is going through rules first, then relations, which might be wrong.
		// Should try to go into relation first, then see which rules (multiple) can be applied.
		// This would allow the same rule (such as tense) to be applied to multiple word in the sentence.
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
