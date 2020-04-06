package gov.nih.nlm.nls.nlp.nlsstrings;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import gov.nih.nlm.nls.utils.StringUtils;

/**
 * MetaMap tokenization routines.
 *<p>
 * A re-implementation of Alan Aronson's Metamap Tokenization in Java. 
 * (orignal prolog source: metamap_tokenization.pl)
 *</p>
 *
 * Created: Mon Oct 22 16:50:58 2001
 *
 * @author <a href="mailto: "Willie Rogers</a>
 * @version $Id: MetamapTokenization.java,v 1.3 2005/03/04 16:16:34 wrogers Exp $
 */

public final class MetamapTokenization {
  public static final String UTTER_TOKEN_DELIMITERS = 
    " \t\n\r\f$@\\{|}:;~(),.-&/'+<>*^[]=#?%\"!_";
  /** delimiters for normalizeMetaString */
  public static final String TOKEN_DELIMITERS = 
    " \t\n\r\f$|~";
  /** delimiters for normalizeText */
  public static final String WORD_DELIMITERS = 
    " \t\n\r\f$%\\{|~(),-&/";

  /**
   * Tokenize the supplied text utterly!
   * @param string string to be tokenized 
   * @return utterly tokenized text
   */
  public static List<String> tokenizeTextUtterly(String string)
  {
    List<String> list = new ArrayList<String>();
    StringTokenizer st = 
      new StringTokenizer(string, UTTER_TOKEN_DELIMITERS);
    while (st.hasMoreTokens())
      {
	  list.add((String)st.nextToken());
      }
    return list;
  }

  /**
   * Tokenize the supplied text using MetaMap style tokenization:
   * tokenize text utterly and then remove possessives and non-words.
   *
   * @param text string to be tokenized 
   * @return tokenized text
   */
  public static List<String> tokenizeTextMM(String text) { 
    List<String> stringTokens0 = tokenizeTextUtterly(text);
    return removePossessivesAndNonwords(stringTokens0);
  }

  /**
   * Normalize text 
   *
   * @param string string to be normalized 
   * @return normalized text string
   */
  public static String normalizeText(String string)
  {
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = 
      new StringTokenizer(string, WORD_DELIMITERS, true);
    while (st.hasMoreTokens())
      {
	String token = (String)st.nextToken();
	if ( ! (token.length() == 1 && WORD_DELIMITERS.indexOf(token) >= 0))
	  {	  
	    sb.append(token).append(" ");
	  }
      }
    return sb.toString();
  }

  /**
   * Remove possessive from string.
   * <pre>
   * e.g.
   *  addison's -> addison
   *  chris' -> chris
   * </pre>
   * @param token token to be transformed.
   * @return transformed token
   */
  public static String removePossessives(String token)
  {
    int pos = 0;
    if ((pos = token.lastIndexOf("'s")) >= 0 && pos == (token.length() - 2))
      {
	if ((pos - 1 >= 0) && Character.isLetterOrDigit(token.charAt(pos - 1))) {
	  return token.substring(0, pos) + token.substring(pos + 2, token.length());
	}
      }
    else if ((pos = token.lastIndexOf("'")) >= 0 && 
	     (pos == (token.length() - 1)) &&
	     pos != 0 &&
	     (token.charAt(pos - 1) == 's'))
      {
	return token.substring(0, pos) + token.substring(pos + 1, token.length());
      }
    return token;
  }

  /**
   * Determine if token is not a non-word.
   * @param token token to be evaluated.
   * @return true if token is not a non-word
   */
  public static boolean isWsWord(String token)
  {
    for (int i = 0; i < token.length(); i++)
      {
	if (Character.isDigit(token.charAt(i)) || 
	    ((! Character.isLetter(token.charAt(i))) 
	     && token.charAt(i) != '\'') )
	  {
	    return false;
	  }
      }
    return true;
  }

  /**
   * Remove possessive words and non-words.
   * @return string with possessive words and non-words removed.
   */
  public static String removePossessivesAndNonwords(String string)
  {
    // is_ws_word(Word),
    // ends_with_s(Word),
    // is_ws(WhiteSpace),
    if ( isWsWord(string) ) {
      return removePossessives(string);
    }
    return string;
  }

  /**
   * Remove possessive words and non-words from list of tokens.
   * @return token list with possessive words and non-words removed.
   */
  public static List<String> removePossessivesAndNonwords(List<String> tokens)
  {
    ListIterator iter = tokens.listIterator();
    while (iter.hasNext())
      {
	String token = (String)iter.next();
	iter.remove();
	iter.add(removePossessivesAndNonwords(token));
      }
    return tokens;
  }

  /**
   * main program -- a test program
   * @param args command line arguments
   */
  public static void main(String[] args)
  {
    if (args.length > 0)
      {
	StringBuffer testSB = new StringBuffer(args[0]);
	for (int i=1; i < args.length; i++) {
	  testSB.append(" ").append(args[i]);
	}
	String testString = testSB.toString();
	List result = tokenizeTextMM(testString);
	System.out.println(testString + " -> tokenize_text_mm -> " + 	StringUtils.list(result));
	result = tokenizeTextUtterly(testString);
	System.out.println(testString + " -> tokenize_text_utterly -> " + 	StringUtils.list(result));
      }
  }
}// MetamapTokenization
