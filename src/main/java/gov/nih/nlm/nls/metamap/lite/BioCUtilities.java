package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import bioc.BioCPassage;
import bioc.BioCSentence;
import bioc.BioCAnnotation;
import bioc.BioCLocation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;

/**
 * Describe class BioCUtilities here.
 *
 *
 * Created: Fri Apr  7 13:50:05 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class BioCUtilities {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(BioCUtilities
							    .class);

  /**
   * Keep annotations marked as type "token", discard all others.
   * @param annotationList list of annotations
   * @return list of annotations of type "token"
   */
  public static List<BioCAnnotation> keepTokenAnnotations(List<BioCAnnotation> annotationList) {
    List<BioCAnnotation> tokenAnnotations = new ArrayList<BioCAnnotation> ();
    for (BioCAnnotation annotation: annotationList) {
      if (annotation.getInfon("type").equals("token")) {
	tokenAnnotations.add(annotation);
      }
    }
    return tokenAnnotations;
  }

  /**
   * Keep annotations marked as type supplied by caller, discard all others.
   * @param annotationList list of annotations
   * @param typeName name of annotations to keep under infon "type".
   * @return list of annotations of type "token"
   */
  public static List<BioCAnnotation> keepAnnotationsOfType(List<BioCAnnotation> annotationList, String typeName) {
    List<BioCAnnotation> tokenAnnotations = new ArrayList<BioCAnnotation> ();
    for (BioCAnnotation annotation: annotationList) {
      if (annotation.getInfon("type").equals(typeName)) {
	tokenAnnotations.add(annotation);
      }
    }
    return tokenAnnotations;
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
   * @param sentenceAnnotator sentence annotator class
   * @param sentence target sentence
   * @return sentence annotated with entities
   * @throws IOException Input/Output exception
   */
  public static BioCSentence addEntities(EntityLookup entityLookup, SentenceAnnotator sentenceAnnotator, BioCSentence sentence)
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
    sentenceAnnotator.addPartOfSpeech(tokenList);
	
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

  
}
