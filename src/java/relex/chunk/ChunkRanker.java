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
	private int numParses;
	private ArrayList<LexChunk> chunks;
	
	public ChunkRanker(int np)
	{
		numParses = np;
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
	public void add(LexChunk ch)
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
			ch.setTruthValue(new SimpleTruthValue());
			chunks.add(ch);
		}

		TruthValue tv = ch.getTruthValue();
		SimpleTruthValue stv = (SimpleTruthValue) tv;
		double confidence = stv.getConfidence();
		confidence += 1.0/numParses;

		stv.setConfidence(confidence);
	}

	public void add(ArrayList<LexChunk> lst)
	{
		for (LexChunk ch : lst)
			add(ch);
	}

	/**
	 * Ad-hoc printed representation of the ranked contents.
	 * Meant to be human readable, and nothing more.
	 * May change from one Relex version to another, not stable.
	 */
	public String toString()
	{
		String str = "";
		for (LexChunk ch: chunks)
		{
			TruthValue tv = ch.getTruthValue();
			SimpleTruthValue stv = (SimpleTruthValue) tv;
			double confidence = stv.getConfidence();

			str += "Confidence: " + confidence;
			str += " " + ch.toString() + "\n";
		}
		return str;
	}
}
