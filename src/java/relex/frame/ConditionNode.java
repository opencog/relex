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
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single basic condition of a rule
 */
class ConditionNode extends ASTNode
{
	String conditionStr;
	Pattern regpat;
	Pattern relpat;
	Pattern varNamePattern;
	Pattern relexLinePattern;
	Pattern indexPattern;

	ConditionNode(String condStr)
	{
		conditionStr = condStr.trim().replace(" ","");
		children = null;

		//09/06/01 fabricio: changed to match the md5 uuid that contains "@" and "-"
		//09/06/14 Joel: This is wrong, the md5 uuid is not in the conditionStr which
		// this is matched against:
		//varNamePattern = Pattern.compile("(?<!_)\\$[\\w@-]+");
		//varNamePattern = Pattern.compile("((?<!_)\\$\\w+");
		// Include the constants too
		varNamePattern = Pattern.compile("((?<!_)\\$\\w+)|\\((\\w+),|,(\\w+)\\)");
		indexPattern = Pattern.compile("\\_\\d+\\z");

		// relpat is for getting relationship names
		relpat = Pattern.compile(".+\\(");

		// Prepare condition for regex search.
		// Add word boundary at the beginning to avoid partial word matches
		// (e.g., to($var0) should not match relex into(house)
		// Add escapes to parens, brackets.
		String regex = "\\b" + conditionStr.replace("(","\\(").replace(")","\\)")
			.replace("[","\\[").replace("]","\\]").replace("{","\\{").replace("}","\\}");
		

		// Replace $varsX ($var0,$var1,$copula.) with equiv of regex wildcard,
		// but exclude vars preceded by underscore (e.g., _$qVar)
		// Add "$" and "%" chars for special relex variables.
		//09/06/01 fabricio: changed to match the md5 uuid that contains "@" and "-"
		//regex = regex.replaceAll("(?<!_)\\$\\w+","[\\\\w\\$%]+");
		regex = regex.replaceAll("(?<!_)\\$\\w+","[\\\\w\\$%@-]+");

		// Escape any remaining $'s, (e.g., _$qVar)
		regex = regex.replace("$", "\\$");

		regpat = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);

		// Add escapes to parens.
		//09/06/01 fabricio: changed to match the md5 uuid that contains "@" and "-"		
		regex = conditionStr.replace("(","\\(").replace(")","\\)")
				// Escape brackets [ ].
				.replace("[", "\\[").replace("]", "\\]")
				.replace("{","\\{").replace("}","\\}")
				// Replace vars with wildcard and put in groups.
				// Exclude vars preceded by underscore (eg, _$qVar).
				.replaceAll("(?<!_)\\$\\w+","([\\\\w\\$%@-]+)")
				// Escape any remaning $'s.
				.replace("$", "\\$");
		
		// Regex to also group constants. Constants need to also be mapped, since
		// when using word instances with UUIDs, they may not necessarily map to
		// the constant directly... i.e. one of the lemmas may equal the constant.
		regex = regex.replaceAll("\\((\\w+),", "(($1),").replaceAll(",(\\w+)\\)", ",($1)\\)");

		relexLinePattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
	}

	String print() {
		return conditionStr;
	}

	HashSet<String> getRelationNames()
	{
		Matcher m = relpat.matcher(conditionStr);
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
	boolean matchesRelex(String relex, HashMap<String, String> uuidToBase)
	{
		// If it's a concept var definition ($var0=$Concept), then return true.
		if (conditionStr.startsWith("$var") && conditionStr.contains("=")) {
			return true;
		}
		String relexBase = relex;
		if (uuidToBase != null) {
			for (String uuid : uuidToBase.keySet()) {
				relexBase = relexBase.replaceAll(uuid, uuidToBase.get(uuid));
			}
		}

		Matcher m = regpat.matcher(relexBase);
		if (m.find()) {
			if (VERBOSE) {
				System.out.println(
				        "\nCondition match  relex: " + m.group()
						+ "\n     condition: " + conditionStr);
						// + "\n     regex: " + regex);
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
	boolean processVariableMatch(String relex, HashMap<String, String> uuidToBase)
	{
		nodeVarMapList = null;

		ArrayList<String> vars = new ArrayList<String>(2);
		// First find all the $vars (words beginning with $'s)
		// in the rule condition and all constants. Exclude vars preceded by 
		// underscore (eg, _$qVar)
		Matcher varNameMatcher = varNamePattern.matcher(conditionStr);
		while(varNameMatcher.find()) {
			int i = 1;
			// constants and variables are in different groups of the regex
			while (varNameMatcher.group(i) == null) i++;
			vars.add(varNameMatcher.group(i));
		}

		// If no $vars in the condition, return true.
		if (vars.size() == 0) {
			nodeVarMapList = new VarMapList(0);
			return true;
		}

		// Check for $var=$Concept syntax.
		// Store in VarMap as $varCONCEPT => $Concept
		if (conditionStr.startsWith("$var") && conditionStr.contains("=")) {
			if (vars.size() < 2) {
				System.err.println("Error: badly formed condition in a rule; " +
				      "the condition is: " + conditionStr);
				return true;
			}
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
		// Replace UUIDs in relex string with their lemma/base/root. 
		String relexBase = relex;
		ArrayList<PosUUIDPair> indexToUUID = new ArrayList<PosUUIDPair>(); 
		if (uuidToBase != null) {
			for (String uuid : uuidToBase.keySet()) {
				int sizeDiff = uuidToBase.get(uuid).length() - uuid.length();
				int found = 0; // Number of tokens with same uuid
				// Compile regular expression
		        Pattern pattern = Pattern.compile(uuid);
		        // Replace all occurrences of pattern in input
		        Matcher matcher = pattern.matcher(relexBase);
		        ArrayList<PosUUIDPair> newPairs = new ArrayList<PosUUIDPair>();
		        for (int j=0; j < indexToUUID.size(); j++) {
		        	newPairs.add(new PosUUIDPair(indexToUUID.get(j).pos,indexToUUID.get(j).UUID));
		        }
		        while (matcher.find()) {
		        	int matchPos = matcher.start();
		        	for (int j=0; j < indexToUUID.size(); j++) {
		        		if (matchPos < indexToUUID.get(j).pos) {
		        			newPairs.get(j).pos += sizeDiff;
		        		}
		        		
		        	}
		        	// Store where and what replacement happened.
		        	// Also adjust match pos for prior matches found.
		        	newPairs.add(new PosUUIDPair(matchPos+(sizeDiff*found),uuid));
		        	found++;
		        }
		        // Commented out code prints out the mapping between position and the UUID
		        // for the var/constant.
		        /*ArrayList<String> al = new ArrayList<String>();
		        for (int j = 0; j < indexToUUID.size(); j++) {
		        	int lineEnd = relexBase.indexOf("\n",indexToUUID.get(j).pos);
		        	if (lineEnd < 0) lineEnd = indexToUUID.get(j).pos + 10;
    			 	String t = new String(relexBase.substring(indexToUUID.get(j).pos,java.lang.Math.min(lineEnd,relexBase.length()-1)));
    			 	al.add(t);
		        }*/
    			relexBase = relexBase.replaceAll(uuid,uuidToBase.get(uuid));
    			/*for (int j = 0; j < indexToUUID.size(); j++) {
    				System.out.print("word " + indexToUUID.get(j).UUID + " @pos " + indexToUUID.get(j).pos + " [\"" + al.get(j) + "\"]");
    				System.out.print(" -> @pos " + newPairs.get(j).pos);
    				int lineEnd = relexBase.indexOf("\n",newPairs.get(j).pos);
    				if (lineEnd < 0) lineEnd = newPairs.get(j).pos + 10;
    				String t = relexBase.substring(newPairs.get(j).pos,java.lang.Math.min(lineEnd,relexBase.length()-1));
    				System.out.println(" [\"" + t + "\"]");
    			}*/
    			indexToUUID = newPairs;
		        
			}
		}
		
		// Find all the relex lines that match the condition.
		Matcher relexLineMatcher = relexLinePattern.matcher(relexBase);
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
								
				// Check var is not a constant and then...
				// check if var is a concept variable
				if (varName.startsWith("$") && !varName.startsWith("$var")) {
					// Strip off any var indexes (e.g., $Noun_2)
					indexMatcher.reset(varName);
					String varNameCheck = varName;
					if (indexMatcher.find()) {
						varNameCheck = varName.substring(0,indexMatcher.start());
					}

					if (!Rule.conceptContainsWord(varNameCheck,word)) {
						continue matchingLine;
					}
					if (VERBOSE) {
						System.out.println("Concept " + varNameCheck + 
								" contains " + word + "(" + word + ")");
					}
				} //end var is a concept var
				
				if (uuidToBase != null) {
					// Find original UUID for word in the indexToUUID array
					// Optimise this so that we don't just search the array stupidly.
					int j;
					for (j=0; j < indexToUUID.size(); j++) {
		        		if (indexToUUID.get(j).pos == relexLineMatcher.start(varNum)) {
		        			if (VERBOSE) System.out.print(word);
		        			word = indexToUUID.get(j).UUID;
		        			if (VERBOSE) System.out.println(" mapped back to " + word);
		        			break;
		        		}
					}
					if (VERBOSE && j == indexToUUID.size())
						System.err.println("Couldn't map " + word + " back to a UUID word");
				}
					        	
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

