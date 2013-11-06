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
 * An atom that holds a string.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class StringNode extends Atom
{
	private static final long serialVersionUID = 1087852981117134672L;

	protected String string;

	public String getValue()
	{
		return string;
	}
	public void setValue(String str)
	{
		string = str;
	}
}
