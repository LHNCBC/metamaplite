
//
package gov.nih.nlm.nls.metamap.document;

import gov.nih.nlm.nls.types.Document;

/**
 *
 */

public interface PubMedDocument extends Document {
  String getId();
  String getTitle();
  String getAbstract();
}
