
//
package gov.nih.nlm.nls.trie_ner;

/**
 *
 */

public class Span {
  public long start;
  public long end;

  public Span(int start, int end) {
    this.start = start; this.end = end;
  }
  public long getStart() {
    return start;
  }
  public long getEnd() {
    return end;
  }
  public String toString() {
    return "start: " + start + ", end: " + end;
  }
}
