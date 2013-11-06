package relex.morphy;

public interface Morphy {

	public static String NOUN_F = "noun";
	public static String VERB_F = "verb";
	public static String ADJ_F = "adj";
	public static String ADV_F = "adv";
	public static String ROOT_F = "root";
	public static String TYPE_F = "type";
	public static String NEG_F = "neg";

	public abstract void initialize();

	public abstract Morphed morph(String word);

}
