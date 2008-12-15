package relex.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.linkgrammar.Link;
import org.linkgrammar.Linkage;
import org.linkgrammar.ParseResult;

import relex.ParsedSentence;
import relex.Sentence;
import relex.feature.FeatureNode;
import relex.feature.LinkView;
import relex.feature.LinkableView;

public class RemoteLGParser extends LGParser
{
	private String parserVersion;
	private String hostname;
	private int port;
	private int parseRetryCount = 2;
	private int connectRetryCount = Integer.MAX_VALUE;
	private long connectRetryWait = 1000l;
	
	@SuppressWarnings("unchecked")
	private ParseResult jsonToParseResult(String json)
	{
		JSONReader reader = new JSONReader();
		Map top = (Map)reader.read(json);
		ParseResult result = new ParseResult();
		result.setParserVersion((String)top.get("version"));
		result.setWords(((List<String>)(top.get("tokens"))).toArray(new String[0]));
		boolean [] A = new boolean[result.getWords().length];
		for (Long idx : ((List<Long>)top.get("entity")))
			A[idx.intValue()] = true;
		result.setEntityFlags(A);
		A = new boolean[result.getWords().length];
		for (Long idx : ((List<Long>)top.get("pastTense")))
			A[idx.intValue()] = true;
		result.setPastTenseFlags(A);
		result.setNumSkippedWords(((Number)top.get("numSkippedWords")).intValue());
		for (Map x : (List<Map>)top.get("linkages"))
		{
			Linkage linkage = new Linkage();			
			linkage.setAndCost(((Number)x.get("andCost")).intValue());
			linkage.setDisjunctCost(((Number)x.get("disjunctCost")).intValue());
			linkage.setLinkCost(((Number)x.get("linkageCost")).intValue());
			linkage.setNumViolations(((Number)x.get("numViolations")).intValue());
			linkage.setWords(((List<String>)(x.get("words"))).toArray(new String[0]));
			linkage.setLinkedWordCount(linkage.getWords().length); // TODO?? is this right?
			for (Map y : (List<Map>)x.get("links"))
			{
				Link link = new Link();
				link.setLabel((String)y.get("label"));
				link.setLeftLabel((String)y.get("leftLabel"));
				link.setRightLabel((String)y.get("rightLabel"));
				link.setLeft(((Number)y.get("left")).intValue());
				link.setRight(((Number)y.get("right")).intValue());
				linkage.getLinks().add(link);
			}
			if (config.isStoreConstituentString())
				linkage.setConstituentString((String)x.get("constituentString"));
			result.getLinkages().add(linkage);
		}
		return result;
	}
	
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
	
	private String makeLGRequest(String text)
	{
		if (config != null)
			return "storeConstituentString:" + config.isStoreConstituentString() + "\0" +
				   "maxCost:" + config.getMaxCost() + "\0" +
				   "maxLinkages:" + config.getMaxLinkages() + "\0" +
				   "maxParseSeconds:" + config.getMaxParseSeconds() + "\0" +
				   "text:" + text + "\0";
		else
			return "text:" + text + "\0";
	}
	
	private String readResponse(Reader in) throws IOException
	{
		int length = 0;
		char [] buf = new char[1024];
		for (int count = in.read(buf, length, buf.length - length); 
			 count > -1; 
			 count = in.read(buf, length, buf.length - length))
		{
			length += count;
			if (length == buf.length)
			{
				char [] nbuf = new char[buf.length + 1024];
				System.arraycopy(buf, 0, nbuf, 0, buf.length);
				buf = nbuf;
			}	
			if (buf[length-1] == '\n')
				break;
		}		
		return new String(buf, 0, length);
	}
	
	private String callParser(String request) throws InterruptedException, IOException
	{
		if (hostname == null || hostname.length() == 0 || port <= 1024)
			throw new ParseException("No hostname for remote parser or invalid port number < 1024");
		
		//
		// Connect:
		//
		Socket socket = null;
		
		for (int i = 0; i < connectRetryCount && socket == null; i++)
		{
			try
			{
				socket = new Socket(hostname, port);
			}
			catch (UnknownHostException ex)
			{
				throw new ParseException("Host '" + hostname + "' not found.");
			}
			catch (IOException ex)
			{
				// ignore, retry...
			}
			if (socket == null)
				Thread.sleep(connectRetryWait);
		}
		
		if (socket == null)
			throw new ParseException("Failed to connect to " + hostname + ":" + port);
		
		//
		// Call parser:
		//
		PrintWriter out = null; 
		Reader in = null;
		try
		{
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new InputStreamReader(socket.getInputStream());			
			out.print(request);
			out.print('\n');
			out.flush();
			return readResponse(in);
		}
		finally
		{
			if (out != null) try { out.close(); } catch (Throwable t) { }
			if (in != null) try { in.close(); } catch (Throwable t) { }	
			try { socket.close(); } catch (Throwable t) { }
		}
	}
	
	private String parseRequest(String text) throws InterruptedException, IOException
	{
		return callParser(makeLGRequest(text));
	}
	
	public Sentence parse(String sentence)
	{
		try
		{
			String parserResponse = null;
			for (int i = 0; i < parseRetryCount && parserResponse == null; i++)
				try { parserResponse = parseRequest(sentence); }
				catch (IOException ex) { ex.printStackTrace(); /* retry... */}
			if (parserResponse == null)
				throw new ParseException("Failed to parse sentence " + sentence);
			ParseResult parseResult = jsonToParseResult(parserResponse);
			parseResult.setText(sentence);
			return parseResultToSentence(parseResult);
		}
		catch (InterruptedException ex)
		{
			throw new ParseException("Thread interrupted.", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public String getVersion()
	{
		if (parserVersion == null)
		{
			try
			{
				String json = callParser("get:version\0");
				JSONReader reader = new JSONReader();
				Map top = (Map)reader.read(json);
				parserVersion = (String)top.get("version");
			}
			catch (IOException ex)
			{
				parserVersion = "unavailable";
			}
			catch (InterruptedException ex)
			{
				throw new ParseException("Thread interrupted.", ex);
			}
		}
		return parserVersion;
	}
	
	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public int getParseRetryCount()
	{
		return parseRetryCount;
	}

	public void setParseRetryCount(int parseRetryCount)
	{
		this.parseRetryCount = parseRetryCount;
	}

	public int getConnectRetryCount()
	{
		return connectRetryCount;
	}

	public void setConnectRetryCount(int connectRetryCount)
	{
		this.connectRetryCount = connectRetryCount;
	}

	public long getConnectRetryWait()
	{
		return connectRetryWait;
	}

	public void setConnectRetryWait(long connectRetryWait)
	{
		this.connectRetryWait = connectRetryWait;
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
		parser.setHostname(hostPort[0].trim());
		parser.setPort(Integer.parseInt(hostPort[1].trim()));
		parser.getConfig().setAllowSkippedWords(true);
		parser.getConfig().setMaxLinkages(5);
		parser.parse(args[1]);
//		JSONReader reader = new JSONReader();
//		reader.read(
//		"{\"tokens\":[\"LEFT-WALL\",\"if\",\"the\",\"Certificate\",\"shows\",\"that\",\"your\",\"house\",\"is\", "+ "\"lower\",\",\",\"then\",\"the\",\"so\",\"called\",\"50\",\"%\",\"rule\",\"would\",\"apply\",\"to\",\"your\",\"house\",\".\",\"RIGHT-WALL\"], "+ "\"numSkippedWords\":2, "+ 
//		"\"entity\":[], "+ "\"pastTense\":[13,14], "+ "\"linkages\":[]}"
//		);
	}	
}