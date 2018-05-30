package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Describe interface TermInfo here.
 *
 *
 * Created: Mon Apr 16 11:01:50 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface TermInfo<T> {
  /** normalized form of term
   * @return string containing normalized form of term
   */
  String getNormTerm();
  /** original term 
   * @return string containing original term
   */
  String getOriginalTerm();
  /** get term tokenList 
   * @return list of token generated from term.
   */
  List<? extends Token> getTokenList();
  /**
   * get dictionary info 
   * @return dictionary of type T
   */
  T getDictionaryInfo();
}
