
//
package gov.nih.nlm.nls.metamap.document;

import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.types.Document;
import gov.nih.nlm.nls.types.Passage;
import gov.nih.nlm.nls.types.PassageImpl;

/**
 *
 */

public class PubMedDocumentImpl implements PubMedDocument {
  String id;
  List<Passage> passages;

  /*
   * @param docId       document identifier
   * @param docTitle    document title
   * @param docAbstract document abstract
   */
  public PubMedDocumentImpl(String docId, String docTitle, String docAbstract)
  {
    this.id = docId;
    this.passages = new ArrayList<Passage>();
    PassageImpl titlePassage = new PassageImpl();
    titlePassage.setText(docTitle);
    titlePassage.putInfon("section","title");
    // titlePassage.setOffset(0);
    this.passages.add(titlePassage);
    PassageImpl abstractPassage = new PassageImpl();;
    abstractPassage.setText(docAbstract);
    abstractPassage.putInfon("section","abstract");
    // abstractPassage.setOffset(0);
    this.passages.add(abstractPassage);
  }
  
  public String getId() { return id; }
  public String getTitle() { return this.passages.get(0).getText(); }
  public String getAbstract() { return this.passages.get(1).getText(); }
  public String getText() { return this.getTitle()  + " " + this.getAbstract(); }
  public List<Passage> getPassages() { return this.passages; }
}
