/*
 * Copyright 2008 Novamente LLC
 * Copyright 2009 Linas Vepstas
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
package relex.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.feature.SemanticView;
import relex.output.RawView;

public class EntityTagger implements Serializable
{
	private static final long serialVersionUID = -8186219027158709714L;

	// An array of EntityInfos, ordered by their order in the sentence
	private List<EntityInfo> orderedEntityInfos;

	// Maps feature nodes to entity IDs
	// private HashMap<FeatureNode, String> featureNodes2EntityIDs;

	static List<String> emolist = new ArrayList<String>();

	static
	{
		// Emoticons -- smiley faces, right :-)
		// Partial list, only of the basics, taken from wikipedia
		// This could be improved on by automatically generating
		// these with and without noses, etc.
		//
		// More generally, we should have a "bogus punctutation entity"
		// for any sort of markup that is not recognized here.
		//
		// Some of these have leading or trailing whitespace ...
		// these are typically ones that have numbers, that's so that
		// number expressions don't get mangled; others which might get
		// mistaken for initials.
		//
		emolist.add(":-)");
		emolist.add(":-(");
		emolist.add(":)");
		emolist.add(":(");
		emolist.add(":'-)");
		emolist.add(":')");
		emolist.add(":D");
		emolist.add(":-D");
		emolist.add(":-O");
		emolist.add(":-S");
		emolist.add(":-$");
		emolist.add(":-*");
		emolist.add(":[");
		emolist.add(":'[");
		emolist.add(":'\\");
		emolist.add(":-B");
		emolist.add(":-#");
		emolist.add(":-|");
		emolist.add(":-&");
		emolist.add(":-X");
		emolist.add(":-K");
		emolist.add(":]");
		emolist.add(":-@");
		emolist.add(":@");
		emolist.add(":O]");
		emolist.add(":d");
		emolist.add("|-O");
		emolist.add("%-(");
		emolist.add("=)");
		emolist.add("=O");
		emolist.add(";)");
		emolist.add(";-)");
		emolist.add(";]");
		emolist.add(";O]");
		emolist.add(";O");
		emolist.add(";D");
		emolist.add("B-)");
		emolist.add(" T.T "); // XXX Danger, may be initials
		emolist.add("`:-)");
		emolist.add(":P");
		emolist.add("O:-)");
		emolist.add("><");
		emolist.add(">_<");
		emolist.add("<_<");
		emolist.add(">_>");
		emolist.add(" Oo ");
		emolist.add(">:D");
		emolist.add(" e.e "); // XXX Danger, may be initials
		emolist.add("-.-*");
		emolist.add("~.^");
		emolist.add("(-_-)");
		emolist.add("(-.-)");
		emolist.add("-.-'");
		emolist.add(" E.E "); // XXX Danger, may be initials
		emolist.add("-.O");
		emolist.add("*o*");
		emolist.add("=^.^=");
		emolist.add(" 8)");  // XXX Danger! may be legit non-smiley
		emolist.add(" 8D "); // XXX Danger! may be legit non-smiley
		emolist.add(">O");
		emolist.add("(:-D");
		emolist.add("c^:3");
		emolist.add("~:>");
		emolist.add("x-(");
		emolist.add(";:^)B>");
		emolist.add(" O.O "); // XXX Danger, may be initials
		emolist.add(" o.o ");
		emolist.add(" O.o ");
		emolist.add(" o.O ");
		emolist.add(" 8| ");   // XXX Daner, may be numerical expr.
		emolist.add(">8V-()<");
		emolist.add(" =3 ");  // XXX Danger, may be part of formula
		emolist.add("-:3");
		emolist.add(" <3 ");  // XXX Danger, may be part of formula
		emolist.add("<><");
		emolist.add("<@:)");
		emolist.add(":3=");
	}

	// --------------------------------------------------------
	/**
	 * Returns true iff the character after the entity is legal.
	 * Only whitespace and certain punction are allowed.
	 */
	private boolean isLegalEntityFollowingString(String s)
	{
		if (s.length() == 0)
			return true;
		char c = s.charAt(0);
		if (Character.isWhitespace(c))
			return true;
		if (c == '.' || c == ',' || c == ';' || c == ':')
			return true;
		if (s.startsWith("'s ") || s.startsWith("' "))
			return true;
		return false;
	}

	// --------------------------------------------------------
	/**
	 * Strip out emoticons, smileys :-)
	 */
	private void identifyEmoticons(String sentence)
	{
		for(String emo : emolist)
		{
			int start = sentence.indexOf(emo);
			while (0 <= start)
			{
				int end = start + emo.length();

				EntityInfo ei = new EntityInfo(sentence, start, end, EntityType.EMOTICON);
				addEntity(ei);
				start = sentence.indexOf(emo, end);
			}
		}
	}

	/**
	 * Escape parenthesis, treating them as entities.
	 * This is needed for one reason only: the phrase markup
	 * uses a LISP-like structure for the Penn-treebank markup,
	 * and stray parens in the original sentence mess it up.
	 */
	private void escapePunct(String sentence, char punct)
	{
		int start = 0;
		while (true)
		{
			start = sentence.indexOf(punct, start);
			if (start < 0) break;

			EntityInfo ei = new EntityInfo(sentence, start, start+1, EntityType.PUNCTUATION);
			addEntity(ei);
			start++;
		}
	}

	private void escapeParens(String sentence)
	{
		escapePunct(sentence, '(');
		escapePunct(sentence, ')');
		escapePunct(sentence, '[');
		escapePunct(sentence, ']');
	}

	public List<EntityInfo> tagEntities(String sentence)
	{
		escapeParens(sentence);
		identifyEmoticons(sentence);
		return orderedEntityInfos;
	}

	// --------------------------------------------------------

	/**
	 * Add the entity info to the list, inserting it in sorted order.
	 */
	public void addEntity(EntityInfo ei)
	{
		int open = 0;
		int start = ei.getFirstCharIndex();
		int end = ei.getLastCharIndex();
		for (EntityInfo e: orderedEntityInfos)
		{
			int beg = e.getFirstCharIndex();
			if ((open <= start) && (end <= beg))
			{
				int idx = orderedEntityInfos.indexOf(e);
				orderedEntityInfos.add(idx, ei);
				return;
			}
			open = e.getLastCharIndex();

			// If our entity overlaps with existing entities, ignore it.
			if (start < open) return;
		}
		orderedEntityInfos.add(ei);
	}

	// --------------------------------------------------------

	/**
	 * Default constructor is mainly used for de-serialization purposes.
	 */
	public EntityTagger()
	{
		orderedEntityInfos = new ArrayList<EntityInfo>();
	}

	// --------------------------------------------------------

	/**
	 * Return all EntityInfo's ordered by their starting character
	 * position.
	 */
	public List<EntityInfo> getEntities()
	{
		return orderedEntityInfos;
	}

	/**************
	 * WTF   XXX this is just plain wrong ... no one should be calling this ... 
	public void setEntities(List<EntityInfo> orderedEntityInfos)
	{
		this.orderedEntityInfos = orderedEntityInfos;
	}
	**************/

	public boolean equals(Object other)
	{
		if (! (other instanceof EntityTagger)) return false;
		EntityTagger et = (EntityTagger)other;
		return this.orderedEntityInfos.equals(et.orderedEntityInfos);
	}
}
