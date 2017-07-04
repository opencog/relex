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

import java.util.HashMap;
import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.morphy.Morphed;

/**
 * Uses Morphy to generate the root of a word.
 */
public class MorphyAlg extends SentenceAlgorithm
{
	protected int init(String s)
	{
		if (s.length() > 0)
			throw new RuntimeException(
				"MorphyAlg should always be initialized with empty string.");
		return 0;
	}

	protected String getSignature()
	{
		return "MORPHY_ALG";
	}

	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		String original = LinkableView.getWordString(node);

		// Don't bother looking for morphology of LEFT-WALL
		if (original.equals("LEFT-WALL")) return;

		// Do not look up morphology of skipped-words.
		if (original.charAt(0) == '[') return;

		// Work-around for bug in WordNet.  WordNet incorrectly
		// converts "his" to "him". We really don't want that.
		if (original.equals("his")) return;

		String pos = LinkableView.getPOS(node);
		if (pos == null)
			throw new RuntimeException("All nodes with 'str' should have 'POS'");

		Morphed m = context.getMorphy().morph(original);
		FeatureNode f = null;
		if (pos.equals("noun"))
			f = m.getNoun();
		else if (pos.equals("verb"))
			f = m.getVerb();
		else if (pos.equals("adj"))
			f = m.getAdj();
		else if (pos.equals("adv"))
			f = m.getAdv();
		else if (pos.equals("WORD"))
		{
			// For a single word, try to find one that
			// is different from the original word.
			FeatureNode tmp = null;
			for (int i = 0; i < 4; i++)
			{
				if (i == 0) tmp = m.getNoun();
				if (i == 1) tmp = m.getVerb();
				if (i == 2) tmp = m.getAdj();
				if (i == 3) tmp = m.getAdv();

				if (tmp != null &&
				    !original.equalsIgnoreCase(tmp.get("root").getValue()))
				{
					f = tmp;
					break;
				}
			}
		}

		if (f != null) {
			node.set("morph", f);
			String root = f.get("root").getValue();
			if (!original.equalsIgnoreCase(root))
				node.set("str", f.get("root"));
		}
	}

	protected Map<String,FeatureNode> canApplyTo(FeatureNode node)
	{
		if ((!node.isValued()) && (node.get("str") != null))
			return new HashMap<String,FeatureNode>();
		return null;
	}
}
