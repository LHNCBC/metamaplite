
//
package gov.nih.nlm.nls.trie_ner;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.tools.TokenBasedReferenceTrie;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.PosToken;

/**
 *
 */

public class TokenTrieNER {
  int maxTokenScan = 30;

  public List<Result> maximalRightScanIterative(TokenBasedReferenceTrie trie, 
					  List<Token> inputTokenlist) {
    List<Token> tokenlist = inputTokenlist.subList(0, Math.min(maxTokenScan, inputTokenlist.size()));
    List<Result> resultList = new ArrayList<Result>();
    /*
      check term using expanding window of tokens expanding to right.
     */
    for (int n = 1; n < Math.min(inputTokenlist.size(), maxTokenScan); n++) {
      List<Token> candidate = inputTokenlist.subList(0,n);
      if (trie.has(candidate)) {
	if (trie.hasReference(candidate) != null) {
	  resultList.add(new Result(Tokenize.getTextFromTokenList(candidate),
				    new Span(((PosToken)candidate.get(0)).getPosition(),
					     ((PosToken)candidate.get(0)).getPosition() + 
					     Tokenize.getTextFromTokenList(candidate).length()),
				    trie.hasReference(candidate)));
	} else {
	  resultList.add(new Result(Tokenize.getTextFromTokenList(candidate),
				    new Span(((PosToken)candidate.get(0)).getPosition(),
					     ((PosToken)candidate.get(0)).getPosition() + 
					     Tokenize.getTextFromTokenList(candidate).length())));
	}
      }
    }
    return resultList;
  }

  public List<Result> tagText(TokenBasedReferenceTrie trie, String inputText) {
    List<Token> tokenlist = Tokenize.mmPosTokenize(inputText, 0);
    List<Result> resultList = new ArrayList<Result>();
    for (int idx = 0; idx< tokenlist.size(); idx++) {
      // System.out.println("prefix: " + tokenlist.subList(idx, idx+1));
      if (trie.hasPrefix(tokenlist.subList(idx, idx+1))) {
	resultList.addAll(maximalRightScanIterative(trie, tokenlist.subList(idx, tokenlist.size())));
      }
    }
    return resultList;
  }

}
