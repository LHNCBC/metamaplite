
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import bioc.BioCAnnotation;
import bioc.BioCLocation;
import bioc.BioCNode;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.BioCPassage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.Entity;


/**
 *
 */

public class MarkAbbreviations {
  private static final Logger logger = LogManager.getLogger(MarkAbbreviations.class);


  public static List<Entity> findMatches(BioCPassage passage, Entity entity) {
    String target = entity.getText();
    List<Entity> newEntityList = new ArrayList<Entity>();
    for (ERToken token: Scanner.analyzeText(passage.getText())) {
      if (token.getText().equals(target)) {
	Entity newEntity = new Entity(entity);
	newEntity.setText(token.getText());
	newEntity.setStart(token.getPosition());
	newEntity.setLength(token.getText().length());
	newEntityList.add(newEntity);
      }
    }
    return newEntityList;
  }

  /**
   * add any entity that are abbreviations.
   * @param passage text of target passage
   * @param entityList initial list of entities 
   * @return initial entity list with abbreviation added.
   */
  public static List<Entity> markAbbreviations(BioCPassage passage, List<Entity> entityList) {
    // add abbreviations to entity set if present
    List<Entity> newEntityList = new ArrayList<Entity>(entityList);
    // generate abbrevation Maps 
    Map<String,String> abbrMap = new HashMap<String,String>(); // short form -> long form
    Map<String,List<BioCAnnotation>> shortFormMap = new HashMap<String,List<BioCAnnotation>>(); // short form -> annotation list
    Map<String,List<BioCAnnotation>> longFormMap = new HashMap<String,List<BioCAnnotation>>(); // long form -> annotation list
    for (BioCRelation relation: passage.getRelations()) {
      if (relation.getInfon("type") != null) {
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
      }
    } /*for relation in annotation relations*/

    // Actually add the entities
    if (abbrMap.size() > 0) {
      List<Entity> abbrevEntities = new ArrayList<Entity>();
      for (Entity entity: entityList) {
	String key = entity.getText();
	if (abbrMap.containsKey(key)) {
	  logger.info("text -> " + key + " -> " + abbrMap.get(key));
	  if (shortFormMap.containsKey(abbrMap.get(key))) {
	    for (BioCAnnotation abbrAnnot: shortFormMap.get(abbrMap.get(entity.getText()))) {
	      BioCLocation location = abbrAnnot.getLocations().get(0);
	      // verify if abbreviation is in original text at specified offset 
	      if ((location.getOffset() > 0) && (abbrAnnot.getText().length() > 0)) {
		logger.debug("abbrev annotation: " + abbrAnnot.getText() ); 
		logger.debug("location offset: " + location.getOffset()); 
		if (passage.getText().substring(location.getOffset(), location.getOffset() + abbrAnnot.getText().length()).equals(abbrAnnot.getText())) {
		  logger.info("adding " + abbrAnnot.getText() + " "  + abbrAnnot.getLocations());
		  Entity newEntity = new Entity(entity);
		  newEntity.setText(abbrAnnot.getText());
		  newEntity.setStart(location.getOffset());
		  newEntity.setLength(abbrAnnot.getText().length());
		  logger.info("newEntity: " + newEntity);
		  newEntityList.add(newEntity);
		  newEntityList.addAll(findMatches(passage,newEntity));
		}
	      }
	    }
	  }
	}
      } /* entity */
    }

    return newEntityList;
  }
}
