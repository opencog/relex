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
import relex.feature.RelationCallback;
import relex.tree.PhraseTree;

/**
 * Identify relation-based lexical chunks.
 * XXX This is so rudimentary that it might not be useful. XXX
 * XXX This might go away ...  XXX
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class RelationChunker extends LexicalChunker
{
	static final int debug = 0;
	static final int minwords = 3;
	static final int maxwords = 5;
	
	public void findChunks(ParsedSentence parse)
	{
		RelChunks obj = new RelChunks();
		parse.foreach(obj);
	}

	/* -------------------------------------------------------- */
	/**
	 * John Dillinger was a man who broke the law.
	 * _obj(break, law)
	 *  will generate th phrase "broke the law"
	 *
	 */
	private class RelChunks implements RelationCallback
	{
		public Boolean UnaryRelationCB(FeatureNode from, String rel)
		{
			return false;
		}
		public Boolean BinaryHeadCB(FeatureNode from)
		{
			return false;
		}
		public Boolean BinaryRelationCB(String relation, FeatureNode from, FeatureNode to)
		{
			// if (relation.equals("_subj")) return false;
			// if (relation.equals("_psubj")) return false;

			FeatureNode fm = from.get("nameSource");

			if (0 < debug)
				System.out.println(relation + "(" + 
				                   from.get("name").getValue() + "," + 
				                   to.get("name").getValue() + ")");

			PhraseTree pt = new PhraseTree(fm);
			int breadth = pt.getBreadth();
			if (breadth < minwords) return false; // don't report short chunks;
			if (breadth > maxwords) return false; // don't report long chunks;

			int degree = pt.getDegree();
			if (degree <= 2) return false;  // don't report repeats!

			LexChunk chunk = new LexChunk();
			chunk.addPhrase(fm);
			add(chunk);

			return false;
		}
	}
}
