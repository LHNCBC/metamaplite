package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
// import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Describe interface DictionaryLookup here.
 *
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
   * @param originalTerm original term
   * @param normTerm normalized form of term
   * @param tokenSubList a tokenlist 
   * @return a <code>TermInfo</code> value associated with input term.
   */
  T lookup(String originalTerm, String normTerm, List<? extends Token> tokenSubList);
  /**
   * Lookup term in dictionary
   *
   * @param originalTerm original term
   * @param normTerm normalized form of term
   * @return a <code>TermInfo</code> value associated with input term.
   */
  T lookup(String originalTerm, String normTerm);
}
