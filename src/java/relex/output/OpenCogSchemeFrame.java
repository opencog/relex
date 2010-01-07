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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	// The id_map translates UUID back to words.
	private HashMap<String,String> uuid_to_root_map = null;
	
	private Frame frame;

	/* ------------------------------------------------------------- */
	/* Constructors, and setters/getters for private members. */

	public OpenCogSchemeFrame()
	{
		sent = null;
		frame = new Frame();
	}

	public void setParse(ParsedSentence s)
	{
		sent = s;
		uuid_to_root_map = new HashMap<String,String>();
		FeatureNode fn = s.getLeft();
		while (fn != null)
		{
			FeatureNode fs = fn.get("str");
			if (null != fs)
			{
				String lemma = fs.getValue();
				String uuid = fn.get("uuid").getValue();
				uuid_to_root_map.put(uuid, lemma);
			}
			fn = fn.get("NEXT");
		}
	}
		
	/* ------------------------------------------------------------- */
	
	private String printFrames()
	{
		String ret = "";
		String fin = SimpleView.printRelationsUUID(sent);
		String[] fms = frame.process(fin,uuid_to_root_map);
		
		boolean onlyNewFormat = true;
		
		HashMap<String, ArrayList< FrameItem> > frames = 
							new HashMap< String, ArrayList<FrameItem> >();
		
		/*
		 * fms contains the frames like:
		 * ^1_Color:Color(red,ball)
		 * ^1_Color:Entity(red,ball)
		 */		
		for (String fm : fms) 
		{
			// First, parse out the framenet string 
			if (fm.charAt(0) != '^') continue;
			int uscore = fm.indexOf('_');
			if (0 > uscore) continue;

			// String r = fm.substring(0, uscore);
			
			// fm contains: Color:Color(red,ball)
			fm = fm.substring(uscore+1);
			int colon = fm.indexOf(':');
			if (0 > colon) continue;

			//Color (1o) => frame name
			String frm = fm.substring(0,colon);

			fm = fm.substring(colon+1);
			int lparen = fm.indexOf('(');
			if (0 > lparen) continue;
			
			//Color (2o) => frame element name
			String felt = fm.substring(0,lparen);

			fm = fm.substring(lparen+1);
			int rparen = fm.indexOf(')');
			if (0 > rparen) continue;

			//red,ball
			String cpt1 = fm.substring(0,rparen);
			String cpt2 = null;

			// Echo the parsed string 
			ret += "; " + frm + ":" + felt + "(" + cpt1 + ")\n";

			// Are there two concepts, or just one?
			int comma = cpt1.indexOf(',');
			if (0 < comma)
			{
				//frame element values
				cpt2 = cpt1.substring(comma+1);//ball
				cpt1 = cpt1.substring(0, comma);//red
			}
			if (cpt1 == null) continue;

			// Is cpt1 a "DefinedLinguisticConceptNode"?
			Boolean cpt1_is_ling = false;
			Boolean cpt2_is_ling = false;
			if (cpt1.charAt(0) == '#') cpt1_is_ling = true;

			if (cpt2 != null)
			{
				if (cpt2.charAt(0) == '#') cpt2_is_ling = true;
			}

			// Are cpt1 and 2 true WordInstances, or concepts?
			Boolean cpt1_is_word = false;
			Boolean cpt2_is_word = false;
			if (-1 < cpt1.indexOf('@')) cpt1_is_word = true;
			if ((cpt2 != null) && (-1 < cpt2.indexOf('@'))) cpt2_is_word = true;

			// Are cpt1 and 2 variables?
			Boolean cpt1_is_var = false;
			Boolean cpt2_is_var = false;
			if (-1 < cpt1.indexOf("$qVar")) cpt1_is_var = true;
			if ((cpt2 != null) && (-1 < cpt2.indexOf("$qVar"))) cpt2_is_var = true;



			/*** Fabricio: change the frames output ***********/
			//Add the FrameItem to the corresponding frame
			if(frames.get(frm) == null){
				frames.put(frm, new ArrayList<FrameItem>());
			}
			
			FrameItem frameItem = new FrameItem();

			frameItem.element1 = cpt1;
			frameItem.predicateName = cpt1+"_"+frm;
			if(cpt2 != null){
				//frameItem.predicateName = cpt1 +"_"+ cpt2;
				frameItem.element2 = cpt2;
			}
			
			int indexOfFrameItem = frames.get(frm).indexOf(frameItem);
			if(indexOfFrameItem < 0){
				frames.get(frm).add(frameItem);
				indexOfFrameItem = frames.get(frm).indexOf(frameItem);
			}
			
			FrameElementItem frameElementItem = new FrameElementItem();
			frameElementItem.elementName = felt;
			
			if(cpt2 != null){
				frameElementItem.wordInstanceValueElement = cpt2;
				if(cpt2_is_ling) frameElementItem.isLinguistic = true;
				if(cpt2_is_word) frameElementItem.isWord = true;
				if(cpt2_is_var) frameElementItem.isVar = true;
			} else {
				frameElementItem.wordInstanceValueElement = cpt1;
				if(cpt1_is_ling) frameElementItem.isLinguistic = true;
				if(cpt1_is_word) frameElementItem.isWord = true;
				if(cpt1_is_var) frameElementItem.isVar = true;
			}
			
			frames.get(frm).get(indexOfFrameItem).elements.add(frameElementItem);

			if(onlyNewFormat)
				continue;
			
			/***************************************************/
			
			// Link together.
			ret += "(FrameElementLink (stv 1 1)\n" +
			       "   (DefinedFrameNode \"#" + frm + "\")\n" +
			       "   (DefinedFrameElementNode \"#" +
			       frm + ":" + felt + "\")\n)\n";

			// Now, for the specific mappings
			ret += "(InheritanceLink ";

			// Assign a nominal truth value
			if (cpt1_is_word) ret += "(stv 1 0.2)\n";
			else ret += "(stv 1 1)\n";

			if (cpt1_is_ling)
			{
				ret += "   (DefinedLinguisticConceptNode \"#" + 
				            cpt1 + "\")\n";
			}
			else if (cpt1_is_word)
			{
				ret += "   (WordInstanceNode \"" + cpt1 + "\")\n";
			}
			else
			{
				ret += "   (VariableNode \"#" + cpt1 + "\")\n";
			}
			ret += "   (DefinedFrameNode \"#" + frm + "\")\n)\n";

			// If no second concept, then we are done.
			if (cpt2 == null) continue;

			// Finally link the frame element
			// Assign some bogus place-holder truth value.
			ret += "(EvaluationLink (stv 1 0.2)\n";
			ret += "   (DefinedFrameElementNode \"#" +
			       frm + ":" + felt + "\")\n";

			// Embedded: Link first and second concepts together.
			ret += "   (ListLink\n";
			if (cpt1_is_ling)
			{
				ret += "      (DefinedLinguisticConceptNode \"#" + 
				       cpt1 + "\")\n";
			}
			else if (cpt1_is_word)
			{
				ret += "      (WordInstanceNode \"" + cpt1 + "\")\n";
			}
			else
			{
				ret += "      (VariableNode \"#" + cpt1 + "\")\n";
			}
			if (cpt2_is_ling)
			{
				ret += "      (DefinedLinguisticConceptNode \"#" + 
				            cpt2 + "\")\n";
			}
			else if (cpt2_is_word)
			{
				ret += "      (WordInstanceNode \"" + cpt2 + "\")\n";
			}
			else
			{
				ret += "      (VariableNode \"#" + cpt2 + "\")\n";
			}
			ret += "   )\n)\n";
		}
		
		
		/******* Fabricio: new frame format output *****************/
		ret += "; New Frame Format Output\n\n";
		for (String frameName : frames.keySet())
		{
			ArrayList<FrameItem> frameItemList = frames.get(frameName);
			boolean foundGround = false;
			for (FrameItem frameItem : frameItemList)
			{
				for (FrameElementItem element : frameItem.elements)
				{
					if (frameName.equals("Locative_relation") &&
					   element.elementName.equals("Ground"))
					{
						if (foundGround)
						{
							element.elementName = "Ground_2";
						}
						foundGround = true;
					}

					String frameElementPredicateName = frameItem.predicateName + 
					     "_" + element.elementName;
					
					ret += "(InheritanceLink (stv 1 1)\n" +
					"   (PredicateNode \"" + frameElementPredicateName + "\")\n" +
					"   (DefinedFrameElementNode \"#" + frameName + 
					":" + element.elementName +
					"\")\n)\n";
					
					ret += "(FrameElementLink (stv 1 1)\n" +
					"   (PredicateNode \"" + frameItem.predicateName +"\")\n" +
					"   (PredicateNode \"" + frameElementPredicateName +
					"\")\n)\n";
					
					ret += "(EvaluationLink (stv 1 1)\n" +
					"   (PredicateNode \"" + frameElementPredicateName +"\")\n";
					if (element.isLinguistic)
					{
						ret += "   (DefinedLinguisticConceptNode \"#" +  element.wordInstanceValueElement;
					}
					else if (element.isWord)
					{
						ret += "   (WordInstanceNode \"" + element.wordInstanceValueElement;
					}
					else if (element.isVar)
					{
						ret += "   (VariableNode \"" + 
						    element.wordInstanceValueElement.replace("_","");
					}
					else
					{
						ret += "   (ConceptNode \"#" + element.wordInstanceValueElement;
					}					
					//"   (WordInstanceNode \"" + element.wordInstanceValueElement +
					ret += "\")\n)\n";					

				}
				
				ret += "(InheritanceLink (stv 1 1)\n" +
				       "   (PredicateNode \"" + frameItem.predicateName + "\")\n" +
				       "   (DefinedFrameNode \"#" + frameName + 
				       "\")\n)\n";
				
				
			}
		}
		ret += "; END of New Frame Format Output\n\n";
		
		/***********************************************************/
		
		
		return ret;
	}

	/* ------------------------------------------------------------- */
	public String toString()
	{
		return printFrames();
	}
}

class FrameElementItem{
	String elementName, wordInstanceValueElement;
	boolean isLinguistic=false, isWord=false, isVar=false;
}

class FrameItem {
	String predicateName;
	String element1, element2;
	List<FrameElementItem> elements;
	
	FrameItem(){
		elements = new ArrayList<FrameElementItem>();
	}	
	
	public int hashCode(){
		int hash = 7;
		hash = 31 * hash + (null == element1 ? 0 : element1.hashCode());
		//hash = 31 * hash + (null == element2 ? 0 : element2.hashCode());
		return  hash;
	}
	
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		FrameItem item = (FrameItem)obj;
		return (item.hashCode() == this.hashCode());

	}
}

/* ============================ END OF FILE ====================== */
