package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 * Convert a PubMed RIS (Research Information Systems, Inc.) Format
 * Document into a BioC document preserving positional information.
 * See Also:
 * <dl>
 * <dt>Wikipedia article on RIS file format<dd><a href="https://en.wikipedia.org/wiki/RIS_(file_format)">
https://en.wikipedia.org/wiki/RIS_(file_format)</a>
 * 
 * <dt>RIS Format Specification
 * <dd><a href="https://jira.sakaiproject.org/secure/attachment/21845/RIS+Format+Specifications.pdf">https://jira.sakaiproject.org/secure/attachment/21845/RIS+Format+Specifications.pdf (PDF file)</a>
 * </dl>
 * Created: Thu Aug 31 14:26:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class PubMedRISFormat
  implements BioCDocumentLoader {

  Charset charset = Charset.forName("utf-8");

  /**
   * Instantiate Medline Document as a BioC document.
   * <p>
   * @param documentId document identifier
   * @param titleText text of title
   * @param abstractText text of abstract
   * @return BioC document instance
   */
  public static BioCDocument instantiateBioCDocument(String documentId,
						     String titleText,
						     String abstractText) 
  {
    BioCDocument doc = new BioCDocument();
    doc.setID(documentId);
    BioCPassage titlePassage = new BioCPassage();
    titlePassage.setText(titleText);
    // include length of PMID field identifier
    titlePassage.setOffset(documentId.length() + 13); // add offset of PMID and TI headers plus newline
    titlePassage.putInfon("docid",documentId);
    // titlePassage.putInfon("section","title"); 
    titlePassage.putInfon("section", "TI");
    doc.addPassage(titlePassage);

    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(abstractText);
    abstractPassage.putInfon("docid",documentId);
    // abstractPassage.putInfon("section","abstract");
    abstractPassage.putInfon("section", "AB");
    // include length of PMID and TI field identifier
    abstractPassage.setOffset(documentId.length() + titleText.length() + 19); // add offset of PMID, TI, and AB headers plus two newlines
    doc.addPassage(abstractPassage);
    return doc;
  }
  
  String ID_LABEL = "U1";
  String TITLE_LABEL = "T1";
  String ABSTRACT_LABEL = "AB";

  @Override
  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException
  {
    String documentId = "";
    StringBuilder titleText = new StringBuilder();
    StringBuilder abstractText = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), this.charset));
    String line;
    String key = "";
    while ((line = br.readLine()) != null) {
      String header = line.substring(0,4);
      String content = line.substring(6);
      if (header.trim().length() > 0) {
	key = header.trim();
      }
      if (key.length() > 0) {
	if (key.equals(ID_LABEL)) {
	  documentId = content.replace("[pmid]","");
	} else if (key.equals(TITLE_LABEL)) {
	  titleText.append(content).append(" ");
	} else if (key.equals(ABSTRACT_LABEL)) {
	  abstractText.append(content).append(" ");
	}
      }
    }
    br.close();
    return instantiateBioCDocument(documentId,
				   titleText.toString(),
				   abstractText.toString());
  }

  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), this.charset));
    List<BioCDocument> documentList = readAsBioCDocumentList(br);
    br.close();
    return documentList;
  }

  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String documentId = "";
    StringBuilder titleText = new StringBuilder();
    StringBuilder abstractText = new StringBuilder();
    BufferedReader br = new BufferedReader(reader);
    String line;
    String key = "";
    while ((line = br.readLine()) != null) {
      String header = line.substring(0,4);
      String content = line.substring(6);
      if (header.trim().length() > 0) {
	key = header.trim();
      }
      if (key.equals("ER")) { // end of document
	documentList.add(instantiateBioCDocument(documentId,
						 titleText.toString(),
						 abstractText.toString()));
	titleText.setLength(0);
	abstractText.setLength(0);
      } else {
	if (key.length() > 0) {
	  if (key.equals(ID_LABEL)) {
	    documentId = content.replace("[pmid]","");
	  } else if (key.equals(TITLE_LABEL)) {
	    titleText.append(content).append(" ");
	  } else if (key.equals(ABSTRACT_LABEL)) {
	    abstractText.append(content).append(" ");
	  }
	}
      }
    }
    documentList.add(instantiateBioCDocument(documentId,
					     titleText.toString(),
					     abstractText.toString()));
    return documentList;
  }

}
