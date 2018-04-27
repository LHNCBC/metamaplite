package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;

/**
 * Describe class TokenUtils here.
 *
 *
 * Created: Wed May 22 15:23:11 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TokenUtils {
  public static boolean isNumber(Token token) {
    try {
      Integer.parseInt(token.getText());
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }
  public static boolean isLeftParen(Token token) {
    return token.getText().equals("(");
  }
  public static boolean isRightParen(Token token) {
    return token.getText().equals(")");
  }
  public static boolean isSpace(Token token) {
    return token.getText().equals(" ") ||
      token.getText().equals("\t") ||
      token.getText().equals("\n");
  }


  public static boolean isTokenContentEqual(String content, Token token) {
    return token.getText().equals(content);
  }

  /** return next printable token in tokenlist
   * @param tokenList token list
   * @param start start character position
   * @return next printable token
   */
  public static Token nextPrintableToken(List<Token> tokenList, int start) {
    int i = start;
    while (i < tokenList.size()) {
      if (CharUtils.isWhiteSpace(tokenList.get(i).getText().charAt(0))) {
	i++;
      } else {
	break;
      }
    }
    return (i < tokenList.size()) ? tokenList.get(i) : null;
  }
  /** return position of next printable token in tokenlist 
   * @param tokenList token list
   * @param start start character position
   * @return next printable token position
   */
  public static int nextPrintableTokenPosition(List<Token> tokenList, int start) {
    int i = start;
    while (i < tokenList.size()) {
      if (CharUtils.isWhiteSpace(tokenList.get(i).getText().charAt(0))) {
	i++;
      } else {
	break;
      }
    }
    return i;
  }






}
