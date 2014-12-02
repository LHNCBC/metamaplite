
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

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
import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCSentence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */

public class BioCPipeline {
  /** log4j logger instance */
  private static final Logger logger = LogManager.getLogger(Pipeline.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/bioc.metamaplite.properties");

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
      System.out.println(passage.getText());
      this.processPassage(passage);
    }
    return document;
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
    BioCPipeline pipeline = new BioCPipeline();
    PluginRegistry.registerPlugins(properties);
    System.out.println("plugins:");
    for (String name: PluginRegistry.listPlugins()) {
      System.out.println(" " + name);
    }
    System.out.println("pipesequence keys:");
    PipelineRegistry.registerPipeSequences("metamaplite.pipeline", properties);
    for (String content: PipelineRegistry.listPipeContents()) {
      System.out.println(" " + content);
    }
    logger.debug("exit initPipeline");
    return pipeline;
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
   * The application currently only outputs to standard output. (See method: gov.nih.nlm.nls.metamap.lite.EntityLookup.displayEntitySet) 
   * </pre>
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   ParseException , InvocationTargetException {
    if (args.length > 0) {
      BioCPipeline pipeline = initPipeline();
      String filename = null;
      String option = "--freetext";
      int i = 0;
      while (i < args.length) {
	if (args[i].equals("--chemdnersldi")) {
	  option = args[i];
	} else if (args[i].equals("--chemdner")) {
	  option = args[i];
	} else if (args[i].equals("--freetext")) {
	  option = args[i];
	} else {
	  filename = args[i];
	}
	i++;
      }

      if (option.equals("--chemdnersldi")) {
	List<BioCDocument> documentList = ChemDNER.bioCLoadSLDIFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (BioCDocument document: documentList) {
	  pipeline.processDocument(document);
	}
      } else if (option.equals("--chemdner")) {
	List<BioCDocument> documentList = ChemDNER.bioCLoadFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (BioCDocument document: documentList) {
	  pipeline.processDocument(document);
	} 
      } else if (option.equals("--ncbicorpus")) {
	List<BioCDocument> documentList = NCBICorpusDocument.bioCLoadFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (BioCDocument document: documentList) {
	  pipeline.processDocument(document);
	} 
      } else if (option.equals("--freetext")) {
	String inputtext = FreeText.loadFile(filename);
	System.out.println(inputtext);
	BioCPassage passage = new BioCPassage();
	passage.setText(inputtext);
	passage.putInfon("freetext", "freetext");
	pipeline.processPassage(passage);
      }

    } else {
      System.err.println("usage: [options] filename");
      System.err.println("options:");
      System.err.println("  --freetext (default)");
      System.err.println("  --ncbicorpus");
      System.err.println("  --chemdner");
      System.err.println("  --chemdnersldi");
    }   
  }
}
