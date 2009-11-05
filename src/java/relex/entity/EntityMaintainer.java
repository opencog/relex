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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import relex.ParsedSentence;
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

	// The list of entities
	private EntityTagger tagger;

	// Maps entity ID strings to EntityInfos
	private Map<String,EntityInfo> iDs2Entities;

	// The original sentence string
	private String originalSentence;

	// The converted sentence is a copy of the original, 
	// except all entities are replaced with ID strings
	private String convertedSentence;

    // Keeps track of the last entityIDIndex created
    int entityIDIndex;

    // Maps feature nodes to entity IDs
    // private HashMap<FeatureNode, String> featureNodes2EntityIDs;

    // A set of integer indexes of inserted whitespace characters
    private Set<Integer> insertedWhitespaceCharIndexes;


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
		List<EntityInfo> orderedEntityInfos = tagger.tagEntities(originalSentence);
		if ((orderedEntityInfos == null) || (orderedEntityInfos.size() == 0))
		{
			convertedSentence = originalSentence;
			return;
		}
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

			curIndex = eInfo.getLastCharIndex();

			// If the entity ends with a period, then sometimes the
			// entity detector doesn't include the period as part of 
			// the entity. Fix this up (as long as the period is not
			// at the enbd of the sentence).
			// For example: "The A.D.A. advises against this."
			if ((originalSentence.length() > curIndex) &&
			    (originalSentence.charAt(eInfo.getLastCharIndex()) == '.'))
			{
				eInfo.setLastCharIndex(eInfo.getLastCharIndex()+1);
				curIndex++;
			}

			// Insert the ID string of the entity.
			convertedSentence += makeID(eInfo);

			// If the entity is at the end of the sentence, then the 
			// entity detector may swallow the period at the end 
			// of the sentence. This can confuse downstream handlers,
			// so add a new period to end the sentence.
			// For example: "It is located in Wshington, D.C."
			if ((originalSentence.length() == curIndex) &&
			    (originalSentence.charAt(eInfo.getLastCharIndex()-1) == '.'))
			{
				convertedSentence += '.';
				insertedWhitespaceCharIndexes.add(
					new Integer(convertedSentence.length() - 1));
			}

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
	 * Add the entity info to the list, inserting it in sorted order.
	 */
	public void addEntity(EntityInfo ei)
	{
		tagger.addEntity(ei);
	}

	public void set(EntityTagger et)
	{
		tagger = et;
	}

	// --------------------------------------------------------

	/**
	 * Default constructor is mainly used for de-serialization purposes.
	 */
	public EntityMaintainer()
	{
		tagger = new EntityTaggerBasic();
	}
	
	public void convertSentence(String _originalSentence, Collection<EntityInfo> eis)
	{
		if (null != eis)
		{
			if (eis.size() > MAX_NUM_ENTITIES)
			{
				System.err.println("WARNING: Sentence had more than "
						+ MAX_NUM_ENTITIES
						+ ".  Ignoring extras.\nOriginal sentence:"
						+ originalSentence);
			}
			for (EntityInfo it : eis)
			{
				tagger.addEntity(it);
			}
		}

		originalSentence = _originalSentence;
		iDs2Entities = new HashMap<String, EntityInfo>();
		entityIDIndex = 0; // the first used index will be '1'

		insertedWhitespaceCharIndexes = new TreeSet<Integer>();
		createConvertedSentence();
	}

	// --------------------------------------------------------

	public boolean isEntityID(String e)
	{
		return iDs2Entities.containsKey(e);
	}

	public EntityInfo getEntityInfo(String entityID)
	{
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
			for (FeatureNode fn = leftNode; 
				fn != null; fn = LinkableView.getNext(fn))
			{
				int start = LinkableView.getStartChar(fn);
				int previousWhiteSpace = 
				      previouslyInsertedWhitespace(start);

				String wordName = LinkableView.getWordString(fn);

				LinkableView.setStartChar(fn, 
					start + charDelta - previousWhiteSpace);

				// LinkableView.setExpandedStartChar(fn, LinkableView.getExpandedStartChar(fn)+charDelta);

				if (isEntityID(wordName))
				{
					EntityInfo entInfo = getEntityInfo(wordName);
					String origName = entInfo.getOriginalString();
					charDelta += origName.length() - wordName.length();
					try
					{
						FeatureNode ref = fn.get("ref");
						SemanticView.setName(ref, origName);
					}
					catch (Exception e) {}
				}

				int end = LinkableView.getEndChar(fn);
				LinkableView.setEndChar(fn, end + charDelta - previousWhiteSpace);
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
	 * tagConvertedSentence() -- markup parsed sentence with entity 
	 * information. This needs to be done before the relex algs run,
	 * as the relex algs may use some of this information.
	 */
	public void tagConvertedSentence(ParsedSentence parse)
	{
		FeatureNode fn = parse.getLeft();
		while (fn != null)
		{
			String wordName = LinkableView.getWordString(fn);
			if (isEntityID(wordName))
			{
				EntityInfo entInfo = getEntityInfo(wordName);
				entInfo.setProperties(fn);
			}
			fn = LinkableView.getNext(fn);
		}
	}

    /**
     * Return all EntityInfo's ordered by their starting character
     * position.
     */
    public List<EntityInfo> getEntities()
    {
        return tagger.getEntities();
    }
    
    public String getConvertedSentence()
    {
        return convertedSentence;
    }

    public void setConvertedSentence(String convertedSentence)
    {
        this.convertedSentence = convertedSentence;
    }
    
    public String getOriginalSentence()
    {
        return originalSentence;
    }
	
    public void setOriginalSentence(String originalSentence)
    {
        this.originalSentence = originalSentence;
    }
        
	public Map<String, EntityInfo> getIDs2Entities()
    {
        return iDs2Entities;
    }

	/* XXX WTF? what is this for? This seems wrong to me ... */
	@Deprecated
    public void setIDs2Entities(Map<String, EntityInfo> ds2Entities)
    {
        iDs2Entities = ds2Entities;
    }

    public int getEntityIDIndex()
    {
        return entityIDIndex;
    }

    public void setEntityIDIndex(int entityIDIndex)
    {
        this.entityIDIndex = entityIDIndex;
    }

    public Set<Integer> getInsertedWhitespaceCharIndexes()
    {
        return insertedWhitespaceCharIndexes;
    }

    public void setInsertedWhitespaceCharIndexes(Set<Integer> insertedWhitespaceCharIndexes)
    {
        this.insertedWhitespaceCharIndexes = insertedWhitespaceCharIndexes;
    }

    public String toString()
	{
		List<EntityInfo> orderedEntityInfos = tagger.getEntities();
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

    public int hashCode()
    {
        return originalSentence == null ? 0 : originalSentence.hashCode();
    }
    
    public boolean equals(Object other)
    {
        if (! (other instanceof EntityMaintainer))
            return false;
        EntityMaintainer em = (EntityMaintainer)other;
        if (originalSentence == null)
            return em.originalSentence == null;
        else
            return this.originalSentence.equals(em.originalSentence) &&
                   this.tagger.equals(em.tagger); 
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
		EntityMaintainer em = new EntityMaintainer();
		em.convertSentence(sentence, list);
		System.out.println(em.getConvertedSentence());
	}
}
