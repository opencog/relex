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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import relex.feature.FeatureNode;

/**
 * This class is just a data-class to store information about an entity
 * that appears in a sentence.  The original sentence is assumed to be
 * a plain string. The index of the first and last position of the
 * entity-identifying substring are stored.  The class may be subclassed
 * to store info about particular entity types such as dates, times,
 * people, etc.
 *
 * The convention is that the lastCharIndex points at the character
 * just after the entity, so that 
 *    lastCharIndex = firstCharIndex + entity.length() 
 * holds true.
 *
 * Test sentences that need to be handled correctly:
 *    The National Audio-Visual Conservation Center is in Culpeper,
 *    Virginia, near Washington, D.C.
 *
 *    The A.D.A. advises against this.
 */

public class EntityInfo implements Serializable
{
	/**
	 * The name of the attribute that constitutes the string to be prepended
	 * to identifiers replacing entities in a sentence for parsing
	 * purposes. Relex predefined entities already have predefined prefixes
	 * which can be overriden by setting this attribute. Custom entity types
	 * can specify their own prefix or rely on the generic default which
	 * is simply "entityID".  
	 */
	public static final String ID_PREFIX = "ID_PREFIX";
	
	/**
	 * Default prefix for entities of all types.
	 */
	public static final String DEFAULT_PREFIX = "entityID";
	
	/**
	 * Name of gender attribute for entities of type person. 
	 */
	public static final String GENDER = "GENDER";
	
	private static final long serialVersionUID = 344692623956286950L;
	
	private static Map<String, String> defaultPrefixes = 
		new HashMap<String, String>();
	static
	{
		defaultPrefixes.put("GENERIC", DEFAULT_PREFIX);
		defaultPrefixes.put("DATE", "dateID");
		defaultPrefixes.put("EMOTICON", "emoticonID");
		defaultPrefixes.put("LOCATION", "locationID");
		defaultPrefixes.put("MONEY", "moneyID");
		defaultPrefixes.put("ORGANIZATION", "organizationID");
		defaultPrefixes.put("PERSON", "personID");
	}
	
	private String originalSentence;
	private int firstCharIndex;
	private int lastCharIndex;
	private String type = "ENTITY";
	private Map<String, Object> attributes = 
		Collections.synchronizedMap(new HashMap<String, Object>());	
	private ArrayList<String> nodeProperties = new ArrayList<String>(1);
	
	private String getPunctuationPrefix()
	{
		String id = null;
		if ('(' == originalSentence.charAt(firstCharIndex))
			id = "lparenID";
		else if (')' == originalSentence.charAt(firstCharIndex))
			id = "rparenID";
		else if ('[' == originalSentence.charAt(firstCharIndex))
			id = "lbracketID";
		else if (']' == originalSentence.charAt(firstCharIndex))
			id = "rbracketID";
		else
			id = "punctuationID";
		return id;
	}
	
	public String getOriginalSentence()
	{
		return originalSentence;
	}	

	public int getFirstCharIndex()
	{
		return firstCharIndex;
	}

	public int getLastCharIndex()
	{
		return lastCharIndex;
	}

	public void setLastCharIndex(int i)
	{
		lastCharIndex = i;
	}

	public String getOriginalString()
	{
		return originalSentence.substring(firstCharIndex, lastCharIndex);
	}

	public String idStringPrefix()
	{
		String prefix = (String)attributes.get(ID_PREFIX);
		if (prefix == null)
		{
			if (type.equals(EntityType.PUNCTUATION.name()))
			{
			 	prefix = getPunctuationPrefix();
			}
			else
			{
				prefix = defaultPrefixes.get(type);
			}
		}
		if (prefix == null)
		{
			prefix = DEFAULT_PREFIX;
		}
		return prefix;
	}
	
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Map<String, Object> getAttributes()
	{
		return attributes;
	}
	
	/**
	 * Set an attribute which will also be added as a FeatureNode
	 * property. Whenever, an attribute of the EntityInfo needs
	 * to be propagated to the FeatureNode structure, this method 
	 * should be used instead of getAttributes().put(name, value). 
	 * 
	 * @param name The name of the attribute.
	 * @param value
	 */
	public void setNodeProperty(String name, String value)
	{
		attributes.put(name, value);
		nodeProperties.add(name);
	}
	
	/**
	 * Override to add additional information & markup to 
	 * the feature node for a given word. May be used to
	 * indicate gender, count, etc.
	 */
	protected void setProperties(FeatureNode fn)
	{
		fn.set(type +  "-FLAG", new FeatureNode("T"));
		for (String p : nodeProperties)
			fn.set(p, new FeatureNode((String)attributes.get(p)));
	}

	/** 
	 * Return a list of names of attributes that are propagated as 
	 * FeatureNode's in a call to setProperties(FeatureNode).
	 */
	public List<String> getNodePropertyNames()
	{
		return nodeProperties;
	}
	
	public EntityInfo(String _originalSentence,
	                  int _firstCharIndex,
	                  int _lastCharIndex)
	{
		originalSentence = _originalSentence;
		firstCharIndex = _firstCharIndex;
		lastCharIndex = _lastCharIndex;
	}
	
	public EntityInfo(String _originalSentence,
  		              int _firstCharIndex,
			          int _lastCharIndex,
			          String type)
	{
		this(_originalSentence, _firstCharIndex, _lastCharIndex);
		this.type = type;
	}
	
	public EntityInfo(String _originalSentence,
		              int _firstCharIndex,
			          int _lastCharIndex,
			          EntityType type)
	{
		this(_originalSentence, _firstCharIndex, _lastCharIndex, type.name());
	}	
}
