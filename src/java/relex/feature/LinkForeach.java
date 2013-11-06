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

import java.util.HashSet;

/**
 * The LinkForeach class provides a simple, easy-to-use way of
 * traversing all of the link-grammar links in a feature graph.
 *
 * It is an alternative to obtaining the links directly from the
 * link-grammar, as it returns pointers into the feature graph,
 * rather than to words in a sentence.
 *
 * The best way to think of a "foreach" class is to think of it as
 * a for-loop, with a "hidden" iterator. The callback is then the
 * inner block of the for loop. Because Java does not have the concept
 * of anonymous blocks, the inner block callback has to be implemented
 * as an actual class, making this programming style rather bulky.
 * However, its well worth it, as, despite the extra bulk, it can
 * dramatically simplify code.
 *
 *  Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */

public class LinkForeach
{
	/**
	 * Walk the graph, calling a callback for each link label node visited.
	 * The callback is called only when a node has a "LAB" entry. Only
	 * link-grammar linkage nodes have such an entry.
	 */
	private static Boolean
	_graphCrawl(FeatureNode f,
	            HashSet<FeatureNode> alreadyVisited,
	            FeatureNodeCallback cb)
	{
		Boolean rc = false;
		if (alreadyVisited.contains(f))
			return rc;

		alreadyVisited.add(f);

		// Should never find that the node is valued at this point!
		if (f.isValued())
			return rc;

		for (String key : f.getFeatureNames())
		{
			// Review the structure of the graph!
			// FeatureNode fk = f.get(key);
			// if(!fk.isValued())
			// 	System.err.println(key + " => " + RelationView._prt_vals(fk));
			// else
			// 	System.err.println(key + " <-- " + fk.getValue());
			//
			// The NEXT pointer is enough to walk through all of the words
			// in the the sentence; however, that is not quite enough to
			// to crawl the entire graph. In particular, comparative links
			// to _$crVar aren't linked by words. So follow all pointers,
			// unless they are one of the particularly boring kinds below.
			if (key.equals("str")) continue;
			if (key.equals("orig_str")) continue;
			if (key.equals("SIG")) continue;
			if (key.equals("POS")) continue;
			if (key.equals("pos")) continue;
			if (key.equals("PREV")) continue;
			if (key.equals("this")) continue;
			if (key.startsWith("phr-")) continue;
			if (key.endsWith("-FLAG")) continue;
			if (key.endsWith("_char")) continue;
			if (key.endsWith("_links")) continue;

			FeatureNode fn = f.get(key);

			// If there's a "LAB", look for other stuff too.
			if (key.equals("LAB"))
			{
				rc = cb.FNCallback(f);
				if (rc) return rc;
			}

			if (!fn.isValued())
			{
				rc = _graphCrawl(fn, alreadyVisited, cb);
				if (rc) return rc;
			}
		}
		return rc;
	}

	/**
	 * Walk the graph, calling a callback for each link grammar link
	 * node visited. The callback is called only when a node has a
	 * "LAB" entry. Only link-grammar links nodes have such an entry.
	 */
	public static Boolean foreach(FeatureNode root, FeatureNodeCallback cb)
	{
		HashSet<FeatureNode> alreadyVisited = new HashSet<FeatureNode>();
		Boolean rc = _graphCrawl(root, alreadyVisited, cb);
		return rc;
	}
}

/* =========================== END OF FILE ========================= */
