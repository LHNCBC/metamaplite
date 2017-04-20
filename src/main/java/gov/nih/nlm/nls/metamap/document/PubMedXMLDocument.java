
//
package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import bioc.BioCDocument;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import gov.nih.nlm.nls.tools.Citation;
import gov.nih.nlm.nls.tools.MedlineDomReader;

import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 *
 */

public class PubMedXMLDocument 
  implements BioCDocumentLoader
{
  MedlineDomReader medlineDomReader = new MedlineDomReader();

  /**
   * Instantiate PubMed XML as a BioC document.
   * <p>

   * @param citation citation instance
   * @return BioC document instance
   */
  public static BioCDocument instantiateBioCDocument(Citation citation) 
  {
    BioCDocument doc = new BioCDocument();
    String pmid = citation.getSection("pmid");
    if (pmid == null) {
      pmid = "missing";
    }
    doc.setID(pmid);
    BioCPassage titlePassage = new BioCPassage();
    String titleText = citation.getSection("articleTitle");
    titlePassage.setText(titleText);
    titlePassage.setOffset(0);
    titlePassage.putInfon("docid",pmid);
    titlePassage.putInfon("section","title");
    doc.addPassage(titlePassage);

    BioCPassage abstractPassage = new BioCPassage();
    abstractPassage.setText(citation.getSection("articleAbstract"));
    abstractPassage.putInfon("docid",pmid);
    abstractPassage.putInfon("section","abstract");
    abstractPassage.setOffset(titleText.length());
    doc.addPassage(abstractPassage);
    return doc;
  }

  @Override
  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException
  {
    // TODO: Stub
    return null;
  }

  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    // TODO: Stub
    Map<String,Citation> citationMap = this.medlineDomReader.readCitations(filename);
    for (Citation citation: citationMap.values()) {
      documentList.add(instantiateBioCDocument(citation));
    }
    return documentList;
  }

  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader Reader)
    throws IOException
  {
    // TODO: Stub
    return null;
  }
}
