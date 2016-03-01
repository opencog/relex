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
import relex.feature.FeatureNode;
import relex.feature.FeatureTemplate;

public abstract class TemplateMatchingAlg extends SentenceAlgorithm {

	// separates the template from the rest of the algorithm in its init string.
	private final static String TEMPLATE_DELINEATOR = "\n=\n";

	private FeatureTemplate template;

	private String signature;

	protected FeatureTemplate getTemplate()
	{
		return template;
	}

	protected void setTemplate(FeatureTemplate t)
	{
		template = t;
	}

	/**
	 * Tests if ConstituentNode is a superset of template
	 *
	 * @param node
	 * @return map of matching nodes
	 */
	protected Map<String,FeatureNode> canApplyTo(FeatureNode node)
	{
		return template.match(node);
	}

	protected String getSignature()
	{
		return signature;
	}

	public int init(String str)
	{
		if (!Character.isLetter(str.charAt(0)))
			throw new RuntimeException("TemplateMatchingAlg string must start with a letter.\n" + str);
		// get signature
		int sigEnd = str.indexOf("\n");
		signature = str.substring(0, sigEnd);
		// get template
		int templateEnd = str.indexOf(TEMPLATE_DELINEATOR);
		if (templateEnd < 0)
			throw new RuntimeException("TemplateMAtchingAlg must have:"
			      + TEMPLATE_DELINEATOR
					+ "separating the template from the actions.\n" + str);
		setTemplate(new FeatureTemplate(str.substring(sigEnd + 1, templateEnd)));
		return templateEnd + TEMPLATE_DELINEATOR.length();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getSignature()).append("\n").append(getTemplate()).append(TEMPLATE_DELINEATOR);
		return sb.toString();
	}

} // end TemplateMatchingAlg
