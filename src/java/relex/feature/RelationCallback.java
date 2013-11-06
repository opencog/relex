/*
 * Copyright 2008 Novamente LLC
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
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

package relex.feature;

/**
 * The RelationCallback is an interface class.
 * It provides a a simple, easy callback interface
 * that can be invoked when traversing a graph.
 */

public interface RelationCallback
{
	/**
	 * UnaryRelationCB() -- called for every unary relation in the graph.
	 * Example usage to get various desired items:
	 *
	 * FeatureNode attr = node.get(attrName);
	 * String value = attr.getValue();
	 * String name = node.get("name").getValue();
	 * FeatureNode srcs = node.get("nameSource");
	 *
	 * Return true to halt traversal; return false to call again.
	 */
	public Boolean UnaryRelationCB(FeatureNode node, String attrName);

	/**
	 * BinaryRelationCB() -- called for every binary relation in the graph.
	 * Example usage to get various desired items:
	 *
	 * String srcName = srcNode.get("name").getValue();
	 * FeatureNode srcs = srcNode.get("nameSource");
	 *
	 * Return true to halt traversal; return false to call again.
	 */
	public Boolean BinaryRelationCB(String relation,
	                                FeatureNode srcNode, FeatureNode tgtNode);

	/**
	 * BinaryHeadCB() -- called once for every binary relation head.
	 *
	 * Typically, there are multiple binary relations originating from a
	 * given node. Whereas BinaryRelationCB() will be called once for
	 * each relation originating from that node, the BinaryHeadCB() will
	 * be called only once for the originating node.
	 *
	 * Return true to halt traversal; return false to call again.
	 */
	public Boolean BinaryHeadCB(FeatureNode from);
}
