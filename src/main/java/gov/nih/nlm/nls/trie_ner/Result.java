
//
package gov.nih.nlm.nls.trie_ner;

/**
 *
 */

public class Result {
  String text;
  Span span;
  Object reference = null;

  public Result(String text, Span span) {
    this.text = text;
    this.span = span;
  }

  public Result(String text, Span span, Object reference) {
    this.text = text;
    this.span = span;
    this.reference = reference;
  }

  public String toString() {
    if (this.reference != null) {
      return "text: " + text + ", span: " + span + ", reference: " + reference;
    } else {
      return "text: " + text + ", span: " + span;
    }
  }
}
