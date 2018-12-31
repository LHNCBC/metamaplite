package gov.nih.nlm.nls.metamap.prefix;

/**
 * Utilities for classifying characters.
 *
 *
 * Created: Wed Sep 12 15:28:58 2012
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CharUtils {
  /**
   * Is input character a punctuation symbol?
   * @param ch input character.
   * @return true if character is a punctuation symbol.
   */
  public static boolean isPunct(char ch) {
    switch (ch)
      {
      case '~': case '!': case '@': case '#': case '$': case '%':
      case '^': case '&': case '*': case '(': case ')':
      case '_': case '+': case '-': case '=': case '|':
      case '\\': case '<': case '>': case '?': case '/':
      case ',': case '.': case '`': case '\'': case ';': case ':':
      case '[': case ']': case '{': case '}': case '"':
	return true;
      default:
	return false;
      }
  }

  /**
   * Is input character a digit?
   * @param ch input character.
   * @return true if character is a digit.
   */
  public static boolean isDigit(char ch) {
    switch (ch)
      {
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
	return true;
      default:
	return false;
      }
  }

  /**
   * Is input character a alphabetic or digit?
   * @param ch input character.
   * @return true if character is a alphabetic or digit.
   */
  public static boolean isAlphaNumeric(char ch) {
    switch (ch)
      {
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
      case 'A':      case 'B':      case 'C':      case 'D':
      case 'E':      case 'F':      case 'G':      case 'H':
      case 'I':      case 'J':      case 'K':      case 'L':
      case 'M':      case 'N':      case 'O':      case 'P':
      case 'Q':      case 'R':      case 'S':      case 'T':
      case 'U':      case 'V':      case 'W':      case 'X':
      case 'Y':      case 'Z':
      case 'a':      case 'b':      case 'c':      case 'd':
      case 'e':      case 'f':      case 'g':      case 'h':
      case 'i':      case 'j':      case 'k':      case 'l':
      case 'm':      case 'n':      case 'o':      case 'p':
      case 'q':      case 'r':      case 's':      case 't':
      case 'u':      case 'v':      case 'w':      case 'x':
      case 'y':      case 'z':
	return true;
      default:
	return false;
      }
  }

  /**
   * Is input character a alphabetic or digit?
   * @param ch input character.
   * @return true if character is a alphabetic or digit.
   */
  public static boolean isAlpha(char ch) {
    switch (ch)
      {
      case 'A':      case 'B':      case 'C':      case 'D':
      case 'E':      case 'F':      case 'G':      case 'H':
      case 'I':      case 'J':      case 'K':      case 'L':
      case 'M':      case 'N':      case 'O':      case 'P':
      case 'Q':      case 'R':      case 'S':      case 'T':
      case 'U':      case 'V':      case 'W':      case 'X':
      case 'Y':      case 'Z':
      case 'a':      case 'b':      case 'c':      case 'd':
      case 'e':      case 'f':      case 'g':      case 'h':
      case 'i':      case 'j':      case 'k':      case 'l':
      case 'm':      case 'n':      case 'o':      case 'p':
      case 'q':      case 'r':      case 's':      case 't':
      case 'u':      case 'v':      case 'w':      case 'x':
      case 'y':      case 'z':
	return true;
      default:
	return false;
      }
  }

  public static boolean isWhiteSpace(char ch) {
    switch (ch) {
    case ' ': case '\r': case '\t': case '\n':
      /* utf-8 other spaces */
    case '\u00A0': // U+00A0  NO-BREAK SPACE
    case '\u1680': // U+1680  OGHAM SPACE MARK
    case '\u180E': // U+180E  MONGOLIAN VOWEL SEPARATOR
    case '\u2000': // U+2000  EN QUAD
    case '\u2001': // U+2001  EN QUAD
    case '\u2002': // U+2002  EN SPACE (nut)
    case '\u2003': // U+2003  EM SPACE (mutton)
    case '\u2004': // U+2004  THREE-PER-EM SPACE (thick space)
    case '\u2005': // U+2005  FOUR-PER-EM SPACE (mid space)
    case '\u2006': // U+2006  SIX-PER-EM SPACE
    case '\u2007': // U+2007  FIGURE SPACE
    case '\u2008': // U+2008  PUNCTUATION SPACE
    case '\u2009': // U+2009  THIN SPACE
    case '\u200A': // U+200A  HAIR SPACE
    case '\u200B': // U+200B  ZERO WIDTH SPACE
    case '\u202F': // U+202F  NARROW NO-BREAK SPACE
    case '\u205F': // U+205F  MEDIUM MATHEMATICAL SPACE
    case '\u3000': // U+3000  IDEOGRAPHIC SPACE
    case '\uFEFF': // U+FEFF  ZERO WIDTH NO-BREAK SPACE
      return true;	                                                                                                                                          
    default:
      return false;
    }
  }
}
