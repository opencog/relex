package relex.morphy;

/**
 *
 * <p>
 * A stemmer will produce one or more variants of possible root forms of a
 * given word.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public interface Stemmer
{
	Iterable<String> stemIt(String word);
}
