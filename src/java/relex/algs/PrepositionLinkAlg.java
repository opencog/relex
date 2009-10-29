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

import java.util.Iterator;
import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;
import relex.feature.SemanticView;

public class PrepositionLinkAlg extends TemplateMatchingAlg
{
	private void applyTo(FeatureNode modifiedRef,
	                       FeatureNode prepObj,
	                       FeatureNode prepStringValue,
	                       FeatureNode prepStringSource)
	{
		String prep = prepStringValue.getValue();
		FeatureNode modifiedLinks = modifiedRef.getOrMake("prep-links");
		FeatureNode modifiedLinkSources = modifiedRef.getOrMake("linkSources");

		FeatureNode existingPrepObj = modifiedLinks.get(prep);
		// add prep to links, converting to a group if necessary
		if (existingPrepObj == null)
		{
			// System.err.println("PREP does not exist");
			modifiedLinks.set(prep, prepObj);
			modifiedLinkSources.set(prep, prepStringSource);
		}
		else
		{
			// System.err.println("PREP exists");

			// add a new indexed-prep
			int i = 2;
			String indexedPrep = prep + i;
			// Find an unused index
			while (modifiedLinks.get(indexedPrep) != null)
			{
				i++;
				indexedPrep = prep + i;
			}
			modifiedLinks.set(indexedPrep, prepObj);
			modifiedLinkSources.set(indexedPrep, prepStringSource);
		}
	}

	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		FeatureNode modified = getTemplate().val("modified", vars);
		FeatureNode prepStringValue = getTemplate().val("prep", vars);
		FeatureNode prep_obj = getTemplate().val("prep_obj", vars);
		FeatureNode prep_source = getTemplate().val("prep_source", vars);

		/*
		 * System.err.println("\nCalling PrepositionLinkAlg:");
		 * System.err.println("Modified word: " + modified.get("str"));
		 * System.err.println("prep: " + prepStringValue);
		 * System.err.println("prep obj: " + prep_obj.get("name"));
		 * System.err.println("prep source: " + prep_source.get("str"));
		 */

		if (SemanticView.isGroup(modified))
		{
			// If the modified object is a group, apply prep to each of its
			// elements
			Iterator<FeatureNode> i = SemanticView.groupMemberIterator(modified);
			while (i.hasNext())
			{
				applyTo(i.next().get("ref"), prep_obj, prepStringValue, prep_source);
			}

		}
		else
		{
			// Otherwise, apply to just the one element.
			applyTo(modified.get("ref"), prep_obj, prepStringValue, prep_source);
		}
	}
}

/* =================================== END OF FILE ===================== */
