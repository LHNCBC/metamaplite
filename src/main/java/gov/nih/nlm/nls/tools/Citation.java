
//
package gov.nih.nlm.nls.tools;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * set of method signatures for Citation instances.
 */

public interface Citation {
  void addSection(String name, String text);
  void add(String name, String text);
  /** get text from section.
   * @param name name of section
   * @return content of section 
   */
  String getSection(String name);
  /** get section name set
   * @return set of section names
   */
  Set<String> sectionNameSet();
}
