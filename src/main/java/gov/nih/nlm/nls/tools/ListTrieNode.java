package gov.nih.nlm.nls.tools;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;

/**
 * Describe class TrieNode here.
 *
 *
 * Created: Tue Feb 19 15:11:11 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class ListTrieNode implements TrieNode, Serializable {
  /** list of children of this node */
  public Map<String,TrieNode> childMap;	
  /** end of token list */ 
  public boolean endFlag = false;
  
  /**
   * Creates a new <code>TrieNode</code> instance.
   *
   */
  public ListTrieNode() {
    this.childMap = new TreeMap<String,TrieNode>();
  }

  public void insert(List<Token> tokenList) {
    // System.out.println("insert> " + Tokenize.getTextFromTokenList(tokenList));
    String key = tokenList.get(0).getText();

    // if node for key is not present, then add it.
    if (! this.childMap.containsKey(key)) {
      this.childMap.put(key, new ListTrieNode());
    }

    // If token list length > 1 then continue adding to trie,
    // otherwise set flag to state a token sequence ends here.
    if (tokenList.size() > 1) {
      this.childMap.get(key).insert(tokenList.subList(1,tokenList.size()));
    } else {
      this.childMap.get(key).setEndFlag(true);
    }
  }

  public boolean has(List<Token> tokenList) {

    String key = tokenList.get(0).getText();
    // System.out.println("has (key:" + key + ") (seq len: " + tokenList.size() 
    // + ")> input:" + Tokenize.getTextFromTokenList(tokenList));

    if (this.childMap.containsKey(key)) {
      // System.out.println(key + " -> endFlag: " + this.childMap.get(key).getEndFlag());
	if (tokenList.size() > 1) {
      // found token match, check rest of tokenlist
	  return this.childMap.get(key).has(tokenList.subList(1,tokenList.size()));
	} else if (this.childMap.get(key).getEndFlag() == true && tokenList.size() == 1) {
      // found end of valid token sequence, return true.
	  return true;
	}
      }
    // token sequence match not found.
    return false;
  }

  public boolean hasPrefix(List<Token> tokenList) {
    String key = tokenList.get(0).getText();
    if (this.childMap.containsKey(key)) {
	if (tokenList.size() > 1) {
	  // found token match, check rest of tokenlist
	  return this.childMap.get(key).hasPrefix(tokenList.subList(1,tokenList.size()));
	} else if (tokenList.size() == 1) {
	  // found end of valid token prefix sequence, return true.
	  return true;
	}
      }
    // token sequence match not found.
    return false;
  }

  public boolean getEndFlag() { return this.endFlag; }
  public void setEndFlag(boolean value) { this.endFlag = value; }

}
