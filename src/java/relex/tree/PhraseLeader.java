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
package relex.tree;

import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.RelationCallback;
import relex.feature.RelationForeach;

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
	private final static int DEBUG = 0;

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
	 *
	 * Now, "chair" can be identified as the leader of "(NP the red chair)"
	 * because there is a binary relation: "amod(chair, red)" that 
	 * identifies chair.  However, "(NP the chair)" has no such binary 
	 * relation, and so instead, we look for either "definite-FLAG(chair, T)"
	 * or for "noun_number(chair, singular)" to identify "chair".
	 */
	private static void _phraseHeads(FeatureNode fn)
	{
		FeatureNode fn_name_source = fn.get("nameSource");
		if (fn_name_source == null) return;
		FeatureNode phr = fn_name_source.get("phr-head");
		if (phr == null) return;

		// Definite nouns that are part of entity names will have 
		// their "str" field blanked. These can never be heads.
		if (fn_name_source.get("str") == null) return;

		// If already set, return. Do this because we should trust the
		// binary relations more than the unary ones; and the binary
		// ones are checked first. (The unary relations can accidentally
		// over-ride a correct binary ID.)
		if (null != phr.get("phr-leader")) return;

		if (0 < DEBUG) System.err.println("Debug: set leader=" + fn.get("name") +
		                " for phrase=" + PhraseTree.toString(fn_name_source));
		phr.set("phr-leader", fn);
	}

	private static class phraseHeads implements RelationCallback
	{
		public Boolean UnaryRelationCB(FeatureNode from, String rel)
		{
			if (rel.equals("definite-FLAG") || rel.equals("noun_number"))
			{
				_phraseHeads(from);
			}
			return false;
		}
		public Boolean BinaryHeadCB(FeatureNode from)
		{
			_phraseHeads(from);
			return false;
		}
		public Boolean BinaryRelationCB(String relation, FeatureNode from, FeatureNode to)
		{
			// Headword is always the first word.
			_phraseHeads(from);
			return false;
		}
	}

	/* -------------------------------------------------------------------- */
	/**
	 * Walk the constituent tree, looking for phrases that 
	 * have only one leaf under them. Assume that the leaf
	 * must be the phrase leader. So e.g. "(PP for (NP fun))"
	 * "fun" will be declared as the leader of "for fun".
	 *
	 * Note that is only makes sense for PP and NP, but not for 
	 * S or VP. So for example: (S (VP drop (NP it))) the head
	 * for S and VP is "drop", and not "it". 
	 */
	private static class leafHeads implements FeatureNodeCallback
	{
		public Boolean FNCallback(FeatureNode phr)
		{
			FeatureNode leaf = PhraseTree.getOneLeafOnly(phr);
			if (leaf != null)
			{
				// Reject phrases that aren't NP or PP
				FeatureNode fn = phr.get("phr-head");
				FeatureNode ft = fn.get("phr-type");
				if (null == ft) return false;
				String pt = ft.getValue();
				if (0 < DEBUG) System.err.println("Debug: leaf phrase=" + PhraseTree.toString(phr));
				if (!(pt.equals("NP") || pt.equals("PP"))) return false;

				leaf = leaf.get("phr-head");
				leaf = leaf.get("phr-next");
				leaf = leaf.get("phr-word");
				leaf = leaf.get("ref");

				fn.set("phr-leader", leaf);
			}
			return false;
		}
	}

	/* -------------------------------------------------------------------- */
	public static void markup(FeatureNode sent)
	{
		PhraseTree.foreach(sent, new leafHeads());
		RelationForeach.foreach(sent, new phraseHeads());
	}
};

/* =========================== END OF FILE ================== */
