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

package relex.parser_old;

import relex.Sentence;

/**
 * The interface to
 * 
 * @deprecated
 */
public abstract class Parser
{
	/**
	 * Given a sentence, returns a list of parses, ordered by likelihood
	 * 
	 * @param sentence
	 * @param LinkParserClient The client to be used for link-grammar processing
	 * @return a list of parses and supporting information.
	 *
	 */
	public abstract Sentence parse(String sentence, LinkParserClient lpc);

	/**
	 * Given a sentence, returns a string representation of a parse of that sentence.
	 * 
	 * @param sentence
	 * @param LinkParserClient The client to be used for link-grammar processing
	 * @return a string representation of a parse of that sentence.
	 */
	public abstract String simpleParse(String sentence, LinkParserClient lpc);

}

