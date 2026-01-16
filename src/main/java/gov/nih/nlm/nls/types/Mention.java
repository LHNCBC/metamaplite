//
package gov.nih.nlm.nls.types;

import java.util.List;

/**
 *
 */

public interface Mention {
  String getMatchingText();
  List<Span> getSpanList();
  Object getInfo();
}
