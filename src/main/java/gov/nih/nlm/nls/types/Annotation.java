
//
package gov.nih.nlm.nls.types;

/**
 *
 */

public interface Annotation {
  String getId();
  String getType();
  int getOffset();
  int getLength();
  String getText();
}
