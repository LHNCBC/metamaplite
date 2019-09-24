package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;


public class TermInfoStringImpl implements TermInfo, Comparable {
  final String originalTerm;
  final String normTerm;
  final List<? extends Token>  tokenlist;
  String cui;
  public TermInfoStringImpl(final String originalTerm, final String normTerm, String cui, final List<? extends Token>  tokenlist) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.cui = cui;
    this.tokenlist = tokenlist;
  }

    public TermInfoStringImpl(final String originalTerm, final String normTerm, String cui) {
    this.originalTerm = originalTerm;
    this.normTerm = normTerm;
    this.cui = cui;
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
  public final Object getDictionaryInfo() {
    return this.cui;
  }

  public final String getDictionaryInfoAsString() {
    return this.cui;
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
      this.normTerm.compareTo(((TermInfo)o).getNormTerm()) +
      this.cui.compareTo(((TermInfoStringImpl)o).getDictionaryInfoAsString());
  }

  public int compareTo(TermInfoStringImpl o) {
    return this.originalTerm.compareTo(o.getOriginalTerm()) +
      this.normTerm.compareTo(o.getNormTerm()) +
      this.cui.compareTo(o.getDictionaryInfoAsString());
  }

  public int hashCode()
  {
    return this.originalTerm.hashCode() +
      this.normTerm.hashCode() +
      this.cui.hashCode();
  }
  
  public String toString()
  {
    return this.originalTerm + "|" +
      this.normTerm + "|" +
      this.tokenlist + "|" +
      this.cui;
  }
    
}

