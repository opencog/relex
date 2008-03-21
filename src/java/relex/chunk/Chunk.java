


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
		pt.foreach(this);

	}

	public Boolean FNCallback(FeatureNode fn)
	{
		PhraseTree pt = new PhraseTree(fn);

		String type = pt.getPhraseType();
		if (!type.equals("NP") && !type.equals("VP")) return false;

		int depth = pt.getDepth();
		if (depth > 3) return false;

		int breadth = pt.getBreadth();
		if (breadth < 2) return false;

System.out.println("candiddate phrase " +  pt.toString());

		return false;
	}
}
