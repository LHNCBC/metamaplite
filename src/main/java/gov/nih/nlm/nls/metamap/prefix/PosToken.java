package gov.nih.nlm.nls.metamap.prefix;

import java.io.Serializable;

public interface PosToken extends Token {
  /**
   * @return int position or offset of token in original input text.
   */
  int getOffset();
  /**
   * @return int position or offset of token in original input text.
   * @deprecated
   */
  @Deprecated
  int getPosition();
}
