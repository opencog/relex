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

package relex.wsd;

import java.util.ArrayList;

import relex.ParsedSentence;
import relex.RelationExtractor;
import relex.RelexInfo;
import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.feature.RelationCallback;
import relex.stats.SimpleTruthValue;
import relex.stats.TruthValue;

/**
 * Experimental word-sense disambiguation code.
 *
 * 1) part-of-speech must match, weighted by parse ranking.
 * 2) look for _nn in one but not another, this disqualifies things.
 *    (e.g. fishing expedition vs. went fishing.
 */
public class WordSense
{
	public SimpleTruthValue wordSenseMatch(String word,
	                                       RelexInfo example,
	                                       RelexInfo target)
	{
		SimpleTruthValue stv = new SimpleTruthValue();

		class SensePair
		{
			public SimpleTruthValue stv;
			public ParsedSentence xmp;
			public ParsedSentence tgt;
			public FeatureNode xmp_word;
			public FeatureNode tgt_word;
		}

		ArrayList<SensePair> spl = new ArrayList<SensePair>();

		// Create the initial set of example-target matchups.
		// Immediately rule out any that do not have matching
		// parts of speech.
		//
		for (ParsedSentence exparse : example.parsedSentences)
		{
			FeatureNode xmp_fn = exparse.findWord(word);
			String xmp_pos = LinkableView.getPOS(xmp_fn);

			TruthValue xmp_tv = exparse.getTruthValue();
			double xmp_conf = xmp_tv.getConfidence();
			for (ParsedSentence tgparse : target.parsedSentences)
			{
				FeatureNode tgt_fn = tgparse.findWord(word);
				String tgt_pos = LinkableView.getPOS(tgt_fn);
				if (tgt_pos.equals(xmp_pos))
				{
					SensePair sp = new SensePair();
					TruthValue tgt_tv = tgparse.getTruthValue();
					double tgt_conf = tgt_tv.getConfidence();
					sp.stv = new SimpleTruthValue(1.0, xmp_conf*tgt_conf);
					sp.xmp = exparse;
					sp.tgt = tgparse;
					sp.xmp_word = xmp_fn;
					sp.tgt_word = tgt_fn;
System.out.println("got "+ word + " pos " + xmp_pos + " conf="+xmp_conf*tgt_conf);
					spl.add(sp);
				}
			}
		}

		class RCB implements RelationCallback
		{
			FeatureNode word;
			public Boolean UnaryRelationCB(FeatureNode node, String attrName)
			{
				return false;
			}
			public Boolean BinaryRelationCB(String relation,
			                  FeatureNode srcNode, FeatureNode tgtNode)
			{
if (word == srcNode.get("nameSource")) System.out.println("src match "+relation);
if (word == tgtNode.get("nameSource")) System.out.println("tgt match "+relation);
				return false;
			}
			public Boolean BinaryHeadCB(FeatureNode from)
			{
				return false;
			}
		}

		RCB rcb = new RCB();

		// Now, loop over the plausible pairs, and see if 
		// the word is used in the same way in both sentences.
		for (SensePair sp : spl)
		{
System.out.println("-------");
			rcb.word = sp.xmp_word;
			sp.xmp.foreach(rcb);
		}

		return stv;
	}

	/**
	 * Test/sample usage
	 */

	public static void main(String[] args)
	{
		RelationExtractor re = new RelationExtractor(false);

		String example_sentence = "I was fishing for an answer";
		String target_sentence = "We went on a fishing expedition.";

		RelexInfo ri_example = re.processSentence(example_sentence);
		RelexInfo ri_target = re.processSentence(target_sentence);

		WordSense ws = new WordSense();
		ws.wordSenseMatch("fishing", ri_example, ri_target);
	}

} // end WordSense

