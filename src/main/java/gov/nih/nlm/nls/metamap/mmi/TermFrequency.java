package gov.nih.nlm.nls.metamap.mmi;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Container for conceptname, semantic types, positional information
 * (tuples), concept identifier, frequency count, average value, and
 * treecodes.
 *
 * Created: Wed Mar  8 08:57:44 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TermFrequency {

  /** Metathesaurus Concept String */
  private String        metaConcept;
  /** List of semantic types */
  private List<String>  semanticTypes;
  /** Meta information list (tuples?) */
  private Set<Tuple>   tupleSet;
  /** Is the term in the title? */
  private boolean       titleFlag;
  /** Concept unique identifier */
  private String        cui;             
  private int           frequencyCount;  
  private double        averageValue;
  /** Treecode List */
  private List<String>  treecodes;        

  
  /**
   * Creates a new <code>TermFrequency</code> instance.
   *
   * @param metaConcept a <code>String</code> value
   * @param semanticTypes list of semantic types <code>String</code> instances
   * @param tupleSet list of tuples
   * @param titleFlag <code>boolean</code> true if concept occurred in title
   * @param cui a <code>String</code> value
   * @param frequencyCount a <code>int</code> frequency of term/concept in target text.
   * @param averageValue a <code>double</code> mean value
   * @param treecodes list of MeSH treecodes
   */
  public TermFrequency(String        metaConcept,
		       List<String>  semanticTypes,
		       Set<Tuple>   tupleSet,
		       boolean       titleFlag,
		       String        cui,             
		       int           frequencyCount,  
		       double        averageValue,
		       List<String>  treecodes) {
    this.metaConcept = metaConcept;      
    this.semanticTypes = semanticTypes;    
    this.tupleSet = tupleSet;        
    this.titleFlag = titleFlag;        
    this.cui = cui;              
    this.frequencyCount = frequencyCount;   
    this.averageValue = averageValue;     
    this.treecodes = treecodes;        

  }
  public String getMetaConcept() { return this.metaConcept; }
  public List<String> getSemanticTypes() { return this.semanticTypes; }
  /**
   * returns a mutable set of tuples referenced by the TermFrequency instance 
   * @return reference to mutable set of tuples.
   */
  public Set<Tuple> getTupleSet() { return this.tupleSet; }
  /**
   * returns a copy of the tuple set as a list.
   * @return copy of tuple set conforming to the list interface.
   */
  public List<Tuple> getTupleList() { return new ArrayList(this.tupleSet); }
  public boolean getTitleFlag() { return this.titleFlag; }
  public String getCui() { return this.cui;}
  public int getFrequencyCount() { return this.frequencyCount; }
  public double getAverageValue() { return this.averageValue; }
  public List<String> getTreecodes() { return this.treecodes; }
  public void setFrequencyCount(int count) { this.frequencyCount = count; }
}
