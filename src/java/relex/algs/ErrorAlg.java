package relex.algs;
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

import relex.feature.FeatureNode;
import relex.parser.LinkParserClient;

/**
 * Throws a runtime exception if an error is encountered
 */
public class ErrorAlg extends TemplateMatchingAlg {

	protected void applyTo(FeatureNode node, LinkParserClient lpc) {
		throw new RuntimeException("ErrorAlg with template:\n" + getTemplate() + "\n\napplied to FeatureNode:\n" + node);
	}

}
