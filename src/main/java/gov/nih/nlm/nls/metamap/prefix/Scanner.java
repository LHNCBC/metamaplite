
//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import gov.nih.nlm.nls.types.Sentence;

/**
 * Tokenize sentence then classify tokens, and add offsets.
 */

public class Scanner {

  // compiled regular expressions
  static Pattern wspattern = Pattern.compile("^\\s$");
  static Pattern anpattern = Pattern.compile("^[A-Za-z]+[0-9]+$");

  static Pattern ucpattern = Pattern.compile("^[A-Z][A-Z0-9]+$");
  static Pattern lcpattern = Pattern.compile("^[a-z]+$");
  static Pattern icpattern = Pattern.compile("^[A-Za-z]+$");
  static Pattern nupattern = Pattern.compile("^[0-9]+$");
  // Pattern pnpattern (re-pattern pattern);       ; using string.punctuation
  static Pattern pnpattern = Pattern.compile("^[\\(\\)!@#$%^&*\\+\\=\\-\\_\\[\\]\\{\\}\\.\\,\\?\\/\\']+$");
  static Pattern grpattern = Pattern.compile("^[\\p{InGreek}]+$");
  static Pattern chempattern = Pattern.compile("^[\\p{Alnum}\\p{InGreek}\\,\\[\\]\\+\\-\\(\\)]+$");

  static Pattern openparen = Pattern.compile("^\\($");
  static Pattern closeparen = Pattern.compile("^\\)$");
  static Pattern openbrack = Pattern.compile("^\\[$");
  static Pattern closebrack = Pattern.compile("^\\]$");
  static Pattern comma = Pattern.compile("^,$");
  static Pattern period = Pattern.compile("^.$");

  public static List<ERToken> addOffsets(List<ClassifiedToken> tokenlist, int start) {
    List<ERToken> newtokenlist = new ArrayList<ERToken>();
    int offset = start;
    for (ClassifiedToken token: tokenlist) {
      newtokenlist.add(new ERTokenImpl(token.getText(), offset, token.getTokenClass()));
      offset = offset + token.getText().length();
    }
    return newtokenlist;
  }

  public static List<ERToken> addOffsets(List<ClassifiedToken> tokenlist) {
    return addOffsets(tokenlist, 0);
  }

  /**
   * Classify tokens, metamap style.
   * <p>
   * What are the classes?:
   * <pre>
   *  "ws" - whitespace
   *  "an" - alphanumeric
   *  "pn" - punctuation
   *   "uc" - uppercase
   *  "ic" - ignore case
   *  "lc" - lowercase
   *  "nu" - numeric
   *  "gr" - greek
   *  "op" - open paren
   *  "cp" - close paren
   *  "ob" - open bracket
   *  "cb" - close bracket
   *  "cm" - comma
   *  "un" - unknown"
   * </pre>
   * @param tokenlist original unclassified tokenlist
   * @return tokenlist with classified tokens
   */
  public static List<ClassifiedToken> classifyTokenList(List<Token> tokenlist) {
    List<ClassifiedToken> newtokenlist = new ArrayList<ClassifiedToken>();
    // Clojure is much more concise representing this...
    for (Token token: tokenlist) {
      if (Scanner.wspattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "ws"));
      } else if (Scanner.anpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "an"));
      } else if (Scanner.ucpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "uc"));
      } else if (Scanner.lcpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "lc"));
      } else if (Scanner.icpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "ic"));
      } else if (Scanner.nupattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "nu"));
      } else if (Scanner.grpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "gr"));
      } else if (Scanner.openparen.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "op"));
      } else if (Scanner.closeparen.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "cp"));
      } else if (Scanner.openbrack.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "ob"));
      } else if (Scanner.closebrack.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "cb"));
      } else if (Scanner.comma.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "cm"));
      } else if (Scanner.period.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "pd"));
      } else if (Scanner.pnpattern.matcher(token.getText()).matches()) {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "pn"));
      } else {
	newtokenlist.add(new ClassifiedTokenImpl(token.getText(), "unknown"));
      }
    }
    return newtokenlist;
  }

  /**
   * Tokenize sentence then classify tokens, and add offsets.
   * @param text input text
   * @return tokenlist with token classification and token offsets in input text.
   */
  public static List<ERToken> analyzeText(String text) {
    System.out.println("analyzeText");
    return addOffsets(classifyTokenList(new ArrayList<Token>(Tokenize.mmPosTokenize(text,0))));
  }


  /**
   * Tokenize sentence then classify tokens, and add offsets.
   * @param text input text
   * @return tokenlist with token classification and token offsets in input text.
   */
  public static List<ERToken> analyzeText(Sentence sentence) {
    System.out.println("analyzeText");
    return addOffsets(classifyTokenList(new ArrayList<Token>(Tokenize.mmPosTokenize(sentence.getText(),0))),
		      sentence.getOffset());
  }

  public static List<ERToken> removeWhiteSpaceTokens(List<ERToken> tokenlist) {
    List<ERToken> newtokenlist = new ArrayList<ERToken>();
    for (ERToken token: tokenlist) {
      if (! token.getTokenClass().equals("ws")) {
	newtokenlist.add(token);
      }
    }
    return newtokenlist;
  }

  /**
   * Tokenize sentence then classify tokens, and add offsets, then remove whitespace tokens.
   * @param text input text
   * @return tokenlist with token classification and token offsets in input text.
   */
  public static List<ERToken> analyzeTextNoWS(String text) {
    return removeWhiteSpaceTokens(addOffsets
				  (classifyTokenList
				   (new ArrayList<Token>(Tokenize.mmPosTokenize(text,0)))));
  }
}
