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

package relex.output;

import java.util.HashMap;

import relex.ParsedSentence;
import relex.feature.FeatureNode;

/**
 * The OpenCogXML object outputs a ParsedSentence in the Novamente
 * OpenCog-style XML format. The actual format used, and its rational,
 * is described in greater detail in the README file in the opencog
 * source code directory src/nlp/wsd/README.
 *
 * As the same sentence can have multiple parses, this class only
 * displays a single, particular parse.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
public class OpenCogXML
{
	private HashMap<FeatureNode,String> id_map = null;
	private RelXML rel_xml;
	private FrameXML frame_xml;

	/* -------------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */
	public OpenCogXML()
	{
		rel_xml = new RelXML();
		frame_xml = new FrameXML();
	}

	public void setParse(ParsedSentence sent)
	{
		id_map = new HashMap<FeatureNode,String>();
		rel_xml.setParse(sent, id_map);
		frame_xml.setParse(sent, id_map);
	}

	/* -------------------------------------------------------------------- */
	public String toString()
	{
		String ret = "";

		ret += "<list>\n";
		ret += rel_xml.toString();
		ret += frame_xml.toString();
		ret += "</list>\n";

		return ret;
	}

} // end OpenCogXML
