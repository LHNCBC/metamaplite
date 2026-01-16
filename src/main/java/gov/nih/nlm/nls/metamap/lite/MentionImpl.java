package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import gov.nih.nlm.nls.types.Span;
import gov.nih.nlm.nls.types.Mention;

public class MentionImpl<T> implements Mention {

  String matchingText;
  List<Span> spanlist;
  T info;

  public MentionImpl(T info, List<Span> spanlist) {
    this.info = info;
    this.spanlist = spanlist;
  }

  public String getMatchingText() { return this.matchingText; }
  public List<Span> getSpanList() { return this.spanlist; }
  public T getInfo() { return this.info; }
}
