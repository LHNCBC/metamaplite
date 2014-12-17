
//
package gov.nih.nlm.nls.ner;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

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
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;
import gov.nih.nlm.nls.metamap.document.SingleLineInput;

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCSentence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.util.Span;
import gov.nih.nlm.nls.metamap.lite.SemanticGroupFilter;
import gov.nih.nlm.nls.metamap.lite.SemanticGroups;


public class MetaMapLite {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(MetaMapLite.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");

  Properties properties;

  public MetaMapLite(Properties properties) {
    this.properties = properties;
  }

  /**
   * Invoke sentence processing pipeline on asentence
   * @param sentence
   * @return updated sentence
   */
  public static BioCSentence processSentence(BioCSentence sentence)
    throws IllegalAccessException, InvocationTargetException, 
	   IOException, ParseException
  {
    logger.debug("enter processSentence");
    BioCSentence result = 
      SemanticGroupFilter.keepEntitiesInSemanticGroup
       (SemanticGroups.getDisorders(), 
	SentenceAnnotator.addEntities
	(SentenceAnnotator.tokenizeSentence(sentence)));
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
      resultList.add(MetaMapLite.processSentence(sentence));
    }
    passage.setSentences(resultList);
    logger.debug("exit processSentences");
    return passage;
  }

  public BioCPassage processPassage(BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
  {
    logger.debug("enter processPassage");
    BioCPassage newPassage = MetaMapLite.processSentences(SentenceExtractor.createSentences(passage));
    logger.debug("exit processPassage");
    return newPassage;
  }

  public BioCDocument processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
  {
    for (BioCPassage passage: document.getPassages()) {
      logger.info(passage.getText());
      this.processPassage(passage);
    }
    return document;
  }

  public List<BioCDocument> processDocumentList(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException, ParseException
  {
    List<BioCDocument> newDocumentList = new ArrayList<BioCDocument>();
    for (BioCDocument document: documentList) {
      newDocumentList.add(this.processDocument(document));
    }
    return newDocumentList;
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
    System.err.println("usage: [options] filename");
    System.err.println("options:");
    System.err.println("  --freetext (default)");
    System.err.println("  --ncbicorpus");
    System.err.println("  --chemdner");
    System.err.println("  --chemdnersldi");
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
   * gov.nih.nlm.nls.metamap.lite.EntityLookup.displayEntitySet)
   * </pre>
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   ParseException , InvocationTargetException {
    if (args.length > 0) {
      MetaMapLite metaMapLite = initMetaMapLite();
      String filename = null;
      String processingOption = "--freetext";
      String displayOption = "--mmi";
      String outputFile = null;
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
	  displayOption = args[i];
	} else if (args[i].equals("--mmi")) {
	  displayOption = args[i];
	} else {
	  filename = args[i];
	}
	i++;
      }

      List<BioCDocument> newDocumentList = new ArrayList<BioCDocument>();;
      if (processingOption.equals("--chemdnersldi")) {
	List<BioCDocument> documentList = ChemDNER.bioCLoadSLDIFile(filename);
	/*CHEMDNER SLDI style documents*/
	newDocumentList = metaMapLite.processDocumentList(documentList);
      } else if (processingOption.equals("--chemdner")) {
	List<BioCDocument> documentList = ChemDNER.bioCLoadFile(filename);
	/*CHEMDNER SLDI style documents*/
	newDocumentList = metaMapLite.processDocumentList(documentList);
      } else if (processingOption.equals("--ncbicorpus")) {
	List<BioCDocument> documentList = NCBICorpusDocument.bioCLoadFile(filename);
	/*CHEMDNER SLDI style documents*/
	newDocumentList = metaMapLite.processDocumentList(documentList);
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
	newDocumentList = metaMapLite.processDocumentList(documentList);
      } else if (processingOption.equals("--sli")) {
	List<BioCDocument> documentList = SingleLineInput.bioCLoadFile(filename);
	/*Single line documents*/
	newDocumentList = metaMapLite.processDocumentList(documentList);
      } else if (processingOption.equals("--help")) {
	displayHelp();
	System.exit(1);
      } 

      if (displayOption.equals("--bc-evaluate") ||
	  displayOption.equals("--bc") ||
	  displayOption.equals("--bioc") ||
	  displayOption.equals("--cdi")) {
	logger.info("writing BC evaluate format file...");
	for (BioCDocument document: newDocumentList) {
	  EntityLookup.writeBcEvaluateAnnotations(System.out, document);
	}
      } else if (displayOption.equals("--mmi")) {
	for (BioCDocument document: newDocumentList) {
	  EntityLookup.writeEntities(System.out, document);
	}
      }


    } else {
      displayHelp();
      System.exit(1);
    }   
  }
}
