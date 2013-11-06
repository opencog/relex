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

package relex.concurrent;

import relex.morphy.Morphy;
import relex.parser.IParser;

public class RelexContext
{
//	private LinkParserClient linkParserClient;
	private IParser parser;
	private Morphy morphy;

/*	public LinkParserClient getLinkParserClient() {
		return linkParserClient;
	}

	public void setLinkParserClient(LinkParserClient linkParserClient) {
		this.linkParserClient = linkParserClient;
	} */

	public IParser getParser()
	{
		return parser;
	}

	public Morphy getMorphy() {
		return morphy;
	}

	public void setMorphy(Morphy morphy) {
		this.morphy = morphy;
	}

	public RelexContext() {
	}

	public RelexContext(IParser parser, Morphy morphy) {
		//this.linkParserClient= lpc;
		this.parser = parser;
		this.morphy = morphy;
	}

}
