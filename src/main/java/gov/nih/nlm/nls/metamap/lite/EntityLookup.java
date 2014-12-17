//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Collection;
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

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIndexes;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;

import gov.nih.nlm.nls.types.Sentence;

import gov.nih.nlm.nls.utils.StringUtils;

import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.dictionary.serializer.Entry;



/**
 *
 */
public class EntityLookup {
  private static final Logger logger = LogManager.getLogger(EntityLookup.class);
  int resultLength = 
    Integer.parseInt(System.getProperty("metamaplite.entitylookup.resultlength","110"));

  public MetaMapEvaluation metaMapEvalInst;
  public MetaMapIndexes mmIndexes;

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
  
  public EntityLookup() 
    throws IOException, FileNotFoundException, ParseException
  {
    this.mmIndexes = new MetaMapIndexes();
    this.metaMapEvalInst = new MetaMapEvaluation(this.mmIndexes);
  }

  public String findPreferredName(String cui)
    throws FileNotFoundException, IOException, ParseException
 {
    List<Document> hitList = 
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 1);
    if (hitList.size() > 0) {
      return hitList.get(0).get("str");
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
    if (inputtext.indexOf(" of the") > 0) {
      return MWIUtilities.normalizeMetaString(inputtext.replaceAll(" of the", ","));
    } 
    return inputtext;
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
  public Collection<List<Entity>> findLongestMatch(String docid, List<Document> documentList,
				       List<? extends Token> tokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    logger.debug("findLongestMatch");
    Map<String,List<Entity>> candidateMap = new HashMap<String,List<Entity>>();
    // logger.debug("tokenlist text: " + Tokenize.getTextFromTokenList(tokenList));
    for (int i = tokenList.size(); i > 0; i--) { 
      // List<ERToken> tokenSubList = removePunctuation(tokenList.subList(0, i));
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      // logger.debug("token sublist text: " + Tokenize.getTextFromTokenList(tokenSubList));
      List<String> tokenTextSubList = new ArrayList<String>();
      for (Token token: tokenSubList) {
	tokenTextSubList.add(token.getText());
      }
      ERToken firstToken = (ERToken)tokenSubList.get(0);
      ERToken lastToken = (ERToken)tokenSubList.get(tokenSubList.size() - 1);
      int termLength = (tokenSubList.size() > 1) ?
	(lastToken.getPosition() + lastToken.getText().length()) - firstToken.getPosition() : 
	firstToken.getText().length();
      String originalTerm = StringUtils.join(tokenTextSubList, "").trim();
      String term = transformPreposition(originalTerm);
      for (Document doc: documentList) {
	String cui = doc.get("cui");
	String docStr = doc.get("str");
	logger.debug("term: \"" + term + 
			   "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
		     term.equalsIgnoreCase(docStr));

	if (term.equalsIgnoreCase(docStr)) {
	  Entity entity;
	  if (tokenSubList.get(0) instanceof PosToken) {
	    entity = new Entity(docid,
				cui, 
				doc.get("str"), 
				this.findPreferredName(cui),
				this.getSourceSet(cui),
				this.getSemanticTypeSet(cui),
				originalTerm,
				((PosToken)tokenSubList.get(0)).getPosition(),
				termLength,
				0.0);
	    if (! candidateMap.containsKey(cui)) {
	      List<Entity> newEntityList = new ArrayList<Entity>();
	      newEntityList.add(entity);
	      candidateMap.put(cui, newEntityList);
	    }
	    if (tokenSubList.get(0) instanceof ERToken) {
	      ((ERToken)tokenSubList.get(0)).addEntity(entity);
	    }
	  }
	}
      }
    }
    // for (Entity candidate: candidateMap.values()) {
    //   candidate.setScore
    // 	(this.metaMapEvalInst.calculateScore(candidate.getConceptName(),
    // 					     candidate.getPreferredName(),
    // 					     candidate.getCUI(),
    // 					     candidate.getInputTextTokenList(),
    // 					     candidateMap.values()));/
    // }
    return candidateMap.values();
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
    Set<Entity> entitySet = new HashSet<Entity>();
    for (int i = 0; i<sentenceTokenList.size(); i++) {
      String prefix = sentenceTokenList.get(i).getText();
      if (prefix.trim().length() > 1) {
	logger.debug("processSentenceTokenList: prefix term: " + prefix);
	List<Document> hitList;
	try {
	  hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(prefix.toLowerCase(),
							     this.mmIndexes.strQueryParser,
							     resultLength);
	} catch (ParseException pe) {
	  System.err.println("errant term prefix: " + prefix);
	  System.err.println("tokenlist: " + Tokenize.getTextFromTokenList(sentenceTokenList));
	  hitList = new ArrayList<Document>(); // empty array list
	}
	if (hitList.size() > 0) {
	  logger.debug("processSentenceTokenList: hit size: " + hitList.size());
	  if (logger.isDebugEnabled()) {
	    logHits(hitList);
	  }
	  for (List<Entity> entityList: this.findLongestMatch
		 (docid,
		  hitList,
		  sentenceTokenList.subList(i,Math.min(i+30,sentenceTokenList.size())))) {
	    for (Entity entity: entityList) {
	      entitySet.add(entity);
	    }
	  }
	}
      }
    }
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


  public static Set<BioCAnnotation> generateBioCEntitySet(String docid,
							  List<? extends Token> sentenceTokenList)
    throws IOException, FileNotFoundException, ParseException
  {
    logger.debug("generateEntitySet: ");
    EntityLookup entityLookup = EntityLookup.singleton;
    Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
    for (Entity entity: entityLookup.processSentenceTokenList(docid, sentenceTokenList)) {
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
	System.out.print(((BioCEntity)annotation).getEntity().toString());
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
	  if (annotation instanceof BioCEntity) {
	    writer.print(((BioCEntity)annotation).getEntity().toString());
	    for (Map.Entry<String,String> entry: annotation.getInfons().entrySet()) {
	      writer.print(entry.getKey() + ":" + entry.getValue() + "|");
	    }
	    writer.println();
	  } else {
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
		     term + "\t" +
		     rindex + "\t" +
		     0.9);
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

