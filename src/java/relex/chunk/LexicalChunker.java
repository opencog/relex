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

/**
 * Lexical chunking base class.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public abstract class LexicalChunker
{
	private ArrayList<LexChunk> chunks;

	public LexicalChunker()
	{
		chunks = new ArrayList<LexChunk>();
	}

	/**
	 * The workhorse routine: find the actual chunks in the parse.
	 */
	abstract public void findChunks(ParsedSentence parse);

	public ArrayList<LexChunk> getChunks()
	{
		return chunks;
	}

	public void clear()
	{
		chunks.clear();
	}

	/**
	 * Add a chunk, but only if its unique.
	 */
	public void add(LexChunk ch)
	{
		for (int i=0; i<chunks.size(); i++)
		{
			LexChunk c = chunks.get(i);
			if (ch.equals(c)) return;
		}
		chunks.add(ch);
	}
}
