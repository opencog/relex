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

package relex.feature;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * SemanticViews can represent either SimpleConcepts, Relations, or Groups
 */
public class SemanticView extends View
{
	private static final String MEMBER_PREFIX = "member";

	private static final String NAME_FEATURE = "name";

	private static final String NEGATION_FEATURE = "NEGATIVE-FLAG";

	private static final String HYP_FEATURE = "HYP";

	private static final String STRENGTH_FEATURE = "strength";

	private static final String CONFIDENCE_FEATURE = "confidence";

	private static final String IMPORTANCE_FEATURE = "importance";

	private static final String SPECIFIC_FEATURE = "specific";

	public SemanticView(FeatureNode ths) {
		super(ths);
	}

	public String toString() {
		return toString(fn());
	}

	public static String toString(FeatureNode ths) {
		return ths.toString(FeatureNode.defaultFilter());
	}

	public String getName() {
		return getName(fn());
	}

	public static String getName(FeatureNode ths) {
		FeatureNode f = ths.get(NAME_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setName(String name) {
		setName(fn(), name);
	}

	public static void setName(FeatureNode ths, String name) {
		FeatureNode f = ths.get(NAME_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(NAME_FEATURE, f);
		}
		f.forceValue(name);
	}

	public String getNeg() {
		return getNeg(fn());
	}

	public static String getNeg(FeatureNode ths) {
		FeatureNode f = ths.get(NEGATION_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setNeg(String val) {
		setNeg(fn(), val);
	}

	public static void setNeg(FeatureNode ths, String val) {
		FeatureNode f = ths.get(NEGATION_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(NEGATION_FEATURE, f);
		}
		f.forceValue(val);
	}

	public String getHyp() {
		return getHyp(fn());
	}

	public static String getHyp(FeatureNode ths) {
		FeatureNode f = ths.get(HYP_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setHyp(String val) {
		setHyp(fn(), val);
	}

	public static void setHyp(FeatureNode ths, String val) {
		FeatureNode f = ths.get(HYP_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(HYP_FEATURE, f);
		}
		f.forceValue(val);
	}

	public String getSpecific() {
		return getSpecific(fn());
	}

	public static String getSpecific(FeatureNode ths) {
		FeatureNode f = ths.get(SPECIFIC_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setSpecific(String val) {
		setSpecific(fn(), val);
	}

	public static void setSpecific(FeatureNode ths, String val) {
		FeatureNode f = ths.get(SPECIFIC_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(SPECIFIC_FEATURE, f);
		}
		f.forceValue(val);
	}

	public String getStrength() {
		return getStrength(fn());
	}

	public static String getStrength(FeatureNode ths) {
		FeatureNode f = ths.get(STRENGTH_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setStrength(String val) {
		setStrength(fn(), val);
	}

	public static void setStrength(FeatureNode ths, String val) {
		FeatureNode f = ths.get(STRENGTH_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(STRENGTH_FEATURE, f);
		}
		f.forceValue(val);
	}

	public String getConfidence() {
		return getConfidence(fn());
	}

	public static String getConfidence(FeatureNode ths) {
		FeatureNode f = ths.get(CONFIDENCE_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setConfidence(String val) {
		setConfidence(fn(), val);
	}

	public static void setConfidence(FeatureNode ths, String val) {
		FeatureNode f = ths.get(CONFIDENCE_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(CONFIDENCE_FEATURE, f);
		}
		f.forceValue(val);
	}

	public String getImportance() {
		return getImportance(fn());
	}

	public static String getImportance(FeatureNode ths) {
		FeatureNode f = ths.get(IMPORTANCE_FEATURE);
		if (f == null)
			return null;
		return f.getValue();
	}

	public void setImportance(String val) {
		setImportance(fn(), val);
	}

	public static void setImportance(FeatureNode ths, String val) {
		FeatureNode f = ths.get(IMPORTANCE_FEATURE);
		if (f == null) {
			f = new FeatureNode("");
			ths.set(IMPORTANCE_FEATURE, f);
		}
		f.forceValue(val);
	}

	/**
	 * We keep track of whether there are an even or odd number of characters.
	 * This allows nested relations to just append their NEG features to each
	 * other.
	 */
	public boolean isNegative() {
		return isNegative(fn());
	}

	public static boolean isNegative(FeatureNode ths) {
		String n = getNeg(ths);
		if (n == null || n.length() % 2 == 0)
			return false;
		return true;
	}

	public boolean isHypothetical() {
		return isHypothetical(fn());
	}

	public static boolean isHypothetical(FeatureNode ths) {
		String n = getHyp(ths);
		if (n == null || n.length() == 0 || !n.equals("T"))
			return false;
		return true;
	}

	public boolean isSpecific() {
		return isSpecific(fn());
	}

	public static boolean isSpecific(FeatureNode ths) {
		String n = getSpecific(ths);
		if (n == null || n.length() == 0 || !n.equals("T"))
			return false;
		return true;
	}


	public boolean isGroup() {
		return isGroup(fn());
	}

	public static boolean isGroup(FeatureNode ths) {
		return groupMembers(ths).size() > 0;
	}

	private static ArrayList<FeatureNode> groupMembers(FeatureNode ths) {
		ArrayList<FeatureNode> members = new ArrayList<FeatureNode>();
		if (ths.isValued())
			return members;
		Iterator<String> i = ths.getFeatureNames().iterator();
		while (i.hasNext()) {
			String name = i.next();
			if ((name.length() > MEMBER_PREFIX.length())
					&& name.substring(0, MEMBER_PREFIX.length()).equals(
							MEMBER_PREFIX)) {
				members.add(ths.get(name));
			}
		}
		return members;
	}

	public Iterator<FeatureNode> groupMemberIterator() {
		return groupMemberIterator(fn());
	}

	public List<FeatureNode> groupMembers(){
		return groupMembers(fn());
	}

	public static Iterator<FeatureNode> groupMemberIterator(FeatureNode ths) {
		return groupMembers(ths).iterator();
	}

	/**
	 * Creates a new group which contains this SemanticView.
	 */
	public static FeatureNode newGroupFromSelf(FeatureNode ths) {
		if (isGroup(ths))
			return ths;
		FeatureNode group = new FeatureNode();
		SemanticView.addGroupMember(group, ths);
		return group;
	}

	public void addGroupMember(FeatureNode other) {
		addGroupMember(fn(), other);
	}

	public static void addGroupMember(FeatureNode ths, FeatureNode other) {
		// for consistency sake, we never nest groups -- we just keep one
		// top-level group
		if (isGroup(other)) {
			Iterator<FeatureNode> i = groupMemberIterator(other);
			while (i.hasNext())
				addGroupMember(ths, i.next());
		} else {
			if (hasGroupMember(ths, other))
				return;
			int numGroupMembers = groupMembers(ths).size();
			ths.set(MEMBER_PREFIX + numGroupMembers, other);
		}
	}

	/**
	 * Removes a group member, and shifts all the others down.
	 */
	public void removeGroupMember(FeatureNode other) {
		removeGroupMember(fn(), other);
	}

	protected static void removeGroupMember(FeatureNode ths, FeatureNode other) {
		int numGroupMembers = groupMembers(ths).size();
		boolean found = false;
		for (int i = 0; i < numGroupMembers; i++) {
			FeatureNode mem = ths.get(MEMBER_PREFIX + i);
			if (mem == other) {
				found = true;
			}
			if (found) {
				if (i == numGroupMembers - 1)
					ths.set(MEMBER_PREFIX + i, null);
				else
					ths
							.set(MEMBER_PREFIX + i, ths.get(MEMBER_PREFIX
									+ (i + 1)));
			}
		}
	}

	protected boolean hasGroupMember(FeatureNode other) {
		return hasGroupMember(fn(), other);
	}

	protected static boolean hasGroupMember(FeatureNode ths, FeatureNode other)
	{
		Iterator<FeatureNode> i = groupMemberIterator(ths);
		while (i.hasNext())
		{
			if (i.next().equiv(other))
				return true;
		}
		return false;
	}

	/**
	 * Repairs this group if it is a member of another group
	 * It adds all of the members of this group to the "parent" group
	 */
	public void repairGroup() {
		repairGroup(fn());
	}

	public static void repairGroup(FeatureNode ths) {
		if (!SemanticView.isGroup(ths))
			return;
		Iterator<FeatureNode> i = ths.getParents();
		HashSet<SemanticView> semanticParents = new HashSet<SemanticView>();
		// Note that FeatureNode.getParents does not return the parents
		// in the SemanticView sense. We have to check for that explicitly:
		while (i.hasNext()) {
			SemanticView sp = new SemanticView(i.next());
			if (sp.hasGroupMember(ths)) {
				semanticParents.add(sp);
				// cannot add and remove "ths" here, because we are
				// still iterating through ths.getParents().
				// changing ths here, would violate iterator contract
				// on parents of ths
			}
		}

		// Must do process in 2 steps to prevent violating iterator contract.
		Iterator<SemanticView> j = semanticParents.iterator();
		while (j.hasNext()) {
			SemanticView sp = j.next();
			sp.removeGroupMember(ths);
			sp.addGroupMember(ths); // this add will eliminate the nested group
		}
	}

	public String toPrettyString() {
		return toPrettyString(fn());
	}

	public static String toPrettyString(FeatureNode ths) {
		return ths.toString();
	}

	// total hack to handle conjunctions!!
	public void appendToSelf(FeatureNode other, FeatureNode parent, String featName) {
		appendToSelf(fn(), other, parent, featName);
	}

	public int hashCode() {
		return fn().hashCode();
	}

	public boolean equals(Object other)
	{
		if (other instanceof SemanticView)
			return ((SemanticView) other).fn().equiv(fn());
		return false;
	}

	public static void appendToSelf(FeatureNode ths, FeatureNode other,
	                                FeatureNode parent, String featName)
	{
		if (other.equiv(ths))
			return;
		FeatureNode group = ths;
		if (!isGroup(ths)) {
			group = newGroupFromSelf(ths);
			parent.set(featName, group);
		}
		addGroupMember(group, other);
	}
}
