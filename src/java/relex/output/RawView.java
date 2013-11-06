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
package relex.output;

import java.util.HashSet;
import java.util.ArrayList;

import relex.feature.FeatureNameFilter;
import relex.feature.FeatureNode;

/* Various different views of the parsed sentence */
public class RawView
{
	public static String printSem(FeatureNode head,
	                       FeatureNode background,
	                       FeatureNameFilter filter)
	{
		if (head == null)
			return "";
		if (head.isEmpty())
			return "";
		FeatureNode headSet = new FeatureNode();
		headSet.set("head", head);
		headSet.set("background", background);

		return headSet.toString(filter);
	}

	public static FeatureNameFilter getZHeadsFilter()
	{
		HashSet<String> ignores = new HashSet<String>();

		ignores.add("nameSource");

		ignores.add("syn_location");
		ignores.add("SIG");
		ignores.add("linkR0");
		ignores.add("linkR1");
		ignores.add("linkR2");
		ignores.add("linkR3");
		ignores.add("linkR4");
		ignores.add("linkR5");
		ignores.add("linkR6");
		ignores.add("linkR7");
		ignores.add("linkL0");
		ignores.add("linkL1");
		ignores.add("linkL2");
		ignores.add("linkL3");
		ignores.add("linkL4");
		ignores.add("linkL5");
		ignores.add("linkL6");
		ignores.add("linkL7");
		ignores.add("first_verb");
		ignores.add("HEAD-FLAG");
		ignores.add("POS");
		ignores.add("head-word");
		ignores.add("morph");
		ignores.add("num");
		ignores.add("num_left_links");
		ignores.add("num_right_links");
		ignores.add("str");
		ignores.add("ref");
		ignores.add("subj");
		ignores.add("obj");
		ignores.add("iobj");
		// ignores.add("that");
		ignores.add("this");
		ignores.add("wall");
		ignores.add("COMP-FLAG");
		ignores.add("VTAlg_flag");
		ignores.add("comparative-name");
		ignores.add("comparative-nameSource");
		ignores.add("comparative-obj");
		ignores.add("comparative-obj-name");
		ignores.add("comparative-obj-nameSource");
		ignores.add("comp-obj-copy");
		ArrayList<String> order = new ArrayList<String>();
		order.add("_subj");
		order.add("_obj");
		order.add("_iobj");
		order.add("name");
		order.add("tense");
		order.add("PREP-FLAG");
		order.add("links");
		order.add("");
		order.add("head");
		order.add("background");
		order.add("words");
		return new FeatureNameFilter(ignores, order);
	}

	/**
	 * Print the so-called "raw" relex output, in structured form.
	 * Used primarily in debugging and development.
	 */
	public static String printZHeads(FeatureNode left)
	{
		if (left == null) return "";

		String out = printSem(left.get("head"),
		                      left.get("background"),
		                      getZHeadsFilter());
		return out;
	}

	/**
	 * Print the entire relex graph. Caution, large!
	 */
	public static String printZHeadsComplete(FeatureNode left)
	{
		if (left == null) return "";
		FeatureNameFilter filter =
			      new FeatureNameFilter(new HashSet<String>(),
		                               new ArrayList<String>());
		String out = printSem(left.get("head"),
		                      left.get("background"), filter);
		return out;
	}
}

/* =========================== END OF FILE ============== */
