package relex.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceUtils
{

    private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

    /**
     * Finds the file location and returns an input stream to it.
     * <p>
     * First try to load the the file in the directory defined by
     * the system property. Then try to load the file as a resource
     * in the jar file. Finally, tries the default location
     * (equivalent to -Dproperty="defaultDir/fileName")
     *
     * @param propertyName the property to read the file from
     * @param fileName     file to read
     * @param defaultDir   default directory where to look at the file
     * @return input stream to the requested file
     * @throws RuntimeException in case the file has not been found.
     */

    public static InputStream getResource(String propertyName,
                                          String fileName,
                                          String defaultDir)
    {
        try {
            String filePath = System.getProperty(propertyName);
            InputStream in = loadFromFile(filePath);
            if (in != null) {
                logger.info("Info: Loading resource from file name: {}," +
                        " defined by Java property: {}", filePath, propertyName);
                return in;
            }

            in = ResourceUtils.class.getResourceAsStream(
                    "/" + fileName);
            if (in != null) {
                logger.info("Info: Loading resource {} from jar.",
                        fileName);
                return in;
            }

            File defaultRelexPath = new File(defaultDir, fileName);
            in = loadFromFile(defaultRelexPath);
            if (in != null) {
                logger.info("Info: Loading resource from file {}/{}.",
                        defaultDir, fileName);
                return in;
            }

            throw new RuntimeException("Error loading " + fileName + " file.");
        } catch (FileNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }


    private static InputStream loadFromFile(String file) throws FileNotFoundException
    {
        return file != null ? loadFromFile(new File(file)) : null;

    }

    private static InputStream loadFromFile(File filePath) throws FileNotFoundException
    {
        return filePath.exists() ? new FileInputStream(filePath) : null;

    }
}
