package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * TermInfoImpl - Implementation of TermInfo class.
 *
 *
 * Created: Thu May 30 09:38:59 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TermInfoImpl<T> implements TermInfo {
  private String originalTerm;
  private String normTerm;
  private T dictionaryInfo;
  private List<? extends Token> tokenList;
  public TermInfoImpl(String originalTerm, 
		        String normTerm,
		        T dictionaryInfo,
  		        List<? extends Token> tokenSubList) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.dictionaryInfo = dictionaryInfo;
    this.tokenList = tokenSubList;
  }
  public TermInfoImpl(String originalTerm, 
		        String normTerm,
		        T dictionaryInfo) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.dictionaryInfo = dictionaryInfo;
  }
  /** normalized form of term */
  public String getNormTerm() { return this.normTerm; }
  /** original term */
  public String getOriginalTerm() { return this.originalTerm; }
  /** dictionary info */
  public T getDictionaryInfo() { return this.dictionaryInfo; }
  public List<? extends Token> getTokenList() { return this.tokenList; }
  public String toString() {
    return this.originalTerm + "|" + this.normTerm + "|" + this.dictionaryInfo;
  }
}
