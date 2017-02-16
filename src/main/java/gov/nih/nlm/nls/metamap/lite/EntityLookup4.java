//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Properties;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.OutputStreamWriter;

import bioc.BioCSentence;
import bioc.BioCAnnotation;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCLocation;


import gov.nih.nlm.nls.metamap.lite.context.ContextWrapper;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.TokenListUtils;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

import gov.nih.nlm.nls.types.Sentence;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.utils.LRUCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.dictionary.serializer.Entry;

/**
 *
 */

public class EntityLookup4 implements EntityLookup {
  private static final Logger logger = LogManager.getLogger(EntityLookup4.class);

  /** Set property
   * "metamaplite.entitylookup4.term.concept.cache.enable" to true to
   * enable term --> concept cache. */
  boolean enableTermConceptInfoCache = 
    Boolean.getBoolean("metamaplite.entitylookup4.term.concept.cache.enable");
  /** Set property
   * "metamaplite.entitylookup4.cui.preferredname.cache.enable" to
   * true to enable cui --> preferred name cache. */
  boolean enableCuiPreferredNameCache =
    Boolean.getBoolean("metamaplite.entitylookup4.cui.preferredname.cache.enable");
  
  public MetaMapIvfIndexes mmIndexes;
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  
  /** string column for cuisourceinfo index*/
  int strColumn = 3;		
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		
  SpecialTerms excludedTerms = new SpecialTerms();
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup4.maxtokensize","15"));

  SentenceAnnotator sentenceAnnotator;
  NegationDetector negationDetector;
  boolean addPartOfSpeechTagsFlag =
    Boolean.parseBoolean(System.getProperty("metamaplite.enable.postagging","true"));
  
  public void defaultAllowedPartOfSpeech() {
    this.allowedPartOfSpeechSet.add("RB"); // should this be here?
    this.allowedPartOfSpeechSet.add("NN");
    this.allowedPartOfSpeechSet.add("NNS");
    this.allowedPartOfSpeechSet.add("NNP");
    this.allowedPartOfSpeechSet.add("NNPS");
    this.allowedPartOfSpeechSet.add("JJ");
    this.allowedPartOfSpeechSet.add("JJR");
    this.allowedPartOfSpeechSet.add("JJS");
    this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
  }

  public EntityLookup4() 
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes();
    this.sentenceAnnotator = new SentenceAnnotator();
    this.defaultAllowedPartOfSpeech();
    this.negationDetector = new ContextWrapper();
  }

  public EntityLookup4(Properties properties) 
    throws IOException, FileNotFoundException
  {
    // override any system properties here
    this.enableTermConceptInfoCache = 
      Boolean.parseBoolean
      (properties.getProperty
       ("metamaplite.entitylookup4.term.concept.cache.enable",
	Boolean.toString(this.enableTermConceptInfoCache)));
    this.enableCuiPreferredNameCache =
      Boolean.parseBoolean
      (properties.getProperty
       ("metamaplite.entitylookup4.cui.preferredname.cache.enable",
	Boolean.toString(this.enableCuiPreferredNameCache)));
    
    this.mmIndexes = new MetaMapIvfIndexes(properties);
    
    this.addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));

    if (this.addPartOfSpeechTagsFlag) {
      this.sentenceAnnotator = new SentenceAnnotator(properties);
    }

    // Instantiate user-specified negation detector if present,
    // otherwise use ConText.
    try {
      this.negationDetector =
	(NegationDetector)
	Class.forName
	(properties.getProperty
	 ("metamaplite.negation.detector",
	  "gov.nih.nlm.nls.metamap.lite.context.ContextWrapper")).newInstance();
      this.negationDetector.initProperties(properties);
      // this.negationDetector = new ContextWrapper();
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException(cnfe);
    } catch (InstantiationException ie) {
      throw new RuntimeException(ie);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }

    this.defaultAllowedPartOfSpeech();
    if (properties.containsKey("metamaplite.excluded.termsfile")) {
      this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
    } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
      this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
    }
    MAX_TOKEN_SIZE =
      Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
					      Integer.toString(MAX_TOKEN_SIZE)));
  }

  /** cache of string -&gt; concept and attributes */
  public static LRUCache<String,Set<ConceptInfo>> termConceptCache = 
    new LRUCache<String,Set<ConceptInfo>>
    (Integer.parseInt
     (System.getProperty
      ("metamaplite.entity.lookup4.term.concept.cache.size","10000")));

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
						    this.findPreferredName(cui),
						    this.getSourceSet(cui),
						    this.getSemanticTypeSet(cui));
	  conceptInfoSet.add(conceptInfo);
	}
      }
    }
    return conceptInfoSet;
  }

  /**
   * Lookup Term - if term info is already in cache then use cached
   * term info, otherwise, lookup term info in index.
   * @param term Term to lookup
   * @return map of entities keyed by span
   */
  public Set<ConceptInfo> lookupTermConceptInfo(String originalTerm,
						String normTerm,
						List<? extends Token> tokenlist)
    throws FileNotFoundException, IOException
  {
    if (enableTermConceptInfoCache) {
      if (this.termConceptCache.containsKey(normTerm) ) {
	return this.termConceptCache.get(normTerm);
      } else {
	Set<ConceptInfo> conceptInfoSet = lookupTermConceptInfoIVF(originalTerm, normTerm, tokenlist);
	this.cacheConceptInfoSet(normTerm, conceptInfoSet);
	return conceptInfoSet;
      }
    } else {
      return lookupTermConceptInfoIVF(originalTerm, normTerm, tokenlist);
    }
  }

  public static LRUCache<String,String> cuiPreferredNameCache =
  new LRUCache<String,String>
     (Integer.parseInt
      (System.getProperty
       ("metamaplite.entity.lookup4.cui.preferred.name.cache.size","10000")));
  
  public void cachePreferredTerm(String cui, String preferredTerm) {
    synchronized (cuiPreferredNameCache) {
      cuiPreferredNameCache.put(cui, preferredTerm);
    }
  }

  /**
   * Lookup preferred name for cui (concept unique identifier) in inverted file.
   * @param cui target cui
   * @return preferredname for cui or null if not found
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
      return lookupPreferredNameIVF(cui);
    }
  }

  /**
   * Get source vocabulary abbreviations for cui (concept unique identifier)
   * @param cui target cui
   * @return set of source vocabulary abbreviations a for cui or empty set if none found.
   */
  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> sourceSet = new HashSet<String>();
    List<String> hitList =
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, 0);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      sourceSet.add(fields[4]);
    }
    return sourceSet;
  }

  /**
   * Get semantic type set for cui (concept unique identifier)
   * @param cui target cui
   * @return set of semantic type abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public Set<String> getSemanticTypeSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> semanticTypeSet = new HashSet<String>();
    List<String> hitList = 
      this.mmIndexes.cuiSemanticTypeIndex.lookup(cui, this.cuiColumn);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      semanticTypeSet.add(fields[1]);
    }
    return semanticTypeSet;
  }

  /**
   * Given the string:
   *   "cancer of the lung" -&gt; "cancer, lung" -&gt; "lung cancer"
   * <pre>
   * what it does:
   *  1. replace "of the" with comma (",")
   *  2. inversion
   * </pre>
   * TBD: should be updated for other relevant prepositions.
   *
   * @param inputtext input text
   * @return string with preposition "of the" removed and the term inverted.
   */
  public static String transformPreposition(String inputtext) {
    if (inputtext.indexOf(" of the") > 0) {
      return NormalizedStringCache.normalizeString(inputtext.replaceAll(" of the", ","));
    } 
    return inputtext;
  }

  public class SpanEntityMapAndTokenLength {
    Map<String,Entity> spanEntityMap;
    int length;
    public SpanEntityMapAndTokenLength(    Map<String,Entity> spanEntityMap, int length) {
      this.spanEntityMap = spanEntityMap;
      this.length = length;
    }
    public Map<String,Entity> getSpanEntityMap() {
      return this.spanEntityMap;
    }
    public int getLength() {
      return this.length;
    }
    public List<Entity> getEntityList() {
      return new ArrayList<Entity>(this.spanEntityMap.values());
    }
  }


  public class SpanInfo {
    int start;
    int length;
    int getStart()  { return this.start; }
    int getLength() { return this.length; }
  }

  public void addEvSetToSpanMap(Map<String,Entity> spanMap, Set<Ev> evSet, 
				String docid, String matchedText, 
				int offset, int length) {
    String span = offset + ":" + length;
    if (spanMap.containsKey(span)) {
      Entity entity = spanMap.get(span);
      Set<String> currentCuiSet = new HashSet<String>();
      for (Ev currentEv: entity.getEvSet()) {
	currentCuiSet.add(currentEv.getConceptInfo().getCUI());
      }
      for (Ev newEv: evSet) {
	if (! currentCuiSet.contains(newEv.getConceptInfo().getCUI())) {
	  entity.addEv(newEv);
	}
      }
    } else {
      Entity entity = new Entity(docid, matchedText, offset, length, 0.0, evSet);
      spanMap.put(span, entity);
    }
  }

  boolean isCuiInSemanticTypeRestrictSet(String cui, Set<String> semanticTypeRestrictSet)
  {
    if (semanticTypeRestrictSet.isEmpty() || semanticTypeRestrictSet.contains("all"))
      return true;
    try {
      boolean inSet = false;
      for (String semtype: getSemanticTypeSet(cui)) {
	inSet = inSet || semanticTypeRestrictSet.contains(semtype);
      }
      return inSet;
    } catch (FileNotFoundException fnfe) {
      return false;
    } catch (IOException ioe) {
      return false;
    }
  }


  boolean isCuiInSourceRestrictSet(String cui, Set<String> sourceRestrictSet)
  {
    if (sourceRestrictSet.isEmpty() || sourceRestrictSet.contains("all"))
      return true;
    try {
      boolean inSet = false;
      for (String semtype: getSourceSet(cui)) {
	inSet = inSet || sourceRestrictSet.contains(semtype);
      }
      return inSet;
    } catch (FileNotFoundException fnfe) {
      return false;
    } catch (IOException ioe) {
      return false;
    }
  }

  public static boolean isLikelyMatch(String term, String normTerm, String docStr) {
    if (term.length() < 5) {
      return term.equals(docStr);
    } else {
      return normTerm.equals(NormalizedStringCache.normalizeString(docStr));
    }
  }


  /**
   * Given Example:
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
   * 
   * Check the following:
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity"
   *   "Papillary Thyroid Carcinoma is a Unique Clinical"
   *   "Papillary Thyroid Carcinoma is a Unique"
   *   "Papillary Thyroid Carcinoma is a"
   *   "Papillary Thyroid Carcinoma is"
   *   "Papillary Thyroid Carcinoma"
   *   "Papillary Thyroid"
   *   "Papillary"
   *             "Thyroid Carcinoma is a Unique Clinical Entity"
   *             "Thyroid Carcinoma is a Unique Clinical"
   *             "Thyroid Carcinoma is a Unique"
   *             "Thyroid Carcinoma is a"
   *             "Thyroid Carcinoma is"
   *             "Thyroid Carcinoma"
   *             "Thyroid"
   *    ...
   * @param docid document id
   * @param tokenList tokenlist of document
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public SpanEntityMapAndTokenLength findLongestMatch(String docid, 
						      List<ERToken> tokenList)
    throws FileNotFoundException, IOException
  {
    logger.debug("findLongestMatch");
    String normTerm ="";
    int longestMatchedTokenLength = 0;
    // span -> entity list map
    Map<String,Entity> spanMap = new HashMap<String,Entity>();
    List<List<? extends Token>> listOfTokenSubLists = TokenListUtils.createSubListsOpt(tokenList);
    for (List<? extends Token> tokenSubList: listOfTokenSubLists) {
      List<String> tokenTextSubList = new ArrayList<String>();
      for (Token token: tokenSubList) {
       	tokenTextSubList.add(token.getText());
      }
      ERToken firstToken = (ERToken)tokenSubList.get(0);
      ERToken lastToken = (ERToken)tokenSubList.get(tokenSubList.size() - 1);
      if ((! firstToken.getText().toLowerCase().equals("other")) &&
       	  this.allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech())) {
       	int termLength = (tokenSubList.size() > 1) ?
       	  (lastToken.getOffset() + lastToken.getText().length()) - firstToken.getOffset() : 
       	  firstToken.getText().length();
       	String originalTerm = StringUtils.join(tokenTextSubList, "");
	if ((originalTerm.length() > 2) &&
	    (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	 
	  // String term = originalTerm;
	  // String query = term;
	  normTerm = NormalizedStringCache.normalizeString(originalTerm);
	  int offset = ((PosToken)tokenSubList.get(0)).getOffset();
	  if (CharUtils.isAlpha(originalTerm.charAt(0))) {
	    Set<Ev> evSet = new HashSet<Ev>();
	    Integer tokenListLength = new Integer(tokenSubList.size());

	    for (ConceptInfo concept: lookupTermConceptInfo(originalTerm,
							    normTerm,
							    tokenSubList)) {
	      //   if (this.termConceptCache.containsKey(normTerm)) {
	      //     for (ConceptInfo concept: this.termConceptCache.get(normTerm)) {
	      String cui = concept.getCUI();
	      Ev ev = new Ev(concept,
			     originalTerm,
			     normTerm,
			     ((PosToken)tokenSubList.get(0)).getOffset(),
			     termLength,
			     0.0,
			     ((ERToken)tokenSubList.get(0)).getPartOfSpeech());
	      if (! evSet.contains(ev)) {
		logger.debug("add ev: " + ev);
		evSet.add(ev);
	      }
	      //   } else {
	      //     // if not in cache then lookup term 
	      //     for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(normTerm, 3)) {
	      // 	String[] fields = doc.split("\\|");
	      // 	String cui = fields[0];
	      // 	String docStr = fields[3];
	      
	      // 	// If term is not in excluded term list and term or
	      // 	// normalized form of term matches lookup string or
	      // 	// normalized form of lookup string then get
	      // 	// information about lookup string.
	      // 	if ((! excludedTerms.isExcluded(cui,normTerm)) && isLikelyMatch(term,normTerm,docStr)) {
	      // 	  if (tokenSubList.get(0) instanceof PosToken) {
	      // 	    ConceptInfo concept = new ConceptInfo(cui, 
	      // 						  this.findPreferredName(cui),
	      // 						  this.getSourceSet(cui),
	      // 						  this.getSemanticTypeSet(cui));
	      // 	    this.cacheConcept(normTerm, concept);
	      // 	    cui = concept.getCUI();
	      // 	    Ev ev = new Ev(concept,
	      // 			   originalTerm,
	      // 			   docStr,
	      // 			   offset,
	      // 			   termLength,
	      // 			   0.0,
	      // 			   ((ERToken)tokenSubList.get(0)).getPartOfSpeech());
	      // 	    if (! evSet.contains(ev)) {
	      // 	      logger.debug("add ev: " + ev);
	      // 	      evSet.add(ev);
	      // 	    }
	      // 	  } /*if token instance of PosToken*/
	      // 	} /*if term equals doc string */
	      //     } /* for doc in documentList */
	    } /* if term in concept cache */
	    if (evSet.size() > 0) {
	      this.addEvSetToSpanMap(spanMap, evSet, 
	  			     docid,
	  			     originalTerm,
	  			     offset, termLength);
	      longestMatchedTokenLength = Math.max(longestMatchedTokenLength,tokenTextSubList.size());
	    }
	    
	  } /* if term alphabetic */
	} /* if term length > 0 */
      } /* first token has allowed partOfSpeech */
    } /* for token-sublist in list-of-token-sublists */
    return new SpanEntityMapAndTokenLength(spanMap, longestMatchedTokenLength);
  }

  public void filterEntityEvListBySemanticType(Entity entity, Set<String> semanticTypeRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      if (isCuiInSemanticTypeRestrictSet(cui, semanticTypeRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }


  public void filterEntityEvListBySource(Entity entity, Set<String> sourceRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      if (isCuiInSourceRestrictSet(cui, sourceRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }

  /**
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence. 
   *
   * What actually happens is this:
   *
   *   1. Query the cui &lt;--&gt; sourceinfo index using the prefix of the term.
   *   2. Given the hitlist from the query, keep any matches that are
   *      a subset of the token list that has the prefix at the head of
   *      the tokenlist.
   *
   *
   *  Organization of cui &lt;--&gt; sourceinfo table: cui|sui|seqno|str|src|tty
   *
   * Example from Experimental Factor Ontology [non-UMLS]:
   *
   *   BTO_0001033|S00044858|1|prostate cancer cell line|obo|PT
   *   BTO_0001038|S00044209|1|peritrophic membrane|obo|PT
   *   BTO_0001093|S00034929|1|WEHI-231 cell|obo|PT
   *   BTO_0001130|S00044863|1|prostate gland cancer cell|obo|PT
   *   BTO_0001202|S00045431|1|saliva|obo|PT
   *   BTO_0001205|S00029779|1|RT4-D6P2T cell|obo|PT
   *   BTO_0001383|S00036387|1|alveolar bone|obo|PT
   * 
   * To generate, see extract_mrconso_sources.perl in Public MM repository:
   *  http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/public_mm/tree/bin/extract_mrconso_sources.perl

   * Or ORF version in NLS repository:
   *  http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/nls/tree/mmtx/sources/gov/nih/nlm/nls/mmtx/dfbuilder/ExtractMrconsoSources.java
   *
   * @param sentenceTokenList sentence to be examined.
   * @param semTypeRestrictSet semantic type 
   * @param sourceRestrictSet source list to restrict to
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentenceTokenList(String docid, List<ERToken> sentenceTokenList,
					      Set<String> semTypeRestrictSet,
					      Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException {
    Set<Entity> entitySet = new HashSet<Entity>();    int i = 0;
    while (i<sentenceTokenList.size()) {
      SpanEntityMapAndTokenLength spanEntityMapAndTokenLength = 
	this.findLongestMatch
	(docid,
	 sentenceTokenList.subList(i,Math.min(i+MAX_TOKEN_SIZE,sentenceTokenList.size())));
      for (Entity entity: spanEntityMapAndTokenLength.getEntityList()) {
	if (entity.getEvList().size() > 0) {
	  entitySet.add(entity);
	}
      }
      i++;
    } /*while*/
    return entitySet;
  }

  // static methods
  public static Set<Entity> removeSubsumingEntities(Set<Entity> entitySet) {
    Map <Integer,Entity> startMap = new HashMap<Integer,Entity>();
    logger.debug("-input entity set spans-");
    for (Entity entity: entitySet) {
      Integer key = new Integer(entity.getStart());
      if (startMap.containsKey(key)) {
	if (startMap.get(key).getLength() < entity.getLength()) {
	  // replace entity with larger span
	  startMap.put(key,entity);
	}
      } else {
	startMap.put(key, entity);
      }
    }
    logger.debug("-shorter entities with same start have been removed-");
    Map <Integer,Entity> endMap = new HashMap<Integer,Entity>();
    for (Entity entity: startMap.values()) {
      Integer key = new Integer(entity.getStart() + entity.getLength());
      if (endMap.containsKey(key)) {
	if (endMap.get(key).getStart() > entity.getStart()) {
	  // replace entity with larger span
	  endMap.put(key,entity);
	}
      } else {
	endMap.put(key, entity);
      }
    }

    logger.debug("-final entity set spans-");
    Set<Entity> newEntitySet = new HashSet<Entity>();
    for (Entity entity: endMap.values()) {
      newEntitySet.add(entity);
    }
    return newEntitySet;
  }

  static class EntityStartComparator implements Comparator<Entity> {
    public int compare(Entity o1, Entity o2) { return o1.getStart() - o2.getStart(); }
    public boolean equals(Object obj) { return false; }
    public int hashCode() { return 0; }
  }

  static EntityStartComparator entityComparator = new EntityStartComparator();

  /**
   * Apply negation detection to sentence using entity set.
   * @param entitySet entitySet associated with sentence 
   * @param sentence sentence to apply negation detection to.
   */
  public void detectNegations(Set<Entity> entitySet, String sentence, List<ERToken> tokenList) 
    throws Exception {
    this.negationDetector.detectNegations(entitySet, sentence, tokenList);
  }

  public List<Entity> lookupTerm(String term,
				 Set<String> semTypeRestrictSet,
				 Set<String> sourceRestrictSet) {
    List<Entity> entityList = new ArrayList<Entity>();
    try {
      String docid = "text";
      List<ERToken> tokenList = Scanner.analyzeText(term);
      Set<Entity> entitySet = this.processSentenceTokenList(docid, tokenList,
							    semTypeRestrictSet,
							    sourceRestrictSet);
      entityList.addAll(entitySet);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return entityList;
  }

  /** Process passage */
  public List<Entity> processPassage(String docid, BioCPassage passage,
				     boolean detectNegationsFlag,
				     Set<String> semTypeRestrictSet,
				     Set<String> sourceRestrictSet) 
  {
    try {
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
	if (this.addPartOfSpeechTagsFlag) {
	  sentenceAnnotator.addPartOfSpeech(tokenList);
	}
	Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, tokenList,
								      semTypeRestrictSet,
								      sourceRestrictSet);
	for (Entity entity: sentenceEntitySet) {
	  entity.setLocationPosition(i);
	}
	entitySet0.addAll(sentenceEntitySet);
	// look for negation and other relations using Context.
	if (detectNegationsFlag) {
	  detectNegations(sentenceEntitySet, sentence.getText(), tokenList);
	}
	i++;
      }
      Set<Entity> entitySet1 = removeSubsumingEntities(entitySet0);
      Set<Entity> entitySet = new HashSet<Entity>();
      for (Entity entity: entitySet1) {
	filterEntityEvListBySemanticType(entity, semTypeRestrictSet);
	filterEntityEvListBySource(entity, sourceRestrictSet);
	if (entity.getEvList().size() > 0) {
	  entitySet.add(entity);
	}
      }
      List<Entity> resultList = new ArrayList<Entity>(entitySet);
      Collections.sort(resultList, entityComparator);
      return resultList;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Entity> processSentences(String docid, List<Sentence> sentenceList,
				       boolean detectNegationsFlag,
				       Set<String> semTypeRestrictSet,
				       Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException, Exception
  {
    Set<Entity> entitySet0 = new HashSet<Entity>();
    int i = 0;
    for (Sentence sentence: sentenceList) {
      List<ERToken> tokenList = Scanner.analyzeText(sentence);
      if (this.addPartOfSpeechTagsFlag) {
	sentenceAnnotator.addPartOfSpeech(tokenList);
      }
      Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, tokenList,
								    semTypeRestrictSet,
								    sourceRestrictSet);
      for (Entity entity: sentenceEntitySet) {
	entity.setLocationPosition(i);
      }
      entitySet0.addAll(sentenceEntitySet);
      // fix this for ConText
      // look for negation and other relations using Context.
      if (detectNegationsFlag) {
	// detectNegation(sentenceEntitySet, sentence, tokenList);
      }
      i++;
    }
    Set<Entity> entitySet = removeSubsumingEntities(entitySet0);
    List<Entity> resultList = new ArrayList<Entity>(entitySet);
    Collections.sort(resultList, entityComparator);
    return resultList;
  }

  public Set<BioCAnnotation> generateBioCEntitySet(String docid,
						   List<ERToken> sentenceTokenList)
  {
    try {
      Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
      Set<Entity> entitySet = 
	removeSubsumingEntities
	(this.processSentenceTokenList(docid, sentenceTokenList,
				       new HashSet<String>(),
				       new HashSet<String>()));
      for (Entity entity: entitySet) {
	for (Ev ev: entity.getEvList()) {
	  BioCAnnotation entityAnnotation = new BioCAnnotation();
	  // entity attributes
	  entityAnnotation.setText(entity.getMatchedText());
	  entityAnnotation.addLocation(new BioCLocation(entity.getOffset(), entity.getLength()));
	  entityAnnotation.putInfon("score",Double.toString(entity.getScore()));
	  entityAnnotation.putInfon("negated",Boolean.toString(entity.isNegated()));
	  // ev (evaluation) attributes
	  entityAnnotation.putInfon("partofspeech", ev.getPartOfSpeech());
	  // concept info attributes
	  entityAnnotation.putInfon("cui", ev.getConceptInfo().getCUI());
	  entityAnnotation.putInfon("preferredname", ev.getConceptInfo().getPreferredName());
	  entityAnnotation.putInfon("semantictypes",
				    Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()));
	  entityAnnotation.putInfon("sources",
				    Arrays.toString(ev.getConceptInfo().getSourceSet().toArray()));
	  bioCEntityList.add(entityAnnotation);
	}
      }
      return bioCEntityList;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }


  BioCPassage bioCProcessPassage(String docid, BioCPassage passage, boolean useContext,
				 Set<String> semTypeRestrictSet,
				 Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException, Exception {
    List<Entity> entityList = processPassage(docid, passage, useContext,
					     semTypeRestrictSet,
					     sourceRestrictSet);
    BioCPassage newPassage = new BioCPassage(passage);
    List<BioCAnnotation> annotationList = new ArrayList<BioCAnnotation>();
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvList()) {
	// convert ev to set of bioCAnnotations
	BioCAnnotation annotation = new BioCAnnotation();
	annotation.setLocation(ev.getStart(), ev.getLength());
	annotation.setText(ev.getMatchedText());
	annotation.putInfon("part-of-speech", ev.getPartOfSpeech());
	annotation.putInfon("score", Double.toString(ev.getScore()));
	annotation.putInfon("evalution-id", ev.getId());
	annotation.putInfon("entity-id", entity.getId());
	annotation.putInfon("negation", Boolean.toString(entity.isNegated()));
	annotation.putInfon("temporality", entity.getTemporality());
	annotation.putInfon("location", Integer.toString(entity.getLocationPosition()));
	annotation.putInfon("cui", ev.getConceptInfo().getCUI());
	annotation.putInfon("preferredName",  ev.getConceptInfo().getPreferredName());
	for (String semanticType: ev.getConceptInfo().getSemanticTypeSet()) {
	  annotation.putInfon("semanticType", semanticType);
	}
	for (String source: ev.getConceptInfo().getSourceSet()) {
	  annotation.putInfon("UMLS Source",  source);
	}
	annotationList.add(annotation);
      }
    }
    // newPassage.setAnnotations(annotationList); // BioC 1.0.1
    for (BioCAnnotation annotation: annotationList) {
      passage.addAnnotation(annotation);
    }
    return newPassage;
  }
}

