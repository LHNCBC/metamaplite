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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

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

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.util.Span;

public class MetaMapLite {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(MetaMapLite.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");

  static final int BRAT_FORMAT = 1;
  static final int CDI_FORMAT  = 2;
  static final int MMI_FORMAT  = 3;

  AbbrConverter abbrConverter = new AbbrConverter();
  Properties properties;

  public MetaMapLite(Properties properties) {
    this.properties = properties;
  }

  /**
   * Invoke sentence processing pipeline on asentence
   * @param sentence
   * @return updated sentence
   */
  public static BioCSentence processSentence(BioCSentence sentence, BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, 
	   IOException, ParseException
  {
    logger.debug("enter processSentence");
    BioCSentence result0 = 
      SentenceAnnotator.addEntities
      (SentenceAnnotator.tokenizeSentence(sentence), passage);
    // System.out.println("unfiltered entity list: ");
    // Brat.listEntities(result0);
    BioCSentence result = 
      SemanticGroupFilter.keepEntitiesInSemanticGroup
      (SemanticGroups.getClinicalDisorders(), result0);
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
  public static BioCPassage processSentences(BioCPassage passage) 
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
  {
    logger.debug("enter processSentences");
    List<BioCSentence> resultList = new ArrayList<BioCSentence>();
    for (BioCSentence sentence: passage.getSentences()) {
      logger.info("Processing: " + sentence.getText());
      resultList.add(processSentence(sentence, passage));
    }
    passage.setSentences(resultList);
    logger.debug("exit processSentences");
    return passage;
  }

  public List<Entity> processPassage(BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
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
       SemanticGroupFilter.keepEntitiesInSemanticGroup
       (SemanticGroups.getDisordersEdited(), 
	EntityLookup2.processPassage("0000000.tx", passageWithSentsAndAbbrevs)));
    logger.debug("exit processPassage");
    return entityList;
  }

  public List<Entity> processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
  {
    List<Entity> entityList = new ArrayList<Entity>();    
    for (BioCPassage passage: document.getPassages()) {
      entityList.addAll(processPassage(passage));
    }
    return entityList;
  }

  public List<Entity> processDocumentList(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
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
    System.err.println("  --ncbicorpus");
    System.err.println("  --chemdner");
    System.err.println("  --chemdnersldi");
    System.err.println("output options:");
    System.err.println("  --bioc|cdi|bc|bc-evaluate");
    System.err.println("  --mmilike|mmi");
    System.err.println("  --mmi");
    System.err.println("  --brat");    
    System.err.println("  --luceneresultlen");
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
	   ParseException , InvocationTargetException {
    if (args.length > 0) {
      MetaMapLite metaMapLiteInst = initMetaMapLite();
      List<String> filenameList = new ArrayList<String>();
      String processingOption = "--freetext";
      int outputOption = MMI_FORMAT;
      String outputExtension = ".mmi";
      String outputFile = null;
      String entityLookupResultLengthString = "";
      int i = 0;
      while (i < args.length) {
	if (args[i].equals("--chemdnersldi")) {
	  processingOption = args[i];
	} else if (args[i].equals("--chemdner")) {
	  processingOption = args[i];
	} else if (args[i].equals("--ncbicorpus")) {
	  processingOption = args[i];
	} else if (args[i].equals("--freetext")) {
	  processingOption = args[i];
	} else if (args[i].equals("--sli")) {
	  processingOption = args[i];
	} else if (args[i].equals("--bc-evaluate") ||
		   args[i].equals("--bioc") ||
		   args[i].equals("--bc") ||
		   args[i].equals("--cdi")) {
	  outputOption = CDI_FORMAT;
	  outputExtension = ".cdi";
	} else if (args[i].equals("--mmi") || 
		   args[i].equals("--mmilike")) {
	  outputOption = MMI_FORMAT;
	} else if (args[i].equals("--brat") || 
		   args[i].equals("--BRAT")) {
	  outputOption = BRAT_FORMAT;
	  outputExtension = ".ann";
	} else if (args[i].equals("--luceneresultlen")) {
	  i++;
	  entityLookupResultLengthString = args[i];
	} else if (args[i].equals("--help")) {
	  displayHelp();
	  System.exit(1);
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
	List<Entity> entityList = new ArrayList<Entity>();
	if (processingOption.equals("--chemdnersldi")) {
	  List<BioCDocument> documentList = ChemDNERSLDI.bioCLoadSLDIFile(filename);
	  /*CHEMDNER SLDI style documents*/
 	  entityList = metaMapLiteInst.processDocumentList(documentList);
	} else if (processingOption.equals("--chemdner")) {
	  List<BioCDocument> documentList = ChemDNER.bioCLoadFile(filename);
	  /*CHEMDNER SLDI style documents*/
	  entityList = metaMapLiteInst.processDocumentList(documentList);
	} else if (processingOption.equals("--ncbicorpus")) {
	  List<BioCDocument> documentList = NCBICorpusDocument.bioCLoadFile(filename);
	  /*CHEMDNER SLDI style documents*/
	  entityList = metaMapLiteInst.processDocumentList(documentList);
	} else if (processingOption.equals("--freetext")) {
	  
	  // String inputtext = FreeText.loadFile(filename);
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
	  
	  List<BioCDocument> documentList = FreeText.loadFreeTextFile(filename);
	  entityList = metaMapLiteInst.processDocumentList(documentList);
	} else if (processingOption.equals("--sli")) {
	  List<BioCDocument> documentList = SingleLineInput.bioCLoadFile(filename);
	  /*Single line documents*/
	  entityList = metaMapLiteInst.processDocumentList(documentList);
	} else if (processingOption.equals("--help")) {
	  displayHelp();
	  System.exit(1);
	} 
	// logger.debug("document list length: " + newDocumentList.size());
	// for (BioCDocument doc: newDocumentList) {
	//   logger.debug(doc);
	// }
	
	String basename = filename.substring(0,filename.lastIndexOf(".")); // 
	String outputFilename = basename + outputExtension;
	logger.info("outputing results to " + outputFilename);

	switch (outputOption) {
	case CDI_FORMAT:
	  logger.info("writing BC evaluate format file...");
	  // for (BioCDocument document: newDocumentList) {
	  // 
	  // EntityAnnotation.writeBcEvaluateAnnotations(outputFilename, document);
	  // }
	  break;
	case BRAT_FORMAT:
	  logger.debug("writing mmi format output");
	  PrintWriter pw = new PrintWriter(new BufferedWriter
					   (new FileWriter(outputFilename)));
	  Brat.writeAnnotationList("MMLite", pw, entityList);
	  pw.close();
	  break;
	case MMI_FORMAT:
	  logger.debug("writing mmi format output");
	  MMI.displayEntityList(entityList);
	  break;
      default:
	  logger.debug("writing mmi format output");
	  MMI.displayEntityList(entityList);
	}
      } /* for filename */

    } else {
      displayHelp();
      System.exit(1);
    }   
  }
}
