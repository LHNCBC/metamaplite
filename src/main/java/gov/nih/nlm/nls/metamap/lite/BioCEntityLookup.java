
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import bioc.BioCSentence;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public interface BioCEntityLookup 
{
  /** 
   * Process sentence containing token and part-of-speech (postag)
   * annotations finding entities. Return set of entities.
   *
   * @param docid document id of parent BioCDocument of sentence
   * @param tokenizedSentence with token and part-of-speech annotations.
   * @return set of entities corresponding to sentence.
   */
  Set<Entity> listEntities(String docid, BioCSentence tokenizedSentence);

  /** 
   * Process sentence containing token and part-of-speech (postag)
   * annotations finding entities. Return sentence with entity and
   * concept annotations with linking relations. 
   *
   * @param docid document id of parent BioCDocument of sentence
   * @param tokenizedSentence with token and part-of-speech annotations.
   * @return BioCSentence with entity and concept annotations with linking relations.
   */
  BioCSentence findLongestMatches(String docid, BioCSentence tokenizedSentence);

  /** 
   * Process sentence containing token and part-of-speech (postag)
   * annotations finding entities. Return sentence with entity and
   * concept annotations with linking relations. 
   *
   * @param docid document id of parent BioCDocument of sentence
   * @param sentence with token and part-of-speech annotations.
   * @param semTypeRestrictSet keep only entities with concepts that belong to semantic type set.
   * @param sourceRestrictSet keep only entities with concepts from source set.
   * @return BioCSentence with entity and concept annotations with linking relations.
   */
  BioCSentence findLongestMatches(String docid,
				  BioCSentence sentence,
				  Set<String> semTypeRestrictSet,
				  Set<String> sourceRestrictSet);
}
