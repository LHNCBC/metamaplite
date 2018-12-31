package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;

import bioc.BioCSentence;

/**
 * Tokenization support for FindPrefix, emulates original tokenization
 * of regime used by C support code originally used by MetaMap.
 *
 *
 * Created: Tue Sep 11 09:28:57 2012
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Tokenize {

  /** Tokenize style - keep white space tokens. */
  public static final int KEEP_WHITE_SPACE         = 0;
  /** Tokenize style - remove white space tokens. */
  public static final int STRIP_WHITE_SPACE        = 2;

  /**
   * An attempt to replicate tokenization regime in lexicon/function/tokenize.c.
   * @param term  term, possible multi-word to be tokenized.
   * @param tokenizeStyle  style of tokenization currently strip white
   * space (value: 2) or not (value:0).
   * @return array of token strings.
   */
  public static String[] mmTokenize(String term, int tokenizeStyle) {
    List<String> wl = new ArrayList<String>();
    StringBuilder currentWord = new StringBuilder(term.length());
    int x = 0;
    int wordBoundary = 0;
    int punctBoundary = 0;

    for (int aptr = 0; aptr < term.length(); aptr++) 
      {
	char ch = term.charAt(aptr);
	if ( Character.isWhitespace(ch) || Character.isSpaceChar(ch) || CharUtils.isPunct(ch) ) 
	  {
	    if (Character.isWhitespace(ch) || Character.isSpaceChar(ch) || CharUtils.isPunct(ch))
	      {
		if (currentWord.length() > 0)
		  {
		    wl.add(currentWord.toString());
		  }
		currentWord.setLength(0);
		x = 0;
		wordBoundary = 1;
		punctBoundary = 0;
	      }
	    else
	      {
		if (wordBoundary == 0)
		  {
		    if (currentWord.length() > 0) 
		      {
			wl.add(currentWord.toString());
		      }
		    currentWord.setLength(0);
		    x = 0;
		    wordBoundary = 1;
		    punctBoundary = 0;
		  }
	      }

	    if ((tokenizeStyle == STRIP_WHITE_SPACE) &&
		(Character.isWhitespace(ch) || Character.isSpaceChar(ch)))
	      {
	      }
	    else
	      {
		currentWord.append(ch);
		x++;
	      }
	  }
	else
	  {
	    if (( wordBoundary == 1 ) || ( punctBoundary == 1 ))
	      {
		if ( currentWord.length() > 0 )
		  {
		    wl.add(currentWord.toString());
		  }
		currentWord.setLength(0);
		x = 0;
		wordBoundary = 0;
		punctBoundary = 0;
	      }
	    currentWord.append(ch);
	    x++;
	  }
      }
    wl.add(currentWord.toString());
    currentWord.setLength(0);
    return wl.toArray(new String[wl.size()]);
  }

  /**
   * An attempt to replicate tokenization regime in lexicon/function/tokenize.c.
   * @param term  term, possible multi-word to be tokenized.
   * @param tokenizeStyle  style of tokenization currently strip white
   * space (value: 2) or not (value:0).
   * @return List of position token instances.
   */
  public static List<Token> mmPosTokenize(String term, int tokenizeStyle) {
    List<Token> wl = new ArrayList<Token>();
    StringBuilder currentWord = new StringBuilder(term.length());
    int x = 0;
    int wordBoundary = 0;
    int punctBoundary = 0;
    int position = 0;

    for (int aptr = 0; aptr < term.length(); aptr++) 
      {
	char ch = term.charAt(aptr);
	if ( Character.isWhitespace(ch) || Character.isSpaceChar(ch) || CharUtils.isPunct(ch) ) 
	  {
	    if (Character.isWhitespace(ch) || Character.isSpaceChar(ch) || CharUtils.isPunct(ch))
	      {
		if (currentWord.length() > 0)
		  {
		    wl.add(new PosTokenImpl(currentWord.toString(), position));
		    position = aptr;
		  }
		currentWord.setLength(0);
		x = 0;
		wordBoundary = 1;
		punctBoundary = 0;
	      }
	    else
	      {
		if (wordBoundary == 0)
		  {
		    if (currentWord.length() > 0) 
		      {
			wl.add(new PosTokenImpl(currentWord.toString(), position));
			position = aptr;
		      }
		    currentWord.setLength(0);
		    x = 0;
		    wordBoundary = 1;
		    punctBoundary = 0;
		  }
	      }

	    if ((tokenizeStyle == STRIP_WHITE_SPACE) &&
		(Character.isWhitespace(ch) || Character.isSpaceChar(ch)))
	      {
		position = aptr + 1;
	      }
	    else
	      {
		currentWord.append(ch);
		x++;
	      }
	  }
	else
	  {
	    if (( wordBoundary == 1 ) || ( punctBoundary == 1 ))
	      {
		if ( currentWord.length() > 0 )
		  {
		    wl.add(new PosTokenImpl(currentWord.toString(), position));
		    position = aptr;
		  }
		currentWord.setLength(0);
		x = 0;
		wordBoundary = 0;
		punctBoundary = 0;
	      }
	    currentWord.append(ch);
	    x++;
	  }
      }
    wl.add(new PosTokenImpl(currentWord.toString().intern(), position));
    currentWord.setLength(0);
    return wl;
  }

  /**
   * @param tokenList tokenlist representing text with preserved
   * punctuation and whitespace.
   *
   * @return text generated from tokens.
   */
  public static String getTextFromTokenList(List<? extends Token> tokenList)
  {
    StringBuilder sb = new StringBuilder();
    for (Token token: tokenList) {
      sb.append(token.getText());
    }      
    return sb.toString();
  }

  /**
   * @param tokenList tokenlist representing text with preserved
   * punctuation and whitespace.
   *
   * @return text generated from tokens.
   */
  public static String getTextFromTokenList(String[] tokenList)
  {
    StringBuilder sb = new StringBuilder();
    for (String token: tokenList) {
      sb.append(token);
    }      
    return sb.toString();
  }


  /**
   * @param tokenList tokenlist representing text with 
   * punctuation and whitespace removed, inserting whitespace between text.
   *
   * @return text generated from tokens.
   */
  public static String getTextFromNoWsTokenList(List<? extends Token> tokenList)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i<tokenList.size(); i++) {
      Token token = tokenList.get(i);
      if (i > 0) {
	sb.append(" ");
      }
      sb.append(token.getText());
    }      
    return sb.toString();
  }

  public static String[] stringArrayFromTokenList(List<? extends Token> tokenList) {
    String[] stringArray = new String[tokenList.size()];
    int i = 0;
    for (Token token: tokenList) {
      stringArray[i] = token.getText();
      i++;
    }
    return stringArray;
  }

  public static boolean isWhiteSpaceToken(Token token)
  {
    String text = token.getText();
    int i = 0;
    while (i < text.length()) {
      if (! (CharUtils.isWhiteSpace(text.charAt(i)) ||
	     Character.isSpaceChar(text.charAt(i)))) {
	return false;
      }
      i++;
    }
    return true;
  }

  public static boolean isSemicolonToken(Token token)
  {
    String text = token.getText();
    int i = 0;
    while (i < text.length()) {
      if (text.charAt(i) != ';') {
	return false;
      }
      i++;
    }
    return true;
  }

  public static void displayTokenList(List<? extends Token> tokenList) {
    for (Token token: tokenList) {
      System.out.print("\"" + token + "\" ");
    }
    System.out.println();
  }

}
