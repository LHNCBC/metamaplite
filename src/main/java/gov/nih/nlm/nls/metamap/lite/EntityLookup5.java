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
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;

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
import gov.nih.nlm.nls.metamap.lite.dictionary.VariantLookup;
import gov.nih.nlm.nls.metamap.lite.dictionary.AugmentedDictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opennlp.tools.dictionary.serializer.Entry;

import gov.nih.nlm.nls.metamap.evaluation.Scoring;

/**
 * EntityLookup5 - Entity lookup of Chunked Phrases
 */

public class EntityLookup5 implements EntityLookup {
    private static final Logger logger = LoggerFactory.getLogger(EntityLookup5.class);

    // How long of a token can we consider for an entity map?
    public static final int DEFAULT_MIN_ORIG_TERM_LENGTH = 3; // Original EntityLookup5 hard-coded ≥3
    public final int MIN_ORIG_TERM_LENGTH;

    ChunkerMethod chunkerMethod;
  
  public MMLDictionaryLookup<TermInfo> dictionaryLookup;
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup4.maxtokensize","15"));
  SpecialTerms excludedTerms = new SpecialTerms();
  SentenceAnnotator sentenceAnnotator;
  SentenceExtractor sentenceExtractor;
  NegationDetector negationDetector;
  boolean addPartOfSpeechTagsFlag =
    Boolean.parseBoolean(System.getProperty("metamaplite.enable.postagging","true"));
  boolean disableChunker = 
    Boolean.parseBoolean(System.getProperty("metamaplite.disable.chunker","false"));
  
  /** Part of speech tags used for term lookup, can be set using
   * property: metamaplite.postaglist; the tag list is a set of Penn
   * Treebank part of speech tags separated by commas. */
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();

    /**
     * Normally, only runs of tokens whose first token's first character is alphanumeric
     * (a-zA-Z0-9 plus greek letters) are considered for matching; however, we want to
     * enable
     */
  boolean considerNonAlphaTokens;
  Set<Character> additionalTokenStartChars = new HashSet<Character>();

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

  /** Phrase types that can be used for lookup */
  Set<String> allowedPhraseTypeSet = new HashSet<String>();
  public void defaultAllowedPhraseTypes() {
    this.allowedPhraseTypeSet.add("NP"); // just noun phrases for now.
  }

  // In cases where there are subsumed (entirely overlapped) entities, do we remove them?
  // For example, "blood sugar level" and "blood sugar"
  // Most of the time, the answer is "yes"; in some high-recall scenarios, we don't want to.
  boolean shouldRemoveSubsumedEntities = true;

  public EntityLookup5(Properties properties) 
    throws IOException, FileNotFoundException
  {
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

    this.disableChunker = 
      Boolean.parseBoolean(properties.getProperty("metamaplite.disable.chunker","false"));
    if (this.addPartOfSpeechTagsFlag) {
      this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
      String allowedPartOfSpeechTaglist = properties.getProperty("metamaplite.postaglist");
      if (allowedPartOfSpeechTaglist != null) {
	for (String pos: allowedPartOfSpeechTaglist.split(",")) {
	  this.allowedPartOfSpeechSet.add(pos);
	}
      } else {
	this.defaultAllowedPartOfSpeech();
      }
    } else {
      this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
    }

    this.considerNonAlphaTokens  = Boolean.parseBoolean(properties.getProperty("metamaplite.entitylookup5.considerNonAlphaTokens", "false"));
    if (this.considerNonAlphaTokens) { // no need to bother with this if we're not allowing non-alpha tokens
        String bonusChars = properties.getProperty("metamaplite.entitylookup5.additionalAllowedFirstChars", "");
        for (char c : bonusChars.toCharArray()) {
            this.additionalTokenStartChars.add(c);
        }
    }

    String allowedPhraseTypeList = properties.getProperty("metamaplite.phrasetypelist");
    if (allowedPhraseTypeList != null) {
      for (String phraseType: allowedPhraseTypeList.split(",")) {
	this.allowedPhraseTypeSet.add(phraseType);
      }
    } else {
      this.defaultAllowedPhraseTypes();
    }

    this.chunkerMethod = new OpenNLPChunker(properties);

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
    MAX_TOKEN_SIZE =
      Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
					      Integer.toString(MAX_TOKEN_SIZE)));

    this.addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));

    if (this.addPartOfSpeechTagsFlag) {
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

    // remove subsumed?
    if (properties.containsKey("metamaplite.removeSubsumedEntities")) {
      this.shouldRemoveSubsumedEntities = Boolean.parseBoolean(properties.getProperty("metamaplite.removeSubsumedEntities"));
    }

    // Now check for custom minimum token lengths
    if (properties.containsKey("metamaplite.entitylookup5.min_token_length")) {
        // validate: must be > 0
        int tempTokenLength = Integer.parseInt(properties.getProperty("metamaplite.entitylookup5.min_token_length"));
        if (tempTokenLength >= 1) {
            this.MIN_ORIG_TERM_LENGTH = Integer.parseInt(properties.getProperty("metamaplite.entitylookup5.min_token_length"));
        } else {
            this.MIN_ORIG_TERM_LENGTH = DEFAULT_MIN_ORIG_TERM_LENGTH;
        }
    } else {
        this.MIN_ORIG_TERM_LENGTH = DEFAULT_MIN_ORIG_TERM_LENGTH;
    }
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
   * <pre>
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
   * @param fieldid id of field in document
   * @param sentenceNumber number of sentence in field
   * @param tokenList tokenlist of document
   * @param phraseTokenList list of tokens in phrase
   * @param phraseType   type of phrase (noun, verb, prep, etc.) 
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
       	  (this.allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech()) ||
	   this.allowedPhraseTypeSet.contains(phraseType))) {
       	int termLength = (tokenSubList.size() > 1) ?
       	  (lastToken.getOffset() + lastToken.getText().length()) - firstToken.getOffset() : 
       	  firstToken.getText().length();
       	String originalTerm = StringUtils.join(tokenTextSubList, "");
	if ((originalTerm.length() >= MIN_ORIG_TERM_LENGTH) &&
	    (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	 
	  // String term = originalTerm;
	  // String query = term;
	  normTerm = NormalizedStringCache.normalizeString(originalTerm);
	  int offset = ((PosToken)tokenSubList.get(0)).getOffset();

      // Should we see if we can find a match for tokenSubList?
      boolean shouldConsiderMatch = false;

      char firstChar = originalTerm.charAt(0);
      if (CharUtils.isAlphaNumeric(firstChar)) { // fast path, most likely
          // If we're dealing with an alphanumeric start char, we always allow
          shouldConsiderMatch = true;
      } else if (this.considerNonAlphaTokens && this.additionalTokenStartChars.contains(firstChar)) {
          // Depending on settings and what that first char is, maybe allow this token
          shouldConsiderMatch = true;
      }
	  if (shouldConsiderMatch) {
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
			       scoreTerm(originalTerm, concept.getConceptString(),
					 ((PosToken)tokenSubList.get(0)).getOffset(),
					 phraseTokenList, phraseType),
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
   * @return copy of subTokenList with missing whitespace added. 
   */
  public static List<ERToken> mapToTokenList(List<ERToken> originalTokenList, List<ERToken> subTokenList) {
    List<ERToken> newTokenlist = new ArrayList<ERToken>(subTokenList);
    try {
      ERToken firstToken = newTokenlist.get(0);
      ERToken lastToken = newTokenlist.get(newTokenlist.size() - 1);
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
	logger.error(iae.toString());
      throw new RuntimeException("originaltokenlist: " + originalTokenList +
				 ", subtokenlist: " + newTokenlist, iae);
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
   * @return true if head of phrase in matched token list.
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
    logger.debug("scoreTerm");
    logger.debug("phraseTokenList: " + phraseTokenList);
    logger.debug("phraseType: " + phraseType);

    List<ERToken> matchTokenList = Scanner.analyzeTextNoWS(matchedText);
    logger.debug("matchTokenList: " + matchTokenList);
    int headPos = findHeadPos(phraseTokenList, phraseType);
    double sum = 0;
    double centrality =
      isHeadInMatchedTokenList(phraseTokenList, matchTokenList, headPos, matchedTermOffset) ? 1.0 : 0.0;
    // double variation = 4.0/(variantLookup.lookupVariant(matchedText, metaTerm) + 4.0 );
    double variation = Scoring.computeVariation(matchedText, metaTerm,
						matchTokenList, 
						(VariantLookup)this.dictionaryLookup);
    // coverage steps:
    //  1. extract components
    // extractComponents();
    //  2. compute lower and upper bounds of phrase
    // computeBounds(phraseComponents);
    int phraseLowerBound = 0;
    logger.debug("phraseLowerBound: " + phraseLowerBound);
    int phraseUpperBound = phraseTokenList.size();
    logger.debug("phraseUpperBound: " + phraseUpperBound);
    int nTokenPhraseWords = phraseTokenList.size();
    //  3. compute phrase span
    double phraseSpan = (double)(phraseUpperBound - phraseLowerBound);
    //  4. compute lower and upper bounds of metathesaurus string
    // computeBounds(metaComponents);
    List<ERToken> metaTokenList = Scanner.analyzeTextNoWS(metaTerm);
    int metaLowerBound = phraseTokenList.indexOf(metaTokenList.get(0));
    logger.debug("metaLowerBound: " + metaLowerBound);
    int metaUpperBound = metaLowerBound + metaTokenList.size();
    logger.debug("metaUpperBound: " + metaUpperBound);
    int nMetaWords = metaTokenList.size();
    
    //  5. metathesaurus string span
    double metaSpan = (double)(metaUpperBound - metaLowerBound);
    logger.debug("variation: " + variation);
    logger.debug("centrality: " + centrality);	
    logger.debug("phraseSpan: " + phraseSpan);
    logger.debug("nTokenPhraseWords: " + nTokenPhraseWords);
    logger.debug("metaSpan: " + metaSpan);
    logger.debug("nTokenMetaWords: " + nMetaWords);
    double coverage = ((phraseSpan / nTokenPhraseWords) + (2 * (metaSpan / nMetaWords)))/3.0;
    logger.debug("coverage: " + coverage);
    double cohesiveness = (((phraseSpan*phraseSpan) / (nTokenPhraseWords*nTokenPhraseWords) +
			    (2 * (metaSpan*metaSpan) / (nMetaWords*nMetaWords))))/3.0;
    logger.debug("cohesiveness: " + cohesiveness);
    double score = 1000*((centrality + variation + (2.0*(coverage + cohesiveness)))/6.0);
    logger.debug("score: " + score);
    return score;
  }
  
  public void scoreEntity(Entity entity, List<ERToken> phraseTokenList, String phraseType)
  {
    logger.debug("scoreEntity");
    logger.debug("phraseTokenList: " + phraseTokenList);
    logger.debug("phraseType: " + phraseType);

    if (entity.getEvList().size() > 0) {
      // List<ERToken> matchTokenList = Scanner.analyzeTextNoWS(entity.getMatchedText());
      // logger.debug("matchTokenList: " + matchTokenList);
      // int headPos = findHeadPos(phraseTokenList, phraseType);
      double sum = 0;
      for (Ev ev: entity.getEvList()) {
	List<ERToken> metaTokenList = Scanner.analyzeTextNoWS(ev.getConceptInfo().getConceptString());
	double score = scoreTerm(entity.getMatchedText(), ev.getConceptString(),
				 phraseTokenList.indexOf(metaTokenList.get(0)),
				 phraseTokenList, phraseType);
	ev.setScore(score);
	sum = score + sum;
      }
      entity.setScore( sum / entity.getEvList().size());
    }
  }

  class PhraseImpl implements Phrase {
    List<ERToken> tokenlist;
    String tag;
    PhraseImpl(List<ERToken> tokenlist, String tag) {
      this.tokenlist = tokenlist;
      this.tag = tag;
    }
    public List<ERToken> getPhrase() { return this.tokenlist; }
    public String getTag() { return this.tag; }
    // public String toString() { 
    //   return this.tokenlist.stream().map(i -> i.toString()).collect(Collectors.joining(", ")) + "/" + this.tag;
    // }
  }

  /**
   * Glom Noun Phrase + Prepositional Phrase into a single composite
   * phrase.
   *
   * @param phraseList list of phrases to be modified.
   * @return modified list of phrases
   */
  public List<Phrase> glomNounPhrasePrepPhrase(List<Phrase> phraseList) {
    if (phraseList.size() > 0) {
      List<Phrase> newPhraseList = new ArrayList<Phrase>();
      Phrase first = phraseList.get(0);
      for (Phrase phrase: phraseList.subList(1, phraseList.size())) {
	
      }
    }
    return phraseList;
  }

  /**
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence. 
   *
   * What actually happens is this:
   * <ul>
   *   <li> Query the cui &lt;--&gt; sourceinfo index using the prefix of the term.</li>
   *   <li>Given the hitlist from the query, keep any matches that are
   *      a subset of the token list that has the prefix at the head of
   *      the tokenlist.</li>
   * </ul>
   *
   *  Organization of cui &lt;--&gt; sourceinfo table: <code>cui|sui|seqno|str|src|tty</code>
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
   * @param docid document id
   * @param fieldid field id
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
    logger.debug("minimalSentenceTokenList: " + minimalSentenceTokenList);
    if (this.addPartOfSpeechTagsFlag) {
      sentenceAnnotator.addPartOfSpeech(minimalSentenceTokenList);
    }
    List<Phrase> phraseList;
    if (this.disableChunker) {
      phraseList = new ArrayList<Phrase>();
      phraseList.add(new PhraseImpl(minimalSentenceTokenList, "NP")); // not really a noun phrase
    } else {
    // chunk first, then find entities in the chunks
      phraseList = this.chunkerMethod.applyChunker(minimalSentenceTokenList);
    }
    List<Phrase> newPhraseList = glomNounPhrasePrepPhrase(phraseList);
    for (Phrase phrase: newPhraseList) {
      logger.debug("phrase: " + phrase);
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
	   phrase.getPhrase(),
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
      List<Sentence> sentenceList = this.sentenceExtractor.createSentenceList(text);
      for (Sentence sentence: sentenceList) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
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

	// mark abbreviations that are entities and add them to sentence entity set.
	Set<Entity> abbrevEntitySet =
	  new HashSet(MarkAbbreviations.markAbbreviations
		      (text, this.uaMap,
		       new ArrayList(entitySet0)));
	entitySet0.addAll(abbrevEntitySet);
	if (detectNegationsFlag) {
	  detectNegations(entitySet0, sentence.getText(), tokenList);
	}
      }

      Set<Entity> entitySet1;
      if (this.shouldRemoveSubsumedEntities) {
          // remove any entities subsumed by another entity
          entitySet1 = removeSubsumedEntities(entitySet0);
      } else {
          entitySet1 = entitySet0;
      }
      // filter entities by semantic type and source sets.
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
    if (fieldid == null) {
      fieldid = "text";
    }
    try {
      Set<Entity> entitySet0 = new HashSet<Entity>();
      int i = 0;
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);
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
      for (BioCSentence sentence: passage.getSentences()) {
	List<ERToken> tokenList = Scanner.analyzeText(sentence);

	// mark abbreviations that are entities and add them to sentence entity set.
	Set<Entity> abbrevEntitySet =
	  new HashSet(MarkAbbreviations.markAbbreviations
		      (passage, this.uaMap,
		       new ArrayList(entitySet0)));
	entitySet0.addAll(abbrevEntitySet);
	if (detectNegationsFlag) {
	  detectNegations(entitySet0, sentence.getText(), tokenList);
	}
      }
      
      // remove any entities subsumed by another entity
      Set<Entity> entitySet1;
      if (this.shouldRemoveSubsumedEntities) {
          entitySet1 = removeSubsumedEntities(entitySet0);
      } else {
          entitySet1 = entitySet0;
      }
      // filter entities by semantic type and source sets.
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

