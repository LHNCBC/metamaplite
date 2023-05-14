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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;

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

import gov.nih.nlm.nls.metamap.lite.mapdb.MapDbLookup;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.utils.NameIdListMap;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.utils.LRUCache;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookupRegistry;
import gov.nih.nlm.nls.metamap.lite.dictionary.AugmentedDictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opennlp.tools.dictionary.serializer.Entry;

/**
 * EntityLookup4 - Entity Lookup of text segments - sentences, lines, or other user defined segments
 */

public class EntityLookup4 implements EntityLookup {
  private static final Logger logger = LoggerFactory.getLogger(EntityLookup4.class);

  public MMLDictionaryLookup<TermInfo> dictionaryLookup;
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup4.maxtokensize","15"));
  SpecialTerms excludedTerms = new SpecialTerms();
  SentenceAnnotator sentenceAnnotator;
  SentenceExtractor sentenceExtractor;
  NegationDetector negationDetector;
  boolean addPartOfSpeechTagsFlag =
    Boolean.parseBoolean(System.getProperty("metamaplite.enable.postagging","true"));

  // In cases where there are subsumed (entirely overlapped) entities, do we remove them?
  // For example, "blood sugar level" and "blood sugar"
  // Most of the time, the answer is "yes"; in some high-recall scenarios, we don't want to.
  boolean shouldRemoveSubsumedEntities = true;

  Properties properties;

  /** Part of speech tags used for term lookup, can be set using
   * property: metamaplite.postaglist; the tag list is a set of Penn
   * Treebank part of speech tags separated by commas. */
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  public void defaultAllowedPartOfSpeech() {
    this.allowedPartOfSpeechSet.add("CD"); // cardinal number (need this for chemicals)
    this.allowedPartOfSpeechSet.add("FW"); // foreign word
    this.allowedPartOfSpeechSet.add("RB"); // should this be here?
    this.allowedPartOfSpeechSet.add("IN"); // preposition, subordinating conjunction	(in, of, like) ?what?
    this.allowedPartOfSpeechSet.add("NN");
    this.allowedPartOfSpeechSet.add("NNS");
    this.allowedPartOfSpeechSet.add("NNP");
    this.allowedPartOfSpeechSet.add("NNPS");
    this.allowedPartOfSpeechSet.add("JJ");
    this.allowedPartOfSpeechSet.add("JJR");
    this.allowedPartOfSpeechSet.add("JJS");
    this.allowedPartOfSpeechSet.add("LS"); // list item marker (need this for chemicals)
    // this.allowedPartOfSpeechSet.add("VB");
    // this.allowedPartOfSpeechSet.add("."); // abbreviation? period?
    this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
  }

  /** short form to long form user defined acronym map */
  Map<String,UserDefinedAcronym<TermInfo>> udaMap =
    new HashMap<String,UserDefinedAcronym<TermInfo>>();

  Map<String,String> uaMap = new HashMap<String,String>();

  /**
   * Instantiate EntityLookup4 instance
   *
   * @param properties metamaplite properties instance
   */
  public EntityLookup4(Properties properties) 
    throws IOException, FileNotFoundException
  {
    this.properties = properties;
    MMLDictionaryLookupRegistry registry = new MMLDictionaryLookupRegistry();
    registry.put("ivf", new IVFLookup(properties));
    registry.put("mapdb", new MapDbLookup());
    String directoryPath = properties.getProperty("metamaplite.index.directory");
    if (! new File(directoryPath).exists()) {
      throw new RuntimeException("index directory: " + directoryPath + " does not exist.");
    }
    Map.Entry<String,MMLDictionaryLookup> entry = registry.determineImplementation(directoryPath);
    if (properties.containsKey("metamaplite.cuitermlistfile.filename")) {
      MMLDictionaryLookup persistantLookup = entry.getValue();
      Map<String,List<String>> strCuiListMap =
	NameIdListMap.loadNameIdListMap
	(properties.getProperty("metamaplite.cuitermlistfile.filename"));
      this.dictionaryLookup =
	new AugmentedDictionary(persistantLookup, strCuiListMap);
      this.dictionaryLookup.init(properties);
    } else {
      if (entry == null) {
	this.dictionaryLookup = new IVFLookup(properties);
      } else {
	this.dictionaryLookup = entry.getValue();
      }
      this.dictionaryLookup.init(properties);
    }

    this.MAX_TOKEN_SIZE =
      Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
						Integer.toString(MAX_TOKEN_SIZE)));
    this.addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));

    if (this.addPartOfSpeechTagsFlag) {
      if (this.sentenceAnnotator == null) {
	this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
	String allowedPartOfSpeechTaglist = properties.getProperty("metamaplite.postaglist");
	if (allowedPartOfSpeechTaglist != null) {
	  for (String pos: allowedPartOfSpeechTaglist.split(",")) {
	    this.allowedPartOfSpeechSet.add(pos);
	  } 
	} else {
	  this.defaultAllowedPartOfSpeech();
	}
      }
    } else {
      this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
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

    if (properties.containsKey("metamaplite.excluded.termsfile")) {
      this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
    } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
      this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
    }

    // user defined acronyms
    if (properties.containsKey("metamaplite.uda.filename")) {
      String udaFilename = properties.getProperty("metamaplite.uda.filename");
      this.udaMap = UserDefinedAcronym.loadUDAList(udaFilename, this.dictionaryLookup);
      for (Map.Entry<String,UserDefinedAcronym<TermInfo>> acronym: udaMap.entrySet()) {
	logger.info(acronym.getKey() + " -> " + acronym.getValue());
      }
      this.uaMap = UserDefinedAcronym.udasToUA(this.udaMap);
    }

    if (properties.containsKey("metamaplite.removeSubsumedEntities")) {
        this.shouldRemoveSubsumedEntities = Boolean.parseBoolean(properties.getProperty("metamaplite.removeSubsumedEntities"));
    }

  }

  /**
   * Set tagger using input stream, usually from a resource
   * (classpath, servlet context, etc.)
   *
   * @param properties properties instance
   * @param instream input stream
   */
  public void setPoSTagger(Properties properties, InputStream instream) {
    if (this.addPartOfSpeechTagsFlag) {
      // skip this if pos tag is false
      if (this.sentenceAnnotator == null) {
	this.sentenceAnnotator = new OpenNLPPoSTagger(instream);
	String allowedPartOfSpeechTaglist = properties.getProperty("metamaplite.postaglist");
	if (allowedPartOfSpeechTaglist != null) {
	  for (String pos: allowedPartOfSpeechTaglist.split(",")) {
	    this.allowedPartOfSpeechSet.add(pos);
	  } 
	} else {
	  this.defaultAllowedPartOfSpeech();
	}
      }
    } else {
      this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
    }
  }

  /**
   * Given the string:
   * <pre>
   *   "cancer of the lung" -&gt; "cancer, lung" -&gt; "lung cancer"
   * </pre>
   * what it does:
   * <ul>
   *  <li>replace "of the" with comma (",")</li>
   *  <li>inversion</li>
   * </ul>
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

  public void addEvSetToSpanMap(Map<String,Entity> spanMap, Set<Ev> evSet, 
				String docid, String fieldid, String matchedText,
				String lexicalCategory,
				int sentenceNumber,
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
      Entity entity = new Entity(docid, fieldid, matchedText,
				 lexicalCategory, sentenceNumber,
				 offset, length, 0.0, evSet);
      spanMap.put(span, entity);
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
   * <pre>
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
   * </pre>
   * 
   * Check the following:
n   * <pre>
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
   * </pre>
   * @param docid document id
   * @param fieldid field id
   * @param sentenceNumber numeric index of sentence 
   * @param tokenList tokenlist of document
   * @return Span to entity map + token length map instance
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public SpanEntityMapAndTokenLength findLongestMatch(String docid, 
						      String fieldid,
						      int sentenceNumber,
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
	  if (CharUtils.isAlphaNumeric(originalTerm.charAt(0))) {
	    Set<Ev> evSet = new HashSet<Ev>();
	    Integer tokenListLength = new Integer(tokenSubList.size());

	    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
	    // TermInfo termInfo =
	    //   dictionaryLookup.lookup(originalTerm, normTerm, tokenSubList);
	    TermInfo termInfo = dictionaryLookup.lookup(originalTerm);
	    if (termInfo != null) {
	      for (ConceptInfo conceptInfo: (Collection<ConceptInfo>)termInfo.getDictionaryInfo()) {
		conceptInfoSet.add(conceptInfo);
	      }
	    }
	    termInfo = dictionaryLookup.lookup(normTerm);
	    if (termInfo != null) {
	      for (ConceptInfo conceptInfo: (Collection<ConceptInfo>)termInfo.getDictionaryInfo()) {
		conceptInfoSet.add(conceptInfo);
	      }
	    }

	    for (ConceptInfo concept: conceptInfoSet) {
	      //   if (this.termConceptCache.containsKey(normTerm)) {
	      //     for (ConceptInfo concept: this.termConceptCache.get(normTerm)) {
	      String cui = concept.getCUI();
	      if (! this.excludedTerms.isExcluded(cui,normTerm)) {
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
				     fieldid,
				     originalTerm,
				     ((ERToken)tokenSubList.get(0)).getPartOfSpeech(), // should this be noun/verb/adj phrase?
				     sentenceNumber,
				     offset, termLength);
	      longestMatchedTokenLength = Math.max(longestMatchedTokenLength,tokenTextSubList.size());
	    }	    
	  } /* if term alphabetic */
	} /* if term length > 0 */
      } /* first token has allowed partOfSpeech */
    } /* for token-sublist in list-of-token-sublists */
    return new SpanEntityMapAndTokenLength(spanMap, longestMatchedTokenLength);
  }

  /**
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence. 
   *
   * What actually happens is this:
   *
   * <ul>
   *   <li> Query the cui &lt;--&gt; sourceinfo index using the prefix of the term.</li>
   *   <li> Given the hitlist from the query, keep any matches that are
   *      a subset of the token list that has the prefix at the head of
   *      the tokenlist.</li>
   * </ul>
   *
   *  Organization of cui &lt;--&gt; sourceinfo table: cui|sui|seqno|str|src|tty
   *
   * Example from Experimental Factor Ontology [non-UMLS]:
   *
   * <pre>
   *   BTO_0001033|S00044858|1|prostate cancer cell line|obo|PT
   *   BTO_0001038|S00044209|1|peritrophic membrane|obo|PT
   *   BTO_0001093|S00034929|1|WEHI-231 cell|obo|PT
   *   BTO_0001130|S00044863|1|prostate gland cancer cell|obo|PT
   *   BTO_0001202|S00045431|1|saliva|obo|PT
   *   BTO_0001205|S00029779|1|RT4-D6P2T cell|obo|PT
   *   BTO_0001383|S00036387|1|alveolar bone|obo|PT
   * </pre>
   * 
   * To generate, see extract_mrconso_sources.perl in Public MM repository:
   *  http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/public_mm/tree/bin/extract_mrconso_sources.perl

   * Or ORF version in NLS repository:
   *  http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/nls/tree/mmtx/sources/gov/nih/nlm/nls/mmtx/dfbuilder/ExtractMrconsoSources.java
   *
   * @param docid document identifier
   * @param fieldid field identifier
   * @param sentenceTokenList sentence to be examined.
   * @param semTypeRestrictSet semantic type set to restrict to
   * @param sourceRestrictSet source list to restrict to
   * @return set of entities found in the sentence.
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public Set<Entity> processSentenceTokenList(String docid, String fieldid,
					      List<ERToken> sentenceTokenList,
					      Set<String> semTypeRestrictSet,
					      Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException {
    Set<Entity> entitySet = new HashSet<Entity>();
    int i = 0;
    while (i<sentenceTokenList.size()) {
      SpanEntityMapAndTokenLength spanEntityMapAndTokenLength = 
	this.findLongestMatch
	(docid,
	 fieldid,
	 i,
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
  /** 
   * Is entity subsumed?
   *
   * If any entity in supplied entitylist subsumes target entity then
   * the method returns true.
   *
   * @param entity target entity
   * @param entityColl collection of entities to test for subsumption.
   * @return true if entity is subsumed by at least one entity.
   */
  public static boolean isEntitySubsumed(Entity entity, Collection<Entity> entityColl) {
    List<Entity> subsumingEntityList = new ArrayList<Entity>();
    for (Entity otherEntity: entityColl) {
      if ((entity.getText() != otherEntity.getText()) &&
	  (entity.getStart() >= otherEntity.getStart()) &&
	  (entity.getStart()+entity.getLength() <= otherEntity.getStart()+otherEntity.getLength())) {
	subsumingEntityList.add(otherEntity);
      }
    }
    return subsumingEntityList.size() > 0;
  }

  /**
   * Remove any entities subsumed by any other entity.
   *
   * @param entitySet list of entities to test for subsumption.
   * @return entitySet with any subsumed entities removed.
   */
  public static Set<Entity> removeSubsumedEntities(Set<Entity> entitySet) {
    Set<Entity> newEntitySet = new HashSet<Entity>();
    for (Entity entity: entitySet) {
      if (! isEntitySubsumed(entity, entitySet)) {
	newEntitySet.add(entity);
      }
    }
    return newEntitySet;
  }

  /**
   * Apply negation detection to sentence using entity set.
   * @param entitySet entitySet associated with sentence 
   * @param sentence sentence to apply negation detection to.
   * @param tokenList token list
   * @throws Exception general exception
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
      String docid = "000000";
      String fieldid = "text";
      List<ERToken> tokenList = Scanner.analyzeText(term);
      Set<Entity> entitySet = this.processSentenceTokenList(docid, fieldid, tokenList,
							    semTypeRestrictSet,
							    sourceRestrictSet);
      entityList.addAll(entitySet);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return entityList;
  }

  /** Process text string */
  public List<Entity> processText(String docid,
				  String fieldid,
				  String text,
				  boolean detectNegationsFlag,
				  Set<String> semTypeRestrictSet,
				  Set<String> sourceRestrictSet) {
    EntityStartComparator entityComparator = new EntityStartComparator();
    try {
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      if (this.sentenceExtractor == null) {
	this.sentenceExtractor = new OpenNLPSentenceExtractor(this.properties);
      }
      List<Sentence> sentenceList = this.sentenceExtractor.createSentenceList(text);
      for (Sentence sentence: sentenceList) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
	if (this.addPartOfSpeechTagsFlag) {
	  sentenceAnnotator.addPartOfSpeech(tokenList);
	}
	Set<Entity> sentenceEntitySet =
	  this.processSentenceTokenList(docid, fieldid, tokenList,
					semTypeRestrictSet,
					sourceRestrictSet);
	sentenceEntitySet.addAll(UserDefinedAcronym.generateEntities
				 (docid, this.udaMap, tokenList));
	for (Entity entity: sentenceEntitySet) {
	  entity.setLocationPosition(i);
	}
	entitySet0.addAll(sentenceEntitySet);
	i++;
      }
      // look for negation and other relations using Context.
      for (Sentence sentence: sentenceList) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);

	// mark abbreviations that are entities and add them to entity set.
	
	Set<Entity> abbrevEntitySet =
	  new HashSet(MarkAbbreviations.markAbbreviations
		      (text, this.uaMap,
		       new ArrayList(entitySet0)));
	// dbg
	// for (Entity entity: abbrevEntitySet) {
	//   logger.debug("abbrevEntitySet.entity: " + entity);
	// }
	// end of dbg
	entitySet0.addAll(abbrevEntitySet);
	if (detectNegationsFlag) {
	  detectNegations(entitySet0, sentence.getText(), tokenList);
	}
      }

      Set<Entity> entitySet1 = new HashSet<Entity>();
      for (Entity entity: entitySet0) {
	ConceptInfoUtils.filterEntityEvListBySemanticType(entity, semTypeRestrictSet);
	ConceptInfoUtils.filterEntityEvListBySource(entity, sourceRestrictSet);
	if (entity.getEvList().size() > 0) {
	  entitySet1.add(entity);
	}
      }

        Set<Entity> entitySet;

      if (this.shouldRemoveSubsumedEntities) {
          entitySet = removeSubsumedEntities(entitySet1);
      } else {
          entitySet = entitySet1;
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

  /** Process text string */
  public List<Entity> processText(String text,
				  boolean useNegationDetection,
				  Set<String> semTypeRestrictSet,
				  Set<String> sourceRestrictSet) {
    return processText("000000", "text", text, useNegationDetection,
		       semTypeRestrictSet, sourceRestrictSet);
  }

  /** Process passage */
  public List<Entity> processPassage(String docid, BioCPassage passage,
				     boolean detectNegationsFlag,
				     Set<String> semTypeRestrictSet,
				     Set<String> sourceRestrictSet) 
  {
    EntityStartComparator entityComparator = new EntityStartComparator();
    String fieldid = passage.getInfon("section");
    try {
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
	if (this.addPartOfSpeechTagsFlag) {
	  sentenceAnnotator.addPartOfSpeech(tokenList);
	}
	Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, fieldid, tokenList,
								      semTypeRestrictSet,
								      sourceRestrictSet);
	sentenceEntitySet.addAll(UserDefinedAcronym.generateEntities(docid, this.udaMap, tokenList));
	for (Entity entity: sentenceEntitySet) {
	  entity.setLocationPosition(i);
	}
	entitySet0.addAll(sentenceEntitySet);
	i++;
      }

      // look for negation and other relations using Context.
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);

	// mark abbreviations that are entities and add them to entity set.
	
	Set<Entity> abbrevEntitySet = new HashSet(MarkAbbreviations.markAbbreviations(passage,
										      this.uaMap,
										      new ArrayList(entitySet0)));
	// dbg
	// for (Entity entity: abbrevEntitySet) {
	//   logger.debug("abbrevEntitySet.entity: " + entity);
	// }
	// end of dbg
	entitySet0.addAll(abbrevEntitySet);
	if (detectNegationsFlag) {
	  detectNegations(entitySet0, sentence.getText(), tokenList);
	}
      }

      Set<Entity> entitySet1 = new HashSet<Entity>();
      for (Entity entity: entitySet0) {
	ConceptInfoUtils.filterEntityEvListBySemanticType(entity, semTypeRestrictSet);
	ConceptInfoUtils.filterEntityEvListBySource(entity, sourceRestrictSet);
	if (entity.getEvList().size() > 0) {
	  entitySet1.add(entity);
	}
      }


      Set<Entity> entitySet;
      if (this.shouldRemoveSubsumedEntities) {
          entitySet = removeSubsumedEntities(entitySet1);
      } else {
          entitySet = entitySet1;
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
    EntityStartComparator entityComparator = new EntityStartComparator();
    String fieldid = "text";
    Set<Entity> entitySet0 = new HashSet<Entity>();
    int i = 0;
    for (Sentence sentence: sentenceList) {
      List<ERToken> tokenList = Scanner.analyzeText(sentence);
      if (this.addPartOfSpeechTagsFlag) {
	sentenceAnnotator.addPartOfSpeech(tokenList);
      }
      Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, fieldid, tokenList,
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
    Set<Entity> entitySet;
    if (this.shouldRemoveSubsumedEntities) {
        entitySet = removeSubsumedEntities(entitySet0);
    } else {
        entitySet = entitySet0;
    }

    List<Entity> resultList = new ArrayList<Entity>(entitySet);
    Collections.sort(resultList, entityComparator);
    return resultList;
  }

  public Set<BioCAnnotation> generateBioCEntitySet(String docid,
						   List<ERToken> sentenceTokenList)
  {
    String fieldid = "text";
    try {
      Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
      Set<Entity> entitySet = this.processSentenceTokenList(docid, fieldid, sentenceTokenList,
              new HashSet<String>(),
              new HashSet<String>());
      if (this.shouldRemoveSubsumedEntities) {
          entitySet = removeSubsumedEntities(entitySet);
      }

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

