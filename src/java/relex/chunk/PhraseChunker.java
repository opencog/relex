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

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.tree.PhraseTree;

/**
 * Discover phrase chunks.
 * XXX This is so rudimentary that it might not be useful. XXX
 * XXX This might go away ...  XXX
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class PhraseChunker extends LexicalChunker
{
	static final int debug = 0;
	
	public void findChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
		BasicChunks pc = new BasicChunks(pt);
		pt.foreach(pc);
	}

	/* -------------------------------------------------------- */
	/* Use the phrase-tree approach to finding chunks */
	private class BasicChunks implements FeatureNodeCallback
	{
		private FeatureNode root;
		public BasicChunks (PhraseTree pt)
		{
			root = pt.getNode();
		}

		/**
		 * Called for each phrase in a parse.
		 * Add all parts of the phrase tree.
		 */
		public Boolean FNCallback(FeatureNode fn)
		{
			if (root == fn) return false;  // don't report the whole sentence 

			PhraseTree pt = new PhraseTree(fn);
			int breadth = pt.getBreadth();
			if (breadth < 2) return false; // don't report single words.

			int degree = pt.getDegree();
			if (degree <= 2) return false;  // don't report repeats!

			Chunk chunk = new Chunk();
			chunk.addPhrase(fn);
			add(chunk);
			return false;
		}
	}
}
