package relex.feature;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FeatureNameFilter
{
	Set<String> ignoreSet;

	private ArrayList<String> featureOrder;

	void transferMultiNames(ArrayList<String> output, Set<String> featureNames, String namePrefix) {
		transferName(output, featureNames, namePrefix);
		Iterator<String> i = featureNames.iterator();

		// Prefixed names are ordered alphabetically by their suffix. (??? Why bother ???)
		TreeSet<String> ts = new TreeSet<String>();
		while (i.hasNext()) {
			String s = i.next();
			if (s.indexOf(namePrefix) == 0) ts.add(s);
		}
		i = ts.iterator();
		while (i.hasNext())
			transferName(output, featureNames, i.next());
	}

	private void transferName(ArrayList<String> output, Set<String> featureNames, String name) {
		if (featureNames.contains(name)) {
			featureNames.remove(name);
			output.add(name);
		}
	}

	private void copyRest(ArrayList<String> output, Set<String> featureNames) {
		TreeSet<String> t = new TreeSet<String>(featureNames);
		Iterator<String> i = t.iterator();
		while (i.hasNext()) {
			output.add(i.next());
		}
	}

	/*
	 * Copy feature names to output, optionally sorting them with a specified
	 * sort order. The sort order is curious: if the sort list contains an empty
	 * string, this halts sorting, and everything following the empty string is
	 * placed at the end!!
	 */
	protected void transfer(ArrayList<String> output, Set<String> featureNames) {
		Iterator<String> i = featureOrder.iterator();
		boolean foundEmpty = false;

		// Transfer all features before the unexpected ones.
		while (i.hasNext() && !foundEmpty) {
			String s = i.next();
			if (s.equals(""))
				foundEmpty = true; // null-string delimiter
			else
				transferMultiNames(output, featureNames, s);
		}

		// Store all features for putting after the unexpected ones in
		// afterOthers
		ArrayList<String> afterOthers = new ArrayList<String>();
		while (i.hasNext()) transferMultiNames(afterOthers, featureNames, i.next());

		// Transfer all the unexpected features
		copyRest(output, featureNames);

		// Transfer all the afterOthers
		i = afterOthers.iterator();
		while (i.hasNext()) output.add(i.next());
	}

	public FeatureNameFilter() {
		ignoreSet = new HashSet<String>();
		featureOrder = new ArrayList<String>();
	}

	public FeatureNameFilter(Set<String> ignores, ArrayList<String> order) {
		this();
		ignoreSet.addAll(ignores);
		if (order != null)
			featureOrder.addAll(order);
		else {
			featureOrder.add("");
		}
	}

	protected Set<String> getIgnoreSet(){
		return ignoreSet;
	}
}

// ================================= End of File ===============================
