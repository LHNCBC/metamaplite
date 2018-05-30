package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;

/**
 * Describe class ConceptInfoUtils here.
 *
 *
 * Created: Thu Apr 19 11:12:47 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class ConceptInfoUtils {

  /**
   * Is intersection of concept semantic type set and semantic type restrict set not empty?
   * @param conceptSemanticTypeSet concept semantic type set
   * @param semanticTypeRestrictSet semantic type restrict set 
   * @return true if at least one semantic type in concept semantic
   * type set is in semantic type restrict set.
   */
  public static boolean inSemanticTypeRestrictSet(Set<String> conceptSemanticTypeSet, Set<String> semanticTypeRestrictSet)
  {
    // empty or all then use semantic types
    if (semanticTypeRestrictSet.isEmpty() || semanticTypeRestrictSet.contains("all")) {
      return true;
    } else {
      boolean inSet = false;
      for (String semtype: conceptSemanticTypeSet) {
	inSet = inSet || semanticTypeRestrictSet.contains(semtype);
      }
      return inSet;
    }
  }

  /**
   * Is intersection of concept source set and source restrict set not empty?
   * @param conceptSourceSet concept source set
   * @param sourceRestrictSet source restrict set 
   * @return true if at least one source in concept source set is in
   * source restrict set.
   */
  public static boolean inSourceRestrictSet(Set<String> conceptSourceSet, Set<String> sourceRestrictSet)
  {
    // empty or all then use sources
    if (sourceRestrictSet.isEmpty() || sourceRestrictSet.contains("all")) {
      return true;
    } else {
      boolean inSet = false;
      for (String semtype: conceptSourceSet) {
	inSet = inSet || sourceRestrictSet.contains(semtype);
      }
      return inSet;
    }
  } 

  /** 
   * Filter ev instances for entity by semantic type,
   * remove any ev instances not in semantic type restrict set.
   * @param entity entity 
   * @param semanticTypeRestrictSet semantic type restrict set
   */
  public static void filterEntityEvListBySemanticType(Entity entity, Set<String> semanticTypeRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      Set<String> conceptSemanticTypeSet = ev.getConceptInfo().getSemanticTypeSet();
      if (inSemanticTypeRestrictSet(conceptSemanticTypeSet, semanticTypeRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }

  /** filter ev instances for entity by source
   * remove any ev instances not in source restrict set.
   * @param entity entity 
   * @param sourceRestrictSet source restrict set
   */
  public static void filterEntityEvListBySource(Entity entity, Set<String> sourceRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      Set<String> conceptSourceSet = ev.getConceptInfo().getSourceSet();
      if (inSourceRestrictSet(conceptSourceSet, sourceRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }
}
