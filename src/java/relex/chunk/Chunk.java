


package relex.chunk;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.tree.PhraseTree;

/**
 * Discover phrase chunks
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class Chunk implements FeatureNodeCallback
{
	public Chunk() {}

	public void findChunks(ParsedSentence parse)
	{
		PhraseTree pt = parse.getPhraseTree();
System.out.println("duude its " + pt.toString());
		pt.foreach(this);

	}

	public Boolean FNCallback(FeatureNode fn)
	{
		PhraseTree pt = new PhraseTree(fn);

		String type = pt.getPhraseType();
		if (type.equals("NP"))
		{
System.out.println("have NP"+ pt.toString());
		}
		if (type.equals("VP"))
		{
System.out.println("have VP"+ pt.toString());
		}

		return false;
	}
}
