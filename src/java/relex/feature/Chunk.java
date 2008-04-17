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

/**
 * Holder of lists of feature nodes.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Chunk extends Atom
{
	protected ArrayList<FeatureNode> chunk;
	public Chunk()
	{
		chunk = new ArrayList<FeatureNode>();
	}

	public void addNode(FeatureNode fn)
	{
		chunk.add(fn);
	}
	public void addNodes(ArrayList<FeatureNode> words)
	{
		chunk.addAll(words);
	}
	public void clear()
	{
		chunk.clear();
	}
	public int size()
	{
		return chunk.size();
	}

	/**
	 * Add an entire phrase.
	 */
	public void addPhrase(FeatureNode fn)
	{
		fn = fn.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) addNode(wd);

			// Add subphrases to the word list
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) 
			{
				addPhrase(fn);
			}
			fn = fn.get("phr-next");
		}
	}

	/**
	 * Return true if the other oject equals this one, else return false.
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof Chunk)) return false;
		Chunk oth = (Chunk) other;
		if (oth.chunk.size() != chunk.size()) return false;
		for (int i=0; i<chunk.size(); i++)
		{
			if(chunk.get(i) != oth.chunk.get(i)) return false;
		}
		return true;
	}
}
