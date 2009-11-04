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

package relex.anaphora;

import java.util.ArrayList;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.ParsedSentence;
import relex.tree.PhraseTree;

/**
 * Return a list of all the pronouns in a parse.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class FindPronouns
{
	private static class GetPronouns implements FeatureNodeCallback
	{
		public ArrayList<PhraseTree> pronouns;
		GetPronouns()
		{
			pronouns = new ArrayList<PhraseTree>();
		}
		public Boolean FNCallback(FeatureNode fn)
		{
			// System.out.println("ola " + fn._prt_vals());
			FeatureNode pr = fn.get("pronoun-FLAG");
			if (pr != null)
			{
				pr = fn.get("nameSource");
				pronouns.add(new PhraseTree(pr));
			}
			return false;
		}
	}

	public static ArrayList<PhraseTree> findPronouns(ParsedSentence parse)
	{
		GetPronouns gp = new GetPronouns();
		parse.foreach(gp);
		return gp.pronouns;
	}
} // end FindPronouns

/* ==================== END OF FILE ================== */
