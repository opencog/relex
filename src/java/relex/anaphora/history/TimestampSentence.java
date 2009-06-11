package relex.anaphora.history;

import java.util.Date;
import relex.Sentence;

/**
 * Class that contains the timestamp of the sentence in order to
 * select the sentences according to it. It is used by
 * TimestampSentenceHistory.
 * 
 * @author fabricio <fabricio@vettalabs.com>
 */
public class TimestampSentence implements Comparable<TimestampSentence> 
{
	private Date timestamp;
	private Sentence sentence;
	
	public Date getTimestamp() 
	{
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) 
	{
		this.timestamp = timestamp;
	}
	
	public Sentence getSentence() 
	{
		return sentence;
	}
	
	public void setSentence(Sentence sentence) 
	{
		this.sentence = sentence;
	}
	
	/*
	  Use compareTo method of java Date class to compare two date objects.
	  compareTo returns value grater than 0 if first date is after another date,
	  returns value less than 0 if first date is before another date and returns
	  0 if both dates are equal.
	 */
	public int compareTo(TimestampSentence o) 
	{
		return this.getTimestamp().compareTo(o.getTimestamp()); 
	}	
}
