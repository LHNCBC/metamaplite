
//
package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.types.Annotation;

/**
 *
 */

public class ConceptInfo {
  String cui;
  String preferredName;
  /** metathesaurus string that matched the input term. */
  String conceptString;

  Set<String> sourceSet = null; 
  Set<String> semanticTypeSet = null;

  public ConceptInfo(String cui, String prefname,
		     String conceptString,
		     Set<String> newSourceSet,
		     Set<String> newSemanticTypeSet) {
    // this.cui = cui.intern();
    this.cui = cui;
    this.preferredName = prefname;
    this.conceptString = conceptString;
    this.sourceSet = newSourceSet;
    this.semanticTypeSet = newSemanticTypeSet;
  }
  public ConceptInfo(String cui, String prefname, 
		     Set<String> newSourceSet,
		     Set<String> newSemanticTypeSet) {
    // this.cui = cui.intern();
    this.cui = cui;
    this.preferredName = prefname;
    this.sourceSet = newSourceSet;
    this.semanticTypeSet = newSemanticTypeSet;
  }

  public ConceptInfo(ConceptInfo conceptInfo) {
    this.cui = conceptInfo.getCUI();
    this.preferredName = conceptInfo.getPreferredName();
    this.conceptString = conceptInfo.getConceptString();
    this.sourceSet = conceptInfo.getSourceSet();
    this.semanticTypeSet = conceptInfo.getSemanticTypeSet();
  }

  public String getCUI() { return this.cui; }
  public void setCUI(String cui) { this.cui = cui; }
  public String getPreferredName() { return this.preferredName; }
  public void setPreferredName(String name) { this.preferredName = name; }
  public String getConceptString() { return this.conceptString; }
  public void setConceptString(String name) { this.conceptString = name; }
  public void addSourceSet(Collection<String> newSourceList) {
    this.sourceSet.addAll(newSourceList);
  }
  public void addSource(String source) {
    this.sourceSet.add(source);
  }
  public Set<String> getSourceSet() { return this.sourceSet; }

  public Set<String> getSemanticTypeSet() { return this.semanticTypeSet; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.cui).append("|");
    sb.append(this.preferredName).append("|");
    sb.append(this.conceptString).append("|");
    sb.append(Arrays.toString(this.semanticTypeSet.toArray()).replaceAll("(^\\[)|(\\]$)",""));
    sb.append("|");
    sb.append(Arrays.toString(this.sourceSet.toArray()).replaceAll("(^\\[)|(\\]$)",""));
    return sb.toString();
  }

  public boolean equals(Object obj) {
    return ((ConceptInfo)obj).getCUI().equals(this.cui);
  }

  public int hashCode() {
    return this.cui.hashCode();
  }

}
