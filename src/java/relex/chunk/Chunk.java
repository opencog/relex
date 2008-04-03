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

import relex.feature.FeatureNode;

/**
 * Holder of lexical chunks
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Chunk
{
	private ArrayList<FeatureNode> chunk;
	public Chunk()
	{
		chunk = new ArrayList<FeatureNode>();
	}

	public void addWord(FeatureNode fn)
	{
		chunk.add(fn);
	}
	public void addWords(ArrayList<FeatureNode> words)
	{
		chunk.addAll(words);
	}
	public void clear()
	{
		chunk.clear();
	}
	public int size()
	{
		return chunk.size();
	}

	/**
	 * Add an entire phrase.
	 */
	public void addPhrase(FeatureNode fn)
	{
		fn = fn.get("phr-head");
		while (fn != null)
		{
			FeatureNode wd = fn.get("phr-word");
			if (wd != null) addWord(wd);

			// Add subphrases to the word list
			FeatureNode subf = fn.get("phr-head");
			if (subf != null) 
			{
				addPhrase(fn);
			}
			fn = fn.get("phr-next");
		}
	}

	/**
	 * A very simple output routine.
	 */
	public String toString()
	{
		// First, print out the phrase itself.
		String str = "Phrase: (";
		for (int i=0; i<chunk.size(); i++)
		{
			FeatureNode fn = chunk.get(i);
			// FeatureNode sf = fn.get("str");
			FeatureNode sf = fn.get("orig_str");
			if (sf != null)
			{
				if (i != 0) str += " ";
				str += sf.getValue();
			}
		}
		str += ")";

		// Next, print out the character ranges.
		str += "      Character ranges: ";
		int chunk_start = -1;
		int chunk_end = -1;
		for (int i=0; i<chunk.size(); i++)
		{
			FeatureNode fn = chunk.get(i);
			// FeatureNode sf = fn.get("str");
			FeatureNode sf = fn.get("orig_str");
			if (sf != null)
			{
				FeatureNode start = fn.get("start_char");
				FeatureNode orig = fn.get("orig_str");
				String st = start.getValue();
				String or = orig.getValue();
				if (st == null || or == null)
				{
					System.err.println("Error: chunk is missing feature nodes");
					continue;
				}
				int ist = Integer.parseInt(st);
				int len = or.length();
				int end = ist+len;
				if (chunk_start < 0)
				{
					chunk_start = ist;
					chunk_end = end;
				}
				else if (ist <= chunk_end+1)
				{
					chunk_end = end;
				}
				else
				{
					str += "["+ chunk_start + "-" + chunk_end + "]";
					chunk_start = ist;
					chunk_end = end;
				}
			}
		}
		if (0 <= chunk_start)
		{
			str += "["+ chunk_start + "-" + chunk_end + "]";
		}
		return str;
	}

	/**
	 * return true if the other oject equails this one, else return false.
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof Chunk)) return false;
		Chunk oth = (Chunk) other;
		if (oth.chunk.size() != chunk.size()) return false;
		for (int i=0; i<chunk.size(); i++)
		{
			if(chunk.get(i) != oth.chunk.get(i)) return false;
		}
		return true;
	}
}
