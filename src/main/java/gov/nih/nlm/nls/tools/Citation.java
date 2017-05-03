
//
package gov.nih.nlm.nls.tools;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 *
 */

public interface Citation {
  void addSection(String name, String text);
  void add(String name, String text);
  /** get text from section. */
  String getSection(String name);
  /** get section name set */
  Set<String> sectionNameSet();
}
