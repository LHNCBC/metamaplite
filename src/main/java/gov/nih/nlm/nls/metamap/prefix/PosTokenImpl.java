package gov.nih.nlm.nls.metamap.prefix;

import java.io.Serializable;

public class PosTokenImpl implements PosToken, Token, Serializable {
  String tokenText;
  int position;
  public PosTokenImpl(String tokenText, int position) {
    this.tokenText = tokenText.intern();
    this.position = position;
  }
  public String getText() { return this.tokenText; }
  public int getPosition() { return this.position; }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(this.tokenText).append("\"|").append(this.position);
    return sb.toString();
  }
  public boolean equals(Object o)
  { return this.tokenText.equals(((PosTokenImpl)o).tokenText); }
}
