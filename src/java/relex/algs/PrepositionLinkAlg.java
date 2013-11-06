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

/**
 * XXX It would be more elegant if a new kind of markup was invented for this,
 * and TemplateActionAlg did the heavy lifting here. That way, we could
 * obsolete this class.  i.e. we need some way of saying "get the string
 * of this thing" and having TemplateActionAlg do the string manipulation.
 * Because, really, this alg doesn't really do anything sophisticatted, at all.
 */
public class PrepositionLinkAlg extends TemplateActionAlg
{
	private void applyTo(FeatureNode modifiedRef,
	                       FeatureNode prepObj,
	                       FeatureNode prepStringValue,
	                       FeatureNode prepStringSource)
	{
		String prep = prepStringValue.getValue();
		FeatureNode modifiedLinks = modifiedRef.getOrMake("prep-links");

		FeatureNode existingPrepObj = modifiedLinks.get(prep);

		// Add prep to links, converting to a group if necessary
		if (existingPrepObj == null)
		{
			// System.err.println("Debug: PREP does not exist");
			modifiedLinks.set(prep, prepObj);
		}
		else
		{
			// System.err.println("Debug: PREP exists");

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
		 * System.err.println("\nDebug: Calling PrepositionLinkAlg:");
		 * System.err.println("\tModified word: " + modified.get("str"));
		 * System.err.println("\tprep: " + prepStringValue);
		 * System.err.println("\tprep obj: " + prep_obj.get("name"));
		 * System.err.println("\tprep source: " + prep_source.get("str"));
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

		// The superclass is TemplateActionAlg -- go and run its apply method.
		// That allows us to perform some additional bits & teeaks as needed,
		// and lessesn the difference between this alg and the emplateActionAlg.
		super.applyTo(node, context, vars);
	}
}

/* =================================== END OF FILE ===================== */
