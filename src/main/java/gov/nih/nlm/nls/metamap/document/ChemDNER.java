
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import bioc.BioCDocument;
import bioc.BioCPassage;
import gov.nih.nlm.nls.types.Document;

/**
 *
 */
public class ChemDNER {

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
    doc.setID(docFields[0]);
    BioCPassage title = new BioCPassage();
    title.setText(bodyFields[0]);
    title.setOffset(0);
    title.putInfon("title","title");
    doc.addPassage(title);
    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(bodyFields[1]);
    abstractPassage.putInfon("abstract","abstract");
    abstractPassage.setOffset(0);
    doc.addPassage(abstractPassage);
    return doc;
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
    title.putInfon("title","title");
    doc.addPassage(title);
    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(docFields[2]);
    abstractPassage.putInfon("abstract","abstract");
    abstractPassage.setOffset(0);
    doc.addPassage(abstractPassage);
    return doc;
  }



  /**
   * Load list of PubMedDocumentImpl documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<PubMedDocument> loadSLDIFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateSLDIDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Load list of PubMedDocumentImpl documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<PubMedDocument> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Load list of BioCDocument documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<BioCDocument> bioCLoadSLDIFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateBioCSLDIDocument(line));
    }
    br.close();
    return documentList;
  }


  /**
   * Load list of BioCDocument documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<BioCDocument> bioCLoadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateBioCDocument(line));
    }
    br.close();
    return documentList;
  }
  

}
