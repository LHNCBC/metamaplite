//
package gov.nih.nlm.nls.metamap.lite;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import javax.xml.stream.XMLStreamException;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.io.BioCCollectionReader;
import bioc.io.BioCCollectionWriter;
import bioc.io.BioCFactory;
import bioc.io.standard.BioCFactoryImpl;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.Plugin;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PipelineRegistry;
import gov.nih.nlm.nls.metamap.lite.BioCEntityLookup;
import gov.nih.nlm.nls.metamap.lite.BioCLRLongestMatchLookup;
import gov.nih.nlm.nls.metamap.lite.BioCUtilities;

import gov.nih.nlm.nls.utils.Configuration;

/**
 *
 */

public class BioCProcess {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(BioCProcess.class);

  Set<String> semanticGroup = new HashSet<String>(); // initially empty
  Set<String> sourceSet = new HashSet<String>(); // initially empty

  BioCEntityLookup bioCEntityLookup;
  SentenceAnnotator sentenceAnnotator;
  SentenceExtractor sentenceExtractor;

  public BioCProcess()
    throws IOException
  {
    this.setSemanticGroup(System.getProperty("metamaplite.semanticgroup", "all").split(","));
    this.setSourceSet(System.getProperty("metamaplite.sourceset","all").split(","));
    
    bioCEntityLookup = new BioCLRLongestMatchLookup();
  }

  public BioCProcess(Properties properties)
    throws IOException
  {
    this.setSemanticGroup(properties.getProperty("metamaplite.semanticgroup", "all").split(","));
    this.setSourceSet(properties.getProperty("metamaplite.sourceset","all").split(","));
    this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
    bioCEntityLookup = new BioCLRLongestMatchLookup(properties, this.sentenceAnnotator);
  }

  /**
   * Set list of semantic types concepts must belong to be retrieved.
   * @param semanticTypeList list of semantic type strings
   */
  public void setSemanticGroup(String[] semanticTypeList) {
    this.semanticGroup = new HashSet<String>(Arrays.asList(semanticTypeList));
  }

  /**
   * Set list of sources concepts must belong to be retrieved.
   * @param sourceList list of source strings
   */
  public void setSourceSet(String[] sourceList) {
    this.sourceSet = new HashSet<String>(Arrays.asList(sourceList));
  }

  /**
   * Invoke sentence processing pipeline on asentence
   * @param docid document identifier
   * @param sentence BioC sentence
   * @param semanticGroup semantic type group set
   * @param sourceSet vocabulary source set.
   * @return updated sentence
   * @throws IOException IO Exception
   * @throws FileNotFoundException File Not Found Exception
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class 
   */
  public BioCSentence processSentence(String docid,
				      BioCSentence sentence,
				      Set<String> semanticGroup,
				      Set<String> sourceSet)
    throws IllegalAccessException, InvocationTargetException,
	   IOException, FileNotFoundException
  {
    logger.debug("enter processSentence");
    // List<Plugin> pipeSequence = PipelineRegistry.get("simple.sentence");
    // Object current = sentence;
    // Object resultObject = null;
    // for (Plugin plugin: pipeSequence) {
    //   // resultObject = plugin.getMethod().invoke(plugin.getClassInstance(), current);
    //   current = resultObject;
    // }
    // if (resultObject instanceof BioCSentence) {
    //   result = (BioCSentence)resultObject;
    // }
    BioCSentence taggedSentence = BioCUtilities.tokenizeSentence(sentence);
    this.sentenceAnnotator.addPartOfSpeech(taggedSentence);
    BioCSentence entityTaggedSentence = bioCEntityLookup.findLongestMatches(docid,
									    taggedSentence,
									    semanticGroup,
									    sourceSet);
    return entityTaggedSentence;
  }
  
  /**
   * Invoke sentence processing pipeline on each sentence in supplied sentence list.
   * @param docid document identifier
   * @param passage containing list of sentences
   * @return list of results from sentence processing pipeline, one per sentence in input list.
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class 
   */
  public BioCPassage processSentences(String docid, BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException,
	   IOException, FileNotFoundException
  {
    logger.debug("enter processSentences");
    BioCPassage newPassage = new BioCPassage();
    newPassage.setInfons(passage.getInfons());
    newPassage.setText(passage.getText());
    newPassage.setOffset(passage.getOffset());
    List<BioCSentence> resultList = new ArrayList<BioCSentence>();
    for (BioCSentence sentence: passage.getSentences()) {
      logger.info("Processing: " + sentence.getText());
      resultList.add(processSentence("txt",
				     sentence, 
				     this.semanticGroup,
				     this.sourceSet));
    }
    logger.debug("exit processSentences");
    // passage.setSentences(resultList);  // BioC 1.0.1
    for (BioCSentence sentence: resultList) {
      newPassage.addSentence(sentence);
    }
    return newPassage;
  }

    public BioCPassage processPassage(String docid, BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException,
	   IOException, FileNotFoundException 
  {
    logger.debug("enter processPassage");
    BioCPassage newPassage = processSentences(docid, this.sentenceExtractor.createSentences(passage));
    logger.debug("exit processPassage");
    return newPassage;
  }

  public BioCDocument processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException,
	   IOException, FileNotFoundException 
  {
    BioCDocument newDocument = new BioCDocument();
    for (BioCPassage passage: document.getPassages()) {
      logger.info(passage.getText());
      newDocument.addPassage(this.processPassage(document.getID(), passage));
    }
    return newDocument;
  }
  
  public BioCCollection processCollection(BioCCollection collection)
    throws IllegalAccessException, InvocationTargetException, IOException
  {
    BioCCollection newCollection = new BioCCollection();
    newCollection.setKey(collection.getKey());
    newCollection.setSource(collection.getSource());
    newCollection.setInfons(collection.getInfons());
    for (BioCDocument document: collection.getDocuments()) {
      BioCDocument newDocument = this.processDocument(document);
      newDocument.setInfons(document.getInfons());
      newCollection.addDocument(newDocument);
    }
    return newCollection;
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
    defaultConfiguration.setProperty("metamaplite.usecontext", "true");
    defaultConfiguration.setProperty("metamaplite.segment.sentences", "true");

    defaultConfiguration.setProperty("opennlp.en-sent.bin.path", 
				     modelsDirectory + "/en-sent.bin");
    defaultConfiguration.setProperty("opennlp.en-token.bin.path",
				     modelsDirectory + "/en-token.bin");
    defaultConfiguration.setProperty("opennlp.en-pos.bin.path",
				     modelsDirectory + "/en-pos-maxent.bin");

    defaultConfiguration.setProperty("metamaplite.ivf.cuiconceptindex", 
				     indexDirectory + "/strict/indices/cuiconcept");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisourceinfoindex", 
				     indexDirectory + "/strict/indices/cuisourceinfo");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisemantictypeindex", 
				     indexDirectory + "/strict/indices/cuist");
      
    defaultConfiguration.setProperty("bioc.document.loader.chemdner",
				     "gov.nih.nlm.nls.metamap.document.ChemDNER");
    defaultConfiguration.setProperty("bioc.document.loader.freetext",
				     "gov.nih.nlm.nls.metamap.document.FreeText");
    defaultConfiguration.setProperty("bioc.document.loader.ncbicorpus",
				     "gov.nih.nlm.nls.metamap.document.NCBICorpusDocument");
    return defaultConfiguration;
  }

  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException, XMLStreamException {
    if (args.length > 1) {
      String inputFilename = args[0];
      String outputFilename = args[1];
      File inputFile = new File(inputFilename);
      if (inputFile.exists()) {
        // Initialize MetaMapLite
	Properties defaultConfiguration = getDefaultConfiguration();
	String configPropertiesFilename = System.getProperty("metamaplite.propertyfile",
							     "config/metamaplite.properties");
	Properties configProperties = new Properties();
	FileReader fr = new FileReader(configPropertiesFilename);
	configProperties.load(fr);
	fr.close();
	Properties properties =
	  Configuration.mergeConfiguration(configProperties,
					   defaultConfiguration);
	BioCProcess process = new BioCProcess(properties);
	System.out.println("semantic group: " + process.semanticGroup);
	
	// read BioC XML collection
	Reader inputReader = new FileReader(inputFile);
	BioCFactory bioCFactory = BioCFactory.newFactory("STANDARD");
	BioCCollectionReader collectionReader = bioCFactory.createBioCCollectionReader(inputReader);
	BioCCollection collection = collectionReader.readCollection();

	// Run named entity recognition on collection
	BioCCollection newCollection = process.processCollection(collection);

	// write out the annotated collection
	File outputFile = new File(outputFilename);
	Writer outputWriter = new PrintWriter(outputFile, "UTF-8");
	BioCCollectionWriter collectionWriter = bioCFactory.createBioCCollectionWriter(outputWriter);
	collectionWriter.writeCollection(newCollection);
	outputWriter.close();
      }
    } else {
      System.out.println("usage: BioCProcess bio-c-xml-input-file bio-c-xml-output-file");
    }    
  }
}
