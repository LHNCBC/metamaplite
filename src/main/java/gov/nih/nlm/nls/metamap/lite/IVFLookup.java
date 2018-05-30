package gov.nih.nlm.nls.metamap.lite;

import java.io.FileNotFoundException;
import java.io.IOException;

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

/**
 * Describe class IVFLookup here.
 *
 *
 * Created: Wed Apr 18 15:10:13 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class IVFLookup implements DictionaryLookup<TermInfo> {
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

  public static class IVFTermInfo implements TermInfo {
    String originalTerm;
    String normTerm;
    Set<ConceptInfo> dictionaryInfo;
    List<? extends Token> tokenList;
    public IVFTermInfo(String originalTerm, 
		       String normTerm,
		       Set<ConceptInfo> dictionaryInfo,
		       List<? extends Token> tokenSubList) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = dictionaryInfo;
      this.tokenList = tokenSubList;
    }
    public IVFTermInfo(String originalTerm, 
		       String normTerm,
		       Set<ConceptInfo> dictionaryInfo) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = dictionaryInfo;
    }
    /** normalized form of term */
    public String getNormTerm() { return this.normTerm; }
    /** original term */
    public String getOriginalTerm() { return this.originalTerm; }
    /** dictionary info */
    public Set<ConceptInfo> getDictionaryInfo() { return this.dictionaryInfo; }
    public List<? extends Token> getTokenList() { return this.tokenList; }
    public String toString() { return this.originalTerm + "|" + this.normTerm + "|" + this.dictionaryInfo; }
  }

  /**
   * Creates a new <code>IVFLookup</code> instance.
   * @param properties metamaplite configuration properties instance
   */
  public IVFLookup(Properties properties) {
    try {
      this.MAX_TOKEN_SIZE =
	Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
						Integer.toString(MAX_TOKEN_SIZE)));

      this.mmIndexes = new MetaMapIvfIndexes(properties);
      this.cuiPreferredNameCache = new CuiPreferredNameCache(properties, mmIndexes);
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

  // Implementation of gov.nih.nlm.nls.metamap.lite.DictionaryLookup

  /**
   * Lookup term in IVF index 
   *
   * @param originalTerm original term
   * @param normTerm normalized form of term
   * @param tokenSubList a tokenlist 
   * @return a <code>TermInfo</code> value associated with input term.
   */
  public final TermInfo lookup(final String originalTerm, 
			       final String normTerm, 
			       final List<? extends Token> tokenSubList) {
    try {
    return new IVFTermInfo
      (originalTerm,
       normTerm,
       this.termConceptInfoCache.lookupTermConceptInfo(originalTerm,
						       normTerm,
						       tokenSubList),
       tokenSubList);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }


  /**
   * Lookup term in IVF index
   *
   * @param originalTerm original term
   * @param normTerm normalized form of term
   * @return a <code>TermInfo</code> value associated with input term.
   */
  public final TermInfo lookup(final String originalTerm, 
			       final String normTerm) {
    try {
    return new IVFTermInfo
      (originalTerm,
       normTerm,
       this.termConceptInfoCache.lookupTermConceptInfo(originalTerm,
						       normTerm));
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  
}
  
