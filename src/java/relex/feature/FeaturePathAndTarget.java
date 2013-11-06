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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * FeaturePath can either store two paths: <a b c> = <x y z>
 *
 * Or it can be a simple path with a target string: <a b c> = hello
 */
public class FeaturePathAndTarget
{
	private FeaturePath path;

	private FeaturePath targetPath;

	private String targetString;

	private String separator;

	private static HashSet<String> pathSeparators;
	static {
		pathSeparators = new HashSet<String>();
		pathSeparators.add("=");
		pathSeparators.add("!=");
	}

	public FeaturePathAndTarget(String str)
	{
		// split the string around the = character
		String[] items = str.split("[ ]*" + pathSeparatorRegex() + "[ ]*");
//		System.err.println("\n"+str);
//		for (int i=0; i < items.length; i++){
//			System.err.println(items[i]);
//		}
		if (items.length != 2)
			throw new RuntimeException(
				"Error: relex: invalid path and target string, " +
				"need exactly one pathSeparator, but found "
							+ items.length + "\n\t" + str
							+ "\n\tRegular expression was:"
							+ pathSeparatorRegex());
		setPath(items[0]);
		setTarget(items[1]);

		Iterator<String> i = getPathSeparators().iterator();
		while (i.hasNext()) {
			String sep = i.next();
			/*
			 * Regular expression: any sequence of characters, > 0 or more
			 * spaces sep string 0 or more spaces < or $ or % or \ (for a regex)
			 * or a word character any sequence of characters
			 */
			String re = ".*>[ ]*\\Q" + sep + "\\E[ ]*[<$%\\.\\,\\?\\-\\:\\;\\'\\w\\\\].*";
			if (Pattern.matches(re, str)) {
				if (!getPathSeparators().contains(sep)) {
					throw new RuntimeException("Error: relex: " +
						sep + " is not a valid path separator.");
				}
				separator = sep;
				return;
			}
		}
		throw new RuntimeException("Error: relex: Parsed Path And "
			+ "Target but could not determine separator");
	}

	public FeaturePathAndTarget(FeaturePathAndTarget other) {
		this(other.toString());
	}

	// Subclasses should override
	public Set<String> getPathSeparators() {
		return pathSeparators;
	}


	public String pathSeparatorRegex() {
		return "(!=|=)";
//		Iterator<String> i = getPathSeparators().iterator();
//		StringBuffer sb = new StringBuffer("(");
//		while (i.hasNext()) {
//			//sb.append("\\Q").append((String) i.next()).append("\\E");
//			sb.append((String) i.next());
//			if (i.hasNext()) sb.append("|");
//		}
//		sb.append(")");
//		return sb.toString();
	}

	public String getSeparator() {
		return separator;
	}

	private void setPath(String str) {
		path = new FeaturePath(str);
	}

	public FeaturePath getPath() {
		return path;
	}

	public void setTarget(String str) {
		targetString = null;
		targetPath = null;
		try {
			targetPath = new FeaturePath(str);
		} catch (Exception e) {
			// Trim whitespace!! Else crapola whitespace in the algs file
			// will damage results e.g. blah != %  with trailing whitespace.
			targetString = str.trim();
		}
	}

	public String lastStep() {
		return path.lastStep();
	}

	public void removeLastStep() {
		path.removeLastStep();
	}

	public boolean isPathPair() {
		return targetPath != null;
	}

	public FeaturePath getTargetPath() {
		return targetPath;
	}

	public String getTargetString() {
		return targetString;
	}

	public Iterator<String> iterator() {
		return path.iterator();
	}

	public String toString()
   {
		StringBuffer sb = new StringBuffer(path.toString());
		sb.append(" ").append(getSeparator()).append(" ");
		if (isPathPair())
			sb.append(getTargetPath().toString());
		else
			sb.append(getTargetString());
		return sb.toString();
	}

	public static void main(String[] args)
   {
		FeaturePathAndTarget fp = new FeaturePathAndTarget("<a b c d>=<ww xx yy zz>");
		FeaturePathAndTarget fp1 = new FeaturePathAndTarget("<a b c d>!=$1");
		System.out.println(fp);
		System.out.println(fp1);
		FeaturePathAndTarget fp2 = new FeaturePathAndTarget(fp);
		fp2.removeLastStep();
		System.out.println(fp2);
	}

}
