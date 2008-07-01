/*
 * Copyright 2008 Borislav Iordanov
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

package relex.corpus;

import relex.entity.EntityMaintainer;

/**
 * An EntityMaintainerFactory implementation is responsible for detecting
 * named entities in a sentence and producing an instance of 
 * {@link EntityMaintainer}.
 *
 * This class also serves as a factory of a globally configured 
 * EntityMaintainerFactory through the 
 * relex.corpus.EntityMaintainerFactory system property.  The default
 * value of this property is relex.corpus.GateEntityDetector. This
 * globally configured EntityMaintainerFactory factory is created only
 * once and maintained as a singleton within this class.
 */
public abstract class EntityMaintainerFactory
{	
	public static final String DEFAULT_FACTORY = "relex.corpus.GateEntityDetector";
	
	/**
	 * Detect named entities in the string sentence and return
	 * an EntityMaintainer.
	 */
	public abstract EntityMaintainer makeEntityMaintainer(String sentence);
	
	private static EntityMaintainerFactory factory = null;
	
	public static synchronized EntityMaintainerFactory get()
	{
		if (factory != null)
			return factory;
		
		String classname = System.getProperty("relex.corpus.EntityMaintainerFactory");
		if (classname == null)
			classname = DEFAULT_FACTORY;
		try
		{
			Class<?> cl = Class.forName(classname);
			return factory = (EntityMaintainerFactory)cl.newInstance();				
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
