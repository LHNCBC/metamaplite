//
package examples;
import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.ner.MetaMapLite;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.io.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of using metaMapLite from Java
 */

public class Example {
 /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(Example.class);

  /**
   * Main program
   * @param args - Arguments passed from the command line
   * @throws Exception General exception
   * @throws ClassNotFoundException Class Not Found Exception
   * @throws IOException IO Exception
   * @throws NoSuchMethodException  no method in class
   * @throws IllegalAccessException illegal access of class
   * @throws InstantiationException exception while instantiating class 
   * @throws InvocationTargetException invocation target exception
   */
  public static void main(String[] args)
    throws Exception, IOException, ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException
  {
    // Initialization Section
    Properties myProperties = MetaMapLite.getDefaultConfiguration();
    MetaMapLite.expandModelsDir(myProperties,
				"/export/home/wjrogers/Projects/metamaplite/data/models");
    MetaMapLite.expandIndexDir(myProperties,
			       "/export/home/wjrogers/Projects/metamaplite/data/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
			     "/export/home/wjrogers/Projects/metamaplite/data/specialterms.txt");
    // Loading properties file in "config", overriding previously
    // defined properties.
    FileReader fr = new FileReader("config/metamaplite.properties");
    myProperties.load(fr);
    fr.close();

    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);
  
    // Processing Section

    // Each document must be instantiated as a BioC document before processing
    BioCDocument document = FreeText.instantiateBioCDocument("diabetes");
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    documentList.add(document);
    List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvSet()) {
	System.out.print(ev.getConceptInfo().getCUI() + "|" + entity.getMatchedText());
	System.out.println();
      }
    }
  }
}
