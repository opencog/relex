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
