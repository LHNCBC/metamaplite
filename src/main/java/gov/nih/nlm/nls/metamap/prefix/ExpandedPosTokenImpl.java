package gov.nih.nlm.nls.metamap.prefix;

import java.io.Serializable;

/**
 * Describe class ExpandedPosToken here.
 *
 *
 * Created: Thu Jan 17 10:41:06 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class ExpandedPosTokenImpl extends PosTokenImpl implements Token, PosToken, ExpandedPosToken, Serializable {
  /** token text before expansion */
  String originalText;
  /** offset before expansion */
  int originalOffset;
  /**
   * Creates a new <code>ExpandedPosToken</code> instance.
   * @param tokenText text of token
   * @param offset of token in text
   * @param originalText original text
   * @param originalOffset original offset of token in text
   */
  public ExpandedPosTokenImpl(String tokenText, int offset, 
			  String originalText, int originalOffset) {
    super(tokenText, offset);
    this.originalText = originalText;
    this.originalOffset = originalOffset;
  }
  public String getOriginalText() { return this.originalText; }
  public int getOriginalOffset() { return this.originalOffset; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(super.toString()).append("\"|").append(this.offset);;
    sb.append("|\"").append(this.originalText).append("\"|").append(this.originalOffset);
    return sb.toString();
  }
}
