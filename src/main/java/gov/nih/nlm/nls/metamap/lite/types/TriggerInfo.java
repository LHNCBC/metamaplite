
//
package gov.nih.nlm.nls.metamap.lite.types;

import gov.nih.nlm.nls.metamap.lite.types.Span;

/**
 *
 */

public class TriggerInfo {
  /** synonym or preferredname closest to matched string */
  String conceptString;
  /** location of text: TXT, AB, TI, etc. */
  String location;
  /** matched text */
  String matchedText;
  /** part of speech of matched text */
  String partOfSpeech;
  /** which sentence in passage where matched text was found */
  int sentencePos;
  /** negation status: true if negated. */
  boolean negationStatus;
  /** start and end spans in passage. */
  Span span;

  public TriggerInfo(String conceptString,
		     String location,
		     String matchedText,
		     int sentencePos,
		     String partOfSpeech,
		     boolean negationStatus,
		     Span span) {
    this.conceptString = conceptString;
    this.location = location;
    this.matchedText = matchedText;
    this.sentencePos = sentencePos;
    this.partOfSpeech = partOfSpeech;
    this.negationStatus = negationStatus;
    this.span = span;
  }
  public String getConceptString() {
    return this.conceptString;
  }
  public String getLocation() {
    return this.location;
  }
  public String getPartOfSpeech() {
    return this.partOfSpeech;
  }
  public int getSentencePos() {
    return this.sentencePos;
  }
  public Span getSpan() {
    return this.span;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(this.conceptString).append("\"").append("-")
      .append(this.location).append("-")
      .append(this.sentencePos).append("-")
      .append("\"").append(this.matchedText).append("\"").append("-")
      .append(this.partOfSpeech).append("-")
      .append(this.negationStatus ? "1" : "0");
    return sb.toString();
  }
}
