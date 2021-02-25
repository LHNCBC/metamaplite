//
package gov.nih.nlm.nls.metamap.lite;

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


import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.Plugin;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PluginRegistry;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PipelineRegistry;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.ChemDNERSLDI;
import gov.nih.nlm.nls.metamap.document.PubMedDocumentImpl;
import gov.nih.nlm.nls.metamap.document.PubMedDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;

import gov.nih.nlm.nls.types.Sentence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What should a pipeline look-like?

 * pipe-elements: (properties)
 * <pre>
 * metamaplite.pipe.element.{element-name}: transformation method|input class|output class
 * </pre>
 *
 * pipeline:  (one property line)
 * <pre>
 * metamaplite.pipeline.{name}: element1|element2|...
 * </pre>
 *
 *<pre>
 * metamaplite.pipe.element.analyzetext: gov.nih.nlm.nls.metamap.prefix.analyzeText|java.lang.String|java.util.List
 * metamaplite.pipe.element.add-pos-tags: ...
 * metamaplite.pipe.element.add-entitylists: ...
 * metamaplite.pipe.element.add-preferred-names: ...
 * metamaplite.pipe.element.add-scores: ...
 * metamaplite.pipe.element.filter-entities: ...
 * </pre>
 *
 * <pre>
 * metamaplite.pipeline.simple.sentence: analyzetext|remove-ws-tokens|add-pos-tags|add-entitylists|add-preferred-names|add-scores|filter-entities
 * </pre>
 */

public class Pipeline {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");

  /**
   * Apply processing pipeline to sentence.
   * @param sentence String containing a properly segmented sentence.
   * @return result from final plugin in processing pipeline.
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class
   */
  public Object processSentence(Sentence sentence)
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processSentence");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.sentence");
    Object current = sentence;
    Object result = null;
    for (Plugin plugin: pipeSequence) {
      result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
    logger.debug("exit processSentence");
    return result;
  }

  /**
   * Invoke sentence processing pipeline on each sentence in supplied sentence list.
   * @param sentenceList list of strings, one sentence per string.
   * @return list of results from sentence processing pipeline, one per sentence in input list.
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class
   */
  public List<Object> processSentenceList(List<Sentence> sentenceList) 
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processSentenceList");
    List<Object> resultList = new ArrayList<Object>();
    for (Sentence sentence: sentenceList) {
      resultList.add(this.processSentence(sentence));
    }
    logger.debug("exit processSentenceList");
    return resultList;
  }

  /**
   * Initialize pipeline application.
   * @return pipeline application instance
   * @throws ClassNotFoundException class not found exception
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws IllegalAccessException illegal access of class
   * @throws InstantiationException exception instantiating instance of class
   * @throws NoSuchMethodException  no method in class
   */
  static Pipeline initPipeline()
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException {
    logger.debug("enter initPipeline");

    Properties properties = new Properties();
    FileReader fr = new FileReader(configPropertyFilename);
    properties.load(fr);
    fr.close();
    if (logger.isDebugEnabled()) {
      for (Map.Entry<Object,Object> entry: properties.entrySet()) {
	logger.debug(entry.getKey() + " -> " + entry.getValue());
      }
    }
    Pipeline pipeline = new Pipeline();
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
   * Apply text processing pipeline to input text.
   * @param text input text
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class 
   */
  public void processText(String text)
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("enter processText");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.text");
    Object current = text;
    for (Plugin plugin: pipeSequence) {
      Object result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
    logger.debug("exit processText");
  }

  public void processDocument(PubMedDocument document)
    throws IllegalAccessException, InvocationTargetException
  {
    System.out.println(document.getTitle());
    this.processText(document.getTitle());
    System.out.println(document.getAbstract());
    this.processText(document.getAbstract());
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
   * The application currently only outputs to standard output.
   * (See method: gov.nih.nlm.nls.metamap.lite.EntityLookup.displayEntitySet) 
   * @param args - Arguments passed from the command line
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws InstantiationException exception instantiating instance of class
   * @throws NoSuchMethodException  no method in class
   * @throws IllegalAccessException illegal access of class
   * @throws InvocationTargetException exception while invoking target class
   * @throws ClassNotFoundException class not found exception
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException {
    if (args.length > 0) {
      Pipeline pipeline = initPipeline();
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
	List<PubMedDocument> documentList = ChemDNERSLDI.loadSLDIFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (PubMedDocument document: documentList) {
	  pipeline.processDocument(document);
	}
      } else if (option.equals("--chemdner")) {
	List<PubMedDocument> documentList = ChemDNER.loadFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (PubMedDocument document: documentList) {
	  pipeline.processDocument(document);
	} 
      } else if (option.equals("--ncbicorpus")) {
	List<PubMedDocument> documentList = NCBICorpusDocument.loadFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (PubMedDocument document: documentList) {
	  pipeline.processDocument(document);
	} 
      } else if (option.equals("--freetext")) {
	String inputtext = FreeText.loadFile(filename);
	System.out.println(inputtext);
	pipeline.processText(inputtext);
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
