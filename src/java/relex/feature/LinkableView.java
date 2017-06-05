/*
 * Copyright 2008 Novamente LLC
 * Copyright 2009 Linas Vepstas
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
import java.util.ArrayList;

/**
 * This class allows for a view of a FeatureNode as an element that
 * contains left and right links.
 */
public class LinkableView extends View // implements TreeNode , LinkNode
{
	private static final String NUM_LEFT_LINKS_FEATURE_NAME = "num_left_links";

	private static final String NUM_RIGHT_LINKS_FEATURE_NAME = "num_right_links";

	private static final String LEFT_LINK_PREFIX = "linkL";

	private static final String RIGHT_LINK_PREFIX = "linkR";

	private static final String POS_FEATURE_NAME = "POS";
	private static final String SUBSCRIPT_NAME = "subscript";

	private static final String WORD_STRING_FEATURE_NAME = "str";

	private static final String ORIG_WORD_STRING_FEATURE_NAME = "orig_str";

	private static final String TENSE_FEATURE_NAME = "tense";

	private static final String NEXT_NAME = "NEXT";

	private static final String PREV_NAME = "PREV";

	private static final String START_NAME = "start_char";

	private static final String END_NAME = "end_char";

	private static final String COLLOCATION_START = "collocation_start";

	private static final String COLLOCATION_END = "collocation_end";

	private static final String INDEX_NAME = "index_in_sentence";

	private static FeatureNameFilter filter;

	static
	{
		// Create the filter which controls how LinkableViews are printed.
		ArrayList<String> featureOrder = new ArrayList<String>();
		featureOrder.add(POS_FEATURE_NAME);
		featureOrder.add(WORD_STRING_FEATURE_NAME);
		featureOrder.add(TENSE_FEATURE_NAME);
		featureOrder.add("");
		featureOrder.add(NUM_LEFT_LINKS_FEATURE_NAME);
		featureOrder.add(LEFT_LINK_PREFIX);
		featureOrder.add(NUM_RIGHT_LINKS_FEATURE_NAME);
		featureOrder.add(RIGHT_LINK_PREFIX);

		HashSet<String> ignoreFeatures = new HashSet<String>();

		filter = new FeatureNameFilter(ignoreFeatures, featureOrder);
	}

	public LinkableView(FeatureNode f) {
		super(f);
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

	private static void throwIfNoFN(FeatureNode ths) {
		if (ths == null)
			throw new RuntimeException("View requires a FeatureNode");

	}

	private static void throwIfBadDirection(int direction) {
		if (direction == 0)
			throw new RuntimeException("direction must be negative (left) or positive (right)");
	}

	private static void setNumLinks(FeatureNode ths, int direction, int num) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		FeatureNode f = null;
		String str = null;
		if (direction < 0)
			str = NUM_LEFT_LINKS_FEATURE_NAME;
		else if (direction > 0)
			str = NUM_RIGHT_LINKS_FEATURE_NAME;
		f = ths.get(str);
		if (f == null) {
			f = new FeatureNode(Integer.toString(num));
			ths.set(str, f);
		} else
			f.setValue(Integer.toString(num));
	}

	public int numLinks(int direction) {
		return numLinks(fn(), direction);
	}

	public static int numLinks(FeatureNode ths, int direction) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		FeatureNode f = null;
		if (direction < 0)
			f = ths.get(NUM_LEFT_LINKS_FEATURE_NAME);
		else if (direction > 0)
			f = ths.get(NUM_RIGHT_LINKS_FEATURE_NAME);
		if (f == null)
			return 0;
		return Integer.parseInt(f.getValue());
	}

	private static FeatureNode _getLink(FeatureNode ths, int direction, int i) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		FeatureNode f = null;
		if (direction < 0)
			f = ths.get(LEFT_LINK_PREFIX + i);
		else
			f = ths.get(RIGHT_LINK_PREFIX + i);
		if (f == null)
			return null;
		return f;
	}

	public boolean hasLink(int direction, String label) {
		return hasLink(fn(), direction, label);
	}

	public static boolean hasLink(FeatureNode ths, int direction, String label) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		int lim = numLinks(ths, direction);
		for (int i = 0; i < lim; i++) {
			if (LinkView.getLabel(getLink(ths, direction, i), 0).equals(label))
				return true;
		}
		return false;
	}

	public FeatureNode getLink(int direction, int i) {
		return getLink(fn(), direction, i);
	}

	public static FeatureNode getLink(FeatureNode ths, int direction, int i) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		int numL = numLinks(ths, direction);
		if (i < 0 || i >= numL)
			throw new RuntimeException("Illegal link index");
		return _getLink(ths, direction, i);
	}

	public void addLink(int direction, FeatureNode link) {
		addLink(fn(), direction, link);
	}

	public static void addLink(FeatureNode ths, int direction, FeatureNode link) {
		throwIfNoFN(ths);
		throwIfBadDirection(direction);

		int n = numLinks(ths, direction);
		setNumLinks(ths, direction, n + 1);
		String s = null;
		if (direction < 0)
			s = LEFT_LINK_PREFIX + n;
		else if (direction > 0)
			s = RIGHT_LINK_PREFIX + n;
		ths.set(s, link);
	}

	private static void setFeat(FeatureNode ths, String featname, String featval)
	{
		throwIfNoFN(ths);
		FeatureNode f = ths.get(featname);
		if (f != null)
			f.setValue(featval);
		else
			ths.set(featname, new FeatureNode(featval));
	}

	public void setSubscript(String inf) {
		setSubscript(fn(), inf);
	}

	public static void setSubscript(FeatureNode ths, String inf)
	{
		setFeat(ths, SUBSCRIPT_NAME, inf);
	}

	public void setPOS(String pos) {
		setPOS(fn(), pos);
	}

	public static void setPOS(FeatureNode ths, String pos)
	{
		setFeat(ths, POS_FEATURE_NAME, pos);
	}

	public String getPOS() {
		return getPOS(fn());
	}

	public static String getPOS(FeatureNode ths) {
		throwIfNoFN(ths);
		FeatureNode f = ths.get(POS_FEATURE_NAME);
		if (f != null)
			return f.getValue().toString();
		return null;
	}

	public void setWord(String wordString) {
		setWord(fn(), wordString);
	}

	public static void setWord(FeatureNode ths, String wordString)
	{
		throwIfNoFN(ths);
		FeatureNode f = new FeatureNode(wordString);
		ths.set(ORIG_WORD_STRING_FEATURE_NAME, f);
	}

	public String getWordString() {
		return getWordString(fn());
	}

	public static String getWordString(FeatureNode ths) {
		throwIfNoFN(ths);
		FeatureNode f = ths.get(WORD_STRING_FEATURE_NAME);
		if (f != null)
			return f.getValue().toString();
		return null;
	}

	public String getOrigWordString() {
		return getOrigWordString(fn());
	}

	public static String getOrigWordString(FeatureNode ths) {
		throwIfNoFN(ths);
		FeatureNode f = ths.get(ORIG_WORD_STRING_FEATURE_NAME);
		if (f != null)
			return f.getValue().toString();
		return null;
	}

	public void setEntityFlag() {
		setEntityFlag(fn());
	}

	public static void setEntityFlag(FeatureNode ths) {
		ths.set("ENTITY-FLAG", new FeatureNode("T"));
	}

	public boolean hasEntityFlag()	{
		return fn().get("ENTITY-FLAG") != null;
	}

	public void setNext(FeatureNode f) {
		setNext(fn(), f);
	}

	public static void setNext(FeatureNode ths, FeatureNode f) {
		throwIfNoFN(ths);
		ths.set(NEXT_NAME, f);
	}

	public void setPrev(FeatureNode f) {
		setPrev(fn(), f);
	}

	public static void setPrev(FeatureNode ths, FeatureNode f) {
		throwIfNoFN(ths);
		ths.set(PREV_NAME, f);
	}

	public FeatureNode getNext() {
		return getNext(fn());
	}

	public static FeatureNode getNext(FeatureNode ths) {
		throwIfNoFN(ths);
		return ths.get(NEXT_NAME);
	}

	public FeatureNode getPrev() {
		return getPrev(fn());
	}

	public static FeatureNode getPrev(FeatureNode ths) {
		throwIfNoFN(ths);
		return ths.get(PREV_NAME);
	}

	public int getStartChar() {
		return getStartChar(fn());
	}

	public static int getStartChar(FeatureNode ths) {
		throwIfNoFN(ths);
		int val = -1;
		try {
			val = Integer.parseInt(ths.get(START_NAME).getValue());
		} catch (Exception e) {
		}
		return val;
	}

	public int getEndChar() {
		return getEndChar(fn());
	}

	public static int getEndChar(FeatureNode ths) {
		throwIfNoFN(ths);
		int val = -1;
		try {
			val = Integer.parseInt(ths.get(END_NAME).getValue());
		} catch (Exception e) {
		}
		return val;
	}

	public void setStartChar(int val) {
		setStartChar(fn(), val);
	}

	public static void setStartChar(FeatureNode ths, int val) {
		throwIfNoFN(ths);
		try {
			ths.get(START_NAME).setValue(Integer.toString(val));
		} catch (Exception e) {
		}
	}

	public void setEndChar(int val) {
		setEndChar(fn(), val);
	}

	public static void setEndChar(FeatureNode ths, int val) {
		throwIfNoFN(ths);
		try {
			ths.get(END_NAME).setValue(Integer.toString(val));
		} catch (Exception e) {
		}
	}

	public int getExpandedStartChar() {
		return getExpandedStartChar(fn());
	}

	public static int getExpandedStartChar(FeatureNode ths) {
		throwIfNoFN(ths);
		int val = -1;
		try {
			val = Integer.parseInt(ths.get(COLLOCATION_START).get(START_NAME)
					.getValue());
		} catch (Exception e) {
			val = getStartChar(ths);
		}
		return val;
	}

	public int getExpandedEndChar() {
		return getExpandedEndChar(fn());
	}

	public static int getExpandedEndChar(FeatureNode ths) {
		throwIfNoFN(ths);
		int val = -1;
		try {
			val = Integer.parseInt(ths.get(COLLOCATION_END).get(END_NAME)
					.getValue());
		} catch (Exception e) {
			val = getEndChar(ths);
		}
		return val;
	}

	public void setExpandedStartChar(int val) {
		setExpandedStartChar(fn(), val);
	}

	public static void setExpandedStartChar(FeatureNode ths, int val) {
		throwIfNoFN(ths);
		try {
			ths.get(COLLOCATION_START).get(START_NAME).setValue(
					Integer.toString(val));
		} catch (Exception e) {
		}
	}

	public void setExpandedEndChar(int val) {
		setExpandedEndChar(fn(), val);
	}

	public static void setExpandedEndChar(FeatureNode ths, int val) {
		throwIfNoFN(ths);
		try {
			ths.get(COLLOCATION_END).get(END_NAME).setValue(
					Integer.toString(val));
		} catch (Exception e) {
		}
	}

	public int getIndexInSentence() {
		return getIndexInSentence(fn());
	}

	public static int getIndexInSentence(FeatureNode ths) {
		throwIfNoFN(ths);
		int val = -1;
		try {
			val = Integer.parseInt(ths.get(INDEX_NAME).getValue());
		} catch (Exception e) {
		}
		return val;
	}

	public void setCharIndices(int start, int end, int indexInSentence) {
		setCharIndices(fn(), start, end, indexInSentence);
	}

	public static void setCharIndices(FeatureNode ths,
	                           int start, int end, int indexInSentence)
	{
		throwIfNoFN(ths);
		FeatureNode f = ths.get(START_NAME);
		if (f == null) {
			f = new FeatureNode();
			ths.set(START_NAME, f);
		}
		f.forceValue(new Integer(start).toString());
		f = ths.get(END_NAME);
		if (f == null) {
			f = new FeatureNode();
			ths.set(END_NAME, f);
		}
		f.forceValue(new Integer(end).toString());
		f = ths.get(INDEX_NAME);
		if (f == null) {
			f = new FeatureNode();
			ths.set(INDEX_NAME, f);
		}
		f.forceValue(new Integer(indexInSentence).toString());
	}

	public static void main(String[] args) {

		FeatureNode l = new FeatureNode("L");
		FeatureNode r = new FeatureNode("R");

		FeatureNode f = new FeatureNode();
		LinkableView flv = new LinkableView(f);
		flv.addLink(-1, l);
		flv.addLink(1, r);
		System.out.println(f);

	}

} // end LinkableView

