
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

import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.Entity;


/**
 *
 */

public class MarkAbbreviations {
  private static final Logger logger = LoggerFactory.getLogger(MarkAbbreviations.class);

  AbbrConverter abbrConverter = new AbbrConverter();
  static ExtractAbbrev extractAbbr = new ExtractAbbrev();

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

  public static List<Entity> findMatches(String text, Entity entity) {
    String target = entity.getText();
    List<Entity> newEntityList = new ArrayList<Entity>();
    for (ERToken token: Scanner.analyzeText(text)) {
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


  public static List<Entity> markAbbreviations(String text, List<Entity> entityList)
  {
    Map<String,String> uaMap = new HashMap<String,String>();
    return markAbbreviations(text, uaMap, entityList);
  }

  public static List<Entity> markAbbreviations(String text,
					       Map<String,String> uaMap,
					       List<Entity> entityList) {
    // add abbreviations to entity set if present
    List<Entity> newEntityList = new ArrayList<Entity>(entityList);
    // Generate abbrevation Maps

    Map<String,String> abbrMap = new HashMap<String,String>(); // long form <-> short form
    // short form -> abbrevation info list
    Map<String,List<AbbrInfo>> shortFormMap = new HashMap<String,List<AbbrInfo>>(); 
    // long form -> abbrevation info list
    Map<String,List<AbbrInfo>> longFormMap = new HashMap<String,List<AbbrInfo>>();

    ArrayList<AbbrInfo> abbrInfoList = extractAbbr.extractAbbrPairsString(text);
    for (AbbrInfo abbrInfo: abbrInfoList) {
      System.out.println("abbrInfo:");
      System.out.println(" shortForm: " + abbrInfo.shortForm);
      System.out.println("  shortFormIndex: " + abbrInfo.shortFormIndex);	
      System.out.println(" longForm: " + abbrInfo.longForm);
      System.out.println("  longFormIndex: " + abbrInfo.longFormIndex);	
      if (shortFormMap.containsKey(abbrInfo.shortForm)) {
	shortFormMap.get(abbrInfo.shortForm).add(abbrInfo);
      } else {
	List<AbbrInfo> newList = new ArrayList<AbbrInfo>();
	newList.add(abbrInfo);
	shortFormMap.put(abbrInfo.shortForm, newList);
      }
      if (longFormMap.containsKey(abbrInfo.longForm)) {
	longFormMap.get(abbrInfo.longForm).add(abbrInfo);
      } else {
	List<AbbrInfo> newList = new ArrayList<AbbrInfo>();
	newList.add(abbrInfo);
	longFormMap.put(abbrInfo.longForm, newList);
      }
      if (! abbrMap.containsKey(abbrInfo.longForm)) {
	abbrMap.put(abbrInfo.longForm, abbrInfo.shortForm);
      }
    }

    // Actually add the entities
    if (abbrMap.size() > 0) {
      List<Entity> abbrevEntities = new ArrayList<Entity>();
      for (Entity entity: entityList) {
    	String key = entity.getText();
    	if (abbrMap.containsKey(key)) {
    	  logger.info("text -> " + key + " -> " + abbrMap.get(key));
    	  if (shortFormMap.containsKey(abbrMap.get(key))) {
    	    for (AbbrInfo abbrInfo: shortFormMap.get(abbrMap.get(entity.getText()))) {
	      System.out.println("abbrev shortForm: " + abbrInfo.shortForm); 
	      System.out.println("abbrev shortForm index: " + abbrInfo.shortFormIndex);
    	      int location = abbrInfo.shortFormIndex;
    	      // verify if abbreviation is in original text at specified offset 
    	      if ((location >= 0) && (abbrInfo.shortForm.length() > 0)) {
    		System.out.println("abbrev shortForm: " + abbrInfo.shortForm); 
    		System.out.println("abbrev location offset: " + location);
    		String passageText = text;
    		int begin = Math.max(0, location);
    		int end = Math.min(begin + abbrInfo.shortForm.length(), passageText.length());
    		System.out.println("abbrev begin: " + begin);
    		System.out.println("abbrev end: " + end);
    		String passageSubstring = passageText.substring(begin, end);
    		System.out.println("abbrev passageSubstring: " + passageSubstring);
    		if (passageSubstring.equals(abbrInfo.shortForm)) {
    		  System.out.println("adding entity " + abbrInfo.shortForm +
			       " " + abbrInfo.shortFormIndex);
    		  Entity newEntity = new Entity(entity);
    		  newEntity.setText(abbrInfo.shortForm);
    		  newEntity.setStart(location);
    		  newEntity.setLength(abbrInfo.shortForm.length());
    		  logger.info("newEntity: " + newEntity);
    		  newEntityList.add(newEntity);
    		  newEntityList.addAll(findMatches(text, newEntity));
    		}
    	      }
    	    }
    	  }
    	}
      } /* entity */
    }
    return newEntityList;
  }  

  /**
   * Add any entity that has an abbreviations.  Passage has been
   * pre-annotated using the abbreviation detector.
   * @param passage text of target passage
   * @param entityList initial list of entities 
   * @return initial entity list with abbreviation added.
   */
  public static List<Entity> markAbbreviations(BioCPassage passage, List<Entity> entityList)
  {
    Map<String,String> uaMap = new HashMap<String,String>();
    return markAbbreviations(passage, uaMap, entityList);
  }
  
  /**
   * Add any entity that has an abbreviations.  Passage has been
   * pre-annotated using the abbreviation detector.
   * @param passage text of target passage
   * @param uaMap user defined abbreviation map
   * @param entityList initial list of entities 
   * @return initial entity list with abbreviation added.
   */
  public static List<Entity> markAbbreviations(BioCPassage passage,
					       Map<String,String> uaMap,
					       List<Entity> entityList) {
    // add abbreviations to entity set if present
    List<Entity> newEntityList = new ArrayList<Entity>(entityList);
    // Generate abbrevation Maps

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
		  longForm = lfAnnotation.getText().replace("\n", " ");
		  List<BioCAnnotation> newLfAnnotList = new ArrayList<BioCAnnotation>();
		  newLfAnnotList.add(lfAnnotation);
		  longFormMap.put(longForm, newLfAnnotList);
		}
	      }
	    }
	  }
	  System.out.println("abbrvMap: " + shortForm + " -> " + longForm);
	  abbrMap.put(shortForm,longForm);
	  System.out.println("abbrvMap: " + longForm + " -> " + shortForm);
	  abbrMap.put(longForm,shortForm);
	}
      }
    } /*for relation in annotation relations*/

    if (uaMap.size() > 0) {
      abbrMap.putAll(uaMap);
    }
    
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
		logger.debug("abbrev location offset: " + location.getOffset());
		String passageText = passage.getText();
		int begin = Math.max(0, location.getOffset());
		int end = Math.min(begin + abbrAnnot.getText().length(), passageText.length());
		logger.debug("abbrev begin: " + begin);
		logger.debug("abbrev end: " + end);
		String passageSubstring = passageText.substring(begin, end);
		logger.debug("abbrev passageSubstring: " + passageSubstring);
		if (passageSubstring.equals(abbrAnnot.getText())) {
		  logger.debug("adding entity " + abbrAnnot.getText() + " "  + abbrAnnot.getLocations());
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
