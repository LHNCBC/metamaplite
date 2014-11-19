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
public class ObjectListTrieNode extends ListTrieNode implements TrieNode, ReferenceTrieNode, Serializable {
  /** list of children of this node */
  public Map<String,ObjectListTrieNode> childMap;	
  /** object the trie sequence refers to. */
  public Object reference;

  /**
   * Creates a new <code>TrieNode</code> instance.
   *
   */
  public ObjectListTrieNode(Object reference) {
    super();
    this.childMap = new TreeMap<String,ObjectListTrieNode>();
    this.reference = reference;
  }

  public void insert(List<Token> tokenList, Object reference) {
    Map<String,ObjectListTrieNode> currentChildMap = this.childMap;
    Map<String,ObjectListTrieNode> parentChildMap = null;
    String lastKey = "";
    for (Token token: tokenList) {
      String key = token.getText();
      if (! currentChildMap.containsKey(key)) {
	// System.out.println("insert: put key: \"" + key + "\"");
	currentChildMap.put(key, new ObjectListTrieNode(null));
      }
      parentChildMap = currentChildMap;
      currentChildMap = currentChildMap.get(key).childMap;
      lastKey = key;
    }
    // System.out.println("insert: setReference key: \"" + lastKey + "\": " + reference);
    ObjectListTrieNode trieNode = parentChildMap.get(lastKey);
    if (trieNode != null) {
      trieNode.setReference(reference);
      trieNode.setEndFlag(true);
    }
  }

  public Object hasReference(List<Token> tokenList) {
    Map<String,ObjectListTrieNode> currentChildMap = this.childMap;
    int i = 0;
    for (Token token: tokenList) {
      String key = token.getText();
      if (i == (tokenList.size() - 1)) {
	if (currentChildMap.containsKey(key)) {
	  if (currentChildMap.get(key).getEndFlag()) {
	    return currentChildMap.get(key).getReference();
	  }
	} else {
	  return null;
	}
      } else {
	if (currentChildMap.containsKey(key)) {
	  // found token match, check rest of tokenlist
	  currentChildMap = currentChildMap.get(key).childMap;
	}
      }
      i++;
    }
    return null;
  }

  public boolean has(List<Token> tokenList) {
    Map<String,ObjectListTrieNode> currentChildMap = this.childMap;
    int i = 0;
    for (Token token: tokenList) {
      String key = token.getText();
      if (i == (tokenList.size() - 1)) {
	if (currentChildMap.containsKey(key)) {
	  if (currentChildMap.get(key).getEndFlag()) {
	    return currentChildMap.get(key).getReference() != null;
	  }
	} else {
	  return false;
	}
      } else {
	if (currentChildMap.containsKey(key)) {
	  // found token match, check rest of tokenlist
	  currentChildMap = currentChildMap.get(key).childMap;
	}
      }
      i++;
    }
    return false;
  }

  public boolean hasPrefix(List<Token> tokenList) {
    Map<String,ObjectListTrieNode> currentChildMap = this.childMap;
    int i = 0;
    for (Token token: tokenList) {
      String key = token.getText();
      if (currentChildMap.containsKey(key)) {
	  // found token match, return true;
	  return true;
      } else {
	if (i == (tokenList.size() - 1)) {
	  if (currentChildMap.containsKey(key)) {
	    if (currentChildMap.get(key).getEndFlag()) {
	      return currentChildMap.get(key).getReference() != null;
	    }
	  } else {
	    return false;
	  }
	}
      }
      i++;
    }
    return false;
  }

  public boolean getEndFlag() { return this.endFlag; }
  public void setEndFlag(boolean value) { this.endFlag = value; }
  public Object getReference() { return this.reference; }
  public void setReference(Object reference) { this.reference = reference; }
}
