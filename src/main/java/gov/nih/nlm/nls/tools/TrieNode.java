package gov.nih.nlm.nls.tools;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Trie node interface.
 *
 *
 * Created: Tue Feb 19 15:11:11 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface TrieNode {
  void insert(List<Token> tokenList);
  boolean has(List<Token> tokenList);
  boolean hasPrefix(List<Token> tokenList);
  boolean getEndFlag();
  void setEndFlag(boolean value);
}
