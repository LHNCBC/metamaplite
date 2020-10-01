
//
package gov.nih.nlm.nls.metamap.lite.pipeline.plugins;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import java.lang.reflect.Method;

import gov.nih.nlm.nls.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently a singleton class.
 */

public class PluginRegistry {
  private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

  /** Map of plugins by plugin name */
  static Map<String,Plugin> pluginMap = new HashMap<String,Plugin>();

  /**
   * Register plugin.
   * @param name               plugin name
   * @param description        plugin description
   * @param className          fully qualified classname of plugin
   * @param methodName         target method name 
   * @param inputParameterType type of input parameter 
   * @throws ClassNotFoundException class not found exception
   * @throws InstantiationException class instantiation exception
   * @throws NoSuchMethodException no such method exception
   * @throws IllegalAccessException illegal access exception
   */
  public static void register(String name, String description, 
			      String className, String methodName,
			      String inputParameterType)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException
 {
    Object classInstance = Class.forName(className).newInstance();
    Class<?> parameterTypeClass = Class.forName(inputParameterType);
    Method newMethod = classInstance.getClass().getMethod(methodName,parameterTypeClass);

    Plugin newPlugin = new Plugin(name, description, classInstance,newMethod);
    pluginMap.put(name,newPlugin);
  }

  public static List<String> listPlugins() {
    List<String> descriptionList = new ArrayList<String>();
    for (Map.Entry<String,Plugin> entry: pluginMap.entrySet()) {
      descriptionList.add(entry.getKey() + ": " + entry.getValue().getDescription());
    }
    return descriptionList;
  }

  public static Method getMethod(String pluginName) {
    return pluginMap.get(pluginName).getMethod();
  }

  public static Object getClassInstance(String pluginName) {
    return pluginMap.get(pluginName).getClassInstance();
  }

  public static Plugin getPlugin(String pluginName) {
    return pluginMap.get(pluginName);
  }


  public static void registerPlugins(Properties properties)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException

  {
    String prefix = "metamaplite.pipe.element";
    for (Map.Entry<Object,Object> entry: properties.entrySet()) {
      if ((((String)entry.getKey()).length() > prefix.length()) &&
	  ((String)entry.getKey()).substring(0,prefix.length()).equals(prefix)) {
	String[] keyfields = ((String)entry.getKey()).split("\\.");
	String pluginName = keyfields[keyfields.length - 1];

	String[] fields = ((String)entry.getValue()).split("\\|"); // methodname | input params | output params
	logger.debug((String)entry.getValue() + " -> " + StringUtils.join(fields, " , ") +
			   "length of fields: " + fields.length);

	String[] classfields = fields[0].split("\\.");
	String className = StringUtils.join(Arrays.copyOfRange(classfields, 0, classfields.length - 1), ".");
	String methodName = classfields[classfields.length - 1];
	String[] inputParameters = fields[1].split(",");

	logger.debug(pluginName +  ", " + className + ", " + methodName + ", " + inputParameters[0]);
	PluginRegistry.register(pluginName, "", className, methodName, inputParameters[0]);
      }
    }
  }


}
