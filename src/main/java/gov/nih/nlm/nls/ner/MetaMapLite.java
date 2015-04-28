//
package gov.nih.nlm.nls.ner;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

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

import org.apache.lucene.queryparser.classic.ParseException;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.Plugin;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PluginRegistry;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PipelineRegistry;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.MarkAbbreviations;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.EntityLookup2;
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

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.util.Span;

/**
 * Properties precedence (from highest to lowest):
 * <ul>
 *   <li>Command line options</li>
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

  Set<String> semanticGroup = new HashSet<String>(); // initially empty

  AbbrConverter abbrConverter = new AbbrConverter();
  Properties properties;

  boolean useContext = false;

  public MetaMapLite(Properties properties)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException
  {
    this.properties = properties;
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

    outputExtensionMap.put("brat",".ann");
    outputExtensionMap.put("mmi",".mmi");
    outputExtensionMap.put("cdi",".cdi");
  }

  void setSemanticGroup(String[] semanticTypeList) {
    this.semanticGroup = new HashSet<String>(Arrays.asList(semanticTypeList));
  }

  /**
   * Invoke sentence processing pipeline on a sentence
   * @param sentence
   * @return updated sentence
   */
  public BioCSentence processSentence(BioCSentence sentence, BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, 
	   IOException, ParseException, Exception
  {
    logger.debug("enter processSentence");
    BioCSentence result0 = 
      SentenceAnnotator.addEntities
      (SentenceAnnotator.tokenizeSentence(sentence), passage);
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
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException, Exception
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
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException, Exception
  {
    logger.debug("enter processPassage");
    logger.debug(passage.getText());
    BioCPassage passageWithSentences = SentenceExtractor.createSentences(passage);
    BioCPassage passageWithSentsAndAbbrevs = abbrConverter.getPassage(passageWithSentences);
    logger.info("passage relations: " + passageWithSentsAndAbbrevs.getRelations());
    logger.info("passage annotations: " + passageWithSentsAndAbbrevs.getAnnotations());
    // BioCPassage newPassage = processSentences(passageWithSentsAndAbbrevs);
    List<Entity> entityList =
      MarkAbbreviations.markAbbreviations
      (passageWithSentsAndAbbrevs,
       EntityLookup2.processPassage
       ("0000000.tx", passageWithSentsAndAbbrevs, this.useContext, this.semanticGroup));
    logger.debug("exit processPassage");
    return entityList;
  }

  public List<Entity> processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException, Exception
  {
    List<Entity> entityList = new ArrayList<Entity>();    
    for (BioCPassage passage: document.getPassages()) {
      entityList.addAll(processPassage(passage));
    }
    return entityList;
  }

  public List<Entity> processDocumentList(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException, Exception
  {
    List<Entity> entityList = new ArrayList<Entity>();    
    for (BioCDocument document: documentList) {
      entityList.addAll(this.processDocument(document));
    }
    return entityList;
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
    System.err.println("  --luceneresultlen");
    System.err.println("  --outputformat=<format type>");
    System.err.println("    Available format types:");  
    for (String name: ResultFormatterRegistry.listNameSet()) {
      System.err.println("      " + name);
    }
    System.err.println("processing options:");
    System.err.println("  --restrict-to-sts=<semtype>[,<semtype>,<semtype>...]");
    System.err.println("performance/effectiveness options:");
    System.err.println("  --luceneresultlen=<length>");
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
	   ParseException , InvocationTargetException, 
	   Exception
  {
    if (args.length > 0) {
      MetaMapLite metaMapLiteInst = initMetaMapLite();
      Properties properties = metaMapLiteInst.properties;

      /** set any options in properties configuration file and system properties first */
      BioCDocumentLoaderRegistry.register(properties);

      List<String> filenameList = new ArrayList<String>();
      String documentInputOption =
	properties.getProperty("metamaplite.document.inputtype", "freetext");
      String outputFormatOption =
 	properties.getProperty("metamaplite.outputformat", "mmi");
      String outputExtension =
 	properties.getProperty("metamaplite.outputextension",  ".mmi");
      String outputFile =
 	properties.getProperty("metamaplite.outputfilename", null);
      String entityLookupResultLengthString = 
 	properties.getProperty("metamaplite.entitylookup.resultlength", "");
      metaMapLiteInst.setSemanticGroup
	(properties.getProperty("metamaplite.semanticgroup", "all").split(","));
      metaMapLiteInst.useContext = 
	Boolean.parseBoolean(properties.getProperty("metamaplite.usecontext", "false"));
      int i = 0;
      while (i < args.length) {
        if (args[i].substring(0,2).equals("--")) {
	  String[] fields = args[i].split("=");
	  if (fields[0].equals("--inputdocformat")) {
	    documentInputOption = fields[1];
	  } else if (fields[0].equals("--freetext")) {
	    documentInputOption = "freetext";
	  } else if (fields[0].equals("--outputformat")) {
	    outputFormatOption = fields[1];
	    outputExtension = outputExtensionMap.get(outputFormatOption);
	  } else if (fields[0].equals("--brat") || 
		     fields[0].equals("--BRAT")) {
	    outputFormatOption = "brat";
	    outputExtension = outputExtensionMap.get(outputFormatOption);
	  } else if (fields[0].equals("--mmi") || 
		     fields[0].equals("--mmilike")) {
	    outputFormatOption = "mmi";
	    outputExtension = outputExtensionMap.get(outputFormatOption);
	  } else if (fields[0].equals("--luceneresultlen")) {
	    entityLookupResultLengthString = fields[1];
	  } else if (fields[0].equals("--restrict-to-semantic-types") ||
		     fields[0].equals("--restrict-to-sts") ||
		     fields[0].equals("--restrict_to_semantic-types") ||
		     fields[0].equals("--restrict_to_sts")) {
	    String[] semanticTypeList = fields[1].split(",");
	    metaMapLiteInst.setSemanticGroup(semanticTypeList);
	  } else if (fields[0].equals("--usecontext")) {
	    metaMapLiteInst.useContext = true;
	  } else if (args[i].equals("--help")) {
	    displayHelp();
	    System.exit(1);
	  }
	} else {
	  filenameList.add(args[i]);
	}
	i++;
      }

      if (entityLookupResultLengthString.length() > 0) {
	System.setProperty("metamaplite.entitylookup.resultlength", 
			   entityLookupResultLengthString);
      }
 
      logger.info("Loading and processing documents");
      for (String filename: filenameList) {
	System.out.println("Loading and processing " + filename);
	logger.info("Loading and processing " + filename);

	// load documents
	BioCDocumentLoader docLoader = BioCDocumentLoaderRegistry.get(documentInputOption);
	List<BioCDocument> documentList = docLoader.loadFileAsBioCDocumentList(filename);

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
      } /* for filename */

    } else {
      displayHelp();
      System.exit(1);
    }   
  }

}
