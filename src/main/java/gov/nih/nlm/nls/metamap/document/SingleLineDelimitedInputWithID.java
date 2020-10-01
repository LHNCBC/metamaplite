
//
package gov.nih.nlm.nls.metamap.document;

import bioc.BioCDocument;
import bioc.BioCPassage;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */

public class SingleLineDelimitedInputWithID implements BioCDocumentLoader {
  private static final Logger logger = LoggerFactory.getLogger(SingleLineDelimitedInputWithID.class);

  static Charset charset = Charset.forName("utf-8");
  static String delimiterRegexp = System.getProperty("metamaplite.sldiwd.delimiter.regexp", "\\|");

  /**
   * Instantiate BioCDocument document instance reading single-line
   * delimited document with ID.
   * <p>
   * Each sldi document consists of docid followed by separator (|)
   * followed by the text.
   *
   * <pre>
   * id|text
   * </pre>
   *
   * @param docText string containing single-line delimited version of
   * CHEMDNER document.
   * @return BioCDocument document instance
   */
  public static BioCDocument instantiateBioCDocument(String docText) 
  {
    String[] docFields = docText.split(delimiterRegexp);
    BioCDocument doc = new BioCDocument();
    if (docFields.length > 1) {
      doc.setID(docFields[0]);
      BioCPassage textPassage = new BioCPassage();
      textPassage.setText(docFields[1]);
      textPassage.setOffset(0);
      textPassage.putInfon("docid", docFields[0]);
      textPassage.putInfon("text","text");
      textPassage.putInfon("section","text");
      textPassage.putInfon("inputformat", "sldiwi");
      doc.addPassage(textPassage);
    } else {
      logger.warn("Too few fields in line: " + docText + ", returning an empty document.");
    }
    return doc;
  }

  public static List<BioCDocument> bioCLoadFile(Reader inputReader)
    throws IOException {
    BufferedReader br;
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    int i = 0;
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    while ((line = br.readLine()) != null) {
      documentList.add(instantiateBioCDocument(line));
    }
    return documentList;
  }
  
  /**
   * Load list of BioCDocument documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   * @throws FileNotFoundException thrown when file can not be found for opening.
   * @throws IOException throw when an I/O exception occurs.
   */
  public static List<BioCDocument> bioCLoadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");

    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(instantiateBioCDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Save a list of BioCDocuments as SLDI.
   * @param outputfilename output filename
   * @param documentList list of BioCDocument instances
   * @throws IOException throw when an I/O exception occurs.
   */
  public static void saveBioCDocumentListAsSLDIFile(String outputfilename, List<BioCDocument> documentList)
    throws IOException
  {
    for (BioCDocument doc: documentList) {
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getID()).append("|");
      for (BioCPassage passage: doc.getPassages()) {
	sb.append(passage.getText()).append("  ");
      }
    }
  }

  public BioCDocument loadFileAsBioCDocument(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    BioCDocument document = instantiateBioCDocument(inputtext);
    return document;
  }

  @Override
 public List<BioCDocument> loadFileAsBioCDocumentList(String filename) 
    throws FileNotFoundException, IOException
  {
    return bioCLoadFile(filename);
  }

  /** read freetext into BioC Document instance */
  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException {
    return bioCLoadFile(reader);
  }

}
