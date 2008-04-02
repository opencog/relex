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
import relex.tree.PatternCallback;
import relex.tree.PatternMatch;
import relex.tree.PhraseTree;

/**
 * Discover phrase chunks
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class MatchChunks
{
	private ArrayList<Chunk> chunks;
	private PatCB callback;
	
	public MatchChunks()
	{
		chunks = new ArrayList<Chunk>();
		callback = new PatCB();
	}

	public void findChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
		SubPhrase sb = new SubPhrase();
		pt.foreach(sb);
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
	/* Try to pattern match each subphrase */
	private class SubPhrase implements FeatureNodeCallback
	{
		private PhraseTree pt;
		/**
		 * Called for each phrase in a parse.
		 * Add all parts of the phrase tree.
		 */
		public Boolean FNCallback(FeatureNode fn)
		{
			pt = new PhraseTree(fn);

			// A list of clauses to match, in disjunctive normal form.
			matcher("(NP (NP a) (PP a (NP r)))");
			matcher("(NP (NP (NP a) a) (PP r (NP r)))");

			matcher("(S (VP a (NP a)))");
			matcher("(SBAR a (S (NP a) (VP a (NP r))))");

			//
			matcher("(VP a (PP a (NP r)))");
			matcher("(VP a (NP r) (NP a))");
			matcher("(VP a (NP r) (PP a (NP r)))");
			matcher("(VP r (NP a) (S (VP a (VP a))))");
			matcher("(VP r (PP a (NP a)) (PP a (NP r)))");
			matcher("(VP a (PP a) (PP a (NP r)) (PP r (NP r)))");
			// matcher("");

			return false;
		}

		private void matcher(String str)
		{
			PatternMatch.match(str, pt, callback);
		}
	}

	/* -------------------------------------------------------- */

	private static void chunkWords(FeatureNode fn, Chunk chunk)
	{
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) chunk.addWord(wd);

			// Quit when a subprhase is seen.
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) return;

			fn = fn.get("phr-next");
		}
	}

	/* -------------------------------------------------------- */
	/* Use the phrase-tree approach to finding chunks */
	private class PatCB implements PatternCallback
	{
		private Chunk curr_chunk;
		public void FoundCallback(String pattern, PhraseTree pt)
		{
 System.out.println("========== hot dog! "+ pattern + " == " + pt.toString());

			curr_chunk = new Chunk();
			chunks.add(curr_chunk);
		}
		public Boolean PMCallback(String pattern, PhraseTree pt)
		{
System.out.println(">>> enfin duude >" + pattern + "< " + pt.toString());
			if (pattern.equals("a"))
			{
System.out.println(">>> enfin ptr " + PhraseTree.toString(pt.getCursor()));
				chunkWords(pt.getCursor(), curr_chunk);
			}
			return false;
		}
	}

}
