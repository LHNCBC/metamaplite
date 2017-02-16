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
import gov.nih.nlm.nls.metamap.lite.EntityLookup3;
import gov.nih.nlm.nls.metamap.lite.EntityLookup4;
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

import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatterRegistry;

// import gov.nih.nlm.nls.metamap.lite.context.ContextWrapper;
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
 * Using MetaMapLite from a Java program:
 * <pre>
 * Properties myProperties = MetaMapLite.getDefaultConfiguration();
 * myProperties.setProperty("opennlp.models.directory", 
 *                          "/Projects/metamaplite/data/models");
 * MetaMapLite.expandModelsDir(myProperties);
 * myProperties.setProperty("metamaplite.index.directory",
 * 		     "/Projects/metamaplite/data/ivf/strict");
 * myProperties.setProperty("metamaplite.excluded.termsfile",
 *			     "/Projects/metamaplite/data/specialterms.txt");
 * MetaMapLite.expandIndexDir(myProperties);
 * MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);
 * BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning ...");
 * List&lt;BioCDocument&gt; documentList = new ArrayList&lt;BioCDocument&gt;();
 * documentList.add(document);
 * List&lt;Entity&gt; entityList = metaMapLiteInst.processDocumentList(documentList);
 * for (Entity entity: entityList) {
 *   for (Ev ev: entity.getEvSet()) {
 *	System.out.print(ev.getConceptInfo().getCUI() + "|" + entity.getMatchedText());
 *	System.out.println();
 *   }
 * }
 * </pre>
 * Properties precedence (from highest to lowest):
 * <ul>
 *   <li>Command line options</li>
 *   <li>System properties</li>
 *   <li>MetaMap property file</li>
 * </ul>
 * <p>
 * Configuration Properties:
 * <dl>
 * <dt>metamaplite.semanticgroup</dt><dd>restrict output to concepts with specified semantic types</dd>
 * <dt>metamaplite.sourceset</dt><dd>restrict output to concepts in specified sources</dd>
 * <dt>metamaplite.segmentation.method</dt><dd>Set method for text segmentation (values: SENTENCES, BLANKLINES, LINES; default: SENTENCES)</dd>
 * <dt>metamaplite.negation.detector</dt><dd>negation detector class: default: gov.nih.nlm.nls.metamap.lite.NegEx</dd>
 * <dt>opennlp.models.directory</dt><dd>parent location of opennlp models</dd>
 * <dt>opennlp.en-pos.bin.path</dt><dd> path for part-of-speech model (default: data/models/en-pos-maxent.bin)</dd>
 * <dt>metamaplite.index.directory</dt><dd>parent location of metamap indexes, (sets the following properties)</dd>
 * <dt>metamaplite.ivf.cuiconceptindex</dt><dd>location of cui-concept index</dd>
 * <dt>metamaplite.ivf.cuisourceinfoindex</dt><dd>location of cui-sourceinfo index</dd>
 * <dt>metamaplite.ivf.cuisemantictypeindex</dt><dd>location of cui-semantictype index</dd>
 * <dt>metamaplite.document.inputtype</dt><dd>document input type (default: freetext)</dd>
 * <dt>metamaplite.property.file</dt><dd>load configuration from file (default: ./config/metamaplite.properties)</dd>
 * <dt></dt><dd></dd>
 * </dl>
 * <p>
 * Command line frontend properties 
 * <dl>
 * <dt>metamaplite.document.inputtype</dt><dd>set input type for document reader</dd>
 * <dt>metamaplite.inputfilelist</dt><dd>list input files separated by commas in value of property.</dd>
 * <dt>metamaplite.inputfilelistfile</dt><dd>use file containing list of files for input, one file per line.</dd>
 * <dt>metamaplite.list.acronyms</dt><dd>list document acronyms</dd>
 * <dt>metamaplite.list.sentences</dt><dd>list document sentences only.</dd>
 * <dt>metamaplite.list.sentences.with.postags</dt><dd>list document sentences only with part-of-speech tags</dd>
 * <dt>metamaplite.outputformat</dt><dd>output format of entity result set.</dd>
 * <dt>metamaplite.outputextension</dt><dd>set output file extension for result file(s).</dd>
 * </dl>
 * <p>
 * User supplied document loader/reader properties
 * <p>
 * Properties are prefixed with string: "bioc.document.loader.freetext"
 * followed by a period with the name of the document loader.
 * <dl>
 * <dt>bioc.document.loader.{name}<dt><dd>classname</dd>
 * </dl>
 * The class must implement the gov.nih.nlm.nls.metamap.document.BioCDocumentLoader interface.
 * <dl>
 * <dt>bioc.document.loader.freetext</dt><dd>gov.nih.nlm.nls.metamap.document.FreeText</dd>
 * <dt>bioc.document.loader.chemdner</dt><dd>gov.nih.nlm.nls.metamap.document.ChemDNER</dd>
 * </dl>
 * <p>
 * User supplied result formatter properties
 * <p>
 * Properties are prefixed with string: "metamaplite.result.formatter"
 * followed by a period with the name of the formatter.
 * <dl>
 * <dt>metamaplite.result.formatter.{name}<dt><dd>classname</dd>
 * </dl>
 * The class must implement 
 * <dl>
 * <dt>metamaplite.result.formatter.cuilist</dt><dd>gov.nih.nlm.nls.metamap.lite.resultformats.CuiList</dd>
 * <dt>metamaplite.result.formatter.brat</dt><dd>gov.nih.nlm.nls.metamap.lite.resultformats.Brat</dd>
 * </dl>
 */
public class MetaMapLite {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(MetaMapLite.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");
  static Map<String,String> outputExtensionMap = new HashMap<String,String>();
  static {
    outputExtensionMap.put("bioc",".bioc");
    outputExtensionMap.put("brat",".ann");
    outputExtensionMap.put("mmi",".mmi");
    outputExtensionMap.put("cdi",".cdi");
    outputExtensionMap.put("cuilist",".cuis");
  }

  Set<String> semanticGroup = new HashSet<String>(); // initially empty
  Set<String> sourceSet = new HashSet<String>(); // initially empty

  AbbrConverter abbrConverter = new AbbrConverter();
  static ExtractAbbrev extractAbbr = new ExtractAbbrev();
  Properties properties;

  boolean detectNegationsFlag = false;
  SentenceAnnotator sentenceAnnotator;
  EntityLookup entityLookup;
  enum SegmentatonType {
    SENTENCES,
    BLANKLINES,
    LINES
  };

  SegmentatonType segmentationMethod = SegmentatonType.SENTENCES;

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
    this.entityLookup = new EntityLookup4(properties);
    BioCDocumentLoaderRegistry.register("bioc",
					"For BioC XML documents.", 
					new FreeText());
    BioCDocumentLoaderRegistry.register("freetext",
					"For freetext documents that are grammatically well behaved.", 
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
    ResultFormatterRegistry.register("brat",
				     "BRAT Annotation format (.ann)",
				     new Brat());
    ResultFormatterRegistry.register("mmi",
				     "Fielded MetaMap Indexing-like Output",
				     new MMI());
    ResultFormatterRegistry.register("cuilist",
				     "UMLS CUI List Output",
				     new CuiList());

    /** augment or override any built-in formats with ones specified by property file. */
    BioCDocumentLoaderRegistry.register(properties);
    ResultFormatterRegistry.register(properties);

    this.setSemanticGroup(properties.getProperty("metamaplite.semanticgroup", "all").split(","));
    this.setSourceSet(properties.getProperty("metamaplite.sourceset","all").split(","));
    System.setProperty("metamaplite.result.formatter.property.brat.typename",
		       properties.getProperty("metamaplite.result.formatter.property.brat.typename",
					      "metamaplite"));
    this.detectNegationsFlag = 
      Boolean.parseBoolean(properties.getProperty("metamaplite.detect.negations", "true"));
    this.setSegmentationMethod
      (properties.getProperty("metamaplite.segmentation.method","SENTENCE"));
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
   * Set seqmentation method used by passage segmenter.
   * segmentation methods:
   * <dl>
   * <dt><tt>SENTENCES</tt>  <dd>seqment text using sentence breaker.
   * <dt><tt>BLANKLINES</tt> <dd>seqment text using blank lines as delimitor
   * <dt><tt>LINES</tt>      <dd>seqment text using newlines as delimitor
   * </dl>
   * @param typeName name of segmentation method to use
   */
  public void setSegmentationMethod(String typeName) {
    if (typeName.equals("SENTENCES")) {
      this.segmentationMethod = SegmentatonType.SENTENCES;
    } else if (typeName.equals("BLANKLINES")) {
      this.segmentationMethod = SegmentatonType.BLANKLINES;
    } else if (typeName.equals("LINES")) {
      this.segmentationMethod = SegmentatonType.LINES;
    }
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
    // // look for negation and other relations using Context.
    // if (this.detectNegationsFlag) {
    //    ContextWrapper.applyContext(result);
    // }

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
    BioCPassage passage0;
    List<BioCSentence> sentenceList;
    int offset;
    int passageOffset;
    String text;
    String[] segmentList;
    switch (segmentationMethod) {
    case SENTENCES:
      passage0 = SentenceExtractor.createSentences(passage);
      break;
    case BLANKLINES:
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      passageOffset = passage.getOffset();
      text = passage.getText();
      segmentList = text.split("\n\n");
      for (String segment: segmentList) {
	BioCSentence sentence = new BioCSentence();
	offset = text.indexOf(segment, offset);
	sentence.setOffset(offset);
	sentence.setText(segment);
	sentence.setInfons(passage.getInfons());
	sentenceList.add(sentence);
	passage.addSentence(sentence);
	offset = segment.length(); 
      }
      passage0 = passage;
      break;
    case LINES:
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      passageOffset = passage.getOffset();
      text = passage.getText();
      segmentList = text.split("\n");
      for (String segment: segmentList) {
	offset = text.indexOf(segment, offset);
	if (segment.trim().length() > 0) {
	  BioCSentence sentence = new BioCSentence();
	  sentence.setOffset(offset);
	  sentence.setText(segment);
	  sentence.setInfons(passage.getInfons());
	  sentenceList.add(sentence);
	  passage.addSentence(sentence);
	}
	offset = segment.length(); // preserve offsets even for blank lines.
      }
      passage0 = passage;
      break;
    default:
      // copy entire text of passage into one sentence
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      BioCSentence sentence = new BioCSentence();
      sentence.setText(passage.getText());
      sentence.setOffset(offset);
      sentence.setInfons(passage.getInfons());
      sentenceList.add(sentence);
      passage.addSentence(sentence);
      passage0 = passage;
      break;
    }
    //BioCPassage passageWithSentsAndAbbrevs = abbrConverter.getPassage(passage0);
    BioCPassage passageWithSentsAndAbbrevs = new BioCPassage();
    passageWithSentsAndAbbrevs.setOffset( passage0.getOffset() );
    passageWithSentsAndAbbrevs.setText( passage0.getText() );
    for (BioCAnnotation note : passage0.getAnnotations() ) {
      passageWithSentsAndAbbrevs.addAnnotation( abbrConverter.getAnnotation(note) );
    }
    for (BioCRelation rel : passage0.getRelations() ) {
      passageWithSentsAndAbbrevs.addRelation(rel);
    }
    for (BioCSentence sentence: passage0.getSentences()) {
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
	passageWithSentsAndAbbrevs, this.detectNegationsFlag, this.semanticGroup, this.sourceSet));
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
	sentenceList.addAll(SentenceExtractor.createSentenceList(passage.getText(), passage.getOffset()));
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

  public static List<String> loadInputFileList(String inputfileListFileName)
    throws FileNotFoundException, IOException
  {
    List<String> inputFileList = new ArrayList<String>();
    BufferedReader br = 
      new BufferedReader(new FileReader(inputfileListFileName));
    String line;
    while ((line = br.readLine()) != null) {
      inputFileList.add(line.trim());
    }
    br.close();
    return inputFileList;
  }

  static void displayHelp() {
    System.err.println("usage: [options] filenames");
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
    System.err.println("  --restrict_to_sources=<source>[,<source>...]");
    System.err.println("  --segmentation_method=SENTENCES|BLANKLINES|LINES    set method for text segmentation");
    System.err.println("  --segment_sentences    Set method for text segmentation to sentences");
    System.err.println("  --segment_blanklines   Set method for text segmentation to each blanklines");
    System.err.println("  --segment_lines        Set method for text segmentation to each line");
    System.err.println("  --usecontext           Use ConText negation algorithm.");
    System.err.println("  --enable_postagging=[true|false]  Use part-of-speech tagging (default: true).");
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
  
  public static void displayProperties(String label, Properties properties) {
    System.out.println(label);
    for (String name: properties.stringPropertyNames()) {
      System.out.println("   " + name + ": " + properties.getProperty(name));
    }
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

  static Properties setConfiguration(String propertiesFilename,
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
    ClassLoader loader = MetaMapLite.class.getClassLoader();
    if(loader==null)
      loader = ClassLoader.getSystemClassLoader(); // use system class loader if class loader is null
    java.net.URL url = loader.getResource(propertiesFilename);
    try {
      localConfiguration.load(url.openStream());
    } catch(Exception e) {
      logger.info("Could not load configuration file from classpath: " + propertiesFilename);
    }

    // check filesystem 
    File localConfigurationFile = new File(propertiesFilename);
    if (localConfigurationFile.exists()) {
      logger.info("loading local configuration from " + localConfigurationFile);
      if (verbose) {
	System.out.println("loading local configuration from " + localConfigurationFile);
      }
      localConfiguration.load(new FileReader(localConfigurationFile));
      logger.info("loaded " + localConfiguration.size() + " records from local configuration");
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

  /** list entities using document list from stdin */
  void listSentences(List<BioCDocument> documentList)
  {
    // output results for file
    // create output filename
    logger.info("outputing results to Standard Output");
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
    for (Sentence sent: this.getSentenceList(documentList)) {
      pw.println(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText());
    }
    pw.flush();
  }

  void listAcronyms(List<BioCDocument> documentList) {
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
    for (AbbrInfo acronym: this.getAcronymList(documentList)) {
      pw.println(acronym.shortForm + "|" + acronym.shortFormIndex + "|" +
		 acronym.longForm + "|" + acronym.longFormIndex );
    }
    pw.flush();
    pw.close();
  }

  void listSentencesWithPosTags(List<BioCDocument> documentList)
    throws IOException
  {
    logger.info("outputing results to Standard Output");
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
    for (Sentence sent: this.getSentenceList(documentList)) {
      List<ERToken> tokenList = sentenceAnnotator.addPartOfSpeech(sent);
      pw.println(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText());
      for (ERToken token: tokenList) {
	pw.print(token.getText() + "(" + token.getPartOfSpeech() + "),");
      }
      pw.println();
    }
    pw.close();
  }

  void listEntities(List<BioCDocument> documentList, String outputFormatOption)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    // process documents
    List<Entity> entityList = this.processDocumentList(documentList);

    logger.info("outputing results to standard output." );

    // output results for file
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
    // format output
    ResultFormatter formatter = ResultFormatterRegistry.get(outputFormatOption);
    if (formatter != null) {
      formatter.entityListFormatter(pw, entityList);
    } else {
      System.out.println("! Couldn't find formatter for output format option: " + outputFormatOption);
    }
    pw.flush();
    pw.close();
  }

  void listSentences(String filename, 
		     List<BioCDocument> documentList)
    throws IOException
  {
    // output results for file
    // create output filename
    String basename = filename.substring(0,filename.lastIndexOf(".")); // 
    String outputFilename = basename + ".sentences";
    logger.info("outputing results to " + outputFilename);
    PrintWriter pw = new PrintWriter(new BufferedWriter
				     (new FileWriter(outputFilename)));
    for (Sentence sent: this.getSentenceList(documentList)) {
      pw.println(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText());
    }
    pw.close();
  }

  void listAcronyms(String filename, 
		    List<BioCDocument> documentList)
    throws IOException
  {
    String basename = filename.substring(0,filename.lastIndexOf(".")); // 
    String outputFilename = basename + ".acronyms";
    PrintWriter pw = new PrintWriter(new BufferedWriter
				     (new FileWriter(outputFilename)));
    for (AbbrInfo acronym: this.getAcronymList(documentList)) {
      pw.println(acronym.shortForm + "|" + acronym.shortFormIndex + "|" +
		 acronym.longForm + "|" + acronym.longFormIndex );
    }
    pw.close();
  }

  void listSentencesWithPosTags(String filename, 
				List<BioCDocument> documentList)
    throws IOException
  {
    // output results for file
    // create output filename
    String basename = filename.substring(0,filename.lastIndexOf(".")); // 
    String outputFilename = basename + ".sentences";
    logger.info("outputing results to " + outputFilename);
    PrintWriter pw = new PrintWriter(new BufferedWriter
				     (new FileWriter(outputFilename)));
    for (Sentence sent: this.getSentenceList(documentList)) {
      List<ERToken> tokenList = sentenceAnnotator.addPartOfSpeech(sent);
      pw.println(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText());
      for (ERToken token: tokenList) {
	pw.print(token.getText() + "(" + token.getPartOfSpeech() + "),");
      }
      pw.println();
    }
    pw.close();
  }
  
  void listEntities(String filename, 
		    List<BioCDocument> documentList,
		    String outputExtension,
		    String outputFormatOption)
    throws IOException, IllegalAccessException, InvocationTargetException, Exception
  {
    // process documents
    List<Entity> entityList = this.processDocumentList(documentList);
    String basename = "output";
    // create output filename
    if (filename.lastIndexOf(".") >= 0) {
      basename = filename.substring(0,filename.lastIndexOf(".")); //
    } else {
      basename = filename;
    }
    String outputFilename = basename + outputExtension;
    logger.info("outputing results to " + outputFilename);
    
    // output results for file
    PrintWriter pw = new PrintWriter(new BufferedWriter
				     (new FileWriter(outputFilename)));
    // format output
    ResultFormatter formatter = ResultFormatterRegistry.get(outputFormatOption);
    if (formatter != null) {
      formatter.initProperties(this.properties);
      formatter.entityListFormatter(pw, entityList);
    } else {
      System.out.println("! Couldn't find formatter for output format option: " + outputFormatOption);
    }
    pw.close();
  } /* processFile */

  /**
   * log information about caches.
   */
  void logCacheInfo() {
    System.out.println("cui -> preferred-name cache size: " +
		       ((EntityLookup4)entityLookup).cuiPreferredNameCache.size());
    System.out.println("term -> concept cache size: " +
		       ((EntityLookup4)entityLookup).termConceptCache.size());
    System.out.println("string -> normalized string cache size: " +
		       gov.nih.nlm.nls.metamap.lite.NormalizedStringCache.normalizeStringCache.size());
    logger.info("cui -> preferred-name cache size: " +
		       ((EntityLookup4)entityLookup).cuiPreferredNameCache.size());
    logger.info("term -> concept cache size: " +
		       ((EntityLookup4)entityLookup).termConceptCache.size());
    logger.info("string -> normalized string cache size: " +
		       gov.nih.nlm.nls.metamap.lite.NormalizedStringCache.normalizeStringCache.size());
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
   * <pre>
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
	  } else if (fields[0].equals("--segmentation_method")) {
	    optionsConfiguration.setProperty ("metamaplite.segmentation.method",fields[1]);
	  } else if (fields[0].equals("--segment_sentences")) {
	      optionsConfiguration.setProperty ("metamaplite.segmentation.method","SENTENCES");
	  } else if (fields[0].equals("--segment_blanklines")) {
	      optionsConfiguration.setProperty ("metamaplite.segmentation.method","BLANKLINES");
	  } else if (fields[0].equals("--segment_lines")) {
	      optionsConfiguration.setProperty ("metamaplite.segmentation.method","LINES");
	  } else if (fields[0].equals("--freetext")) {
	    optionsConfiguration.setProperty ("metamaplite.document.inputtype","freetext");
	  } else if (fields[0].equals("--outputformat")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat",fields[1]);
	    optionsConfiguration.setProperty("metamaplite.outputextension",
					     (outputExtensionMap.containsKey(fields[1]) ?
					      outputExtensionMap.get(fields[1]) :
					      "out"));
	  } else if (fields[0].equals("--brat") || 
		     fields[0].equals("--BRAT")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat","brat");
	    optionsConfiguration.setProperty("metamaplite.outputextension",
					     (outputExtensionMap.containsKey("brat") ?
					      outputExtensionMap.get("brat") :
					      "brat"));
	  } else if (fields[0].equals("--mmi") || 
		     fields[0].equals("--mmilike")) {
	    optionsConfiguration.setProperty("metamaplite.outputformat","mmi");
	    optionsConfiguration.setProperty("metamaplite.outputextension",
					     (outputExtensionMap.containsKey("mmi") ?
					      outputExtensionMap.get("mmi") :
					      "mmi"));
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
	  } else if (fields[0].equals("--negationDetectorClass")) {
	    optionsConfiguration.setProperty("metamaplite.negation.detector", fields[1]);
	  } else if (fields[0].equals("--usecontext")) {
	    optionsConfiguration.setProperty("metamaplite.negation.detector",
					     "gov.nih.nlm.nls.metamap.lite.context.ContextWrapper");
	  } else if (fields[0].equals("--enable_postagging")) {
	    optionsConfiguration.setProperty("metamaplite.enable.postagging",fields[1]);
	  } else if (fields[0].equals("--brat_type_name")) {
	    optionsConfiguration.setProperty("metamaplite.result.formatter.property.brat.typename", fields[1]);
	  } else if (args[i].equals("--filelist")) {
	    if (fields.length < 2) {
	      System.err.println("missing argument in \"" + args[i] + "\" option");
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
	  } else if (fields[0].equals("--set_property")) {
	    optionsConfiguration.setProperty(fields[1],fields[2]);
	  } else if (args[i].equals("--verbose")) {
	    verbose = true;
	  } else if (args[i].equals("--help")) {
	    Properties properties = setConfiguration(propertiesFilename,
						     defaultConfiguration,
						     System.getProperties(),
						     optionsConfiguration,
						     verbose);
	    BioCDocumentLoaderRegistry.register(properties);
	    ResultFormatterRegistry.register(properties);
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
      Properties properties = setConfiguration(propertiesFilename,
					       defaultConfiguration,
					       System.getProperties(),
					       optionsConfiguration,
					       verbose);
      if (verbose) {
	displayProperties("properties:", properties);
      }
      MetaMapLite metaMapLiteInst = new MetaMapLite(properties);
      /** set any options in properties configuration file and system properties first */
      BioCDocumentLoaderRegistry.register(properties);

      String documentInputOption = properties.getProperty("metamaplite.document.inputtype", "freetext");
      String outputFormatOption = properties.getProperty("metamaplite.outputformat","mmi");
      String outputExtension =	properties.getProperty("metamaplite.outputextension", ".mmi");
      
      boolean listSentences =
	Boolean.parseBoolean(properties.getProperty("metamaplite.list.acronyms","false"));
      boolean listAcronyms =
	Boolean.parseBoolean(properties.getProperty("metamaplite.list.sentences","false"));
      boolean listSentencesWithPosTags =
	Boolean.parseBoolean(properties.getProperty
			     ("metamaplite.list.sentences.with.postags", "false"));
      
      String inputfileListPropValue = properties.getProperty("metamaplite.inputfilelist");
      if (inputfileListPropValue != null) {
	if (filenameList.size() > 0) {
	  filenameList.addAll(Arrays.asList(inputfileListPropValue.split(",")));
	} else {
	  filenameList = Arrays.asList(inputfileListPropValue.split(","));
	}
      }
      String inputfileListFilenamePropValue = properties.getProperty("metamaplite.inputfilelist.filename");
      if (inputfileListFilenamePropValue != null) {
	if (filenameList.size() > 0) {
	  filenameList.addAll(loadInputFileList(inputfileListFilenamePropValue));
	} else {
	  filenameList = loadInputFileList(inputfileListFilenamePropValue);
	}
      }
      
      // load documents
      logger.debug("documentInputOutput: " + documentInputOption);
      BioCDocumentLoader docLoader = new FreeText();
      if (BioCDocumentLoaderRegistry.contains(documentInputOption)) {
	docLoader = BioCDocumentLoaderRegistry.get(documentInputOption);
	if (docLoader == null) {
	  throw new Exception("Fatal: Document loader for input option \"" +
			      documentInputOption +
			      "\" is not instantiated check configuration or properties");
	}
      } else {
	logger.fatal("Document loader for input option \"" +
		     documentInputOption + "\" is not available.");

	throw new Exception("Fatal: Document loader for input option \"" +
			    documentInputOption + "\" is not available. Check properties or configuration.");
      }
      if (inputFromStdin) {
	if (verbose) {
	  logger.info("Reading and processing documents from standard input");
	}
	List<BioCDocument> documentList = docLoader.readAsBioCDocumentList(new InputStreamReader(System.in));
	if (listSentences) {
	  metaMapLiteInst.listSentences(documentList);
	} else if (listAcronyms) {
	  metaMapLiteInst.listAcronyms(documentList);
	} else if (listSentencesWithPosTags) {
	  metaMapLiteInst.listSentencesWithPosTags(documentList);
	} else {
	  metaMapLiteInst.listEntities(documentList,outputFormatOption);
	}
      } else {
	logger.info("Loading and processing documents");
	for (String filename: filenameList) {
	  if (verbose) {
	    System.out.println("Loading and processing " + filename);
	  }
	  logger.info("Loading and processing " + filename);
	  List<BioCDocument> documentList = docLoader.loadFileAsBioCDocumentList(filename);
	  if (listSentences) {
	    metaMapLiteInst.listSentences(filename, documentList);
	  } else if (listAcronyms) {
	    metaMapLiteInst.listAcronyms(filename, documentList);
	  } else if (listSentencesWithPosTags) {
	    metaMapLiteInst.listSentencesWithPosTags(filename, documentList);
	  } else {
	    metaMapLiteInst.listEntities(filename, documentList,
					 outputExtension, outputFormatOption);
	  }
	} /*for filename */
      }
      metaMapLiteInst.logCacheInfo();
    } else {
      // register default document loaders and result formatter for help display.
      BioCDocumentLoaderRegistry.register(defaultConfiguration);
      ResultFormatterRegistry.register(defaultConfiguration);
      displayHelp();
      System.exit(1);
    }   
  }
}
