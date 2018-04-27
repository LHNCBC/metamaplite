package gov.nih.nlm.nls.metamap.lite.context;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import gov.nih.nlm.nls.metamap.lite.NegationDetector;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.types.Sentence;

import context.implementation.ConText;

import bioc.BioCSentence;
import bioc.BioCAnnotation;

/**
 * A wrapper around Wendy Chapman's ConTexT algorithm.
 * <p>
 * All methods run the context instance method: applyContext and the
 * return the result.
 */

public class ContextWrapper implements NegationDetector {
  static ConText contextInstance = new ConText();

  public void initProperties(Properties properties) {
    // currently empty.
  }
  
  /** Given annotated sentence list with entities, determine hedging
   * relations using ConText.
   * @param conceptList list of concepts
   * @param sentenceList list of sentences
   * @throws Exception any exception
   * @return list of lists of strings containing relations
   */
  public static List<List<String>> applyContext(List<String> conceptList, List<String> sentenceList) 
    throws Exception {
    List<List<String>> resultlist = new ArrayList<List<String>>();
    for (String concept: conceptList) {
      for (String sentence: sentenceList) {
	resultlist.add(contextInstance.applyContext(concept, sentence));
      }
    }
    return resultlist;
  }

  public static List<List<String>> applyContextUsingEntities(Collection<Entity> entityList, 
							     String sentence)
    throws Exception {
    List<List<String>> resultlist = new ArrayList<List<String>>();
    for (Entity entity: entityList) {
      List<String> result = contextInstance.applyContext(entity.getText(), sentence);
      resultlist.add(result);
      if (result.get(2).equals("Negated")) {
	entity.setNegated(true);
      }
      entity.setTemporality(result.get(3));
    }
    return resultlist;
  }

  /** Given annotated sentence list with entities, determine hedging
   * relations using ConText.
   * @param entityList list of entities found in sentence list.
   * @param sentenceList list of sentences.
   * @return list of lists of strings containing relations
   * @throws Exception general exception
   */
  public static List<List<String>> applyContextUsingEntities(List<Entity> entityList, 
							     List<Sentence> sentenceList) 
    throws Exception {
    List<List<String>> resultlist = new ArrayList<List<String>>();
    for (Entity entity: entityList) {
      for (Sentence sentence: sentenceList) {
	List<String> result = contextInstance.applyContext(entity.getText(), sentence.getText());
	resultlist.add(result);
	if (result.get(2).equals("Negated")) {
	  entity.setNegated(true);
	}
	entity.setTemporality(result.get(3));
      }
    }
    return resultlist;
  }

  /**
   * Given a set of annotated sentence with associated entities,
   * determine hedging relations using ConText.
   * @param sentence BioC sentence
   * @return BioC sentence with negation apply to sentences
   * @throws Exception general exception
   */
  public static BioCSentence applyContext(BioCSentence sentence) throws Exception {
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof BioCEntity) {
	for (Entity entity: ((BioCEntity)annotation).getEntitySet()) {
	  List<String> result = 
	    contextInstance.applyContext(entity.getMatchedText(), 
					 sentence.getText());
	  if (result != null) {
	    /** add negation information to existing annotation */
	    annotation.putInfon("concept",     result.get(0));
	    annotation.putInfon("sentence",    result.get(1));
	    annotation.putInfon("negstatus",   result.get(2));
	    annotation.putInfon("temporality", result.get(3));
	    annotation.putInfon("subject",     result.get(4));
	    if (result.get(2).equals("Negated")) {
	      entity.setNegated(true);
	    }
	    entity.setTemporality(result.get(3));
	  }
	}
      }
    }
    return sentence;
  }

  public void detectNegations(Set<Entity> entitySet, String sentence, List<ERToken> tokenList)
  {
    try {
      for (Entity entity: entitySet) {
	List<String> result = 
	  contextInstance.applyContext(entity.getMatchedText(), sentence);
	if (result != null) {
	  if (result.get(2).equals("Negated")) {
	    entity.setNegated(true);
	  }
	  entity.setTemporality(result.get(3));
	}
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
 
