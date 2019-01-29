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
	// greek characters, not exhaustive? (see gov.nih.nlm.nls.metamap.prefix.utf8.GreekCharacters)
      case '\u0370': case '\u0371': case '\u0372': case '\u0373':
      case '\u0374': case '\u0375': case '\u0376': case '\u0377':
      case '\u037A': case '\u037B': case '\u037C': case '\u037D':
      case '\u037E': case '\u0384': case '\u0385': case '\u0386':
      case '\u0387': case '\u0388': case '\u0389': case '\u038A':
      case '\u038C': case '\u038E': case '\u038F': case '\u0390':
      case '\u0391': case '\u0392': case '\u0393': case '\u0394':
      case '\u0395': case '\u0396': case '\u0397': case '\u0398':
      case '\u0399': case '\u039A': case '\u039B': case '\u039C':
      case '\u039D': case '\u039E': case '\u039F': case '\u03A0':
      case '\u03A1': case '\u03A3': case '\u03A4': case '\u03A5':
      case '\u03A6': case '\u03A7': case '\u03A8': case '\u03A9':
      case '\u03AA': case '\u03AB': case '\u03AC': case '\u03AD':
      case '\u03AE': case '\u03AF': case '\u03B0': case '\u03B1':
      case '\u03B2': case '\u03B3': case '\u03B4': case '\u03B5':
      case '\u03B6': case '\u03B7': case '\u03B8': case '\u03B9':
      case '\u03BA': case '\u03BB': case '\u03BC': case '\u03BD':
      case '\u03BE': case '\u03BF': case '\u03C0': case '\u03C1':
      case '\u03C2': case '\u03C3': case '\u03C4': case '\u03C5':
      case '\u03C6': case '\u03C7': case '\u03C8': case '\u03C9':
      case '\u03CA': case '\u03CB': case '\u03CC': case '\u03CD':
      case '\u03CE': case '\u03CF': case '\u03D0': case '\u03D1':
      case '\u03D2': case '\u03D3': case '\u03D4': case '\u03D5':
      case '\u03D6': case '\u03D7': case '\u03D8': case '\u03D9':
      case '\u03DA': case '\u03DB': case '\u03DC': case '\u03DD':
      case '\u03DE': case '\u03DF': case '\u03E0': case '\u03E1':
      case '\u03E2': case '\u03E3': case '\u03E4': case '\u03E5':
      case '\u03E6': case '\u03E7': case '\u03E8': case '\u03E9':
      case '\u03EA': case '\u03EB': case '\u03EC': case '\u03ED':
      case '\u03EE': case '\u03EF': case '\u03F0': case '\u03F1':
      case '\u03F2': case '\u03F3': case '\u03F4': case '\u03F5':
      case '\u03F6': case '\u03F7': case '\u03F8': case '\u03F9':
      case '\u03FA': case '\u03FB': case '\u03FC': case '\u03FD':
      case '\u03FE': case '\u03FF':
	return true;
      default:
	return false;
      }
  }

  /**
   * Is input character a alphabetic?
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
	// greek characters, not exhaustive? (see gov.nih.nlm.nls.metamap.prefix.utf8.GreekCharacters)
      case '\u0370': case '\u0371': case '\u0372': case '\u0373':
      case '\u0374': case '\u0375': case '\u0376': case '\u0377':
      case '\u037A': case '\u037B': case '\u037C': case '\u037D':
      case '\u037E': case '\u0384': case '\u0385': case '\u0386':
      case '\u0387': case '\u0388': case '\u0389': case '\u038A':
      case '\u038C': case '\u038E': case '\u038F': case '\u0390':
      case '\u0391': case '\u0392': case '\u0393': case '\u0394':
      case '\u0395': case '\u0396': case '\u0397': case '\u0398':
      case '\u0399': case '\u039A': case '\u039B': case '\u039C':
      case '\u039D': case '\u039E': case '\u039F': case '\u03A0':
      case '\u03A1': case '\u03A3': case '\u03A4': case '\u03A5':
      case '\u03A6': case '\u03A7': case '\u03A8': case '\u03A9':
      case '\u03AA': case '\u03AB': case '\u03AC': case '\u03AD':
      case '\u03AE': case '\u03AF': case '\u03B0': case '\u03B1':
      case '\u03B2': case '\u03B3': case '\u03B4': case '\u03B5':
      case '\u03B6': case '\u03B7': case '\u03B8': case '\u03B9':
      case '\u03BA': case '\u03BB': case '\u03BC': case '\u03BD':
      case '\u03BE': case '\u03BF': case '\u03C0': case '\u03C1':
      case '\u03C2': case '\u03C3': case '\u03C4': case '\u03C5':
      case '\u03C6': case '\u03C7': case '\u03C8': case '\u03C9':
      case '\u03CA': case '\u03CB': case '\u03CC': case '\u03CD':
      case '\u03CE': case '\u03CF': case '\u03D0': case '\u03D1':
      case '\u03D2': case '\u03D3': case '\u03D4': case '\u03D5':
      case '\u03D6': case '\u03D7': case '\u03D8': case '\u03D9':
      case '\u03DA': case '\u03DB': case '\u03DC': case '\u03DD':
      case '\u03DE': case '\u03DF': case '\u03E0': case '\u03E1':
      case '\u03E2': case '\u03E3': case '\u03E4': case '\u03E5':
      case '\u03E6': case '\u03E7': case '\u03E8': case '\u03E9':
      case '\u03EA': case '\u03EB': case '\u03EC': case '\u03ED':
      case '\u03EE': case '\u03EF': case '\u03F0': case '\u03F1':
      case '\u03F2': case '\u03F3': case '\u03F4': case '\u03F5':
      case '\u03F6': case '\u03F7': case '\u03F8': case '\u03F9':
      case '\u03FA': case '\u03FB': case '\u03FC': case '\u03FD':
      case '\u03FE': case '\u03FF':
	return true;
      default:
	return false;
      }
  }

  /**
   * Is input character a digit (not including hexdecimal)?
   * @param ch input character.
   * @return true if character is a alphabetic or digit.
   */
  public static boolean isNumeric(char ch) {
    switch (ch)
      {
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
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
