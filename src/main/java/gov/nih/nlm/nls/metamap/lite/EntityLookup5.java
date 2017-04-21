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
import gov.nih.nlm.nls.metamap.lite.ChunkerMethod;
import gov.nih.nlm.nls.metamap.lite.OpenNLPChunker;

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

import info.debatty.java.stringsimilarity.SorensenDice;
import gov.nih.nlm.nls.metamap.evaluation.Scoring;

/**
 *
 */

public class EntityLookup5 implements EntityLookup {
  private static final Logger logger = LogManager.getLogger(EntityLookup4.class);

  public MetaMapIvfIndexes mmIndexes;
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  ChunkerMethod chunkerMethod = new OpenNLPChunker();
  
  /** string column for cuisourceinfo index*/
  int strColumn = 3;		
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		
  SpecialTerms excludedTerms = new SpecialTerms();
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup4.maxtokensize","15"));

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

  SentenceAnnotator sentenceAnnotator;
  NegationDetector negationDetector;
  boolean addPartOfSpeechTagsFlag =
    Boolean.parseBoolean(System.getProperty("metamaplite.enable.postagging","true"));

  /** Sorensen Dice similarity measure. */
  SorensenDice sd = new SorensenDice();

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

  public EntityLookup5(Properties properties) 
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes(properties);
    
    this.addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));

    if (this.addPartOfSpeechTagsFlag) {
      this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
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
    public SpanEntityMapAndTokenLength(Map<String,Entity> spanEntityMap, int length) {
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
    public int size() { return this.spanEntityMap.size(); }
  }

  public class SpanInfo {
    int start;
    int length;
    int getStart()  { return this.start; }
    int getLength() { return this.length; }
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

  boolean isCuiInSemanticTypeRestrictSet(String cui, Set<String> semanticTypeRestrictSet)
  {
    if (semanticTypeRestrictSet.isEmpty() || semanticTypeRestrictSet.contains("all"))
      return true;
    try {
      boolean inSet = false;
      for (String semtype: cuiSemanticTypeSetIndex.getSemanticTypeSet(cui)) {
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
      for (String semtype: cuiSourceSetIndex.getSourceSet(cui)) {
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
   * @param fieldid id of field in document
   * @param sentenceNumber number of sentence in field
   * @param tokenList tokenlist of document
   * @return Span to entity map + token length map instance
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public SpanEntityMapAndTokenLength findLongestMatch(String docid, 
						      String fieldid,
						      int sentenceNumber,
						      List<ERToken> tokenList,
						      List<ERToken> phraseTokenList,
						      String phraseType)
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

	    for (ConceptInfo concept: this.termConceptInfoCache.lookupTermConceptInfo(originalTerm,
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
			     scoreTerm(originalTerm, concept.getConceptString(),
				       ((PosToken)tokenSubList.get(0)).getOffset(),
				       phraseTokenList, phraseType),
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

  public void filterEntityEvListBySemanticType(Entity entity, Set<String> semanticTypeRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      if (this.isCuiInSemanticTypeRestrictSet(cui, semanticTypeRestrictSet)) {
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
      if (this.isCuiInSourceRestrictSet(cui, sourceRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }

  /**
   * Find first occurrance of token in tokenlist with specified part
   * of speech.
   * @param tokenlist tokenlist to search for part of speech
   * @param postag part of speech to search for.
   * @return position of head in tokenlist, -1 if token not found.
   */
  public static int indexOfPartOfSpeech(List<ERToken> tokenlist, String postag) {
    int i = 0;
    for (ERToken token: tokenlist) {
      if (token.getPartOfSpeech().length() >= 2) {
	if (token.getPartOfSpeech().substring(0,2).equals(postag)) {
	  return i;
	}
      }
      i++;
    }
    return -1;
  }

  /** 
   * Add missing whitespace tokens to subtokenlist.
   * @param originalTokenList original tokenlist with whitespace.
   * @param subTokenList subrange of tokenlist with whitespace removed.
   * @return subTokenList with missing whitespace added. 
   */
  public static List<ERToken> mapToTokenList(List<ERToken> originalTokenList, List<ERToken> subTokenList) {
    try {
      ERToken firstToken = subTokenList.get(0);
      ERToken lastToken = subTokenList.get(subTokenList.size() - 1);
      int firstIndex = originalTokenList.indexOf(firstToken);
      int lastIndex = originalTokenList.indexOf(lastToken);
      logger.debug("firstToken: " + firstToken);
      logger.debug("lastToken: " + lastToken);
      logger.debug("firstIndex: " + firstIndex);
      logger.debug("lastIndex: " + lastIndex);
      if (lastIndex < firstIndex) {
	lastIndex = originalTokenList.lastIndexOf(lastToken);
      }
      if (lastIndex >= firstIndex) {
	logger.debug("returning sublist: " + originalTokenList.subList(firstIndex, lastIndex));
	return originalTokenList.subList(firstIndex, lastIndex + 1);
      } else {
	logger.debug("returning originalTokenlist: " + originalTokenList);
	return originalTokenList;
      }
    } catch (IllegalArgumentException iae) {
      logger.error(iae);
      throw new RuntimeException("originaltokenlist: " + originalTokenList +
				 ", subtokenlist: " + subTokenList, iae);
    }
  }

  /**
   * Return index of token in tokenlist with the same token text.
   * @param tokenList list of tokens to searched
   * @param targetToken query token
   * @return index of token with the same token text or -1 if not present.
   */
  public int indexOfTokenText(List<ERToken> tokenList, ERToken targetToken) {
    int i = 0;
    for (Token token: tokenList) {
      if (token.getText() == targetToken.getText()) {
	return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Is head in match token list of entity?
   *
   * @param phraseTokenList phrase tokenlist 
   * @param matchTokenList entity matched text tokenlist
   * @param headPos position of head of phrase
   * @param targetOffset offset of target entity.
   */
  public boolean isHeadInMatchedTokenList(List<ERToken> phraseTokenList, List<ERToken> matchTokenList, int headPos, int targetOffset) {
    if (phraseTokenList.get(headPos).getOffset() == targetOffset) {
      return true;
    } else {
      // find token in matchTokenList
      ERToken headToken = phraseTokenList.get(headPos);
      // if token is in matchTokenlist
      int matchTokenIndex = indexOfTokenText(matchTokenList, headToken);
      if (matchTokenIndex >= 0) {
	return true;
      }
    } 
    return false;
  }

  public int findHeadPos(List<ERToken> phraseTokenList, String phraseType) {
    // Does entity start at head of phrase?
      int headPos = 0;
      if (phraseType.equals("NP")) {
	headPos = indexOfPartOfSpeech(phraseTokenList, "NN");
      } else if (phraseType.equals("VP")) {
	headPos = indexOfPartOfSpeech(phraseTokenList, "VB"); // should this look for a verb?
      } else if (phraseType.equals("PP")) {
	headPos = indexOfPartOfSpeech(phraseTokenList, "NN"); // head should be first noun?
      }
      else if (phraseType.equals("ADVP")) {
	headPos = indexOfPartOfSpeech(phraseTokenList, "NN");
      } else if (phraseType.equals("ADJP")) {
	headPos = indexOfPartOfSpeech(phraseTokenList, "NN");
      } 
      logger.debug("headpos: " + headPos);
      if ((headPos >= 0) && (phraseTokenList.size() > headPos)) {
	logger.debug("headpos offset: " + phraseTokenList.get(headPos).getOffset());
      }
      if (headPos < 0) {
	logger.warn("headPos = " + headPos + ", phase type = " + phraseType + ", setting headPos to zero.");
	headPos = 0;
      }
      return headPos;
  }

  public double scoreTerm(String matchedText, String metaTerm,
			  int matchedTermOffset,
			  List<ERToken> phraseTokenList, String phraseType) {
    logger.debug("phraseTokenList: " + phraseTokenList);
    logger.debug("phraseType: " + phraseType);

    List<ERToken> matchTokenList = Scanner.analyzeTextNoWS(matchedText);
    logger.debug("matchTokenList: " + matchTokenList);
    int headPos = findHeadPos(phraseTokenList, phraseType);
    double sum = 0;
    double centrality =
      isHeadInMatchedTokenList(phraseTokenList, matchTokenList, headPos, matchedTermOffset) ? 1.0 : 0.0;
    int variation = variantLookup.lookupVariant(matchedText, metaTerm);
    // coverage steps:
    //  1. extract components
    // extractComponents();
    //  2. compute lower and upper bounds of phrase
    // computeBounds(phraseComponents);
    int phraseLowerBound = 0;
    int phraseUpperBound = phraseTokenList.size();
    int nTokenPhraseWords = phraseTokenList.size();
    //  3. compute phrase span
    double phraseSpan = (double)(phraseUpperBound - phraseLowerBound + 1.0);
    //  4. compute lower and upper bounds of metathesaurus string
    // computeBounds(metaComponents);
    List<ERToken> metaTokenList = Scanner.analyzeText(metaTerm);
    int metaLowerBound = phraseTokenList.indexOf(metaTokenList.get(0));
    int metaUpperBound = metaLowerBound + metaTokenList.size();
    int nMetaWords = metaTokenList.size();
    
    //  5. metathesaurus string span
    double metaSpan = (double)(metaUpperBound - metaLowerBound + 1.0);
    logger.debug("variation: " + variation);
    logger.debug("centrality: " + centrality);	
    logger.debug("phraseSpan: " + phraseSpan);
    logger.debug("nTokenPhraseWords: " + nTokenPhraseWords);
    logger.debug("metaSpan: " + metaSpan);
    logger.debug("nTokenMetaWords: " + nMetaWords);
    double coverage = ((phraseSpan / nTokenPhraseWords) + (2 * (metaSpan / nMetaWords)))/3.0;
    double cohesiveness = (((phraseSpan*phraseSpan) / (nTokenPhraseWords*nTokenPhraseWords) +
			    (2 * (metaSpan*metaSpan) / (nMetaWords*nMetaWords))))/3.0;
    logger.debug("cohesiveness: " + cohesiveness);
    double score = -1000*((centrality + variation + (2.0*coverage) + (2.0*cohesiveness))/6.0);
    logger.debug("score: " + score);
    return score;
  }
  
  public void scoreEntity(Entity entity, List<ERToken> phraseTokenList, String phraseType)
  {
    logger.debug("phraseTokenList: " + phraseTokenList);
    logger.debug("phraseType: " + phraseType);

    if (entity.getEvList().size() > 0) {
      List<ERToken> matchTokenList = Scanner.analyzeTextNoWS(entity.getMatchedText());
      logger.debug("matchTokenList: " + matchTokenList);
      int headPos = findHeadPos(phraseTokenList, phraseType);
      double sum = 0;
      for (Ev ev: entity.getEvList()) {
	// centrality: does entity involve the head of phrase?
	logger.debug("ev offset: " + ev.getOffset());	    
	logger.debug("phrase head offset: " +  phraseTokenList.get(headPos).getOffset());
	double centrality =
	  isHeadInMatchedTokenList(phraseTokenList, matchTokenList, headPos, ev.getOffset()) ? 1.0 : 0.0;
	int variation =
	  variantLookup.lookupVariant(entity.getMatchedText(),
				      entity.getEvList().get(0).getConceptString());
	// coverage steps:
	//  1. extract components
	// extractComponents();
	//  2. compute lower and upper bounds of phrase
	// computeBounds(phraseComponents);
	int phraseLowerBound = 0;
	int phraseUpperBound = phraseTokenList.size();
	int nTokenPhraseWords = phraseTokenList.size();
	//  3. compute phrase span
	double phraseSpan = (double)(phraseUpperBound - phraseLowerBound + 1.0);
	//  4. compute lower and upper bounds of metathesaurus string
	// computeBounds(metaComponents);
	List<ERToken> metaTokenList = Scanner.analyzeText(ev.getConceptInfo().getConceptString());
	int metaLowerBound = phraseTokenList.indexOf(metaTokenList.get(0));
	int metaUpperBound = metaLowerBound + metaTokenList.size();
	int nMetaWords = metaTokenList.size();

	//  5. metathesaurus string span
	double metaSpan = (double)(metaUpperBound - metaLowerBound + 1.0);
	logger.debug("variation: " + variation);
	logger.debug("centrality: " + centrality);	
	logger.debug("phraseSpan: " + phraseSpan);
	logger.debug("nTokenPhraseWords: " + nTokenPhraseWords);
	logger.debug("metaSpan: " + metaSpan);
	logger.debug("nTokenMetaWords: " + nMetaWords);
	double coverage = ((phraseSpan / nTokenPhraseWords) + (2 * (metaSpan / nMetaWords)))/3.0;
	
	double cohesiveness = (((phraseSpan*phraseSpan) / (nTokenPhraseWords*nTokenPhraseWords) +
				(2 * (metaSpan*metaSpan) / (nMetaWords*nMetaWords))))/3.0;
	logger.debug("cohesiveness: " + cohesiveness);
	double score = -1000*((centrality + variation + (2.0*coverage) + (2.0*cohesiveness))/6.0);
	logger.debug("score: " + score);
	ev.setScore(score);
	sum = score + sum;
      }
      entity.setScore( sum / entity.getEvList().size());
    }
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
   * @param docid document id
   * @param sentenceTokenList sentence to be examined.
   * @param semTypeRestrictSet semantic type 
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
    List<ERToken> minimalSentenceTokenList = new ArrayList<ERToken>();
    for (ERToken token: sentenceTokenList) {
      if (! token.getTokenClass().equals("ws")) { // only keep non-ws tokens
	minimalSentenceTokenList.add(token);
      }
    }
    if (this.addPartOfSpeechTagsFlag) {
      sentenceAnnotator.addPartOfSpeech(minimalSentenceTokenList);
    }
    // chunk first, then find entities in the chunks
    List<Phrase> phraseList = this.chunkerMethod.applyChunker(minimalSentenceTokenList);
    for (Phrase phrase: phraseList) {
      logger.debug("phrase: " + phrase);
      logger.info("phrase: " + phrase);
      List<ERToken> phraseTokenList = mapToTokenList(sentenceTokenList, phrase.getPhrase());
      logger.debug("phraseTokenList: " + phraseTokenList);
      int i = 0;
      while (i<phraseTokenList.size()) {
	SpanEntityMapAndTokenLength spanEntityMapAndTokenLength = 
	  this.findLongestMatch
	  (docid,
	   fieldid,
	   i,
	   phraseTokenList.subList(i,Math.min(i+MAX_TOKEN_SIZE,phraseTokenList.size())),
	   phraseTokenList,
	   phrase.getTag());

	if (spanEntityMapAndTokenLength.size() > 0) {
	  for (Entity entity: spanEntityMapAndTokenLength.getEntityList()) {
	    scoreEntity(entity, phrase.getPhrase(), phrase.getTag());
	    entitySet.add(entity);
	  }
	}
	i++;
      } /*while*/
    }
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

  /** Process passage */
  public List<Entity> processPassage(String docid, BioCPassage passage,
				     boolean detectNegationsFlag,
				     Set<String> semTypeRestrictSet,
				     Set<String> sourceRestrictSet) 
  {
    String fieldid = passage.getInfon("section");
    if (fieldid == null) {
      fieldid = "text";
    }
    try {
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
	Set<Entity> sentenceEntitySet = this.processSentenceTokenList(docid, fieldid, tokenList,
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
    String fieldid = "text";
    Set<Entity> entitySet0 = new HashSet<Entity>();
    int i = 0;
    for (Sentence sentence: sentenceList) {
      List<ERToken> tokenList = Scanner.analyzeText(sentence);
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
    Set<Entity> entitySet = removeSubsumingEntities(entitySet0);
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
      Set<Entity> entitySet = 
	removeSubsumingEntities
	(this.processSentenceTokenList(docid, fieldid, sentenceTokenList,
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

