package examples;

import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import gov.nih.nlm.nls.metamap.lite.Normalization;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.ner.MetaMapLite;

/**
 * Example of looking up terms while skipping the recognition step.
 *
 *
 * Created: Tue Apr 11 16:31:38 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class DirectLookup {

  Properties myProperties;
  MetaMapIvfIndexes mmIndexes;
  
  /**
   * Creates a new <code>DirectLookup</code> instance.
   * @throws IOException I/O exception
   */
  public DirectLookup()
    throws IOException
  {
    this.myProperties = MetaMapLite.getDefaultConfiguration();
    MetaMapLite.expandModelsDir(this.myProperties, "data/models");
    MetaMapLite.expandIndexDir(this.myProperties,
			       System.getProperty("metamaplite.index.directory",
						  "data/ivf/2016AB/USAbase/strict"));
    this.myProperties.setProperty("metamaplite.excluded.termsfile", "data/specialterms.txt");
    // Loading properties file in "config", overriding previously
    // defined properties.
    FileReader fr = new FileReader("config/metamaplite.properties");
    myProperties.load(fr);
    fr.close();
    this.mmIndexes = new MetaMapIvfIndexes(this.myProperties);
  }

  public List<String[]> lookup(String term)
    throws IOException
  {
    List<String[]> hitList = new ArrayList<String[]>();
    for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(term, 3)) {
      String[] fields = doc.split("\\|");
      String cui = fields[0];
      String docStr = fields[3];
      hitList.add(fields);
    }
    return hitList;
  }

    /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
   * @exception IOException i/o exception
   */
  public static final void main(final String[] args)
    throws IOException
  {

    if (args.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (String arg: args) {
	sb.append(arg).append(" ");
      }
      DirectLookup instance = new DirectLookup();
      int i = 0;
      for (String term: sb.toString().split(",")) {
	System.out.println("term:" + term);
	for (String[] hit: instance.lookup(Normalization.normalizeLiteString(term))) {
	  System.out.println(i + ": " + Arrays.toString(hit));
	  i++;
	}
	System.out.println("i =" + i);
      }
    } else {
      System.err.println("examples.DirectLookup multi-word-term");
      System.err.println(" Terms are separated by commas.");
    }
  }
}
