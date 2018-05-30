package gov.nih.nlm.nls.metamap.lite;

import java.util.Map;
import gov.nih.nlm.nls.metamap.prefix.ERToken;


/**
 * Describe interface TermFilter here.
 *
 *
 * Created: Fri Apr 27 09:40:09 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface TermFilter {
  /**
   * @param firstToken firstToken of List
   * @param contextInfo contextual information for token.
   * @return boolean: returns true if criteria met
   */
  boolean filterToken(ERToken firstToken, Map<String,Object> contextInfo);
}
