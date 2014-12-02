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

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import bioc.BioCSentence;
import bioc.BioCAnnotation;

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


/**
 *
 */
public class EntityLookup {
  private static final Logger logger = LogManager.getLogger(EntityLookup.class);

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
    return MWIUtilities.normalizeMetaString(inputtext.replaceAll(" of the", ","));
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
  public Collection<Entity> findLongestMatch(List<Document> documentList,
				       List<? extends Token> tokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    logger.debug("findLongestMatch");
    Map<String,Entity> candidateMap = new HashMap<String,Entity>();
    logger.debug("tokenlist text: " + Tokenize.getTextFromTokenList(tokenList));
    for (int i = tokenList.size(); i > 0; i--) { 
      // List<ERToken> tokenSubList = removePunctuation(tokenList.subList(0, i));
      List<? extends Token> tokenSubList = tokenList.subList(0, i);
      logger.debug("token sublist text: " + Tokenize.getTextFromTokenList(tokenSubList));
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
	logger.debug("term: \"" + term + 
			   "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
			   term.toLowerCase().equals(doc.get("str").toLowerCase()));

	if (term.toLowerCase().equals(doc.get("str").toLowerCase())) {
	  Entity entity;
	  if (candidateMap.containsKey(doc.get("cui"))) {
	    entity = candidateMap.get(doc.get("cui"));
	    entity.addMatchedWord(doc.get("str"));
	  } else {
	    if (tokenSubList.get(0) instanceof PosToken) {
	      entity = new Entity(doc.get("cui"), 
				  doc.get("str"), 
				  this.findPreferredName(doc.get("cui")),
				  this.getSourceSet(doc.get("cui")),
				  this.getSemanticTypeSet(doc.get("cui")),
				  originalTerm,
				  ((PosToken)tokenSubList.get(0)).getPosition(),
				  termLength,
				  0.0);
	      candidateMap.put(doc.get("cui"),entity);
	      if (tokenSubList.get(0) instanceof ERToken) {
		((ERToken)tokenSubList.get(0)).addEntity(entity);
	      }
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
    // 					     candidateMap.values()));
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
   * @param sentenceTokenList sentence to be examined.
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentenceTokenList(List<? extends Token> sentenceTokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<Entity> entitySet = new HashSet<Entity>();
    for (int i = 0; i<sentenceTokenList.size(); i++) {
      String prefix = sentenceTokenList.get(i).getText();
      if (prefix.trim().length() > 1) {
	logger.debug("processSentenceTokenList: prefix term: " + prefix);
	List<Document> hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(prefix,
									  this.mmIndexes.strQueryParser,
									  100);
	if (hitList.size() > 0) {
	  logger.debug("processSentenceTokenList: hit size: " + hitList.size());
	  if (logger.isDebugEnabled()) {
	    logHits(hitList);
	  }
	  for (Entity entity: this.findLongestMatch
		 (hitList,
		  sentenceTokenList.subList(i,Math.min(i+30,sentenceTokenList.size())))) {
	    entitySet.add(entity);
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
    return entityLookup.processSentenceTokenList(sentenceTokenList);
  }


  public static Set<BioCAnnotation> generateBioCEntitySet(List<? extends Token> sentenceTokenList)
    throws IOException, FileNotFoundException, ParseException
  {
    logger.debug("generateEntitySet: ");
    EntityLookup entityLookup = EntityLookup.singleton;
    Set<BioCAnnotation> bioCEntityList = new HashSet<BioCAnnotation>();
    for (Entity entity: entityLookup.processSentenceTokenList(sentenceTokenList)) {
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

  public static void displayEntitySet(BioCSentence sentence) {
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
  }
}
