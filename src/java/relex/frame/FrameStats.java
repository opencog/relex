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
    public HashMap<Integer, ArrayList<String> > ruleExampleSentences = new HashMap<Integer,ArrayList<String> >();

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
            if (sentencesProcessed % 100 == 0)  {
                System.err.print(".");
                System.err.flush();
            }
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
                if (ruleExampleSentences.containsKey(r.lineno) ) {
                    ruleExampleSentences.get(r.lineno).add(currentSentence); 
                } else {
                    ArrayList<String> examples = new ArrayList<String>();
                    examples.add(currentSentence);
                    ruleExampleSentences.put(r.lineno,examples); 
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

    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
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
        String filename = args[0];

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(false);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(fr);
        xmlReader.parse(convertToFileURL(filename));

        // dump new mapping rules file with counts and examples sentences
		/*for (Rule r: rules) {
            r.printout();
            System.err.println("was used " + fr.ruleCount.get(r.lineno) + " times.\n");
        }*/

        System.err.println("Sentences processed=" + fr.sentencesProcessed);
        System.err.println("Parses processed=" + fr.parsesProcessed);

        fr.dumpNewMappingWithFrequencies();

	}

    public void dumpNewMappingWithFrequencies()
    {
		try {
            int lineno = 0;
            String msg = "";
            File outf = new File("new_mapping_rules.txt");
            FileWriter fw = new FileWriter(outf);
            String lastExample = "";
            int exampleMatches = 0;
			try {
				BufferedReader in = new BufferedReader(getReader(MAPPING_RULES_FILE, MAPPING_RULES_DIR));
				String line;
				while ((line = in.readLine()) != null)
				{
                    lineno++;
					// ignore comments
					int cmnt = line.indexOf(COMMENT_DELIM);
					if (-1 < cmnt) {
                        fw.write(line + "\n");
                        continue;
                    }
                    String[] relexRule = line.split("#");
                    if (relexRule.length < 2) {
                        fw.write(line + "\n");
                        continue;
                    }
                    //process rules - store in Rule objects
                    String ruleline = relexRule[1];
                    if (ruleExampleSentences.containsKey(lineno)){
                        ArrayList<String> examples = ruleExampleSentences.get(lineno);
                        String shortest = "";
                        for (String s: examples) {
                            if (shortest.length() == 0 || shortest.length() > s.length())
                                shortest=s;
                        }
                        //if (!shortest.equals(lastExample)) {
                        fw.write(";; example: " + shortest + "\n");
                            //lastExample = shortest;
                        //}
                    }

                    fw.write(line.trim());
                    if (ruleCount.containsKey(lineno)){
                        fw.write(" # " + ruleCount.get(lineno));
                    }
                    fw.write("\n");
                }
				in.close();
			} catch (IOException e) {
				msg = "Error";
				System.err.println(msg);
				System.err.println(e);
                return;
			}
            fw.close();
	 	} catch (Exception e) {
			String msg = "Error";
			System.err.println(msg);
			System.err.println(e);
			e.printStackTrace();
			return;
		}
    }
} //end class FrameStats


