//
package gov.nih.nlm.nls.ner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

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
import gov.nih.nlm.nls.metamap.lite.EntityLookup3;
import gov.nih.nlm.nls.metamap.lite.SemanticGroupFilter;
import gov.nih.nlm.nls.metamap.lite.SemanticGroups;
import gov.nih.nlm.nls.metamap.lite.EntityAnnotation;
import gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.resultformats.Brat;

import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.ChemDNERSLDI;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;
import gov.nih.nlm.nls.metamap.document.SingleLineInput;
import gov.nih.nlm.nls.metamap.document.SingleLineDelimitedInputWithID;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoader;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoaderRegistry;
import gov.nih.nlm.nls.metamap.document.SemEvalDocument;

import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatterRegistry;

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
import opennlp.tools.util.Span;

import gov.nih.nlm.nls.utils.Configuration;

/**
 * Properties precedence (from highest to lowest):
 * <ul>
 *   <li>Command line options</li>
 *   <li>System properties</li>
 *   <li>MetaMap property file</li>
 * </ul>
 */
public class MetaMapLite {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(MetaMapLite.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");
  static Map<String,String> outputExtensionMap = new HashMap<String,String>();
  static {
    outputExtensionMap.put("brat",".ann");
    outputExtensionMap.put("mmi",".mmi");
    outputExtensionMap.put("cdi",".cdi");
  }

  Set<String> semanticGroup = new HashSet<String>(); // initially empty
  Set<String> sourceSet = new HashSet<String>(); // initially empty

  AbbrConverter abbrConverter = new AbbrConverter();
  static ExtractAbbrev extractAbbr = new ExtractAbbrev();
  Properties properties;

  boolean useContext = false;
  SentenceAnnotator sentenceAnnotator;
  EntityLookup3 entityLookup;

  public MetaMapLite(Properties properties)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException,
	   IOException
  {
    this.properties = properties;
    if (properties.getProperty("opennlp.en-sent.bin.path") != null) {
      SentenceExtractor.setModel(properties.getProperty("opennlp.en-sent.bin.path"));
    }
    this.sentenceAnnotator = new SentenceAnnotator(properties);
    this.entityLookup = new EntityLookup3(properties);
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
    BioCDocumentLoaderRegistry.register("semeval14",
					"SemEval Document (Almost FreeText)",
					new SemEvalDocument());
    BioCDocumentLoaderRegistry.register("sli",
					"Single Line Input document sets",
					new SingleLineInput());
    BioCDocumentLoaderRegistry.register("sldi",
					"Single Line Input document sets",
					new SingleLineDelimitedInputWithID());
    ResultFormatterRegistry.register("brat",
				     "BRAT Annotation format (.ann)",
				     new Brat());
    ResultFormatterRegistry.register("mmi",
				     "Fielded MetaMap Indexing-like Output",
				     new MMI());

    /** augment or override any built-in formats with ones specified by property file. */
    BioCDocumentLoaderRegistry.register(properties);
    ResultFormatterRegistry.register(properties);
  }

  void setSemanticGroup(String[] semanticTypeList) {
    this.semanticGroup = new HashSet<String>(Arrays.asList(semanticTypeList));
  }

  void setSourceSet(String[] sourceList) {
    this.sourceSet = new HashSet<String>(Arrays.asList(sourceList));
  }

  /**
   * Invoke sentence processing pipeline on a sentence
   * @param sentence
   * @return updated sentence
   */
  public BioCSentence processSentence(BioCSentence sentence, BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, 
	   IOException, Exception
  {
    logger.debug("enter processSentence");
    // BioCSentence annotatedSentence = SentenceAnnotator.tokenizeSentence(passage, sentence);
    BioCSentence result0 = 
      sentenceAnnotator.addEntities
      (this.entityLookup, sentence, passage);
    // System.out.println("unfiltered entity list: ");
    // Brat.listEntities(result0);
    BioCSentence result = result0;
    if ((! this.semanticGroup.contains("all")) &&
        (this.semanticGroup.size() > 0)) {
      result = SemanticGroupFilter.keepEntitiesInSemanticGroup
	(this.semanticGroup, result0);
    }
    // look for negation and other relations using Context.
    if (this.useContext) {
      ContextWrapper.applyContext(result);
    }

    // System.out.println("filtered entity list: ");
    // Brat.listEntities(result);
    logger.debug("exit processSentence");
    return result;
 }

  /**
   * Invoke sentence processing pipeline on each sentence in supplied sentence list.
   * @param passage containing list of sentences
   * @return list of results from sentence processing pipeline, one per sentence in input list.
   */
  public BioCPassage processSentences(BioCPassage passage) 
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    logger.debug("enter processSentences");
    List<BioCSentence> resultList = new ArrayList<BioCSentence>();
    for (BioCSentence sentence: passage.getSentences()) {
      logger.info("Processing: " + sentence.getText());
      resultList.add(this.processSentence(sentence, passage));
    }
    /*passage.setSentences(resultList);*/
    for (BioCSentence sentence: resultList) {
      passage.addSentence(sentence);
    }
    logger.debug("exit processSentences");
    return passage;
  }

  public List<Entity> processPassage(BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    logger.debug("enter processPassage");
    logger.debug(passage.getText());
    BioCPassage passageWithSentences = SentenceExtractor.createSentences(passage);
    //BioCPassage passageWithSentsAndAbbrevs = abbrConverter.getPassage(passageWithSentences);
    BioCPassage passageWithSentsAndAbbrevs = new BioCPassage();
    passageWithSentsAndAbbrevs.setOffset( passageWithSentences.getOffset() );
    passageWithSentsAndAbbrevs.setText( passageWithSentences.getText() );
    for (BioCAnnotation note : passageWithSentences.getAnnotations() ) {
      passageWithSentsAndAbbrevs.addAnnotation( abbrConverter.getAnnotation(note) );
    }
    for (BioCRelation rel : passageWithSentences.getRelations() ) {
      passageWithSentsAndAbbrevs.addRelation(rel);
    }
    for (BioCSentence sentence: passageWithSentences.getSentences()) {
      BioCSentence newSentence = abbrConverter.getSentence(sentence);
      passageWithSentsAndAbbrevs.addSentence(newSentence);
      for (BioCAnnotation note : newSentence.getAnnotations() ) {
	passageWithSentsAndAbbrevs.addAnnotation( abbrConverter.getAnnotation(note) );
      }
      for (BioCRelation rel : newSentence.getRelations() ) {
	passageWithSentsAndAbbrevs.addRelation(rel);
      }
    }
    logger.info("passage relations: " + passageWithSentsAndAbbrevs.getRelations());
    logger.info("passage annotations: " + passageWithSentsAndAbbrevs.getAnnotations());
    // BioCPassage newPassage = processSentences(passageWithSentsAndAbbrevs);
    List<Entity> entityList =
      MarkAbbreviations.markAbbreviations
      (passageWithSentsAndAbbrevs,
       this.entityLookup.processPassage
       ((passage.getInfon("section") != null) ? passage.getInfon("section") : "text",
	passageWithSentsAndAbbrevs, this.useContext, this.semanticGroup, this.sourceSet));
    logger.debug("exit processPassage");
    return entityList;
  }

  public List<Entity> processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    List<Entity> entityList = new ArrayList<Entity>();    
    for (BioCPassage passage: document.getPassages()) {
      entityList.addAll(processPassage(passage));
    }
    return entityList;
  }

  public List<Entity> processDocumentList(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    List<Entity> entityList = new ArrayList<Entity>();    
    for (BioCDocument document: documentList) {
      entityList.addAll(this.processDocument(document));
    }
    return entityList;
  }

  public List<Sentence> getSentenceList(List<BioCDocument> documentList) {
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (BioCDocument document: documentList) {
      for (BioCPassage passage: document.getPassages()) {
	sentenceList.addAll(SentenceExtractor.createSentenceList(passage.getText()));
      }
    }
    return sentenceList;
  }

    public List<AbbrInfo> getAcronymList(List<BioCDocument> documentList) {
    List <AbbrInfo> infos = new ArrayList<AbbrInfo>();
    for (BioCDocument document: documentList) {
      for (BioCPassage passage: document.getPassages()) {
	for (Sentence sentence: SentenceExtractor.createSentenceList(passage.getText())) {
	  infos.addAll(extractAbbr.extractAbbrPairsString(sentence.getText()));
	}
      }
    }
    return infos;
  }


  public static MetaMapLite initMetaMapLite()
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException
  {
    Properties properties = new Properties();
    properties.load(new FileReader(configPropertyFilename));
    if (logger.isDebugEnabled()) {
      for (Map.Entry<Object,Object> entry: properties.entrySet()) {
	logger.debug(entry.getKey() + " -> " + entry.getValue());
      }
    }
    return new MetaMapLite(properties);
  }

  static void displayHelp() {
    System.err.println("usage: [options] filenames");
    System.err.println("document processing options:");
    System.err.println("  --freetext (default)");
    System.err.println("  --inputdocformat=<document type>");
    System.err.println("    Available document types:");
    for (String name: BioCDocumentLoaderRegistry.listNameSet()) {
      System.err.println("      " + name);
    }
    System.err.println("output options:");
    System.err.println("  --bioc|cdi|bc|bc-evaluate");
    System.err.println("  --mmilike|mmi");
    System.err.println("  --mmi");
    System.err.println("  --brat");    
    //    System.err.println("  --luceneresultlen");
    System.err.println("  --outputformat=<format type>");
    System.err.println("    Available format types:");  
    for (String name: ResultFormatterRegistry.listNameSet()) {
      System.err.println("      " + name);
    }
    System.err.println("processing options:");
    System.err.println("  --restrict_to_sts=<semtype>[,<semtype>...]");
    System.err.println("  --restrict_to_sourcess=<source>[,<source>...]");
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
  }

  public static void expandModelsDir(Properties properties) {
    String modelsDir = properties.getProperty("metamaplite.models.directory");
    if (modelsDir != null) {
      properties.setProperty("opennlp.en-sent.bin.path", modelsDir + "/data/models/en-sent.bin");
      properties.setProperty("opennlp.en-token.bin.path", modelsDir + "/data/models/en-token.bin");
      properties.setProperty("opennlp.en-pos-maxent.bin.path", modelsDir + "/data/models/en-pos-maxent.bin");
    }
  }
  public static void expandIndexDir(Properties properties) {
    String indexDirName = properties.getProperty("metamaplite.index.directory");
    if (indexDirName != null) {
      properties.setProperty("metamaplite.ivf.cuiconceptindex", indexDirName + "/indices/cuiconcept");
      properties.setProperty("metamaplite.ivf.firstwordsofonewideindex", indexDirName + "/indices/first_words_of_one_WIDE");
      properties.setProperty("metamaplite.ivf.cuisourceinfoindex", indexDirName + "/indices/cuisourceinfo");
      properties.setProperty("metamaplite.ivf.cuisemantictypeindex", indexDirName + "/indices/cuist");
      properties.setProperty("metamaplite.ivf.varsindex", indexDirName + "/indices/vars");
    }
  }
  
  public static void displayProperties(String label, Properties properties) {
      System.out.println(label);
      for (String name: properties.stringPropertyNames()) {
	System.out.println("   " + name + ": " + properties.getProperty(name));
      }
  }
  
  /**
   * MetaMapLite application commandline.
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
   * gov.nih.nlm.nls.metamap.lite.EntityAnnotation.displayEntitySet)
   * </pre>
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException,
	   Exception
  {
    if (args.length > 0) {
      List<String> filenameList = new ArrayList<String>();
      String propertiesFilename = System.getProperty("metamaplite.propertyfile", "config/metamaplite.properties");
      String modelsDirectory = System.getProperty("metamaplite.opennlp.models.directory", "data/models");
      String indexDirectory = System.getProperty("metamaplite.index.directory", "data/ivf");
      Properties defaultConfiguration = new Properties();
      defaultConfiguration.setProperty("metamaplite.excluded.termsfile",
				       System.getProperty("metamaplite.excluded.termsfile", "data/specialterms.txt"));
      defaultConfiguration.setProperty("metamaplite.opennlp.models.directory",
				       System.getProperty("metamaplite.opennlp.models.directory", "data/models"));
      defaultConfiguration.setProperty("metamaplite.index.directory",
				       System.getProperty("metamaplite.opennlp.index.directory", "data/ivf"));
      defaultConfiguration.setProperty("metamaplite.document.inputtype", "freetext");
      defaultConfiguration.setProperty("metamaplite.outputformat", "mmi");
      defaultConfiguration.setProperty("metamaplite.outputextension",  ".mmi");
      defaultConfiguration.setProperty("metamaplite.semanticgroup", "all");
      defaultConfiguration.setProperty("metamaplite.sourceset", "all");
      defaultConfiguration.setProperty("metamaplite.usecontext", "false");

      defaultConfiguration.setProperty("opennlp.en-sent.bin.path", modelsDirectory + "/en-sent.bin");
      defaultConfiguration.setProperty("opennlp.en-token.bin.path", modelsDirectory + "/en-token.bin");
      defaultConfiguration.setProperty("opennlp.en-pos.bin.path", modelsDirectory + "/en-pos-maxent.bin");

      defaultConfiguration.setProperty("metamaplite.ivf.cuiconceptindex", indexDirectory + "/strict/indices/cuiconcept");
      defaultConfiguration.setProperty("metamaplite.ivf.cuisourceinfoindex", indexDirectory + "/strict/indices/cuisourceinfo");
      defaultConfiguration.setProperty("metamaplite.ivf.cuisemantictypeindex", indexDirectory + "/strict/indices/cuist");
      
      defaultConfiguration.setProperty("bioc.document.loader.chemdner", "gov.nih.nlm.nls.metamap.document.ChemDNER");
      defaultConfiguration.setProperty("bioc.document.loader.freetext", "gov.nih.nlm.nls.metamap.document.FreeText");
      defaultConfiguration.setProperty("bioc.document.loader.ncbicorpus", "gov.nih.nlm.nls.metamap.document.NCBICorpusDocument");

      System.out.println("Reading options");
      Properties optionsConfiguration = new Properties();
      int i = 0;
      while (i < args.length) {
        if (args[i].substring(0,2).equals("--")) {
	  String[] fields = args[i].split("=");
	  if (fields[0].equals("--configfile") ||
	      fields[0].equals("--propertiesfile")) {
	    propertiesFilename = fields[1];
	  } else if (fields[0].equals("--indexdir")) {
	    optionsConfiguration.setProperty ("metamaplite.index.directory",fields[1]);
	  } else if (fields[0].equals("--modelsdir")) {
	    optionsConfiguration.setProperty ("metamaplite.opennlp.models.directory",fields[1]);
	  } else if (fields[0].equals("--specialtermsfile")) {
	    optionsConfiguration.setProperty ("metamaplite.excluded.termsfile",fields[1]);
	  } else if (fields[0].equals("--inputdocformat")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype",fields[1]);
	  } else if (fields[0].equals("--freetext")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","freetext");
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","freetext");
	  } else if (fields[0].equals("--outputformat")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat",fields[1]);
	    optionsConfiguration.setProperty("metamaplite.outputextension", outputExtensionMap.get(fields[1]));
	  } else if (fields[0].equals("--brat") || 
		     fields[0].equals("--BRAT")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat","brat");
	    optionsConfiguration.setProperty("metamaplite.outputextension", outputExtensionMap.get("brat"));
	  } else if (fields[0].equals("--mmi") || 
		     fields[0].equals("--mmilike")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat","mmi");
	    optionsConfiguration.setProperty("metamaplite.outputextension", outputExtensionMap.get("mmi"));
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
	    optionsConfiguration.setProperty("metamaplite.usecontext", "true");
	  } else if (fields[0].equals("--brat_type_name")) {
	    optionsConfiguration.setProperty("metamaplite.result.formatter.property.brat.typename", fields[1]);
	  } else if (args[i].equals("--list_sentences")) {
	    optionsConfiguration.setProperty("metamaplite.list.acronyms", "true");
	  } else if (args[i].equals("--list_acronyms")) {
	    optionsConfiguration.setProperty("metamaplite.list.sentences", "true");
	  } else if (args[i].equals("--help")) {
	    displayHelp();
	    System.exit(1);
	  } 
	} else {
	  filenameList.add(args[i]);
	}
	i++;
      }

      Properties localConfiguration = new Properties();
      File localConfigurationFile = new File(propertiesFilename);
      if (localConfigurationFile.exists()) {
	System.out.println("loading local configuration from " + localConfigurationFile);
	localConfiguration.load(new FileReader(localConfigurationFile));
	System.out.println("loaded " + localConfiguration.size() + " records from local configuration");
      }
      expandModelsDir(defaultConfiguration);
      expandModelsDir(localConfiguration);
      expandModelsDir(optionsConfiguration);

      expandIndexDir(defaultConfiguration);
      expandIndexDir(localConfiguration);
      expandIndexDir(optionsConfiguration);

      displayProperties("defaultConfiguration:", defaultConfiguration);
      displayProperties("localConfiguration:", localConfiguration);
      displayProperties("optionsConfiguration:", optionsConfiguration);

      Properties properties = Configuration.getConfiguration(defaultConfiguration, localConfiguration, optionsConfiguration);
      
      displayProperties("properties:", properties);
      MetaMapLite metaMapLiteInst = new MetaMapLite(properties);
      /** set any options in properties configuration file and system properties first */
      BioCDocumentLoaderRegistry.register(properties);

      String documentInputOption = properties.getProperty("metamaplite.document.inputtype", "freetext");
      String outputFormatOption = properties.getProperty("metamaplite.outputformat","mmi");
      String outputExtension =	properties.getProperty("metamaplite.outputextension", ".mmi");
      metaMapLiteInst.setSemanticGroup(properties.getProperty("metamaplite.semanticgroup", "all").split(","));
      metaMapLiteInst.setSourceSet(properties.getProperty("metamaplite.sourceset","all").split(","));
      System.setProperty("metamaplite.result.formatter.property.brat.typename",
			 properties.getProperty("metamaplite.result.formatter.property.brat.typename", "metamaplite"));
      metaMapLiteInst.useContext = 
	Boolean.parseBoolean(properties.getProperty("metamaplite.usecontext", "false"));
      boolean listSentences =
	Boolean.parseBoolean(properties.getProperty("metamaplite.list.acronyms","false"));
      boolean listAcronyms =
	Boolean.parseBoolean(properties.getProperty("metamaplite.list.sentences","false"));

      logger.info("Loading and processing documents");
      for (String filename: filenameList) {
	System.out.println("Loading and processing " + filename);
	logger.info("Loading and processing " + filename);

	// load documents
	BioCDocumentLoader docLoader = BioCDocumentLoaderRegistry.get(documentInputOption);
	List<BioCDocument> documentList = docLoader.loadFileAsBioCDocumentList(filename);

	if (listSentences) {
	  // output results for file
	  // create output filename
	  String basename = filename.substring(0,filename.lastIndexOf(".")); // 
	  String outputFilename = basename + ".sentences";
	  logger.info("outputing results to " + outputFilename);
	  PrintWriter pw = new PrintWriter(new BufferedWriter
					   (new FileWriter(outputFilename)));
	  for (Sentence sent: metaMapLiteInst.getSentenceList(documentList)) {
	    pw.println(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText());
	  }
	  pw.close();
	} else if (listAcronyms) {
	  String basename = filename.substring(0,filename.lastIndexOf(".")); // 
	  String outputFilename = basename + ".acronyms";
	  PrintWriter pw = new PrintWriter(new BufferedWriter
					   (new FileWriter(outputFilename)));
	  for (AbbrInfo acronym: metaMapLiteInst.getAcronymList(documentList)) {
	    pw.println(acronym.shortForm + "|" + acronym.shortFormIndex + "|" +
		       acronym.longForm + "|" + acronym.longFormIndex );
	  }
	  pw.close();
	} else {
	  // process documents
	  List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);
	  
	  // create output filename
	  String basename = filename.substring(0,filename.lastIndexOf(".")); // 
	  String outputFilename = basename + outputExtension;
	  logger.info("outputing results to " + outputFilename);
	  
	  // output results for file
	  PrintWriter pw = new PrintWriter(new BufferedWriter
					   (new FileWriter(outputFilename)));
	  // format output
	  ResultFormatter formatter = ResultFormatterRegistry.get(outputFormatOption);
	  if (formatter != null) {
	    formatter.entityListFormatter(pw, entityList);
	  } else {
	    System.out.println("! Couldn't find formatter for output format option: " + outputFormatOption);
	  }
	  pw.close();
	}
      } /* for filename */

    } else {
      displayHelp();
      System.exit(1);
    }   
  }

}
