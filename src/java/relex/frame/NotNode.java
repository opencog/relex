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

import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents a negative condition--a condition or parentheses
 * child node that is prefixed by a "NOT".
 */
class NotNode extends ASTNode
{
	Rule rule; // for access to ruleNegationVarMapList
	public void setRule(Rule r) {
		rule = r;
	}

	/**
	 * Constructor for when child is a single condition
	 *
	 * @param frag 	the negative condition as a string
	 */
	NotNode(String frag) {
		//TODO handle parsing / rule error, e.g. NOT by itself
		frag = frag.substring(3).trim();
		ConditionNode condNode = new ConditionNode(frag);
		this.addChild(condNode);
	}

	/**
	 * Constructor when child is a parensNode
	 */
	NotNode(ParensNode parensNode) {
		this.addChild(parensNode);
	}

	String print() {
		if (children.isEmpty()) return null;
		return " NOT:" + children;
	}

	HashSet<String> getRelationNames() {
		return children.get(0).getRelationNames();
	}

	boolean matchesRelex(String relex, HashMap<String, String> uuidToBase) {
		//if no matches, we return true (since it's a negative condition)
		//if there is a match, but no vars, return false
		//if there is a match, but has vars, then return true because exclusion of rule based on
		//	this NOT condition will depend on variable match
		ASTNode childNode = children.get(0);
		if (childNode.matchesRelex(relex, uuidToBase)) {
			//there is a match
			if (childNode.processVariableMatch(relex, uuidToBase)) {
				//if no vars then we have a definite match so return false
				nodeVarMapList = childNode.getVarMapList();
				if (nodeVarMapList==null || nodeVarMapList.size()==0) {
					return false;
				}
				else {
					//match needs to be checked against vars
					return true;
				}
			}
		}
		//no match, so return true since this is a NOT condition
		//create empty nodeVarMapList for use in other functions(mergeVarMapLists())
		if (nodeVarMapList==null) {
			nodeVarMapList = new VarMapList();
		}
		return true;
	}


	/**
	 * Creates a VarMapList of negation variable values,
	 * ruleNegationVarMapList. The ruleNegationVarMapList
	 * is applied against the ruleVarMapList in satisfiedByRelex().
	 * Always returns true.
	 */
	boolean processVariableMatch(String relex, HashMap<String, String> uuidToBase)
	{
		// nodeVarMapList has already been set in matchesRelex()
		// Add nodeVarMapList to ruleNegationVarMapList
		if (nodeVarMapList!=null && !nodeVarMapList.isEmpty()) {
			if (rule.ruleNegationVarMapList==null) {
				rule.ruleNegationVarMapList = new VarMapList();
			}
			rule.ruleNegationVarMapList.addAll(nodeVarMapList);
		}
		// Set nodeVarMapList to null since these vars are now
		// in the negation VarMapList
		nodeVarMapList = null;
		return true;
	}
} //end class NotNode
