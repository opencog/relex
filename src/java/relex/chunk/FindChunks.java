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

package relex.chunk;

import java.util.ArrayList;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.tree.PhraseTree;

/**
 * Discover phrase chunks
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class FindChunks implements FeatureNodeCallback
{
	private ArrayList<Chunk> chunks;
	
	public FindChunks()
	{
		chunks = new ArrayList<Chunk>();
	}

	public void findChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
		pt.foreach(this);

	}

	public Boolean FNCallback(FeatureNode fn)
	{
		PhraseTree pt = new PhraseTree(fn);

		String type = pt.getPhraseType();
		if (!type.equals("NP") && !type.equals("VP")) return false;

		int depth = pt.getDepth();
		if (depth > 3) return false;

		int breadth = pt.getBreadth();
		if (breadth < 2) return false;

System.out.println("candidate phrase " +  pt.toString());

		return false;
	}
}
