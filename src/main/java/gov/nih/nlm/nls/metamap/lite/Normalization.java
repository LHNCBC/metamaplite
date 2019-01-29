
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Map;
import java.util.HashMap;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import gov.nih.nlm.nls.nlp.nlsstrings.NLSStrings;
import gov.nih.nlm.nls.metamap.prefix.utf8.GreekCharacters;
import gov.nih.nlm.nls.metamap.prefix.utf8.Ligatures;

/**
 * Morphological string normalization functions.
 */

public class Normalization {

  /**
   * Convert utf-8 greek characters to expanded representation.
   *
   * @param utfString string with utf-8 charcters
   * @return sring with utf-8 characters expanded.
   */
  public static String greekToAscii(String utfString) 
  {
    StringBuilder sb = new StringBuilder();
    
    CharacterIterator iter = new StringCharacterIterator(utfString);
    for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
      Character character = new Character(c);
      if (GreekCharacters.isGreek(character)) {
	sb.append(GreekCharacters.getExpansion(character));
      } else {
	sb.append(c);
      }
    }
    return sb.toString();
  }
  
  /**
   * Similar to normalize_meta_string except hyphens are not removed
   * normalizeAstString(String) performs "normalization" on String to
   * produce the resulting normalized string.  Also, syntactic
   * uninversion is not done.  The purpose of normalization is to
   * detect strings which are effectively the same.  The normalization
   * process (also called lexical filtering) consists of the following
   * steps:
   *
   * <ul>
   *    <li>convert utf8 to ascii
   *    <li> removal of (left []) parentheticals;
   *    <li>conversion to lowercase;
   *    <li>stripping of possessives.
   * </ul>
   * <p>
   * @param astString string meta string to normalize.
   * @return normalized AST string.
   */
  public static String normalizeUtf8AsciiString(String astString)
  {
    String asciiString = greekToAscii(astString);
    String pstring = NLSStrings.removeLeftParentheticals(asciiString);
    String lcUnPstring = pstring.toLowerCase();
    String normString = NLSStrings.stripPossessives(lcUnPstring);
    // System.out.println("normalizeUtf8AsciiString(\"" + astString + "\") -> \"" + normString +"\"");
    return normString;
  } 

  /**
   * Similar to normalize_meta_string except hyphens are not removed
   * normalizeAstString(String) performs "normalization" on String to
   * produce the resulting normalized string.  Also, syntactic
   * uninversion is not done.  The purpose of normalization is to
   * detect strings which are effectively the same.  The normalization
   * process (also called lexical filtering) consists of the following
   * steps:
   *
   * <ul>
   *    <li> removal of (left []) parentheticals;
   *    <li>conversion to lowercase;
   *    <li>stripping of possessives.
   * </ul>
   * <p>
   * @param astString string meta string to normalize.
   * @return normalized AST string.
   */
  public static String normalizeLiteString(String astString)
  {
    String pstring = NLSStrings.removeLeftParentheticals(astString);
    String lcUnPstring = pstring.toLowerCase();
    String normString = NLSStrings.stripPossessives(lcUnPstring);
    return normString;
  } 

  /**
   * Normalize metathesaurus string.
   * <p>
   * Similar to MWIUtilties.normalizeMetaString,
   * normalizeLiteMetaString(String) performs "normalization" on
   * String to produce the returned normalized string.  The purpose of
   * normalization is to detect strings which are effectively the
   * same.  The normalization process (also called lexical filtering)
   * consists of the following steps:
   * <ol>
   *    <li> removal of (left []) parentheticals;
   *    <li> conversion to lowercase;
   *    <li> replacement of hyphens with spaces; and
   *    <li> stripping of possessives.
   * </ol>
   * Some right parentheticals used to be stripped, but no longer are.
   * Lexical Filtering Examples:
   * The concept "Abdomen" has strings "ABDOMEN" and "Abdomen, NOS".
   * Similarly, the concept "Lung Cancer" has string "Cancer, Lung".
   * And the concept "1,4-alpha-Glucan Branching Enzyme" has a string
   * "1,4 alpha Glucan Branching Enzyme".
   * <p>
   * Note that the order in which the various normalizations occur is important.
   * The above order is correct.
   * important; e.g., parentheticals must be removed before either lowercasing
   * or normalized syntactic uninversion (which includes NOS normalization)
   * are performed.
   * <p>
   * @param string meta string to normalize.
   * @return normalized meta string.
   */
  public static String normalizeLiteMetaString(String string)
  {
    String pstring = NLSStrings.removeLeftParentheticals(string);
    String lcUnPstring = pstring.toLowerCase();
    String hLcUnPstring = NLSStrings.removeHyphens(lcUnPstring);
    String normString = NLSStrings.stripPossessives(hLcUnPstring);
    return normString;
  }

  /**
   * Normalize metathesaurus string.
   * <p>
   * normalizeMetaString(String) performs "normalization" on String to produce
   * the returned normalized string.  The purpose of normalization is to detect strings
   * which are effectively the same.  The normalization process (also called
   * lexical filtering) consists of the following steps:
   * <ol>
   *    <li> removal of (left []) parentheticals;</li>
   *    <li> removal of multiple meaning designators (&lt;n&gt;);</li>
   *    <li> NOS normalization;</li>
   *    <li> syntactic uninversion;</li>
   *    <li> conversion to lowercase;</li>
   *    <li> replacement of hyphens with spaces; and</li>
   *    <li> stripping of possessives.</li>
   * </ol>
   * Some right parentheticals used to be stripped, but no longer are.
   * Lexical Filtering Examples:
   * The concept "Abdomen" has strings "ABDOMEN" and "Abdomen, NOS".
   * Similarly, the concept "Lung Cancer" has string "Cancer, Lung".
   * And the concept "1,4-alpha-Glucan Branching Enzyme" has a string
   * "1,4 alpha Glucan Branching Enzyme".
   *
   * <p>
   * Note that the order in which the various normalizations occur is important.
   * The above order is correct.
   * important; e.g., parentheticals must be removed before either lowercasing
   * or normalized syntactic uninversion (which includes NOS normalization)
   * are performed.
   *
   * @param string meta string to normalize.
   * @return normalized meta string.
   */
  public static String normalizeMetaString(String string)
  {
    String pstring = NLSStrings.removeLeftParentheticals(string);
    String unPstring = NLSStrings.normalizedSyntacticUninvertString(pstring);
    String lcUnPstring = unPstring.toLowerCase();
    String hLcUnPstring = NLSStrings.removeHyphens(lcUnPstring);
    String normString = NLSStrings.stripPossessives(hLcUnPstring);
    return normString;
  }

  /**
   * Similar to normalize_meta_string except hyphens are not removed
   * normalizeAstString(String) performs "normalization" on String to
   * produce the resulting normalized string.  The purpose of
   * normalization is to detect strings which are effectively the
   * same.  The normalization process (also called lexical filtering)
   * consists of the following steps:
   *
   * <ul>
   *    <li>removal of (left []) parentheticals;</li>
   *    <li>syntactic uninversion;</li>
   *    <li>conversion to lowercase;</li>
   *    <li>stripping of possessives.</li>
   * </ul>
   * 
   * @param astString meta string to normalize.
   * @return normalized AST string.
   */
  public static String normalizeAstString(String astString)
  {
    String pstring = NLSStrings.removeLeftParentheticals(astString);
    String unPstring = NLSStrings.syntacticUninvertString(pstring);
    String lcUnPstring = unPstring.toLowerCase();
    String normString = NLSStrings.stripPossessives(lcUnPstring);
    return normString;
  } 
}
