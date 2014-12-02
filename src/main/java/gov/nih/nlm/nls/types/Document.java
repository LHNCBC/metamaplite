
//
package gov.nih.nlm.nls.types;

import java.util.List;

/**
 *
 */

public interface Document {
  String getId();
  List<Passage> getPassages();
}
