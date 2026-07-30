package gov.nih.nlm.nls.ner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

import gov.nih.nlm.nls.utils.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe class MetaMapLiteConfiguration here.
 *
 *
 * Created: Wed Mar 13 13:55:48 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MetaMapLiteConfiguration {
/** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(MetaMapLiteConfiguration.class);
  /**
   * Creates a new <code>MetaMapLiteConfiguration</code> instance.
   *
   */
  public MetaMapLiteConfiguration() {

  }
  
  public static Properties getDefaultConfiguration() {
    String modelsDirectory = System.getProperty("opennlp.models.directory", "data/models");
    String indexDirectory = System.getProperty("metamaplite.index.directory", "data/ivf/strict");
    Properties defaultConfiguration = new Properties();
    defaultConfiguration.setProperty("metamaplite.excluded.termsfile",
				     System.getProperty("metamaplite.excluded.termsfile",
							"data/specialterms.txt"));
    defaultConfiguration.setProperty("opennlp.models.directory", modelsDirectory);
    defaultConfiguration.setProperty("metamaplite.index.directory", indexDirectory);
    defaultConfiguration.setProperty("metamaplite.document.inputtype", "freetext");
    defaultConfiguration.setProperty("metamaplite.outputformat", "mmi");
    defaultConfiguration.setProperty("metamaplite.outputextension",  ".mmi");
    defaultConfiguration.setProperty("metamaplite.semanticgroup", "all");
    defaultConfiguration.setProperty("metamaplite.sourceset", "all");
    defaultConfiguration.setProperty("metamaplite.segmentation.method", "SENTENCES");

    defaultConfiguration.setProperty("opennlp.en-sent.bin.path", 
				     modelsDirectory + "/en-sent.bin");
    defaultConfiguration.setProperty("opennlp.en-token.bin.path",
				     modelsDirectory + "/en-token.bin");
    defaultConfiguration.setProperty("opennlp.en-pos.bin.path",
				     modelsDirectory + "/en-pos-maxent.bin");
    defaultConfiguration.setProperty("opennlp.en-chunker.bin.path",
				     modelsDirectory + "/en-chunker.bin");

    defaultConfiguration.setProperty("metamaplite.ivf.cuiconceptindex", 
				     indexDirectory + "/strict/indices/cuiconcept");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisourceinfoindex", 
				     indexDirectory + "/strict/indices/cuisourceinfo");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisemantictypeindex", 
				     indexDirectory + "/strict/indices/cuist");
    defaultConfiguration.setProperty("metamaplite.ivf.varsindex", 
				     indexDirectory + "/strict/indices/varss");
    defaultConfiguration.setProperty("metamaplite.ivf.meshtcrelaxedindex", 
				     indexDirectory + "/strict/indices/meshtcrelaxed");
      
    defaultConfiguration.setProperty("bioc.document.loader.chemdner",
				     "gov.nih.nlm.nls.metamap.document.ChemDNER");
    defaultConfiguration.setProperty("bioc.document.loader.freetext",
				     "gov.nih.nlm.nls.metamap.document.FreeText");
    defaultConfiguration.setProperty("bioc.document.loader.ncbicorpus",
				     "gov.nih.nlm.nls.metamap.document.NCBICorpusDocument");
    defaultConfiguration.setProperty("bioc.document.loader.sldi",
				     "gov.nih.nlm.nls.metamap.document.SingleLineInput");
    defaultConfiguration.setProperty("bioc.document.loader.sldiwi",
				     "gov.nih.nlm.nls.metamap.document.SingleLineDelimitedInputWithID");


    defaultConfiguration.setProperty("metamaplite.result.formatter.cuilist",
				     "gov.nih.nlm.nls.metamap.lite.resultformats.CuiList");
    defaultConfiguration.setProperty("metamaplite.result.formatter.brat",
				     "gov.nih.nlm.nls.metamap.lite.resultformats.Brat");
    defaultConfiguration.setProperty("metamaplite.result.formatter.mmi",
				     "gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI");
    defaultConfiguration.setProperty("metamaplite.negation.detector",
				     "gov.nih.nlm.nls.metamap.lite.NegEx");
    return defaultConfiguration;
  }



  public static void expandModelsDir(Properties properties, String modelsDir) {
    if (modelsDir != null) {
      properties.setProperty("opennlp.en-sent.bin.path", modelsDir + "/en-sent.bin");
      properties.setProperty("opennlp.en-token.bin.path", modelsDir + "/en-token.bin");
      properties.setProperty("opennlp.en-pos.bin.path", modelsDir + "/en-pos-maxent.bin");
      properties.setProperty("opennlp.en-chunker.bin.path", modelsDir + "/en-chunker.bin");
    }
  }
  public static void expandModelsDir(Properties properties) {
    String modelsDir = properties.getProperty("opennlp.models.directory");
    expandModelsDir(properties, modelsDir);
  }
  public static void expandIndexDir(Properties properties, String indexDirName) {
    if (indexDirName != null) {
      properties.setProperty("metamaplite.ivf.cuiconceptindex", indexDirName + "/indices/cuiconcept");
      properties.setProperty("metamaplite.ivf.firstwordsofonewideindex", indexDirName + "/indices/first_words_of_one_WIDE");
      properties.setProperty("metamaplite.ivf.cuisourceinfoindex", indexDirName + "/indices/cuisourceinfo");
      properties.setProperty("metamaplite.ivf.cuisemantictypeindex", indexDirName + "/indices/cuist");
      properties.setProperty("metamaplite.ivf.varsindex", indexDirName + "/indices/vars");
      properties.setProperty("metamaplite.ivf.meshtcrelaxedindex", indexDirName + "/indices/meshtcrelaxed");
    }
  }
  public static void expandIndexDir(Properties properties) {
    String indexDirName = properties.getProperty("metamaplite.index.directory");
    expandIndexDir(properties, indexDirName);
  }
  
  public static Properties setConfiguration(String propertiesFilename,
					    Properties defaultConfiguration,
					    Properties systemConfiguration,
					    Properties optionsConfiguration,
					    boolean verbose)
    throws IOException, FileNotFoundException
  {
    // Attempt to get local configuration from properties file on
    // classpath and then from file system.  file system has
    // precedence of classpath and gets loaded last (if it exists).

    Properties localConfiguration = new Properties();
    // check classpath for "metamaplite.properties" resource
    // get class loader
    ClassLoader loader = MetaMapLiteConfiguration.class.getClassLoader();
    if(loader==null)
      loader = ClassLoader.getSystemClassLoader(); // use system class loader if class loader is null
    java.net.URL url = loader.getResource(propertiesFilename);
    try {
      localConfiguration.load(url.openStream());
    } catch(Exception e) {
      logger.debug("Could not load configuration file from classpath: " + propertiesFilename);
    }

    // check filesystem 
    File localConfigurationFile = new File(propertiesFilename);
    if (localConfigurationFile.exists()) {
      logger.debug("loading local configuration from " + localConfigurationFile);
      if (verbose) {
	System.out.println("loading local configuration from " + localConfigurationFile);
      }
      localConfiguration.load(new FileReader(localConfigurationFile));
      logger.debug("loaded " + localConfiguration.size() + " records from local configuration");
      if (verbose) {
	System.out.println("loaded " + localConfiguration.size() + " records from local configuration");
      }
    }
    expandModelsDir(defaultConfiguration);
    expandModelsDir(localConfiguration);
    expandModelsDir(systemConfiguration);
    expandModelsDir(optionsConfiguration);

    expandIndexDir(defaultConfiguration);
    expandIndexDir(localConfiguration);
    expandIndexDir(systemConfiguration);
    expandIndexDir(optionsConfiguration);

    // displayProperties("defaultConfiguration:", defaultConfiguration);
    // displayProperties("localConfiguration:", localConfiguration);
    // displayProperties("optionsConfiguration:", optionsConfiguration);

    Properties properties =
      Configuration.mergeConfiguration(defaultConfiguration,
				       localConfiguration,
				       systemConfiguration,
				       optionsConfiguration);
    return properties;
  }
}
