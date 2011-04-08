/*
 * Copyright (C) 2008,2009 Novamente LLC
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
 * Frame.java
 */

package relex.frame;

/**
 * Main class--Converts RelEx output to Frame relationships
 * using supplied mapping rules
 */
public class Frame extends FrameProcessor
{
	static /*final*/ boolean VERBOSE = false;

	private static final String MAPPING_RULES_DIR = "data/frame";
	private static final String MAPPING_RULES_FILE = "mapping_rules.txt";
	private static final String CONCEPT_VARS_FILE = "concept_vars.txt";

	public Frame()
	{
		set_data_files(MAPPING_RULES_DIR, CONCEPT_VARS_FILE, MAPPING_RULES_FILE);
	}


	/**
	 * A simple demonstration of how to use this class.
	 */
	public static void main(String args[])
	{
		Frame fr = new Frame();
		String verbose = System.getProperty("verbose");
		if (verbose!=null && verbose.equals("true")) {
			Frame.VERBOSE = true;
		}
		String fin = "_subj(eat, Linas)\n_obj(eat, pizza)\n";
		String[] fout = fr.process(fin);
		for (int i=0; i < fout.length; i++) {
			System.out.println(fout[i]);
		}
		System.out.println("\n=========\n");
		System.out.println(fr.printAppliedRules());

        // access fireRules to see which ones were applied
        // public HashMap<Rule, VarMapList> fireRules = new HashMap<Rule,VarMapList>();
        
	}
} //end class Frame


