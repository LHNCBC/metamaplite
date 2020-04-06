package gov.nih.nlm.nls.nlp.nlsstrings;
/**
 * Lexical library.
 *
 * <p>
 *  Implementation of lexical library. (translated from lex.c.)
 * </p>
 * Created: Fri Oct 12 10:25:50 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: Lex.java,v 1.3 2005/03/04 16:08:11 wrogers Exp $
 */

public class Lex
{
  static final char COMMA = ',';
  static final char SPACE = ' ';

  /**
   * <p>
   * Recursively uninverts a string. I.e., injury, abdominal ==> abdominal injury
   * </p>
   * <p>
   * Translated directly from uninvert(s,t) in lex.c.
   * </p>
   * @param s INPUT: string "s" containing the term to be uninverted.
   * @return OUTPUT: string containing the uninverted string.
   */
  public static String uninvert(String s)
  {
    int sp;
    int cp;
    
    if (s.length() == 0)
      {
	return s;
      }
    sp = 0;
    while ((sp=s.indexOf((int)COMMA, sp)) != -1)
      {
	cp = sp;
	cp++;
	if (cp < s.length() && s.charAt(cp) == SPACE)
	  {
	    while (cp < s.length() && 
		   s.charAt(cp) == SPACE) {
	      cp++;
	    }
	    return uninvert(s.substring(cp, s.length())) +
	      " " + s.substring(0, sp);
	  }
	else
	  {
	    sp++;
	  }
      }
    return s;
  }

  /**
   * a test program to test uninvert method.
   * @param args command line arguments.
   */
  public static void main(String[] args)
  {
    if (args.length > 0)
      {
	String result = uninvert(args[0]);
	System.out.println(args[0] + " -> " + result);
      }
  }
  
} // Lex
