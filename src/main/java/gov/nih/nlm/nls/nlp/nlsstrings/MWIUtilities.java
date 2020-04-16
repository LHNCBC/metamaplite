package gov.nih.nlm.nls.nlp.nlsstrings;

/**
 * MetaWordIndex Utilities.
 * <p>
 * Currently only contains an java implementation of the Prolog
 * predicate normalize_meta_string/2.
 * </p>
 * <p>
 * Translated from Alan Aronson's original prolog version: mwi_utilities.pl.
 * </p>
 * Created: Fri Oct  5 16:11:33 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: MWIUtilities.java,v 1.2 2005/03/04 16:11:12 wrogers Exp $
 */

public final class MWIUtilities {

  /**
   * Normalize metathesaurus string.
   * <p>
   * normalizeMetaString(String) performs "normalization" on String to produce
   * the returned normalized string.  The purpose of normalization is to detect strings
   * which are effectively the same.  The normalization process (also called
   * lexical filtering) consists of the following steps:
   * <ol>
   *    <li> removal of (left []) parentheticals;
   *    <li> removal of multiple meaning designators (<n>);
   *    <li> NOS normalization;
   *    <li> syntactic uninversion;
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
   * </p>
   * <p>
   * Note that the order in which the various normalizations occur is important.
   * The above order is correct.
   * important; e.g., parentheticals must be removed before either lowercasing
   * or normalized syntactic uninversion (which includes NOS normalization)
   * are performed.
   * </p>
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
   *    <li> removal of (left []) parentheticals;
   *    <li>syntactic uninversion;
   *    <li>conversion to lowercase;
   *    <li>stripping of possessives.
   * </ul>
   * </p>
   * @param string meta string to normalize.
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



  /**
   * a test program to test normalizeMetaString method.
   * @param args command line arguments.
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
	// should have a JUnit test suite here. 
	String result = normalizeMetaString(testString);
	System.out.println("\"" + result + "\"");
      }
  }
} // MWIUtilities
