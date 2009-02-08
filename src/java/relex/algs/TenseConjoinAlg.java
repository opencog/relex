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

package relex.algs;

import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;

/**
 * Traverses a sequence of tense feature nodes in which each node has a "val"
 * feature pointing to a tense name, and a "prev" feature pointing to the
 * previous name. Returns the conjoined list of names, separated by '_'
 * character.
 */
public class TenseConjoinAlg extends TemplateMatchingAlg
{
	String recursiveTenseExtract(FeatureNode node)
	{
		// return at end of recursion
		if (node == null) return "";
		// set val to the current value
		String val = node.featureValue("val");
		if (val == null) val = "";
		// append previous vals
		String prevVal = recursiveTenseExtract(node.get("prev"));
		String appendString = "_";
		if ((prevVal.length() == 0) || (val.length() == 0)) appendString = "";
		val = prevVal + appendString + val;
		return val;
	}

	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		FeatureNode tenseNode = node.get("tense");
		tenseNode.set("name", new FeatureNode(recursiveTenseExtract(tenseNode)));
	}

}
