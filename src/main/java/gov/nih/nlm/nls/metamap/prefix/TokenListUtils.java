
//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Token;

/**
 *
 */

public class TokenListUtils {
  
  /** 
   * Create sequence of sublists of tokenlist always starting from
   * the beginning of original tokenlist.
   * @param tokenList original token list.
   * @return listOftokenlists list of token sublists.
   */
  public static List<List<? extends Token>> applyHeadSubTokenLists(List<? extends Token> tokenList) {
    List<List<? extends Token>> listOfTokenLists = new ArrayList<List<? extends Token>>();
    for (int i = tokenList.size(); i > 0; i--) { 
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      listOfTokenLists.add(tokenSubList);
    }
    return listOfTokenLists;
  }

  /** 
   * Create sequence of sublists of tokenlist always starting from
   * the head each sublist smaller than the previous.
   * @param tokenList original token list.
   * @return listOftokenlists list of token sublists.
   */
  public static List<List<? extends Token>> createSubLists(List<? extends Token> tokenList) {
    List<List<? extends Token>> listOfTokenLists = new ArrayList<List<? extends Token>>();
    for (int i=0; i<tokenList.size(); i++) {
      listOfTokenLists.addAll(applyHeadSubTokenLists
			      (tokenList.subList(i,tokenList.size())));
    }
    return listOfTokenLists;
  }

  /* optimized versions */

  /** 
   * Create sequence of sublists of tokenlist always starting from
   * the head each sublist smaller than the previous.
   * @param listOfTokenLists unpopulated list to contain token sublists.
   * @param tokenList original token list.
   */
  public static void applyHeadSubTokenListsOpt
				    (List<List<? extends Token>> listOfTokenLists,
				     List<? extends Token> tokenList) {
    for (int i = tokenList.size(); i > 0; i--) { 
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      listOfTokenLists.add(tokenSubList);
    }
  }

  /**
   * Generate token sublists from original list using successive heads
   * of original list.
   * @param tokenList original tokenlist.
   * @return list of token sublist
   */
  public static List<List<? extends Token>> createSubListsOpt(List<? extends Token> tokenList) {
    List<List<? extends Token>> listOfTokenLists = new ArrayList<List<? extends Token>>();
    for (int i=0; i<tokenList.size(); i++) {
      applyHeadSubTokenListsOpt
	(listOfTokenLists, tokenList.subList(i,tokenList.size()));
    }
    return listOfTokenLists;
  }

  public static void processTokenList(List<? extends Token> tokenList) {
    for (int i = 0; i < tokenList.size(); i++) {
      
    }
  }

  /**
   * Concatenate text components of tokens in tokenlist into one string.
   * @param tokenList list of tokens
   * @return string containing text contents concatenated.
   */
  public static String tokenListToString(List<? extends Token> tokenList) {
    StringBuilder sb = new StringBuilder();
    for (Token token: tokenList) {
      sb.append(token.getText());
    }
    return sb.toString();
  }
}
