//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.util.List;
import java.util.ArrayList;

import bioc.BioCDocument;
import bioc.BioCPassage;
import gov.nih.nlm.nls.types.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChemDNERSLDI implements BioCDocumentLoader {
  private static final Logger logger = LoggerFactory.getLogger(SingleLineDelimitedInputWithID.class);

  /**
   * Instantiate PubMedDocumentImpl document instance reading single-line
   * delimited version of CHEMDNER document.
   * <p>
   * Each sldi document consists of docid followed by separator (|)
   * followed by title which is followed by a tab and abstract.
   *
   * <pre>
   * id|title\tabstract
   * </pre>
   *
   * @param docText string containing single-line delimited version of
   * CHEMDNER document.
   * @return PubMedDocumentImpl document instance
   */
  public static PubMedDocument instantiateSLDIDocument(String docText) 
  {
    String[] docFields = docText.split("\\|");
    String docBody = docFields[1];
    String[] bodyFields = docBody.split("\t");

    return new PubMedDocumentImpl(docFields[0], bodyFields[0], bodyFields[1]);
  }


  /**
   * Instantiate BioCDocument document instance reading single-line
   * delimited version of CHEMDNER document.
   * <p>
   * Each sldi document consists of docid followed by separator (|)
   * followed by title which is followed by a tab and abstract.
   *
   * <pre>
   * id|title\tabstract
   * </pre>
   *
   * @param docText string containing single-line delimited version of
   * CHEMDNER document.
   * @return BioCDocument document instance
   */
  public static BioCDocument instantiateBioCSLDIDocument(String docText) 
  {    
    String[] docFields = docText.split("\\|");
    String docBody = docFields[1];
    String[] bodyFields = docBody.split("\t");

    BioCDocument doc = new BioCDocument();
    if (docFields.length > 1) {
      doc.setID(docFields[0]);
      BioCPassage title = new BioCPassage();
      title.setText(bodyFields[0]);
      title.setOffset(0);
      title.putInfon("docid", docFields[0]);
      title.putInfon("section","title");
      doc.addPassage(title);
      BioCPassage abstractPassage = new BioCPassage();
      abstractPassage.setText(bodyFields[1]);
      abstractPassage.putInfon("docid", docFields[0]);
      abstractPassage.putInfon("section","abstract");
      abstractPassage.setOffset(0);
      doc.addPassage(abstractPassage);
    } else {
      logger.warn("Too few fields in line: " + docText + ", returning an empty document.");
    }
    return doc;
  }

  /**
   * Load list of PubMedDocumentImpl documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public static List<PubMedDocument> loadSLDIFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    BufferedReader br =
      new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename),
					       charset));
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(instantiateSLDIDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Read list of BioCDocument documents
   * @param inputReader input reader
   * @return List of strings, one document per line.
   * @throws IOException i/o exception
   */
  public static List<BioCDocument> bioCReadSLDI(Reader inputReader)
    throws IOException
  {
    BufferedReader br;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(instantiateBioCSLDIDocument(line));
    }
    return documentList;
  }

  /**
   * Load list of BioCDocument documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public static List<BioCDocument> bioCLoadSLDIFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    BufferedReader br =
      new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename),
					       charset));
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(instantiateBioCSLDIDocument(line));
    }
    br.close();
    return documentList;
  }

  public BioCDocument loadFileAsBioCDocument(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    BioCDocument document = instantiateBioCSLDIDocument(inputtext);
    return document;
  }
  
 public List<BioCDocument> loadFileAsBioCDocumentList(String filename) 
    throws FileNotFoundException, IOException
  {
    return bioCLoadSLDIFile(filename);
  }
    
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    return bioCReadSLDI(reader);
  }
}

