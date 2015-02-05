
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
   * the head each sublist smaller than the previous.
   */
  public static List<List<? extends Token>> applyHeadSubTokenLists(List<? extends Token> tokenList) {
    List<List<? extends Token>> listOfTokenLists = new ArrayList<List<? extends Token>>();
    for (int i = tokenList.size(); i > 0; i--) { 
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      listOfTokenLists.add(tokenSubList);
    }
    return listOfTokenLists;
  }

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
   */
  public static void applyHeadSubTokenListsOpt
				    (List<List<? extends Token>> listOfTokenLists,
				     List<? extends Token> tokenList) {
    for (int i = tokenList.size(); i > 0; i--) { 
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      listOfTokenLists.add(tokenSubList);
    }
  }

  public static List<List<? extends Token>> createSubListsOpt(List<? extends Token> tokenList) {
    List<List<? extends Token>> listOfTokenLists = new ArrayList<List<? extends Token>>();
    for (int i=0; i<tokenList.size(); i++) {
      applyHeadSubTokenListsOpt
	(listOfTokenLists, tokenList.subList(i,tokenList.size()));
    }
    return listOfTokenLists;
  }

  public static void processTokenList(List<Token> tokenList) {
    for (int i = 0; i < tokenList.size(); i++) {
      
    }
  }
}
