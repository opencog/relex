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

import java.lang.String;
import relex.stats.Histogram;

/**
 * This class collects a miscellany of statistics about a parsed text.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org> 
 */
public class ParseStats
{
	private Histogram parse_count;
	private Histogram word_count;
	public ParseStats()
	{
		word_count = new Histogram(1,31);
		parse_count = new Histogram(0,10);
	}

	public void bin(RelexInfo ri)
	{
		parse_count.bin(ri.parsedSentences.size());
	}

	public String toString()
	{
		String str = "";
		return str;
	}
}

