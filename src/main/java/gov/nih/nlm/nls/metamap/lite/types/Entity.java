package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Comparator;

/**
 * Class 
 */
public class Entity implements Comparable<Entity> {		// for lack of a better name.
  double score;
  String cui;
  String conceptName;
  String[] inputTextTokenList;
  String preferredName;
  public Entity(String cui, String name, String prefname, 
	 String[] textInputTokens, double scoreValue) {
    this.cui = cui;
    this.conceptName = name;
    this.preferredName = prefname; 
    this.inputTextTokenList = textInputTokens;
    this.score = scoreValue;
  }

  public int compareTo(Entity o) {
    if (this.getScore() != o.getScore()) {
      return Double.compare(this.getScore(),o.getScore());
    } 
    return this.getConceptName().compareTo(o.getConceptName());
  }
  public double getScore() { return this.score; }
  public String getCUI() { return this.cui; }
  public String getConceptName() { return this.conceptName; }
  public String getPreferredName() { return this.preferredName; }
  public String[] getInputTextTokenList() { return this.inputTextTokenList; }
  public void setScore(double value) { this.score = value; }
  public void setPreferredName(String name) { this.preferredName = name; }

  public static class EntityScoreComparator implements Comparator<Entity> {
    public int compare(Entity o1, Entity o2) {
      return Double.compare(o1.getScore(),o2.getScore());
    }
    public boolean equals(Object other) {
      return this == other;
    }
  }
  
  public static class EntityScoreConceptNameComparator implements Comparator<Entity> {
    public int compare(Entity o1, Entity o2) {
      if (o1.getScore() != o2.getScore()) {
	return Double.compare(o1.getScore(),o2.getScore());
      } 
      return o2.getConceptName().compareTo(o1.getConceptName());
    }
    public boolean equals(Object other) {
      return this == other;
    }
  }
}

