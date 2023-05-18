package gov.nih.nlm.nls.metamap.lite;

import java.util.*;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.utils.LRUCache;

/**
 * Describe class CuiSourceSetIndex here.
 *
 *
 * Created: Fri Mar 24 12:34:38 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CuiSourceSetIndex {
  /** inverted file indexes */
  public MetaMapIvfIndexes mmIndexes;

  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;

  private final int DEFAULT_CACHE_SIZE = 100_000;
  private final boolean shouldCache;
  private final LRUCache<String, Set<String>> sourceSetCache;


  /**
   * Creates a new <code>CuiSourceSetIndex</code> instance.
   * @param mmIndexes container for metamap lite indexes
   */
  public CuiSourceSetIndex(MetaMapIvfIndexes mmIndexes, Properties properties)
  {
    this.mmIndexes = mmIndexes;

    if (properties != null) {
      this.shouldCache = Boolean.parseBoolean(properties.getProperty("metamaplite.cuiSourceSetIndex.cache.enable", "true"));
    } else {
      this.shouldCache = true;
    }

    if (this.shouldCache) {
      int cacheSize = DEFAULT_CACHE_SIZE;
      if (properties != null) { // check to see if we have a different cache size we are supposed to use
        int fromProps = Integer.parseInt(properties.getProperty("metamaplite.cuiSourceSetIndex.cache.size","-1"));
        if (fromProps > 0) {
          cacheSize = fromProps;
        }
      }
      this.sourceSetCache = new LRUCache<>(cacheSize);
    } else {
      this.sourceSetCache = null;
    }

  }

  /**
   * Get source vocabulary abbreviations for cui (concept unique identifier)
   * @param cui target cui
   * @return set of source vocabulary abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException
  {
    if (this.shouldCache && sourceSetCache.containsKey(cui)) {
      return sourceSetCache.get(cui);
    }
    Set<String> sourceSet = new HashSet<String>();
    List<String> hitList =
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, cuiColumn);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      sourceSet.add(fields[4]);
    }
    if (this.shouldCache) {
      sourceSetCache.put(cui, sourceSet);
    }
    return sourceSet;
  }
}
