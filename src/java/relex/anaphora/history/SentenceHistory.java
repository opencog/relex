package relex.anaphora.history;

import java.util.ArrayList;
import relex.Sentence;

/**
 * 
 * Interface used as the sentences history for Hobbs
 * 
 * @author fabricio <fabricio@vettalabs.com>
 *
 */
public interface SentenceHistory 
{
	public void addSentence(Sentence sentence);
	public ArrayList<Sentence> getSentenceList();
}
