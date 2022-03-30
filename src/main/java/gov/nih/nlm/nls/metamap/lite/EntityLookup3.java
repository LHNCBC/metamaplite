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
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opennlp.tools.dictionary.serializer.Entry;

/**
 *
 */
public class EntityLookup3 implements EntityLookup {
  private static final Logger logger = LoggerFactory.getLogger(EntityLookup3.class);

  public MetaMapIvfIndexes mmIndexes;
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  
  /** string column for cuisourceinfo index*/
  int strColumn = 3;		
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		
  SpecialTerms excludedTerms = new SpecialTerms();
  static final int MAX_TOKEN_SIZE = Integer.parseInt(System.getProperty("metamaplite.entitylookup3.maxtokensize","15"));

  SentenceAnnotator sentenceAnnotator;

  public void defaultAllowedPartOfSpeech() {
    this.allowedPartOfSpeechSet.add("RB"); // should this be here?
    this.allowedPartOfSpeechSet.add("NN");
    this.allowedPartOfSpeechSet.add("NNS");
    this.allowedPartOfSpeechSet.add("NNP");
    this.allowedPartOfSpeechSet.add("NNPS");
    this.allowedPartOfSpeechSet.add("JJ");
    this.allowedPartOfSpeechSet.add("JJR");
    this.allowedPartOfSpeechSet.add("JJS");
  }

  public EntityLookup3() 
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes();
    this.sentenceAnnotator = new OpenNLPPoSTagger();
    this.defaultAllowedPartOfSpeech();
  }

  public EntityLookup3(Properties properties) 
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes(properties);
    this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
    String allowedPartOfSpeechTaglist = properties.getProperty("metamaplite.postaglist");
    if (allowedPartOfSpeechTaglist != null) {
	for (String pos: allowedPartOfSpeechTaglist.split(",")) {
	  this.allowedPartOfSpeechSet.add(pos);
	}
	this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
      } else {
      this.defaultAllowedPartOfSpeech();
    }
    if (System.getProperty("metamaplite.excluded.termsfile") != null) {
      this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
    } else if (properties.containsKey("metamaplite.excluded.termsfile") &&
	       (this.excludedTerms.size() == 0)) {
      this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
    }
  }

  /** cache of string -&gt; concept and attributes */
  public static Map<String,Set<ConceptInfo>> termConceptCache = new HashMap<String,Set<ConceptInfo>>();

  public void cacheConcept(String term, ConceptInfo concept) {
    synchronized (termConceptCache) {
      if (termConceptCache.containsKey(term)) {
	synchronized (termConceptCache.get(term)) {
	  termConceptCache.get(term).add(concept);
	}
      } else {
	Set<ConceptInfo> newConceptSet = new HashSet<ConceptInfo>();
	newConceptSet.add(concept);
	termConceptCache.put(term, newConceptSet);
      }
    }
  }

  public static Map<String,String> cuiPreferredNameCache =  new HashMap<String,String>();
  
  public void cachePreferredTerm(String cui, String preferredTerm) {
    synchronized (cuiPreferredNameCache) {
      cuiPreferredNameCache.put(cui, preferredTerm);
    }
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
    if (cuiPreferredNameCache.containsKey(cui)) {
      return cuiPreferredNameCache.get(cui);
    } else {
      List<String> hitList = 
	this.mmIndexes.cuiConceptIndex.lookup(cui, 0);
      if (hitList.size() > 0) {
	String[] fields = hitList.get(0).split("\\|");
	this.cachePreferredTerm(cui, fields[1]);
	return fields[1];
      }
    }
    return null;
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
    // logger.debug("entering: transformPreposition");
    if (inputtext.indexOf(" of the") > 0) {
      // return MWIUtilities.normalizeAstString(inputtext.replaceAll(" of the", ","));
      return NormalizedStringCache.normalizeString(inputtext.replaceAll(" of the", ","));
    } 
    // logger.debug("leaving: transformPreposition");
    return inputtext;
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
   * @return span to entity and token length map instance
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
      // logger.debug("firstToken: " + firstToken + "-> " + firstToken.getPartOfSpeech() +
      // 		   " -> " +
      // 		   this.allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech()));
      if ((! firstToken.getText().toLowerCase().equals("other")) &&
	  this.allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech())) {
	int termLength = (tokenSubList.size() > 1) ?
	  (lastToken.getOffset() + lastToken.getText().length()) - firstToken.getOffset() : 
	  firstToken.getText().length();
	String originalTerm = StringUtils.join(tokenTextSubList, "");
	// logger.debug("originalTerm: " + originalTerm);
	if ((originalTerm.length() > 2) &&
	    (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	  String term = originalTerm;
	  String query = term;
	  // String normTerm = MWIUtilities.normalizeAstString(term);
	  normTerm = NormalizedStringCache.normalizeString(term);
	  int offset = ((PosToken)tokenSubList.get(0)).getOffset();
	  if (CharUtils.isAlpha(term.charAt(0))) {
	    Set<Ev> evSet = new HashSet<Ev>();
	    Integer tokenListLength = new Integer(tokenSubList.size());
	    if (termConceptCache.containsKey(normTerm)) {
	      for (ConceptInfo concept: termConceptCache.get(normTerm)) {
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
	      }
	    } else {
	      // if not in cache then lookup term 
	      for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(normTerm, 3)) {
		String[] fields = doc.split("\\|");
		String cui = fields[0];
		String docStr = fields[3];
		// logger.debug("term: \"" + term + 
		// 	     "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
		// 	     term.equalsIgnoreCase(docStr));
		// if (normTerm.equals(MWIUtilities.normalizeAstString(docStr))) {
	      if (logger.isDebugEnabled() &&
		  excludedTerms.isExcluded(cui,normTerm)) {
		// logger.debug( cui + "|" + normTerm + " is in excluded terms file.");
	      }
	      if ((! excludedTerms.isExcluded(cui,normTerm)) && isLikelyMatch(term,normTerm,docStr)) {
		if (tokenSubList.get(0) instanceof PosToken) {
		  ConceptInfo concept = new ConceptInfo(cui, 
							this.findPreferredName(cui),
							this.getSourceSet(cui),
							this.getSemanticTypeSet(cui));
		  this.cacheConcept(docStr, concept);
		  cui = concept.getCUI();
		  Ev ev = new Ev(concept,
				 originalTerm,
				 docStr,
				 offset,
				 termLength,
				 0.0,
				 ((ERToken)tokenSubList.get(0)).getPartOfSpeech());
		  if (! evSet.contains(ev)) {
		    logger.debug("add ev: " + ev);
		    evSet.add(ev);
		  }
		} /*if token instance of PosToken*/
	      } /*if term equals doc string */
	    } /* for doc in documentList */
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
   * @param docid document identifier
   * @param sentenceTokenList sentence to be examined.
   * @param semTypeRestrictSet semantic type 
   * @param sourceRestrictSet source list to restrict to
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentenceTokenList(String docid, List<ERToken> sentenceTokenList,
					      Set<String> semTypeRestrictSet,
					      Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException {
    // logger.debug("sentence tokenlist: " + sentenceTokenList);
    Set<Entity> entitySet = new HashSet<Entity>();
    int i = 0;
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
      // logger.debug("i: " + i);
    } /*while*/
    return entitySet;
  }

  // static methods
  public static Set<Entity> removeSubsumingEntities(Set<Entity> entitySet) {
    Map <Integer,Entity> startMap = new HashMap<Integer,Entity>();
    logger.debug("-input entity set spans-");
    for (Entity entity: entitySet) {
      // logger.debug(entity.getStart() + "," + entity.getLength());
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
      // logger.debug(entity.getStart() + "," + entity.getLength());
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
      // logger.debug(entity.getStart() + "," + entity.getLength() + ":" + entity);
      newEntitySet.add(entity);
    }
    return newEntitySet;
  }

  static EntityStartComparator entityComparator = new EntityStartComparator();

  /**
   * Apply Context negation and temporality matching to sentence using entity set.
   * @param entitySet entitySet associated with sentence 
   * @param sentence sentence to apply Context annotations to.
   * @throws Exception general exception
   */
  public static void applyContext(Set<Entity> entitySet, BioCSentence sentence) 
    throws Exception {
    for (List<String> result: ContextWrapper.applyContextUsingEntities(entitySet, sentence.getText())) {
      logger.debug("result: " + result);
    }
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


  /** Process text string */
  public List<Entity> processText(String docid,
				  String fieldid,
				  String text,
				  boolean detectNegationsFlag,
				  Set<String> semTypeRestrictSet,
				  Set<String> sourceRestrictSet) {
    return null;
  }

    /** Process text string */
  public List<Entity> processText(String text,
				  boolean useNegationDetection,
				  Set<String> semTypeRestrictSet,
				  Set<String> sourceRestrictSet) {
    return processText("000000", "text", text, useNegationDetection,
		       semTypeRestrictSet, sourceRestrictSet);
  }


  /** Process passage
   *
   * @param docid document id
   * @param passage passage of document to be processed
   * @param useContext if true the use ContexT negation detection 
   * @param semTypeRestrictSet set of semantic types to restrict concepts to
   * @param sourceRestrictSet set of sources to restrict concepts to
   * @return List of entity instances
   */
  public List<Entity> processPassage(String docid, BioCPassage passage, boolean useContext,
				     Set<String> semTypeRestrictSet,
				     Set<String> sourceRestrictSet) 
  {
    try {
      // logger.debug("enter processPassage");
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
	sentenceAnnotator.addPartOfSpeech(tokenList);
	Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, tokenList,
								      semTypeRestrictSet,
								      sourceRestrictSet);
	for (Entity entity: sentenceEntitySet) {
	  entity.setLocationPosition(i);
	}
	entitySet0.addAll(sentenceEntitySet);
	// look for negation and other relations using Context.
	if (useContext) {
	  applyContext(sentenceEntitySet, sentence);
	}
	i++;
      }
      // logger.debug("exit processPassage");
      Set<Entity> entitySet1 = removeSubsumingEntities(entitySet0);
      Set<Entity> entitySet = new HashSet<Entity>();
      for (Entity entity: entitySet1) {
	ConceptInfoUtils.filterEntityEvListBySemanticType(entity, semTypeRestrictSet);
	ConceptInfoUtils.filterEntityEvListBySource(entity, sourceRestrictSet);
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
					      boolean useContext,
					      Set<String> semTypeRestrictSet,
					      Set<String> sourceRestrictSet)
    throws IOException, FileNotFoundException, Exception
  {
    Set<Entity> entitySet0 = new HashSet<Entity>();
    int i = 0;
    for (Sentence sentence: sentenceList) {
      List<ERToken> tokenList = Scanner.analyzeText(sentence);
      sentenceAnnotator.addPartOfSpeech(tokenList);
      Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, tokenList,
								    semTypeRestrictSet,
								    sourceRestrictSet);
      for (Entity entity: sentenceEntitySet) {
	entity.setLocationPosition(i);
      }
      entitySet0.addAll(sentenceEntitySet);
      // fix this for ConText
    // look for negation and other relations using Context.
      // if (useContext) {
	// applyContext(sentenceEntitySet, sentence);
      // }
      i++;
    }
    // logger.debug("exit processPassage");
    Set<Entity> entitySet = removeSubsumingEntities(entitySet0);
    List<Entity> resultList = new ArrayList<Entity>(entitySet);
    Collections.sort(resultList, entityComparator);
    return resultList;
  }

  public Set<BioCAnnotation> generateBioCEntitySet(String docid,
						   List<ERToken> sentenceTokenList)
  {
    try {
      // logger.debug("generateEntitySet: ");
      Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
      Set<Entity> entitySet = 
	removeSubsumingEntities
	(this.processSentenceTokenList(docid, sentenceTokenList,
				       new HashSet<String>(),
				       new HashSet<String>()));
      for (Entity entity: entitySet) {
	bioCEntityList.add((BioCAnnotation)new BioCEntity(entity));
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
      newPassage.addAnnotation(annotation);
    }
    return newPassage;
  }
}

