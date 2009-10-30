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

package relex.algs;

import relex.ParsedSentence;
import relex.concurrent.RelexContext;

/**
 * SentenceAlgorithmApplier is responsible for loading SentenceAlgorithms from a
 * file, and applying them to a ParsedSentence.
 */
public class SentenceAlgorithmApplier
{
	private AlgorithmApplier tagger;
	private AlgorithmApplier penn;
	private AlgorithmApplier preps;
	private AlgorithmApplier semant;
	private AlgorithmApplier stanford;

	public SentenceAlgorithmApplier()
	{
		tagger = new AlgorithmApplier(
			"relex.tagalgpath", "relex-tagging-algs.txt");
		semant = new AlgorithmApplier(
			"relex.semalgpath", "relex-semantic-algs.txt");
		penn = new AlgorithmApplier(
			"relex.pennalgpath", "relex-penn-tagging-algs.txt");
		preps = new AlgorithmApplier(
			"relex.prepalgpath", "relex-prep-algs.txt");
		stanford = new AlgorithmApplier(
			"relex.sfalgpath", "relex-stanford-algs.txt");
	}
	
	public void tagFeatures(ParsedSentence sentence, RelexContext context)
	{
		tagger.applyAlgs(sentence, context);
	}

	public void extractSemantics(ParsedSentence sentence, RelexContext context)
	{
		semant.applyAlgs(sentence, context);
	}

	public void extractStanford(ParsedSentence sentence, RelexContext context)
	{
		stanford.applyAlgs(sentence, context);
	}

	public void pennTag(ParsedSentence sentence, RelexContext context)
	{
		penn.applyAlgs(sentence, context);
	}

	public void expandPreps(ParsedSentence sentence, RelexContext context)
	{
		preps.applyAlgs(sentence, context);
	}

	// The apply method, for the core relations only.
	public void applyAlgs(ParsedSentence sentence, RelexContext context)
	{
		tagger.applyAlgs(sentence, context);
		semant.applyAlgs(sentence, context);
	}

	public static void main(String[] args)
	{
		new SentenceAlgorithmApplier();
	}
}
