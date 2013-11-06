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
import java.io.Serializable;

import relex.stats.TruthValue;

/**
 * An object that ever so vaguely resembles an OpenCog Atom.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Atom implements Serializable
{
	private static final long serialVersionUID = 4851663034991577183L;

	protected TruthValue truth_value;

	public Atom()
	{
		truth_value = null;
	}

	public TruthValue getTruthValue()
	{
		return truth_value;
	}
	public void setTruthValue(TruthValue tv)
	{
		truth_value = tv;
	}
}
