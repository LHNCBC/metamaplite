package gov.nih.nlm.nls.metamap.lite;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

/**
 * Describe class CuiSourceSetIndex here.
 *
 *
 * Created: Fri Mar 24 12:34:38 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CuiSourceSetIndex {
  /** inverted file indexes */
  public MetaMapIvfIndexes mmIndexes;

  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		

  /**
   * Creates a new <code>CuiSourceSetIndex</code> instance.
   * @param mmIndexes container for metamap lite indexes
   */
  public CuiSourceSetIndex(MetaMapIvfIndexes mmIndexes)
  {
    this.mmIndexes = mmIndexes;
  }

  /**
   * Get source vocabulary abbreviations for cui (concept unique identifier)
   * @param cui target cui
   * @return set of source vocabulary abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   */
  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> sourceSet = new HashSet<String>();
    List<String> hitList =
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, cuiColumn);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      sourceSet.add(fields[4]);
    }
    return sourceSet;
  }
}
