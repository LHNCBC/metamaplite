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
 * Entity class, a span with information attached. 
 */
public class Entity implements Annotation, Comparable<Entity>
{		// for lack of a better name.
  /** entity id */
  String id = "en0";
  /** id of document entity occurred in. */
  String docid = "0000000";
  /** id of field  entity occurred in. */
  String fieldId = "TXT";
  /** lexical category of entity */
  String lexicalCategory = "UNK";
  /** ordinal number of sentence entity occurred in */
  int sentenceNumber = 1;
  /** text that matched entity */
  String matchedText;
  // offset in the original text
  int start;
  int length;
  double score;
  /** negation status from ConText or other negation detection program */
  boolean negationStatus = false;
  /** temporality set from ConText or other program */
  String temporality = "";	
  int locationPosition = 0;	
  Set<Ev> evSet;

  public Entity(String docid,  
		String matchedText,
		int start, int length,
		double scoreValue,
		Set<Ev> evSet) {
    this.docid = docid;
    this.matchedText = matchedText;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = evSet;
  }

  public Entity(String id,
		String docid,  
		String matchedText,
		int start, int length,
		double scoreValue,
		Set<Ev> evSet) {
    this.id = id;
    this.docid = docid;
    this.matchedText = matchedText;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = evSet;
  }

  public Entity(String docid,  
		String matchedText,
		int start, int length,
		double scoreValue,
		List<Ev> evList) {
    this.docid = docid;
    this.matchedText = matchedText;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = new HashSet<Ev>(evList);
  }


  public Entity(String id,
		String docid,
		String fieldId,
		String matchedText,
		String lexicalCategory,
		int sentenceNumber,
		int start, int length,
		double scoreValue,
		Set<Ev> evSet) {
    this.id = id;
    this.docid = docid;
    this.fieldId = fieldId;
    this.matchedText = matchedText;
    this.lexicalCategory = lexicalCategory;
    this.sentenceNumber = sentenceNumber;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = evSet;
  }


  public Entity(String docid,
		String fieldId,
		String matchedText,
		String lexicalCategory,
		int sentenceNumber,
		int start, int length,
		double scoreValue,
		Set<Ev> evSet) {
    this.docid = docid;
    this.fieldId = fieldId;
    this.matchedText = matchedText;
    this.lexicalCategory = lexicalCategory;
    this.sentenceNumber = sentenceNumber;
    this.score = scoreValue;
    this.start = start;
    this.length = length;
    this.evSet = evSet;
  }


  public Entity(Entity entity) {
    this.docid = entity.getDocid();
    this.fieldId = entity.getFieldId();
    this.lexicalCategory = entity.getLexicalCategory();
    this.matchedText = entity.getMatchedText();
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
      return Double.compare(this.getScore(), o.getScore());
    }
    if (! this.getEvList().equals(o.getEvList())) { 
      return Integer.compare(o.getEvList().size(), this.getEvList().size());
    }
    return this.getText().compareTo(o.getText());
  }
  public String getDocid() { return this.docid; }
  public double getScore() { return this.score; }

  /** get start position of input text 
   * @return start position as an integer
   */
  public int getStart() { return this.start; }
  public void setScore(double value) { this.score = value; }
  /** set start position of entity in text 
   * @param start start position of entity in text */
  public void setStart(int start) { this.start = start; }
  /** set length of entity in text
   * @param length length of entity in text */
  public void setLength(int length) { this.length = length; }

  public List<Ev> getEvList() { return new ArrayList<Ev>(this.evSet); }
  public Set<Ev> getEvSet() { return this.evSet; }
  public void addEv(Ev ev) { this.evSet.add(ev); }
  public void addAllEv(Collection<Ev> evCollection) { this.evSet.addAll(evCollection); }
  public void setEvList(List<Ev> newEvList) { this.evSet.addAll(newEvList); }
  public void setEvSet(Set<Ev> newEvSet) {
    this.evSet.clear();
    this.evSet.addAll(newEvSet);
  }

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

  public String getFieldId() {
    return this.fieldId;
  }

  public String getLexicalCategory() {
    return this.lexicalCategory;
  }

  public int getSentenceNumber() {
    return this.sentenceNumber;
  }

  @Override
    public String getType() {
    return "concept";
  }

  /** get offset of entity in the text. */  
  @Override
    public int getOffset() {
    return this.start;
  }

  /** get length of entity in text */
  @Override
  public int getLength() {
    return this.length;
  }

  /** get entity text */  
  @Override
  public String getText() {
    return this.matchedText;
  }

  /** get matched text of entity
   * @return String containing matched text.
   */  
  public String getMatchedText() {
    return this.matchedText;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append(this.docid).append("|").append(this.id).append("|");
    sb.append(this.matchedText).append("|").append(this.fieldId);
    sb.append("|").append(this.start).append(":").append(this.length).append("|");
    for (Ev ev: this.evSet) {
      sb.append(ev.toString().replace("+","~").replace("|","+")).append("|");
    }
    sb.append(this.negationStatus ? "negated" : "affirmed");
    return sb.toString();
  }

  public boolean equals(Object obj) {
    return (((Entity)obj).start == this.start) &&
      (((Entity)obj).length == this.length);
  }

  public int hashCode() {
    return this.length + this.start;
  }

}


