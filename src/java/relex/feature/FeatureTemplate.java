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

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class FeatureTemplate
{
	private static final String NULL_STRING = "%";

	private ArrayList<FeaturePathAndTarget> pathsAndTargs;

//	private HashMap<String,FeatureNode> vars;

	/**
	 * if test begins with "\" it is interpretted as a regular expression
	 * (unless it begins with "\\" in which case the first character is removed)
	 *
	 * Also "\." is replaced with "[a-z\*]"
	 */
	private boolean matchesVal(String test, String val)
	{
		if (test.charAt(0) == '\\') {
			test = test.substring(1);
			if (test.charAt(0) != '\\') {
				test = test.replaceAll("\\\\.", "[a-z\\*]");
				// System.err.println(test + " --- " + val + " --- " +
				// Pattern.matches(test,val));
				return Pattern.matches(test, val); // interprets test as a regex
			}
		}
		return test.equals(val);
	}

	private boolean matchesString(FeatureNode target, String pathTarget, Map<String,FeatureNode> vars)
	{
		// if path target is a variable name, set that variable name to the
		// FeatureNode target
		if (pathTarget.charAt(0) == '$') {
			vars.put(pathTarget.substring(1), target);
			return true;
		}
		// otherwise, interpret path target as a disjunction of strings
		// separated by | characters.
		if (!target.isValued())
			return false;
		String valString = target.getValue().toString();
		String[] toks = pathTarget.split("[ ]*\\|[ ]*");
		for (int i = 0; i < toks.length; i++)
			if (matchesVal(toks[i], valString))
				return true;
		return false;
	}

	public FeatureNode val(String varName, Map<String,FeatureNode> vars)
	{
		return vars.get(varName);
	}

	// Iterate through the pathAndTargs, making sure each matches f
	public Map<String,FeatureNode> match(FeatureNode f)
	{
		boolean matched = true;
		Iterator<FeaturePathAndTarget> i = pathsAndTargs.iterator();
		Map<String,FeatureNode> vars = new HashMap<String,FeatureNode>();
		// vars.clear();
		while (matched && i.hasNext()) {
			FeaturePathAndTarget pathAndTarget = i.next();
			if (pathAndTarget.getSeparator().equals("=")) {
				matched = match(f, pathAndTarget, vars);
			}
			if (pathAndTarget.getSeparator().equals("!=")) {
				matched = !match(f, pathAndTarget, vars);
			}
		}
/*		if (!matched)
			vars.clear();
		return matched; */
		return matched ? vars : null;
	}

	private boolean match(FeatureNode f, FeaturePathAndTarget pathAndTarget, Map<String,FeatureNode> vars)
	{
		// get the target of the path in F
		FeaturePath path = pathAndTarget.getPath();
		FeatureNode fTarget = f.pathTarget(path);
		if (fTarget == null) {
			if (!pathAndTarget.isPathPair()
					&& pathAndTarget.getTargetString().equals(NULL_STRING))
				return true;
			return false;
		}
		if (pathAndTarget.isPathPair()) {
			// If path is a pair, make sure that F has both paths
			// and that their destination are equal.
			FeaturePath path2 = pathAndTarget.getTargetPath();
			FeatureNode fTarget2 = f.pathTarget(path2);
			if (fTarget.isValued() && fTarget2.isValued())
			{
				return (fTarget.getValue() == fTarget2.getValue());
			}
			return fTarget.equiv(fTarget2);
		} else {
			// Otherwise, test if F's target matches the path's.
			String pathTarg = pathAndTarget.getTargetString();
			if (pathTarg.equals(NULL_STRING))
				// only empty string would match null
				return (fTarget.isEmpty() || (fTarget.isValued() && fTarget
						.getValue().length() == 0));
			if (!matchesString(fTarget, pathTarg, vars)) {
				return false;
			}
		}

		return true;
	}

	public String toString(Map<String,FeatureNode> vars)
	{
		StringBuffer sb = new StringBuffer();
		Iterator<FeaturePathAndTarget> i = pathsAndTargs.iterator();
		if (i.hasNext())
			sb.append(i.next().toString());
		while (i.hasNext())
			sb.append("\n").append(i.next().toString());
		Iterator<String> k = vars.keySet().iterator();
		while (k.hasNext()) {
			String key = k.next();
			sb.append("\n$").append(key).append(":");
			sb.append(vars.get(key).toString());
		}
		return sb.toString();
	}

	public FeatureTemplate()
	{
		pathsAndTargs = new ArrayList<FeaturePathAndTarget>();
		//vars = new HashMap<String, FeatureNode>();
	}

	public FeatureTemplate(ArrayList<FeaturePathAndTarget> pathAndTargVec)
	{
		this();
		pathsAndTargs.addAll(pathAndTargVec);
	}

	public FeatureTemplate(String str)
	{
		this();
		String[] lines = str.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!line.substring(0, 2).equals("//"))
				pathsAndTargs.add(new FeaturePathAndTarget(lines[i]));
		}
	}

	public static void main(String[] args)
	{
		/*
		 * FeaturePath abc = new FeaturePath("<a b c>"); FeaturePath xyz = new
		 * FeaturePath("<x y z>"); FeaturePath var = new FeaturePath("<x y
		 * z>"); FeaturePath empty = new FeaturePath("<>");
		 * abc.setTarget(empty); xyz.setTarget("M N | O");
		 * var.setTarget("$VAR"); ArrayList v = new ArrayList(); v.add(abc);
		 * v.add(xyz); v.add(empty); v.add(var); FeatureTemplate template = new
		 * FeatureTemplate(v);
		 */

		String templateString = "<a b c> = <>\n";
		templateString += "<x y z> =  M N | O\n";
		templateString += "<x y z> = $VAR\n";
		templateString += "<b1> = %\n";
		templateString += "<b2> = %\n";
		templateString += "<b3> = %\n";
		FeatureTemplate template = new FeatureTemplate(templateString);

		FeatureNode val = new FeatureNode();

		FeatureNode f = new FeatureNode();
		f.add("a").add("b").set("c", f);
		f.add("x").add("y").set("z", val);
		f.add("b1");
		f.set("b2", new FeatureNode(""));

		// START TEST
		ArrayList<String> vals = new ArrayList<String>();
		vals.add("M N");
		vals.add("O");
		vals.add("Z");
		Iterator<String> i = vals.iterator();
		while (i.hasNext()) {
			val.forceValue(i.next());
			System.out.println("Testing with " + val);
			Map<String,FeatureNode> vars = template.match(f);
			//if (template.match(f)) {
			if (vars != null) {
				System.out.println("MATCHED");
				System.out.println("VAR = " + template.val("VAR", vars));
			} else {
				System.out.println("FAILED MATCH"); // Want this for the last value!
			}
		}
	}
}

// ======================== End of File ===========================
