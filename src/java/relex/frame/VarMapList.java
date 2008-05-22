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

import java.util.ArrayList;

/**
 * A list of VarMaps. Multiple VarMaps are needed for when there is
 * more than one match for a condition in a RelEx sentence, and thus
 * the same variable has different values for each match. (This class
 * primarily serves as a typedef.)
 */
public class VarMapList extends ArrayList<VarMap>
{
	private static final long serialVersionUID = 857891970832913935L;

	public VarMapList() { super(); }
	public VarMapList(int elements) { super(elements); }

	public StringBuffer print() {
		//System.out.println("\nPRINT VarMapList");
		StringBuffer out = new StringBuffer("\nPRINT VarMapList\n");
		if (this.isEmpty()) {
			//System.out.println("  empty");
			out.append(  "empty\n");
		}
		else {
			for (VarMap varMap : this) {
				out.append(varMap.print());
			}
		}
		return out;
	}
}
