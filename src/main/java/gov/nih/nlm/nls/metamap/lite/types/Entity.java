package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.types.Annotation;

/**
 * Class 
 */
public class Entity implements Annotation, Comparable<Entity>
{		// for lack of a better name.
  String docid;
  String id = "en0";
  String matchedText;
  // offset in the original text
  int start;
  int length;
  double score;
  boolean negationStatus = false; // negation status from ConText
  String temporality = "";	// temporality set from ConText
  int locationPosition = 0;	// 
  Set<Ev> evSet;

  public Entity(String docid,  
		String matchedText, int start, int length,
		double scoreValue,
		Set<Ev> evSet) {
    this.docid = docid;
    this.matchedText = matchedText;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = evSet;
  }

  public Entity(String docid,  
		String matchedText, int start, int length,
		double scoreValue,
		List<Ev> evList) {
    this.docid = docid;
    this.matchedText = matchedText;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = new HashSet<Ev>(evList);
  }

  public Entity(Entity entity) {
    this.docid = entity.getDocid();
    this.matchedText = getMatchedText();
    this.score = entity.getScore();
    this.start = entity.getStart();
    this.length = entity.getLength();    
    this.evSet = new HashSet<Ev>(entity.getEvList());
  }

  public void setMatchedText(String text) {
    this.matchedText = text;
  }
  public void setText(String text) {
    this.matchedText = text;
  }
  public void setNegated(boolean value) {
    this.negationStatus = value;
  }
  public boolean isNegated() {
    return this.negationStatus;
  }
  public void setTemporality(String value) {
    this.temporality = value;
  }
  public String getTemporality() {
    return this.temporality;
  }
  public void setLocationPosition(int i) {
    this.locationPosition = i;
  }
  public int getLocationPosition() {
    return this.locationPosition;
  }
  public int compareTo(Entity o) {
    if (this.getScore() != o.getScore()) {
      return Double.compare(this.getScore(),o.getScore());
    } 
    return this.getText().compareTo(o.getText());
  }
  public String getDocid() { return this.docid; }
  public double getScore() { return this.score; }
  
  public int getStart() { return this.start; }
  public void setScore(double value) { this.score = value; }
  public void setStart(int start) { this.start = start; }
  public void setLength(int length) { this.length = length; }

  public List<Ev> getEvList() { return new ArrayList<Ev>(this.evSet); }
  public Set<Ev> getEvSet() { return this.evSet; }
  public void addEv(Ev ev) { this.evSet.add(ev); }
  public void addAllEv(Collection<Ev> evCollection) { this.evSet.addAll(evCollection); }
  public void setEvList(List<Ev> newEvList) { this.evSet.addAll(newEvList); }

  public static class EntityScoreComparator implements Comparator<Entity> {
    public int compare(Entity o1, Entity o2) {
      return Double.compare(o1.getScore(),o2.getScore());
    }
    public boolean equals(Object other) {
      return this == other;
    }
    public int hashCode() {
      return this.hashCode();
    }
  }
  
  // this should probably compare member of matchedWordSet?
  //
  // public static class EntityScoreConceptNameComparator implements Comparator<Entity> {
  //   public int compare(Entity o1, Entity o2) {
  //     if (o1.getScore() != o2.getScore()) {
  // 	return Double.compare(o1.getScore(),o2.getScore());
  //     } 
  //     return o2.getPreferredName().compareTo(o1.getPreferredName());
  //   }
  //   public boolean equals(Object other) {
  //     return this == other;
  //   }
  // }

  @Override
    public String getId() {
    return this.id;
  }

  @Override
    public String getType() {
    return "concept";
  }

  @Override
    public int getOffset() {
    return this.start;
  }

  @Override
  public int getLength() {
    return this.length;
  }

  @Override
  public String getText() {
    return this.matchedText;
  }

  public String getMatchedText() {
    return this.matchedText;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append(this.docid).append("|").append(this.id).append("|");
    sb.append(this.matchedText).append("|");
    sb.append("|").append(this.start).append(":").append(this.length).append("|");
    for (Ev ev: this.evSet) {
      sb.append(ev).append("|");
    }
    return sb.toString();
  }
}




