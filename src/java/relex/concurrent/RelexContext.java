package relex.concurrent;

import relex.morphy.Morphy;
import relex.parser.LinkParserClient;

public class RelexContext {
	private LinkParserClient linkParserClient; 
	private Morphy morphy;
	
	public LinkParserClient getLinkParserClient() {
		return linkParserClient;
	}

	public void setLinkParserClient(LinkParserClient linkParserClient) {
		this.linkParserClient = linkParserClient;
	}

	public Morphy getMorphy() {
		return morphy;
	}
	
	public void setMorphy(Morphy morphy) {
		this.morphy = morphy;
	}

	public RelexContext() {
	}

	public RelexContext(LinkParserClient lpc, Morphy morphy) {
		super();
		this.linkParserClient= lpc;
		this.morphy = morphy;
	}
	
}
