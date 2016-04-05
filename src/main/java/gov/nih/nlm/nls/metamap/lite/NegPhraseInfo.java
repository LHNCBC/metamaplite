package gov.nih.nlm.nls.metamap.lite;
import java.util.List;

/**
 * Negation Phrase Information Class
 */
public class NegPhraseInfo {
  List<String> phrase;
  String type;
  List<Integer> positionList;
  public NegPhraseInfo(List<String> phrase,
		       String type,
		       List<Integer> posList) {
    this.phrase = phrase;
    this.type = type;
    this.positionList = posList;
  }
  List<String> getPhrase() { return this.phrase; }
  String getType() { return this.type; }
  List<Integer> getPositionList() { return this.positionList; }
  public String toString() {
    return this.phrase + ", " + this.type + ", " + this.positionList;
  }
}
