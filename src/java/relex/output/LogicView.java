package relex.output;

import relex.feature.FeatureNode;
import relex.logic.*;
import relex.ParsedSentence;

// Description: Class based on SimpleView, to allow similar calling from relex.relationExtractor.

public class LogicView {

	// Loader for the rules to be loaded into ReLex2LogicRules from the supplied text file.
	private ReLex2LogicRuleLoader _relex2LogicRuleLoader = new ReLex2LogicRuleLoader();

	// Summary: Loads the ReLex2LogicRules from the rule file.
	public void loadRules(String ruleFileName) {
		_relex2LogicRuleLoader.loadRules(ruleFileName);
	}

	// Summary: Main function, applies the loaded rules to the parsed sentence.
	public String printRelationsNew(ParsedSentence parse) {
		FeatureNode root = parse.getLeft();

		FeatureNode headSet = new FeatureNode();
		headSet.set("head", root.get("head"));
		headSet.set("background", root.get("background"));

		ReLex2LogicRuleSet relexRuleSet = _relex2LogicRuleLoader.getFreshRuleSet();

		LogicProcessor ruleProcessor = new LogicProcessor(relexRuleSet);

		String schemeOutput = ruleProcessor.applyRulesToParse(headSet);

		return schemeOutput;
	}

	// Summary: Dev function for printing the children of a node recursively,
	// not required for ReLex2Logic to work.
/*	private static void printKids(FeatureNode fn, int callerLevel) {
		for (String key : fn.getFeatureNames()) {
			FeatureNode fnKid = fn.get(key);

			if (fnKid.isValued()) {
				System.out.println(callerLevel + " - Key: " + key + ", Value: " + fn.getValue());
			} else {
				int newCallerLevel = callerLevel + 1;
				printKids(fnKid, newCallerLevel);
			}
		}
	}*/

	// Summary: Dev function for printing the features of a node, not required for ReLex2Logic to work.
	/*public static void printFeatureNodeDetails(FeatureNode nodeToPrint) {
		if (nodeToPrint.isValued()) {
			System.out.println("Printing details for FeatureNode '" + nodeToPrint.getValue() + "'...");
		} else {
			System.out.println("Printing details for FeatureNode unnamed...");

			for (String strFeatureName : nodeToPrint.getFeatureNames()) {
				System.out.println("It has a feature called '" + strFeatureName + "'");
			}
		}
	}*/

}
