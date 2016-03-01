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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import relex.output.PrologList;

/**
 * FeatureNodes may store either a set of key-value pairs, where the
 * values are other FeatureNodes, or they may store a single string.
 *
 * XXX FIXME: The FeatureNode class is nothing more than a home-grown
 * version of a standard key-value aka frame-slot class. It should
 * be replaced by, or, at least, inherit from, some standard java
 * class library implementing these functions.
 *
 * This class also needs to be refactored: by attempting to store
 * either a string value or set of keyed nodes, it has to check
 * for bad access and throw exceptions when this occurs. It would
 * have been more effective to either run-time up-cast, and to
 * implement LISP-like cons, pair, car and cdr primitves to avoid
 * this confusion.
 */
public class FeatureNode extends Atom
{
	private static final long serialVersionUID = -1498736655984934453L;

	// Width of output of toString(), before it is truncated with "..."
	private static final int PRINT_LIMIT = 300;

	private static FeatureNameFilter DEFAULT_FEATURE_NAME_FILTER = new FeatureNameFilter();

	public static FeatureNameFilter defaultFilter()
	{
		return DEFAULT_FEATURE_NAME_FILTER;
	}

	/**
	 * The set of all FeatureNodes which have features pointing to this one.
	 * This is used to properly implement mergeWith(other: FeatureNode) In this
	 * case, the other is destroyed, and the parents must be notified in order
	 * to reset their features to point to the new FeatureNode.
	 */
	private HashSet<FeatureNode> parents;

	/**
	 * FeatureNodes may store either a set of key-value pairs, where the
	 * values are other FeatureNodes, or they may store a single string.
	 * Most access routines to this class will throw an exception if
	 * the wrong one of these two different things is requested.
	 */
	private HashMap<String,FeatureNode> kv_pairs;
	private String value;

	/**
	 * By default, feature structures have no string value
	 */
	public FeatureNode()
	{
		parents = new HashSet<FeatureNode>();
		kv_pairs = new HashMap<String,FeatureNode>();
		value = null;
	}

	/**
	 */
	public FeatureNode(String str)
	{
		this();

	 	// If str can be interpreted as a FeatureAction, it is.
		// Otherwise, str is interpreted as the value of this FeatureNode.
		//
		// But this is used only during development of new relex rules;
		// do not do this in ordinary production code. In particular,
		// this code will mis-identify any lingering HTML markup, and
		// crash the system! (We shouldn't feed relex any HTML, but
		// sometimes some slips through ..)
		//
		//if (str.length() > 0 && str.charAt(0) == '<') {
		//	String[] lines = str.split("\\n");
		//	for (int i = 0; i < lines.length; i++) {
		//		FeatureAction act = new FeatureAction(lines[i]);
		//		act.doAction(this);
		//	}
		//} else {
		//	forceValue(str);
		//}
		forceValue(str);
	}

	protected Iterator<FeatureNode> getParents()
	{
		return parents.iterator();
	}

	/**
	 * @return true if the node value is simply a string.
	 */
	public boolean isValued()
	{
		return kv_pairs == null;
	}

	/**
	 * Force node to store a hash of key-value pairs
	 */
	public void forceFeatures()
	{
		if (value != null)
			throw new RuntimeException(
					"Must set value to null before forcing features.");
		if (kv_pairs == null)
			kv_pairs = new HashMap<String,FeatureNode>();
	}

	/**
	 * Force node to store a single string value.
	 */
	public void forceValue(String val)
	{
		if (kv_pairs != null) {
			if (getFeatureNames().size() > 0)
				throw new RuntimeException(
						"Must clear features before forcing value.");
		}
		kv_pairs = null;
		setValue(val);
	}

	/**
	 * @return true if this feature structure has no values or features
	 */
	public boolean isEmpty()
	{
		if (isValued())
			return value == null;
		return kv_pairs.size() == 0;
	}

	/**
	 * Sets the value of this feature node. Method will fail if this FeatureNode
	 * is not valued, by throwing RuntimeException.
	 *
	 * XXX this copies the string ... why ????
	 *
	 * @param value
	 *            the value of this feature node
	 */
	public void setValue(String value)
	{
		if (!isValued())
			throw new RuntimeException(
					"Cannot set the value of a non-valued FeatureNode");
		if (value == null)
			throw new RuntimeException("Cannot set to null value");
		// this.value = new String(value);
		this.value = value;
	}

	/**
	 * @return the value of this FeatureNode, or null if it has none
	 */
	public String getValue()
	{
		if (!isValued())
			throw new RuntimeException("non-valued FeatureNodes have no string value");
		return value;
	}

	/**
	 * Perform a key-value lookup.
	 * @param key
	 *        the name of a feature to be looked up.
	 * @return the string value associated with the key, else
	 *         null, if the thing associated with the key is
	 *         a non-string-valued FeatureNode.
	 */
	public String featureValue(String key)
	{
		FeatureNode f = get(key);
		if (f == null)
			return null;
		return f.getValue();
	}

	/**
	 * Convenience method to create a feature node and add it to this one.
	 * Throws RuntimeException if this FeatureNode is string-valued.
	 * @param key
	 * @return the new FeatureNode
	 */
	public FeatureNode add(String key)
	{
		if (isValued())
			throw new RuntimeException(
					"Cannot add a key for a string-valued FeatureNode");
		FeatureNode f = new FeatureNode();
		set(key, f);
		return f;
	}

	public void add(String key, String value)
	{
		FeatureNode fn = add(key);
		fn.forceValue(value);
	}

	/**
	 * Substitutes oldF with newF in any of the feature targets of this node.
	 */
	public void substitute(FeatureNode oldF, FeatureNode newF)
	{
		if (oldF == newF)
			return;
		Iterator<String> i = getFeatureNames().iterator();
		while (i.hasNext()) {
			String name = i.next();
			if (get(name).equiv(oldF))
				set(name, newF);
		}
	}

	/**
	 * Merges other into this, throwing an exception if they are not unifiable.
	 * If "this" isEmpty, then it is replaced with other, which means that after
	 * calling this method, "this" is not guaranteed to be a legitimate part of
	 * the entire feature structure you were dealing with.
	 *
	 * XXX -- this is somewhat strangely named, it doesn't really
	 * merge, per-se, since if both featurenodes have features
	 * in them, then it throws an exeption if the features aren't
	 * identical. So really, a "merge" occurs only when one of the
	 * featurenodes is empty.
	 *
	 * What's even crazier, the SentenceAlgorithm.java silently
	 * eats the exceptions thrown here. So its all pretty pointless...
	 *
	 * So basically, this is just kind of crazy, I think.  This
	 * makes sense only if the algs files are cleanly built...
	 *
	 * Returns the merged node.
	 */
	public FeatureNode mergeWith(FeatureNode other)
	{
		if (other == this)
			return this;
		if (other.isEmpty()) {
			other.replaceSelfWith(this);
			return other;
		}
		if (isEmpty()) {
			replaceSelfWith(other);
			return this;
		}

		// Throw execeptions if the two FeatureNodes are non-unifiable
		if (isValued())
		{
			if ((!other.isValued())
					|| (!other.getValue().equals(getValue())))
			{
				throw new RuntimeException(
						"Cannot merge a nonvalued node or a node with a different value into a non-empty valued node");
			}
		}
		else
		{ // this has features
			if (other.isValued())
			{ // other has value
				throw new RuntimeException(
						"Cannot merge a non-empty valued node into a non-empty normal feature node");
			}

			// other has features
			for (String fName : other.getFeatureNames())
			{
				FeatureNode otherf = other.get(fName);
				FeatureNode thisf = this.get(fName);

				if (otherf != thisf)
				{
					throw new RuntimeException(
						"Cannot merge two non-valued feature nodes with inconsistent features.\n" +
						"\tSuggest using += instead of = in algs file. fName = " + fName + "\n" +
						"\tthis = " + thisf.get("orig_str") + "-" + thisf.get("index_in_sentence") + "\n" +
						"\tother = " + otherf.get("orig_str") + "-" + otherf.get("index_in_sentence") + "\n");
				}
			}
		}

		return this;
	}

	public FeatureNode copyInto(FeatureNode other)
	{
		if (other == this)
			return this;

		// Throw execeptions if the two FeatureNodes are non-unifiable
		if (isValued())
		{
			if (!other.isValued())
			{
				throw new RuntimeException(
						"Cannot merge a nonvalued node into a non-empty valued node");
			}
		}
		else
		{ // this has features
			if (other.isValued())
			{ // other has value
				throw new RuntimeException(
						"Cannot merge a non-empty valued node into a non-empty normal feature node");
			}
		}

		// After checking for inconsistencies above, we can safely execute the
		// code below
		if (!isValued())
		{
			for (String fName : other.getFeatureNames())
			{
				set(fName, other.get(fName));
			}
		}
		return this;
	}

	public void replaceSelfWith(FeatureNode other)
	{
		if (other == this)
			return;
		// Create a new hashset to avoid ConcurrentModificationException.
		// That is, we will be modifying parents as we iterate through its
		// *copy*.
		Iterator<FeatureNode> i = new HashSet<FeatureNode>(parents).iterator();
		while (i.hasNext()) {
			FeatureNode p = i.next();
			p.substitute(this, other);
		}
		if (!parents.isEmpty())
			throw new RuntimeException("replace self failed");
	}

	/**
	 * Associates key to the target FeatureNode. Drops references to the
	 * existing target associated with this key, if any.  An exiting target
	 * is notified that it must update it's parent set. Setting to null
	 * removes the feature from this FeatureNode. If this FeatureNode is
	 * string-valued, the method will fail and throw RuntimeException.
	 *
	 * @param key
	 *            the key that will reference the target.
	 * @param target
	 *            the FeatureNode to set it to.
	 */
	public void set(String key, FeatureNode target)
	{
		if (isValued())
			throw new RuntimeException("Cannot set key-value pair for a string-valued FeatureNode");
		if (key == null)
			throw new RuntimeException("key must be non-null");
		FeatureNode oldTarget = get(key);
		if (target == null) {
			kv_pairs.remove(key);
		} else {
			target.parents.add(this);
			kv_pairs.put(key, target);
		}
		if (oldTarget != null) {
			// If there are no other features pointing to the old target,
			// remove this from the list of oldTarget's parents.
			if (!kv_pairs.containsValue(oldTarget))
				oldTarget.parents.remove(this);
		}
	}

	/**
	 * Returns a feature's value, i.e. the node associated with
	 * the name "key". If this node is string-valued, NULL is returned.
	 *
	 * @param key
	 * @return the value of the feature, or null if it has no value
	 */
	public FeatureNode get(String key)
	{
		if (isValued())
			throw new RuntimeException("String-valued FeatureNodes have no keys.");
		if (key == null)
			throw new RuntimeException("Key was null");
		return kv_pairs.get(key);
	}

	// Like "get" but makes the feature node if it doesn't exist.
	public FeatureNode getOrMake(String key)
	{
		FeatureNode ret = get(key);
		if (ret == null) {
			ret = new FeatureNode();
			set(key, ret);
		}
		return ret;
	}

	/**
	 * Returns the set of keys in this FeatureNode
	 *
	 * @return the set of feature names
	 */
	public Set<String> getFeatureNames()
	{
		if (isValued())
			throw new RuntimeException("valued FeatureNodes have no features");
		return kv_pairs.keySet();
	}

	/**
	 * Recursive function implements toString, keeping track of how far to
	 * indent each level, and appending values to a StringBuffer.
	 *
	 * The use of an "already visited" hashset lowers performance
	 * dramatically, but makes this routine thread-safe in principle.
	 *
	 * @param indents
	 *            the number of double spaces to indent the output
	 * @param sb
	 *            the string buffer to output to
	 * @param indices
	 *            a map from FeatureNodes to indices
	 * @param alreadyVisited
	 *            the set of FeatureNodes which have already been visited
	 * @param filter
	 *            a filter that controls which features are printed, and their
	 *            order
	 */
	protected void _toString(int indents, StringBuffer sb,
			HashMap<FeatureNode,Integer> indices,
			HashSet<FeatureNode> alreadyVisited, FeatureNameFilter filter)
	{
		// Get index and return if its already been printed.
		Integer index = (Integer) indices.get(this);
		int indexIndents = 0;
		if (index != null) {
			sb.append("$").append(index.toString());
			if (alreadyVisited.contains(this) && !isValued())
				return;
			indexIndents = (index.intValue() > 9 ? 3 : 2);
		}
		// else
		alreadyVisited.add(this);

		// Don't print too deep into a feature structure.
		if (indents > PRINT_LIMIT) {
			sb.append("...");
			return;
		}

		// For string-valued feature nodes, print value and return.
		if (isValued()) {
			sb.append(toString());
			return;
		}

		// Print current node.
		sb.append("[");

		// Set the number of spaces to indent features in this node.
		StringBuffer spaces = new StringBuffer();
		for (int j = 0; j < indents + indexIndents + 1; j++)
			spaces.append(" ");
		boolean firstLoop = true;

		// For each key, recurse.
		Iterator<String> i = features(filter);
		while (i.hasNext()) {
			if (firstLoop)
				firstLoop = false;
			else
				sb.append("\n").append(spaces);
			String featName = i.next();
			int newindents = indents + indexIndents + 2 + featName.length();
			sb.append(featName).append(" ");
			get(featName)._toString(newindents, sb, indices, alreadyVisited,
					filter);
		}
		sb.append("]");
	}

	/*
	 * Creates a map from FeatureNodes that appear more than once,
	 * to integers.  Used by toString() to print indices only once
	 * when FeatureNodes appear more than once.
	 */
	private HashMap<FeatureNode,Integer>
		makeIndices(HashMap<FeatureNode,Integer> indices, HashSet<FeatureNode> alreadyVisited,
			FeatureNameFilter filter)
	{
		if (alreadyVisited.contains(this)) {
			if (!indices.keySet().contains(this))
				indices.put(this, indices.size());
			return indices;
		}
		alreadyVisited.add(this);
		if (!isValued()) {
			Iterator<String> i = features(filter);
			while (i.hasNext()) {
				String s = i.next();
				get(s).makeIndices(indices, alreadyVisited, filter);
			}
		}
		return indices;
	}

	/**
	 * Returns a pretty-printed, multi-line indented string representing
	 * the contents of this FeatureNode. The filter is used to determine
	 * which nodes are printed.
	 *
	 * @return
	 */
	public String toString(FeatureNameFilter filter)
	{
		if (isValued())
			return "<<" + getValue() + ">>";
		StringBuffer sb = new StringBuffer();
		_toString(0, sb, makeIndices(new HashMap<FeatureNode,Integer>(), new HashSet<FeatureNode>(), filter),
				new HashSet<FeatureNode>(), filter);
		return sb.toString();
	}

	public String toString()
	{
		return toString(DEFAULT_FEATURE_NAME_FILTER);
	}

	/**
	 * pathTarget() -- find the feature node at the end of the path
	 * @return feature or null if not in path
	 *
	 * This routine walks the indicated chain of keys, looking
	 * up each in turn. If any key in the path is not found,
	 * null is returned. If particular, if the very first
	 * path element is not found, null is returned. If the
	 * navigation was successful, then the FeatureNode at
	 * the end of the path is returned.
	 *
	 * Example usage:
	 * pathTarget(new FeaturePath("<ref noun_number>"));
	 */
	public FeatureNode pathTarget(FeaturePath path)
	{
		FeatureNode cur = this;
		Iterator<String> feats = path.iterator();
		while (feats.hasNext() && cur != null) {
			if (cur.isValued())
				return null;
			cur = cur.get(feats.next());
		}
		return cur;
	}

	public FeatureNode pathTarget(String str)
	{
		return pathTarget(new FeaturePath(str));
	}

	/**
	 * pathValue() --
	 * Returns the value of the path target -- or null if the path does not exist.
	 */
	public String pathValue(FeaturePath path)
	{
		FeatureNode target = pathTarget(path);
		if (target == null)
			return null;
		return target.getValue();
	}

	public String pathValue(String str)
	{
		FeaturePath path = new FeaturePath(str);
		FeatureNode target = pathTarget(path);
		if (target == null) return null;
		if (!target.isValued()) return null;
		return target.getValue();
	}

	/**
	 * Like pathTarget(), but creates the path if it doesn't already exist.
	 * setting its target to the passed target. If forceTarget is false, then an
	 * error will be thrown if the path does not unify with this FeatureNode.
	 */
	public void makePath(FeaturePath path, FeatureNode target,
	                     boolean forceTarget)
	{
		FeatureNode cur = this;
		Iterator<String> feats = path.iterator();
		while (feats.hasNext()) {
			FeatureNode last = cur;
			String name = feats.next();
			cur = cur.get(name);
			if (cur == null) {
				if (feats.hasNext()) {
					cur = new FeatureNode();
				} else {
					cur = target;
					if ((!forceTarget) && last.get(name) != null)
						throw new RuntimeException("Path already exists");
				}
				last.set(name, cur);
			}
		}
	}

	public void makePath(FeaturePath path, FeatureNode target)
	{
		makePath(path, target, false);
	}

	/**
	 * equiv() -- returns true if two references point to the same object.
	 * @return true if equivalent, else false
	 * @param other pointer to the object being compared to
	 *
	 * The standard comp-sci name for this is "equiv", and not "equals".
	 * The method "equals" will return true if two objects contain the
	 * same data; whereas this method return true only if its one and the
	 * same object.
	 *
	 * This routine is used to handle the case where two features are set
	 * to each other even though they have no value. In this case, we
	 * might have something like: <a b c> = <d e f>. The target of both
	 * these paths will be the same object: an empty FeatureNode.
	 *
	 */
	public final boolean equiv(Object other)
	{
		// MUST BE ==
		return other == this;
	}

	/*
 	 * features() -- returns a list of the features minus those specified by a filter
 	 * @return an iterator to an ArrayList containing those features of this FeatureNode not in the filter's ignore list
 	 * @param filter the FeatureNameFilter
	 * This routine returns the feature node's features without
	 * the features specified by the filter pa
	 */
	public Iterator<String> features(FeatureNameFilter filter)
	{
		ArrayList<String> output = new ArrayList<String>();
		HashSet<String> featureNamesCopy = new HashSet<String>(getFeatureNames());

		ArrayList<String> ignored = new ArrayList<String>();
		for(String aFilter: filter.getIgnoreSet()){
			filter.transferMultiNames(ignored, featureNamesCopy, aFilter);
		}
		filter.transfer(output, featureNamesCopy);
		return output.iterator();
	}

	/**
	 * Debugging function -- return string containing key names.
	 */
	public String _prt_keys()
	{
		if (isValued()) return "";
		String ret = "";
		Iterator<String> j = getFeatureNames().iterator();
		while (j.hasNext()) {
			String stuff = j.next();
			ret += stuff + " ";
		}
		ret += "\n";
		return ret;
	}

	/**
	 * Debugging function -- return list of key-value pairs.
	 */
	public String _prt_vals()
	{
		if (isValued()) return "this=" + value;
		String ret = "";
		Iterator<String> j = getFeatureNames().iterator();
		while (j.hasNext()) {
			String stuff = j.next();
			ret += stuff + "=";
			String v = pathValue("<" + stuff + ">");
			if (v == null) ret += "@";
			else ret += v;
			ret += "\n";
		}
		return ret;
	}

	// Test method
	static public void main(String[] args)
	{
		FeatureNode three = new FeatureNode("3");
		FeatureNode root = new FeatureNode();
		root.add("a").add("b").add("c").set("d", three);
		root.get("a").set("c", new FeatureNode("4"));
		root.add("a0").add("b0").set("c0", root.get("a").get("b").get("c"));
		System.out.println(root);

	}
}

// =========================== End of File ===============================
