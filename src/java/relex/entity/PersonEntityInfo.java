package relex.entity;
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

import relex.feature.FeatureNode;

public class PersonEntityInfo extends EntityInfo
{
	private static final long serialVersionUID = -2016132731519768171L;
	
	public final static String MALE = "male";
	public final static String FEMALE = "female";

	private String gender;

	public PersonEntityInfo(String _originalSentence, int _firstCharIndex, int _lastCharIndex)
	{
		super(_originalSentence, _firstCharIndex, _lastCharIndex);
		gender = null;
	}

	public String idStringPrefix() {
		return "personID";
	}

	public void setGender(String g)
	{
		if (g == null) return;
		if (g.equals("male")) gender = "masculine";
		else if (g.equals("masculine")) gender = "masculine";
		else if (g.equals("female")) gender = "feminine";
		else if (g.equals("feminine")) gender = "feminine";
	}

	protected void setProperties(FeatureNode fn)
	{
		super.setProperties(fn);
		fn.set("PERSON-FLAG", new FeatureNode("T"));
		if (gender != null)
		{
			fn.set("GENDER", new FeatureNode(gender));
		}
	}
}
