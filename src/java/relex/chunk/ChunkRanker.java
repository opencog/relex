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

public abstract class ChunkRanker
{
	private ArrayList<LexChunk> chunks;
	
	public ChunkRanker()
	{
		chunks = new ArrayList<LexChunk>();
	}

	public ArrayList<LexChunk> getChunks()
	{
		rank();
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
		for (int i=0; i<chunks.size(); i++)
		{
			LexChunk c = chunks.get(i);

			// Increase the overall truth value if found,
			if (ch.equals(c))
			{
				TruthValue tv = c.getTruthValue();
				SimpleTruthValue stv = (SimpleTruthValue) tv;
				// stv.increment(1.0);
				return;
			}
		}
		chunks.add(ch);
	}

	private void rank()
	{
	}
}
