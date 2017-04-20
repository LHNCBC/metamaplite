package gov.nih.nlm.nls.metamap.mmi;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.io.Serializable;

/**
 * Container for negated rank, conceptname, concept unique identifier
 * (cui), treecodes, list of tuples (see Tuple7), semantic types
 * <p>
 * Created: Wed Mar  8 15:05:06 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class AATF implements Serializable, Comparable {
  private double negNRank;
  private String concept;
  private String cui;
  private List<String> treeCodes;
  private List<Tuple> tuplelist;
  private List<String> semanticTypes;
  
  /**
   * Creates a new <code>AATF</code> instance.
   *
   * @param negNRank a <code>double</code> value
   * @param concept a <code>String</code> value
   * @param semanticTypes list of semantic types <code>String</code> instances
   * @param cui a <code>String</code> value
   * @param tuplelist list of tuples
   * @param treeCodes list of MeSH treecodes
   */
  public AATF(double        negNRank,
	      String        concept,
	      List<String>  semanticTypes,
	      String        cui,
	      List<Tuple>   tuplelist,
	      List<String>  treeCodes) {
    this.negNRank = negNRank;
    this.concept = concept;      
    this.semanticTypes = semanticTypes;    
    this.cui = cui;              
    this.tuplelist = tuplelist;        
    this.treeCodes = treeCodes;    
  }

  /**
   * Describe <code>getNegNRank</code> method here.
   *
   * @return a <code>double</code> value
   */
  public final double getNegNRank() {
    return negNRank;
  }

  public final void setNegNRank(final double negNRank) {
    this.negNRank = negNRank;
  }

  public final String getConcept() {
    return concept;
  }

  public final void setConcept(final String concept) {
    this.concept = concept;
  }

  public final List<String> getSemanticTypes() {
    return semanticTypes;
  }

  public final void setSemanticTypes(final List<String> semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

  public final String getCui() {
    return cui;
  }

  public final void setCui(final String cui) {
    this.cui = cui;
  }

  public final List<Tuple> getTuplelist() {
    return tuplelist;
  }

  public final void setTuplelist(final List<Tuple> tuplelist) {
    this.tuplelist = tuplelist;
  }

  public final List<String> getTreeCodes() {
    return treeCodes;
  }

  public final void setTreeCodes(final List<String> treeCodes) {
    this.treeCodes = treeCodes;
  }
  public String toString() {
    return this.getNegNRank() + "|" +
      this.getConcept() +"|" +
      this.getCui() +"|" +
      this.getSemanticTypes() +"|" +
      this.getTuplelist().stream().map(i -> i.toString()).collect(Collectors.joining(","))  + "|" +
      this.getTreeCodes();
  }
  public int compareTo(AATF o) {
    if (this.getNegNRank() != o.getNegNRank()) {
      return Double.compare(this.getNegNRank(), o.getNegNRank());
    }
    return this.getConcept().compareTo(o.getConcept());
  }
  @Override
  public int compareTo(Object o) {
    if (this.getNegNRank() != ((AATF)o).getNegNRank()) {
      return Double.compare(this.getNegNRank(), ((AATF)o).getNegNRank());
    }
    return this.getConcept().compareTo(((AATF)o).getConcept());
  }
}
