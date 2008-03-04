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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single mapping rule
 */
public class Rule
{
	static boolean VERBOSE;

	ArrayList<String> relationships;

	// Matching variables of rule applied to a relex parse
	VarMapList ruleVarMapList;

 	// Matching variables of rule NOT conditions
	VarMapList ruleNegationVarMapList;

	// Abstract Syntax Tree that represents the conditions of the rule
	ASTNode conditionSyntaxTree;

	public String ruleStr; //for debugging, could remove for production
	private HashMap<String,ParensNode> parensNodeMap;

	void addRelationship(String relationship) {
		relationships.add(relationship);
	}

	public void setRuleStr(String rule) {
		ruleStr = rule.trim();
	}

	public String getRuleStr() {
		return ruleStr;
	}

	/**
	 * Fire this rule and return a Set of Frame Relationships (as Strings)
	 * with variable substitutions.
	 */
	public HashSet<String> fire(VarMapList varMapList, boolean verbose)
	{
		if (varMapList==null) {
			println("ENCOUNTERED NULL ruleVarMapList");
			return new HashSet<String>();
		}
		if (verbose) {
			//debugging info
			println("\n-----Firing relationship----");
			//print var map for debugging
			println("VarMaps:");

			for (VarMap varMap: varMapList) {
				for (String varName: varMap.keySet()) {
					System.out.print(varName + " = " + varMap.get(varName) + "    ");
				}
				println();
			}
			for (String relationship: relationships) {
				println("raw: " + relationship);
			}
		}

		// Handle duplicate relex relation matches in a single sentence
		// e.g. "Bob says killing for the Mafia beats killing for the government"
		// Handle by doing frame argument indexing
		// e.g., for(killing, Mafia), for(killing_1, government)
		// We can tell there are such duplicate matches when there is more than
		//  one VarMap in the ruleVarMapList
		HashSet<String> relationNames = null;
		boolean handleMultMatches = false;
		//if (ruleVarMapList!=null && ruleVarMapList.size()>=2) {
		if (varMapList!=null && varMapList.size()>=2) {
			// First get relex relation names for each condition,
			// to be used later in the rule firing process.
			relationNames = conditionSyntaxTree.getRelationNames();
			handleMultMatches = true;
			//println("===========================================\n" + ruleStr);
		}

		// Fire off each frame relationship (for each VarMap).
		HashSet<String> relationsSet = new HashSet<String>();
		//String relationship=null;
		for (String origRelationship : relationships) {
			//println("-----------------\nOrig Relationship: " + origRelationship);
			String relationship = origRelationship;
			// No var substition, so just add the raw relationship.
			if (varMapList==null || varMapList.isEmpty()) {
				relationsSet.add(relationship);
			}
			else {
				// Do var substitution for each VarMap in ruleVarMapList.
				//for (VarMap varMap: ruleVarMapList) {
				for (int i=0;i<varMapList.size();i++) {
					VarMap varMap = varMapList.get(i);

					// Handle duplicate relex relation matches by doing frame
					// argument indexing (see above)
					// Do frame argument indexing for each VarMap, except the first
					if (handleMultMatches && i>0) {  // i>0: all varMaps except the first
						// Replace any relex relation names in frame 
						// relationship arguments with relationName_index
						relationship = origRelationship;
						//varMap.print();
						for (String relationName : relationNames) {
							//print("relationName: " + relationName + "   ");
							// Search for relex relationName in frame
							// arguments.
							String regex = "[\\(,]\\s*" + relationName;
							Pattern p = Pattern.compile(regex);
							Matcher m = p.matcher(relationship);
							if (m.find()) {
								String newName = relationName + "_" + i;
								relationship = relationship.substring(0,m.start()) +
									m.group().substring(0,1) + //this is the "(" or ","
									newName + relationship.substring(m.end());
							}
							//println("new relationship: " + relationship);
						}
					} //end handleMultMatches
					String substRelationship = relationship;
					for (String var: varMap.keySet()) {
						String value = varMap.get(var);
						// Prefix $'s with \ (for potential $'s in value, 
						// such as _$qVar).
						value = value.replace("$", "\\$");
						substRelationship = substRelationship.replaceAll("(?<!_)\\$" + var.substring(1) + "(?i)", value);
					}
					if (VERBOSE) println("sub: " + substRelationship);
					relationsSet.add(substRelationship);
					//println(relationship);
				}
			}
		}
		//ruleVarMapList.clear();
		return relationsSet;
	}


	public void printout()
	{
		println("\n------- Rule -------");
		println(ruleStr);
		println("Conditions: " + conditionSyntaxTree.print());
		//println("Relation names (xcept for OR's): " + conditionSyntaxTree.getRelationNames());
		for (String relationship: relationships) {
			println("Relationship: " + relationship);
		}
	}

	public void setAST(ASTNode ast) {
		this.conditionSyntaxTree = ast;
	}

	/**
	 * Parse the rule string. Create abstract syntax tree (AST) to represent
	 * the conditions. Add the Frame relationships to relationships list.
	 *
	 * @return true if parse is successful, otherwise false
	 */
	public boolean parseRule(String line) {
		setRuleStr(line);
		String[] frags = line.split("THEN");

		if (frags.length!=2) {
			//reportIllformedRule(line);
			//rules.remove(this);
			return false;
		}
		String conditions = frags[0];
		String relations = frags[1];

		// Parse conditions
		frags = conditions.split("IF");
		if (Array.getLength(frags)!= 2) {
			//reportIllformedRule(line);
			//rules.remove(this);
			return false;
		}

		conditions = frags[1];
		// Convert to lowercase, replace || with "or"
		//conditions = conditions.toLowerCase().replace("||", "or");
		// Replace || with "or"
		conditions = conditions.replace("||", "or");

		ASTNode ast = buildSyntaxNode(conditions);
		if (ast==null) return false;
		setAST(ast);

		// Add relationships.
		String[] relationsArray = relations.split("\\^");
		int relationsCount = Array.getLength(relationsArray)-1;

		// Check for NOT preceding first relationship
		boolean nextIsNot = false;
		boolean hasNotTail = false;
		String relation = relationsArray[0].trim();
		if (relation.toUpperCase().matches("NOT")) {
			nextIsNot = true;
		}
		relationships = new ArrayList<String>(relationsCount);
		for (int i=1;i<=relationsCount;i++) {
			relation = relationsArray[i].trim();
			// Check for NOT tail which should really precede the next relationship

			if (relation.length()>=4 &&
					relation.substring(relation.length()-4).toUpperCase().matches(" NOT")) {
				hasNotTail = true;
				relation = relation.substring(0,relation.length()-4);
			}
			if (nextIsNot) {
				relationships.add("NOT ^" + relation);
			}
			else {
				relationships.add("^" + relation);
			}
			nextIsNot = hasNotTail;
			hasNotTail = false;
		}
		//rule.print();

		parensNodeMap = null;
		return true;

	}

	/**
	 * Recursive function that builds a syntax node based on 
	 * supplied string fragment. Assumes ^ (&&, AND) takes 
	 * precedence over OR (||) 
	 */
	ASTNode buildSyntaxNode(String frag)
	{
		frag = frag.trim();

		// Process parentheses -- Replace parens elements with 
		// string tokens and save corresponding parens element.
		// TODO implement nested parens
		// TODO check for single parens errors and/or unmatched parens
		// Make this a reluctant search -- will need to change if 
		// we implement nested parens
		Pattern p = Pattern.compile("\\{.+?\\}");
		Matcher m = p.matcher(frag);
		//println(frag);

		 int parensIndex = 0;
		 ASTNode childNode;
		 while (m.find()) {
			 String parensStr = m.group().substring(1,m.group().length()-1);
			 ParensNode parensNode = new ParensNode();
			 childNode = buildSyntaxNode(parensStr);
			 if (childNode==null) return null;
			 parensNode.addChild(childNode);
			 String key = "%%%PARENS_" + parensIndex + "%%%";
			 if (parensNodeMap==null) {
				 parensNodeMap = new HashMap<String,ParensNode>();
			 }
			 parensNodeMap.put(key,parensNode);
//			 if (VERBOSE) {
//				 println("\nfrag with parens: " + frag);
//				 println("PARENS STRING: " + parensStr);
//				 println("full parens: " + m.group());
//			 }
			 frag = frag.replace(m.group(),key);
			 //if (VERBOSE) println("frag with token: " + frag);
			 parensIndex++;
		 }

		// Process OR's and return OrNode
		String[] subfrags = frag.split("(?i)\\s+or\\s+");
		if (Array.getLength(subfrags)>1) {
			//build and return OrNode
			OrNode orNode = new OrNode();
			for (String subfrag: subfrags) {
				childNode = buildSyntaxNode(subfrag);
				if (childNode==null) return null;
				orNode.addChild(childNode);
			}
			return orNode;
		}

		// Process AND's and return andNode
		if (frag.contains("^")) {
			subfrags = frag.split("\\^");
			if (Array.getLength(subfrags)< 2) {
				//improperly formed rule encountered
				return null;
			}
			//build and return AndNode
			AndNode andNode = new AndNode();
			for (String subfrag: subfrags) {
				childNode = buildSyntaxNode(subfrag);
				if (childNode==null) return null;
				andNode.addChild(childNode);
			}
			return andNode;
		}

		// Process %%%PARENS_#%%% token and return parensNode from map
		//frag = frag.trim();
		if (frag.matches("%%%PARENS_\\d+%%%")) {
			String key = frag.trim();
			return parensNodeMap.get(key);
		}

		// Check for NOT
		if (frag.length() < 3) return null;
		if (frag.substring(0, 3).toLowerCase().equals("not")) {
			// Check if NOT is for a parens
			NotNode n;
			if (frag.substring(3).trim().matches("%%%PARENS_\\d+%%%")) {
				n = new NotNode(parensNodeMap.get(frag.substring(3).trim()));
			} else {
				//NOT is for a single condition
				//make sure there are no (unexpected) curly braces within the 
				//condition string
				if (frag.contains("{") || frag.contains("}")) {
					return null;	
				}
				n = new NotNode(frag);
			}
			n.setRule(this);
			return n;
		}

		//else return condition node
		//first check for curly braces in frag for condition string,
		//if curly braces are present within conditionStr, this indicates
		//a malformed rule
		if (frag.contains("{") || frag.contains("}")) {
			return null;	
		}

		return new ConditionNode(frag);
	}

	/**
	 * Determines if this rule's conditions are satisfied by current relex
	 * output. First checks if the condition syntax tree matches the relex
	 * (regardless of variable substitution issues), then checks variable
	 * matching.
	 */
	synchronized boolean satisfiedByRelex(String relex)
	{
		ruleNegationVarMapList = null;
		if (conditionSyntaxTree.matchesRelex(relex)) {
			//check var matching
			if (conditionSyntaxTree.processVariableMatch(relex)) {
				ruleVarMapList = conditionSyntaxTree.getVarMapList();
				//reconcile NOT conditions, negation var map list
				if (reconcileNegationVars()) {
					return true;
				}
			} //end processVarMatch==true
		} //end matchesRelex==true

		return false;
	} //end satisfiedByRelex()

	/**
	 * Wrapper for satisfiedByRelex(relex) in order to handle
	 * potential multi-thread issues with web demo.
	 * Clones the rule VarMaps and puts them into the supplied
	 * VarMapList, so processing by other users won't affect
	 * the current results
	 */
	public synchronized boolean 
	satisfiedByRelex(String relex, VarMapList varMapList)
	{
		if (satisfiedByRelex(relex)) {
			for (VarMap varMap : ruleVarMapList) {
				varMapList.add((VarMap)varMap.clone());
			}
			return true;
		}
		return false;
	}


	/**
	 * Process negation variables resulting from NOT conditions. Removes
	 * VarMaps from the rule VarMap list that are "cancelled out" by
	 * negative conditions.
	 *
	 * @return false if negative conditions leave no positive conditions
	 * satisfied, else true
	 */
	boolean reconcileNegationVars()
	{
		//no negation vars, so return true
		if (ruleNegationVarMapList==null || ruleNegationVarMapList.isEmpty()) {
			return true;
		}
		//there are negation variables that satisfy a NOT condition
		//if no positive variables, then return false
		if (ruleVarMapList==null || ruleVarMapList.isEmpty()) {
			return false;
		}

		//for each variable of each negation VarMap
		for (VarMap negVarMap : ruleNegationVarMapList) {
			for (String negVarName : negVarMap.keySet()) {
				//check against each var of each positive VarMap
				//use iterator b/c we may have to remove items from the list
				for (Iterator<VarMap> varMapIt=ruleVarMapList.iterator(); varMapIt.hasNext(); ) {
					VarMap varMap = varMapIt.next();
					for (String varName : varMap.keySet()) {
						if (negVarName.equals(varName)) {
							if (negVarMap.get(negVarName).equals(varMap.get(varName))) {
								 varMapIt.remove();
							}
						}
					}
					//check for concept variable definitions
					if (negVarName.endsWith("CONCEPT")) {
						String concept = negVarMap.get(negVarName);
						String checkVarName = negVarName.replace("CONCEPT","");
						String word = varMap.get(checkVarName);
						if (conceptContainsWord(concept,word)) {
							varMapIt.remove();
							//break;? and above?
						}
					}
				}
			}
		}
		//if ruleVarMap is empty after reconciling negation vars,
		//	then all positive vars have been "cancelled" by neg vars,
		//  so return false
		if (ruleVarMapList.isEmpty()) {
			return false;
		}

		return true;
	}


	/**
	 * Util function to Merge together 2 VarMap lists into a new single list.
	 * VarMaps with conflicting values are invalid and not included in the
	 * new returned list.
	 */
	static VarMapList mergeVarMapLists(VarMapList aList, VarMapList bList) {
		int newSize = aList.size() * bList.size();
		VarMapList newList = new VarMapList(newSize);
		//if either list is empty, return the other
		if (aList.isEmpty()) {
			newList.addAll(bList);
			return newList;
		}
		else if (bList.isEmpty()) {
			newList.addAll(aList);
			return newList;
		}
		for (VarMap aMap: aList) {
			for (VarMap bMap: bList) {
				VarMap newMap = new VarMap(aMap);
				newList.add(newMap);
				for (String key: bMap.keySet()) {
					String word = bMap.get(key);
					//if key/varName is not already in the map, add it
					if (!newMap.containsKey(key)) {
						newMap.put(key,word);
					}
					else {
						//var is already in the map
						//check to make sure it's the same word value
						if (!word.equals(newMap.get(key))) {
							//found conflicting var values, this VarMap is not valid
							//so remove it from the newList
							newList.remove(newMap);
						}
					} //var is already in the map

				}
			}
		}

		if (newList.isEmpty()) {
			//new list is empty, no valid varmaps, return null to indicate no success
			return null;
		}
		return newList;
	}

	static boolean conceptContainsWord(String concept, String word)
	{
		if (word==null) {
			return true;
		}
		if (VERBOSE) 
			System.out.println("\nchecking concept var for legal vaue: " + concept);

		boolean legal = false;
		//handle $Number variable special case
//TODO: check how $Number should be handled
/*						if (varName.toLowerCase().equals("$number")) {
			try {
				Double.parseDouble(word.replace(",",""));
				legal = true;
			}
			catch (NumberFormatException e) {
				legal = false;
			}
		}
		else {
*/
			ArrayList<String> legalValues = Frame.conceptVarMap.get(concept.toLowerCase());
			if (legalValues==null) {
				if (VERBOSE)
					System.out.println("no concept values list found for var " + concept);
				return false;
			}
			for (String value: legalValues) {
				if (value.equalsIgnoreCase(word)) {
					legal = true;
					break;
				}
			}
//		}
		if (legal==false) {
			if (VERBOSE)
				System.out.println("'"+word+"'" +
					" is NOT a legal value for " + concept);
			//goto the next matching line
			//continue matchingLine;
		}
		else {
			if (VERBOSE)
				System.out.println("'"+word+"'" +
					" IS a legal value for " + concept);
		}

		return legal;

	} //end conceptContainsWord()

	void println(String s) {
		System.out.println(s);
	}

	void println() {
		println("");
	}

} //end class Rule

