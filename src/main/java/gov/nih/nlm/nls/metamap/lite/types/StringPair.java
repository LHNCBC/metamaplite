
//
package gov.nih.nlm.nls.metamap.lite.types;

/**
 *
 */

public class StringPair {
  String first;
  String second;
  public StringPair(String first, String second) {
    this.first = first;
    this.second = second;
  }
  public String getFirst() { return this.first; }
  public String getSecond() { return this.second; }
}
