
//
package gov.nih.nlm.nls.types;

import java.util.List;
import java.util.Map;

/**
 *
 */

public interface Passage {
  Map<String,String> getInfons();
  int getOffset();
  String getText();
  List<Sentence> getSentences();
  List<Annotation> getAnnotations();
}
