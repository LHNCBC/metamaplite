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

import org.apache.lucene.queryparser.classic.ParseException;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.Plugin;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PluginRegistry;
import gov.nih.nlm.nls.metamap.lite.pipeline.plugins.PipelineRegistry;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.Document;
import gov.nih.nlm.nls.metamap.document.FreeText;

import gov.nih.nlm.nls.types.Sentence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * What should a pipeline look-like?

 * pipe-elements: (properties)
 * <pre>
 * metamaplite.pipe.element.<element-name>: transformation method|input class|output class
 * </pre>
 *
 * pipeline:  (one property line)
 * <pre>
 * metamaplite.pipeline.<name>: element1|element2|...
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
  private static final Logger logger = LogManager.getLogger(Pipeline.class);
  /** location of metamaplite.properties configuration file */
  static String configPropertyFilename =
    System.getProperty("metamaplite.property.file", "config/metamaplite.properties");

  /**
   * Apply processing pipeline to sentence.
   * @param sentence String containing a properly segmented sentence.
   * @return result from final plugin in processing pipeline.
   */
  public Object processSentence(Sentence sentence)
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("processSentence");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.sentence");
    Object current = sentence;
    Object result = null;
    for (Plugin plugin: pipeSequence) {
      result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
    return result;
  }

  /**
   * Invoke sentence processing pipeline on each sentence in supplied sentence list.
   * @param sentenceList list of strings, one sentence per string.
   * @return list of results from sentence processing pipeline, one per sentence in input list.
   */
  public List<Object> processSentenceList(List<Sentence> sentenceList) 
    throws IllegalAccessException, InvocationTargetException
  {
    logger.debug("processSentenceList");
    List<Object> resultList = new ArrayList<Object>();
    for (Sentence sentence: sentenceList) {
      resultList.add(this.processSentence(sentence));
    }
    return resultList;
  }

  /**
   * Initialize pipeline application.
   * @return pipeline application instance
   */
  static Pipeline initPipeline()
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException {
    Properties properties = new Properties();
    properties.load(new FileReader(configPropertyFilename));
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
    return pipeline;
  }

  /**
   * Apply text processing pipeline to input text.
   * @param text input text
   */
  public void processText(String text)
    throws IllegalAccessException, InvocationTargetException
  {
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.text");
    Object current = text;
    for (Plugin plugin: pipeSequence) {
      Object result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
  }

  /**
   * Pipeline application commandline.
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   ParseException , InvocationTargetException {
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
	List<ChemDNER> documentList = ChemDNER.loadSLDIFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (ChemDNER document: documentList) {
	  System.out.println(document.getTitle());
	  pipeline.processText(document.getTitle());
	  System.out.println(document.getAbstract());
	  pipeline.processText(document.getAbstract());
	}
      } else if (option.equals("--chemdner")) {
	List<ChemDNER> documentList = ChemDNER.loadFile(filename);
	/*CHEMDNER SLDI style documents*/
	for (ChemDNER document: documentList) {
	  System.out.println(document.getTitle());
	  pipeline.processText(document.getTitle());
	  System.out.println(document.getAbstract());
	  pipeline.processText(document.getAbstract());
	} 
      } else if (option.equals("--freetext")) {
	String inputtext = FreeText.loadFile(filename);
	System.out.println(inputtext);
	pipeline.processText(inputtext);
      }

    } else {
      System.err.println("usage: [options] filename");
    }   
  }
    
}
