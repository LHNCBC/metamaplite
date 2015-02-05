
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import gov.nih.nlm.nls.types.Annotation;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.TokenListAnnotation;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

import bioc.BioCAnnotation;
import bioc.BioCLocation;
import bioc.BioCNode;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.BioCPassage;
import bioc.tool.AbbrConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */

public class SentenceAnnotator {
  private static final Logger logger = LogManager.getLogger(SentenceAnnotator.class);

  static AbbrConverter abbrConverter = new AbbrConverter();

  public static void addAbbreviationsToEntitySet(BioCPassage passage, Set<BioCAnnotation> entitySet) {
    // add abbreviations to entity set if present
    Map<String,String> abbrMap = new HashMap<String,String>(); // short form -> long form
    Map<String,List<BioCAnnotation>> shortFormMap = new HashMap<String,List<BioCAnnotation>>(); // short form -> annotation list
    Map<String,List<BioCAnnotation>> longFormMap = new HashMap<String,List<BioCAnnotation>>(); // long form -> annotation list
    for (BioCRelation relation: passage.getRelations()) {
      if (relation.getInfon("type").equals("ABBR")) {
	String shortForm = "";
	String longForm = "";
	for (BioCNode node: relation.getNodes()) {
	  if (node.getRole().equals("ShortForm")) {
	    String refId = node.getRefid();
	    for (BioCAnnotation sfAnnotation: passage.getAnnotations()) {
	      if (sfAnnotation.getID() == refId) {
		shortForm = sfAnnotation.getText();
		List<BioCAnnotation> newSfAnnotList = new ArrayList<BioCAnnotation>();
		newSfAnnotList.add(sfAnnotation);
		shortFormMap.put(shortForm, newSfAnnotList);
	      }
	    }
	  } else if (node.getRole().equals("LongForm")) {
	    String refId = node.getRefid();
	    for (BioCAnnotation lfAnnotation: passage.getAnnotations()) {
	      if (lfAnnotation.getID() == refId) {
		longForm = lfAnnotation.getText();
		List<BioCAnnotation> newLfAnnotList = new ArrayList<BioCAnnotation>();
		newLfAnnotList.add(lfAnnotation);
		longFormMap.put(longForm, newLfAnnotList);
	      }
	    }
	  }
	}
	logger.debug("abbrvMap: " + shortForm + " -> " + longForm);
	abbrMap.put(shortForm,longForm);
	logger.debug("abbrvMap: " + longForm + " -> " + shortForm);
	abbrMap.put(longForm,shortForm);
      }
    } /*for relation in annotation relations*/
	
    if (abbrMap.size() > 0) {
      List<BioCEntity> abbrevEntities = new ArrayList<BioCEntity>();
      for (BioCAnnotation bioCAnnotation: entitySet) {
	if (bioCAnnotation instanceof BioCEntity) {
	  BioCEntity bioCEntity = (BioCEntity)bioCAnnotation;
	  if (abbrMap.containsKey(bioCEntity.getText())) {
	    BioCEntity newBioCEntity = new BioCEntity();
	    for (Entity entity: bioCEntity.getEntitySet()) {
	      String key = bioCEntity.getText();
	      if (abbrMap.containsKey(key)) {
		logger.info("text -> " + key + " -> " + abbrMap.get(key));
		if (shortFormMap.containsKey(abbrMap.get(key))) {
		  for (BioCAnnotation abbrAnnot: shortFormMap.get(abbrMap.get(bioCEntity.getText()))) {
		    logger.info("adding " + abbrAnnot.getText() + " "  + abbrAnnot.getLocations());
		    Entity newEntity = new Entity(entity);
		    newEntity.setText(abbrAnnot.getText());
		    BioCLocation location = abbrAnnot.getLocations().get(0);
		    newEntity.setStart(location.getOffset());
		    newEntity.setLength(abbrAnnot.getText().length());
		    newBioCEntity.setText(abbrMap.get(key));
		    newBioCEntity.addLocation(location);
		    newBioCEntity.addEntity(newEntity);
		    logger.info("BioCEntity: " + newBioCEntity);
		  }
		}
	      }
	    } /* entity */
	    logger.info("adding BioCEntity: " + newBioCEntity);
	    if (newBioCEntity.getEntitySet().size() > 0) {
	      abbrevEntities.add(newBioCEntity);
	    }
	  } /* if bioC Entity text is present in abbrMap. */
	} 
      } /* for bioannotation */
      entitySet.addAll(abbrevEntities);
    }
  }

  /** apply analyze text to tokenize sentence and then add tokenlist annotation to sentence. */
  public static BioCSentence tokenizeSentence(BioCSentence sentence) {
    sentence.addAnnotation
      (new TokenListAnnotation("",
			       sentence.getText(),
			       Scanner.analyzeText(sentence)));
    return sentence;
  }

  /** precondition: sentence must contain a TokenListAnnotation. */
  public static BioCSentence addEntities(BioCSentence sentence)
    throws IOException, ParseException
  {
    List<BioCAnnotation> originalAnnotations = sentence.getAnnotations();
    // annotate sentence with any abbreviations found.
    // BioCSentence sentence = abbrConverter.getSentence(theSentence);
    // logger.debug("sentence relations: " + sentence.getRelations());
    // logger.debug("sentence annotations: " + sentence.getAnnotations());
    //     for (BioCAnnotation annotation: originalAnnotations) {
    //      sentence.addAnnotation(annotation);
    //    }

    String docid = sentence.getInfon("docid");
    if (docid == null) { docid = "00000000.tx"; }
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof TokenListAnnotation) {

	// find any entities in sentence 
	Set<BioCAnnotation> entitySet = 
	  EntityLookup.generateBioCEntitySet
	  (docid,
	   (List<? extends Token>)((TokenListAnnotation)annotation).getTokenList());
	logger.debug(" sentence: " + sentence.getText());
	logger.debug("                    entitySet: ---------------------");
	for (BioCAnnotation entity: entitySet) {
	  logger.debug(" " + entity);
	}
        logger.debug("entitySet after adding abbrev: ---------------------");
	for (BioCAnnotation entity: entitySet) {
	  logger.debug(" " + entity);
	}
	sentence.setAnnotations(new ArrayList<BioCAnnotation>(entitySet));
      } /*if*/
    }
    return sentence;    
  }

  /** precondition: sentence must contain a TokenListAnnotation. */
  public static BioCSentence addEntities(BioCSentence sentence,
					 BioCPassage passage)
    throws IOException, ParseException
  {
    List<BioCAnnotation> originalAnnotations = sentence.getAnnotations();
    // annotate sentence with any abbreviations found.
    // BioCSentence sentence = abbrConverter.getSentence(theSentence);
    // logger.debug("sentence relations: " + sentence.getRelations());
    // logger.debug("sentence annotations: " + sentence.getAnnotations());
    //     for (BioCAnnotation annotation: originalAnnotations) {
    //      sentence.addAnnotation(annotation);
    //    }

    String docid = sentence.getInfon("docid");
    if (docid == null) { docid = "00000000.tx"; }
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof TokenListAnnotation) {

	// find any entities in sentence 
	Set<BioCAnnotation> entitySet = 
	  EntityLookup.generateBioCEntitySet
	  (docid,
	   (List<? extends Token>)((TokenListAnnotation)annotation).getTokenList());
	logger.debug(" sentence: " + sentence.getText());
	logger.debug("                    entitySet: ---------------------");
	for (BioCAnnotation entity: entitySet) {
	  logger.debug(" " + entity);
	}
	addAbbreviationsToEntitySet(passage, entitySet);
        logger.debug("entitySet after adding abbrev: ---------------------");
	for (BioCAnnotation entity: entitySet) {
	  logger.debug(" " + entity);
	}
	sentence.setAnnotations(new ArrayList<BioCAnnotation>(entitySet));
      } /*if*/
    }
    return sentence;    
  }
}
