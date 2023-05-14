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
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.utils.LRUCache;

/**
 * Describe class TermConceptInfoCache here.
 *
 *
 * Created: Fri Mar 24 09:23:15 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TermConceptInfoCache {
  /** inverted file indexes */
  public MetaMapIvfIndexes mmIndexes;
  /** cui to preferredName cache */
  CuiPreferredNameCache cuiPreferredNameCache;
  /** cui to semantic type set cache */
  CuiSemanticTypeSetIndex cuiSemanticTypeSetIndex;
  /** cui to sourceset cache */
  CuiSourceSetIndex cuiSourceSetIndex;

  /** string column for cuisourceinfo index*/
  int strColumn = 3;		

  /** Set property
   * "metamaplite.entitylookup4.term.concept.cache.enable" to true to
   * enable term to concept cache. */
  boolean enableTermConceptInfoCache = 
    Boolean.getBoolean("metamaplite.entitylookup4.term.concept.cache.enable");

  /** cache of string -&gt; concept and attributes */
  public static LRUCache<String,Set<ConceptInfo>> termConceptCache = 
    new LRUCache<String,Set<ConceptInfo>>
    (Integer.parseInt
     (System.getProperty
      ("metamaplite.entity.lookup4.term.concept.cache.size","10000")));

  SpecialTerms excludedTerms;

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   * @param cuiPreferredNameCache cui to preferred name cache
   */
  public TermConceptInfoCache(MetaMapIvfIndexes mmIndexes, CuiPreferredNameCache cuiPreferredNameCache) {
    this.mmIndexes = mmIndexes;
    this.cuiPreferredNameCache = cuiPreferredNameCache;
    this.excludedTerms = new SpecialTerms();
    this.cuiSemanticTypeSetIndex = new CuiSemanticTypeSetIndex(mmIndexes);
    this.cuiSourceSetIndex = new CuiSourceSetIndex(mmIndexes, null);
  }

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   * @param properties application properties
   * @param cuiPreferredNameCache cui to preferred name cache
   */
  public TermConceptInfoCache(Properties properties,
			      MetaMapIvfIndexes mmIndexes,
			      CuiPreferredNameCache cuiPreferredNameCache) {
    this.mmIndexes = mmIndexes;
    this.cuiPreferredNameCache = cuiPreferredNameCache;
    this.excludedTerms = new SpecialTerms();
    if (properties.containsKey("metamaplite.entity.lookup4.term.concept.cache.size")) {
      termConceptCache = 
	new LRUCache<String,Set<ConceptInfo>>
	(Integer.parseInt
	 (properties.getProperty
	  ("metamaplite.entity.lookup4.term.concept.cache.size","10000")));
    }
    this.cuiSemanticTypeSetIndex = new CuiSemanticTypeSetIndex(mmIndexes);
    this.cuiSourceSetIndex = new CuiSourceSetIndex(mmIndexes, properties);
  }

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param properties application properties
   * @param mmIndexes set of inverted file indexes
   * @param cuiPreferredNameCache cui to preferred name cache
   * @param cuiSemanticTypeSetIndex cui to semantic type set index
   * @param cuiSourceSetIndex cui to source set index
   * @param excludedTerms term to be excluded from result
   */
  public TermConceptInfoCache(Properties properties,
			      MetaMapIvfIndexes mmIndexes,
			      CuiPreferredNameCache cuiPreferredNameCache,
			      CuiSemanticTypeSetIndex cuiSemanticTypeSetIndex,
			      CuiSourceSetIndex cuiSourceSetIndex,
			      SpecialTerms excludedTerms) {
    this.mmIndexes = mmIndexes;
    this.cuiPreferredNameCache = cuiPreferredNameCache;
    this.excludedTerms = excludedTerms;
    if (properties.containsKey("metamaplite.entity.lookup4.term.concept.cache.size")) {
      termConceptCache = 
	new LRUCache<String,Set<ConceptInfo>>
	(Integer.parseInt
	 (properties.getProperty
	  ("metamaplite.entity.lookup4.term.concept.cache.size","10000")));
    }
    this.cuiSemanticTypeSetIndex = cuiSemanticTypeSetIndex;
    this.cuiSourceSetIndex = cuiSourceSetIndex;
  }

  // why do test for this?
  public static boolean isLikelyMatch(String term, String normTerm, String docStr) {
    if (term.length() < 5) {
      return term.toLowerCase().equals(docStr.toLowerCase());
    } else {
      boolean result = normTerm.equals(NormalizedStringCache.normalizeString(docStr));
      return result;
    }
  }

  public void cacheConcept(String term, ConceptInfo concept) {
    synchronized (this.termConceptCache) {
      if (this.termConceptCache.containsKey(term)) {
	synchronized (this.termConceptCache.get(term)) {
	  this.termConceptCache.get(term).add(concept);
	}
      } else {
	Set<ConceptInfo> newConceptSet = new HashSet<ConceptInfo>();
	newConceptSet.add(concept);
	synchronized (this.termConceptCache) {
	  this.termConceptCache.put(term, newConceptSet);
	}
      }
    }
  }
  
  public void cacheConceptInfoSet(String term, Set<ConceptInfo> conceptInfoSet) {
    if (this.termConceptCache.containsKey(term)) {
      synchronized (this.termConceptCache.get(term)) {
	this.termConceptCache.get(term).addAll(conceptInfoSet);
      }
    } else {
      synchronized (this.termConceptCache) {
	this.termConceptCache.put(term, conceptInfoSet);
      }
    }
  }

  public Set<ConceptInfo> lookupTermConceptInfoIVF(String originalTerm,
						   String normTerm) 
    throws FileNotFoundException, IOException
  {
    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
    // if not in cache then lookup term 
    for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(normTerm, 3)) {
      String[] fields = doc.split("\\|");
      String cui = fields[0];
      String docStr = fields[3];
      
      // If term is not in excluded term list and term or
      // normalized form of term matches lookup string or
      // normalized form of lookup string then get
      // information about lookup string.
      if ((! excludedTerms.isExcluded(cui,normTerm)) && isLikelyMatch(originalTerm,normTerm,docStr)) {
	ConceptInfo conceptInfo = new ConceptInfo(cui,
						  this.cuiPreferredNameCache.findPreferredName(cui),
						  docStr,
						  this.cuiSourceSetIndex.getSourceSet(cui),
						  this.cuiSemanticTypeSetIndex.getSemanticTypeSet(cui));
	conceptInfoSet.add(conceptInfo);
      }
    }
    return conceptInfoSet;
  }

  /**
   * Lookup Term - if term info is already in cache then use cached
   * term info, otherwise, lookup term info in index.
   * @param originalTerm Term to lookup
   * @param normTerm normalized version of originalTerm 
   * @return map of entities keyed by span
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public Set<ConceptInfo> lookupTermConceptInfo(String originalTerm,
						String normTerm)
    throws FileNotFoundException, IOException
  {
    // System.out.println("originalTerm: " + originalTerm);
    // System.out.println("normTerm: " + normTerm);
    
    if (this.enableTermConceptInfoCache) {
      if (this.termConceptCache.containsKey(normTerm) ) {
	Set<ConceptInfo> result;
	synchronized(this.termConceptCache) {
	  result = this.termConceptCache.get(normTerm);
	}
	return result;
      } else {
	Set<ConceptInfo> conceptInfoSet = this.lookupTermConceptInfoIVF(originalTerm, normTerm);
	this.cacheConceptInfoSet(normTerm, conceptInfoSet);
	return conceptInfoSet;
      }
    } else {
      return this.lookupTermConceptInfoIVF(originalTerm, normTerm);
    }
  }

  public Set<ConceptInfo> lookupTermConceptInfoIVF(String originalTerm,
						   String normTerm,
						   List<? extends Token> tokenlist) 
    throws FileNotFoundException, IOException
  {
    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
    // if not in cache then lookup term 
    for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(normTerm, 3)) {
      String[] fields = doc.split("\\|");
      String cui = fields[0];
      String docStr = fields[3];
      
      // If term is not in excluded term list and term or
      // normalized form of term matches lookup string or
      // normalized form of lookup string then get
      // information about lookup string.
      if ((! excludedTerms.isExcluded(cui,normTerm)) && isLikelyMatch(originalTerm,normTerm,docStr)) {
	if (tokenlist.get(0) instanceof PosToken) {
	  ConceptInfo conceptInfo = new ConceptInfo(cui,
                                                    this.cuiPreferredNameCache.findPreferredName(cui),
						    docStr,
						    this.cuiSourceSetIndex.getSourceSet(cui),
						    this.cuiSemanticTypeSetIndex.getSemanticTypeSet(cui));
	  conceptInfoSet.add(conceptInfo);
	}
      }
    }
    return conceptInfoSet;
  }

  /**
   * Lookup Term - if term info is already in cache then use cached
   * term info, otherwise, lookup term info in index.
   * @param originalTerm Term to lookup
   * @param normTerm normalized version of originalTerm 
   * @param tokenlist tokenized version of normTerm
   * @return map of entities keyed by span
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public Set<ConceptInfo> lookupTermConceptInfo(String originalTerm,
						String normTerm,
						List<? extends Token> tokenlist)
    throws FileNotFoundException, IOException
  {
    // System.out.println("originalTerm: " + originalTerm);
    // System.out.println("normTerm: " + normTerm);
    // System.out.println("tokenlist: " + tokenlist);
    
    if (this.enableTermConceptInfoCache) {
      if (this.termConceptCache.containsKey(normTerm) ) {
	Set<ConceptInfo> result;
	synchronized(this.termConceptCache) {
	  result = this.termConceptCache.get(normTerm);
	}
	return result;
      } else {
	Set<ConceptInfo> conceptInfoSet = this.lookupTermConceptInfoIVF(originalTerm, normTerm, tokenlist);
	this.cacheConceptInfoSet(normTerm, conceptInfoSet);
	return conceptInfoSet;
      }
    } else {
      return this.lookupTermConceptInfoIVF(originalTerm, normTerm, tokenlist);
    }
  }
}
