package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

/**
 * Describe interface ChunkerMethod here.
 *
 *
 * Created: Mon Mar 27 13:20:40 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface ChunkerMethod {
  List<Phrase> applyChunker(List<ERToken> tokenList);
}
