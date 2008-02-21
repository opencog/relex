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
 * Abstract base class for a node of Abstract Syntax Tree
 */
abstract class ASTNode
{
	static boolean VERBOSE;

	//ArrayList<ASTNode> children = new ArrayList<ASTNode>();
	ArrayList<ASTNode> children;
	VarMapList nodeVarMapList;

	abstract boolean matchesRelex(String relex);
	abstract HashSet<String> getRelationNames();
	abstract boolean processVariableMatch(String relex);
	abstract String print();

	void addChild(ASTNode node) {
		if (children==null) {
			children = new ArrayList<ASTNode>();
		}
		children.add(node);
	}

	public String toString() {
		return print();
	}

	VarMapList getVarMapList() {
		return nodeVarMapList;
	}

//		void clearVarMapList() {
//			//clear for self and for each child
//			nodeVarMapList.clear();
//			if (children!=null) {
//				for (ASTNode childNode: children) {
//					childNode.clearVarMapList();
//				}
//			}
//		}

} //end class ASTNode

