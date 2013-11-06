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
 * word ("as many/much")
 */
package relex.algs;

import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.LinkView;

/**
 * Combines two words into one, stored in the right node.
 * Leave the orig_str alone, however, don't mangle that.
 * (its needed for printing the orig sentence, etc.)
 */
public class TwoWordCombineToRightAlg extends TemplateMatchingAlg
{
	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		FeatureNode leftNode = LinkView.getLeft(node);
		FeatureNode rightNode = LinkView.getRight(node);

		// find the strings and originals
		String leftString = leftNode.featureValue("str");
		// String leftOriginal = leftNode.featureValue("orig_str");
		// if (leftOriginal == null)
		//	 leftOriginal = leftString;

		String rightString = rightNode.featureValue("str");
		// String rightOriginal = rightNode.featureValue("orig_str");
		// if (rightOriginal == null)
		//	 rightOriginal = rightString;

		// make the combined strings
		// String original = leftOriginal + " " + rightOriginal;
		String str = leftString + "_" + rightString;

		// set the values
		rightNode.get("ref").set("name", new FeatureNode(str));
		rightNode.set("str", new FeatureNode(str));
		// rightNode.set("orig_str", new FeatureNode(original));
		rightNode.set("collocation_end", leftNode);
		rightNode.set("collocation_start", rightNode);

		// Erase the other word strings
		leftNode.set("str", new FeatureNode(""));
		// leftNode.set("orig_str", null);
		
		// System.out.println("RIGHT NODE SET TO " + str);
	}
}

