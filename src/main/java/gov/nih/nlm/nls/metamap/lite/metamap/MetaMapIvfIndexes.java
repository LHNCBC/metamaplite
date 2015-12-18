package gov.nih.nlm.nls.metamap.lite.metamap;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Properties;

import irutils.MappedMultiKeyIndex;
import irutils.MappedMultiKeyIndexLookup;

/**
 * Tables from MetaMap imported into inverted files.
 */

public class MetaMapIvfIndexes {
  /** cui -&gt; source index */
  public MappedMultiKeyIndexLookup cuiSourceInfoIndex;
  /** cui -&gt; semantic type index */
  public MappedMultiKeyIndexLookup cuiSemanticTypeIndex;
  /** cui -&gt; concept index (actually bi-directional) */
  public MappedMultiKeyIndexLookup cuiConceptIndex;

  String root =
    // "/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea/wjrogers/data/mult-key-index/strict/indices";
    "data/ivf/strict/indices";

  public MetaMapIvfIndexes() 
    throws IOException, FileNotFoundException
  {
    // indexes
    this.cuiSourceInfoIndex = 
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuisourceinfoindex", root + "/cuisourceinfo")));
    this.cuiSemanticTypeIndex =
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuisemantictypeindex", root + "/cuist")));
    this.cuiConceptIndex =
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuiconceptindex", root + "/cuiconcept")));

  }

  public MetaMapIvfIndexes(Properties properties) 
    throws IOException, FileNotFoundException
  {
    // indexes
    this.cuiSourceInfoIndex =
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuisourceinfoindex", root + "/cuisourceinfo")));
    this.cuiSemanticTypeIndex =
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuisemantictypeindex", root + "/cuist")));
    this.cuiConceptIndex =
      new MappedMultiKeyIndexLookup
      (new MappedMultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuiconceptindex", root + "/cuiconcept")));
  }

}
