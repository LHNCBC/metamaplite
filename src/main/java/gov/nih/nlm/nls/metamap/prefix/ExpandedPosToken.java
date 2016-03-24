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
public interface ExpandedPosToken extends Token, PosToken, Serializable {
  String getOriginalText();
  int getOriginalOffset();
}
