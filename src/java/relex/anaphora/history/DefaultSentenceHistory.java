package relex.anaphora.history;

import java.util.ArrayList;
import relex.Sentence;

/**
 * 
 * Sentence history that keeps just a max number of sentences
 * in the buffer.
 * 
 * @author fabricio <fabricio@vettalabs.com>
 *
 */
public class DefaultSentenceHistory implements SentenceHistory 
{

	ArrayList<Sentence> sentences=null;
	// The max number of sentences to keep in the history buffer.
	private static int max_sentences = 20;
	
	public DefaultSentenceHistory()
	{
		sentences = new ArrayList<Sentence>();
	}
	
	@Override
	public void addSentence(Sentence sentence) 
	{
		sentences.add(0, sentence);
		int size = sentences.size();
		if (size > max_sentences) sentences.remove(max_sentences);
	}

	@Override
	public ArrayList<Sentence> getSentenceList() 
	{
		return sentences;
	}
}
