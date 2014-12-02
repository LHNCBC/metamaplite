
//
package gov.nih.nlm.nls.types;
import java.util.List;

/**
 *
 */

public interface Collection {
  String getCorpus();
  int getDate();
  String getKey();
  List<Document> getDocuments();
}
