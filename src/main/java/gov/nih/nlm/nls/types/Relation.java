
//
package gov.nih.nlm.nls.types;

import java.util.List;

/**
 *
 */

public interface Relation {
  String getId();
  String getType();
  List<String> getLabels();
  List<String> getRefIds();
}
