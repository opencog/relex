package relex.morphy;

public interface Morphy
{
	public static final String NOUN_F = "noun";
	public static final String VERB_F = "verb";
	public static final String ADJ_F = "adj";
	public static final String ADV_F = "adv";
	public static final String ROOT_F = "root";
	public static final String TYPE_F = "type";
	public static final String NEG_F = "neg";

	public abstract void initialize();

	public abstract Morphed morph(String word);
}
