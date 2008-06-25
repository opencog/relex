package relex.morphy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.didion.jwnl.JWNL;
import relex.frame.Frame;

public class MorphyFactory
{
	public static final String MORPHY_IMPLEMENTATION_PROPERTY = "relex.morphy.Morphy";
	public static final String DEFAULT_SINGLE_THREAD_IMPLEMENTATION = "relex.morphy.MorphyJWNL";
	public static final String DEFAULT_MULTI_THREAD_IMPLEMENTATION = "relex.morphy.MapMorphy";

	private static final String WORDNET_PROPERTY = "wordnet.configfile";
	private static final String JWNL_FILE_PROPERTIES_XML = "file_properties.xml";
	private static final String JWNL_DIR_PROPERTIES_XML = "./data/wordnet";

	/**
	 * Obtains a Morphy instance. If the system property 
	 * MORPHY_IMPLEMENTATION_PROPERTY is defined, try to instantiate
	 * the class specified by it; if not, uses the given class name.  
	 * 
	 * @param defaultImplementation
	 * @return
	 */
	public static Morphy getImplementation(String defaultImplementation)
	{
		Morphy instance = null; 
		String implementationClassname = 
			System.getProperty(MORPHY_IMPLEMENTATION_PROPERTY);
		if (implementationClassname == null)
			implementationClassname = defaultImplementation;
		try
		{
			Class<?> cl = Class.forName(implementationClassname);
			instance = (Morphy)cl.newInstance();
			instance.initialize();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(
				"Error: Unable to initialize Morphy algorithm:" + 
				ex.toString(),ex);
		}
		return instance;
	}
	
	/**
	 * By default returns the thread-safe Morphy, if there's no
	 * system property defined. 
	 * @return
	 */
	public static Morphy getImplementation()
	{
		return getImplementation(DEFAULT_MULTI_THREAD_IMPLEMENTATION);
	}
	
	/**
	 * Used by JWNL-based Morphy implementations. 
	 * @return 
	 */
	public static boolean initializeJWNL()
	{
		try
		{
			JWNL.initialize(
					getJWNLConfigFileStream(
							WORDNET_PROPERTY, 
							JWNL_FILE_PROPERTIES_XML, 
							JWNL_DIR_PROPERTIES_XML
					)
			);
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	/**
	 * Determine the file that will be used. 
	 * 
	 * First try to load the the file in the directory defined by 
	 * the system property. Then try to load the file as a resource
	 * in the jar file. Finally, tries the default location 
	 * (equivalent to -Dproperty=default)
	 *
	 * @param propertyName TODO
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 */
	private static InputStream getJWNLConfigFileStream (
		            String propertyName,
	               String file,
	               String defaultDir)
	throws FileNotFoundException
	{
			InputStream in = null; 
			String property = System.getProperty(propertyName);
			
			if (property != null)
			{
				in = new FileInputStream(property);
				if (in != null)
				{
					System.err.println("Info: Using file defined in " +
						propertyName + ":" + property);
					return in;
				}
			}
			
			in = Frame.class.getResourceAsStream("/" + file);
			if (in != null)
			{
				System.err.println("Info: Using " + file +" from resource (jar file).");
				return in;
			}
			
			String defaultFile = defaultDir+"/"+file;
			in = new FileInputStream(defaultFile);
			if (in != null)
			{
				System.err.println("Info: Using default "+ defaultFile);
				return in;
			}
			throw new RuntimeException("Error loading " + file + " file.");
	}
}
