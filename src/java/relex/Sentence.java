/*
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
 *
 * Copyright (c) 2005 Mike Ross (miross)
 * Copyright (c) 2008 Novamente LLC
 * Copyright (c) 2008, 2009 Linas Vepstas <linasvepstas@gmail.com>
 */
/*
 * @author miross
 * Created on Mar 29, 2005
 */
package relex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import relex.output.SimpleView;

/**
 * This class encapsulates various information about a sentence,
 * including the processing performed on it by RelEx.
 * It holds:
 *
 *   1) A string copy of the input sentence.
 *   2) An array of the generated parses of this sentence.
 *   3) Utilities to score (rank) the various parses.
 */
public class Sentence implements Serializable
{
	private static final long serialVersionUID = -3047552550351161106L;

	private String originalSentence;
	private ArrayList<ParsedSentence> parses;
	private int numParses;

	private String sentenceID;

	public Sentence()
	{
		originalSentence = null;
		parses = new ArrayList<ParsedSentence>(); // empty list
		numParses = 0;
	}

	public Sentence(String os, ArrayList<ParsedSentence> pl)
	{
		originalSentence = os;
		parses = pl;
		assign_id();
		numParses = pl.size();
	}

	/**
	 * Assign a unique sentence ID to each sentence; this is required
	 * for OpenCog output, where each sentence and parse needs to be
	 * tagged. In addition, each word is tagged with UUID's as well.
	 */
	private void assign_id()
	{
		UUID guid = UUID.randomUUID();
		sentenceID = "sentence@" + guid;

		int n = 0;
		for (ParsedSentence parse: parses)
		{
			String id = sentenceID + "_parse_" + n;
			parse.setIDString(id);
			parse.setSentence(this);
			parse.assign_id();
			n++;
		}
	}

	public String getID()
	{
		return sentenceID;
	}

	public void setSentence(String s)
	{
		originalSentence = s;
	}

	public String getSentence()
	{
		return originalSentence;
	}

	public void setParses (ArrayList<ParsedSentence> pl)
	{
		parses = pl;
		assign_id();
	}

	public ArrayList<ParsedSentence> getParses()
	{
		return parses;
	}

	public void setNumParses(int np)
	{
		numParses = np;
	}

	public int getNumParses()
	{
		return numParses;
	}

	/**
	 * Return an array of the words in the sentence
	 */
	public String[] getWords()
	{
		if (numParses == 0) return null;

		// Otherwise, the words from the first parse will do.
		ParsedSentence p = parses.get(0);
		int nw = p.getNumWords();
		String[] words = new String[nw];
		for (int i=0; i < nw; i++)
		{
			words[i] = p.getOrigWord(i);
		}
		return words;
	}

	/**
 	 * Assign a simple parse-ranking score, based on LinkGrammar data.
 	 * Sort the parses by decreasing rank.
 	 */
	public void simpleParseRank()
	{
		for (ParsedSentence parse : parses)
		{
			parse.simpleRankParse();
		}
		Collections.sort(parses);
	}

	/**
	 * Normalize the parse ranking, so that the highest-ranked
	 * parse has a confidence of 1.0
	 */
	public void normalizeParseRank()
	{
		double highest_rank = -1000.0;
		for (ParsedSentence parse : parses)
		{
			double rank = parse.getTruthValue().getConfidence();
			if (highest_rank < rank) highest_rank = rank;
		}
		highest_rank = 1.0 / highest_rank;
		for (ParsedSentence parse : parses)
		{
			parse.rescaleRank(highest_rank);
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		int pn = 0;
		for (ParsedSentence parse: parses)
		{
			pn++;
			sb.append("Parse " + pn + " of " + parses.size() + "\n");
			sb.append(SimpleView.printRelations(parse)).append("\n");
		}
		return sb.toString();
	}
}

