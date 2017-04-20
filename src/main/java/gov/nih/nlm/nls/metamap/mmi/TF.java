package gov.nih.nlm.nls.metamap.mmi;

import java.util.List;

/**
 * Container for TermFrequency, conceptname, averageValue, and other
 * elements.
 *
 * Created: Wed Mar  8 15:05:21 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TF {
  private String        metaConcept;
  private List<String>  semanticTypes;
  private List<Tuple>   tuplelist;
  private boolean       titleFlag;
  private String        cui;             
  private int           frequencyCount;  
  private double        averageValue;
  private List<String>  treeCodes;        

  /**
   * Creates a new <code>TF</code> instance.
   *
   * @param metaConcept a <code>String</code> value
   * @param semanticTypes list of semantic types <code>String</code> instances
   * @param tuplelist list of tuples
   * @param titleFlag <code>boolean</code> true if concept occurred in title
   * @param cui a <code>String</code> value
   * @param frequencyCount a <code>int</code> frequency of term/concept in target text.
   * @param averageValue a <code>double</code> mean value
   * @param treeCodes list of MeSH treecodes
   */
  public TF(String        metaConcept,
	    List<String>  semanticTypes,
	    List<Tuple>   tuplelist,
	    boolean       titleFlag,
	    String        cui,             
	    int           frequencyCount,  
	    double        averageValue,
	    List<String>  treeCodes) {
    this.metaConcept = metaConcept;      
    this.semanticTypes = semanticTypes;    
    this.tuplelist = tuplelist;        
    this.titleFlag = titleFlag;        
    this.cui = cui;              
    this.frequencyCount = frequencyCount;   
    this.averageValue = averageValue;     
    this.treeCodes = treeCodes;        
  }

  public String getMetaConcept() { return this.metaConcept; }
  public List<String> getSemanticTypes() { return this.semanticTypes; }    
  public List<Tuple> getTuplelist() { return this.tuplelist; }
  public boolean getTitleFlag() { return this.titleFlag; }
  public String getCui() { return this.cui;}
  public int getFrequencyCount() { return this.frequencyCount; }
  public double getAverageValue() { return this.averageValue; }
  public List<String> getTreeCodes() { return this.treeCodes; }
}
