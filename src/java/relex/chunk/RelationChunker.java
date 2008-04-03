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
import relex.feature.FeatureForeach;
import relex.feature.FeatureNode;
import relex.feature.RelationCallback;

/**
 * Discover phrase chunks.
 * XXX This is so rudimentary that it might not be useful. XXX
 * XXX This might go away ...  XXX
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class FindChunks
{
	static final int debug = 0;
	private ArrayList<Chunk> chunks;
	
	public FindChunks()
	{
		chunks = new ArrayList<Chunk>();
	}

	public void findObjectChunks(ParsedSentence parse)
	{
		ObjChunks obj = new ObjChunks();
		FeatureNode sent = parse.getLeft();
		FeatureForeach.foreach(sent, obj);
	}

	public ArrayList<Chunk> getChunks()
	{
		return chunks;
	}

	public void clear()
	{
		chunks.clear();
	}

	/* -------------------------------------------------------- */

	private void chunkPhrase(FeatureNode fn, Chunk chunk)
	{
		fn = fn.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) chunk.addWord(wd);

			// Add subphrases to the word list
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) 
			{
				chunkPhrase(fn, chunk);
			}
			fn = fn.get("phr-next");
		}
	}

	/* -------------------------------------------------------- */
	/**
	 * John Dillinger was a man who broke the law.
	 * _obj(break, law)
	 *  will generate th phrase "broke the law"
	 *
	 */
	private class ObjChunks implements RelationCallback
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
			if (relation.equals("_subj")) return false;
			if (relation.equals("_prepSubj")) return false;

			FeatureNode fm = from.get("nameSource");

			if (0 < debug)
				System.out.println(relation + "(" + 
				                   from.get("name").getValue() + "," + 
				                   to.get("name").getValue() + ")");

			Chunk chunk = new Chunk();
			chunkPhrase(fm, chunk);
			chunks.add(chunk);

			return false;
		}
	}
}
