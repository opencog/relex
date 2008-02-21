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
 * Frame.java
 *
 * Accept ascii text relex as input, spit out frame related rules
 * as output. Two basic functions are implemented here:
 * -- reading in of the frame rules from data files,
 * -- basic loop that applied rules to the relex input.
 *
 * Some notes about the rules file format:
 * -- assuming ^ (AND) precedence over OR
 * -- nested parens not implemented
 * -- no extraneous text in frame mapping rules once rules start
 *    i.e. # is the sole splitter token
 * -- grouping parens = { }
 * -- matching is case insensitive
 * -- concept variable names and values are case insensitive
 * -- concept variable numerical indexes are preceded by an
 *    underscore (e.g., $Noun_1)
 * -- _$ prefixed variables in the rules that are preceded
 *    by an underscore (e.g. _$qVar) are not treated as
 *    variables, but rather (from the interpreter's perspective)
 *    as literals
 */

package relex.frame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//TODO handle comments in mapping rules file?
//TODO handling commas in relex output (take out?), e.g, 14,000

/**
 * Main class--Converts RelEx output to Frame relationships
 * using supplied mapping rules
 */
public class Frame
{
	static /*final*/ boolean VERBOSE = false;

	static final String MAPPING_RULES_DIR		  = "data/frame/";
	public static final String MAPPING_RULES_FILE		 = "mapping_rules.txt";
	static final String CONCEPT_VARS_DIR		 = MAPPING_RULES_DIR;
	public static final String CONCEPT_VARS_FILE		 = "concept_vars.txt";

	static ArrayList<Rule> rules = new ArrayList<Rule>();

	// A map of the collective concept variables and their legal values
	static HashMap<String,ArrayList<String>> conceptVarMap = new HashMap< String,ArrayList<String> >();

	public HashMap<Rule, VarMapList> fireRules = new HashMap<Rule,VarMapList>();

	// static initializer -- load mapping rules and concept vars
	// only once, when class is loaded
	static {
		loadDataFiles();
	}

	/**
	 * Convert a string of RelEx relationships to Frame relationships
	 *
	 * @param relex
	 * @return a String array of Frame relationships
	 */
	synchronized public String[] process(String relex)
	{
		relex = relex.replace(" ", "").replace("\t", "");
		//ArrayList<Rule> fireRules = new ArrayList<Rule>();
		//HashMap<Rule,VarMapList> fireRules = new HashMap<Rule,VarMapList>();
		fireRules.clear();
		Collection<String> relationships = new LinkedHashSet<String>();
		VarMapList varMapList = new VarMapList();
		for (Rule rule: rules) {
			if (rule.satisfiedByRelex(relex,varMapList)) {
				fireRules.put(rule,varMapList);
				varMapList = new VarMapList();
				if (VERBOSE) System.out.println("\nRULE PASSES: " + rule.getRuleStr());
			}
		}

		// Fire off the matching rules to create set of frame relationship results
		if (VERBOSE) System.out.println("\nFiring Rules NEW count: " + fireRules.size() + "\n");
		//for (Rule rule: fireRules) {
		for (Rule rule: fireRules.keySet()) {
			relationships.addAll(rule.fire(fireRules.get(rule),VERBOSE));
		}

		//println("\nRELEX SENTENCE:\n\n" + relex.trim());
		//print out the applied mapping rules
//		println("\nAPPLIED MAPPING RULES:\n");
//		for (Rule aRule: fireRules) {
//			println(aRule.getRuleStr());
//		}

		return relationships.toArray(new String[relationships.size()]);
	} //end processRelex()


	/**
	 * Same as process(String relex), except takes String array as argument
	 *
	 * @param relex  String array of RelEx relationships
	 * @return String array of Frame relationships
	 */
	public String[] process(String[] relex)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < relex.length; i++) {
		  sb.append(relex[i]);
		}
		return process(sb.toString());
	}

	public String printAppliedRules()
	{
		StringBuffer sb = new StringBuffer();
		for (Rule rule : fireRules.keySet()) {
			sb.append(rule.ruleStr + "\n");
		}
		return sb.toString();
	}

	public static String loadDataFiles()
	{
		String feedback = loadConceptVars();
		feedback += loadMappingRules();
		return feedback;
	}

	private static String loadConceptVars()
	{
		try {
			String dataPath = System.getProperty("frame.datapath");
			if (dataPath==null) {
				dataPath = CONCEPT_VARS_DIR;
			}
			String msg = "";
			StringBuffer fileStr = new StringBuffer();
			try {
				BufferedReader in = new BufferedReader(
					new FileReader(dataPath + CONCEPT_VARS_FILE));
				String line;
				while ((line = in.readLine()) != null) {
					fileStr.append(line + "\n");
				}
				in.close();
			}
			catch (IOException e) {
				msg = "Error reading in Frame concept vars file - file not loaded";
				System.err.println(msg);
				System.err.println(e);
				return "\n" + msg + "\n" + e;
			}

			HashMap<String,ArrayList<String>> newConceptVarMap =
			      new HashMap< String,ArrayList<String> >();
			String[] varEntries = fileStr.toString().split("\\$");

			for (int i=1;i<varEntries.length;i++) {
				String varEntry = varEntries[i];
				String[] lines = varEntry.split("\\n");
				if (lines.length==0) {
					continue;
				}

				// First line is the var name.
				String varName = "$" + lines[0].trim().toLowerCase();
				int valuesCount = lines.length - 1;
				newConceptVarMap.put(varName,new ArrayList<String>());
				for (int v=1;v<=valuesCount;v++) {
					String value = lines[v].trim().toLowerCase();
					if (!value.equals("")) {
						newConceptVarMap.get(varName).add(lines[v].trim().toLowerCase());
					}
				}
			}
			conceptVarMap = newConceptVarMap;
			if (VERBOSE) {
				System.out.println("\nCONCEPT VAR LEGAL VALUES:");
				for (String key: conceptVarMap.keySet()) {
					System.out.println("\n" + key + ":");
					ArrayList<String> values = conceptVarMap.get(key);
					for (String value: values) {
						System.out.println("   - " + value);
					}
				}
			}
			return msg + "Frame concept variables have been loaded.\n";
		} catch (Exception e) {
			String msg = "Error processing Frame concept vars file - file not loaded";
			System.out.println(msg);
			System.out.println(e);
			e.printStackTrace();
			return "\n" + msg + "\n" + e;
		}
	}

	private static String loadMappingRules()
	{
		try {
			String relexRulesStr = "";
			String dataPath = System.getProperty("frame.datapath");
			if (dataPath==null) {
				dataPath = MAPPING_RULES_DIR;
			}
			try {
				BufferedReader in = new BufferedReader(
						new FileReader(dataPath + MAPPING_RULES_FILE));
				String line;
				StringBuffer sb = new StringBuffer();
				while ((line = in.readLine()) != null) {
					sb.append(line + "\n");
				}
				in.close();

				relexRulesStr = sb.toString();

			} catch (IOException e) {
				String msg = "Error reading in Frame mapping rules file!--File not loaded";
				System.err.println(msg);
				System.err.println(e);
				return(msg + "\n" + e);
			}
			String msg = "";
			String[] relexRules = relexRulesStr.split("#");

			//process rules - store in Rule objects
			//start index at 1 b/c we don't want initial files text before first # symbol
			String line = "";

			ArrayList<Rule> newRules = new ArrayList<Rule>();
			for (int i=1; i<relexRules.length; i++) {
				//TODO check for duplicate rules?
				Rule rule = new Rule();
				line = relexRules[i];

				boolean parseSuccess = rule.parseRule(line);
				if (parseSuccess) {
					//rule.print();
					newRules.add(rule);
				}
				else {
					msg += "\n**** Warning: IMPROPERLY FORMED RULE ENCOUNTERED. ****\nRule: " + line.trim() + "\n";
					System.out.println("**** IMPROPERLY FORMED RULE ENCOUNTERED at line " + i + ". ****\nRule: " + line.trim() + "\n");
				}
			}
			rules = newRules;
			if (VERBOSE) {
				for (Rule rule: rules) {
					rule.printout();
					//println(rule.conditionSyntaxTree.print());
				}
			}

			// Check each concept variable to make sure it has an
			// entry in the concept_vars.txt definition file.
			HashSet<String> checkedVars = new HashSet<String>();
			String regex = "(?<!_)\\$\\w+";
			Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(relexRulesStr);
			Pattern indexPattern = Pattern.compile("\\_\\d+\\z");
			Matcher indexMatcher = indexPattern.matcher("");
			while (m.find()) {
				String varName = m.group();
				//strip off any index suffix (e.g., $Noun_1)
				indexMatcher.reset(varName);
				if (indexMatcher.find()) {
					varName = varName.substring(0,indexMatcher.start());
				}

				if (checkedVars.add(varName) && !varName.startsWith("$var")) {
					if (conceptVarMap.get(varName.toLowerCase())==null) {
						msg += "\nWarning: No definition found for concept variable " + varName;
						System.out.println("Warning: No definition found for concept variable " + varName);
					}
				}
			}

			// println("Frame mapping rules have been loaded.");
			return msg + "\nFrame mapping rules have been loaded.\n";
	 	} catch (Exception e) {
			String msg = "Error processing Frame mapping rules file - file not loaded";
			System.out.println(msg);
			System.out.println(e);
			e.printStackTrace();
			return "\n" + msg + "\n" + e;
		}
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
	}
} //end class Frame


