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

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreComparator;
// import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreConceptNameComparator;
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIndexes;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;

import gov.nih.nlm.nls.utils.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class EntityLookup {
  private static final Logger logger = LogManager.getLogger("EntityLookup");

  public MetaMapEvaluation metaMapEvalInst;
  public MetaMapIndexes mmIndexes;

  
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
				       List<ERToken> tokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    logger.debug("findLongestMatch");
    Map<String,Entity> candidateMap = new HashMap<String,Entity>();
    logger.debug("tokenlist text: " + Tokenize.getTextFromTokenList(tokenList));
    for (int i = tokenList.size(); i > 0; i--) { 
      // List<ERToken> tokenSubList = removePunctuation(tokenList.subList(0, i));
      List<ERToken> tokenSubList = tokenList.subList(0, i);
      logger.debug("token sublist text: " + Tokenize.getTextFromTokenList(tokenSubList));
      List<String> tokenTextSubList = new ArrayList<String>();
      for (ERToken token: tokenSubList) {
	tokenTextSubList.add(token.getText());
      }
      String term = StringUtils.join(tokenTextSubList, "");
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
	    entity = new Entity(doc.get("cui"), 
				doc.get("str"), 
				this.findPreferredName(doc.get("cui")),
				this.getSourceSet(doc.get("cui")),
				this.getSemanticTypeSet(doc.get("cui")),
				tokenTextSubList.toArray(new String[0]),
				tokenSubList.get(0).getPosition(),
				tokenSubList.get(0).getText().length(),
				0.0);
	    candidateMap.put(doc.get("cui"),entity);
	  }
	  tokenSubList.get(0).addEntity(entity);
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

  void displayHits(List<Document> hitList) {
    logger.debug("displayHits");
    for (Document hit: hitList) {
      // System.out.println(hit);
      System.out.println(hit.get("cui") + "|" + hit.get("str") + "|" + hit.get("src"));
    }
  }

  /**
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence.
   *
   * @param sentence sentence to be examined.
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentenceTokenList(List<ERToken> sentenceTokenList)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<Entity> entitySet = new HashSet<Entity>();
    for (int i = 0; i<sentenceTokenList.size(); i++) {
      String prefix = sentenceTokenList.get(i).getText();
      if (prefix.trim().length() > 1) {
	logger.debug("processSentenceTokenList: prefix term: " + prefix);
	List<Document> hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(prefix,
									  this.mmIndexes.strQueryParser,
									  10);
	if (hitList.size() > 0) {
	  logger.debug("processSentenceTokenList: hit size: " + hitList.size());
	  displayHits(hitList);
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

  public static Set<Entity> generateEntitySet(List<ERToken> sentenceTokenList)
    throws IOException, FileNotFoundException, ParseException
  {
    logger.debug("generateEntitySet: ");
    EntityLookup entityLookup = new EntityLookup();
    return entityLookup.processSentenceTokenList(sentenceTokenList);
  }
  
  public static void displayEntitySet(Set<Entity> entitySet) {
    logger.debug("displayEntitySet");
    for (Entity entity: entitySet) {
      System.out.println(entity + ", ");
    }
  }
}
