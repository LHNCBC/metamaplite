
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
  double score;
  int start;
  int length;

  public Ev(ConceptInfo conceptInfo,  String matchedText,
	    int start, int length, double scoreValue) {
    this.conceptInfo = conceptInfo;
    this.matchedText = matchedText;
    this.start = start;
    this.length = length;
    this.score = scoreValue;
  }

  public Ev(Ev ev) {
    this.conceptInfo = ev.getConceptInfo();
    this.matchedText = ev.getMatchedText();
    this.start = ev.getStart();
    this.length = ev.getLength();
    this.score = ev.getScore();
  }

  public void setConceptInfo(ConceptInfo conceptInfo) { this.conceptInfo = conceptInfo; }
  public ConceptInfo getConceptInfo() { return this.conceptInfo; }
  public void setMatchedText(String text) { this.matchedText = text;  }
  public void setText(String text) { this.matchedText = text; }
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

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.id).append("|");
    sb.append(this.matchedText).append("|");
    sb.append(this.score).append("|");
    sb.append("|").append(this.start).append(":").append(this.length).append("|");
    sb.append(this.conceptInfo);
    return sb.toString();
  }
}
