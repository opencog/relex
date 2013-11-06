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

/**
 * This algorithm combines sequences of words which should be a single
 * word (proper names, and idioms like "at hand")
 */
package relex.algs;

import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.LinkView;

/**
 * Combines two words into one, stored in the left node.
 * But don't mangle the "orig str", we need this for other uses.
 * The core problem is that combining to the left, which is
 * called on "K" relations, will change word order (e.g. "Let's
 * get it on" combines "get on", eventually giving _obj(get_on, it))
 * However, changed word order is terrible if the orig str needs
 * to be printed.
 */
public class TwoWordCombineToLeftAlg extends TemplateMatchingAlg
{
	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		FeatureNode rightNode = LinkView.getRight(node);
		FeatureNode leftNode = LinkView.getLeft(node);

		// Find the strings and originals
		String rightString = rightNode.featureValue("str");
		// String rightOriginal = rightNode.featureValue("orig_str");
		// if (rightOriginal == null)
		//	rightOriginal = rightString;

		String leftString = leftNode.featureValue("str");
		// String leftOriginal = leftNode.featureValue("orig_str");
		// if (leftOriginal == null)
		//	leftOriginal = leftString;

		// make the combined strings
		String str = leftString + "_" + rightString;
		// String original = leftOriginal + " " + rightOriginal;

		// set the values
		leftNode.get("ref").set("name", new FeatureNode(str));
		leftNode.set("str", new FeatureNode(str));
		// leftNode.set("orig_str", new FeatureNode(original));
		leftNode.set("collocation_end", rightNode);
		leftNode.set("collocation_start", leftNode);

		//erase the other word strings
		rightNode.set("str", new FeatureNode(""));
		// rightNode.set("orig_str", null);
	}
}

