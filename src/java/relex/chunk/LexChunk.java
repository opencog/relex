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
import relex.feature.WordFeature;
import relex.feature.Chunk;

/**
 * Holder of lexical chunks
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class LexChunk extends Chunk
{
	private static final long serialVersionUID = 8648176749497688526L;

	public void addWord(FeatureNode fn)
	{
		if (WordFeature.isPunctuation(fn)) return;
		chunk.add(fn);
	}
	public void addWords(ArrayList<FeatureNode> words)
	{
		addNodes(words);
	}

	/**
	 * Return true if the other object equals this one, else return false.
	 * Equals, in the comp-sci sense (e.g. scheme or lisp): compares 
	 * structure to determine if they have congruent structure.
	 * For lexical object, this means "the same words", and nothing more.
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof LexChunk)) return false;
		LexChunk oth = (LexChunk) other;
		if (oth.chunk.size() != chunk.size()) return false;
		for (int i=0; i<chunk.size(); i++)
		{
			FeatureNode fthis = chunk.get(i);
			FeatureNode foth = oth.chunk.get(i);

			// Compare string values .. 
			FeatureNode sfthis = fthis.get("orig_str");
			FeatureNode sfoth = foth.get("orig_str");
			if (sfthis == null || sfoth == null) return false;
			String sthis = sfthis.getValue();
			String soth = sfoth.getValue();
			if (!sthis.equals(soth)) return false;

			// Make sure that the location in the sentence matches also.
			FeatureNode tstart = fthis.get("start_char");
			FeatureNode ostart = foth.get("start_char");
			String st = tstart.getValue();
			String ot = ostart.getValue();
			if (st == null || ot == null) return false;
			if (!st.equals(ot)) return false;
		}
		return true;
	}

	/**
	 * A very simple output routine. It is meant to provide
	 * common-sense, human readable output, rahter than a fixed,
	 * computer-paraable format. It is subject to change from one
	 * relex release to another.
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
}
