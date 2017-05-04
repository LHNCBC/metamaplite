
//
package gov.nih.nlm.nls.metamap.document;

import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import javax.xml.stream.XMLStreamException;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.io.BioCDocumentReader;
import bioc.io.BioCCollectionReader;
import bioc.io.BioCFactory;
import bioc.io.standard.BioCFactoryImpl;
/**
 *
 */

public class BioCDocumentLoaderImpl implements BioCDocumentLoader {
  BioCFactory factory = new BioCFactoryImpl();

  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException {
    try {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      BioCDocumentReader docReader = this.factory.createBioCDocumentReader(br);
      return docReader.readDocument();
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }

  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException {
    try {
      Reader br = new BufferedReader(new FileReader(filename));
      BioCCollectionReader collectionReader = this.factory.createBioCCollectionReader(br);
      BioCCollection collection = collectionReader.readCollection();
      return collection.getDocuments();
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }

  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException {
    try {
      BioCCollectionReader collectionReader = this.factory.createBioCCollectionReader(reader);
      BioCCollection collection = collectionReader.readCollection();
      return collection.getDocuments();
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }
}
