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

package relex.feature;

/**
 * This class allows FeatureNodes to have different views. Essentially,
 * every view will have special methods for setting and getting
 * particular features.
 */
public abstract class View
{
	private FeatureNode viewed;

	public View(FeatureNode f)
	{
		if (f == null) throw new RuntimeException("Cannot view a null FeatureNode.");
		viewed = f;
	}

	public FeatureNode fn()
	{
		return viewed;
	}

	public abstract String toString();
}
