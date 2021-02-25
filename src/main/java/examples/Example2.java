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
 * Example of using MetaMapLite from Java
 */

public class Example2 {
 /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(Example2.class);

  /**
   * Main program
   * @param args - Arguments passed from the command line
   * @throws Exception general exception
   * @throws IOException IO Exception
   * @throws ClassNotFoundException class not found exception
   * @throws IllegalAccessException illegal access of class
   * @throws InstantiationException exception instantiating instance of class
   * @throws InvocationTargetException exception while invoking target class 
   * @throws NoSuchMethodException  no method in class
   */
  public static void main(String[] args)
    throws Exception, IOException, ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException
  {
    String dataRoot = "./data";
    // Initialization Section
    Properties myProperties = MetaMapLite.getDefaultConfiguration();
    myProperties.setProperty("opennlp.models.directory", 
			     dataRoot + "/models");
    MetaMapLite.expandModelsDir(myProperties);
    myProperties.setProperty("metamaplite.index.directory",
			     dataRoot + "/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
			     dataRoot + "/specialterms.txt");
    myProperties.setProperty("metamaplite.semanticgroup","phsu");
    MetaMapLite.expandIndexDir(myProperties);
    // Loading properties file in "config", overriding previously
    // defined properties.
    FileReader fr = new FileReader("config/metamaplite.properties");
    myProperties.load(fr);
    fr.close();
    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);

    // Processing Section

    // Each document must be instantiated as a BioC document before processing
    BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning for the type 2 diabetes medicine canagliflozin (Invokana, Invokamet) related to the increased risk of bone fractures, and added new information about decreased bone mineral density. To address these safety concerns, FDA added a new Warning and Precaution and revised the Adverse Reactions section of the Invokana and Invokamet drug labels.");
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    documentList.add(document);
    List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvSet()) {
	System.out.print(ev.getConceptInfo().getCUI() + "|" + ev.getConceptInfo().getSemanticTypeSet() + "|" + entity.getMatchedText());
	System.out.println();
      }
    }
  }

  
}
