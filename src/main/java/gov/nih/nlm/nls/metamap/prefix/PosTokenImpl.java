package gov.nih.nlm.nls.metamap.prefix;

import java.io.Serializable;

public class PosTokenImpl implements PosToken, Token, Serializable {
  String tokenText;
  int offset;
  public PosTokenImpl(String tokenText, int offset) {
    // this.tokenText = tokenText.intern();
    this.tokenText = tokenText;
    this.offset = offset;
  }
  public String getText() { return this.tokenText; }
  public int getOffset() { return this.offset; }
  @Deprecated
  public int getPosition() { return this.offset; }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(this.tokenText).append("\"|").append(this.offset);
    return sb.toString();
  }
  public boolean equals(Object o)
  { return this.tokenText.equals(((PosTokenImpl)o).tokenText); }
}
