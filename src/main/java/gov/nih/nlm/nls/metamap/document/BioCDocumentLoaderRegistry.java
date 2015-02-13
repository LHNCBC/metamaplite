//
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

/**
 * list document loaders in properties file in the following format:
 * <pre>
 * bioc.document.loader.<name>: classname
 * </pre>
 */

public class BioCDocumentLoaderRegistry {
  /** Map of BioC document loader by document type name */
  static Map<String,BioCDocumentLoader> bioCDocumentLoaderMap = new HashMap<String,BioCDocumentLoader>();
  /** Map of document loader description by document type name */
  static Map<String,String> descriptionMap = new HashMap<String,String>();

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param classname full classname of loader
   */
  public static void register(String name, String description, 
			      String className)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException
 {
    Object classInstance = Class.forName(className).newInstance();
    if (classInstance instanceof BioCDocumentLoader) {
      BioCDocumentLoader instance = (BioCDocumentLoader)classInstance;
      bioCDocumentLoaderMap.put(name,instance);
      descriptionMap.put(name,description);
    } else {
      throw new RuntimeException("Class instance does not implement the BioCDocumentLoader interface.");
    }
  }

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param class instance of document loader
   */
  public static void register(String name, String description, 
			      BioCDocumentLoader instance)
  {
    bioCDocumentLoaderMap.put(name, instance);
    descriptionMap.put(name,description);
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
  
  public static BioCDocumentLoader get(String name) {
    return bioCDocumentLoaderMap.get(name);
  }

  public static void register(Properties properties) 
     throws ClassNotFoundException, InstantiationException, NoSuchMethodException,IllegalAccessException
  {
    for (String propname: properties.stringPropertyNames()) {
      if (propname.indexOf("bioc.document.loader.") > 0) {
	String name = propname.substring("bioc.document.loader.".length(), propname.length() - 1);
	register(name,"",properties.getProperty(propname));
      }
    }
  }
}
