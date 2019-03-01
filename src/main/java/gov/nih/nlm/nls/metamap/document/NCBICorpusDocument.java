
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
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

public class NCBICorpusDocument implements BioCDocumentLoader {

  Charset charset = Charset.forName("utf-8");

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
    title.putInfon("docid",docFields[0]);
    title.putInfon("section","title");
    doc.addPassage(title);
    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(removeCategoryTags(docFields[2]));
    abstractPassage.putInfon("docid",docFields[0]);
    abstractPassage.putInfon("section","abstract");
    abstractPassage.setOffset(0);
    doc.addPassage(abstractPassage);
    return doc;
  }

  
  public static List<PubMedDocument> read(Reader inputReader)
    throws IOException
  {
    BufferedReader br;
    List<PubMedDocument> documentList = new ArrayList<PubMedDocument>();
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    while ((line = br.readLine()) != null) {
      documentList.add(NCBICorpusDocument.instantiateDocument(line));
    }
    return documentList;
  }
  

  /**
   * Load list of PubMed documents
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
    List<PubMedDocument> documentList = read(br);
    br.close();
    return documentList;
  }

  /**
   * Read list of BioC documents
   * @param inputReader input reader
   * @return List of strings, one document per line.
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public static List<BioCDocument> bioCRead(Reader inputReader)
    throws FileNotFoundException, IOException
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
      documentList.add(NCBICorpusDocument.instantiateBioCDocument(line));
    }
    br.close();
    return documentList;
  }
  


  /**
   * Load list of BioC documents
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
      documentList.add(NCBICorpusDocument.instantiateBioCDocument(line));
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

  @Override
   public List<BioCDocument> readAsBioCDocumentList(Reader reader) 
    throws IOException
  {
    return bioCRead(reader);
  }

}
