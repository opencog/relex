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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import relex.feature.FeatureNode;
import relex.feature.FeatureNameFilter;


/**
 * The PrologList class will take a featureNode graph,
 * and print it as a Prolog expression.
 *
 */
public class PrologList
{
	/**
	 * Export feature structures as Prolog lists.
	 */
	public String toPrologList(FeatureNode root)
	{
		return toPrologList(root, FeatureNode.defaultFilter(), false);
	}

	public String toPrologList(FeatureNode root, FeatureNameFilter filter, boolean indent)
	{
		if (filter == null)
			filter = FeatureNode.defaultFilter();
		HashMap<FeatureNode,Integer> indices = new HashMap<FeatureNode,Integer>();
		HashSet<FeatureNode> visited = new HashSet<FeatureNode>();
		indices.put(root, 0);
		visited.add(root);
		StringBuffer result = new StringBuffer();
		_toPrologList(root, "F_", indices, visited, filter, result, indent ? 0 : -1);
		return result.toString();
	}

	private static void _toPrologList(FeatureNode fn,
	                                  String varPrefix,
	                                  HashMap<FeatureNode,Integer> indices,
	                                  HashSet<FeatureNode> visited,
	                                  FeatureNameFilter filter,
	                                  StringBuffer result,
	                                  int indentLevel /* -1 indicates no indentation*/)
	{
		if (fn.isValued())
		{
			result.append("'");
				result.append(fn.getValue().toLowerCase());
				result.append("'");
				return;
		}
		result.append(varPrefix);
		result.append(indices.get(fn));
		result.append('@');
		result.append("[");
		Iterator<String> i = fn.features(filter);
		if (!i.hasNext())
		{
			result.append("]");
			return;
		}
		while (i.hasNext())
		{
			String name = i.next();
			FeatureNode value = fn.get(name);
			if (indentLevel >= 0)
			{
				result.append('\n');
				for (int j = 0; j < (indentLevel+1)*4; j++) result.append(' ');
			}
			result.append(_toPrologAtom(name) + ":");
			if (visited.contains(value))
			{
				result.append(varPrefix);
				result.append(indices.get(value));
			}
			else
			{
				visited.add(value);
				indices.put(value, indices.size());
				_toPrologList(value, varPrefix, indices, visited, filter,
				              result, indentLevel > -1 ? indentLevel+1:-1);
			}
			result.append(",");
		}
		if (result.charAt(result.length()-1)==',')
			result.deleteCharAt(result.length()-1);
		result.append("|");
		result.append(varPrefix);
		result.append('T'); // 'T' for for 'Tail'
		result.append(indices.get(fn));
		result.append("]");
	}

	private static String _toPrologAtom(String s)
	{
		if (s == null || s.length() == 0) return s;
		StringBuffer result = new StringBuffer();
		if (!Character.isLowerCase(s.charAt(0)))
			result.append("p_P");
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			result.append(Character.isJavaIdentifierPart(c) ? c : '_');
		}
		return result.toString();
	}
}

// =========================== End of File ===============================
