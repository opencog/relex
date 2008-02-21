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

/**
 * Represents a group of child nodes that are connected by OR operator(s)
 */
class OrNode extends ASTNode {

	ArrayList<ASTNode> childNodeMatches;

	String print() {
		if (children.isEmpty()) return null;
		return " OR:" + children;
	}

	HashSet<String> getRelationNames() {
		if (childNodeMatches==null || childNodeMatches.isEmpty()) {
			return null;
		}
		HashSet<String> relationNames = new HashSet<String>(3);
		for (ASTNode childNode : childNodeMatches) {
			HashSet<String> childNodeNames = childNode.getRelationNames();
			if (childNodeNames!=null) {
				relationNames.addAll(childNodeNames);
			}
		}
		return relationNames;
	}

	/**
	 * Returns true if at least 1 child node matches relex. Creates list of
	 * all matching child nodes (childNodeMatches) to be used later for
	 * variable substitution and validation
	 */
	boolean matchesRelex(String relex) {
		childNodeMatches = null;
		boolean pass = false;
		for (ASTNode childNode: children) {
			if (childNode.matchesRelex(relex)) {
				pass = true;
				if (childNodeMatches==null) {
					childNodeMatches = new ArrayList<ASTNode>();
				}
				childNodeMatches.add(childNode);
			}
		}
		return pass;
	}

	/**
	 * Creates VarMapList with VarMaps from each valid condition
	 */
	boolean processVariableMatch(String relex) {
		nodeVarMapList = null;
		boolean pass = false;
		//create VarMapsList with VarMaps from each valid condition
		VarMapList newList = new VarMapList();
		for (ASTNode childNode: childNodeMatches) {
			if (childNode.processVariableMatch(relex)) {
				pass = true;
				if (childNode.getVarMapList()!=null) {
					newList.addAll(childNode.getVarMapList());
				}
			}
		}
		nodeVarMapList = newList;
		return pass;
	}

}

