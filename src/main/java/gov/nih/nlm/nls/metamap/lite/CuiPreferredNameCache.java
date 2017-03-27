package gov.nih.nlm.nls.metamap.lite;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.utils.LRUCache;

/**
 * Describe class CuiPreferredNameCache here.
 *
 *
 * Created: Fri Mar 24 09:24:02 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CuiPreferredNameCache {
  /** inverted file indexes */
  public MetaMapIvfIndexes mmIndexes;

  /** Set property
   * "metamaplite.entitylookup4.cui.preferredname.cache.enable" to
   * true to enable cui to preferred name cache. */
  boolean enableCuiPreferredNameCache =
    Boolean.getBoolean("metamaplite.entitylookup4.cui.preferredname.cache.enable");
  
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		

  public static LRUCache<String,String> cuiPreferredNameCache =
    new LRUCache<String,String>
    (Integer.parseInt
     (System.getProperty
      ("metamaplite.entity.lookup4.cui.preferred.name.cache.size","10000")));
  
  /**
   * Creates a new <code>CuiPreferredNameCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   */
  public CuiPreferredNameCache(MetaMapIvfIndexes mmIndexes) {
    this.mmIndexes = mmIndexes;
  }

  /**
   * Creates a new <code>CuiPreferredNameCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   * @param properties application properties
   */
  public CuiPreferredNameCache(Properties properties, MetaMapIvfIndexes mmIndexes) {
    this.mmIndexes = mmIndexes;
  }

  /**
   * 
   *
   * @param cui target cui
   * @param preferredTerm preferred term
   */
  public void cachePreferredTerm(String cui, String preferredTerm) {
    synchronized (cuiPreferredNameCache) {
      cuiPreferredNameCache.put(cui, preferredTerm);
    }
  }

  /**
   * Lookup preferred name for cui (concept unique identifier) in inverted file.
   * @param cui target cui
   * @return preferredname for cui or null if not found
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public String lookupPreferredNameIVF(String cui)
    throws FileNotFoundException, IOException
  {
    List<String> hitList = 
      this.mmIndexes.cuiConceptIndex.lookup(cui, 0);
    if (hitList.size() > 0) {
      String[] fields = hitList.get(0).split("\\|");
      return fields[1];
    }
    return null;
  }
    
  /**
   * Find preferred name for cui (concept unique identifier)
   * @param cui target cui
   * @return preferredname for cui or null if not found
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public String findPreferredName(String cui)
    throws FileNotFoundException, IOException
  {
    if (enableCuiPreferredNameCache) {
      if (this.cuiPreferredNameCache.containsKey(cui)) {
	return this.cuiPreferredNameCache.get(cui);
      } else {
	String preferredName = lookupPreferredNameIVF(cui);
	this.cachePreferredTerm(cui, preferredName);
	return preferredName;
      }
    } else {
      String preferredName = lookupPreferredNameIVF(cui);
      if (preferredName == null) {
	return "";
      } else {
	return preferredName;
      }
    }
  }

}
