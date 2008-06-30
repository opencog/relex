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
package relex.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import relex.feature.FeatureNode;
import relex.feature.LinkableView;
import relex.feature.SemanticView;
import relex.output.RawView;

public class EntityMaintainer implements Serializable 
{
	private static final long serialVersionUID = -8186219027158709715L;

	/**
	 * This number is derived from the LinkParser dictionary -- 
	 * it only accepts suffixes up to "60." To increase this 
	 * number, you need to add more words to the Link Parser 
	 * dictionary, like "genericID61", "dateID61", etc.
	 */
	public static int MAX_NUM_ENTITIES = 60;

	// The original sentence string
	private String originalSentence;

	public String getOriginalSentence()
	{
		return originalSentence;
	}

	// The converted sentence is a copy of the original, 
	// except all entities are replaced with ID strings
	private String convertedSentence;

	public String getConvertedSentence()
	{
		return convertedSentence;
	}

	// An array of EntityInfos, ordered by their order in the sentence
	public ArrayList<EntityInfo> orderedEntityInfos;

	// Maps entity ID strings to EntityInfos
	private HashMap<String,EntityInfo> iDs2Entities;

	// Keeps track of the last entityIDIndex created
	int entityIDIndex;

	// Maps feature nodes to entity IDs
	// private HashMap<FeatureNode, String> featureNodes2EntityIDs;

	// A set of integer indexes of inserted whitespace characters
	private TreeSet<Integer> insertedWhitespaceCharIndexes;

	static ArrayList<String> emolist = new ArrayList<String>();

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
		emolist.add("T.T");
		emolist.add("`:-)");
		emolist.add(":P");
		emolist.add("O:-)");
		emolist.add("><");
		emolist.add(">_<");
		emolist.add("<_<");
		emolist.add(">_>");
		emolist.add("Oo");
		emolist.add(">:D");
		emolist.add("e.e");
		emolist.add("-.-*");
		emolist.add("~.^");
		emolist.add("(-_-)");
		emolist.add("(-.-)");
		emolist.add("-.-'");
		emolist.add("E.E");
		emolist.add("-.O");
		emolist.add("*o*");
		emolist.add("=^.^=");
		emolist.add("8)");
		emolist.add("8D");
		emolist.add(">O");
		emolist.add("(:-D");
		emolist.add("c^:3");
		emolist.add("~:>");
		emolist.add("x-(");
		emolist.add(";:^)B>");
		emolist.add("O.O");
		emolist.add("o.o");
		emolist.add("O.o");
		emolist.add("o.O");
		emolist.add("8|");
		emolist.add(">8V-()<");
		emolist.add("=3");
		emolist.add("-:3");
		emolist.add("<3");
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

	/**
	 * Creates the converted sentence. The converted sentence 
	 * is a copy of the original, except all entities are 
	 * replaced with ID strings.
	 */
	private void createConvertedSentence()
	{
		if ((orderedEntityInfos == null) || (orderedEntityInfos.size() == 0))
			convertedSentence = originalSentence;
		convertedSentence = "";
		int curIndex = 0;

		// For each entity...
		for(EntityInfo eInfo : orderedEntityInfos)
		{
			if (eInfo.getFirstCharIndex() < curIndex)
			{
				if (eInfo.getFirstCharIndex() == curIndex - 1)
				{
					curIndex = eInfo.getFirstCharIndex();
				}
				else
				{
					System.err.println(
						"Error: Entity start is at an unexpected location:\n" +
						"Sentence= " + originalSentence + "\n" +
						"firt char= " + eInfo.getFirstCharIndex() +
						" curidx= " + curIndex + "\n");
					continue;
				}
			}

			// Copy the preceeding portion (if any) of the original string.
			convertedSentence += originalSentence.substring(curIndex, eInfo
					.getFirstCharIndex());

			// Insert leading white space before eInfo if it is 
			// not preceeded by white space.
			if (convertedSentence.length() > 0
					&& !Character.isWhitespace(convertedSentence
							.charAt(convertedSentence.length() - 1)))
			{
				convertedSentence += ' ';
				insertedWhitespaceCharIndexes.add(
					new Integer(convertedSentence.length() - 1));
			}

			// If the entity is at the end of the sentence, then the 
			// entity detector may swallow the period at the end 
			// of the sentence. This can confuse downstream handlers,
			// so remove the period from the entity, and put it back 
			// at the end of the sentence.
			if ((originalSentence.length() == curIndex) &&
			    (originalSentence.charAt(eInfo.getLastCharIndex()) == '.'))
			{
				eInfo.setLastCharIndex(eInfo.getLastCharIndex()-1);
			}

			// Insert the ID string of the entity.
			curIndex = eInfo.getLastCharIndex();
			convertedSentence += makeID(eInfo);

			// Insert trailing white space after eInfo if it is 
			// not followed by a legal string
			if (originalSentence.length() > curIndex
					&& !isLegalEntityFollowingString(originalSentence
							.substring(curIndex)))
			{
				convertedSentence += ' ';
				insertedWhitespaceCharIndexes.add(
					new Integer(convertedSentence.length() - 1));
			}
		}

		// Copy the remaining portion (if any) of the original string.
		if (curIndex < originalSentence.length())
			convertedSentence += originalSentence.substring(curIndex);
		// System.out.println("CONVERSION\n" + originalSentence + "\n" +
		// convertedSentence);
	}

	private String makeID(EntityInfo eInfo)
	{
		++entityIDIndex;
		String id = eInfo.idStringPrefix() + entityIDIndex;
		iDs2Entities.put(id, eInfo);
		return id;
	}

	// --------------------------------------------------------
	/**
	 * Strip out emoticons, smileys :-)
	 */
	private void identifyEmoticons()
	{
		for(String emo : emolist)
		{
			int start = originalSentence.indexOf(emo);
			if (start < 0) continue;
			int end = start + emo.length();
	
			EntityInfo ei = new EntityInfo(originalSentence, start, end, EntityType.EMOTICON);
			addEntity(ei);
		}
	}

	/**
	 * Escape parenthesis, treating them as entities.
	 * This is needed for one reason only: the phrase markup
	 * uses a LISP-like structure for the Penn-treebank markup,
	 * and stray parens in the original sentence mess it up.
	 */
	private void escapeParens()
	{
		int start = 0;
		while (true)
		{
			start = originalSentence.indexOf('(', start);
			if (start < 0) break;

			EntityInfo ei = new EntityInfo(originalSentence, start, start+1, EntityType.PUNCTUATION);
			addEntity(ei);
			start++;
		}
		while (true)
		{
			start = originalSentence.indexOf(')', start);
			if (start < 0) break;

			EntityInfo ei = new EntityInfo(originalSentence, start, start+1, EntityType.PUNCTUATION);
			addEntity(ei);
			start++;
		}
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
	/*
	 * CONSTRUCTOR
	 */
	public EntityMaintainer(String _originalSentence, Collection<EntityInfo> eis)
	{
		if (eis.size() > MAX_NUM_ENTITIES)
		{
			System.err.println("WARNING: Sentence had more than "
					+ MAX_NUM_ENTITIES
					+ ".  Ignoring extras.\nOriginal sentence:"
					+ originalSentence);
		}
		originalSentence = _originalSentence;
		orderedEntityInfos = new ArrayList<EntityInfo>();

		for (EntityInfo it : eis)
		{
			addEntity(it);
		}

		// Strip out emoticons, which GATE doesn't do.
		// Emoticons confuse the parser.
		identifyEmoticons();

		// Escape parenthesis. These confuse the phrase-tree markup.
		escapeParens();

		iDs2Entities = new HashMap<String, EntityInfo>();
		entityIDIndex = 0; // the first used index will be '1'

		insertedWhitespaceCharIndexes = new TreeSet<Integer>();
		createConvertedSentence();
	}

	// --------------------------------------------------------

	public boolean isEntityID(String e) {
		return iDs2Entities.containsKey(e);
	}

	public EntityInfo getEntityInfo(String entityID) {
		return iDs2Entities.get(entityID);
	}

	private int previouslyInsertedWhitespace(int index)
	{
		int insertedSoFar = 0;
		Iterator<Integer> inserts = insertedWhitespaceCharIndexes.iterator();
		while (inserts.hasNext()) {
			if (inserts.next().intValue() < index)
				++insertedSoFar;
		}
		return insertedSoFar;
	}

	/**
	 * Replaces all feature values containing entityIDs, so that they now
	 * contain original strings. Stores the FeatureNodes in a map which maps
	 * them to entityIDs. Does not repair char index values in the word nodes
	 */
	public void repairSentence(FeatureNode leftNode)
	{
		int charDelta = 0;
		try
		{
			for (LinkableView word = new LinkableView(leftNode);
			     word != null;
			     word = (word.getNext() == null ?
			                  null : new LinkableView(word.getNext())))
			{
				int previousWhiteSpace = 
				      previouslyInsertedWhitespace(word.getStartChar());

				String wordName = word.getWordString();

				word.setStartChar(word.getStartChar() + charDelta
						- previousWhiteSpace);

				// word.setExpandedStartChar(word.getExpandedStartChar()+charDelta);

				if (isEntityID(wordName))
				{
					EntityInfo entInfo = getEntityInfo(wordName);
					String origName = entInfo.getOriginalString();
					charDelta += origName.length() - wordName.length();
					try
					{
						SemanticView semView = new SemanticView(word.fn()
								.get("ref"));
						semView.setName(origName);// .replaceAll("\\s","_"))
					}
					catch (Exception e) {}
				}

				word.setEndChar(word.getEndChar() + charDelta - previousWhiteSpace);
				// word.setExpandedEndChar(word.getExpandedEndChar()+charDelta);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: failed to repair sentence.");
			System.err.println("Broken sentence was: "+getOriginalSentence());
			System.err.println(RawView.printZHeads(leftNode));
			e.printStackTrace();
		}
	}

	/**
	 * Return all EntityInfo's ordered by their starting character
	 * position.
	 */
	public List<EntityInfo> getEntities()
	{
		return orderedEntityInfos;
	}
	
	/**
	 * prepareSentence() -- markup parsed sentence with entity 
	 * information. This needs to be done before the relex algs run,
	 * as the relex algs may use some of this information.
	 */
	public void prepareSentence(FeatureNode leftNode)
	{
		for (LinkableView word = new LinkableView(leftNode); 
		     word != null; 
		     word = (word.getNext() == null ? 
		                null : new LinkableView(word.getNext())))
		{
			String wordName = word.getWordString();
			if (isEntityID(wordName))
			{
				EntityInfo entInfo = getEntityInfo(wordName);
				entInfo.setProperties(word.fn());
			}
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (EntityInfo info: orderedEntityInfos)
		{
			String name = info.getOriginalSentence().substring(
					info.getFirstCharIndex(), 
					info.getLastCharIndex());
			sb.append(makeID(info)).append(": ").append(name).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Arg0: a sentence with an entity Arg1: the first character of the
	 * entity Arg2: the last character of the entity Example:
	 * java relex.entity.EntityMaintainer "Does Mike think it will work?" 5 8
	 * Expected output: Does genericID1 think it will work?
	 */
	public static void main(String[] args)
	{
		String sentence = args[0];
		ArrayList<EntityInfo> list = new ArrayList<EntityInfo>();
		int arg = 1;
		while (args.length >= arg + 2)
		{
			EntityInfo eInfo = new EntityInfo(sentence,
								   Integer.parseInt(args[arg]),
								   Integer.parseInt(args[arg + 1]));
			list.add(eInfo);
			arg += 2;
		}
		EntityMaintainer em = new EntityMaintainer(sentence, list);
		System.out.println(em.getConvertedSentence());
	}
}
