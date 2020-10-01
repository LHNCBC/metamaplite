//
package examples;
import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.BioCPipeline;
import java.io.Reader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of using MetaMapLite from Java to output BioC document.
 */

public class BioCPipeLineExample {
 /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(BioCPipeLineExample.class);

  /**
   * Main program
   * @param args - Arguments passed from the command line
   * @throws Exception General Exception
   * @throws IOException IO Exception
   * @throws ClassNotFoundException thrown if class not found
   * @throws InstantiationException thrown if there is an error instantiating class
   * @throws NoSuchMethodException thrown if called method does not exist
   * @throws IllegalAccessException thrown if class is accessed illegally
   * @throws InvocationTargetException thrown if problem during instance method or class method invocation
   */
  public static void main(String[] args)
    throws Exception, IOException, ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException
  {

    // Initialization Section
    Properties myProperties = BioCPipeline.getDefaultConfiguration();
    Reader propReader = new FileReader("config/metamaplite.properties");
    myProperties.load(propReader);
    propReader.close();
    myProperties.setProperty("opennlp.models.directory", "data/models");
    BioCPipeline.expandModelsDir(myProperties);
    myProperties.setProperty("metamaplite.index.directory", "data/ivf/strict");
    BioCPipeline.expandIndexDir(myProperties);
    myProperties.setProperty("metamaplite.excluded.termsfile", "data/specialterms.txt");
    BioCPipeline metaMapLiteInst = new BioCPipeline(myProperties);

    // Processing Section

    // Each document must be instantiated as a BioC document before processing
    BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning for the type 2 diabetes medicine canagliflozin (Invokana, Invokamet) related to the increased risk of bone fractures, and added new information about decreased bone mineral density. To address these safety concerns, FDA added a new Warning and Precaution and revised the Adverse Reactions section of the Invokana and Invokamet drug labels.");
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    documentList.add(document);
    List<BioCDocument> annotatedDocumentList = metaMapLiteInst.processDocumentList(documentList);
    for (BioCDocument annotatedDocument: annotatedDocumentList) {
        System.out.print(annotatedDocument);
    }
  }

  
}
