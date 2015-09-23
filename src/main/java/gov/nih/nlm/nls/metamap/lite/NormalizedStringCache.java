
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Map;
import java.util.HashMap;
import gov.nih.nlm.nls.metamap.lite.Normalization;

/**
 *
 */

public class NormalizedStringCache {
    /** string -> normalize string cache. */
  public static Map<String,String> normalizeStringCache = new HashMap<String,String>();

  /**
   * A memoization of MWIUtilities.normalizeLiteString 
   * @param input string 
   * @return normalized version of input string.
   */
  static String normalizeString(String originalString) {
    /* in the name of premature optimization, I'm memoizing normalizeAstString */
    if (normalizeStringCache.containsKey(originalString)) {
      return normalizeStringCache.get(originalString);
    } else {
	String normalizedString = Normalization.normalizeLiteString(originalString);
	synchronized (normalizeStringCache) {
	  normalizeStringCache.put(originalString, normalizedString);
	}
	return normalizedString;
    }
  }
}
