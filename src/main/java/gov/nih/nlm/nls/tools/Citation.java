
//
package gov.nih.nlm.nls.tools;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 *
 */

public class Citation {
  /** map of section-name -&gt; text */
  Map<String,String> sectionMap;
  
  public Citation() { this.sectionMap = new HashMap<String,String>(); }
  public void addSection(String name, String text) { this.sectionMap.put(name, text); }
  public void add(String name, String text) { this.sectionMap.put(name, text); }
  /** get text from section. */
  public String getSection(String name) { return this.sectionMap.get(name); }
  /** get section name set */
  public Set<String> sectionNameSet() { return this.sectionMap.keySet(); }
}
