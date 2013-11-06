package relex.corpus;

import java.io.Serializable;

/* This class is not intedned for general public use */
public final class TextInterval implements Serializable
{
	private static final long serialVersionUID = -7788249983906665037L;

	private int start, end;
	
	public static final TextInterval NULL = new TextInterval(0,0);
	
	public TextInterval()
	{		
	}
	
	public TextInterval(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	
	public void setStart(int start)
	{
		this.start = start;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public void setEnd(int end)
	{
		this.end = end;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public int hashCode()
	{
		return (int)(start ^ end);
	}
	
	public boolean equals(Object other)
	{
		if (! (other instanceof TextInterval))
			return false;
		TextInterval i = (TextInterval)other;
		return i.start == start && i.end == end;
	}
	
	public String toString(){
		return "("+start+", "+end+")";
	}
}
