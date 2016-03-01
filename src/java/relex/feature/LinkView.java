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
/*
 * This class allows for a view of a FeatureNode as a link
 */
import java.util.HashSet;
import java.util.ArrayList;

public class LinkView extends View
{
    private static final String LEFT_FEATURE_NAME = "F_L";			// the node to the left
    private static final String RIGHT_FEATURE_NAME = "F_R"; 		// the node to the right
    private static final String LEFT_LABEL_FEATURE_NAME = "lab_L"; 	// the left label of the link
    private static final String RIGHT_LABEL_FEATURE_NAME = "lab_R"; // the right label of the link
    private static final String LABEL_FEATURE_NAME = "LAB"; 		// the combined label of the link

    private static FeatureNameFilter filter;

    static {
        ArrayList<String> featureOrder = new ArrayList<String>();
        featureOrder.add(LABEL_FEATURE_NAME);
        featureOrder.add(LEFT_FEATURE_NAME);
        featureOrder.add(RIGHT_FEATURE_NAME);
        featureOrder.add("");

        HashSet<String> ignoreFeatures = new HashSet<String>();
        ignoreFeatures.add(LEFT_LABEL_FEATURE_NAME);
        ignoreFeatures.add(RIGHT_LABEL_FEATURE_NAME);

        filter = new FeatureNameFilter(ignoreFeatures, featureOrder);
    }

    public static FeatureNameFilter getFilter() {
        return filter;
    }

    public String toString() {
        return toString(fn());
    }

    public static String toString(FeatureNode ths) {
        return ths.toString(getFilter());
    }

    public LinkView(FeatureNode ths) {
        super(ths);
    }

    public void setLinkFeatures(String leftLabel, String rightLabel,
            String label, FeatureNode left, FeatureNode right) {
        setLinkFeatures(fn(), leftLabel, rightLabel, label, left, right);
    }

    public static void setLinkFeatures(FeatureNode ths, String leftLabel,
            String rightLabel, String label, FeatureNode left, FeatureNode right) {
        ths.set(LEFT_FEATURE_NAME, left);
        LinkableView.addLink(left, 1, ths);
        ths.set(RIGHT_FEATURE_NAME, right);
        LinkableView.addLink(right, -1, ths);
        ths.set(LEFT_LABEL_FEATURE_NAME, new FeatureNode(leftLabel));
        ths.set(RIGHT_LABEL_FEATURE_NAME, new FeatureNode(rightLabel));
        ths.set(LABEL_FEATURE_NAME, new FeatureNode(label));
    }

    public String getLabel(int direction) {
        return getLabel(fn(), direction);
    }

    public static String getLabel(FeatureNode ths, int direction) {
        if (direction < 0)
            return ths.get(LEFT_LABEL_FEATURE_NAME).getValue().toString();
        else if (direction > 0)
            return ths.get(RIGHT_LABEL_FEATURE_NAME).getValue().toString();
        else
            return ths.get(LABEL_FEATURE_NAME).getValue().toString();
    }

    public FeatureNode getLeft() {
        return getLeft(fn());
    }

    public static FeatureNode getLeft(FeatureNode ths) {
        return ths.get(LEFT_FEATURE_NAME);
    }

    public FeatureNode getRight() {
        return getRight(fn());
    }

    public static FeatureNode getRight(FeatureNode ths) {
        return ths.get(RIGHT_FEATURE_NAME);
    }

    public static void main(String[] args) {
        FeatureNode l = new FeatureNode();
        FeatureNode r = new FeatureNode();
        l.set("name", new FeatureNode("left"));
        r.set("name", new FeatureNode("right"));
        LinkView lv = new LinkView(new FeatureNode());
        lv.setLinkFeatures("llab", "rlab", "lab", l, r);
        System.out.println(lv.fn());
    }

} // end LinkView

