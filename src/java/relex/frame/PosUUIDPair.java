package relex.frame;

public class PosUUIDPair {
	// Class for storing where the replacements happen.
	// THIS IS A HACK due to RelexToFrame using regexes. It's not efficient and
	// it's not elegant, but it should work.
	public String UUID = new String("");
	public int pos = 0;
	public PosUUIDPair(int _pos, String _UUID)
		{pos=_pos;UUID=_UUID;}
	
}
