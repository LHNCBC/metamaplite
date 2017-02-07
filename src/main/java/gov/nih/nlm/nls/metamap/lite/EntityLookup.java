
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.Set;
import bioc.BioCAnnotation;
import bioc.BioCPassage;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

/**
 *
 */

public interface EntityLookup {
  /**
   * Lookup term
   * <p>
   * Term is automatically assigned noun as part of speech.
   *
   * @param term term containing one or more words to looked up in dictionary.
   * @param semTypeRestrictSet retained concepts must have this set of semantic types, if empty than all concepts are retained.
   * @param sourceRestrictSet  retained concepts must be from this set of source, if empty than all concepts are retained.
   * @return entityList list of entities found.
   */
  List<Entity> lookupTerm(String term,
			  Set<String> semTypeRestrictSet,
			  Set<String> sourceRestrictSet);
  /**
   * Process Passage
   * 
   * @param docid document identifier for passage
   * @param passage BioCPassage instance contains content to processed.
   * @param useContext use ConText or other negation detector 
   * @param semTypeRestrictSet retained concepts must have this set of semantic types, if empty than all concepts are retained.
   * @param sourceRestrictSet  retained concepts must be from this set of source, if empty than all concepts are retained.
   * @return entityList list of entities found.
   */
  List<Entity> processPassage(String docid, BioCPassage passage, boolean useContext,
			      Set<String> semTypeRestrictSet,
			      Set<String> sourceRestrictSet);
  Set<BioCAnnotation> generateBioCEntitySet(String docid, List<ERToken> tokenList);
}
