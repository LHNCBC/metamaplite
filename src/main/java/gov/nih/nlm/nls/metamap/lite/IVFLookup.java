package gov.nih.nlm.nls.metamap.lite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
  private static final Logger logger = LogManager.getLogger(IVFLookup.class);

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
  /** term to concept info index/cache */
  public TermConceptInfoCache termConceptInfoCache;
  /** word to variant lookup */
  VariantLookupIVF variantLookup;


  /**
   * Creates a new <code>IVFLookup</code> instance.
   *
   */
  public IVFLookup() {
  }

  /**
   * Creates a new <code>IVFLookup</code> instance.
   * @param properties application properties
   */
  public IVFLookup(Properties properties) {
    init(properties);
  }

  // Implementation of gov.nih.nlm.nls.metamap.lite.DictionaryLookup


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

      this.mmIndexes = new MetaMapIvfIndexes(properties);
      this.cuiPreferredNameCache = new CuiPreferredNameCache(this, properties);
      this.cuiSemanticTypeSetIndex = new CuiSemanticTypeSetIndex(mmIndexes);
      this.cuiSourceSetIndex = new CuiSourceSetIndex(mmIndexes);
      this.termConceptInfoCache = new TermConceptInfoCache(properties,
							   this.mmIndexes,
							   this.cuiPreferredNameCache,
							   this.cuiSemanticTypeSetIndex,
							   this.cuiSourceSetIndex,
							   this.excludedTerms);
      this.variantLookup = new VariantLookupIVF(this.mmIndexes);
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
      return new TermInfoImpl<Set<ConceptInfo>>(originalTerm, normTerm, conceptInfoSet);
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
