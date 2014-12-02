
//
package gov.nih.nlm.nls.metamap.lite.types;

import java.util.List;
import bioc.BioCAnnotation;
import gov.nih.nlm.nls.metamap.prefix.Token;


/**
 *
 */

public class TokenListAnnotation extends BioCAnnotation
{
  List<? extends Token> tokenList;

  public TokenListAnnotation(String id,
			     String text,
			     List<? extends Token> tokenList) {
    super();
    this.id = id;
    this.text = text;
    this.tokenList = tokenList;
  }

  public List<? extends Token> getTokenList() {
    return this.tokenList;
  }
}
