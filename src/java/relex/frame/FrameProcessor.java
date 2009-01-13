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
 * FrameProcessor.java
 *
 * Accept ascii text relex as input, spit out frame related rules
 * as output. Two basic functions are implemented here:
 * -- reading in of the frame rules from data files,
 * -- basic loop that applied rules to the relex input.
 *
 * Some notes about the rules file format:
 * -- ;; is a comment delimiter; everything after ;; is ignored.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO handling commas in relex output (take out?), e.g, 14,000

/**
 * Main class--Converts RelEx output to Frame relationships
 * using supplied mapping rules
 */
public class FrameProcessor
{
	static /*final*/ boolean VERBOSE = false;

	public static final String MAPPING_RULES_DIR = "data/frame";
	public static final String CONCEPT_VARS_DIR = MAPPING_RULES_DIR;
	public static final String MAPPING_RULES_FILE = "mapping_rules.txt";
	public static final String CONCEPT_VARS_FILE = "concept_vars.txt";

	public static final String COMMENT_DELIM = ";;";

	static boolean is_inited = false;
	static ArrayList<Rule> rules = new ArrayList<Rule>();

	// A map of the collective concept variables and their legal values
	static HashMap<String,ArrayList<String>> conceptVarMap = new HashMap< String,ArrayList<String> >();

	public HashMap<Rule, VarMapList> fireRules = new HashMap<Rule,VarMapList>();

	public FrameProcessor()
	{
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
				if (VERBOSE) System.err.println("\nRULE PASSES: " + rule.getRuleStr());
			}
		}

		// Fire off the matching rules to create set of frame relationship results
		if (VERBOSE) System.err.println("\nFiring Rules NEW count: " + fireRules.size() + "\n");
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
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < relex.length; i++) {
		  sb.append(relex[i]);
		}
		return process(sb.toString());
	}

	public String printAppliedRules()
	{
		StringBuilder sb = new StringBuilder();
		for (Rule rule : fireRules.keySet()) {
			sb.append(rule.ruleStr + "\n");
		}
		return sb.toString();
	}

	private static String loadDataFiles()
	{
		if (!is_inited)
		{
			String feedback = loadConceptVars();
			feedback += loadMappingRules();
			is_inited = true;
			return feedback;
		}
		return "";
	}
	

	/**
	 * Determine the file that will be used. 
	 * 
	 * First try to load the the file in the directory defined by the system property frame.datapath.</li> 
	 * Then try to load the file as a resource in the jar file.</li>  
	 * Finally, tries the default location (equivalent to -Dframe.datapath=./data/frame)</li>
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 */
	private static BufferedReader getReader(String file, String defaultDir) throws FileNotFoundException
	{
			InputStream in = null; 
			String dir = System.getProperty("frame.datapath");
			
			if (dir != null) {
				in = new FileInputStream(dir+"/"+file);
				if (in != null) {
					System.err.println("Info: Using frame directory defined in frame.datapath:"+dir);
					return new BufferedReader(new InputStreamReader(in));
				}
			}
			
			in = FrameProcessor.class.getResourceAsStream("/"+file);
			if (in != null) {
				System.err.println("Info: Using " + file +" from resource (jar file).");
				return new BufferedReader(new InputStreamReader(in));
			}
			
			String defaultFile = defaultDir+"/"+file;
			in = new FileInputStream(defaultFile);
			if (in != null) {
				System.err.println("Info: Using default " + defaultFile);
				return new BufferedReader(new InputStreamReader(in));
			}
			
			throw new RuntimeException("Error loading "+file+" file.");
	}
	
	/**
	 * Determine the frame file that will be used. 
	 * 
	 * First try to load the the file in the directory defined by the system property frame.datapath.</li> 
	 * Then try to load the file as a resource in the jar file.</li>  
	 * Finally, tries the default location (equivalent to -Dframe.datapath=./data/frame)</li>
	 * 
	 * @return
	 */
	private static String loadConceptVars()
	{
		try {
			String msg = "";
			StringBuilder fileStr = new StringBuilder();
			try {
				BufferedReader in = new BufferedReader(getReader(CONCEPT_VARS_FILE, CONCEPT_VARS_DIR));
				String line;
				while ((line = in.readLine()) != null) {
					// ignore comments
					int cmnt = line.indexOf(COMMENT_DELIM);
					if (-1 < cmnt)
					{
						line = line.substring(cmnt);
					}
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
				System.err.println("\nCONCEPT VAR LEGAL VALUES:");
				for (String key: conceptVarMap.keySet()) {
					System.err.println("\n" + key + ":");
					ArrayList<String> values = conceptVarMap.get(key);
					for (String value: values) {
						System.err.println("   - " + value);
					}
				}
			}
			return msg + "Frame concept variables have been loaded.\n";
		} catch (Exception e) {
			String msg = "Error processing Frame concept vars file - file not loaded";
			System.err.println(msg);
			System.err.println(e);
			e.printStackTrace();
			return "\n" + msg + "\n" + e;
		}
	}

	private static String loadMappingRules()
	{
		try {
			String relexRulesStr = "";
			try {
				BufferedReader in = new BufferedReader(getReader(MAPPING_RULES_FILE, MAPPING_RULES_DIR));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = in.readLine()) != null)
				{
					// ignore comments
					int cmnt = line.indexOf(COMMENT_DELIM);
					if (-1 < cmnt)
					{
						line = line.substring(cmnt);
					}
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
					System.err.println("**** IMPROPERLY FORMED RULE ENCOUNTERED at line " + i + ". ****\nRule: " + line.trim() + "\n");
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
						System.err.println("Warning: No definition found for concept variable " + varName);
					}
				}
			}

			// println("Frame mapping rules have been loaded.");
			return msg + "\nFrame mapping rules have been loaded.\n";
	 	} catch (Exception e) {
			String msg = "Error processing Frame mapping rules file - file not loaded";
			System.err.println(msg);
			System.err.println(e);
			e.printStackTrace();
			return "\n" + msg + "\n" + e;
		}
	}

} //end class Frame


