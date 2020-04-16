package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;


public class TermInfoSampleImpl<T> implements TermInfo, Comparable {
  final String originalTerm;
  final String normTerm;
  final List<? extends Token>  tokenlist;
  T dictionaryInfo;
  public TermInfoSampleImpl(final String originalTerm, final String normTerm, T dictionaryInfo, final List<? extends Token>  tokenlist) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.dictionaryInfo = dictionaryInfo;
    this.tokenlist = tokenlist;
  }

    public TermInfoSampleImpl(final String originalTerm, final String normTerm, T dictionaryInfo) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.dictionaryInfo = dictionaryInfo;
    this.tokenlist = new ArrayList<ERToken>();
  }


  // Implementation of gov.nih.nlm.nls.metamap.lite.TermInfo

  /**
   * Describe <code>getNormTerm</code> method here.
   *
   * @return a <code>String</code> value
   */
  public final String getNormTerm() {
    return this.normTerm;
  }

  /**
   * Describe <code>getOriginalTerm</code> method here.
   *
   * @return a <code>String</code> value
   */
  public final String getOriginalTerm() {
    return this.originalTerm;
  }

  /**
   * Describe <code>getDictionaryInfo</code> method here.
   *
   * @return an <code>Object</code> value
   */
  public final T getDictionaryInfo() {
    return this.dictionaryInfo;
  }

  /**
   * Describe <code>getTokenList</code> method here.
   *
   * @return a <code>List</code> value
   */
  public final List getTokenList() {
    return this.tokenlist;
  }

  public int compareTo(Object o) {
    return this.originalTerm.compareTo(((TermInfo)o).getOriginalTerm()) +
      this.normTerm.compareTo(((TermInfo)o).getNormTerm());
  }

  public int hashCode()
  {
    return this.originalTerm.hashCode() +
      this.normTerm.hashCode() +
      this.dictionaryInfo.hashCode();
  }
  
  public String toString()
  {
    return this.originalTerm + "|" +
      this.normTerm + "|" +
      this.tokenlist + "|" +
      this.dictionaryInfo;
  }
    
}

