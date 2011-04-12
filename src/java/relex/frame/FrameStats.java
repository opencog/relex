/*
 * Copyright (C) 2011 Joel Pitt
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

package relex.frame;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;

/**
 * Main class--Converts RelEx output from Compact Output xml to Frame relationships
 * Keeps stats on what frame rules are used.
 */
public class FrameStats extends FrameProcessor implements ContentHandler
{
	static boolean VERBOSE = false;

	private static final String MAPPING_RULES_DIR = "data/frame";
	private static final String MAPPING_RULES_FILE = "mapping_rules.txt";
	private static final String CONCEPT_VARS_FILE = "concept_vars.txt";

    // Line number -> rule application count
    public HashMap<Integer, Integer> ruleCount = new HashMap<Integer,Integer>();
    public HashMap<Integer, String > ruleExampleSentences = new HashMap<Integer,String >();
    public HashMap<Integer, VarMapList > ruleExampleSentenceVarMap = new HashMap<Integer,VarMapList>();
    public HashMap<Integer, Rule > lineToRule = new HashMap<Integer,Rule>();

    // Try to find example sentences that are close to this length. To short
    // and the examples can get cryptic, too long and it's hard to decipher.
    public int idealSentenceLength = 30;

	public FrameStats()
	{
		set_data_files(MAPPING_RULES_DIR, CONCEPT_VARS_FILE, MAPPING_RULES_FILE);
	}

    public void startDocument() throws SAXException {}
    public void endDocument() throws SAXException {}
    public void startElement() throws SAXException {}

    int sentencesProcessed = 0;
    int parsesProcessed = 0;
    String currentSentence = "";
    String relexText = "";
    String elementText = "";
    int parseID = 0;

    public void startElement(String uri, String _name, String qName, Attributes atts)
    {
        String name = qName;
        // set up to receive data
        if ("sentence".equals(name)) {
            elementText = "";
            currentSentence = "";
        } else if ("parse".equals(name) && atts.getValue("id").equals("1")) {
            currentSentence = elementText.trim();
        } else if ("parse".equals(name)) {
            elementText = "";
            parseID = Integer.parseInt(atts.getValue("id"));
        } else if ("relations".equals(name)) {
            elementText = "";
            relexText = "";
        }
    }

    public void endElement(String uri, String _name, String qName)
    {
        String name = qName;
        if ("sentence".equals(name)) {
            // because some sentences have no parse
            currentSentence = elementText.trim();
            sentencesProcessed += 1;
            /*if (sentencesProcessed % 100 == 0)  {
                System.err.print(".");
                System.err.flush();
            }*/
        } else if ("parse".equals(name)) {
            parsesProcessed += 1;
        } else if ("relations".equals(name)) {
            //System.err.println("The currentSentence is: " + currentSentence);
            relexText = elementText.replaceAll("\\[\\d+\\]","");
            //relexText = elementText;
			//System.err.println("relexText:" + relexText);
            // apply relex2frame to buffer
            /*System.err.println("frames:");
            String[] result=process(relexText);
            for (String s: result) {
                System.err.println(s);
            }*/
            process(relexText);
            // access fireRules to see which ones were applied and update
            // counters
            for ( Rule r : fireRules.keySet() ) {
                if (ruleCount.containsKey(r.lineno) ) {
                    ruleCount.put(r.lineno,ruleCount.get(r.lineno)+1); 
                } else {
                    ruleCount.put(r.lineno,1); 
                }
                // replace example sentence if the current one is shorter
                String existing = "";
                if (ruleExampleSentences.containsKey(r.lineno)){
                    existing = ruleExampleSentences.get(r.lineno);
                }
                if (existing.length() == 0 || 
                        Math.abs(currentSentence.length() - idealSentenceLength) <
                        Math.abs(existing.length() - idealSentenceLength)) {
                    existing=currentSentence;
                    ruleExampleSentences.put(r.lineno,existing);
                    ruleExampleSentenceVarMap.put(r.lineno,fireRules.get(r));
                }
            }
        }

    }

    public void characters(char[] ch, int start, int length)
    {
        elementText += String.copyValueOf(ch,start,length);
    }

    public void skippedEntity(String s) {}
    public void endPrefixMapping(String prefix) {}
    public void ignorableWhitespace(char[] ch, int start, int length) {}
    public void processingInstruction(String target, String data) {}
    public void setDocumentLocator(Locator locator) {}
    public void startPrefixMapping(String prefix, String uri) {}

    private static String convertToFileURL(String path2, String filename) {
        String path = new File(path2,filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

	/**
	 * Load a filename in concise xml format and apply relex2frame, then print
     * stats of rules used
	 */
	public static void main(String args[]) throws Exception 
	{
		FrameStats fr = new FrameStats();
		String verbose = System.getProperty("verbose");
		if (verbose!=null && verbose.equals("true")) { Frame.VERBOSE = true; }

        // build map from lineno to rule object
        for (Rule r : rules) {
            fr.lineToRule.put(r.lineno, r);
        }

		for (int i=0; i < args.length; i++) {
            String filename = args[i];
            File dir = new File(args[i]);

            // Only get xml files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };

            String[] children = dir.list(filter);
            if (children == null) {
                // process a single file
                fr.processFile(".",filename);
            } else {
                System.out.println("Queuing " + children.length + " files");
                // process all files in directory
                long start = System.currentTimeMillis();
                for (int j=0; j<children.length; j++) {
                    System.err.print("[" + j + "/"+ children.length + "] ");
                    fr.processFile(filename,children[j]);
                    if (j % 20 == 0) {
                        long timeElapsed = System.currentTimeMillis() - start;
                        float timePerDoc = timeElapsed / (float) j;
                        long timeRemaining = (long) (timePerDoc * (children.length - j));
                        long timeRemainingMinutes = (timeRemaining / 1000) /60;
                        long timeRemainingHours = timeRemainingMinutes/60;
                        timeRemainingMinutes -= timeRemainingHours * 60;
                        System.err.println("Estimated time remaining " + timeRemainingHours + ":"+ timeRemainingMinutes);
                    }
                    //if (j == 200) j = children.length;
                }
            }
        }

        System.err.println("Sentences processed=" + fr.sentencesProcessed);
        System.err.println("Parses processed=" + fr.parsesProcessed);

        // dump new mapping rules file with counts and examples sentences
        fr.dumpNewMappingWithFrequencies();

	}

    public void processFile(String path, String filename) throws Exception 
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(false);
        SAXParser saxParser = spf.newSAXParser();
        System.err.println(" Processing file " + filename);
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        try {
            xmlReader.parse(convertToFileURL(path,filename));
	 	} catch (Exception e) {
			//String msg = "Parse error in file " + filename;
			//System.err.println(msg);
			//System.err.println(e);
            elementText = "";
            currentSentence = "";
			return;
		}
    }

    public void dumpNewMappingWithFrequencies()
    {
		try {
            String msg = "";
            File outf = new File("new_mapping_rules.txt");
            FileWriter fw = new FileWriter(outf);
            String lastExample = "";
            int exampleMatches = 0;
            // sort the r2f rules by frequency
            Map m = sortByValue(ruleCount);

            for (Object o : m.keySet()) {
                Integer lineno = (Integer) o;
                Rule r = lineToRule.get(lineno);

                String ruleline = r.ruleStr;

                // save the original line number so rule can easily be
                // found in original file)
                fw.write(";; line: " + lineno + "\n");
                // save count
                if (ruleCount.containsKey(lineno)){
                    fw.write(";; count: " + ruleCount.get(lineno) + "\n");
                } else {
                    fw.write(" # count: 0\n");
                }

                // save example sentence
                if (ruleExampleSentences.containsKey(r.lineno)){
                    String example = ruleExampleSentences.get(r.lineno);
                    fw.write(";; example: " + example + "\n");
                    for (VarMap varMapList : ruleExampleSentenceVarMap.get(r.lineno) ) {
                        StringBuffer s = varMapList.print();
                        fw.write(";; " + s);
                    }
                }
                // save var mappings
                // save rule 
                fw.write("# " + ruleline.trim());
                fw.write("\n");
            }
            for (Rule r : rules) {
                // dump rules that haven't been applied at the end
                if (!ruleCount.containsKey(r.lineno)) {
                    fw.write(";; line: " + r.lineno + "\n");
                    fw.write(";; count: 0\n");
                    String ruleline = r.ruleStr;
                    fw.write("# " + ruleline.trim());
                    fw.write("\n");
                }
            }
            //in.close();
            fw.close();
	 	} catch (Exception e) {
			String msg = "Error";
			System.err.println(msg);
			System.err.println(e);
			e.printStackTrace();
			return;
		}
    }

    static Map sortByValue(Map map) {
         List list = new LinkedList(map.entrySet());
         Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                   return ((Comparable) ((Map.Entry) (o2)).getValue())
                  .compareTo(((Map.Entry) (o1)).getValue());
              }
         });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
} //end class FrameStats


