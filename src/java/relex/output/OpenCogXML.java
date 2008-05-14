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

/**
 * The OpenCogXML object outputs a ParsedSentence in 
 * the Novamente OpenCog-style XML
 *
 * As the same sentence can have multiple parses, this
 * class only displays a single, particular parse.
 *
 * This class makes heavy use of String. If performance needs to be
 * improved, then a conversion to StringBuff should be considered.
 *
 * Copyright (C) 2007,2008 Linas Vepstas <linas@linas.org>
 */
public class OpenCogXML
{
	private HashMap<String,String> id_map = null;
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
		id_map = new HashMap<String,String>();
		rel_xml.setParse(sent, id_map);
		frame_xml.setParse(sent, id_map);
	}

	/* -------------------------------------------------------------------- */
	public String toString()
	{
		String ret = "";

		ret += "<list>\n";
		ret += "<AssertionLink>\n";
		ret += rel_xml.toString();
		ret += frame_xml.toString();
		ret += "</AssertionLink>\n";
		ret += "</list>\n";

		return ret;
	}

} // end OpenCogXML
