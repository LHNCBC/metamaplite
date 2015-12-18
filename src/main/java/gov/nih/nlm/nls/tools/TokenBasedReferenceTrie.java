package gov.nih.nlm.nls.tools;

import java.io.Serializable;
import java.util.List;

import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Describe class TokenBasedReferenceTrie here.
 *
 * <pre>
 * TokenBasedReferenceTrie trie = new TokenBasedReferenceTrie();
 * trie.insert(Tokenize.mmPosTokenize("Fas antigen", 0), new Integer(355));
 * trie.insert(Tokenize.mmPosTokenize("fas antigen", 0), new Integer(355));
 * trie.insert(Tokenize.mmPosTokenize("Fas ligand",  0), new Integer(356));
 * trie.insert(Tokenize.mmPosTokenize("fas ligand",  0), new Integer(356));
 * System.out.println("trie.hasReference(Tokenize.mmPosTokenize(\"fas antigen\", 0)) -&gt; " + 
 * 		       trie.hasReference(Tokenize.mmPosTokenize("fas antigen", 0)));
 * </pre>
 * Created: Tue Jul  9 13:35:13 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TokenBasedReferenceTrie implements Serializable {
  public ReferenceTrieNode r;

  /**
   * Creates a new <code>TokenBasedReferenceTrie</code> instance.
   *
   */
  public TokenBasedReferenceTrie() {
    r = new ObjectListTrieNode(null);
  }

  public boolean has(List<Token> tokenList) {
    return r.has(tokenList);
  }

  public boolean hasPrefix(List<Token> tokenList) {
    return r.hasPrefix(tokenList);
  }

  public Object hasReference(List<Token> tokenList) {
    return r.hasReference(tokenList);
  }

  public void insert(List<Token> tokenList, Object reference) {
    r.insert(tokenList, reference);
  }

}
