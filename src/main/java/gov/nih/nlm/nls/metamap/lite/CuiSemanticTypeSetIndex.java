package gov.nih.nlm.nls.metamap.lite;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

/**
 * Describe class CuiSemanticTypeIndex here.
 *
 *
 * Created: Fri Mar 24 12:41:08 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CuiSemanticTypeSetIndex {

  /** inverted file indexes */
  public MetaMapIvfIndexes mmIndexes;

  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		

  /**
   * Creates a new <code>CuiSemanticTypeSetIndex</code> instance.
   * @param mmIndexes metamaplite inverted file indexes container
   */
  public CuiSemanticTypeSetIndex(MetaMapIvfIndexes mmIndexes) {
    this.mmIndexes = mmIndexes;
  }

  /**
   * Get semantic type set for cui (concept unique identifier)
   * @param cui target cui
   * @return set of semantic type abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public Set<String> getSemanticTypeSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> semanticTypeSet = new HashSet<String>();
    List<String> hitList = 
      this.mmIndexes.cuiSemanticTypeIndex.lookup(cui, this.cuiColumn);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      semanticTypeSet.add(fields[1]);
    }
    return semanticTypeSet;
  }
}
