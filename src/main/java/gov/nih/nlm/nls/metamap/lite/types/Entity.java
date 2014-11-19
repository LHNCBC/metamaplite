package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * Class 
 */
public class Entity implements Comparable<Entity> {		// for lack of a better name.
  double score;
  String cui;
  Set<String> matchedWordSet = null;
  Set<String> sourceSet = null; 
  Set<String> semanticTypeSet = null; 
  String[] inputTextTokenList;
  String preferredName;
  // offset in the text
  int start;
  int end;

  public Entity(String cui, String word, String prefname, String source,
		String[] textInputTokens, int start, int end,
		double scoreValue) {
    this.cui = cui;
    this.matchedWordSet = new HashSet<String>();
    this.matchedWordSet.add(word);
    this.preferredName = prefname; 
    this.sourceSet = new HashSet<String>();
    this.sourceSet.add(source);
    this.semanticTypeSet = new HashSet<String>();
    this.inputTextTokenList = textInputTokens;
    this.score = scoreValue;
    this.start = start;
    this.end = end;
  }


  public Entity(String cui, String word, String prefname, 
		Set<String> newSourceSet,
		Set<String> newSemanticTypeSet,
		String[] textInputTokens, int start, int end,
		double scoreValue) {
    this.cui = cui;
    this.matchedWordSet = new HashSet<String>();
    this.matchedWordSet.add(word);
    this.preferredName = prefname; 
    this.sourceSet = newSourceSet;
    this.semanticTypeSet = newSemanticTypeSet;
    this.inputTextTokenList = textInputTokens;
    this.score = scoreValue;
    this.start = start;
    this.end = end;
  }

  public int compareTo(Entity o) {
    if (this.getScore() != o.getScore()) {
      return Double.compare(this.getScore(),o.getScore());
    } 
    return this.getPreferredName().compareTo(o.getPreferredName());
  }
  public double getScore() { return this.score; }
  public String getCUI() { return this.cui; }
  public void addMatchedWord(String word) { this.matchedWordSet.add(word); }
  public Set<String> getMatchedWordSet() { return this.matchedWordSet; }
  public String getPreferredName() { return this.preferredName; }
  public String[] getInputTextTokenList() { return this.inputTextTokenList; }
  public int getStart() { return this.start; }
  public int getEnd() { return this.end; }
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
    sb.append(this.cui).append("|").append(this.preferredName).append("|");
    sb.append(Arrays.toString(this.matchedWordSet.toArray()).replaceAll("(^\\[)|(\\]$)", ""));
    sb.append("|");
    sb.append(Arrays.toString(this.semanticTypeSet.toArray()).replaceAll("(^\\[)|(\\]$)", ""));
    sb.append("|");
    sb.append(Arrays.toString(this.sourceSet.toArray()).replaceAll("(^\\[)|(\\]$)", ""));
    sb.append("|").append(this.start).append(":").append(this.end).append("|");

    return sb.toString();
  }
}

