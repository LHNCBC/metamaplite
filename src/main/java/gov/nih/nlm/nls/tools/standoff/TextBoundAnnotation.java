package gov.nih.nlm.nls.tools.standoff;

import java.util.Set;

/**
 * TextBoundAnnotation - an Entity annotation 
 *
 * Each entity annotation has a unique ID and is defined by type
 * (e.g. Person or Organization) and the span of characters containing
 * the entity mention (represented as a "start end" offset pair).
 *
 * <pre>
 * T1	Organization 0 4	Sony
 * T3	Organization 33 41	Ericsson
 * T3	Country 75 81	Sweden
 * </pre>
 *
 * See also, "brat standoff format" (http://brat.nlplab.org/standoff.html)
 *
 * Created: Thu Nov  9 09:25:15 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TextBoundAnnotation implements Annotation {
  String id;
  String type;
  int startOffset;
  int endOffset;
  String text;     
  Set<NormalizationAnnotation> referenceSet = null;

  public TextBoundAnnotation(String id, String type, int start, int end, String text) {
      
    this.id = id; 
    this.type = type;
    this.startOffset = start;
    this.endOffset = end;
    this.text = text.replaceAll("\n","\\n");
  }
  public TextBoundAnnotation(String id, String type, int start, int end, String text, 
			     Set<NormalizationAnnotation> referenceSet) {
    this.id = id; 
    this.type = type;
    this.startOffset = start;
    this.endOffset = end;
    this.text = text.replaceAll("\n","\\n");
    this.referenceSet = referenceSet;
  }
  public String getId() { return this.id; }
  public void setId(String id) { this.id = id; }
  public void setReferenceSet(Set<NormalizationAnnotation> referenceSet) { 
    this.referenceSet = referenceSet;
  }
  public void addToReferenceSet(Set<NormalizationAnnotation> referenceSet) { 
    this.referenceSet.addAll(referenceSet);
  }
  public Set<NormalizationAnnotation> getReferenceSet() { return this.referenceSet; } 
  public String toString() {
    return this.id + "\t" + this.type + " " + this.startOffset + " " + this.endOffset + "\t" + this.text;
  }
  public int hashCode() { return this.type.hashCode() + this.startOffset + this.endOffset + this.text.hashCode(); }
  public String genKey() { return this.type.hashCode() + ":" + this.startOffset + ":" + this.endOffset + "|" + this.text.hashCode(); }
}
