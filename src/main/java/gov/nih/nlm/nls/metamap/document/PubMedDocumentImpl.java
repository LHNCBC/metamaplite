
//
package gov.nih.nlm.nls.metamap.document;

/**
 *
 */

public class PubMedDocumentImpl implements PubMedDocument {
  String id;
  String titleText;
  String abstractText;

  /*
   * @param docId       document identifier
   * @param docTitle    document title
   * @param docAbstract document abstract
   */
  public PubMedDocumentImpl(String docId, String docTitle, String docAbstract)
  {
    this.id = docId;
    this.titleText = docTitle;
    this.abstractText = docAbstract;
  }
  
  public String getId() { return id; }
  public String getTitle() { return titleText; }
  public String getAbstract() { return abstractText; }
  public String getText() { return titleText + " " + abstractText; }
}
