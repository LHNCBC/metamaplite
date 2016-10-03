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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Example of using MetaMapLite from Java
 */

public class Example2 {
 /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(Example2.class);

  /**
   * Main program
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws Exception, IOException, ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException
  {

    // Initialization Section
    Properties myProperties = MetaMapLite.getDefaultConfiguration();
    myProperties.setProperty("opennlp.models.directory", 
			     "/export/home/wjrogers/Projects/metamaplite/data/models");
    MetaMapLite.expandModelsDir(myProperties);
    myProperties.setProperty("metamaplite.index.directory",
			     "/export/home/wjrogers/Projects/metamaplite/data/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
			     "/export/home/wjrogers/Projects/metamaplite/data/specialterms.txt");
    MetaMapLite.expandIndexDir(myProperties);
    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);

    // Processing Section

    // Each document must be instantiated as a BioC document before processing
    BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning for the type 2 diabetes medicine canagliflozin (Invokana, Invokamet) related to the increased risk of bone fractures, and added new information about decreased bone mineral density. To address these safety concerns, FDA added a new Warning and Precaution and revised the Adverse Reactions section of the Invokana and Invokamet drug labels.");
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
