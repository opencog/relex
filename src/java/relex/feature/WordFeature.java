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

package relex.feature;

/**
 * Return simple information about the proprties
 * of the word that the feature is pointing at.
 *
 * These routines will return correct results only *after*
 * the sentence has been parsed, and the algs have been run.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class WordFeature
{
	// Return true if the feature node is a pronoun.
	public static boolean isPronoun(FeatureNode fn)
	{
		fn = fn.get("ref");
		if (fn == null) return false;
		fn = fn.get("pronoun-FLAG");
		if (fn == null) return false;
		return true;
	}

	// Return true if the feature node is punctuation.
	public static boolean isPunctuation(FeatureNode fn)
	{
		fn = fn.get("POS");
		if (fn == null) return false;
		String str = fn.getValue();
		if (str.equals("punctuation")) return true;
		return false;
	}

	// Return true if the featue node is the verb "to be".
	public static boolean isCopula(FeatureNode fn)
	{
		fn = fn.get("morph");
		if (fn == null) return false;
		fn = fn.get("root");
		if (fn == null) return false;
		String root = fn.getValue();
		if (root.equals("be")) return true;
		return false;
	}
}
