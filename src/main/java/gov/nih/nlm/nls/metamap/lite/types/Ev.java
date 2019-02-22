
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

public class Ev implements Annotation {
  ConceptInfo conceptInfo;

  String id = "ev0";
  // Set<String> matchedWordList = null;
  String matchedText;
  /** matching UMLS string either synonym or preferred name */
  String conceptString;
  double score;
  int start;
  int length;
  String partOfSpeech;

  public Ev(ConceptInfo conceptInfo,  String matchedText,
	    String conceptString,
	    int start, int length, double scoreValue,
	    String partOfSpeech) {
    this.conceptInfo = conceptInfo;
    this.matchedText = matchedText.intern();
    this.conceptString = conceptString.intern();
    this.start = start;
    this.length = length;
    this.score = scoreValue;
    this.partOfSpeech = partOfSpeech;
  }

  public Ev(ConceptInfo conceptInfo,  String matchedText,
	    String conceptString,
	    int start, int length, double scoreValue) {
    this.conceptInfo = conceptInfo;
    this.matchedText = matchedText.intern();
    this.conceptString = conceptString.intern();
    this.start = start;
    this.length = length;
    this.score = scoreValue;
  }

  public Ev(Ev ev) {
    this.conceptInfo = ev.getConceptInfo();
    this.matchedText = ev.getMatchedText().intern();
    this.conceptString = ev.getConceptString();
    this.start = ev.getStart();
    this.length = ev.getLength();
    this.score = ev.getScore();
  }

  public void setConceptInfo(ConceptInfo conceptInfo) { this.conceptInfo = conceptInfo; }
  public ConceptInfo getConceptInfo() { return this.conceptInfo; }
  public void setMatchedText(String text) { this.matchedText = text.intern();  }
  public String getConceptString() { return this.conceptString; }
  public void setText(String text) { this.matchedText = text.intern(); }
  public double getScore() { return this.score; }
  public void setScore(double value) { this.score = value; }
  public int getStart() { return this.start; }
  public void setStart(int start) { this.start = start; }
  public void setLength(int length) { this.length = length; }
  public void setId(String id) { this.id = id; }

  @Override
  public String getId() { return this.id; }

  @Override
    public String getType() { return "ev"; }

  @Override
    public int getOffset() { return this.start; }

  @Override
  public int getLength() { return this.length; }

  @Override
  public String getText() { return this.matchedText; }

  public String getMatchedText() { return this.matchedText; }

  public String getPartOfSpeech() { return this.partOfSpeech; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.id).append("|");
    sb.append(this.matchedText).append("|");
    sb.append(this.score).append("|");
    sb.append("|").append(this.start).append(":").append(this.length).append("|");
    sb.append(this.conceptInfo.toString().replace("|","+"));
    return sb.toString();
  }

  public boolean equals(Object obj) {
    return (((Ev)obj).start == this.start) &&
      (((Ev)obj).length == this.length) &&
      (((Ev)obj).getConceptInfo().equals(this.conceptInfo));
  }

  public int hashCode() {
    return this.length + this.start + this.conceptInfo.hashCode();
  }

}
