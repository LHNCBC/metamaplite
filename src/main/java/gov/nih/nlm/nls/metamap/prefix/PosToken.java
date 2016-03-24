package gov.nih.nlm.nls.metamap.prefix;

import java.io.Serializable;

public interface PosToken extends Token {
  int getOffset();
  /**
   * @deprecated
   */
  @Deprecated
  int getPosition();
}
