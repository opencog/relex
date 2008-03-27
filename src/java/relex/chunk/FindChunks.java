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

public class FindChunks
{
	private ArrayList<Chunk> chunks;
	
	public FindChunks()
	{
		chunks = new ArrayList<Chunk>();
	}

	public void findBasicChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
		BasicChunks pc = new BasicChunks();
		pt.foreach(pc);
	}

	public void findChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
		PhraseChunks pc = new PhraseChunks();
		pt.foreach(pc);
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
	/* Use the phrase-tree approach to finding chunks */
	private class BasicChunks implements FeatureNodeCallback
	{
		/**
		 * Called for each phrase in a parse.
		 * Add all parts of the phrase tree.
		 */
		public Boolean FNCallback(FeatureNode fn)
		{
			PhraseTree pt = new PhraseTree(fn);
			int breadth = pt.getBreadth();
			if (breadth < 2) return false;

			int degree = pt.getDegree();
			if (degree <= 2) return false;

// System.out.println("candidate phrase " +  pt.toString());
			Chunk chunk = new Chunk();
			chunkPhrase(fn, chunk);
			chunks.add(chunk);
			return false;
		}

		public void chunkPhrase(FeatureNode fn, Chunk chunk)
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
	}

	/* -------------------------------------------------------- */
	/* Use the phrase-tree approach to finding chunks */
	private class PhraseChunks implements FeatureNodeCallback
	{

		/**
		 * Called for each phrase in a parse.
		 * Pick out phrases that seem distinct.
		 */
		public Boolean FNCallback(FeatureNode fn)
		{
			PhraseTree pt = new PhraseTree(fn);

			String type = pt.getPhraseType();
			if (!type.equals("NP") && !type.equals("VP")) return false;

			int depth = pt.getDepth();
			if (depth > 3) return false;

			int breadth = pt.getBreadth();
			if (breadth < 2) return false;

			Chunk chunk = new Chunk();

			if (type.equals("NP"))
			{
				// ArrayList<FeatureNode> words = pt.getWordList();
				// chunk.addWords(words);
				chunkNounPhrase(pt.getNode(), chunk);
			}
			else if (type.equals("VP"))
			{
				chunkVerbPhrase(pt.getNode(), chunk);
			}
			else if (type.equals("S"))
			{
				chunkVerbPhrase(pt.getNode(), chunk);
			}

			// Discard single-word chunks ... !?
			if (1 < chunk.size())
			{
				chunks.add(chunk);
			}

			return false;
		}

		/**
		 * Add verb phrase words to chunk, skipping sub noun-phrases.
		 * So, for example, given the input phrase:
		 *    (VP took_office (PP on (NP Monday)))
		 * this will add the words "took office on" to the chunk.
		 *
		 * However, the current algo fails to return 
		 * "give ... no quarter" in
		 *     (VP gave (NP the Sioux chief) (NP no quarter))
		 * because it discards "no quarter" as well as "the Sioux chief"
		 */
		public void chunkVerbPhrase(FeatureNode fn, Chunk chunk)
		{
			String phrase_type = "";

			fn = fn.get("phr-head");
			while (fn != null)
			{
				FeatureNode ty = fn.get("phr-type");
				if (ty != null)
				{
					phrase_type = ty.getValue();
					// Skip subphrases that are noun phrases
					if (phrase_type.equals ("NP")) return;
				}
				FeatureNode wd = fn.get("phr-word");
				if (wd != null) chunk.addWord(wd);

				// Add subphrases to the word list, but only if
				// the current phrase isn't a prepostional phrase (PP)
				FeatureNode subf = fn.get("phr-head");
				if (subf != null && !phrase_type.equals ("PP")) 
				{
					chunkVerbPhrase(fn, chunk);
				}
				fn = fn.get("phr-next");
			}
		}

		/**
		 * Add noun phrase words to chunk, skipping subphrases
		 * of prepositional phrases.
		 * So, for example, given the input phrase:
		 *     (NP (NP a wee bit) (PP of (NP cheese)))
		 *    
		 * this will add the words "a wee bit of" to the chunk.
		 */
		public void chunkNounPhrase(FeatureNode fn, Chunk chunk)
		{
			String phrase_type = "";

			fn = fn.get("phr-head");
			while (fn != null)
			{
				FeatureNode ty = fn.get("phr-type");
				if (ty != null)
				{
					phrase_type = ty.getValue();
				}
				FeatureNode wd = fn.get("phr-word");
				if (wd != null) chunk.addWord(wd);

				// Add subphrases to the word list, but only if
				// the current phrase isn't a prepostional phrase (PP)
				FeatureNode subf = fn.get("phr-head");
				if (subf != null && !phrase_type.equals ("PP")) 
				{
					chunkNounPhrase(fn, chunk);
				}
				fn = fn.get("phr-next");
			}
		}

	}
}
