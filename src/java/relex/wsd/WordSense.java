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
 *
 * XXX this cleas does not correctly handle multiple occurances
 * of a word in a sentence.
 */
public class WordSense
{
	public SimpleTruthValue wordSenseMatch(String word,
	                                       RelexInfo example,
	                                       RelexInfo target)
	{
		SimpleTruthValue stv = new SimpleTruthValue();

		// The SensePair class matches up possible pairings
		// of the word usage in the example and target sentence.
		// The associated truth value is meant to indicate the
		// likelihood of the sense usage being the same.
		class SensePair
		{
			public SimpleTruthValue stv;
			public ParsedSentence tgt;
			public ParsedSentence xmp;
			public FeatureNode tgt_word;
			public FeatureNode xmp_word;
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

		// Stubby class to make following just a little easier.
		// This just implements the for-loop block.
		class RCB implements RelationCallback
		{
			FeatureNode word;
			SensePair sp;

			public Boolean UnaryRelationCB(FeatureNode node, String attrName)
			{
				return false;
			}
			public Boolean BinaryRelationCB(String relation,
			                  FeatureNode srcNode, FeatureNode tgtNode)
			{
				return false;
			}
			public Boolean BinaryHeadCB(FeatureNode from)
			{
				return false;
			}
		}

		// Inner loop block -- this class is the non-quite-anonymous
		// inner loop, which runs over all example-sentence relations.
		// (Java does not have the concept of anonymous loop blocks).
		class XmpCB extends RCB
		{
			int which;  // first or second arg of the relation.
			String relation_name;  // rlation we are trying to match.

			public Boolean BinaryRelationCB(String relation,
			                  FeatureNode srcNode, FeatureNode tgtNode)
			{
				// If the word doesn't even appear in the relation,
				// just ignore this relation, try the next one.
				if ((word != srcNode.get("nameSource")) && 
				    (word != tgtNode.get("nameSource")))
				{
					return false;
				}

				// If the relation names don't match, then it
				// is very unlikely that the word sense usage 
				// in the target and example sentences match.
				// So strike this one from the ranks.
				// Assume the relation name matches, we also
				// expect the word to appear in the same slot
				// in the location -- if not, again, the 
				// matching seems unlikely.
				if (relation.equals(relation_name) &&
				   (((word == srcNode.get("nameSource")) && which == 1) ||
				    ((word == tgtNode.get("nameSource")) && which == 2)))
				{
System.out.println("yahoo mtg aatch "+relation);
					return false;
				}

				// System.out.println("Reject " + relation + " vs. " + relation_name);
				// If we got to here, there's no match.
				sp.stv.setMean(0.0, 1.0);
				return false;
			}
		}

		// Outer loop block -- this class is the non-quite-anonymous
		// outer loop, which runs over all target-sentence relations.
		// (Java does not have the concept of anonymous loop blocks).
		class TgtCB extends RCB
		{
			public Boolean BinaryRelationCB(String relation,
			                  FeatureNode srcNode, FeatureNode tgtNode)
			{
				// "which" just says whether its the first or second 
				// arg of the relation.
				int which = 0;
				if (word == srcNode.get("nameSource")) 
				{
					which = 1;
				}
				if (word == tgtNode.get("nameSource"))
				{
					which = 2;
				}

				// "which" isn't zero only if this word participated 
				// in this relation. If it did, then loop over the
				// relations in the example sentence, to see if we
				// can find an analoguous one there.
				if (which != 0)
				{
					XmpCB xmp_cb = new XmpCB();
					xmp_cb.sp = sp;
					xmp_cb.word = sp.xmp_word;
					xmp_cb.which = which;
					xmp_cb.relation_name = relation;

					// Loop over all relations in the example.
					sp.xmp.foreach(xmp_cb);
				}
				return false;
			}
		}

		TgtCB tgt_cb = new TgtCB();

		// Now, loop over the plausible pairs, and see if 
		// the word is used in the same way in both sentences.
		for (SensePair sp : spl)
		{
			tgt_cb.word = sp.tgt_word;
			tgt_cb.sp = sp;

			// loop over all relations in the target
			sp.tgt.foreach(tgt_cb);
		}

		// Remove all pairs that are blatent mis-matches.
		int i = 0;
		while (true)
		{
			SensePair sp = spl.get(i);
			i++;
			if ((sp.stv.getMean() < 0.01) &&
			    (sp.stv.getConfidence() > 0.99))
			{
				spl.remove(sp);
				i = 0;
			}
			if (spl.size() <= i) break;
		}

		// If there are are no remaining matches, then we are
		// unequivally sure that there is no word sense match.
		if (spl.size() == 0)
		{
			stv.setMean(0.0, 1.0);
			return stv;
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
		SimpleTruthValue stv = ws.wordSenseMatch("fishing", ri_example, ri_target);
		System.out.println("Got " + stv.getMean() + " conf=" + stv.getConfidence());
	}

} // end WordSense

