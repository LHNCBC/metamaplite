
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;
import bioc.BioCSentence;
import bioc.BioCAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */

public class SemanticGroupFilter {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(SemanticGroupFilter.class);

  static Set<String> semanticGroup = SemanticGroups.disorders;

  // this isn't threadable
  public static void setSemanticGroup(Set<String> semanticTypeList) {
    semanticGroup = semanticTypeList;
  }

  // public Set<T> intersection(Set<T> set1, Set<T> set2) {
  //   HashSet<T> intersectionSet = new HashSet<T>(set1);
  //   // retainAll for intersection test? that's intuitive! (NOTE: retainAll is destructive.)
  //   intersectionSet.retainAll(set2);
  //   return intersectionSet;
  // }

  public static List<Entity> keepEntitiesInSemanticGroup(Set<String> semanticGroup, 
							     List<Entity> entityList) {
    List<Entity> filteredEntityList = new ArrayList<Entity>();
    for (Entity entity: entityList) {
      List<Ev> newEvList = new ArrayList<Ev>();
      for (Ev ev: entity.getEvList()) {
	if (semanticGroup.contains("all")) {
	  newEvList.add(ev);
	} else if (ev.getConceptInfo().getSemanticTypeSet() instanceof HashSet) {
	  Set<String> semanticTypeSet = ev.getConceptInfo().getSemanticTypeSet();
	  logger.debug("entity has semantic type set: " + semanticTypeSet );
	  // retainAll for intersection test? that's intuitive! (NOTE: retainAll is destructive.)
	  HashSet<String> intersectionSet = new HashSet<String>(semanticTypeSet);
	  intersectionSet.retainAll(semanticGroup);
	  if (intersectionSet.size() > 0) {
	    logger.info("added ev instance with semantic type set: " + 
			ev.getConceptInfo().getSemanticTypeSet() );
	    logger.info("ev: " + ev);
	    newEvList.add(ev);
	    break;
	  }
	}
      } /* for evlist */
      if (newEvList.size() > 0) {
	entity.setEvList(newEvList);
	filteredEntityList.add(entity);
      }
    } /* for entityList */
    return filteredEntityList;
  }


  public static List<BioCAnnotation> keepEntitiesInSemanticGroup(Set<String> semanticGroup, 
								 Collection<BioCAnnotation> annotationList) {
    List<BioCAnnotation> filteredAnnotationList = new ArrayList<BioCAnnotation>();
    for (BioCAnnotation annotation: annotationList) {
      if (annotation instanceof BioCEntity) {
	BioCEntity biocEntity = (BioCEntity)annotation;
	// if entity semantic type intersects with semanticGroup, keep entity
	for (Entity entity: biocEntity.getEntitySet()) {
	  List<Ev> newEvList = new ArrayList<Ev>();
	  for (Ev ev: entity.getEvList()) {
	    if (semanticGroup.contains("all")) {
	      newEvList.add(ev);
	    } else if (ev.getConceptInfo().getSemanticTypeSet() instanceof HashSet) {
	      Set<String> semanticTypeSet = ev.getConceptInfo().getSemanticTypeSet();
	      logger.debug("entity has semantic type set: " + semanticTypeSet );
	      // retainAll for intersection test? that's intuitive! (NOTE: retainAll is destructive.)
	      HashSet<String> intersectionSet = new HashSet<String>(semanticTypeSet);
	      intersectionSet.retainAll(semanticGroup);
	      if (intersectionSet.size() > 0)
		{
		  logger.info("added ev instance with semantic type set: " + 
			       ev.getConceptInfo().getSemanticTypeSet() );
		  logger.info("ev: " + ev);
		  newEvList.add(ev);
		  break;
		}
	    }
	  } /* for evlist */
	  if (newEvList.size() > 0) {
	    entity.setEvList(newEvList);
	    filteredAnnotationList.add(biocEntity);
	  }
	} /* for entitySet */
      } else {
	filteredAnnotationList.add(annotation);
      }
    }
    return filteredAnnotationList;
  }

  public static BioCSentence keepEntitiesInSemanticGroup(Set<String> semanticGroup, BioCSentence sentence) {
    List<BioCAnnotation> filteredAnnotationList = 
      keepEntitiesInSemanticGroup(semanticGroup, sentence.getAnnotations());
    BioCSentence newSentence = new BioCSentence(sentence);
    // newSentence.setAnnotations(filteredAnnotationList);
    for (BioCAnnotation anAnnotation: filteredAnnotationList) {
      newSentence.addAnnotation(anAnnotation);
    }
    return newSentence;
  }

  public static BioCSentence keepEntitiesInSemanticGroup(BioCSentence sentence) {
    return keepEntitiesInSemanticGroup(SemanticGroups.getDisorders(), sentence);
  }
}
