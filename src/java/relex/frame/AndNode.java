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
import java.util.Iterator;

/**
 * Represents a collection of child nodes that are connected by AND
 * operator(s).
 */
class AndNode extends ASTNode
{
	String print() {
		if (children.isEmpty()) return null;
		return " AND:" + children;
	}

	HashSet<String> getRelationNames() {
		HashSet<String> relationNames = new HashSet<String>();
		for (ASTNode childNode : children) {
			HashSet<String> childNodeNames = childNode.getRelationNames();
			if (childNodeNames!=null) {
				relationNames.addAll(childNodeNames);
			}
		}
		return relationNames;
	}

	/**
	 * @param uuidToBase TODO
	 * @return true if each of its child nodes matches the given relex
	 */
	boolean matchesRelex(String relex, HashMap<String, String> uuidToBase) {
		for (ASTNode childNode: children) {
			if (!childNode.matchesRelex(relex, uuidToBase)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that the VarMaps of each of its child nodes are
	 * consistent with each other. Merges the consistent child
	 * VarMaps into a list of VarMaps that are consistent across
	 * all it's child conditions. Stores the new generated
	 * VarMapList in nodeVarMapList.
	 *
	 * @return true if at least 1 generated VarMap is consistent
	 *         across all child nodes
	 */
	boolean processVariableMatch(String relex, HashMap<String, String> uuidToBase) {
		nodeVarMapList = null;
		//create VarMapLists for each of the child nodes
		//get VarMapLists for each child node
		ArrayList<VarMapList> childVarMapLists = new ArrayList<VarMapList>(children.size());
		for (ASTNode childNode: children) {
			if (childNode.processVariableMatch(relex,uuidToBase)) {
				childVarMapLists.add(childNode.getVarMapList());
			}
			else {
				//could not generate varmaps for one of the conditions
				//so return false
				return false;
			}
		}

		//Create new VarMapList by merging all the children nodes' VarMapLists
		//create varMaps that combine varMaps of each of the children
		//iterate through child var maps to make all possible combinations

		//if no child VarMapLists return false
		//i think we should never reach this, b/c we should have at least an empty MapList from each child node
		if (childVarMapLists.size()==0) {
			return false;
		}
		VarMapList newVarMapList = new VarMapList();
		VarMapList childVarMapList = childVarMapLists.get(0);
		//add each of the child VarMaps to the new VarMapList
//			for (VarMap varMap: childVarMapList) {
//				varMapList.add(varMap);
//			}
		if (childVarMapList!=null) {
			newVarMapList.addAll(childVarMapList);
		}
		//if just one child VarMapList, set node varMapList to this and return
		if (childVarMapLists.size()==1) {
			nodeVarMapList = childVarMapList;
			return true;
		}

		//for each additional child node, progressively merge with the newVarMapList
		for (int i=1;i<childVarMapLists.size();i++) {
			childVarMapList = childVarMapLists.get(i);
			if (childVarMapList!=null) {
				newVarMapList = Rule.mergeVarMapLists(newVarMapList,childVarMapList);
			}
			if (newVarMapList==null) {
				//no valid VarMaps, return false
				return false;
			}
		} //end for each additional child node

		//check $var=$concept syntax
		//for each VarMap, check each var to see if it's a concept var definition
		//for (VarMap varMap : newVarMapList) {
		//use iterator b/c we may have to remove items from the list
		//newVarMapList might be empty if all children are NOT's
		if (!newVarMapList.isEmpty()) {
			for (Iterator<VarMap> varMapIt=newVarMapList.iterator(); varMapIt.hasNext(); ) {
				VarMap varMap = varMapIt.next();
				for (String varName : varMap.keySet()) {
					if (varName.endsWith("CONCEPT")) {
						String concept = varMap.get(varName);
						varName = varName.replace("CONCEPT","");
						String word = varMap.get(varName);
						if (!Rule.conceptContainsWord(concept,word)) {
							varMapIt.remove();
							break;
						}
					}
				}
			}
			//if all varMaps have been removed, return false
			if (newVarMapList.isEmpty()) {
				return false;
			}
		} //end check $var=$Concept syntax

		nodeVarMapList = newVarMapList;
		return true;
	} //end processVariableMatch()

} //end AndNode

