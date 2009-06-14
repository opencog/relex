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
 * Represents condition(s) that are grouped by parentheses,
 *  or curly braces, in this case "{       }"
 */
class ParensNode extends ASTNode {
	String print() {
		if (children.isEmpty()) return null;
		return " PARENS:" + children;
	}

	HashSet<String> getRelationNames() {
		return children.get(0).getRelationNames();
	}

	/**
	 * @return true if each child node matches given relex
	 */
	boolean matchesRelex(String relex, HashMap<String, String> uuidToBase) {
		for (ASTNode childNode: children) {
			if (!childNode.matchesRelex(relex, uuidToBase)) {
				return false;
			}
		}
		return true;
	}


	boolean processVariableMatch(String relex, HashMap<String, String> uuidToBase) {
		nodeVarMapList = null;
		//parens only has 1 child, right?
		ASTNode childNode = children.get(0);
		if (childNode.processVariableMatch(relex,uuidToBase)) {
			nodeVarMapList = childNode.getVarMapList();
			return true;
		}
		return false;
	}
} //end ParensNode

