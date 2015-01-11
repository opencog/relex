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

package relex;

import java.lang.Math;
import java.lang.String;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.stats.Histogram;


/**
 * This class collects a miscellany of statistics about a parsed text.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */
public class ParseStats
{
	private int count;
	private Histogram parse_count;
	private int max_parses;
	private Histogram word_count;
	private int failed_parses;
	private Histogram first_parse_confidence;
	private Histogram second_parse_confidence;
	private Histogram third_parse_confidence;
	private Histogram fourth_parse_confidence;

	private Histogram relations;
	private int relcnt;

	public ParseStats()
	{
		count = 0;
		word_count = new Histogram(1,31);

		max_parses = 10;
		parse_count = new Histogram(0,max_parses);
		failed_parses = 0;

		first_parse_confidence = new Histogram(20, 0.0, 1.0);
		second_parse_confidence = new Histogram(20, 0.0, 1.0);
		third_parse_confidence = new Histogram(20, 0.0, 1.0);
		fourth_parse_confidence = new Histogram(20, 0.0, 1.0);
		relations = new Histogram(1, 21);
	}

	public void bin(Sentence sntc)
	{
		if (null == sntc) return;

		count ++;
		int nparses = sntc.getParses().size();
		parse_count.bin(nparses);

		if (nparses <= 0) return;

		ParsedSentence fs = sntc.getParses().get(0);
		word_count.bin(fs.getNumWords());

		// If the first parse has skipped words, the parse is "failed"
		if (fs.getNumSkippedWords() != 0) failed_parses ++;

		// Count the first parse only if its "good"
		if (fs.getNumSkippedWords() == 0)
			first_parse_confidence.bin(fs.getTruthValue().getConfidence());

		if (2 <= nparses)
			second_parse_confidence.bin(sntc.getParses().get(1).getTruthValue().getConfidence());
		if (3 <= nparses)
			third_parse_confidence.bin(sntc.getParses().get(2).getTruthValue().getConfidence());
		if (4 <= nparses)
			fourth_parse_confidence.bin(sntc.getParses().get(3).getTruthValue().getConfidence());

		// Count average number of relations per sentence.
		// But only for the first, most high-confidence parse.
		relcnt = 0;
		RelCount rcnt = new RelCount();
		fs.foreach(rcnt);
		relations.bin(relcnt);
	}

	private class RelCount implements RelationCallback
	{
		public Boolean UnaryRelationCB(FeatureNode from, String rel) { return false; }
		public Boolean BinaryHeadCB(FeatureNode from) { return false; }
		public Boolean BinaryRelationCB(String relation, FeatureNode from, FeatureNode to)
		{
			relcnt ++;
			return false;
		}
	}

	public String toString()
	{
		double failed = 100.0 * ((double) failed_parses) / ((double) count);
		int pf = (int) Math.floor(failed+0.5);

		double overflow = 100.0 * ((double) parse_count.getOverflow()) / ((double) count);
		int ovfl = (int) Math.floor(overflow+0.5);

		String str = "";
		str += "\nTotal sentences: " + count;
		str += "\nFailed parses: " + failed_parses +
		       " Percent failed: " + pf + "%" +
		       " (these are parses with one or more words skipped)";
		str += "\nWords per sentence: " + word_count.getMean();
		str += "\nParses per sentence, mode: " + parse_count.getMode() +
		       " median: " + parse_count.getMedian() +
		       " mean: " +  parse_count.getMean() +
		       " stddev: " + parse_count.getStdDev();
		str += "\nsentences with more than " + max_parses + " parses: " +
		       parse_count.getOverflow() + " as percent: " + ovfl + "%";
		str += "\nRelations per parse, mode: " + relations.getMode() +
		       " median: " + relations.getMedian() +
		       " mean: " +  relations.getMean() +
		       " stddev: " + relations.getStdDev();
		str += "\nConfidence of first parse: " + first_parse_confidence.getMean() +
		       " (out of " + first_parse_confidence.getCount() + " parses)";
		str += "\nFirst parse hi/lo: " + first_parse_confidence.getAllTimeHigh() +
		       " / " + first_parse_confidence.getAllTimeLow();
		str += " stddev: " + first_parse_confidence.getStdDev();
		str += "\nConfidence of second parse: " + second_parse_confidence.getMean() +
		       " (out of " + second_parse_confidence.getCount() + " parses)";
		str += ", stddev: " + second_parse_confidence.getStdDev();
		str += "\nConfidence of third parse: " + third_parse_confidence.getMean() +
		       " (out of " + third_parse_confidence.getCount() + " parses)";
		str += ", stddev: " + third_parse_confidence.getStdDev();
		str += "\nConfidence of fourth parse: " + fourth_parse_confidence.getMean() +
		       " (out of " + fourth_parse_confidence.getCount() + " parses)";
		str += ", stddev: " + fourth_parse_confidence.getStdDev();

		str += "\n";
		return str;
	}
}

