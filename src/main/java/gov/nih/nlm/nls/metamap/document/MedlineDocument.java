package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 * Describe class MedlineDocument here.
 *
 *
 * Created: Thu Aug 31 14:26:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MedlineDocument
  implements BioCDocumentLoader {

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
    titlePassage.setOffset(0);
    titlePassage.putInfon("docid",documentId);
    // titlePassage.putInfon("section","title"); 
    titlePassage.putInfon("section","TI");
    doc.addPassage(titlePassage);

    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(abstractText);
    abstractPassage.putInfon("docid",documentId);
    // abstractPassage.putInfon("section","abstract");
    abstractPassage.putInfon("section","AB");
    abstractPassage.setOffset(titleText.length());
    doc.addPassage(abstractPassage);
    return doc;
  }


  @Override
  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException
  {
    String documentId = "";
    StringBuilder titleText = new StringBuilder();
    StringBuilder abstractText = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    String key = "";
    while ((line = br.readLine()) != null) {
      String header = line.substring(0,4);
      String content = line.substring(6);
      if (header.trim().length() > 0) {
	key = header.trim();
      }
      if (key.length() > 0) {
	if (key.equals("PMID")) {
	  documentId = content;
	} else if (key.equals("TI")) {
	  titleText.append(content).append(" ");
	} else if (key.equals("AB")) {
	  abstractText.append(content).append(" ");
	}
      }
    }
    return instantiateBioCDocument(documentId,
				   titleText.toString(),
				   abstractText.toString());
  }

  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String documentId = "";
    StringBuilder titleText = new StringBuilder();
    StringBuilder abstractText = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    String key = "";
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) { // end of document
	if ((documentId.length() +
	     titleText.toString().length() +
	     abstractText.toString().length()) > 0) {
	  // don't instantiate any empty documents.
	  documentList.add(instantiateBioCDocument(documentId,
						   titleText.toString(),
						   abstractText.toString()));
	  titleText.setLength(0);
	  abstractText.setLength(0);
	}
      } else {
	String header = line.substring(0,4);
	String content = line.substring(6);
	if (header.trim().length() > 0) {
	  key = header.trim();
	}
	if (key.length() > 0) {
	  if (key.equals("PMID")) {
	    documentId = content;
	  } else if (key.equals("TI")) {
	    titleText.append(content).append(" ");
	  } else if (key.equals("AB")) {
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
      if (line.trim().length() == 0) { // end of document
	documentList.add(instantiateBioCDocument(documentId,
						 titleText.toString(),
						 abstractText.toString()));
	titleText.setLength(0);
	abstractText.setLength(0);
      } else {
	String header = line.substring(0,4);
	String content = line.substring(6);
	if (header.trim().length() > 0) {
	  key = header.trim();
	}
	if (key.length() > 0) {
	  if (key.equals("PMID")) {
	    documentId = content;
	  } else if (key.equals("TI")) {
	    titleText.append(content).append(" ");
	  } else if (key.equals("AB")) {
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
