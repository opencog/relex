/*
 * Copyright 2009 Borislav Iordanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.parser;

import java.util.HashMap;

import org.linkgrammar.*;

import relex.ParsedSentence;
import relex.Sentence;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

public class RemoteLGParser extends LGParser
{
    private LGRemoteClient linkGrammarClient = new LGRemoteClient();
	
	private Sentence parseResultToSentence(ParseResult parseResult)
	{
		Sentence sntc = new Sentence();		
		sntc.setSentence(parseResult.getText());
		
		if (parseResult.getLinkages().isEmpty() || 
			!config.isAllowSkippedWords() && parseResult.getNumSkippedWords() > 0)
			return sntc;
		
		String lowerCaseText = parseResult.getText().toLowerCase();
		
		for (Linkage linkage : parseResult)
		{
			ParsedSentence parsedSentence = new ParsedSentence(parseResult.getText());
			parsedSentence.setSentence(sntc);
			sntc.getParses().add(parsedSentence);
			
			boolean ignoreLast = false;
			FeatureNode lastFN = null;
			FeatureNode leftWall = null;
			
			int startChar = 0;
			HashMap<String,Integer> timesTokenSeen = new HashMap<String,Integer>();
			for (int w = 0; w < linkage.getLinkedWordCount(); w++)
			{
				String wordString = linkage.wordAt(w);

				if (wordString.equals("RIGHT-WALL"))
				{
					ignoreLast = true;
				}
				else
				{
					LinkableView fnv = new LinkableView(new FeatureNode());
					if (wordString.equals("LEFT-WALL")) leftWall = fnv.fn();
					// LEFT-WALL should always be first word, so throw an 
					// exception if it was not.
					if (leftWall == null)
						throw new RuntimeException("Invalid parse: " +
							"first word is not left wall");
					
					// set the word and part-of-speach
					fnv.setWordAndPos(wordString);
					
					// create a feature "this" which points to the linkable
					fnv.fn().set("this", fnv.fn());
					
					// set "wall" to point to the left wall
					fnv.fn().set("wall", leftWall);
					if (lastFN != null)
					{
						LinkableView.setNext(lastFN, fnv.fn());
						fnv.setPrev(lastFN);
					}
					
					if (parseResult.getEntityFlags()[w] || Character.isUpperCase(wordString.charAt(0)))
						fnv.setEntityFlag();
					if (parseResult.getPastTenseFlags()[w])
						fnv.setTenseVal("past");
					
					parsedSentence.addWord(fnv.fn());

					//
					// Add char-index information to the feature node
					//
					// Boris: I don't quite understand this code which is copied over previous
					// off this class. The loop is over all words in the current linkage, but
					// the token is obtained from a global array of all tokens in the sentence
					// (regardless of whether they participate in the current parse or are skipped).
					// And anyway, in case of skipped words, how are you going to get the 
					// character index right? Need to know which words exactly have been skipped.
					String tokenString = parseResult.wordAt(w).toLowerCase(); // normalize cases
					
					Integer timesSeenInt = timesTokenSeen.get(tokenString);
					int timesSeen = (timesSeenInt == null ? 0 : timesSeenInt.intValue());

					// "x<=" means we will do at least once
					for (int x = 0; x <= timesSeen; x++)
					{
						startChar = lowerCaseText.indexOf(tokenString,startChar);
					}

					timesTokenSeen.put(tokenString, new Integer(timesSeen + 1));
					int endChar = (startChar >= 0 ? startChar + tokenString.length() : -1);
					// System.out.println("INFO IS " + startChar + "," + endChar);
					fnv.setCharIndices(startChar, endChar, w);

					// Increment index to start looking for next tokenString
					// after the current one. Use "max" to prevent decreasing
					// index in the case the tokenString end is -1
					startChar = Math.max(startChar, endChar);
					lastFN = fnv.fn();
				}
			}
			
			for (Link link : linkage)
				if (!ignoreLast || link.getRight() != linkage.getLinkedWordCount() - 1)
				{
					new LinkView(new FeatureNode()).setLinkFeatures(
							link.getLeftLabel(), 
							link.getRightLabel(), 
							link.getLabel(), 
							parsedSentence.getWordAsNode(link.getLeft()), 
							parsedSentence.getWordAsNode(link.getRight())
					);
				}
			
			parsedSentence.setPhraseString(linkage.getConstituentString());
			
			// set meta data
			FeatureNode meta = new FeatureNode();
			meta.set("num_skipped_words", new FeatureNode(Integer.toString(parseResult.getNumSkippedWords())));
			meta.set("and_cost", new FeatureNode(Integer.toString(linkage.getAndCost())));
			meta.set("disjunct_cost", new FeatureNode(Integer.toString(linkage.getDisjunctCost())));
			meta.set("link_cost", new FeatureNode(Integer.toString(linkage.getLinkCost())));
			meta.set("num_violations", new FeatureNode(Integer.toString(linkage.getNumViolations())));
			parsedSentence.setMetaData(meta);			
		}
		return sntc;		
	}
	
	// @Override
	public Sentence parse(String sentence)
	{
	    try
	    {
	        ParseResult parseResult = linkGrammarClient.parse(sentence);
			parseResult.setText(sentence);
			return parseResultToSentence(parseResult);
		} 
		catch (InterruptedException ex)
		{
			throw new ParseException("Thread interrupted.", ex);
		}
		catch (Throwable t)
		{
		    throw new ParseException(sentence, t);
		}
	}
		
    @Override
    public String getVersion()
    {
        return getLinkGrammarClient().getVersion();
    }
    
	public LGRemoteClient getLinkGrammarClient()
    {
        return linkGrammarClient;
    }

    public void setLinkGrammarClient(LGRemoteClient linkGrammarClient)
    {
        this.linkGrammarClient = linkGrammarClient;
    }

    public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println("Syntax: RemoteLGParser host:port sentence");
			System.exit(-1);
		}
		String [] hostPort = args[0].split(":");
		RemoteLGParser parser = new RemoteLGParser();
		parser.getLinkGrammarClient().setHostname(hostPort[0].trim());
		parser.getLinkGrammarClient().setPort(Integer.parseInt(hostPort[1].trim()));
		parser.getLinkGrammarClient().getConfig().setAllowSkippedWords(true);
		parser.getLinkGrammarClient().getConfig().setMaxLinkages(5);
		parser.parse(args[1]);
	}	
}
