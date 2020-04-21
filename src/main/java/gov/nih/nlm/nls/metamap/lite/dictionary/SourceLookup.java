package gov.nih.nlm.nls.metamap.lite.dictionary;

import java.util.Set;

/**
 * Interface SourceLookup - must be implemented by classes that
 * support lookup of vocabulary sources in knowledge source
 * dictionaries.
 *
 *
 * Created: Sat Apr 18 19:07:33 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface SourceLookup {
  Set<String> getSourceSet(String cui);
}
