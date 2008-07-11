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

import relex.stats.TruthValue;
import relex.stats.SimpleTruthValue;

/**
 * Performs a simple, basic lexical chunk ranking, based on
 * the frequency of occurance.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class ChunkRanker
{
	private ArrayList<LexChunk> chunks;
	
	public ChunkRanker()
	{
		chunks = new ArrayList<LexChunk>();
	}

	public ArrayList<LexChunk> getChunks()
	{
		return chunks;
	}

	public void clear()
	{
		chunks.clear();
	}

	/**
	 * Add a chunk. If its already in the list, then increment 
	 * its liklihood of being appropriate.
	 */
	public void add(LexChunk ch, TruthValue weight)
	{
		add(ch, weight, 1.0);
	}

	public void add(LexChunk ch, TruthValue weight, double twiddle)
	{
		boolean not_found = true;
		for (LexChunk c : chunks)
		{
			// Increase the overall confidence.
			if (ch.equals(c))
			{
				not_found = false;
				ch = c;
				break;
			}
		}

		if (not_found)
		{
			// Technically, we should say "ch = ch.clone()" here ...
			ch.setTruthValue(new SimpleTruthValue(0.0, 0.0));
			chunks.add(ch);
		}

		// update the confidence of the chunk
		ch.incrementConfidence(twiddle * weight.getConfidence());
	}

	public void add(ArrayList<LexChunk> lst, TruthValue weight, double twiddle)
	{
		for (LexChunk ch : lst)
			add(ch, weight, twiddle);
	}

	public void add(ArrayList<LexChunk> lst, TruthValue weight)
	{
		for (LexChunk ch : lst)
			add(ch, weight, 1.0);
	}

	/**
	 * Sort the chunks in order of decreasing confidence.
	 * Uses a simple-minded bubble sort.
	 */
	public void sort()
	{
		int len = chunks.size();
		for (int i=0; i<len; i++)
		{
			LexChunk chi = chunks.get(i);
			double ci = chi.getConfidence();
			for (int j=i+1; j<len; j++)
			{
				LexChunk chj = chunks.get(j);
				double cj = chj.getConfidence();
				if (ci < cj)
				{
					chunks.set(i, chj);
					chunks.set(j, chi);
					chi = chj;
					ci = cj;
				}
			}
		}
	}

	/**
	 * Punish chunks whose length is other than 3.
	 */
	public void length_punish()
	{
		for (LexChunk ch : chunks)
		{
			int sz = ch.size();
			double weight = sz-3;
			if (weight < 0) weight = - weight;
			weight = 1.0 - 0.2 * weight;

			// twiddle the confidence of the chunk
			ch.rescaleConfidence(weight);
		}
	}

	/**
	 * Ad-hoc printed representation of the ranked contents.
	 * Meant to be human readable, and nothing more.
	 * May change from one Relex version to another, not stable.
	 */
	public String toString()
	{
		sort();
		String str = "";
		for (LexChunk ch: chunks)
		{
			str += "Confidence: " + ch.getConfidence();
			str += " " + ch.toString() + "\n";
		}
		return str;
	}
}
