
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import java.lang.reflect.InvocationTargetException;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.Plugin;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PluginRegistry;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PipelineRegistry;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.MarkAbbreviations;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.lite.SemanticGroupFilter;
import gov.nih.nlm.nls.metamap.lite.SemanticGroups;
import gov.nih.nlm.nls.metamap.lite.EntityAnnotation;
import gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.resultformats.Brat;
import gov.nih.nlm.nls.metamap.lite.resultformats.CuiList;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.ChemDNERSLDI;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;
import gov.nih.nlm.nls.metamap.document.SingleLineInput;
import gov.nih.nlm.nls.metamap.document.SingleLineDelimitedInputWithID;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoader;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoaderRegistry;
import gov.nih.nlm.nls.metamap.document.SemEvalDocument;
import gov.nih.nlm.nls.metamap.lite.context.ContextWrapper;
import gov.nih.nlm.nls.types.Sentence;

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nih.nlm.nls.utils.Configuration;

/**
 * Using BioCPipeline from a Java Program
 *
 * <pre>
 * Properties myProperties = BioCPipeline.getDefaultConfiguration();
 * myProperties.setProperty("opennlp.models.directory", 
 *                          "/Projects/metamaplite/data/models");
 * BioCPipeline.expandModelsDir(myProperties);
 * myProperties.setProperty("metamaplite.index.directory",
 * 		     "/Projects/metamaplite/data/ivf/strict");
 * myProperties.setProperty("metamaplite.excluded.termsfile",
 *			     "/Projects/metamaplite/data/specialterms.txt");
 * BioCPipeline.expandIndexDir(myProperties);
 * BioCPipeline pipelineInst = new BioCPipeline(myProperties);
 * BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning ...");
 * List&lt;BioCDocument&gt; documentList = new ArrayList&lt;BioCDocument&gt;();
 * documentList.add(document);
 * </pre>

 */

public class BioCPipeline {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(BioCPipeline.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/bioc.metamaplite.properties");
  static Map<String,String> outputExtensionMap = new HashMap<String,String>();
  static {
    outputExtensionMap.put("brat",".ann");
    outputExtensionMap.put("mmi",".mmi");
    outputExtensionMap.put("cdi",".cdi");
    outputExtensionMap.put("cuilist",".cuis");
  }

  Set<String> semanticGroup = new HashSet<String>(); // initially empty
  Set<String> sourceSet = new HashSet<String>(); // initially empty

  AbbrConverter abbrConverter = new AbbrConverter();
  static ExtractAbbrev extractAbbr = new ExtractAbbrev();
  static String entityLookupResultLengthString = "";
  Properties properties;

  boolean useContext = false;
  boolean detectNegationsFlag = false;
  SentenceAnnotator sentenceAnnotator;
  EntityLookup entityLookup;
  boolean segmentSentences = true;
  boolean segmentBlanklines = false;

public BioCPipeline(Properties properties)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException,
	   IOException
  {
    this.properties = properties;
    if (properties.getProperty("opennlp.en-sent.bin.path") != null) {
      SentenceExtractor.setModel(properties.getProperty("opennlp.en-sent.bin.path"));
    }
    this.sentenceAnnotator = new SentenceAnnotator(properties);
    this.entityLookup = new EntityLookup4(properties);
    BioCDocumentLoaderRegistry.register("freetext",
					"For freetext document that are grammatically well behaved.", 
					new FreeText());
    BioCDocumentLoaderRegistry.register("chemdner",
     					"ChemDNER format document sets",
     					new ChemDNER());
    BioCDocumentLoaderRegistry.register("chemdnersldi",
     					"ChemDNER single line delimited with id format document sets",
     					new ChemDNERSLDI());
    BioCDocumentLoaderRegistry.register("ncbicorpus",
     					"NCBI Disease Corpus format document sets",
     					new NCBICorpusDocument());
    // BioCDocumentLoaderRegistry.register("semeval14",
    // 					"SemEval Document (Almost FreeText)",
    // 					new SemEvalDocument());
    BioCDocumentLoaderRegistry.register("sli",
    					"Single Line Input document sets",
     					new SingleLineInput());
    BioCDocumentLoaderRegistry.register("sldi",
    					"Single Line Input document sets",
    					new SingleLineDelimitedInputWithID());
    /** augment or override any built-in formats with ones specified by property file. */
    BioCDocumentLoaderRegistry.register(properties);
  }

  void setSemanticGroup(String[] semanticTypeList) {
    this.semanticGroup = new HashSet<String>(Arrays.asList(semanticTypeList));
  }

  void setSourceSet(String[] sourceList) {
    this.sourceSet = new HashSet<String>(Arrays.asList(sourceList));
  }

  void setSegmentSentences(boolean status) {
    this.segmentSentences = status;
  }
  
  void setSegmentBlanklines(boolean status) {
    this.segmentBlanklines = status;
  }

  /**
   * Invoke sentence processing pipeline on asentence
   * @param sentence
   * @return updated sentence
   */
  public static BioCSentence processSentence(BioCSentence sentence)
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processSentence");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.sentence");
    Object current = sentence;
    Object resultObject = null;
    for (Plugin plugin: pipeSequence) {
      resultObject = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = resultObject;
    }
    BioCSentence result = null;
    logger.debug("exit processSentence");
    if (resultObject instanceof BioCSentence) {
      result = (BioCSentence)resultObject;
    }
    logger.debug("exit processSentence");
    return result;
 }

  /**
   * Invoke sentence processing pipeline on each sentence in supplied sentence list.
   * @param passage containing list of sentences
   * @return list of results from sentence processing pipeline, one per sentence in input list.
   */
  public static BioCPassage processSentences(BioCPassage passage) 
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processSentences");
    List<BioCSentence> resultList = new ArrayList<BioCSentence>();
    for (BioCSentence sentence: passage.getSentences()) {
      logger.info("Processing: " + sentence.getText());
      resultList.add(BioCPipeline.processSentence(sentence));
    }
    logger.debug("exit processSentences");
    // passage.setSentences(resultList);
    return passage;
  }

  public void processPassage(BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processPassage");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.passage");
    Object current = passage;
    for (Plugin plugin: pipeSequence) {
      Object result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
    // BioCPassage newPassage = BioCPipeline.processSentences(SentenceExtractor.createSentences(passage));
    logger.debug("exit processPassage");
  }

  public BioCDocument processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException
  {
    for (BioCPassage passage: document.getPassages()) {
      logger.info(passage.getText());
      this.processPassage(passage);
    }
    return document;
  }
  
  public List<BioCDocument> processDocumentList(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException
  {
    List<BioCDocument> newDocumentList = new ArrayList<BioCDocument>();
    for (BioCDocument document: documentList) {
      newDocumentList.add(this.processDocument(document));
    }
    return newDocumentList;
  }

  /**
   * Initialize pipeline application.
   * @return pipeline application instance
   */
  static BioCPipeline initPipeline()
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException {
    logger.debug("enter initPipeline");

    Properties properties = new Properties();
    properties.load(new FileReader(configPropertyFilename));
    if (logger.isDebugEnabled()) {
      for (Map.Entry<Object,Object> entry: properties.entrySet()) {
	logger.debug(entry.getKey() + " -> " + entry.getValue());
      }
    }
    BioCPipeline pipeline = new BioCPipeline(properties);
    PluginRegistry.registerPlugins(properties);
    logger.info("plugins:");
    for (String name: PluginRegistry.listPlugins()) {
      logger.info(" " + name);
    }
    logger.info("pipesequence keys:");
    PipelineRegistry.registerPipeSequences("metamaplite.pipeline", properties);
    for (String content: PipelineRegistry.listPipeContents()) {
      logger.info(" " + content);
    }
    logger.debug("exit initPipeline");
    return pipeline;
  }

  public static void expandModelsDir(Properties properties, String modelsDir) {
    if (modelsDir != null) {
      properties.setProperty("opennlp.en-sent.bin.path", modelsDir + "/en-sent.bin");
      properties.setProperty("opennlp.en-token.bin.path", modelsDir + "/en-token.bin");
      properties.setProperty("opennlp.en-pos.bin.path", modelsDir + "/en-pos-maxent.bin");
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
    }
  }
  public static void expandIndexDir(Properties properties) {
    String indexDirName = properties.getProperty("metamaplite.index.directory");
    expandIndexDir(properties, indexDirName);
  }
  


  static void displayHelp() {
    System.err.println("usage: [options] filename");

    System.err.println("input options:");
    System.err.println("  --              Read from standard input, write to standard output");
    System.err.println("  --pipe          Read from standard input, write to standard output");
    System.err.println("document processing options:");
    System.err.println("  --freetext (default)");
    System.err.println("  --inputformat=<document type>");
    System.err.println("    Available document types:");
    for (String name: BioCDocumentLoaderRegistry.listNameSet()) {
      System.err.println("      " + name);
    }

    System.err.println("processing options:");
    System.err.println("  --restrict_to_sts=<semtype>[,<semtype>...]");
    System.err.println("  --restrict_to_sources=<source>[,<source>...]");
    System.err.println("  --segment_sentences=<true|false>    set to false to disable sentence segmentation");
    System.err.println("  --segment_blanklines=<true|false>   set to true to enable blank line segmentation");
    System.err.println("                                      (--segment_sentences must be false.)");
    System.err.println("  --usecontext                        Use ConText negation algorithm.");
    // System.err.println("performance/effectiveness options:");
    // System.err.println("  --luceneresultlen=<length>");
    System.err.println("alternate output options:");
    System.err.println("  --list_sentences");
    System.err.println("  --list_acronyms");
    System.err.println("configuration options:");
    System.err.println("  --configfile=<filename>");
    System.err.println("  --indexdir=<directory>");
    System.err.println("  --modelsdir=<directory>");
    System.err.println("  --specialtermsfile=<filename>");
    System.err.println("  --filelistfn=<filename>");
    System.err.println("  --filelist=<file0,file1,...>");

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

  static Properties setConfiguration(String propertiesFilename,
				     Properties defaultConfiguration,
				     Properties systemConfiguration,
				     Properties optionsConfiguration,
				     boolean verbose)
    throws IOException, FileNotFoundException
  {
    Properties localConfiguration = new Properties();
    File localConfigurationFile = new File(propertiesFilename);
    if (localConfigurationFile.exists()) {
      if (verbose) {
	System.out.println("loading local configuration from " + localConfigurationFile);
      }
      localConfiguration.load(new FileReader(localConfigurationFile));
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
  

  /**
   * Pipeline application commandline.
   * <p>
   * You'll need the model file for the sentence extractor
   * "en-sent.bin" which can be downloaded from the opennlp project at
   * http://opennlp.sourceforge.net/models-1.5
   * <p>
   * Set the system property "en-sent.bin.path":
   * <pre>
   *  -Den-sent.bin.path=location of en-sent.bin
   * </pre>
   * Run the program using a command of the form:
   * <pre>
   * java -cp classpath -Den-sent.bin.path={location of en-sent.bin} gov.nih.nlm.nls.metamap.lite.Pipeline [options] input-file
   * </pre>
   * Or modify metamaplite.sh to set the location of the model file and then
   * run the script to the test application.
   * <pre>
   * ./metamaplite.sh [options] input-file
   * </pre>
   * Current options are:
   * <dl>
   * <dt>--freetext
   * <dd>Text with no markup.
   * <dt>--chemdner
   * <dd>CHEMDNER document: tab separated fields: id \t title \t abstract
   * <dt>--chemdnerSLDI
   * <dd>CHEMDNER document: id with pipe followed by tab separated fields: id | title \t abstract
   * </dl>
   * The application currently only outputs to standard output. (See
   * method:
   * gov.nih.nlm.nls.metamap.lite.EntityLookup.displayEntitySet)
   * 
   * @param args - Arguments passed from the command line
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws InstantiationException exception instantiating instance of class
   * @throws NoSuchMethodException  no method in class
   * @throws IllegalAccessException illegal access of class
   * @throws ParseException except while parsing
   * @throws InvocationTargetException exception while invoking target class
   * @throws ClassNotFoundException class not found exception
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException {
    Properties defaultConfiguration = getDefaultConfiguration();
    if (args.length > 0) {
      boolean verbose = false;
      boolean inputFromStdin = false;
      List<String> filenameList = new ArrayList<String>();
      String propertiesFilename = System.getProperty("metamaplite.propertyfile", "config/metamaplite.properties");
      Properties optionsConfiguration = new Properties();
      int i = 0;
      while (i < args.length) {
	if (args[i].substring(0,2).equals("--")) {
	  String[] fields = args[i].split("=");
	  if (fields[0].equals("--") ||
	      fields[0].equals("--pipe")) {
	    inputFromStdin = true;
	  } else if (fields[0].equals("--configfile") ||
	      fields[0].equals("--propertiesfile")) {
	    propertiesFilename = fields[1];
	  } else if (fields[0].equals("--log4jconfig")) {
	    optionsConfiguration.setProperty ("metamaplite.log4jconfig",fields[1]);
	  } else if (fields[0].equals("--indexdir")) {
	    optionsConfiguration.setProperty ("metamaplite.index.directory",fields[1]);
	  } else if (fields[0].equals("--modelsdir")) {
	    optionsConfiguration.setProperty ("opennlp.models.directory",fields[1]);
	  } else if (fields[0].equals("--specialtermsfile")) {
	    optionsConfiguration.setProperty ("metamaplite.excluded.termsfile",fields[1]);
	  } else if (fields[0].equals("--inputdocformat") ||
		     fields[0].equals("--inputformat")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype",fields[1]);
	  } else if (fields[0].equals("--segment_sentences")) {
	    optionsConfiguration.setProperty ("metamaplite.segment.sentences",fields[1]);
	  } else if (fields[0].equals("--segment_blanklines")) {
	    optionsConfiguration.setProperty ("metamaplite.segment.blanklines",fields[1]);
	  } else if (fields[0].equals("--freetext")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","freetext");
	  } else if (fields[0].equals("--chemdnersldi")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","chemdnersldi");
	  } else if (fields[0].equals("--chemdner")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","chemdner");
	  } else if (fields[0].equals("--ncbicorpus")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","ncbicorpus");
	  } else if (fields[0].equals("--sli")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","sli");
	  } else if (fields[0].equals("--restrict-to-semantic-types") ||
		     fields[0].equals("--restrict-to-sts") ||
		     fields[0].equals("--restrict_to_semantic_types") ||
		     fields[0].equals("--restrict_to_sts")) {
	    optionsConfiguration.setProperty("metamaplite.semanticgroup", fields[1]);
	  } else if (fields[0].equals("--restrict-to-sources") ||
		     fields[0].equals("--restrict-to-src") ||
		     fields[0].equals("--restrict_to_sources") ||
		     fields[0].equals("--restrict_to_src")) {
	    optionsConfiguration.setProperty("metamaplite.sourceset", fields[1]);
	  } else if (fields[0].equals("--usecontext")) {
	    if (fields.length > 1) {
	      optionsConfiguration.setProperty("metamaplite.usecontext", fields[1]);
	    } else {
	      optionsConfiguration.setProperty("metamaplite.usecontext", "true");
	    }
	  } else if (fields[0].equals("--filelist")) {
	    if (fields.length < 2) {
	      System.err.println("missing argument in \"" + fields[0] + "\" option");
	    } else {
	      optionsConfiguration.setProperty("metamaplite.inputfilelist", fields[1]);
	    }
	  } else if (fields[0].equals("--filelistfn") ||
		     fields[0].equals("--filelistfilename")) {
	    if (fields.length < 2) {
	      System.err.println("missing argument in \"" + args[i] + "\" option");
	    } else {
	      optionsConfiguration.setProperty("metamaplite.inputfilelist.filename", fields[1]);
	    }
	  } else if (fields[0].equals("--list_sentences")) {
	    optionsConfiguration.setProperty("metamaplite.list.acronyms", "true");
	  } else if (fields[0].equals("--list_acronyms")) {
	    optionsConfiguration.setProperty("metamaplite.list.sentences", "true");
	  } else if (fields[0].equals("--list_sentences_postags")) {
	    optionsConfiguration.setProperty("metamaplite.list.sentences.with.postags", "true");
	  } else if (fields[0].equals("--output_extension")) {
	    optionsConfiguration.setProperty("metamaplite.outputextension", fields[1]);	    
	  } else if (args[i].equals("--verbose")) {
	    verbose = true;
	  } else if (args[i].equals("--help")) {
	    Properties properties = setConfiguration(propertiesFilename,
						     defaultConfiguration,
						     System.getProperties(),
						     optionsConfiguration,
						     verbose);
	    BioCDocumentLoaderRegistry.register(properties);
	    displayHelp();
	    System.exit(1);
	  } else {
	    System.err.println("unknown option: " + args[i]);
	    System.exit(1);
	  }
	} else {
	  if (inputFromStdin) {
	    System.err.println("unexpected filename in command line argument list: " + args[i]);
	  } else {
	    filenameList.add(args[i]);
	  }
	}
	i++;
      }


      BioCPipeline pipeline = initPipeline();

      
      if (entityLookupResultLengthString.length() > 0) {
	System.setProperty("metamaplite.entitylookup.resultlength", 
			   entityLookupResultLengthString);
      }

      logger.info("Loading and processing documents");
      List<BioCDocument> newDocumentList = new ArrayList<BioCDocument>();;
      if (optionsConfiguration.getProperty("metamaplite.document.inputtype").equals("chemdnersldi")) {
	List<BioCDocument> documentList = ChemDNERSLDI.bioCLoadSLDIFile(filenameList.get(0));
	/*CHEMDNER SLDI style documents*/
	newDocumentList = pipeline.processDocumentList(documentList);
      } else if (optionsConfiguration.getProperty("metamaplite.document.inputtype").equals("chemdner")) {
	List<BioCDocument> documentList = ChemDNER.bioCLoadFile(filenameList.get(0));
	/*CHEMDNER SLDI style documents*/
	newDocumentList = pipeline.processDocumentList(documentList);
      } else if (optionsConfiguration.getProperty("metamaplite.document.inputtype").equals("ncbicorpus")) {
	List<BioCDocument> documentList = NCBICorpusDocument.bioCLoadFile(filenameList.get(0));
	/*CHEMDNER SLDI style documents*/
	newDocumentList = pipeline.processDocumentList(documentList);
      } else if (optionsConfiguration.getProperty("metamaplite.document.inputtype").equals("freetext")) {

	// String inputtext = FreeText.loadFile(filenameList.get(0));
	// BioCDocument document = new BioCDocument();
	// logger.info(inputtext);
	// BioCPassage passage = new BioCPassage();
	// passage.setText(inputtext);
	// passage.putInfon("docid", "00000000.tx");
	// passage.putInfon("freetext", "freetext");
	// document.addPassage(passage);
	// document.setID("00000000.tx");
	// List<BioCDocument> documentList = new ArrayList<BioCDocument>();
	// documentList.add(document);
	
	List<BioCDocument> documentList = FreeText.loadFreeTextFile(filenameList.get(0));
	newDocumentList = pipeline.processDocumentList(documentList);
      } else if (optionsConfiguration.getProperty("metamaplite.document.inputtype").equals("sli")) {
	List<BioCDocument> documentList = SingleLineInput.bioCLoadFile(filenameList.get(0));
	/*Single line documents*/
	newDocumentList = pipeline.processDocumentList(documentList);
      } 
      logger.debug("document list length: " + newDocumentList.size());
      for (BioCDocument doc: newDocumentList) {
	logger.debug(doc);
      }

      logger.info("outputing results ");

      if (optionsConfiguration.getProperty("metamaplite.outputformat").equals("bc-evaluate") ||
	  optionsConfiguration.getProperty("metamaplite.outputformat").equals("bc") ||
	  optionsConfiguration.getProperty("metamaplite.outputformat").equals("bioc") ||
	  optionsConfiguration.getProperty("metamaplite.outputformat").equals("cdi")) {
	logger.info("writing BC evaluate format file...");
	for (BioCDocument document: newDocumentList) {
	  EntityLookup1.writeBcEvaluateAnnotations(System.out, document);
	}
      } else if (optionsConfiguration.getProperty("metamaplite.outputformat").equals("brat")) {
	logger.debug("writing mmi format output");
	for (BioCDocument document: newDocumentList) {
	  Brat.writeBratAnnotations("BioCPipeline",
					    new PrintWriter
					    (new BufferedWriter
					     (new OutputStreamWriter(System.out))), 
					    document);
	}
      } else if (optionsConfiguration.getProperty("metamaplite.outputformat").equals("mmi")) {
	logger.debug("writing mmi format output");
	for (BioCDocument document: newDocumentList) {
	  EntityLookup1.writeEntities(System.out, document);
	}
      } else {
	logger.debug("writing mmi format output");
	for (BioCDocument document: newDocumentList) {
	  EntityLookup1.writeEntities(System.out, document);
	}
      }


    } else {
      displayHelp();
      System.exit(1);
    }   
  }
}
