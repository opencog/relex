/*
 * Copyright 2008 Novamente LLC
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
 */
package relex.frame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single basic condition of a rule
 */
class ConditionNode extends ASTNode
{
	String conditionStr;

	ConditionNode(String condStr) {
		conditionStr = condStr.trim().replace(" ","");
		children = null;
	}

	String print() {
		return conditionStr;
	}

	HashSet<String> getRelationNames() {
		//String regex = "w+\\(";
		Pattern p = Pattern.compile(".+\\(");
		Matcher m = p.matcher(conditionStr);
		if (m.find()) {
			HashSet<String> relationNames = new HashSet<String>(1);
			String name = m.group();
			name = name.substring(0,name.length()-1);
			relationNames.add(name);
			return relationNames;
		}
		return null;
	}

	/**
	 * @return true if this condition has at least one match
	 *         in the supplied relex
	 */
	boolean matchesRelex(String relex){
		//if it's a concept var definition ($var0=$Concept), then return true
		if (conditionStr.startsWith("$var") && conditionStr.contains("=")) {
			return true;
		}

		//prepare condition for regex search
		//add word boundary at the beginning to avoid partial word matches
			//(e.g., to($var0) should not match relex into(house)
		//add escapes to parens, brackets
		String regex = "\\b" + conditionStr.replace("(","\\(").replace(")","\\)")
			.replace("[","\\[").replace("]","\\]");
		//replace $varsX ($var0,$var1,$copula.) with equiv of regex wildcard
		//but exclude vars preceded by underscore (e.g., _$qVar)
		//add "$" and "%" chars for special relex variables
		regex = regex.replaceAll("(?<!_)\\$\\w+","[\\\\w\\$%]+");
		//escape any remaining $'s, (e.g., _$qVar)
		regex = regex.replace("$", "\\$");

		Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(relex);
		if (m.find()) {
			if (VERBOSE) {
				System.out.println(
				        "\nCondition match  relex: " + m.group()
						+ "\n     condition: " + conditionStr
						+ "\n     regex: " + regex);
						// + "\n     full rule: " + rule.ruleStr);
			}
			return true;
		}
		return false;
	}


	/**
	 * Creates a VarMap for each match of this condition in the
	 * relex.  Checks collective concept variables for legal 
	 * values. Stores a list of the generated VarMaps in this 
	 * node's nodeVarMapList
	 *
	 * @return false if no valid variable substitutions were found
	 */
	boolean processVariableMatch(String relex)
	{
		nodeVarMapList = null;

		// First find all the $vars (words beginning with $'s)
		// in the rule condition. Exclude vars preceded by 
		// underscore (eg, _$qVar)
		Pattern varNamePattern = Pattern.compile("(?<!_)\\$\\w+");
		Matcher varNameMatcher = varNamePattern.matcher(conditionStr);

		ArrayList<String> vars = new ArrayList<String>(2);
		while(varNameMatcher.find()) {
			vars.add(varNameMatcher.group());
		}

		// If no $vars in the condition, return true.
		if (vars.size() == 0) {
			nodeVarMapList = new VarMapList(0);
			return true;
		}

		// Check for $var=$Concept syntax.
		// Store in VarMap as $varCONCEPT => $Concept
		if (conditionStr.startsWith("$var") && conditionStr.contains("=")) {
			String varName = vars.get(0);
			String concept = vars.get(1);
			VarMap varMap = new VarMap(1);
			varMap.put(varName+"CONCEPT",concept);
			if (nodeVarMapList == null) {
				nodeVarMapList = new VarMapList();
			}
			nodeVarMapList.add(varMap);
			// This will get checked in AndNode.processVariableMatch()
			// or in the case it's enclosed in a NOT, in reconcileNegationVars()
			return true;
		}

		// Find all the relex lines that match the condition.
		// Add escapes to parens.
		String regex = conditionStr.replace("(","\\(").replace(")","\\)")
				// Escape brackets [ ].
				.replace("[", "\\[").replace("]", "\\]")
				// Replace vars with wildcard and put in groups.
				// Exclude vars preceded by underscore (eg, _$qVar).
				.replaceAll("(?<!_)\\$\\w+","([\\\\w\\$%]+)")
				// Escape any remaning $'s.
				.replace("$", "\\$");

		//println("regex: " + regex);
		// Search relex with regex.
		Pattern relexLinePattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		Matcher relexLineMatcher = relexLinePattern.matcher(relex);
		Pattern indexPattern = Pattern.compile("\\_\\d+\\z");
		Matcher indexMatcher = indexPattern.matcher("");

		//for each matching relex line
		matchingLine:
		while (relexLineMatcher.find()) {
			VarMap varMap = new VarMap(2);
			int varNum = 0;
			//for each variable
			for (String varName: vars) {
				varNum++;
				String word = relexLineMatcher.group(varNum);

				// Check if var is a concept variable
				if (!varName.startsWith("$var")) {
					// Strip off any var indexes (e.g., $Noun_2)
					indexMatcher.reset(varName);
					String varNameCheck = varName;
					if (indexMatcher.find()) {
						varNameCheck = varName.substring(0,indexMatcher.start());
					}

					if (!Rule.conceptContainsWord(varNameCheck,word)) {
						continue matchingLine;
					}
				} //end var is a concept var

				varMap.put(varName,word);
			} //end for each variable

			if (nodeVarMapList==null) {
				nodeVarMapList = new VarMapList();
			}
			nodeVarMapList.add(varMap);
		} //end for each matching relex line

		//if no successful variable matching return false
		if (nodeVarMapList==null) {
			return false;
		}
		else {
			return true;
		}
	} //end processVariableMatch()

} //end class ConditionNode

