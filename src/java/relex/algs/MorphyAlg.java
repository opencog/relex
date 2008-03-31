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

import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.morphy.Morphy;
import relex.morphy.Morphed;
import relex.parser.LinkParserClient;

/**
 * Uses Morphy to generate the root of a word.
 */
public class MorphyAlg extends SentenceAlgorithm
{
	protected int init(String s) {
		if (s.length() > 0)
			throw new RuntimeException(
				"MorphyAlg should always be initialized with empty string.");
		return 0;
	}

	protected String getSignature() {
		return "MORPHY_ALG";
	}

	protected void applyTo(FeatureNode node, LinkParserClient lpc)
	{
		LinkableView w = new LinkableView(node);
		String original = w.getWordString();
		// not thread-safe
		// Morphed m = Morphy.getInstance().morph(original);
		Morphed m = new Morphy().morph(original);
		String pos = w.getPOS();
		if (pos == null)
			throw new RuntimeException("All nodes with 'str' should have 'POS'");
		FeatureNode f = null;
		if (pos.equals("noun"))
			f = m.getNoun();
		else if (pos.equals("verb"))
			f = m.getVerb();
		else if (pos.equals("adj"))
			f = m.getAdj();
		else if (pos.equals("adv"))
			f = m.getAdv();
		node.set("orig_str", node.get("str"));
		if (f != null) {
			node.set("morph", f);
			String root = f.get("root").getValue();
			if (!original.equalsIgnoreCase(root))
				node.set("str", f.get("root"));
		}
	}

	protected boolean canApplyTo(FeatureNode node) {
		return (!node.isValued()) && (node.get("str") != null);
	}
}
