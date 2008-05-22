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

package relex.frame;

import java.util.HashMap;

/**
 * HashMap that maps a set of variables in the rules to values
 * in the RelEx output.
 * (This class primarily serves as a typedef.)
 */
class VarMap extends HashMap<String,String>
{
	private static final long serialVersionUID = -7194605404916748220L;

	VarMap() { super(); }
	VarMap(int elements) { super(elements); }
	VarMap(VarMap varMap) { super(varMap); }

	VarMap(String varName, String value) {
		super();
		this.put(varName,value);
	}

	public StringBuffer print() {
		StringBuffer out = new StringBuffer(" VarMap: ");
		//System.out.print("  VarMap: " );
		for (String varName : this.keySet()) {
			//System.out.print(varName + "=" + this.get(varName) + "  ");
			out.append(varName + "=" + this.get(varName) + "  ");
		}
		out.append("\n");
		return out;
	}
}
