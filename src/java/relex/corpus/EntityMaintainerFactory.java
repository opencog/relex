package relex.corpus;

import relex.entity.EntityMaintainer;

/**
 * 
 * <p>
 * An <code>EntityMaintainerFactory</code> implementation is responsible for
 * detecting named entities in a sentence and producing an instance
 * of {@link EntityMaintainer}.
 * </p>
 *
 * <p>
 * This class also serves as a factory of a globally configured <code>EntityMaintainerFactory</code>
 * through the <code>relex.corpus.EntityMaintainerFactory</code> system property.  The default
 * value of this property is <code>relex.corpus.GateEntityDetector</code>. This globally
 * configured <code>EntityMaintainerFactory</code> factory is created only once and
 * maintained as a singleton within this class;
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public abstract class EntityMaintainerFactory
{	
	public static final String DEFAULT_FACTORY = "relex.corpus.GateEntityDetector";
	
	/**
	 * <p>
	 * Detect named entities in the string <code>sentence</code> and return
	 * an <code>EntityMaintainer</code>.
	 * </p>
	 */
	public abstract EntityMaintainer makeEntityMaintainer(String sentence);
	
	private static EntityMaintainerFactory factory = null;
	
	public static synchronized EntityMaintainerFactory get()
	{
		if (factory != null)
			return factory;
		
		String classname = System.getProperty("relex.corpus.EntityMaintainerFactory");
		if (classname == null)
			classname = DEFAULT_FACTORY;
		try
		{
			Class<?> cl = Class.forName(classname);
			return factory = (EntityMaintainerFactory)cl.newInstance();				
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}