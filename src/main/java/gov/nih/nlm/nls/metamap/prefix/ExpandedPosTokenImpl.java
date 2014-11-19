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
  /** position before expansion */
  int originalPosition;
  /**
   * Creates a new <code>ExpandedPosToken</code> instance.
   *
   */
  public ExpandedPosTokenImpl(String tokenText, int position, 
			  String originalText, int originalPosition) {
    super(tokenText, position);
    this.originalText = originalText;
    this.originalPosition = originalPosition;
  }
  public String getOriginalText() { return this.originalText; }
  public int getOriginalPosition() { return this.originalPosition; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(super.toString()).append("\"|").append(this.position);;
    sb.append("|\"").append(this.originalText).append("\"|").append(this.originalPosition);
    return sb.toString();
  }
}
