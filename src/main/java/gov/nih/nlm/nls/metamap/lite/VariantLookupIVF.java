package gov.nih.nlm.nls.metamap.lite;

import java.util.*;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.metamap.lite.dictionary.VariantLookup;

import gov.nih.nlm.nls.utils.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe class VariantLookupIVF here.
 *
 *
 * Created: Fri Mar 24 16:37:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class VariantLookupIVF implements VariantLookup {
  private static final Logger logger = LoggerFactory.getLogger(VariantLookupIVF.class);

  private final Properties properties;
  private final boolean shouldCache;
//  private final HashMap<String, List<String[]>> variantCache;
  private final LRUCache<String, List<String[]>> variantCache;

  private final int DEFAULT_CACHE_SIZE = 100_000;

  public MetaMapIvfIndexes mmIndexes;

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   */
  public VariantLookupIVF(MetaMapIvfIndexes mmIndexes, Properties properties) {
    this.mmIndexes = mmIndexes;
    this.properties = properties;
    this.shouldCache = Boolean.parseBoolean(properties.getProperty("metamaplite.variantLookup.cache.enable", "true"));
    if (this.shouldCache) {
      int cacheSize = Integer.parseInt(properties.getProperty("metamaplite.variantLookup.cache.size","-1"));
      if (cacheSize < 0) {
        cacheSize = DEFAULT_CACHE_SIZE;
      }
      this.variantCache = new LRUCache<>(cacheSize);
    } else {
      this.variantCache = null;
    }
  }


  /**
   * Get variant records for term
   * @param term user supplied term
   * @return list of variant records with matching term
   * @throws IOException i/o exception
   */
  public List<String[]> getVariantsForTerm(String term)
    throws IOException {
      if (this.shouldCache && variantCache.containsKey(term)) {
          return variantCache.get(term);
      }
    List<String[]> variantList = new ArrayList<String[]>();
    if (this.mmIndexes.varsIndex != null) {
      List<String> hitList = this.mmIndexes.varsIndex.lookup(term, 0);
      for (String hit: hitList) {
	String[] fields = hit.split("\\|");
	variantList.add(fields);
      }
    }
    if (this.shouldCache) {
      variantCache.put(term, variantList);
    }
    return variantList;
  }

  /**
   * Get variant records for word
   * @param word user supplied word
   * @return list of variant records with matching word
   * @throws IOException i/o exception
   */
  public List<String[]> getVariantsForWord(String word)
    throws IOException {
    List<String[]> variantList = new ArrayList<String[]>();
    if (this.mmIndexes.varsIndex != null) {
      List<String> hitList = this.mmIndexes.varsIndex.lookup(word, 2);
      for (String hit: hitList) {
	String[] fields = hit.split("\\|");
	variantList.add(fields);
      }
    }
    return variantList;
  }

  public int lookupVariant(String term, String word)
  {
    /* lookup term variants */
    /* if word is in variant list return varlevel (column 4)*/
    int variance = 9;		// maximum variance (should this value be larger?)
    try {
      logger.debug("term: " + term);
      logger.debug("word: " + word);
      for (String[] varFields: this.getVariantsForTerm(term.toLowerCase())) {
	if ((varFields[2].equalsIgnoreCase(word)) ||
	    (varFields[2].equalsIgnoreCase(term))) {
	  variance = Integer.parseInt(varFields[4]); // use varlevel field
      if (logger.isDebugEnabled()) {
        // No need to pay for expensive string building operation here unless we actually want to
        logger.debug("*varFields: " + Arrays.stream(varFields).map(i -> i).collect(Collectors.joining("|")));
      }
	} else {
      if (logger.isDebugEnabled()) {
        // No need to pay for expensive string building operation here unless we actually want to
        logger.debug(" varFields: " + Arrays.stream(varFields).map(i -> i).collect(Collectors.joining("|")));
      }
	}
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return variance;
  }

  public int lookupVariant(String term) {
     int variance = 9;		// maximum variance (should this value be larger?)
    try {
      logger.debug("term: " + term);
      for (String[] varFields: this.getVariantsForTerm(term.toLowerCase())) {
	if ((varFields[2].equalsIgnoreCase(term))) {
	  variance = Integer.parseInt(varFields[4]); // use varlevel field
      if (logger.isDebugEnabled()) {
        // No need to pay for expensive string building operation here unless we actually want to
        logger.debug("*varFields: " + Arrays.stream(varFields).map(i -> i).collect(Collectors.joining("|")));
      }
	} else {
      if (logger.isDebugEnabled()) {
        // No need to pay for expensive string building operation here unless we actually want to
        logger.debug(" varFields: " + Arrays.stream(varFields).map(i -> i).collect(Collectors.joining("|")));
      }
	}
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return variance;
  }
  
}

