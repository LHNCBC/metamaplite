
//
package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * list result formatters in properties file in the following format:
 * <pre>
 * mml.result.formatter.{name}: classname
 * </pre>
 */

public class ResultFormatterRegistry {
  /** Map of Result Formatters by format name */
  static final Map<String,ResultFormatter> formatterMap = new HashMap<String,ResultFormatter>();
  /** Map of Result Formatter description by format name */
  static final Map<String,String> descriptionMap = new HashMap<String,String>();

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param className full classname of loader
   * @throws ClassNotFoundException Class Not Found Exception
   * @throws IllegalAccessException illegal access of class
   * @throws InstantiationException exception while instantiating class 
   * @throws NoSuchMethodException  no method in class
   */
  public static void register(String name, String description, 
			      String className)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException
 {
    Object classInstance = Class.forName(className).newInstance();
    if (classInstance instanceof ResultFormatter) {
      ResultFormatter instance = (ResultFormatter)classInstance;
      synchronized(formatterMap) {
	formatterMap.put(name,instance);
      }
      synchronized(descriptionMap) {
	descriptionMap.put(name,description);
      }
    } else {
      throw new RuntimeException("Class instance " + className +
				 " for result formatter " + name +
				 " does not implement the BioCResultformatter interface.");
    }
  }

  /**
   * Register loader.
   * @param name name of loader
   * @param description description of loader
   * @param instance class instance of document loader
   */
  public static void register(String name, String description, 
			      ResultFormatter instance)
  {
    synchronized(formatterMap) {
      formatterMap.put(name, instance);
    }
    synchronized(descriptionMap) {
      descriptionMap.put(name,description);
    }
  }

  public static Set<String> listNameSet() {
    return formatterMap.keySet();
  }

  public static boolean contains(String name) {
    return formatterMap.containsKey(name);
  }

  public static List<String> listInfo() {
    List<String> descriptionList = new ArrayList<String>();
    for (Map.Entry<String,ResultFormatter> entry: formatterMap.entrySet()) {
      descriptionList.add(entry.getKey() + ": " + entry.getValue());
    }
    return descriptionList;
  }

  public static ResultFormatter getResultFormatter(String name) {
    return formatterMap.get(name);
  }
  
  public static ResultFormatter get(String name) {
    return formatterMap.get(name);
  }

  public static void register(Properties properties) 
     throws ClassNotFoundException, InstantiationException, NoSuchMethodException,IllegalAccessException
  {
    for (String propname: properties.stringPropertyNames()) {
      if (propname.indexOf("metamaplite.result.formatter.") >= 0) {
	String name = propname.substring("metamaplite.result.formatter.".length());
	register(name,"",properties.getProperty(propname));
      }
    }
  }
}
