
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class ChemDNER extends PubMedDocumentImpl implements PubMedDocument {

  public ChemDNER(String docId, String docTitle, String docAbstract) {
    super(docId, docTitle, docAbstract);
  }

  /**
   * Instantiate ChemDNER document instance reading single-line
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
   * @return ChemDNER document instance
   */
  public static ChemDNER instantiateSLDIDocument(String docText) 
  {
    String[] docFields = docText.split("\\|");
    String docBody = docFields[1];
    String[] bodyFields = docBody.split("\t");

    return new ChemDNER(docFields[0], bodyFields[0], bodyFields[1]);
  }

  /**
   * Instantiate ChemDNER document instance BioCreative
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
   * @return ChemDNER document instance
   */
  public static ChemDNER instantiateDocument(String docText) 
  {
    String[] docFields = docText.split("\\t");
    return new ChemDNER(docFields[0], docFields[1], docFields[2]);
  }

  /**
   * Load list of ChemDNER documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<ChemDNER> loadSLDIFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<ChemDNER> documentList = new ArrayList<ChemDNER>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateSLDIDocument(line));
    }
    br.close();
    return documentList;
  }

  /**
   * Load list of ChemDNER documents
   * @param inputFilename input text filename
   * @return List of strings, one document per line.
   */
  public static List<ChemDNER> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<ChemDNER> documentList = new ArrayList<ChemDNER>();
    String line;
    while ((line = br.readLine()) != null) {
      documentList.add(ChemDNER.instantiateDocument(line));
    }
    br.close();
    return documentList;
  }

}
