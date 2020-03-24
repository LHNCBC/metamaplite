package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 * Describe class PubTator here.
 *
 *
 * Created: Fri Apr 21 11:00:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class PubTator
  implements BioCDocumentLoader {

  Charset charset = Charset.forName("utf-8");

  public PubTator() {

  }

  /**
   * Instantiate PubMed XML as a BioC document.
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
    titlePassage.putInfon("section","title");
    doc.addPassage(titlePassage);

    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(abstractText);
    abstractPassage.putInfon("docid",documentId);
    abstractPassage.putInfon("section","abstract");
    abstractPassage.setOffset(titleText.length());
    doc.addPassage(abstractPassage);
    return doc;
  }
  
  @Override
  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException
  {
    String documentId = "";
    String titleText = "";
    String abstractText = "";
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), this.charset));
    String line;
    while ((line = br.readLine()) != null) {
      String[] split = line.split("\\|");
      if (split.length > 2) {
  	if (split[1].equals("t")) {
  	  documentId = split[0].trim();
  	  titleText = split[2];
  	} else if (split[1].equals("a")) {
  	  abstractText = split[2];
  	}
      }
    }
    br.close();
    return instantiateBioCDocument(documentId, titleText, abstractText);
  }

  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String documentId = "";
    String titleText = "";
    String abstractText = "";
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), this.charset));
    String line;
    while ((line = br.readLine()) != null) {
      String[] split = line.split("\\|");
      if (split.length > 2) {
  	if (split[1].equals("t")) {
  	  documentId = split[0].trim();
  	  titleText = split[2];
  	} else if (split[1].equals("a")) {
  	  abstractText = split[2];
  	}
      }
      br.close();
      documentList.add(instantiateBioCDocument(documentId, titleText, abstractText));
    }
    return documentList;
  }

  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    String documentId = "";
    String titleText = "";
    String abstractText = "";
    BufferedReader br = new BufferedReader(reader);
    String line;
    while ((line = br.readLine()) != null) {
      String[] split = line.split("\\|");
      if (split.length > 2) {
  	if (split[1].equals("t")) {
  	  documentId = split[0].trim();
  	  titleText = split[2];
  	} else if (split[1].equals("a")) {
  	  abstractText = split[2];
  	}
      }
      documentList.add(instantiateBioCDocument(documentId, titleText, abstractText));
    }
    return documentList;
  }
}
