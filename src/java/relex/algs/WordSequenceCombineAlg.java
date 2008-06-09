package relex.algs;
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
 * This algorithm combines sequences of words which should be a single word 
 * (proper names, and idioms like "at hand")
 */
import java.util.regex.Pattern;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

public class WordSequenceCombineAlg extends TemplateMatchingAlg
{
	static String nameLabelRegex = "G[a-z\\*]*"; // a-z or '*'

	static String entityOrIdiomLabelRegex = "ID[A-Z]*[a-z\\*]*";

	static String allLabelRegex = "(" + nameLabelRegex + ")|(" + entityOrIdiomLabelRegex + ")";

	static int directionLeft = -1;

	static int directionRight = 1;

	/*
	 * Finds the next node
	 */
	FeatureNode nextNode(FeatureNode node, int dir, String labelRegex)
	{
		LinkableView linkable = new LinkableView(node);
		for (int i = 0; i < linkable.numLinks(dir); i++) { // iterate right
															// links
			LinkView link = new LinkView(linkable.getLink(dir, i));
			String label = link.getLabel(0);
			if (Pattern.matches(labelRegex, label))
				return (dir == directionLeft ? link.getLeft() : link.getRight());
		}
		return null;
	}

	boolean isRightMost(FeatureNode node, String labelRegex)
	{
		return nextNode(node, directionRight, labelRegex) == null;
	}

	FeatureNode getLeftMost(FeatureNode node, String labelRegex)
	{
		FeatureNode next = nextNode(node, directionLeft, labelRegex);
		if (next == null)
			return node;
		return getLeftMost(next, labelRegex);
	}

	String collectNames(FeatureNode current, FeatureNode rightNode,
	                    String labelRegex, boolean shouldEraseStrAndRef)
	{
		String name = current.get("str").getValue();
		// use the original string in case it has been changed
		if (current.get("orig_str") != null) {
			name = current.get("orig_str").getValue();
		}
		if (shouldEraseStrAndRef && current != rightNode) {
			// TODO: maybe nodes should actually be deleted
			// current.set("str",new FeatureNode(""));
			current.set("ref", null);// .set("name",new FeatureNode(""));
			current.set("str", null);
			current.set("orig_str", null);
		}

		if (current == rightNode)
			return name;
		return name + "_"
				+ collectNames(nextNode(current, directionRight, labelRegex),
		                     rightNode, labelRegex, shouldEraseStrAndRef);
	}

	protected void applyTo(FeatureNode node, RelexContext context)
	{
		FeatureNode rightNode = getTemplate().val("right");
		if (rightNode != LinkView.getRight(node))
			throw new RuntimeException("variable 'right' is not properly assigned");
		if (!isRightMost(rightNode, allLabelRegex))
			return;
		FeatureNode leftNode = getLeftMost(rightNode, allLabelRegex);

		String bigName = collectNames(leftNode, rightNode, allLabelRegex, true);
		String bigNameWithSpaces = bigName.replace('_', ' ');
		rightNode.get("ref").set("name", new FeatureNode(bigName));
		
		// see message "Relex fixes: definites and conjoined prepositions" from Mike Ross
		// rightNode.get("ref").set("specific", new FeatureNode("T"));
		
		rightNode.set("str", new FeatureNode(bigName)); // bigNameWithSpaces));

		// set original string too, since the big name is created using original
		// strings (if possible)
		rightNode.set("orig_str", new FeatureNode(bigNameWithSpaces));
		rightNode.set("collocation_end", rightNode);
		rightNode.set("collocation_start", leftNode);

		// use morephology on the right node, just in case.
		MorphyAlg m = new MorphyAlg();
		m.applyTo(rightNode, context);

	}

}
