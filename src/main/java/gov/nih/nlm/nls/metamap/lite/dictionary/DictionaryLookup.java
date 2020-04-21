package gov.nih.nlm.nls.metamap.lite.dictionary;

/**
 * Simpilied interface for Dictionary Lookup implementations
 *
 * Created: Mon Apr 16 10:57:28 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface DictionaryLookup<T> {

  /**
   * Lookup term in dictionary
   *
   * @param term term to lookup
   * @return generic type T 
   */
  T lookup(String term);

}
