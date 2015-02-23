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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.OutputStreamWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import bioc.BioCSentence;
import bioc.BioCAnnotation;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCLocation;

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIndexes;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.TokenListUtils;

import gov.nih.nlm.nls.types.Sentence;

import gov.nih.nlm.nls.utils.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.dictionary.serializer.Entry;

/**
 *
 */
public class EntityLookup {
  private static final Logger logger = LogManager.getLogger(EntityLookup.class);
  int resultLength = 
    Integer.parseInt(System.getProperty("metamaplite.entitylookup.resultlength","1500"));

  public MetaMapEvaluation metaMapEvalInst;
  public MetaMapIndexes mmIndexes;

  public EntityLookup() 
    throws IOException, FileNotFoundException, ParseException
  {
    this.mmIndexes = new MetaMapIndexes();
    this.metaMapEvalInst = new MetaMapEvaluation(this.mmIndexes);
  }

  public static EntityLookup singleton;
  static {
    try {
      singleton = new EntityLookup();
    } catch (IOException ioe) {
      ioe.printStackTrace(System.err);
    } catch (ParseException pe) {
      pe.printStackTrace(System.err);
    }
  }

  /** cache of string -> concept and attributes */
  public static Map<String,List<ConceptInfo>> termConceptCache = new HashMap<String,List<ConceptInfo>>();

  public void cacheConcept(String term, ConceptInfo concept) {
    synchronized (termConceptCache) {
      if (termConceptCache.containsKey(term)) {
	synchronized (termConceptCache.get(term)) {
	  termConceptCache.get(term).add(concept);
	}
      } else {
	List<ConceptInfo> newConceptList = new ArrayList<ConceptInfo>();
	newConceptList.add(concept);
	termConceptCache.put(term, newConceptList);
      }
    }
  }

  /** cache of string -> lucene document hit list */
  public static Map<String,List<Document>> termHitListCache = new HashMap<String,List<Document>>();

  public void cacheHitList(String term, List<Document> hitList) {
    synchronized (termHitListCache) {
      termHitListCache.put(term, hitList);
    }
  }

  /**
   * A memoization of lucene cuiSourceinfoindex lookup.
   * @param String containing query
   * @return list of lucene index documents
   */
  List<Document> cuiSourceInfoIndexLookup(String query)
    throws FileNotFoundException, IOException, ParseException
  {
    List<Document> hitList;
    if (termHitListCache.containsKey(query)) {
      logger.debug("Using hit List cache for query " + query);
      hitList = termHitListCache.get(query);
    } else {
      hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(query,
							 this.mmIndexes.strQueryParser,
							 resultLength);
      termHitListCache.put(query, hitList);
    }
    return hitList;
  }

  public String findPreferredName(String cui)
    throws FileNotFoundException, IOException, ParseException
 {
    List<Document> hitList = 
      this.mmIndexes.cuiConceptIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 1);
    if (hitList.size() > 0) {
      return hitList.get(0).get("concept");
    }
    return null;
  }

  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<String> sourceSet = new HashSet<String>();
    List<Document> hitList = 
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 20);
    for (Document hit: hitList) {
      sourceSet.add(hit.get("src"));
    }
    return sourceSet;
  }

  public Set<String> getSemanticTypeSet(String cui)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<String> semanticTypeSet = new HashSet<String>();
    List<Document> hitList = 
      this.mmIndexes.cuiSemanticTypeIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 20);
    for (Document hit: hitList) {
      semanticTypeSet.add(hit.get("semtype"));
    }
    return semanticTypeSet;
  }

  List<ERToken> removePunctuation(List<ERToken> tokenList) {
    List<ERToken> newTokenList = new ArrayList<ERToken>();
    for (ERToken token: newTokenList) {
      if ((token.getText().length() > 1) || (! CharUtils.isPunct(token.getText().charAt(0)))) {
	newTokenList.add(token);
      }
    }
    return newTokenList;
  }

  /**
   * Given the string:
   *   "cancer of the lung" -> "cancer, lung" -> "lung cancer"
   *
   * what it does:
   *  1. replace "of the" with comma (",")
   *  2. inversion
   *
   * TBD: should be updated for other relevant prepositions.
   *
   * @param inputtext input text
   * @return string with preposition "of the" removed and the term inverted.
   */
  public static String transformPreposition(String inputtext) {
    // logger.debug("entering: transformPreposition");
    if (inputtext.indexOf(" of the") > 0) {
      // return MWIUtilities.normalizeAstString(inputtext.replaceAll(" of the", ","));
      return NormalizedStringCache.normalizeAstString(inputtext.replaceAll(" of the", ","));
    } 
    // logger.debug("leaving: transformPreposition");
    return inputtext;
  }

  public class EntityListAndTokenLength {
    List<Entity> entityList;
    int length;
    public EntityListAndTokenLength(List<Entity> entityList, int length) {
      this.entityList = entityList;
      this.length = length;
    }
    public List<Entity> getEntityList() {
      return this.entityList;
    }
    public int getLength() {
      return this.length;
    }
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

  public void addEvListToSpanMap(Map<String,Entity> spanMap, List<Ev> evList, 
				 String docid, String matchedText, 
				 int offset, int length) {
    String span = offset + ":" + length;
    if (spanMap.containsKey(span)) {
      Entity entity = spanMap.get(span);
      Set<String> currentCuiSet = new HashSet<String>();
      for (Ev currentEv: entity.getEvList()) {
	currentCuiSet.add(currentEv.getConceptInfo().getCUI());
      }
      for (Ev newEv: evList) {
	if (! currentCuiSet.contains(newEv.getConceptInfo().getCUI())) {
	  entity.addEv(newEv);
	}
      }
    } else {
      Entity entity = new Entity(docid, matchedText, offset, length, 0.0, evList);
      spanMap.put(span, entity);
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
   */
  public SpanEntityMapAndTokenLength findLongestMatch(String docid, 
						   List<Document> documentList, 
						   List<? extends Token> tokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    logger.debug("findLongestMatch");

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
      int termLength = (tokenSubList.size() > 1) ?
	(lastToken.getPosition() + lastToken.getText().length()) - firstToken.getPosition() : 
	firstToken.getText().length();
      String originalTerm = StringUtils.join(tokenTextSubList, "");
      if ((originalTerm.length() > 2) &&
	  (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	String term = transformPreposition(originalTerm);
	String query = term;
	// String normTerm = MWIUtilities.normalizeAstString(term);
	String normTerm = NormalizedStringCache.normalizeAstString(term);
	int offset = ((PosToken)tokenSubList.get(0)).getPosition();
	if (CharUtils.isAlpha(term.charAt(0))) {
	  List<Ev> evList = new ArrayList<Ev>();

	  // List<Document> documentList = this.mmIndexes.cuiSourceInfoIndex.lookup(query,
	  // 									 this.mmIndexes.strQueryParser,
	  // 									 resultLength);
	  Integer tokenListLength = new Integer(tokenSubList.size());
	  if (EntityLookup.termConceptCache.containsKey(normTerm)) {
	    for (ConceptInfo concept: EntityLookup.termConceptCache.get(normTerm)) {
	      Ev ev = new Ev(concept,
			     originalTerm,
			     ((PosToken)tokenSubList.get(0)).getPosition(),
			     termLength,
			     0.0);
	      logger.debug("add ev: " + ev);
	      evList.add(ev);
	    }
	  } else {
	    for (Document doc: documentList) {
	      String cui = doc.get("cui");
	      String docStr = doc.get("str");
	      // logger.debug("term: \"" + term + 
	      // 	     "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
	      // 	     term.equalsIgnoreCase(docStr));
	      if (normTerm.equals(NormalizedStringCache.normalizeAstString(docStr))) {
		if (tokenSubList.get(0) instanceof PosToken) {
		  ConceptInfo concept = new ConceptInfo(cui, 
							this.findPreferredName(cui),
							this.getSourceSet(cui),
							this.getSemanticTypeSet(cui));
		  this.cacheConcept(normTerm, concept);
		  Ev ev = new Ev(concept,
			      originalTerm,
			      offset,
			      termLength,
			      0.0);
		  logger.debug("add ev: " + ev);
		  evList.add(ev);
		} /*if token instance of PosToken*/
	      } /*if term equals doc string */
	    } /* for doc in documentList */
	  } /* if term in concept cache */
	  this.addEvListToSpanMap(spanMap, evList, 
				  docid,
				  originalTerm,
				  offset, termLength);
	  longestMatchedTokenLength = Math.max(longestMatchedTokenLength,tokenTextSubList.size());

	} /* if term alphabetic */
      } /* if term length > 0 */
    } /* for token-sublist in list-of-token-sublists */
    return new SpanEntityMapAndTokenLength(spanMap, longestMatchedTokenLength);
  }

  void logHits(List<Document> hitList) {
    for (Document hit: hitList) {
      logger.debug(hit.get("cui") + "|" + hit.get("str") + "|" + hit.get("src"));
    }
  }

  /**
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence. 
   *
   * What actually happens is this:
   *
   *   1. Query the cui <--> sourceinfo index using the prefix of the term.
   *   2. Given the hitlist from the query, keep any matches that are
   *      a subset of the token list that has the prefix at the head of
   *      the tokenlist.
   *
   *
   *  Organization of cui <--> sourceinfo table: cui|sui|seqno|str|src|tty
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
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentenceTokenList(String docid, List<? extends Token> sentenceTokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    logger.debug("sentence tokenlist: " + sentenceTokenList);
    Set<Entity> entitySet = new HashSet<Entity>();
    int i = 0;
    while (i<sentenceTokenList.size()) {
      String prefix = sentenceTokenList.get(i).getText();
      if (prefix.trim().length() > 2) {
	logger.debug("processSentenceTokenList: prefix term: " + prefix);
	List<Document> hitList;
	try {
	  // this.mmIndexes.strQueryParser.setFuzzyPrefixLength(2);
	  // The added asterisk should make the lucene query parser
	  // create a prefix query.
	  // String query = prefix + "*";
	  String query = prefix;
	  logger.debug("lucene str query: " + query);
	  // if (termHitListCache.containsKey(query)) {
	  //   logger.debug("Using hit List cache for query " + query);
	  //   hitList = termHitListCache.get(query);
	  // } else {
	  //   hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(query,
	  // 						       this.mmIndexes.strQueryParser,
	  // 						       resultLength);
	  //   termHitListCache.put(query, hitList);
	  // }
	  hitList = cuiSourceInfoIndexLookup(query);
	  logger.debug("size of hitList: " + hitList.size());
	} catch (ParseException pe) {
	  System.err.println("errant term prefix: " + prefix);
	  System.err.println("tokenlist: " + Tokenize.getTextFromTokenList(sentenceTokenList));
	  hitList = new ArrayList<Document>(); // empty array list
	}
	if (hitList.size() > 0) {
	  logger.debug("processSentenceTokenList: hit size: " + hitList.size());
	  // if (logger.isDebugEnabled()) {
	  //   logHits(hitList);
	  // }
	  SpanEntityMapAndTokenLength spanEntityMapAndTokenLength = 
	    this.findLongestMatch
	    (docid,
	     hitList,
	     sentenceTokenList.subList(i,Math.min(i+30,sentenceTokenList.size())));
	  for (Entity entity: spanEntityMapAndTokenLength.getEntityList()) {
	    if (entity.getEvList().size() > 0) {
	      entitySet.add(entity);
	    }
	  }
	  // i = i + (spanEntityMapAndTokenLength.getLength() > 0 ? 
	  // 	   spanEntityMapAndTokenLength.getLength() : 1);
	  i++;
	  logger.debug("i: " + i);
	} else {
	  i++;
	}
      } else {
	i++;
      }
    } /*while*/
    return entitySet;
  }

  // static methods
  public static Set<Entity> generateEntitySet(List<? extends Token> sentenceTokenList)
    throws IOException, FileNotFoundException, ParseException
  {
    logger.debug("generateEntitySet: ");
    EntityLookup entityLookup = EntityLookup.singleton;
    return entityLookup.processSentenceTokenList("_____", sentenceTokenList);
  }

  public static Set<Entity> removeSubsumingEntities(Set<Entity> entitySet) {
    Map <Integer,Entity> startMap = new HashMap<Integer,Entity>();
    logger.debug("-input entity set spans-");
    for (Entity entity: entitySet) {
      logger.debug(entity.getStart() + "," + entity.getLength());
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
      logger.debug(entity.getStart() + "," + entity.getLength());
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
      logger.debug(entity.getStart() + "," + entity.getLength() + ":" + entity);
      newEntitySet.add(entity);
    }
    return newEntitySet;
  }


  public static Set<BioCAnnotation> generateBioCEntitySet(String docid,
							  List<? extends Token> sentenceTokenList)
    throws IOException, FileNotFoundException, ParseException
  {
    logger.debug("generateEntitySet: ");
    EntityLookup entityLookup = EntityLookup.singleton;
    Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
    Set<Entity> entitySet = 
      removeSubsumingEntities
      (entityLookup.processSentenceTokenList(docid, sentenceTokenList));
    for (Entity entity: entitySet) {
      bioCEntityList.add((BioCAnnotation)new BioCEntity(entity));
    }
    return bioCEntityList;
  }

  public static void displayEntitySet(Set<Entity> entitySet) {
    logger.debug("displayEntitySet");
    for (Entity entity: entitySet) {
      System.out.println(entity);
    }
  }

  public static BioCSentence displayEntitySet(BioCSentence sentence) {
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof BioCEntity) {
	System.out.print(((BioCEntity)annotation).getEntitySet().toString());
	for (Map.Entry<String,String> entry: annotation.getInfons().entrySet()) {
	  System.out.print(entry.getKey() + ":" + entry.getValue() + "|");
	}
	System.out.println();
      } else {
	System.out.println(annotation);
      }
    }
    return sentence;
  }

  public static void writeEntities(PrintWriter writer, BioCDocument document) {
    int rindex = 0;
    for (BioCPassage passage: document.getPassages()) {
      for (BioCSentence sentence: passage.getSentences()) {
	for (BioCAnnotation annotation: sentence.getAnnotations()) {
	  writer.println(annotation.getText());
	  for (BioCLocation location: annotation.getLocations()) {
	    writer.println(	"|" + location );
	  }
	  if (annotation instanceof BioCEntity) {
	    BioCEntity bioCEntity = (BioCEntity)annotation;
	    for (Entity entity: bioCEntity.getEntitySet()) {
	      System.out.print(entity.toString());
	      writer.print(entity.toString());
	      for (Map.Entry<String,String> entry: annotation.getInfons().entrySet()) {
		System.out.print(entry.getKey() + ":" + entry.getValue() + "|");
		writer.print(entry.getKey() + ":" + entry.getValue() + "|");
	      }
	      System.out.println();
	      writer.println();
	    }
	  } else {
	    System.out.println(annotation);
	    writer.println(annotation);
	  }
	}
      }
    }    
  }

  public static void writeEntities(PrintStream stream, BioCDocument document)
  {
    writeEntities(new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream))), document);
  }

  public static void writeEntities(String filename, BioCDocument document) 
    throws IOException
  {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    writeEntities(pw, document);
    pw.close();
  }

  public static void writeBcEvaluateAnnotations(PrintWriter writer, BioCSentence sentence)
    throws IOException
  {
    writeBcEvaluateAnnotations(writer, sentence);
  }

  public static void writeBcEvaluateAnnotations(PrintWriter writer, BioCDocument document) {
    Set<String> termSet = new HashSet<String>();
    for (BioCPassage passage: document.getPassages()) {
      for (BioCSentence sentence: passage.getSentences()) {
	for (BioCAnnotation annotation: sentence.getAnnotations()) {
	  termSet.add(annotation.getText());
	}
      }
    }
    int rindex = 1;
    for (String term: termSet) {
      System.out.println(document.getID() + "\t" +
			 term + "\t" +
			 rindex + "\t" +
			 0.9);
      writer.println(document.getID() + "\t" +
		     term + "\t");
		     // rindex + "\t" +
		     // 0.9);
      rindex++;
    }
  }

  public static void writeBcEvaluateAnnotations(PrintStream stream, BioCDocument document) 
    throws IOException
  {
    writeBcEvaluateAnnotations(new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream))), document);
  }

  public static void writeBcEvaluateAnnotations(String filename, BioCDocument document) 
    throws IOException
  {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    writeBcEvaluateAnnotations(pw, document);
    pw.close();
  }


}

