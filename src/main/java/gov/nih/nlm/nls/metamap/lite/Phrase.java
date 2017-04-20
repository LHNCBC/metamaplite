package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

/**
 * Describe interface PhraseChunk here.
 *
 *
 * Created: Mon Mar 27 16:54:11 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface Phrase {
  List<ERToken> getPhrase();
  String getTag();
}
