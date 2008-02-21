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
package relex;

import relex.feature.FeatureForeach;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.RelationCallback;

/**
 * The PhraseLeader class adds a pointer to the "leader word"
 * of the Penn tree-bank style phrase structure in the
 * relex ParsedSentence/FeatureNode graph.
 *
 * An example Penn tree phrase structure is
 * (S (NP I) (VP am (NP a big robot)) .)
 *
 * In this example, "robot" is the leader of the phrase
 * "(NP a big robot)"
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class PhraseLeader
{
	/* -------------------------------------------------------------------- */
	/**
	 * Walk the graph, looking for phrase heads.
	 * Thus, for example, given a phrase "(NP the red chair)"
	 * identify "chair" as the head of the phrase.
	 *
	 * This is done by reviewing all of the link relations
	 * in a graph. It is assumed that all words that occur
	 * in a relation are necessarily the heads of some
	 * phrase.
	 */
	private static void _phraseHeads(FeatureNode fn)
	{
		FeatureNode fn_name_source = fn.get("nameSource");
		if (fn_name_source == null) return;
		FeatureNode phr = fn_name_source.get("phr-head");
		if (phr == null) return;
		phr.set("phr-leader", fn);
	}

	private static class phraseHeads implements RelationCallback
	{
		public Boolean UnaryRelationCB(FeatureNode from, String rel)
		{
			return false;
		}
		public Boolean BinaryHeadCB(FeatureNode from)
		{
			_phraseHeads(from);
			return false;
		}
		public Boolean BinaryRelationCB(String relation, FeatureNode from, FeatureNode to)
		{
			_phraseHeads(to);
			return false;
		}
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Walk the constituent tree, looking for phrases that 
	 * have only one leaf under them. Assume that the leaf
	 * must be the phrase leader. So e.g. "(PP for (NP fun))"
	 * "fun" will be declared as the leader of "for fun".
	 */
	private static class leafHeads implements FeatureNodeCallback
	{
		public Boolean FNCallback(FeatureNode fn)
		{
			FeatureNode leaf = PhraseTree.getOneLeafOnly(fn);
			if (leaf != null)
			{
				leaf = leaf.get("phr-head");
				leaf = leaf.get("phr-next");
				leaf = leaf.get("phr-word");
				leaf = leaf.get("ref");

				fn = fn.get("phr-head");
				fn.set("phr-leader", leaf);
			}
			return false;
		}
	}

	/* -------------------------------------------------------------------- */
	public static void markup(FeatureNode sent)
	{
		FeatureForeach.foreach(sent, new phraseHeads());
		PhraseTree.foreach(sent, new leafHeads());
	}
};

/* =========================== END OF FILE ================== */
