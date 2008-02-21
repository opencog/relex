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
