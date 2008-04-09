package relex.morphy;

public class MorphyFactory {

	public static final String MORPHY_IMPLEMENTATION_PROPERTY = "relex.morphy.Morphy";
	public static final String DEFAULT_SINGLE_THREAD_IMPLEMENTATION = "relex.morphy.MorphyJWNL";
	public static final String DEFAULT_MULTI_THREAD_IMPLEMENTATION = "relex.morphy.MapMorphy";

	/**
	 * Obtains a Morphy instance. If the system property MORPHY_IMPLEMENTATION_PROPERTY
	 * is defined, try to instantiate the class specified by it; if not, uses
	 * the given class name.  
	 * 
	 * @param defaultImplementation
	 * @return
	 */
	public static Morphy getImplementation(String defaultImplementation)	{
		Morphy instance = null; 
		String implementationClassname = System.getProperty(MORPHY_IMPLEMENTATION_PROPERTY);
			if (implementationClassname == null) implementationClassname = defaultImplementation;
			try {
				Class<?> cl = Class.forName(implementationClassname);
				instance = (Morphy)cl.newInstance();
				instance.initialize();
			}
			catch (Exception ex) {
				throw new RuntimeException("Unable to initialize Morphy algorithm:" + ex.toString(),ex);
			}
		return instance;
	}
	
	/**
	 * By default returns the thread-safe Morphy, if there's no system property defined. 
	 * @return
	 */
	public static Morphy getImplementation(){
		return getImplementation(DEFAULT_MULTI_THREAD_IMPLEMENTATION);
	}
}
