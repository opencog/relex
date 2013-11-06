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
 * The FeatureForeach class provides a backwards-compatible interface to
 * RelationForeach class.  Its use is deprecated.
 */

public class FeatureForeach extends RelationForeach
{
	/**
	 * Walk the graph, following "NEXT" links only, and then
	 * call the callback for each node visited.  Assuming that
	 * "root" was the left-most word in a sentence, this will
	 * result in the callback being called for each word in
	 * the sentence, in a left-to-right order.
	 *
	 * The callback may return "true" to halt the loop.
	 */
	public static Boolean foreachWord(FeatureNode root, FeatureNodeCallback cb)
	{
		// skip the LEFT-WALL
		root = root.get("NEXT");
		while (root != null)
		{
			Boolean rc = cb.FNCallback(root);
			if (rc) return rc;
			root = root.get("NEXT");
		}
		return false;
	}
}

/* =========================== END OF FILE ========================= */
