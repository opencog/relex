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

import relex.feature.FeatureNode;
import relex.logic.Loader;
import relex.logic.RuleSet;
import relex.ParsedSentence;

/**
 * @author      Alex van der Peet <alex.van.der.peet@gmail.com>
 * @version     1.0                 (current version number of program)
 * @since       2013-11-08          (the version of the package this class was first added to)
 */
public class LogicView
{
	/**
	 * Loader for the rules to be loaded into ReLex2LogicRules from the supplied text file.
	 */
	private Loader _relex2LogicRuleLoader = new Loader();

	/**
	 * Loads the ReLex2LogicRules from the rule file.
	 * @see relex.logic.Loader
	 */
	public void loadRules()
	{
		String ruleFileName = System.getProperty("relex.orfile");
		if (ruleFileName == null)
		{
			ruleFileName = "./data/relex2logic-rules.txt";
		}

		java.io.File f = new java.io.File(ruleFileName);
		if (f.exists())
		{
			// Print RelEx-2-Logic output
			_relex2LogicRuleLoader.loadRules(ruleFileName);
		}
		else
		{
			throw new RuntimeException("Rule file could not be found / does not exist (" + ruleFileName + ")");
		}
	}

	/**
	 * Main function, applies the loaded rules to the parsed sentence.
	 * @param parse The ParsedSentence provided by ReLex
	 * @return The Scheme output as rewritten by the LogicProcessor.
	 */
	public String printRelationsNew(ParsedSentence parse)
	{
		FeatureNode root = parse.getLeft();

		RuleSet relexRuleSet = _relex2LogicRuleLoader.getFreshRuleSet();

		LogicProcessor ruleProcessor = new LogicProcessor(relexRuleSet);

		String schemeOutput = ruleProcessor.applyRulesToParse(root);
		String parseNode = "(ParseNode \"" + parse.getIDString() + "\")";

		// replace sentence_index to reference this parse
		schemeOutput = schemeOutput.replaceAll("sentence_index", parseNode);

		// append the scheme function for post-processing markers
		// TODO makes hypergraphs deletion works with SuReal
		schemeOutput = schemeOutput.concat("(r2l-marker-processing)\n");

		// TODO integrate the following when the representation is finalized
		// schemeOutput = schemeOutput.concat("(create-abstract-version " + parseNode + ")");

		return schemeOutput;
	}
}
