package gov.nih.nlm.nls.metamap.lite;

import java.util.Properties;

/**
 * MMLInstantiate - interface for instantiating class instances used
 * by MetaMapLite application.
 *
 * Created: Tue Jun 11 13:08:44 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface MMLInstantiate {
  /**
   * Initialize dictionary lookup implementation instance.
   * @param properties run-time application properties instance.
   */
  void init(Properties properties);

  // should release be implemented?

}
