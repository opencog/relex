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
import java.util.HashMap;
import relex.tree.PhraseTree;
import relex.feature.FeatureNode;

/**
 * Implements storage of anaphora-antecedent pairs,
 * used during pronoun resolution.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Antecedents
{
	public static final int DEBUG = 0;

	// Map of anaphora to a list of candidate antecedents.
	// What's actually stored are the pointers to the
	// phrases for each. To get the actual words, use the 
	// getPhraseLeader() function.
	private HashMap<PhraseTree, ArrayList<PhraseTree>> ante_map;

	public Antecedents()
	{
		ante_map = new HashMap<PhraseTree, ArrayList<PhraseTree>>();
	}

	/**
	 * add -- add a pronoun/antecedent pair to the list
	 * @return: true if the pair was added to the list.
	 */
	public boolean add(PhraseTree anaph, PhraseTree ante)
	{
		// filter return of true rejects the antecedent
		if (applyFilters(anaph, ante)) return false;

		ArrayList<PhraseTree> ante_list = ante_map.get(anaph);
		if (ante_list == null)
		{
			ante_list = new ArrayList<PhraseTree>();
			ante_map.put(anaph, ante_list);
		}
		ante_list.add(ante);
		return true;
	}

	/**
	 * clear() -- remove all pronoun/antecedent pairs from the list
	 */
	public void clear()
	{
		ante_map = new HashMap<PhraseTree, ArrayList<PhraseTree>>();
	}

	/**
	 * getAntecedents -- return the collection of antecedents
	 * Returns a map of prononous to antecedent lists.
	 * The antecedents are ordered from most likely to 
	 * least likely.
	 */
	private ArrayList<FeatureNode> getAntecedent(PhraseTree anaph)
	{
		ArrayList<PhraseTree> ante_list = ante_map.get(anaph);
		if (ante_list == null) return null;

		ArrayList<FeatureNode> rlist = new ArrayList<FeatureNode>();
		// Integer i is the "Hobbs distance"
		for (int i=0; i<ante_list.size(); i++)
		{
			PhraseTree ante = ante_list.get(i);
			FeatureNode nante = ante.getPhraseLeader();
			if (nante != null) rlist.add(nante);
		}
		return rlist;
	}

	public HashMap<FeatureNode,ArrayList<FeatureNode>> getAntecedents()
	{
		HashMap<FeatureNode,ArrayList<FeatureNode>> fmap = 
			new HashMap<FeatureNode,ArrayList<FeatureNode>>();
		for (PhraseTree anaph : ante_map.keySet())
		{
			ArrayList<FeatureNode> amap = getAntecedent(anaph);
			FeatureNode prn = anaph.getPhraseLeader();
			if (prn != null) fmap.put(prn, amap);
		}
		return fmap;
	}

	/* ----------------------------------------------------------- */
	/* Agreement filters */

	/**
	 * Reject anaphora that reference other anaphora as antecedents.
	 * I.E. "she" can't refer to another "she".
	 * Retun true to reject.
	 */
	private Boolean antiAnaFilter(FeatureNode anph, FeatureNode ante)
	{
		FeatureNode pro = ante.get("PRONOUN-FLAG");
		if (pro == null) return false;
		return true;
	}

	/**
	 * Reject antecedents whose number does not agree with
	 * the anaphora. noun_number is always valued as 
	 * "singular", "plural" or "uncountable".
	 * Retun true to reject.
	 */
	private Boolean numberFilter(FeatureNode anph, FeatureNode ante)
	{
		FeatureNode ant = ante.get("noun_number");

		// Some antecedents may not have a noun number, possibly due
		// to a relex bug, or some valid reason?
		if (ant == null) return false;
		String sant = ant.getValue();

		FeatureNode prn = anph.get("noun_number");

		// "it" won't have a noun_number, since it needs to be
		// singular ("It was a book") or uncountable ("It was hot coffee");
		// However, "it" can never match a plural.
		if (prn == null)
		{
			if (sant.equals("plural")) return true;
			return false;
		}

		String sprn = prn.getValue();
		if (sant.equals(sprn)) return false;
		return true;
	}

	/**
	 * Reject antecedents whose gender does not agree with
	 * the anaphora. gender is always valued as 
	 * "masculine", "feminine" or "neuter"
	 * Retun true to reject.
	 */
	private Boolean genderFilter(FeatureNode anph, FeatureNode ante)
	{
		// XXX All anaphors should have a gender at this point,
		// we should probably signal an error if not.
		FeatureNode prn = anph.get("gender");
		if (prn == null) return false;
		String sprn = prn.getValue();

		// If antecedents don't have gender indicated,
		// assume they are neuter. These can only match 
		// non-sex pronouns.
		FeatureNode ant = ante.get("gender");
		if (ant == null)
		{
			if(sprn.equals("neuter")) return false;
			return true;
		}
		String sant = ant.getValue();

		if (sant.equals(sprn)) return false;
		if (sant.equals("person") && sprn.equals("masculine")) return false;
		if (sant.equals("person") && sprn.equals("feminine")) return false;
		return true;
	}

	/**
	 * Phrases like (NP (NP the boxes) and (NP the cup)) 
	 * will not have a leader. Reject it, and, instead,
	 * try to pick up the individual parts. 
	 * XXX but this is wrong/messy:
	 * "Alice held a lamp and and ashtray. They were ugly"
	 */
	private Boolean nullLeaderFilter(PhraseTree ante, FeatureNode prn)
	{
		return true;
	}

	private Boolean applyFilters(PhraseTree anaph, PhraseTree ante)
	{
		FeatureNode prn = anaph.getPhraseLeader();
		if (prn == null)
		{
			System.err.println ("Warning: Anaphore is missing a phrase leader!\n" +
			             anaph.toString());
			return false;
		}
		prn = prn.get("ref");

		if (DEBUG > 0)
		{
			System.out.println("Anaphore: " + anaph.toString());
			System.out.println("Candidate antecedent: " + ante.toString());
		}

		FeatureNode ref = ante.getPhraseLeader();
		if ((ref == null) && nullLeaderFilter(ante, prn)) return true;

		ref = ref.get("ref");

		// Apply filters. Filter returns true to reject.
		if (antiAnaFilter(prn, ref)) return true;
		if (numberFilter(prn, ref)) return true;
		if (genderFilter(prn, ref)) return true;
		return false;
	}

	/* ----------------------------------------------------------- */

	private String getword(PhraseTree pt)
	{
		FeatureNode ph = pt.getPhraseLeader();
		if (ph == null) return "";
		ph = ph.get("str");
		if (ph == null) return "";
		return ph.getValue();
	}

	/**
	 * toString() -- utility print shows antecedent candidates
	 *
	 * The integer printed in the square brackets is the 
	 * "Hobbs score" for the candidate: the lower, the more likely.
	 */
	public String toString(PhraseTree prn)
	{
		ArrayList<PhraseTree> ante_list = ante_map.get(prn);
		if (ante_list == null) return "";
		String str = "";
		String prw = getword(prn);

		// Integer i is the "Hobbs distance"
		for (int i=0; i<ante_list.size(); i++)
		{
			PhraseTree ante = ante_list.get(i);
			str += "_ante_candidate(" + prw + ", " + getword(ante) + ") {" + i + "}\n";
		}
		return str;
	}

	public String toString()
	{
		String str = "";
		for (PhraseTree prn : ante_map.keySet())
		{
			str += toString(prn);
		}
		return str;
	}

} // end Antecedents

/* ==================== END OF FILE ================== */
