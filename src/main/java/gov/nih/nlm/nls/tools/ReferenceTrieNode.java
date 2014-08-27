package gov.nih.nlm.nls.tools;

import java.util.List;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 * Describe interface ReferenceTrieNode here.
 *
 *
 * Created: Tue Jul  9 10:26:47 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface ReferenceTrieNode extends TrieNode {
  void insert(List<Token> tokenList, Object reference);
  Object hasReference(List<Token> tokenList);
}
