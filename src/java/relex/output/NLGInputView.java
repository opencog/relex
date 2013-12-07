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
import java.util.ArrayList;
import java.util.Arrays;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.ParsedSentence;

/**
 * Implements a very simple, direct printout of the
 * RelEx feature graph readable for natural language generation.
 *
 * Copyright (c) 2009 Blake Lemoine <bal2277@louisiana.edu>
 */
public class NLGInputView
{
	/**
	 * Print out RelEx relations. All relations shown
	 * in a binary form.  This view is based on the SimpleView.
	 * Instead of using the words of the sentence as variables
	 * in the relations, numbered indices are used.  Also,
	 * a binary relation associating indices with lemmas is
	 * added.
	 *
	 * Example:
	 *   _subj(1, 2)
	 *   _obj(1, 3)
	 *   tense(1, past)
	 *   DEFINITE-FLAG(3, T)
	 *   noun_number(3, singular)
	 *   lemma(1, throw)
	 *   lemma(2, John)
         *   lemma(3, ball)
	 */
	public static String printRelations(ParsedSentence parse)
	{
		return printRelations(parse, null);
	}
	public static String printRelations(ParsedSentence parse,
	                                    HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.str = "";
		parse.foreach(v);
		return v.str;
	}

	/**
	 * Print out RelEx relations, alternate format.
	 * Unary relations, including booleans, doen't show
	 * the attribute name.
	 *
	 * Example:
	 *   _subj(throw, John)
	 *   _obj(throw, ball)
	 *   past(throw)
	 *   definite(ball)
	 *   singular(ball)
	 */
	public static String printRelationsAlt(ParsedSentence parse)
	{
		return printRelationsAlt(parse, null);
	}
	public static String printRelationsAlt(ParsedSentence parse,
	                                    HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.unaryStyle = true;
		v.str = "";
		parse.foreach(v);
		return v.str;
	}

	private static class Visit implements RelationCallback
	{
		// Map associating a feature-node to a unique ID string.
		public HashMap<FeatureNode,String> id_map = null;

		/* The entity_markers ArrayList contains the names of the binary relations
		 * that may have entities as their arguments.  t is the case that unary
		 * relations may have entities as their argument, but in all such cases
		 * a binary relation containing that entity will appear before it that
		 * contains the same entity.  If one of these relations is present then
		 * the part of speech of its arguments are checked.  If an argument is either
		 * a verb or a noun and is not yet identified as an entity then it is added
		 * to the entities list and a lemma for it is appended to the output string.
		 * Once an entity is detected any argument of a relation that involves that
		 * entity will be its index rather than its name.
		 */
		public static ArrayList<String> binary_entity_markers = new ArrayList<String>(Arrays.asList(new String[]{"_subj","_obj","_appo","_iobj","_nn","_poss","_to-be","_to-do","under","beneath","below","above","behind.p","within","during","from","at","toward","towards","without","upon","except","but.misc-ex","against","beyond","beside","between","with","among","for.p","into","about","through.r","off","across","along","past.p","around","out","up","down.r","by","in","on","over","just_over","just_under","well_over","like.p","unlike","of","of_them","to","so_as_to","besides","throughout","inside.r","outside.r","underneath","alongside","amid","plus.p","minus.p","via","onto","versus","vs","worth.p","opposite.p","better_off","worse_off","off_of","out_of","despite","notwithstanding","other_than","apart_from","aside_from","rather_than","instead_of","because_of","prior_to","as_well_as","according_to","as_of","in_case_of","in_response_to","unbeknownst_to","thanks_to","due_to","along_with","en_route_to","in_connection_with","regardless_of","as_to","irrespective_of","overhead.r","midway","in_public","in_private","en_route","a_la_mode","a_la_carte","side_by_side","from_coast_to_coast","abroad","upstairs.r","downstairs.r","overseas.r","next_door","elsewhere","ahead","at_hand","in_store","in_reverse","in_place","in_town","under_way","in_office","out_of_office","out_of_reach","in_reach","within_reach","on_guard","at_large","in_hand","on_hand","for_free","on_file","in_line","on_line","in_loco_parentis","on_board","en_route","in_bed","out_of_bed","on_strike","on_top","from_afar","at_stake","in_question","at_issue","on_lease","on_trial","in_league","in_cahoots","in_front","in_back","on_break","on_camera","in_command","in_concert","by_association","in_association","on_deck","on_disk","on_file","on_foot","on_location","on_line","online.r","uptown","downtown.r","underground.r","out_of_town","forward.r","backward","forwards.r","backwards","sideways","ashore","abreast","aft","half-way","two-fold","downhill","southward","underfoot","westward","eastward","northward","overnight.r","on_hold","on_track","in_situ","in_toto","off_balance","in_check","on_course","off_course","under_oath","at_end","by_example","on_holiday","by_invitation","on_patrol","on_stage","in_step","in_tempo","on_schedule","behind_schedule","ahead_of_schedule","for_good","for_keeps","in_phase","out_of_step","out_of_phase","in_tune","out_of_tune","in_session","out_of_session","in_phase","neck_and_neck","under_contract","indoors","outdoors","upstream","downstream","underwater.r","everywhere","anywhere","somewhere","someplace","nowhere","as_usual","to_date","on_average","in_turn","so_far","in_particular","in_response","in_general","thus_far","in_reply","recently","now.r","then.r","later","earlier","away","aboard","apart","home.i","back.k","forth","aside.p","nearby","next_to","in_back_of","in_front_of","close_to","on_top_of","outside_of","inside_of","atop","ahead_of","by_way_of","akin_to","betwixt","vis-a-vis","in_lieu_of","on_account_of","in_place_of","in_search_of","near.p","all_over","all_around","per","such_as","here","there","de","de_la","du","des","del","von","who","what","which","whom","whose","whoever","whatever","whenever","wherever","however.c","no_matter","that.misc-c","that.misc-d","that.misc-r","that.misc-p","because","now_that","just_as","if_only","in_case","whereby","whereupon","insofar_as","inasmuch_as","ere","on_the_grounds_that","on_grounds_that","in_that","in_the_event_that","in_the_event","on_condition","unless","though.c","even_though","as_if","as_though","as_soon_as","until","since","ever_since","after","before","if","if_possible","if_necessary","if_so","if_only","no_wonder","although","while","once","or","but.misc-cnj","and","either","neither","nor","for.c","yet.c","thus","therefore","when","why","why_not","where","whether","whether_or_not","how","not","n't","n’t"}));
		public ArrayList<FeatureNode> entities = new ArrayList<FeatureNode>();

		public Boolean unaryStyle = false;
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }
		public Boolean BinaryRelationCB(String relName,
		                                FeatureNode srcNode,
		                                FeatureNode tgtNode)
		{
			if (binary_entity_markers.contains(relName) && ((!relName.equals(srcNode.get("name").getValue())) && (!relName.equals(tgtNode.get("name").getValue())))){
				if (!entities.contains(srcNode) && (srcNode.get("pos").getValue().equals("noun") || srcNode.get("pos").getValue().equals("verb"))){
					entities.add(srcNode);
					str += "lemma(" + entities.size() + ", " + srcNode.get("name").getValue() + ")\n";
				}
				if (!entities.contains(tgtNode) && (tgtNode.get("pos").getValue().equals("noun") || tgtNode.get("pos").getValue().equals("verb"))){
					entities.add(tgtNode);
					str += "lemma(" + entities.size() + ", " + tgtNode.get("name").getValue() + ")\n";
				}
			}
			String srcName;
			if (entities.contains(srcNode)){
				srcName = "" + (entities.indexOf(srcNode) + 1);
			}
			else{
				srcName = srcNode.get("name").getValue();
			}
			FeatureNode tgt = tgtNode.get("name");
			if (tgt == null)
			{
				System.out.println("Error: No target! rel=" + relName +
				                   " and src=" + srcName);
				return false;
			}
			String tgtName;
			if (entities.contains(tgtNode)){
				tgtName = "" + (entities.indexOf(tgtNode) + 1);
			}
			else{
				tgtName = tgt.getValue();
			}

			if (id_map != null)
			{
				if (entities.contains(srcNode)){
					srcName = "" + (entities.indexOf(srcNode) + 1);
				}
				else{
					srcName = id_map.get(srcNode);
				}
				if (entities.contains(tgtNode)){
					tgtName = "" + (entities.indexOf(tgtNode) + 1);
				}
				else{
					tgtName = id_map.get(tgtNode);
				}
			}
			str += relName + "(" + srcName + ", " + tgtName + ")\n";

			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();
			if ((attrName.equals("subscript-TAG")) && (value.equals(".n")) && (!entities.contains(srcNode)))
			{
				entities.add(srcNode);
				str += "lemma(" + entities.size() + ", " + srcNode.get("name").getValue() + ")\n";
			}
			if ((attrName.equals("DEFINITE-FLAG")) && (value.equals("T")) && (!entities.contains(srcNode)))
			{
				entities.add(srcNode);
				str += "lemma(" + entities.size() + ", " + srcNode.get("name").getValue() + ")\n";
			}
			String srcName;
			if (entities.contains(srcNode)){
				srcName = "" + (entities.indexOf(srcNode) + 1);
			}
			else{
				srcName = srcNode.get("name").getValue();
			}

			if (id_map != null)
			{
				if (entities.contains(srcNode)){
					srcName = "" + (entities.indexOf(srcNode) + 1);
				}
				else{
					srcName = id_map.get(srcNode);
				}
			}
			if (unaryStyle)
			{
				if (attrName.endsWith("-FLAG"))
					value = attrName.replaceAll("-FLAG","").toLowerCase();

				if (attrName.equals("HYP"))
					value = attrName.toLowerCase();

				str += value + "(" + srcName + ")\n";
			}
			else
			{
				str += attrName + "(" + srcName + ", " + value + ")\n";
			}

			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
