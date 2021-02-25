package examples;

//
/**
 * Example of using listAcronyms
 */

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;
import gov.nih.nlm.nls.ner.MetaMapLite;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.types.Sentence;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAcronyms {

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
    if (args.length > 0) {
      String filename = args[0];

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
      // override any properties with those in property file.
      FileReader fr = new FileReader("config/metamaplite.properties");
      myProperties.load(fr);
      fr.close();
      SentenceExtractor sentenceExtractor = new OpenNLPSentenceExtractor(myProperties);
      ExtractAbbrev extractAbbr = new ExtractAbbrev();
      // Processing Section
      
      List<BioCDocument> documentList = FreeText.loadFreeTextFile(filename);
      List <AbbrInfo> infos = new ArrayList<AbbrInfo>();
      for (BioCDocument doc: documentList) {
	for (BioCPassage passage: doc.getPassages()) {
	  for (Sentence sentence: sentenceExtractor.createSentenceList(passage.getText())) {
	    infos.addAll(extractAbbr.extractAbbrPairsString(sentence.getText()));
	  }
	}
      }
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
      for (AbbrInfo acronym: infos) {
	pw.println(acronym.shortForm + "|" + acronym.shortFormIndex + "|" +
		   acronym.longForm + "|" + acronym.longFormIndex );
      }
      pw.flush();
    } else {
      System.out.println("examples.ListAcronyms filename");
    }
  }
}
