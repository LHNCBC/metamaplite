
//
package gov.nih.nlm.nls.metamap.document;

import bioc.BioCCollection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */

public interface BioCCollectionLoader {
  BioCCollection loadFileAsBioCCollection(String filename) throws FileNotFoundException, IOException;
  BioCCollection readAsBioCCollecion(Reader reader) throws IOException;
}
