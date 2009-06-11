package relex.anaphora.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import relex.Sentence;

/**
 * 
 * History that keeps in the buffer just the recent sentences.
 * By recent, we mean that the sentence must be at most max_minutes
 * old.
 * 
 * @author fabricio <fabricio@vettalabs.com>
 *
 */

public class TimestampSentenceHistory implements SentenceHistory 
{

	ArrayList<TimestampSentence> sentences=null;
	//how minutes old a sentence must be to be valid?
	//TODO accept a new value of max_minutes from JVM parameter
	private int max_minutes = 20;
		
	public TimestampSentenceHistory()
	{
		sentences = new ArrayList<TimestampSentence>();
	}
	
	@Override
	public void addSentence(Sentence sentence) 
	{
		TimestampSentence element = new TimestampSentence();
		element.setSentence(sentence);
		element.setTimestamp(new Date());
		sentences.add(element);
	}

	@Override
	public ArrayList<Sentence> getSentenceList() 
	{
		//convert minutes to ms
		long maxMinutesInMillis = (max_minutes * 60) * 1000;
		long currentTimeInMillis = new Date().getTime();
			
		//TODO find a way to remove a sublist of the sentences list.
		//So, after found a item that must be removed, if the list is
		//sorted, all subsequent itens must be removed either
		//Collections.sort(sentences);//desc order
		
		ArrayList<Sentence> list = new ArrayList<Sentence>();
		Iterator<TimestampSentence> iter=sentences.iterator();
		while(iter.hasNext()){
			TimestampSentence t = iter.next();
			//if the sentence is older than the allowed, remove it from the history
			if( (currentTimeInMillis - t.getTimestamp().getTime()) > maxMinutesInMillis){
				iter.remove();
			}else{//otherwise, add the sentence to the list
				System.out.println("Accepted Sentence Date: "+
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getTimestamp()));
				list.add(t.getSentence());
			}
		}
		return list;
	}
	
	public void setMaxMinutes(int minutes)
	{
		this.max_minutes=minutes;
	}
}
