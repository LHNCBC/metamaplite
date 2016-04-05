
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
  List<Entity> processPassage(String docid, BioCPassage passage, boolean useContext,
			      Set<String> semTypeRestrictSet,
			      Set<String> sourceRestrictSet);
  Set<BioCAnnotation> generateBioCEntitySet(String docid, List<ERToken> tokenList);
}
