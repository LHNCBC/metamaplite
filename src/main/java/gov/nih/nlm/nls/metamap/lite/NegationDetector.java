package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import bioc.BioCSentence;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

public interface NegationDetector {
  void initProperties(Properties properties);
  void detectNegations(Set<Entity> entitySet, String sentence, List<ERToken> tokenList);
}
