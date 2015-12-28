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
 * Example of using metaMapLite from Java
 */

public class Example {
 /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(MetaMapLite.class);

  /**
   * Main program
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws Exception, IOException, ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException
  {
    Properties myProperties = MetaMapLite.getDefaultConfiguration();
    MetaMapLite.expandModelsDir(myProperties,
				"/export/home/wjrogers/Projects/metamaplite/data/models");
    MetaMapLite.expandIndexDir(myProperties,
			       "/export/home/wjrogers/Projects/metamaplite/data/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
			     "/export/home/wjrogers/Projects/metamaplite/data/specialterms.txt");
    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);
  
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
