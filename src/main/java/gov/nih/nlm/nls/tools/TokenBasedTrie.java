package gov.nih.nlm.nls.tools;

import java.io.Serializable;
import java.util.List;

import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Describe class TokenBasedTrie here.
 *
 *
 * Created: Tue Feb 19 15:10:22 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TokenBasedTrie implements Serializable {
  TrieNode r;

  /**
   * Creates a new <code>TokenBasedTrie</code> instance.
   *
   */
  public TokenBasedTrie() {
    r = new ListTrieNode();
  }

  public boolean has(List<Token> tokenList) {
    return r.has(tokenList);
  }

  public boolean hasPrefix(List<Token> tokenList) {
    return r.hasPrefix(tokenList);
  }

  public void insert(List<Token> tokenList) {
    r.insert(tokenList);
  }
}
