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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import relex.ParsedSentence;
import relex.concurrent.RelexContext;
import relex.feature.FeatureAction;
import relex.feature.FeatureNode;

public class TemplateActionAlg extends TemplateMatchingAlg
{
	private final static boolean dbg = false;
	/**
	 * An ArrayList of FeatureActions. When the template is matched to a
	 * FeatureNode, each action is applied to the FeatureNode
	 */
	private ArrayList<FeatureAction> featureActions;

	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		Iterator<FeatureAction> i = featureActions.iterator();
		while (i.hasNext())
		{
			FeatureAction act = i.next();
			act.doAction(node, getTemplate(), vars);
		}

		// print out the rule that got applied.
		if (dbg)
		{
			System.out.println(toString());
			if (getSignature().equals("DUMP"))
				System.out.println(node.toString());
		}
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		if (featureActions.size() > 0)
		{
			Iterator<FeatureAction> i = featureActions.iterator();
			sb.append(i.next().toString());
			while (i.hasNext())
				sb.append(i.next().toString());
		}
		return sb.toString();
	}

	public int init(String str)
	{
		int actionStart = super.init(str);
		// Get actions
		featureActions = new ArrayList<FeatureAction>();
		String[] actLines = str.substring(actionStart).split("\n");
		for (int i = 0; i < actLines.length; i++)
		{
			String line = actLines[i];
			line = line.trim();

			// Allow blank lines in rules ...
			if (0 < line.length())
			{
				try {
					featureActions.add(new FeatureAction(line));
				} catch (Exception e) {
					throw new RuntimeException("ALGFILE format error: " + actLines[i] + "\n");
				}
			}
		}
		return str.length();
	}

	public static void main(String[] args)
   {
		String tString = "TestAlg\n" + "<a b> = $1\n" + "<a b> = <a c>\n" + "=\n" + "<a d> += $1\n";
		TemplateActionAlg test = new TemplateActionAlg();
		test.init(tString);
		System.out.println(tString);
		System.out.println(test);
		ParsedSentence ps = new ParsedSentence("test");
		FeatureNode cn = new FeatureNode();
		cn.add("a").set("b", new FeatureNode("X"));
		cn.get("a").set("c", cn.get("a").get("b"));
		ps.addWord(cn);
		System.out.println("Applying to:\n" + cn);
		test.apply(ps, null);
		System.out.println("Result:\n" + cn);
		test.apply(ps, null);
		System.out.println("Result:\n" + cn);

	}
}

/* ================================== END OF FILE ========================= */
