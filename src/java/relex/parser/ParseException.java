package relex.parser;

/**
 * 
 * <p>
 * Represents an unrecoverable error during parsing. Such exceptions are caused
 * by unproper configuration, a bug, a failure to access some resource such as a
 * remote server etc. 
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class ParseException extends RuntimeException
{
	private static final long serialVersionUID = -1;
	
	public ParseException(String msg) { super(msg); }
	public ParseException(String msg, Throwable cause) { super(msg, cause); }
}