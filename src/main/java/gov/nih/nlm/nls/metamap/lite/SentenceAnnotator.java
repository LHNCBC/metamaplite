
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Properties;

import gov.nih.nlm.nls.types.Annotation;
import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
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

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 *
 */

public class SentenceAnnotator {
  private static final Logger logger = LogManager.getLogger(SentenceAnnotator.class);

  public POSModel posModel;
  public POSTaggerME posTagger;

  static AbbrConverter abbrConverter = new AbbrConverter();

  public SentenceAnnotator()
  {
    InputStream modelIn = null;

    try {
      modelIn = new FileInputStream(System.getProperty("opennlp.en-pos.bin.path",
						       "data/models/en-pos-maxent.bin"));
      this.posModel = new POSModel(modelIn);
    this.posTagger = new POSTaggerME(this.posModel);
    }
    catch (IOException e) {
      // Model loading failed, handle the error
      e.printStackTrace();
    }
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException e) {
	}
      }
    }
  }

  public SentenceAnnotator(Properties properties)
  {
    InputStream modelIn = null;

    try {
      modelIn = new FileInputStream(properties.getProperty("opennlp.en-pos.bin.path",
							   "data/models/en-pos-maxent.bin"));
      this.posModel = new POSModel(modelIn);
      this.posTagger = new POSTaggerME(this.posModel);
    }
    catch (IOException e) {
      // Model loading failed, handle the error
      e.printStackTrace();
    }
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException e) {
	}
      }
    }

  }
  
  /**
   * List part of speech tags corresponding to to oridinal position of tokens.
   * @param tokenList input token list
   * @return list string of part of speech corresponding to oridinal position of tokens.
   */
  public List<String> listPartOfSpeech(List<? extends Token> tokenList) {
    String[] textArray = new String[tokenList.size()];
    int i = 0;
    for (Token token: tokenList) {
      textArray[i] = token.getText();
      i++;
    }
    String tags[] = this.posTagger.tag(textArray);
    return Arrays.asList(tags);
  }

  /** 
   * Add part of speech to tokens in tokenlist.
   * @param tokenList input token list
   */
    public void addPartOfSpeech(List<ERToken> tokenList) {
    String[] textArray = new String[tokenList.size()];
    int i = 0;
    for (ERToken token: tokenList) {
      textArray[i] = token.getText();
      i++;
    }
    String tags[] = this.posTagger.tag(textArray);
    i = 0;
    for (ERToken token: tokenList) {
      token.setPartOfSpeech(tags[i]);
      i++;
    }
  }
  
  /**
   * Keep annotations marked as type "token", discard all others.
   * @param annotationList list of annotations
   * @return list of annotations of type "token"
   */
  public List<BioCAnnotation> keepTokens(List<BioCAnnotation> annotationList) {
    List<BioCAnnotation> tokenAnnotations = new ArrayList<BioCAnnotation> ();
    for (BioCAnnotation annotation: annotationList) {
      if (annotation.getInfon("type").equals("token")) {
	tokenAnnotations.add(annotation);
      }
    }
    return tokenAnnotations;
  }

  /**
   * Add part of speech annotations to sentence that already contains
   * token annotations (annotation type is "token").
   * @param sentence bioC sentence containing annotations of type "token".
   */
  public void addPartOfSpeech(BioCSentence sentence) {
    List<BioCAnnotation> tokenAnnotations = keepTokens(sentence.getAnnotations());
    String[] textArray = new String[tokenAnnotations.size()];
    int i = 0;
    for (BioCAnnotation bioCToken: tokenAnnotations) {
      textArray[i] = bioCToken.getText();
      i++;
    }
    String tags[] = this.posTagger.tag(textArray);
    i = 0;
    for (String tag: tags) {
      BioCAnnotation bioCPosTag = new BioCAnnotation();
      bioCPosTag.setID("postag" + Integer.toString(i));
      bioCPosTag.setText(tag);
      bioCPosTag.setLocations(tokenAnnotations.get(i).getLocations());
      tokenAnnotations.get(i).putInfon("postag", tag);
      bioCPosTag.putInfon("type", "postag");
      sentence.addAnnotation(bioCPosTag);
      i++;
    }
  }


  public List<ERToken> addPartOfSpeech(Sentence sentence) {
    List<ERToken> tokenList = Scanner.analyzeText(sentence);
    addPartOfSpeech(tokenList);
    return tokenList;
  }

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

  /**
   * @param passage bioC passage containing sentence
   * @param sentence bioC sentence to be tokenized
   * @return sentence with addition of annotations of type "token".
   */
  public static BioCSentence tokenizeSentence(BioCPassage passage, BioCSentence sentence) {
    int i = 0;
    for (ERToken token: Scanner.analyzeText(sentence)) {
      BioCAnnotation bioCToken = new BioCAnnotation();
      bioCToken.setID(Integer.toString(i));
      bioCToken.setText(token.getText());
      BioCLocation location =
	new BioCLocation(passage.getOffset() + token.getOffset(), 
			 (token.getOffset() + token.getText().length()) - token.getOffset());
      bioCToken.addLocation(location);
      bioCToken.putInfon("type", "token");
      sentence.addAnnotation(bioCToken);
      i++;
    }
    return sentence;
  }

  /** apply analyze text to tokenize sentence and then add tokenlist annotation to sentence.
   * @param sentence sentence to be tokenized.
   * @return sentence 
   */
  public static BioCSentence tokenizeSentence(BioCSentence sentence) {
    int i = 0;
    for (ERToken token: Scanner.analyzeText(sentence)) {
      BioCAnnotation bioCToken = new BioCAnnotation();
      bioCToken.setID(Integer.toString(i));
      bioCToken.setText(token.getText());
      BioCLocation location = 
	new BioCLocation(sentence.getOffset() + token.getOffset(), 
			 (token.getOffset() + token.getText().length()) - token.getOffset());
      bioCToken.addLocation(location);
      bioCToken.putInfon("type", "token");
      sentence.addAnnotation(bioCToken);
      i++;
    }
    return sentence;
  }

  /** 
   * precondition: sentence must contain a TokenListAnnotation.
   * @param entityLookup entityLookup class instance 
   * @param sentence target sentence
   * @return sentence annotated with entities
   */
  public BioCSentence addEntities(EntityLookup entityLookup, BioCSentence sentence)
    throws IOException
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
    List<ERToken>tokenList = Scanner.analyzeText(sentence);
    this.addPartOfSpeech(tokenList);
	
    // find any entities in sentence 
    Set<BioCAnnotation> entitySet = 
      entityLookup.generateBioCEntitySet(docid, tokenList);
    logger.debug(" sentence: " + sentence.getText());
    logger.debug("                    entitySet: ---------------------");
    for (BioCAnnotation entity: entitySet) {
      logger.debug(" " + entity);
    }
    logger.debug("entitySet after adding abbrev: ---------------------");
    for (BioCAnnotation entity: entitySet) {
      logger.debug(" " + entity);
    }
    /* sentence.setAnnotations(new ArrayList<BioCAnnotation>(entitySet)); */
    for (BioCAnnotation anAnnotation: entitySet) {
      sentence.addAnnotation(anAnnotation);
    }
    return sentence;    
  }

  /** precondition: sentence must contain a TokenListAnnotation. */
  public BioCSentence addEntities(EntityLookup entityLookup,
				  BioCSentence sentence,
				  BioCPassage passage)
    throws IOException
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
    List<ERToken>tokenList = Scanner.analyzeText(sentence);
    this.addPartOfSpeech(tokenList);
    // find any entities in sentence 
    Set<BioCAnnotation> entitySet = 
      entityLookup.generateBioCEntitySet(docid, tokenList);
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
    /*sentence.setAnnotations(new ArrayList<BioCAnnotation>(entitySet));*/
    for (BioCAnnotation anAnnotation: entitySet) {
      sentence.addAnnotation(anAnnotation);
    }	
    return sentence;    
  }
}
