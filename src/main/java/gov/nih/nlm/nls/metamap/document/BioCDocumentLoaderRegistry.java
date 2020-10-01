//
package gov.nih.nlm.nls.metamap.document;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * list document loaders in properties file in the following format:
 * <pre>
 * bioc.document.loader.{name}: classname
 * </pre>
 */
public class BioCDocumentLoaderRegistry {
  /** logger instance */
  private static Logger logger = LoggerFactory.getLogger(BioCDocumentLoaderRegistry.class);
  /** Map of BioC document loader by document type name */
  static final Map<String,BioCDocumentLoader> bioCDocumentLoaderMap = new HashMap<String,BioCDocumentLoader>();
  /** Map of document loader description by document type name */
  static final Map<String,String> descriptionMap = new HashMap<String,String>();

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param className full classname of loader
   * @throws ClassNotFoundException class not found exception
   * @throws InstantiationException exception during class instantiation
   * @throws NoSuchMethodException no such method exception
   * @throws IllegalAccessException illegal access exception
   */
  public static void register(String name, String description, 
			      String className)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException
 {
    Object classInstance = Class.forName(className).newInstance();
    if (classInstance instanceof BioCDocumentLoader) {
      BioCDocumentLoader instance = (BioCDocumentLoader)classInstance;
      synchronized(bioCDocumentLoaderMap) {
	bioCDocumentLoaderMap.put(name,instance);
      }
      synchronized(descriptionMap) {
	descriptionMap.put(name,description);
      }
    } else {
      logger.error("class " + className +
		   " for document input type " + name +
		   " is not of class BioCDocumentLoader.  Not adding entry for input type " + name);
      throw new RuntimeException("Class instance does not implement the BioCDocumentLoader interface.");
    }
  }

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param instance class instance of document loader
   */
  public static void register(String name, String description, 
			      BioCDocumentLoader instance)
  {
    synchronized(bioCDocumentLoaderMap) {
      bioCDocumentLoaderMap.put(name, instance);
    }
    synchronized(descriptionMap) {
      descriptionMap.put(name,description);
    }
  }

  public static Set<String> listNameSet() {
    return bioCDocumentLoaderMap.keySet();
  }

  public static List<String> listInfo() {
    List<String> descriptionList = new ArrayList<String>();
    for (Map.Entry<String,BioCDocumentLoader> entry: bioCDocumentLoaderMap.entrySet()) {
      descriptionList.add(entry.getKey() + ": " + entry.getValue());
    }
    return descriptionList;
  }

  public static BioCDocumentLoader getDocumentLoader(String name) {
    return bioCDocumentLoaderMap.get(name);
  }
  
  public static boolean contains(String name) {
    return bioCDocumentLoaderMap.containsKey(name);
  }

  public static BioCDocumentLoader get(String name) {
    return bioCDocumentLoaderMap.get(name);
  }

  public static void register(Properties properties) 
     throws ClassNotFoundException, InstantiationException, NoSuchMethodException,IllegalAccessException
  {
    for (String propname: properties.stringPropertyNames()) {
      if (propname.indexOf("bioc.document.loader.") >= 0) {
	String name = propname.substring("bioc.document.loader.".length());
	String description = properties.getProperty("bioc.document.loader-description.", "");
	register(name, description, properties.getProperty(propname));
      }
    }
    if (logger.isDebugEnabled()) {
      for (Map.Entry<String,BioCDocumentLoader> entry: bioCDocumentLoaderMap.entrySet()) {
	logger.debug(entry.getKey() + " -> " + entry.getValue().getClass().getName());
      }
    }
  }
}
