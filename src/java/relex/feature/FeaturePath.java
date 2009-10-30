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

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * FeaturePath can either store two paths: <a b c> = <x y z>
 *
 * Or it can be a simple path with a target string: <a b c> = hello
 */
public class FeaturePath
{
	/**
	 * The sequence of feature names in the path.
	 */
	private ArrayList<String> path;

	protected FeaturePath()
	{
		path = new ArrayList<String>();
	}

	public FeaturePath(String str)
	{
		this();
		if (str.charAt(0) != '<')
			throw new RuntimeException("invalid feature path init string: " + str);

		if (str.indexOf('>') < 0)
			throw new RuntimeException("invalid feature path init string: " + str);

		StringTokenizer st = new StringTokenizer(str.substring(1, str.indexOf('>')));
		while (st.hasMoreTokens()) path.add(st.nextToken());
	}

	public FeaturePath(FeaturePath other)
	{
		this(other.toString());
	}

	public int size()
	{
		return path.size();
	}

	public String lastStep()
	{
		return path.get(path.size() - 1).toString();
	}

	public void removeLastStep()
	{
		path.remove(path.size() - 1);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("<");
		Iterator<String> i = iterator();
		if (i.hasNext()) sb.append(i.next());
		while (i.hasNext()) sb.append(" ").append(i.next());
		sb.append(">");
		return sb.toString();
	}

	public Iterator<String> iterator()
	{
		return path.iterator();
	}

	// unit-test function
	public static void main(String[] args)
	{
		FeaturePath fp = new FeaturePath("<a b c d>");
		System.out.println(fp);
		FeaturePath fp2 = new FeaturePath(fp);
		fp2.removeLastStep();
		System.out.println(fp2);
	}
}

// ============================== End of File ======================
