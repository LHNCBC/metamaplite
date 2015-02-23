
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Map;
import java.util.HashMap;
import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;

/**
 *
 */

public class NormalizedStringCache {
    /** string -> normalize string cache. */
  public static Map<String,String> normalizeAstStringCache = new HashMap<String,String>();

  /**
   * A memoization of MWIUtilities.normalizeAstString 
   * @param input string 
   * @return normalized version of input string.
   */
  static String normalizeAstString(String astString) {
    /* in the name of premature optimization, I'm memoizing normalizeAstString */
    if (normalizeAstStringCache.containsKey(astString)) {
      return normalizeAstStringCache.get(astString);
    } else {
	String normalizedAstString = MWIUtilities.normalizeAstString(astString);
	synchronized (normalizeAstStringCache) {
	  normalizeAstStringCache.put(astString, normalizedAstString);
	}
	return normalizedAstString;
    }
  }
}
