/*
 * Copyright 2008,2009 Novamente LLC
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

package relex.algs;

import java.util.Map;

import relex.concurrent.RelexContext;
import relex.feature.FeatureNode;

/**
 * Throws a runtime exception if an error is encountered
 */
public class ErrorAlg extends TemplateMatchingAlg
{
	protected void applyTo(FeatureNode node, RelexContext context,
	                       Map<String,FeatureNode> vars)
	{
		throw new RuntimeException("ErrorAlg with template:\n" +
			getTemplate() + "\n\napplied to FeatureNode:\n" + node);
	}
}
