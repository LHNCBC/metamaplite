
//
package gov.nih.nlm.nls.metamap.document;

import java.util.List;
import bioc.BioCDocument;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */

public interface BioCDocumentLoader {
  BioCDocument loadFileAsBioCDocument(String filename) throws FileNotFoundException, IOException;
  List<BioCDocument> loadFileAsBioCDocumentList(String filename) throws FileNotFoundException, IOException;
  List<BioCDocument> readAsBioCDocumentList(Reader reader) throws IOException;
}
