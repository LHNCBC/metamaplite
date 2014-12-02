
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

public class NCBICorpusDocument  {

  public static String removeCategoryTags(String text) {
    return text.replaceAll("<category=\"[A-Za-z]+\">", "").replaceAll("</category>", "");
  }

  /**
   * Instantiate NCBI Corpus document.
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
   * @return ChemDNER document instance
   */
  public static PubMedDocument instantiateDocument(String docText) 
  {
    String[] docFields = docText.split("\\t");

    return new PubMedDocumentImpl(docFields[0], 
				  removeCategoryTags(docFields[1]), 
				  removeCategoryTags(docFields[2]));
  }
  

  /**
   * Instantiate NCBI Corpus document.
   * <p>
   * Each document consists of docid followed by tab followed by the title
   * which is followed by a tab and the abstract.
   *
   * <pre>
   * id\ttitle\tabstract
   * </pre>
   *
   * @param docText string containing tab delimited version of
   * NCBIcorpus document.
   * @return NCBIcorpus document instance
   */
  public static BioCDocument instantiateBioCDocument(String docText) 
  {
    String[] docFields = docText.split("\\t");
    BioCDocument doc = new BioCDocument();
    doc.setID(docFields[0]);
    BioCPassage title = new BioCPassage();
    title.setText(removeCategoryTags(docFields[1]));
    title.setOffset(0);
    title.putInfon("title","title");
    doc.addPassage(title);
    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(removeCategoryTags(docFields[2]));
    abstractPassage.putInfon("abstract","abstract");
    abstractPassage.setOffset(0);
    doc.addPassage(abstractPassage);
    return doc;
  }

  /**
   * Load list of PubMed documents
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
      documentList.add(NCBICorpusDocument.instantiateDocument(line));
    }
    br.close();
    return documentList;
  }


  /**
   * Load list of BioC documents
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
      documentList.add(NCBICorpusDocument.instantiateBioCDocument(line));
    }
    br.close();
    return documentList;
  }

}
