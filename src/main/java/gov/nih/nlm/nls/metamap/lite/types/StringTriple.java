
//
package gov.nih.nlm.nls.metamap.lite.types;

/**
 *
 */

public class StringTriple {
  String first;
  String second;
  String third;
  public StringTriple(String first, String second, String third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }
  public String getFirst() { return this.first; }
  public String getSecond() { return this.second; }
  public String getThird() { return this.third; }
}
