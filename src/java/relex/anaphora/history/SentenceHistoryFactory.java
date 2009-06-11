package relex.anaphora.history;

/**
 * 
 * Factory to create the SentenceHistory object
 * 
 * @author fabricio <fabricio@vettalabs.com>
 *
 */
public class SentenceHistoryFactory 
{
	public enum HistoryEnum 
	{
		DEFAULT,
		TIMESTAMP	
	}
	
	public static SentenceHistory create(HistoryEnum historyType)
	{
		switch(historyType)
		{
			case TIMESTAMP:
				return new TimestampSentenceHistory();
			case DEFAULT:
				return new DefaultSentenceHistory();
		}
		return new DefaultSentenceHistory();
	}	
}
