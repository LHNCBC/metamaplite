//
package gov.nih.nlm.nls.metamap.lite;

import gov.nih.nlm.nls.metamap.lite.Normalization;
import gov.nih.nlm.nls.utils.LRUCache;

/**
 * This module optionally caches the value of the function
 * Normalization.normalizeLiteString.
 * 
 * You can enable the cache by
 * setting the system property "metamaplite.normalized.string.cache.enable".
 */

public class NormalizedStringCache {
    /** string -&gt; normalize string cache. */
  public static LRUCache<String,String> normalizeStringCache =
    new LRUCache<String,String>
    (Integer.parseInt
     (System.getProperty("metamaplite.normalized.string.cache.size","10000")));
  /** set system property "metamaplite.normalized.string.cache.enable" to true to enable cache */
  public static boolean enableCache =
    Boolean.parseBoolean(System.getProperty("metamaplite.normalized.string.cache.enable", "false"));

  /**
   * Set to true to enable cache, false to disable cache.
   * @param status status to set enable cache.
   */
  public static void setCacheEnable(boolean status) {
    enableCache = status;
  }

  /**
   * A memoization of MWIUtilities.normalizeLiteString 
   * @param originalString input string 
   * @return normalized version of input string.
   */
  static public String normalizeString(String originalString) {
    if (enableCache) {
      /* in the name of premature optimization, I'm memoizing normalizeAstString */
      if (normalizeStringCache.containsKey(originalString)) {
	String result;
	synchronized (normalizeStringCache) {
	  result = normalizeStringCache.get(originalString);
	}
	return result;
      } else {
	// String normalizedString = Normalization.normalizeLiteString(originalString);
	String normalizedString = Normalization.normalizeUtf8AsciiString(originalString);
	synchronized (normalizeStringCache) {
	  normalizeStringCache.put(originalString, normalizedString);
	}
	return normalizedString;
      }
    } else {
      return Normalization.normalizeUtf8AsciiString(originalString);
    }
  }
}
