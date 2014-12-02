
//
package gov.nih.nlm.nls.metamap.lite.pipeline.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

/**
 *
 */

public class PipelineRegistry {
  static Map<String,List<Plugin>> pipeSequenceMap = new HashMap<String,List<Plugin>>();

  /**
   * Register plugin sequence in registry.
   * @param name name of plugin sequence.
   * @param sequence ordered list of plugins
   */
  public static void register(String name, List<Plugin> sequence) {
    pipeSequenceMap.put(name, sequence);
  }

  /**
   * Get plugin sequence for pipe with supplied name.
   * @param name name of plugin sequence.
   * @return ordered list of plugins
   */
  public static List<Plugin> get(String name) {
    return pipeSequenceMap.get(name);
  }

  public static Set<String> listPipeNames() {
    return pipeSequenceMap.keySet();
  }

  public static List<String> listPipeContents() {
    List<String> contentList = new ArrayList<String>();
    for (Map.Entry<String,List<Plugin>> entry: pipeSequenceMap.entrySet()) {
      StringBuilder sb = new StringBuilder();
      sb.append (entry.getKey()).append(" -> ");
      for (Plugin plugin: entry.getValue()) {
	sb.append(plugin.getName()).append(", ");
      }
      contentList.add(sb.toString());
    }
    return contentList;
  }

  public static void registerPipeSequences(String prefix, Properties properties) {
    for (Map.Entry<Object,Object> entry: properties.entrySet()) {
      if ((((String)entry.getKey()).length() > prefix.length()) &&
	  ((String)entry.getKey()).substring(0,prefix.length()).equals(prefix)) {
	List<Plugin> pipeSequence = new ArrayList<Plugin>();
	String[] keyfields = ((String)entry.getKey()).split("\\.");
	String pipelineName = keyfields[keyfields.length - 2] + "." + keyfields[keyfields.length - 1];
	String[] pluginArray = ((String)entry.getValue()).split("\\|"); // plugin0|plugin1|...
	for (String pluginName: pluginArray) {
	  pipeSequence.add(PluginRegistry.getPlugin(pluginName));
	}
	PipelineRegistry.register(pipelineName, pipeSequence);
      }
    }
  }
}
