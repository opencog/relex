/*
 * Copyright 2008,2009 Novamente LLC
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

/**
 * This algorithm combines sequences of words which should be a single word
 * (proper names, and idioms like "at hand")
 */
import java.util.regex.Pattern;
import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

public class WordSequenceCombineAlg extends TemplateMatchingAlg
{
	static final String nameLabelRegex = "G[a-z\\*]*"; // a-z or '*'

	static final String entityOrIdiomLabelRegex = "ID[A-Z]*[a-z\\*]*";

	static final String allLabelRegex = "(" + nameLabelRegex + ")|(" + entityOrIdiomLabelRegex + ")";

	static final int directionLeft = -1;

	static final int directionRight = 1;

	/*
	 * Finds the next node
	 */
	private FeatureNode nextNode(FeatureNode node, int dir, String labelRegex)
	{
 		// Iterate on right links.
 		int nl = LinkableView.numLinks(node, dir);
		for (int i = 0; i < nl; i++)
		{
			FeatureNode fn = LinkableView.getLink(node, dir, i);
			String label = LinkView.getLabel(fn, 0);
			if (Pattern.matches(labelRegex, label))
				return (dir == directionLeft ? LinkView.getLeft(fn) : LinkView.getRight(fn));
		}
		return null;
	}

	private boolean isRightMost(FeatureNode node, String labelRegex)
	{
		return nextNode(node, directionRight, labelRegex) == null;
	}

	private FeatureNode getLeftMost(FeatureNode node, String labelRegex)
	{
		FeatureNode next = nextNode(node, directionLeft, labelRegex);
		if (next == null)
			return node;
		return getLeftMost(next, labelRegex);
	}

	private String collectNames(FeatureNode current, FeatureNode rightNode,
	                    String labelRegex, boolean shouldEraseStrAndRef)
	{
		String name = current.get("str").getValue();

		// Use the original string in case it has been changed
		if ((current.get("orig_str") != null) &&
		    (current.get("orig_str").equals("") == false))
		{
			name = current.get("orig_str").getValue();
		}

		if (shouldEraseStrAndRef && current != rightNode)
		{
			// Nodes should *NOT* actually be deleted, especially not the
			// "orig_str". This is because it is needed for printing in
			// some of the output formats, which need to have access to
			// the original, unprocessed sentence.
			// current.set("orig_str", new FeatureNode(""));
			//
			// Also, must *not* delete "ref", as this is neeed by alternate
			// output formats, e.g. the Stanford-parser compatbility output.
			// current.set("ref", null);

			current.set("name", null);

			current.set("str", new FeatureNode(""));
		}

		if (current == rightNode)
			return name;
		return name + "_"
				+ collectNames(nextNode(current, directionRight, labelRegex),
		                     rightNode, labelRegex, shouldEraseStrAndRef);
	}

	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		FeatureNode rightNode = getTemplate().val("right", vars);
		if (rightNode != LinkView.getRight(node))
			throw new RuntimeException("variable 'right' is not properly assigned");
		if (!isRightMost(rightNode, allLabelRegex))
			return;

		FeatureNode leftNode = getLeftMost(rightNode, allLabelRegex);

		String bigName = collectNames(leftNode, rightNode, allLabelRegex, true);
		FeatureNode bigNameF = new FeatureNode(bigName);
		rightNode.get("ref").set("name", bigNameF);

		// See email message "Relex fixes: definites and conjoined
		// prepositions" from Mike Ross
		// rightNode.get("ref").set("specific", new FeatureNode("T"));

		rightNode.set("str", bigNameF);

		// Set original string too, since the big name is created using original
		// strings (if possible) -- err, no, we really need the original
		// string to be the true original string.
		// String bigNameWithSpaces = bigName.replace('_', ' ');
		// rightNode.set("orig_str", new FeatureNode(bigNameWithSpaces));
		rightNode.set("collocation_end", rightNode);
		rightNode.set("collocation_start", leftNode);
		// rightNode.set("start_char", leftNode.get("start_char"));
		rightNode.get("start_char").setValue(leftNode.get("start_char").getValue());

		// Create membership
		// XXX need to set member for words in the middle too
		// rightNode.set("memb", rightNode);
		// leftNode.set("memb", rightNode);

		// Use morphology on the right node, just in case.
		// Err .. maybe not. What does this acheive?
		//
		// The combined string will have underscores in it, and that's OK.
		// WordNet does contain underscored entities in it, so it does
		// make sense to look these up, in principle. On the other hand,
		// these combined strings are either entities (place names like
		// "New York") or idioms ("en masse") and so finding the lemma
		// for such things just doesn't really make sense. Worse, the
		// Morphy algs does damage: it will take a corectly capitalized
		// "New York" and turn it into the mis-capitalized "New york".
		// So we're not going to do this, at least, not without a good
		// explanation.
		//
		// FeatureNode orig = rightNode.get("orig_str");
		// MorphyAlg m = new MorphyAlg();
		// m.applyTo(rightNode, context);
		// rightNode.set("orig_str", orig);
	}
}
