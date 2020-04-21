package gov.nih.nlm.nls.metamap.lite.dictionary;

import java.util.Set;

/**
 * Interface SemanticTypeLookup - must be implemented by classes that
 * support lookup of semantic type identifiers in knowledge source
 * dictionaries.
 *
 *
 * Created: Sat Apr 18 19:08:21 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface SemanticTypeLookup {
  Set<String> getSemanticTypeSet(String cui);
}
