/*
 * Copyright 2008,2009 Novamente LLC
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import relex.ParsedSentence;
import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;

public abstract class SentenceAlgorithm
{
	private static final Logger logger = LoggerFactory.getLogger(SentenceAlgorithm.class);
	private static final String SIGNATURE_FEATURE_NAME = "SIG";

	public static final boolean INTERACTIVE = false;

	/**
	 * Iterates over the entire feature structure in the ParsedSentence,
	 * applying the algorithm where-ever it can be applied.
	 */
	public void apply(ParsedSentence sentence, RelexContext context)
	{
		Iterator<FeatureNode> i = iteratorFromLeft(sentence);
		while (i.hasNext()) {
			FeatureNode c = i.next();
			Map<String,FeatureNode> vars = canApplyTo(c);
			if (null != vars) {
				boolean printResult = false;
				logger.debug("{} ", getSignature());
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
					applyTo(c, context, vars);
				} catch (Exception e) {
					if (logger.isDebugEnabled())
					{
						// System.err.println(sentence);
						// System.err.println(this);
						logger.debug("Error: bad algorithm: {}", getSignature());
						e.printStackTrace();
					}
					sentence.setErrorString(this + "\n" + e.toString());
				}
				if (printResult)
					System.err.println("Info: " + sentence);

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

	/* ---------------------------------------------------------------- */
	/**
	 * Returns an Iterator over ALL the FeatureNodes in the parse.
	 * That is, not only are nodes representing the constituents
	 * returned, but also all their sub-FeatureNodes representing
	 * links, semantic info, etc.
	 *
	 * @return an Iterator over ALL the FeatureNodes in the parse.
	 */
	public Iterator<FeatureNode> iteratorFromLeft(ParsedSentence sent)
	{
		return _iteratorFromLeft(sent.getLeft(),
		                         new LinkedHashSet<FeatureNode>()).iterator();
	}

	private LinkedHashSet<FeatureNode>
	_iteratorFromLeft(FeatureNode f, LinkedHashSet<FeatureNode> alreadyVisited)
	{
		if (alreadyVisited.contains(f))
			return alreadyVisited;
		alreadyVisited.add(f);
		if (f.isValued())
			return alreadyVisited;
		Iterator<String> i = f.getFeatureNames().iterator();
		while (i.hasNext())
			_iteratorFromLeft(f.get(i.next()), alreadyVisited);
		return alreadyVisited;
	}

	/**
	 * Returns index of next character to process in string.
	 */
	protected abstract int init(String s);

	protected abstract String getSignature();

	protected abstract void applyTo(FeatureNode node, RelexContext context,
	                                Map<String,FeatureNode> vars);

	protected abstract Map<String,FeatureNode> canApplyTo(FeatureNode node);

}
