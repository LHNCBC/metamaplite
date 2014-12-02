
//
package gov.nih.nlm.nls.types;

import java.util.List;
import java.util.Map;

/**
 *
 */

public interface Sentence {
  Map<String, String> getInfons();
  int getOffset();
  String getText();
  List<Annotation> getAnnotations();
}
