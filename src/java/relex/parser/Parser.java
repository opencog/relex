package relex.parser;
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

import java.util.ArrayList;

import relex.ParsedSentence;

/**
 * This class has a single public interface which excepts a String and returns a vector of parses.
 */
public abstract class Parser {

	/**
	 * Given a sentence, returns a list of parses, ordered by likelihood
	 * 
	 * @param sentence
	 * @return a list of parses, ordered by likelihood
	 *
	 */
	public abstract ArrayList<ParsedSentence> parse(String sentence);

	/**
	 * Given a sentence, returns a string representation of a parse of that sentence.
	 * 
	 * @param sentence
	 * @return a string representation of a parse of that sentence.
	 */
	public abstract String simpleParse(String sentence);

}

