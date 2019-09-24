package gov.nih.nlm.nls.metamap.lite.types;

import java.util.List;
import java.util.ArrayList;

/**
 * Describe class MMLEntity here.
 *
 *
 * Created: Mon Mar 18 13:57:43 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MMLEntity<T> {

  List<Span> spanList;
  T info; 

  /**
   * Creates a new <code>MMLEntity</code> instance which essentially
   * contains a span and related information about the span.
   * @param spanList    list of one or more span instances representing entity
   * @param relatedInfo other information associated with entity
   */
  public MMLEntity(List<Span> spanList, T relatedInfo) {
    this.spanList = spanList;
    this.info = relatedInfo;    
  }

  List<Span> getSpanList() { return this.spanList; }
  T getInfo() { return this.info; }
  public String toString() { return this.spanList + ":" + this.info; }
}
