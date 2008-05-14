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

package relex.anaphora;

import java.util.ArrayList;
import java.util.Iterator;
import relex.RelexInfo;
import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.tree.PhraseTree;

/**
 * Implementation of a modified Hobbs algorithm for pronoun resolution.
 * A behaviour closer to the original algorithm can be obtained by
 * setting the modified flag to false.
 *
 * Prototype; under development.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Hobbs
{
	public static final int DEBUG = 0;
	
	// Buffer of sentences previously seen.
	private ArrayList<RelexInfo> sentences;

	// Where anaphora/antecedent collections are kept.
	Antecedents antecedents;

	// The max number of sentences to keep in the history buffer.
	static int max_sentences = 20;

	// The max number of antecedent proposals to make.
	static int max_proposals = 10;

	// Fidelity to the original Hobbs algo:
	// Set to false to stick closer to the original paper.
	static Boolean modified_step_two = true;

	// Pointer to anaphore of the currently-parsed sentence.
	private PhraseTree anaphore;
	private int num_proposals;
	private static Integer ana_id = 1;

	protected Hobbs() {}

	public Hobbs(Antecedents ant)
	{
		sentences = new ArrayList<RelexInfo>();
		antecedents = ant;
		anaphore = null;
		num_proposals = 0;
	}

	public void addParse(RelexInfo ri)
	{
		// Add at the head of the list
		sentences.add(0, ri);
		int sz = sentences.size();
		if (sz > max_sentences) sentences.remove(max_sentences);
	}

	public void resolve(RelexInfo ri)
	{
		ArrayList<ParsedSentence> plist = ri.parsedSentences;
		if (0 >= plist.size()) return;

		// XXX at the moment, ignore all but the first parse.
		ParsedSentence ps = plist.get(0);

		ArrayList<PhraseTree> prlist = FindPronouns.findPronouns(ps);
		if (0 >= prlist.size()) return;

	 	for (PhraseTree prn : prlist)
		{
			relabel(prn);
			scan(prn);
		}
	}

	/* ---------------------------------------------------------- */
	/**
	 * Give the string names of the prnouns unique ids.
	 *
	 * This is a strange hack used for relationship printing.
	 * Each pronoun in each sentence potentially refers to
	 * a different thing, and so, we want too make sure
	 * each pronoun gets a unique id. Now, I could put
	 * these unique id's into thier own node, but the
	 * relation printing code is ignorant of that. So
	 * we'll just brute-force change the string name.
	 *
	 * This idea is subject to redesign/rethinking.
	 */
	private void relabel(PhraseTree ana)
	{
		FeatureNode name = ana.get("str");
		if (name == null)
		{
			System.err.println(
			     "Error: can't find str featurenode during anaphora resolution");
			return;
		}
		String str = name.getValue();
		str += "_" + ana_id;
		name.setValue(str);

		ana_id ++;
	}
	private String getlabel(PhraseTree ana)
	{
		FeatureNode name = ana.get("str");
		return name.getValue();
	}

	public Boolean isReflexive(PhraseTree pt)
	{
		FeatureNode prn = pt.getPhraseLeader();
		if (prn == null) return false;
		prn = prn.get("ref");
		prn = prn.get("REFLEXIVE-FLAG");
		if (prn == null) return false;
		return true;
	}

	/* ---------------------------------------------------------- */
	// In the comments below, the numbered "Steps" refer to the
	// steps of the Hobbs algorithm
	// (reproduced on page 2 of the Handy, Hicks-Wright, Schkufza
	// paper "CS224n Final Project: Anaphora Resolution"
	// Also in Appendix of Walker, "Evaluating Discourse
	// Processing Algorithms".)
	//
	private void scan(PhraseTree pt)
	{
		// Step 1: Begin at the NP node dominating the pronoun
		//         (i.e. begin at the pronoun).
		// FeatureNode ph = prfn.pathTarget("<nameSource phr-head phr-type>");

		String type = pt.getPhraseType();
		if (!type.equals("NP"))
		{
			// OK, normally, we should always expect an NP here, but there
			// are exceptional situations. e.g. "Then Alice saw it, what she
			// was always looking for." The link parser has already resolved
			// "it" to be "what", and so "it" is no longer marked off as an
			// "NP". So don't print any warnings, but also, don't do anything.
			return;
		}
		anaphore = pt; // The "dominating NP" of the pronoun
		num_proposals = 0;

		// Step 2: Walk up the tree to the first node S or NP
		pt = getSorPPorNPabove(pt);
		if (pt == null)
			throw new RuntimeException("Didn't find constituent head");

		// Step 3: pt is now "point X". Traverse, from left to
		// right, looking for NP. If an NP is found, propose it
		// as an antecedent.
		StepThree(pt, anaphore);

		// Step 4a: Is X the highest node in the sentence?
		// If so, goto Step 4b, else, goto Step 5. Then,
		// repeat steps 5,6,7,8 until X is the highest node.
		pt = getSorPPorNPabove(pt);
		while (pt != null)
		{
			// Step 5. There's more to this sentence.
			type = pt.getPhraseType();
			if (type.equals("NP"))
			{
				// Step 6.
				if (DEBUG > 0)
					System.out.println("now do step 6 XXX -- step 6 not implemented!!");
			}
			// Step 7. Same as step 3, but for the new X.
			StepSeven(pt, anaphore);

			// Step 8. Search to the right of the anaphore!
			if (type.equals("S"))
			{
				StepEight(pt, anaphore);
			}
			pt = getSorPPorNPabove(pt);
		}

		// Step 4b: Iterate over earlier sentences.
		for (int i=1; i< sentences.size(); i++)
		{
			if (num_proposals > max_proposals) break;

			RelexInfo ri = sentences.get(i);

			// Whoops . sentence had zero parses!
			if (ri.parsedSentences.size() == 0) continue;

			// XXX ignore all but the first parse right now
			ParsedSentence ps = ri.parsedSentences.get(0);
			PhraseTree head = ps.getPhraseTree();
			StepFour(head);
		}
	}

	/**
	 * getSorNPabove() returns the next S or NP (or VP)
	 * node above this node. This is essentially Step 2
	 * of the Hobbs algorithm.
	 */
	private PhraseTree getSorNPabove(PhraseTree pt)
	{
		pt = pt.up();
		while (pt != null)
		{
			String type = pt.getPhraseType();
			if (type.equals("S")) break;
			if (type.equals("NP")) break;
			if (type.equals("SBAR")) break;
			if (type.equals("SINV")) break;
			pt = pt.up();
		}
		return pt;
	}
	/**
	 * getSorPPorNPabove() returns the next S or NP (or PP)
	 * node above this node. This is essentially Step 2
	 * of the Hobbs algorithm, but with a twist: it
	 * takes PP to be a kind-of-S, so that sentences
	 * (S (NP The window) (VP had (NP a crack) (PP in (NP it_1))) .)
	 * manage to pick up "window", by allowing PP to count
	 * as an intervening node.
	 */
	private PhraseTree getSorPPorNPabove(PhraseTree pt)
	{
		pt = pt.up();
		while (pt != null)
		{
			String type = pt.getPhraseType();
			if (type.equals("S")) break;
			if (type.equals("NP")) break;
			if (modified_step_two && type.equals("PP")) break;
			if (type.equals("SBAR")) break;
			if (type.equals("SINV")) break;
			pt = pt.up();
		}
		return pt;
	}

	/* -------------------------------------------------- */
	/**
	 * Return true to accept the antecedent reference
	 */
	private interface Filter
	{
		public Boolean filter(PhraseTree anaphore,
		                      PhraseTree ante_candidate);
	}

	/**
	 * Trivial accepting filter: accept all antecedent candidates.
	 */
	private class acceptFilter implements Filter
	{
		public Boolean filter(PhraseTree ana, PhraseTree ante)
		{
			return true;
		}
	}

	/**
	 * A contra-index filter, so that "Alice talked to her."
	 * doesn't come up with "Alice" as antecedent to "her".
	 * However, for reflexive pronouns, e.g. "Alice loved herself."
	 * then the immediate antecedent is wanted.
	 */
	private class contraFilter implements Filter
	{
		public Boolean filter(PhraseTree anaphore,
		                      PhraseTree ante_candidate)
		{
			if (isReflexive(anaphore)) return true;

			// Non-reflexive anaphore must have at least one
			// S or NP node between them and the candidate antecedent.
			PhraseTree above_anap = getSorNPabove(anaphore);
			PhraseTree above_ante = getSorNPabove(ante_candidate);
			if (above_anap.equiv(above_ante)) return false;
			return true;
		}
	}

	/* -------------------------------------------------- */
	/**
	 * StepThree() -- Perform Step 3 of the Hobbs algorithm.
	 *
	 * Traverse the constituent tree breadth-first, left to right.
	 * Always stay to the left of the path "p" connecting "head" and
	 * "path_stopper".  If antecendent candidates are found, then
	 * apply a "contra-index filter", e.g. so that the subject of
	 * a sentence or phrase is rejected when the pronoun is the
	 * object. That is, reject "Alice" as an antecedent to "her"
	 * in "Alice talked to her".
	 *
	 * If the S or NP phrase passes the filter test, then add the
	 * antecedent to the list.
	 */
	private void StepThree(PhraseTree ex,
	                       PhraseTree path_stopper)
	{
		WalkTree(ex, path_stopper, new contraFilter(), false);
	}

	/**
	 * StepFour() -- Perform Step 4 of the Hobbs algorithm.
	 *
	 * Similar to Step 3, except that all NP nodes are accepted,
	 * and all of the sentence is traversed, not just the part
	 * to the left of some path.
	 */
	private void StepFour(PhraseTree head)
	{
		WalkTree(head, null, new acceptFilter(), false);
	}

	/**
	 * StepSeven() -- Perform Step 7 of the Hobbs algorithm.
	 *
	 * Similar to Step 3, except that all NP nodes are accepted.
	 */
	private void StepSeven(PhraseTree ex,
	                       PhraseTree path_stopper)
	{
		WalkTree(ex, path_stopper, new acceptFilter(), false);
	}

	/**
	 * StepEight() -- Perform Step 8 of the Hobbs algorithm.
	 *
	 * Traverse the constituent tree to the right of path "p",
	 * in a breadth-first, left to right manner. If an NP is found,
	 * propose that NP as an antecedent. Do not traverse the tree
	 * below the NP found.
	 *
	 * The "do not traverse below" is implemented by the marker flag.
	 * The "to the right of" is implemented implicitly with the marker
	 * also: during previous parses, the marker got set for phrases
	 * to the left, and so no phrases to the left of path "p" will get
	 * visited; only those to the right will be visited.
	 */
	private void StepEight(PhraseTree ex,
	                       PhraseTree path_stopper)
	{
		WalkTree(ex, null, new acceptFilter(), true);
	}

	/* -------------------------------------------------- */

	private void WalkTree(PhraseTree head,
	                      PhraseTree path_stopper,
	                      Filter filt,
	                      Boolean stop_at_np)
	{
		String marker = getlabel(anaphore);

		// Loop around, left to right, breadth first
		Iterator<PhraseTree> iter = head.iterator();
		while (iter.hasNext())
		{
			PhraseTree pt = iter.next();
			if (pt.equiv(path_stopper)) break;

			// The breadth-first, and then move-up-the-tree
			// nature of Hobbs implies that we can often visit
			// the same nodes on the way down. These nodes were
			// either picked up in previous visits, or rejected.
			// The new filters might not reject again. So just
			// don't visit again. Use the marker to perform this
			// visit/no-vist decision.
			if (!pt.getMark(marker))
			{
				String type = pt.getPhraseType();
				if (type.equals("NP"))
				{
					// Accept as antecedents only those that the
					// structure filter allows.
					if (filt.filter(anaphore, pt))
					{
						num_proposals++;
						if (num_proposals > max_proposals) return;

						antecedents.add(anaphore, pt);
						if (DEBUG > 0)
						{
							System.out.println("Found antecedent "
						                 +  pt.toString()
						                 + " to pronoun "
						                 + anaphore.toString());

							System.out.println("Current list:\n"
							              + antecedents.toString(anaphore));
						}
						if (stop_at_np) pt.setMark(marker);
					}
				}
			}

			// Rightmost node left of path p contains the path_stopper
			if (pt.contains(path_stopper)) break;
		}

		// Now loop around again, go to the next depth.
		iter = head.iterator();
		while (iter.hasNext())
		{
			PhraseTree pt = iter.next();
			if (pt.equiv(path_stopper)) break;

			if (!pt.getMark(marker))
			{
				WalkTree(pt, path_stopper, filt, stop_at_np);

				// Mark this, we don't want to revisit again,
				// at least, not for this anaphore
				pt.setMark(marker);
			}

			// Rightmost node left of path p contains the path_stopper
			if (pt.contains(path_stopper)) break;
		}
	}

} // end Hobbs

/* ==================== END OF FILE ================== */
