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

/**
 * This algorithm combines sequences of words which should be a single 
 * word (proper names, and idioms like "at hand")
 */
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.parser.LinkParserClient;

/**
 * Combines two words into one, stored in the left node.
 */
public class TwoWordCombineToLeftAlg extends TemplateMatchingAlg {

    protected void applyTo(FeatureNode node, LinkParserClient lpc) {
        FeatureNode rightNode = LinkView.getRight(node);
        FeatureNode leftNode = LinkView.getLeft(node);

        // find the strings and originals
        String rightString = rightNode.featureValue("str");
        String rightOriginal = rightNode.featureValue("orig_str");
        if (rightOriginal == null)
            rightOriginal = rightString;

        String leftString = leftNode.featureValue("str");
        String leftOriginal = leftNode.featureValue("orig_str");
        if (leftOriginal == null)
            leftOriginal = leftString;

        // make the combined strings
        String original = leftOriginal + " " + rightOriginal;
        String str = leftString + "_" + rightString;

        // set the values
        leftNode.get("ref").set("name", new FeatureNode(str));
        leftNode.set("str", new FeatureNode(str));
        leftNode.set("orig_str", new FeatureNode(original));
        leftNode.set("collocation_end", rightNode);
        leftNode.set("collocation_start", leftNode);
        //erase the other word strings
        rightNode.set("str", null);
        rightNode.set("orig_str", null);
    }

}

