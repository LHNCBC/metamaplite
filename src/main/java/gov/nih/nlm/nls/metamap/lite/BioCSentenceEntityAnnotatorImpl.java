// An implemenattion of a BioC Enitty Annotator
package gov.nih.nlm.nls.metamap.lite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bioc.BioCAnnotation;
import bioc.BioCLocation;
import bioc.BioCNode;
import bioc.BioCRelation;
import bioc.BioCSentence;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.utils.StringUtils;

/**
 * An implemenattion of a BioC Enitty Annotator
 */

public class BioCSentenceEntityAnnotatorImpl implements BioCSentenceEntityAnnotator
{
  /** 
   * Annotate sentence using entities from entity set adding entity
   * and concept annotations with linking relations.  Token and
   * part-of-speech annotations are removed.
   *
   * @param tokenizedSentence with token and part-of-speech annotations.
   * @param entitySet set of entities extracted from the sentence.
   * @return BioCSentence with entity and concept annotations with linking relations.
   */
  public BioCSentence annotateSentence(BioCSentence tokenizedSentence, Set<Entity> entitySet)
  {
    int i = 0;
    int ri = 0;
    int ci = 0;
    BioCSentence annotatedSentence = new BioCSentence();
    annotatedSentence.setInfons(tokenizedSentence.getInfons());
    annotatedSentence.setText(tokenizedSentence.getText());
    annotatedSentence.setOffset(tokenizedSentence.getOffset());

    // Discard any token and part-of-speech annotations present in collection.
    for (BioCAnnotation annotation: tokenizedSentence.getAnnotations()) {
      if ((! annotation.getInfon("type").equals("token")) &&
	  (! annotation.getInfon("type").equals("postag"))) {
	annotatedSentence.addAnnotation(annotation);
      }
    }
    for (BioCRelation relation: tokenizedSentence.getRelations()) {
      annotatedSentence.addRelation(relation);
    }
    Map<String,String> cuiAnnotationIdMap = new HashMap<String,String>();
    for (Entity entity: entitySet) {
      BioCAnnotation bioCEntity = new BioCAnnotation();
      bioCEntity.setID("E" + Integer.toString(i));
      bioCEntity.setText(entity.getText());
      BioCLocation location = new BioCLocation(entity.getStart(), entity.getLength());
      bioCEntity.addLocation(location);
      bioCEntity.putInfon("type", "entity");
      bioCEntity.putInfon("negated", Boolean.toString(entity.isNegated()));
      annotatedSentence.addAnnotation(bioCEntity);
      
      for (Ev ev: entity.getEvList()) {
	ConceptInfo conceptInfo = ev.getConceptInfo();
	String cui = conceptInfo.getCUI();
	String conceptAnnotationId;
	// if we already have an annotation refering to this concept then use it's reference, otherwise add concept to the cui -> concept annotation map.
	if (cuiAnnotationIdMap.containsKey(cui)) {
	  conceptAnnotationId = cuiAnnotationIdMap.get(cui);
	} else {
	  BioCAnnotation conceptAnnotation = new BioCAnnotation();
	  conceptAnnotationId = "EC" + Integer.toString(ci);
	  conceptAnnotation.setID(conceptAnnotationId);
	  conceptAnnotation.putInfon("KnowledgeSource", "UMLS");
	  conceptAnnotation.putInfon("cui", cui);
	  conceptAnnotation.putInfon("preferredname", conceptInfo.getPreferredName());
	  conceptAnnotation.putInfon("semantictypeset", StringUtils.join(conceptInfo.getSemanticTypeSet(), ","));
	  conceptAnnotation.putInfon("sourceset", StringUtils.join(conceptInfo.getSourceSet(), ","));
	  cuiAnnotationIdMap.put(cui, conceptAnnotation.getID());
	  annotatedSentence.addAnnotation(conceptAnnotation);
	  ci++;
	}
	// add relation linking concept to entity
	BioCRelation relation = new BioCRelation();
	relation.setID("R" + Integer.toString(ri));
	BioCNode entityNode = new BioCNode(bioCEntity.getID(),"entity");
	BioCNode conceptNode = new BioCNode(conceptAnnotationId,"concept");
	relation.addNode(entityNode);
	relation.addNode(conceptNode);
	annotatedSentence.addRelation(relation);
      }
      i++;
    }
    return annotatedSentence;
  }
}
