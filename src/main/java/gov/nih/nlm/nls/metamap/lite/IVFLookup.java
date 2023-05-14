package gov.nih.nlm.nls.metamap.lite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import java.util.*;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.utils.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookupRegistry;
import gov.nih.nlm.nls.metamap.lite.dictionary.VariantLookup;

/**
 * Describe class IVFLookup here.
 *
 *
 * Created: Wed Apr 18 15:10:13 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class IVFLookup implements MMLDictionaryLookup<TermInfo>
{
  private static final Logger logger = LoggerFactory.getLogger(IVFLookup.class);

  /** string column for cuisourceinfo index*/
  int strColumn = 3;		
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		
  SpecialTerms excludedTerms = new SpecialTerms();
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup4.maxtokensize","15"));

  /** MetaMapLite indexes */
  public MetaMapIvfIndexes mmIndexes;
  /** cui to preferred name index/cache */
  public CuiPreferredNameCache cuiPreferredNameCache;
  /** cui to semantic type set cache */
  public CuiSemanticTypeSetIndex cuiSemanticTypeSetIndex;
  /** cui to sourceset cache */
  public CuiSourceSetIndex cuiSourceSetIndex;



  /*
   Local cache mapping terms to concept info structs.

   Note that we are not using TermConceptInfoCache.java. IVFLookup _used_ to keep an instance
   of this class around, but was never actually using it for anything. Given that class defined
   there seems to also be doing a lot of other work (keeping track of excluded terms, etc.) that
   is being done elsewhere in IVFLookup, my hypothesis is that it is something of an evolutionary
   holdover from an earlier phase of MML's development, during which that functionality _used_ to
   be needed as part of the lookup but has been refactored to happen elsewhere.

   The good news is that because all of that logic was refactored at some point in the past, we can
   now just do throw in a simple LRUCache here and it will work just fine.

  */

  // This default cache size is totally arbitrary - depending on use case, likely would want to change it.
  private final int DEFAULT_TERM_INFO_CACHE_SIZE = 100_000;
  private final boolean shouldCacheTermInfoLookup;

  private final LRUCache<String, TermInfoImpl<Set<ConceptInfo>>> termInfoCache;

  /** word to variant lookup */
  VariantLookupIVF variantLookup;



  /**
   * Creates a new <code>IVFLookup</code> instance.
   * @param properties application properties
   */
  public IVFLookup(Properties properties) {

    // Doing this in the constructor (rather than in init() so that various things can be set as final)
    this.shouldCacheTermInfoLookup = Boolean.parseBoolean(properties.getProperty("metamaplite.ivflookup.termInfoCache.enable", "true"));
    if (this.shouldCacheTermInfoLookup) {
      int cacheSize = Integer.parseInt(properties.getProperty("metamaplite.ivflookup.termInfoCache.cacheSize","-1"));
      if (cacheSize < 0) {
        cacheSize = DEFAULT_TERM_INFO_CACHE_SIZE;
      }
      this.termInfoCache = new LRUCache<>(cacheSize);
    } else {
      this.termInfoCache = null;
    }

    init(properties);
  }

  // Implementation of gov.nih.nlm.nls.metamap.lite.DictionaryLookup

  public static void expandIndexDir(Properties properties, String indexDirName) {
    if (indexDirName != null) {
      properties.setProperty("metamaplite.ivf.cuiconceptindex", indexDirName + "/indices/cuiconcept");
      properties.setProperty("metamaplite.ivf.firstwordsofonewideindex", indexDirName + "/indices/first_words_of_one_WIDE");
      properties.setProperty("metamaplite.ivf.cuisourceinfoindex", indexDirName + "/indices/cuisourceinfo");
      properties.setProperty("metamaplite.ivf.cuisemantictypeindex", indexDirName + "/indices/cuist");
      properties.setProperty("metamaplite.ivf.varsindex", indexDirName + "/indices/vars");
      properties.setProperty("metamaplite.ivf.meshtcrelaxedindex", indexDirName + "/indices/meshtcrelaxed");
    }
  }

  public void init(Properties properties) {
    try {
      if (properties.containsKey("metamaplite.excluded.termsfile")) {
	this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
      } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
	this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
      }
      this.MAX_TOKEN_SIZE =
	Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
						Integer.toString(MAX_TOKEN_SIZE)));

      // fill-in properties for individual indexes
      expandIndexDir(properties, properties.getProperty("metamaplite.index.directory"));
      this.mmIndexes = new MetaMapIvfIndexes(properties);
      this.cuiPreferredNameCache = new CuiPreferredNameCache(this, properties);
      this.cuiSemanticTypeSetIndex = new CuiSemanticTypeSetIndex(mmIndexes);
      this.cuiSourceSetIndex = new CuiSourceSetIndex(mmIndexes, properties);
      this.variantLookup = new VariantLookupIVF(this.mmIndexes, properties);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public String getPreferredName(String cui) {
    try {
      List<String> hitList = 
	this.mmIndexes.cuiConceptIndex.lookup(cui, 0);
      if (hitList.size() > 0) {
	String[] fields = hitList.get(0).split("\\|");
	return fields[1];
      }
      return null;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public Set<String> getSourceSet(String cui) {
    try {
      return this.cuiSourceSetIndex.getSourceSet(cui);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
  public Set<String> getSemanticTypeSet(String cui) {
    try {
      return this.cuiSemanticTypeSetIndex.getSemanticTypeSet(cui);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public VariantLookup getVariantLookup() {
    return this.variantLookup;
  }
  public int lookupVariant(String term, String word) {
    return this.variantLookup.lookupVariant(term, word);
  }
  public int lookupVariant(String term) {
    return this.variantLookup.lookupVariant(term);
  }

  /**
   * Lookup term in IVF index 
   *
   * @param term term
   * @return a <code>String</code> array associated with input term.
   */
  public final TermInfo lookup(final String term) {
    if (this.shouldCacheTermInfoLookup && termInfoCache.containsKey(term)) {
      return termInfoCache.get(term);
    }
    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
    String originalTerm = term;
    String normTerm = term;
    try {
      for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(term, 3)) {
	String[] fields = doc.split("\\|");
	String cui = fields[0];
	String docStr = fields[3];

	// Note: check for (! excludedTerms.isExcluded(cui,normTerm)) &&
	// (isLikelyMatch(originalTerm,normTerm,docStr))
	// should done one level up
	
	// If term is not in excluded term list and term or
	// normalized form of term matches lookup string or
	// normalized form of lookup string then get
	// information about lookup string.
	ConceptInfo conceptInfo = new ConceptInfo(cui,
						  this.cuiPreferredNameCache.findPreferredName(cui),
						  docStr,
						  this.cuiSourceSetIndex.getSourceSet(cui),
						  this.cuiSemanticTypeSetIndex.getSemanticTypeSet(cui));
	conceptInfoSet.add(conceptInfo);
      }
      TermInfoImpl<Set<ConceptInfo>> toReturn = new TermInfoImpl<Set<ConceptInfo>>(originalTerm, normTerm, conceptInfoSet);
      if (this.shouldCacheTermInfoLookup) {
        termInfoCache.put(term, toReturn);
      }
      return toReturn;
//      return new TermInfoImpl<Set<ConceptInfo>>(originalTerm, normTerm, conceptInfoSet);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public boolean verifyImplementation(String directoryPath) {
    if ((new File(directoryPath + "/indices").exists()) &&
	(new File(directoryPath + "/tables").exists())) {
      return true;
    } else {
      return false;
    }
  }
}
