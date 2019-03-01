//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;

import bioc.BioCDocument;
import bioc.BioCPassage;
import gov.nih.nlm.nls.types.Document;

/**
 *
 */
public class ChemDNER implements BioCDocumentLoader {

  Charset charset = Charset.forName("utf-8");

  /**
   * Instantiate PubMedDocumentImpl document instance BioCreative
   * version of CHEMDNER document.
   * <p>
   * Each document consists of docid followed by tab followed by the title
   * which is followed by a tab and the abstract.
   *
   * <pre>
   * id\ttitle\tabstract
   * </pre>
   *
   * @param docText string containing tab delimited version of
   * CHEMDNER document.
   * @return PubMedDocumentImpl document instance
   */
  public static PubMedDocumentImpl instantiateDocument(String docText) 
  {
    String[] docFields = docText.split("\\t");
    return new PubMedDocumentImpl(docFields[0], docFields[1], docFields[2]);
  }

  /**
   * Instantiate BioCDocumentImpl document instance BioCreative
   * version of CHEMDNER document.
   * <p>
   * Each document consists of docid followed by tab followed by the title
   * which is followed by a tab and the abstract.
   *
   * <pre>
   * id\ttitle\tabstract
   * </pre>
   *
   * @param docText string containing tab delimited version of
   * CHEMDNER document.
   * @return BioCDocument document instance
   */
  public static BioCDocument instantiateBioCDocument(String docText) 
  {
    String[] docFields = docText.split("\\t");
    BioCDocument doc = new BioCDocument();
    doc.setID(docFields[0]);
    BioCPassage title = new BioCPassage();
    title.setText(docFields[1]);
    title.setOffset(0);
    title.putInfon("docid", docFields[0]);

    title.putInfon("section","title");
    doc.addPassage(title);
    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(docFields[2]);
    abstractPassage.putInfon("docid", docFields[0]);
    abstractPassage.putInfon("section","abstract");
    abstractPassage.setOffset(0);
    doc.addPassage(abstractPassage);
    return doc;
  }

  /**
   * Read list of PubMedDocumentImpl documents
   * @param inputReader input text reader
   * @return List of strings, one document per line.
   * @throws IOException i/o exception
   */
  public static List<PubMedDocument> read(Reader inputReader)
    throws IOException
  {
    BufferedReader br;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateDocument(line));
    }
    return documentList;
  }

  /**
   * Load list of PubMedDocumentImpl documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public static List<PubMedDocument> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Read list of BioCDocument documents
   * @param inputReader input text reader
   * @return List of strings, one document per line.
   * @throws IOException i/o exception
   */
  public static List<BioCDocument> bioCRead(Reader inputReader)
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
      documentList.add(ChemDNER.instantiateBioCDocument(line));
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
  public static List<BioCDocument> bioCLoadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateBioCDocument(line));
    }
    br.close();
    return documentList;
  }
  

  public BioCDocument loadFileAsBioCDocument(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    BioCDocument document = instantiateBioCDocument(inputtext);
    return document;
  }
  
 public List<BioCDocument> loadFileAsBioCDocumentList(String filename) 
    throws FileNotFoundException, IOException
  {
    return bioCLoadFile(filename);
  }

  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    return bioCRead(reader);
  }

}
