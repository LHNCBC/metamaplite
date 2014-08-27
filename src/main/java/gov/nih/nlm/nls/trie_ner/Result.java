
//
package gov.nih.nlm.nls.trie_ner;

/**
 *
 */

public class Result {
  String text;
  Span span;

  public Result(String text, Span span) {
    this.text = text;
    this.span = span;
  }

  public String toString() {
    return "text: " + text + ", span: " + span;
  }
}
