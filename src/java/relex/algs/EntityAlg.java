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

import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.parser.LinkParserClient;

/**
 * Any words listed in the LinkParser dictionary as Specific are tagged here.
 * This algorithm should be applied to LinkAbles.
 */
public class EntityAlg extends TemplateMatchingAlg {

	protected void applyTo(FeatureNode node, LinkParserClient lpc) {
		LinkableView linkable = new LinkableView(node);
		String word = linkable.getWordString();
		if (word.length() >= 1) {
			boolean ise = lpc.isEntity(word);
			if (Character.isUpperCase(word.charAt(0)) || ise) {
				node.set("ENTITY-FLAG", new FeatureNode("T"));
			}
		}
	}

}

