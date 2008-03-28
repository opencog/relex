package relex.algs;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.parser.LinkParserClient;

public abstract class SentenceAlgorithm {

	private static String SIGNATURE_FEATURE_NAME = "SIG";

	public static boolean VERBOSE = false;

	public static boolean INTERACTIVE = false;

	/**
	 * Iterates over the entire feature structure in the ParsedSentence,
	 * applying the algorithm whereever it can be applied.
	 */
	public void apply(ParsedSentence sentence, LinkParserClient lpc) {
		Iterator<FeatureNode> i = sentence.iteratorFromLeft();
		while (i.hasNext()) {
			FeatureNode c = i.next();
			if (canApplyTo(c)) {
				boolean printResult = false;
				if (VERBOSE)
					System.out.print(" " + getSignature());
				if (INTERACTIVE) {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						if (br.readLine().length() > 0)
							printResult = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} // end if(INTERACTIVE)
				try {
					applyTo(c, lpc);
				} catch (Exception e) {
					sentence.setErrorString(this + "\n" + e.toString());
					// System.out.println(sentence);
					// System.out.println(this);
					// e.printStackTrace();
				}
				if (printResult)
					System.out.println(sentence);
				
				FeatureNode f = c.get(SIGNATURE_FEATURE_NAME);
				if (f == null) {
					f = new FeatureNode(getSignature());
					c.set(SIGNATURE_FEATURE_NAME, f);
				} else {
					f.forceValue(f.getValue() + " " + getSignature());
				}
			}
		}
	}

	/**
	 * Returns index of next character to process in string.
	 */
	protected abstract int init(String s);

	protected abstract String getSignature();

	protected abstract void applyTo(FeatureNode node, LinkParserClient lpc);

	protected abstract boolean canApplyTo(FeatureNode node);

}
