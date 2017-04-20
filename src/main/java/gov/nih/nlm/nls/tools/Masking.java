package gov.nih.nlm.nls.tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.TokenListMatch;

/**
 * Describe class Masking here.
 *
 *
 * Created: Thu Feb 21 08:59:09 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Masking {

  /** troublesome terms - terms that appear as genes to recognizers */
  public static String[] troublesomeList = {
    "polymerase chain reaction", "vitamin b12",
    "vitamin-b12", "insulin resistant", "insulin resistance", "ace-inhibitor",
    "ace inhibitor", "ace-inhibitors", "ace inhibitors", "array cgh",
    "microarray cgh", "cgh array", "cgh microarray"
  };
  public static List<String[]> troublesomeTokenArrayList = new ArrayList<String[]>();
  static {
    // tokenize strings 
    for (String term: troublesomeList) {
      troublesomeTokenArrayList.add
	(Tokenize.mmTokenize(term, Tokenize.KEEP_WHITE_SPACE));
    }
  }


  /**
   * Mask troublesome terms.
   * <p>
   * Tokenize input text while preserving white space tokens, replace
   * troublesome term-tokens with 'XXX...' and re-constitute input
   * string from newtokens.
   *
   * @param inText text to be processed.
   * @return text with stopwords X'ed out.
   */
  public static String maskTroublesome(String inText)
  {
    StringBuilder sb = new StringBuilder();
    String[] inTokens = Tokenize.mmTokenize(inText, Tokenize.KEEP_WHITE_SPACE);
    int i = 0;
    while (i < inTokens.length){
      int tokenCount = 1;
      String outString = inTokens[i];
      // traverse troublesome list
      for (String[] troublesomeTokenArray: troublesomeTokenArrayList) {
	if ((i+troublesomeTokenArray.length) <= inTokens.length) {
	  String[] slice =
	    Arrays.copyOfRange(inTokens, i, i+troublesomeTokenArray.length);
	  if (TokenListMatch.tokenMatch(slice, troublesomeTokenArray, false)) {
	    tokenCount = troublesomeTokenArray.length;
	    outString = Tokenize.getTextFromTokenList(slice).replaceAll("\\w","X");
	    break;
	  } // if 
	} // if 
      } // for
      i += tokenCount;
      sb.append(outString);
    }
    return sb.toString();
  }

  /**
   * Test fixture 
   */
  public static void main(String[] args)
  {
    String[] sentences = {
      "The substance vitamin b12 was present, along with ace-inhibitors.", 
      "The substance vitamin-b12 was present." };
    for (String sentence: sentences) {
      System.out.println( "       sentence: " + sentence);
      System.out.println( "masked sentence: " + maskTroublesome(sentence));
    }
  }
}
