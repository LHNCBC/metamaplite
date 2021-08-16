package gov.nih.nlm.nls.metamap.lite.metamap;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Properties;

import irutils.MultiKeyIndex;
import irutils.MultiKeyIndexLookup;

/**
 * Tables from MetaMap imported into inverted files.
 */

public class MetaMapIvfDiskIndexes {
  /** cui -&gt; source index */
  public MultiKeyIndexLookup cuiSourceInfoIndex;
  /** cui -&gt; semantic type index */
  public MultiKeyIndexLookup cuiSemanticTypeIndex;
  /** cui -&gt; concept index (actually bi-directional) */
  public MultiKeyIndexLookup cuiConceptIndex;
  /** term/cat/word/cat/level/history index */
  public MultiKeyIndexLookup varsIndex;
  /** MeSh Treecodes Relaxed Model index */
  public MultiKeyIndexLookup meshTcRelaxedIndex;

  static String defaultRoot =
    // "/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea/wjrogers/data/mult-key-index/strict/indices";
    "data/ivf/strict";

  public String root = defaultRoot;
  

  public MetaMapIvfDiskIndexes() 
    throws IOException, FileNotFoundException
  {
    // indexes
    this.cuiSourceInfoIndex = 
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuisourceinfoindex", defaultRoot + "/indices/cuisourceinfo")));
    this.cuiSemanticTypeIndex =
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuisemantictypeindex", defaultRoot + "/indices/cuist")));
    this.cuiConceptIndex =
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (System.getProperty("metamaplite.ivf.cuiconceptindex", defaultRoot + "/indices/cuiconcept")));

    if (Files.exists(Paths.get(System.getProperty("metamaplite.ivf.varsindex", defaultRoot + "/indices/vars")))) {
      this.varsIndex =
	new MultiKeyIndexLookup
	(new MultiKeyIndex
	 (System.getProperty("metamaplite.ivf.varsindex", defaultRoot + "/indices/vars")));
    }
    
    if (Files.exists(Paths.get(System.getProperty("metamaplite.ivf.meshtcrelaxedindex", defaultRoot + "/indices/meshtcrelaxed")))) {
      this.meshTcRelaxedIndex =
	new MultiKeyIndexLookup
	(new MultiKeyIndex
	 (System.getProperty("metamaplite.ivf.meshtcrelaxedindex", defaultRoot + "/indices/meshtcrelaxed")));
    }
    this.root = System.getProperty("metamaplite.index.directory", defaultRoot);
  }

  public MetaMapIvfDiskIndexes(Properties properties) 
    throws IOException, FileNotFoundException
  {
    // indexes
    this.cuiSourceInfoIndex =
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuisourceinfoindex", defaultRoot + "/indices/cuisourceinfo")));
    this.cuiSemanticTypeIndex =
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuisemantictypeindex", defaultRoot + "/indices/cuist")));
    this.cuiConceptIndex =
      new MultiKeyIndexLookup
      (new MultiKeyIndex
       (properties.getProperty("metamaplite.ivf.cuiconceptindex", defaultRoot + "/indices/cuiconcept")));

    if (Files.exists(Paths.get(properties.getProperty("metamaplite.ivf.varsindex", defaultRoot + "/indices/vars")))) {
      this.varsIndex =
	new MultiKeyIndexLookup
	(new MultiKeyIndex
	 (properties.getProperty("metamaplite.ivf.varsindex", defaultRoot + "/indices/vars")));
    }

    if (Files.exists(Paths.get(properties.getProperty("metamaplite.ivf.meshtcrelaxedindex", defaultRoot + "/indices/meshtcrelaxed")))) {
      this.meshTcRelaxedIndex =
	new MultiKeyIndexLookup
	(new MultiKeyIndex
	 (properties.getProperty("metamaplite.ivf.meshtcrelaxedindex", defaultRoot + "/indices/meshtcrelaxed")));
    }
    this.root = properties.getProperty("metamaplite.index.directory", defaultRoot);
  }

  
  /**
   * Get root path of index
   * @return path of index root directory.
   */  
  public String getRoot() {
    return this.root;
  }

  /** get cui -&gt; source info index 
   * @return lookup class for index. */
  public MultiKeyIndexLookup getCuiSourceInfoIndex() { return this.cuiSourceInfoIndex; }

  /** get cui -&gt; semantic type index
   * @return lookup class for index. */
  public MultiKeyIndexLookup getCuiSemanticTypeIndex() { return this.cuiSemanticTypeIndex; }

  /** get cui -&gt; concept index (actually bi-directional)
   * @return lookup class for index. */
  public MultiKeyIndexLookup getCuiConceptIndex() { return this.cuiConceptIndex; }

  /** get term/cat/word/cat/level/history index
   * @return lookup class for index. */
  public MultiKeyIndexLookup getVarsIndex() { return this.varsIndex; }

  /** get MeSH Treecodes Relaxed Model index
   * @return lookup class for index. */
  public MultiKeyIndexLookup getMeshTcRelaxedIndex() { return this.meshTcRelaxedIndex; }

}
