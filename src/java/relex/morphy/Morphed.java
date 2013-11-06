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

package relex.morphy;

import java.util.HashMap;

import relex.feature.FeatureNode;

public class Morphed
{
	protected String original;
	private HashMap<String, FeatureNode> features;

	public Morphed(String original)
	{
		this.original = original;
		this.features = new HashMap<String, FeatureNode>();
	}

	public void putRootNegative(String type, String root)
	{
		putRoot(type, root);
		FeatureNode f = features.get(type);
		f.set(Morphy.NEG_F, new FeatureNode("T"));
	}

	public void putRoot(String type, String root)
	{
		FeatureNode f = features.get(type);
		if (f == null)
			f = new FeatureNode();
		f.set(Morphy.ROOT_F, new FeatureNode(root));
		f.set(Morphy.TYPE_F, new FeatureNode(type));
		features.put(type, f);
	}

	public String getOriginal()
	{
		return original;
	}

	public HashMap<String, FeatureNode> getFeatures()
	{
		return features;
	}

	public boolean hasRoot() {
		return features.size() > 0;
	}

	public FeatureNode getNoun()
	{
		return features.get(Morphy.NOUN_F);
	}

	public FeatureNode getVerb()
	{
		return features.get(Morphy.VERB_F);
	}

	public FeatureNode getAdj()
	{
		return features.get(Morphy.ADJ_F);
	}

	public FeatureNode getAdv()
	{
		return features.get(Morphy.ADV_F);
	}

	private String getRoot(FeatureNode f) {
		return f == null ? null : f.get(Morphy.ROOT_F).getValue();
	}

	public String getNounString() {
		return getRoot(getNoun());
	}

	public String getVerbString() {
		return getRoot(getVerb());
	}

	public String getAdjString() {
		return getRoot(getAdj());
	}

	public String getAdvString() {
		return getRoot(getAdv());
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(getOriginal());
		if (getNounString() != null)
			sb.append(" N:" + getNounString());
		if (getVerbString() != null)
			sb.append(" V:" + getVerbString());
		if (getAdjString() != null)
			sb.append(" Adj:" + getAdjString());
		if (getAdvString() != null)
			sb.append(" Adv:" + getAdvString());
		return sb.toString();
	}
}
