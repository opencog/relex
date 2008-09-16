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

import relex.feature.FeatureNode;
import relex.frame.Frame;
import relex.ParsedSentence;

/**
 * Implements OpenCog Scheme output of the semantic frames.
 *
 * Copyright (c) 2008 Linas Vepstas <linas@linas.org>
 */
public class OpenCogSchemeFrame
{
	// The sentence being examined.
	private ParsedSentence sent;

	// The id_map, previously created, for OpenCog id's
	private HashMap<FeatureNode,String> id_map = null;

	private Frame frame;

	/* ------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */

	public OpenCogSchemeFrame()
	{
		sent = null;
		id_map = null;
		frame = new Frame();
	}

	public void setParse(ParsedSentence s, HashMap<FeatureNode,String> im)
	{
		sent = s;
		id_map = im;
	}

	/* ------------------------------------------------------------- */

	private String printFrames()
	{
		String ret = "";
		String fin = SimpleView.printRelationsAlt(sent, id_map);
		String[] fms = frame.process(fin);

		for (String fm : fms) 
		{
			// First, parse out the framenet string 
			if (fm.charAt(0) != '^') continue;
			int uscore = fm.indexOf('_');
			if (0 > uscore) continue;

			// String r = fm.substring(0, uscore);

			fm = fm.substring(uscore+1);
			int colon = fm.indexOf(':');
			if (0 > colon) continue;

			String frm = fm.substring(0,colon);

			fm = fm.substring(colon+1);
			int lparen = fm.indexOf('(');
			if (0 > lparen) continue;

			String felt = fm.substring(0,lparen);

			fm = fm.substring(lparen+1);
			int rparen = fm.indexOf(')');
			if (0 > rparen) continue;

			String cpt1 = fm.substring(0,rparen);
			String cpt2 = null;

			// Echo the parsed string 
			ret += "; " + frm + ":" + felt + "(" + cpt1 + ")\n";

			// Are there two concepts, or just one?
			int comma = cpt1.indexOf(',');
			if (0 < comma)
			{
				cpt2 = cpt1.substring(comma+1);
				cpt1 = cpt1.substring(0, comma);
			}
			if (cpt1 == null) continue;
			// cpt1 = id_map.get(cpt1);
			if (cpt1 == null) continue;

			// Is cpt1 a "DefinedLinguisticConceptNode"?
			Boolean cpt1_is_ling = false;
			Boolean cpt2_is_ling = false;
			if (cpt1.charAt(0) == '#') cpt1_is_ling = true;

			if (cpt2 != null)
			{
				// cpt2 = id_map.get(cpt2);
				if (cpt2 == null) continue;
				if (cpt2.charAt(0) == '#') cpt2_is_ling = true;
			}

			// Link together.
			ret += "(FrameElementLink\n" +
			       "   (DefinedFrameNode \"#" + frm + "\")\n" +
			       "   (DefinedFrameElementNode \"#" +
                     frm + ":" + felt + "\")\n)\n";

			// Now, for the specific mappings
			ret += "(InheritanceLink\n";
			if (cpt1_is_ling)
			{
				ret += "   (DefinedLinguisticConceptNode \"" + 
				            cpt1 + "\")\n";
			}
			else
			{
				ret += "   (ConceptNode \"" + cpt1 + "\")\n";
			}
			ret += "   (DefinedFrameNode \"#" + frm + "\")\n)\n";

			// If no second concept, then we are done.
			if (cpt2 == null) continue;

			// Finally link the frame element
			ret += "(EvaluationLink\n";
			ret += "   (DefinedFrameElementNode \"#" +
                     frm + ":" + felt + "\")\n";

			// Embedded: Link first and second concepts together.
			ret += "   (ListLink\n";
			if (cpt1_is_ling)
			{
				ret += "      (DefinedLinguisticConceptNode \"" + 
				            cpt1 + "\")\n";
			}
			else
			{
				ret += "      (ConceptNode \"" + cpt1 + "\")\n";
			}
			if (cpt2_is_ling)
			{
				ret += "      (DefinedLinguisticConceptNode \"" + 
				            cpt2 + "\")\n";
			}
			else
			{
				ret += "      (ConceptNode \"" + cpt2 + "\")\n";
			}
			ret += "   )\n)\n";
		}
		return ret;
	}

	/* ------------------------------------------------------------- */
	public String toString()
	{
		return printFrames();
	}
}

/* ============================ END OF FILE ====================== */
