/*
 * Copyright 2008 Novamente LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FeatureAction extends FeaturePathAndTarget
{
	static private HashSet<String> actionSeparators;

	static private FeatureTemplate blankTemplate = new FeatureTemplate();
	static private Map<String, FeatureNode> emptyVarSet = new HashMap<String, FeatureNode>();

	static {
		actionSeparators = new HashSet<String>();
		actionSeparators.add("=");
		actionSeparators.add("+=");
		actionSeparators.add("<=");
	}

	public FeatureAction(String str)
	 {
		// read in the path and target
		super(str);
		// read in the action string
	}

   public Set<String> getPathSeparators()
	{
		return actionSeparators;
	}

	public String getAction()
	{
		return getSeparator();
	}

	private void doActionEquals(FeatureNode f, FeatureNode left,
			FeatureNode right, String rightValStr)
	{
		// make left if left and right are both null
		if ((left == null) && (right == null)) {
			left = new FeatureNode();
			f.makePath(getPath(), left);
		}
		// Start handling cases:
		// CASE 1:
		if ((right == null) && (rightValStr == null)) {
			// since right is null, it must be a path that didnt exist
			f.makePath(getTargetPath(), left);
			return;
		}
		// CASE 2: rightValStr != null
		if (right == null) {
			if ((!left.isEmpty()) && (!left.isValued()))
				throw new RuntimeException(
						"Cannot set a non-empty non-valued node to a value");
			left.forceValue(rightValStr);
			return;
		}
		// CASE 3: right!=null
		if (left == null) {
			f.makePath(getPath(), right);
			return;
		}
		left.mergeWith(right);
		return;
	}

	private void doActionCopyIn(FeatureNode f, FeatureNode left,
			FeatureNode right, String rightValStr)
	{
		// Make left if left is null
		if (left == null)
		{
			left = new FeatureNode();
			f.makePath(getPath(), left);
		}
		// Start handling cases:
		// CASE 1:
		if ((right == null) && (rightValStr == null)) {
			// since right is null, it must be a path that didnt exist
			f.makePath(getTargetPath(), left);
			return;
		}
		// CASE 2: rightValStr != null
		if (right == null) {
			if ((!left.isEmpty()) && (!left.isValued()))
				throw new RuntimeException(
						"Cannot set a non-empty non-valued node to a value");
			left.forceValue(rightValStr);
			return;
		}

		// CASE 3: right != null
		left.copyInto(right);
		return;
	}

	private void doActionAppend(FeatureNode f, FeatureNode left,
		FeatureNode right, String rightValStr)
	{
		if ((left == null) || (left.isEmpty())) {
			doActionEquals(f, left, right, rightValStr);
			return;
		}
		if (left.isValued()) {
			String leftValStr = left.getValue();
			if (right != null) {
				if (!right.isValued()) throw new RuntimeException("Cannot append nonvalued node to a valued one.");
				rightValStr = right.getValue();
			}
			left.forceValue(leftValStr + "_" + rightValStr);
			return;
		}
		if (right == null || right.isValued() || right.isEmpty())
			throw new RuntimeException(
					"Cannot append an empty node or value to a non-valued node");
		// left and right are non-valued and non-empty
		// so interpret them both semanticly
		if ((left != null) && (right != null) && (!left.isEmpty())
				&& (!right.isEmpty())) {
			FeaturePath leftParentPath = new FeaturePath(getPath());
			leftParentPath.removeLastStep();
			FeatureNode leftParent = f.pathTarget(leftParentPath);
			String leftFeature = getPath().lastStep();
			SemanticView.appendToSelf(left, right, leftParent, leftFeature);
			return;
		}
		throw new RuntimeException("Unhandled ActionAppend Case");
	}

	private void doActionClear(FeatureNode f, FeatureNode left)
	{
		if (left == null)
			return;
		FeaturePath leftParentPath = new FeaturePath(getPath());
		leftParentPath.removeLastStep();
		FeatureNode leftParent = f.pathTarget(leftParentPath);
		String leftFeature = getPath().lastStep();
		leftParent.set(leftFeature, null);
	}

	public void doAction(FeatureNode f, FeatureTemplate template,
		Map<String, FeatureNode> vars)
	{
		FeatureNode left = f.pathTarget(getPath());
		FeatureNode right = null;
		String rightValStr = null;
		if (isPathPair()) {
			right = f.pathTarget(getTargetPath());
		} else {
			// interpret val as a variable name or a plain string
			String str = getTargetString();
			if (str.charAt(0) == '$') {
				right = template.val(str.substring(1, str.length()), vars);
			} else if (str.equals("%")) { // str.charAt(0)=='%') {
				doActionClear(f, left);
				return;
			} else {
				if (left == null)
					right = new FeatureNode(str);
				else
					rightValStr = str;
			}
		}
		String act = getAction();
		if (act.equals("="))
			doActionEquals(f, left, right, rightValStr);
		else if (act.equals("<="))
			doActionCopyIn(f, left, right, rightValStr);
		else if (act.equals("+="))
			doActionAppend(f, left, right, rightValStr);
		else
			throw new RuntimeException("Invalid action: " + act);
	}

	public void doAction(FeatureNode f)
	{
		doAction(f, blankTemplate, emptyVarSet);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(getPath().toString());
		sb.append(" ").append(getAction()).append(" ");
		if (isPathPair())
			sb.append(getTargetPath());
		else
			sb.append(getTargetString());
		return sb.toString();

	} // end toString

	public static Iterator<String> actionSeparatorIterator()
	{
		return actionSeparators.iterator();
	}

	public static void main(String[] args)
	{
		FeatureAction act = new FeatureAction("<a b c>=<x y z>");
		System.out.println(act.pathSeparatorRegex());
		System.out.println(act);
		if (!act.toString().equals("<a b c> = <x y z>")) {
			System.out.println("Test failed.");
			return;
		}
		act = new FeatureAction("<a b c> += <x y z>");
		System.out.println(act);
		if (!act.toString().equals("<a b c> += <x y z>")) {
			System.out.println("Test failed.");
			return;
		}
		try {
			act = new FeatureAction("<a b c>lala<x y z>");
			System.out.println("Test failed.");
			System.out.println(act);
		} catch (Exception e) {
			System.out.println("Test passed.");
		}

		/*
		 * FeatureNode f = new FeatureNode("<x y> = 3\n"+ "<x z> = 2");
		 * FeatureAction a1 = new FeatureAction("<x zz> = <x z>");
		 * FeatureAction a2 = new FeatureAction("<x double> += <x z>");
		 * FeatureAction a3 = new FeatureAction("<x y> = <x double>");
		 *
		 * System.out.println(f + "\nexecuting: " +a1); a1.doAction(f);
		 * System.out.println(f + "\nexecuting: " +a2); a2.doAction(f);
		 * System.out.println(f + "\nexecuting: " +a2); a2.doAction(f);
		 * System.out.println(f + "\nexecuting: " +a3); a3.doAction(f);
		 * System.out.println(f);
		 */
		FeatureNode f = new FeatureNode();
		f.set("test1", new FeatureNode("<x y> = 3\n" + "<x z> = 2"));
		FeatureAction a1 = new FeatureAction("<x zz> = <x z>");
		FeatureAction a2 = new FeatureAction("<x double> += <x z>");
		FeatureAction a3 = new FeatureAction("<x y> = <x double>");

		System.out.println(f + "\nexecuting: " + a1);
		a1.doAction(f);
		System.out.println(f + "\nexecuting: " + a2);
		a2.doAction(f);
		System.out.println(f + "\nexecuting: " + a2);
		a2.doAction(f);
		System.out.println(f + "\nexecuting: " + a3);
		try {
			a3.doAction(f);
		} catch (Exception e) {
			System.out.println("failed to execute (Test passed!)");
		}
		System.out.println(f);

		System.out.println("\n\nTest 2:\n");
		f .set("test2", new FeatureNode("<x zz name> = MOVE\n" + "<x zz arg0> = <x y>\n"
				+ "<x y name> = BALL\n" + "<x z name> = JIM"));

		FeatureAction a4 = new FeatureAction("<x double> += <x zz>");
		FeatureAction a5 = new FeatureAction("<x arg0> += <x zz arg0>");

		System.out.println(f + "\nexecuting: " + a2);
		a2.doAction(f);
		System.out.println(f + "\nexecuting: " + a4);
		a4.doAction(f);
		System.out.println(f + "\nexecuting: " + a5);
		a5.doAction(f);
		System.out.println(f);

	}
}
