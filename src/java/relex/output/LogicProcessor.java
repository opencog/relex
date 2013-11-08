package relex.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import relex.feature.FeatureNode;
import relex2logic.ReLex2LogicRule;
import relex2logic.ReLex2LogicRuleCriterium;
import relex2logic.ReLex2LogicRuleSet;

// Description: Class that takes a ReLex2LogicRuleSet and applies it to a dependency graph through its root FeatureNode.
public class LogicProcessor {

	// Relex2LogicRuleSet that contains a fresh rule set.
	private ReLex2LogicRuleSet _relex2LogicRuleSet;

	// Boolean to control verbose output
	private Boolean bVerboseMode = true;

	// Summary: Constructor, receives a fresh rule set.
	public LogicProcessor(ReLex2LogicRuleSet relex2LogicRuleSet) {
		_relex2LogicRuleSet = relex2LogicRuleSet;
	}

	// Summary: Checks whether a rule can be applied, but does not apply it. It does however register the values 
	// for found candidates that match the variables in the criteria.
	// Parameter 'relexRule': the rule which should be checked for applicability.
	// Parameter 'rootNode': the FeatureNode from which to start the application search.
	// Parameter 'appliedRules': the rules which have already been applied, so that application of mutually exclusive rules is not attempted.
	public Boolean checkRuleApplicability(ReLex2LogicRule relexRule, FeatureNode rootNode, List<String> appliedRules)
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
			for (ReLex2LogicRuleCriterium ruleCriterium: relexRule.getCriteria())
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
	
	// Summary: Retrieves the Scheme output from a rule whose applicability has been established, rewriting the variables in the rule
	//          to the values that have been determined in the verification process.
	// Parameter 'ruleToApply': The rule that has been determined applicable, and contains the established values for the variable criteria.
	// Parameter 'schemeBuilder': A StringBuilder to which to append the Scheme output.
	private void applyRule(ReLex2LogicRule ruleToApply, StringBuilder schemeBuilder)
	{
		String schemeOutput = ruleToApply.getSchemeOutput();
		schemeBuilder.append(schemeOutput);
		schemeBuilder.append("\n");
	}
	
	// Summary: Applies the local ruleset to the dependency graph that starts with rootNode.
	// Parameter 'rootNode': The root of the dependency graph.
	public String applyRulesToParse(FeatureNode rootNode)
	{
		StringBuilder schemeBuilder = new StringBuilder();
		List<String> appliedRules = new ArrayList<String>();
		
		List<ReLex2LogicRule> ruleSet = _relex2LogicRuleSet.getRulesByCriteriaCountDesc();
		
		int numberOfRules = ruleSet.size();
		
		for (ReLex2LogicRule relexRule: ruleSet) {
			
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


	// Summary: Finds a node based on it having a node within it's feature
	// 'links' that matches childLinkName.
	public static List<FeatureNode> findFeatureNodeByChildLinkName(FeatureNode nodeToSearchThrough, String childLinkName, HashSet<FeatureNode> alreadyVisited,
			List<FeatureNode> alreadyFound) {
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

	// Summary: Searches the passed nodeToSearchThrough for one or more nodes
	// with name 'name'.
	// Parameter 'alreadyVisited': a list of nodes that has already been visited
	// to avoid infinite recursion.
	// Parameter 'alreadyFound': a list of nodes that has already been found to
	// avoid duplicates in the results.
	public static List<FeatureNode> findFeatureNodes(FeatureNode nodeToSearchThrough, String name, HashSet<FeatureNode> alreadyVisited,
			List<FeatureNode> alreadyFound) {
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

	// Summary: Searches the passed nodeToSearchThrough for a single node with
	// name 'name'.
	// Parameter 'alreadyVisited': a list of nodes that has already been visited
	// to avoid infinite recursion.
	public static FeatureNode findFeatureNode(FeatureNode nodeToSearchThrough, String name, HashSet<FeatureNode> alreadyVisited) {
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
