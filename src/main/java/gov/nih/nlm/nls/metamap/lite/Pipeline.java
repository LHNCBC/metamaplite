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

import gov.nih.nlm.nls.types.Sentence;

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

  public Object processSentence(Sentence sentence)
    throws IllegalAccessException, InvocationTargetException
  {
    System.out.println("processSentence");
    List<Plugin> pipeSequence = PipelineRegistry.get("simple.sentence");
    Object current = sentence;
    Object result = null;
    for (Plugin plugin: pipeSequence) {
      result = plugin.getMethod().invoke(plugin.getClassInstance(), current);
      current = result;
    }
    return result;
  }

  public List<Object> processSentenceList(List<Sentence> sentenceList) 
    throws IllegalAccessException, InvocationTargetException
  {
    System.out.println("processSentenceList");
    List<Object> resultList = new ArrayList<Object>();
    for (Sentence sentence: sentenceList) {
      resultList.add(this.processSentence(sentence));
    }
    return resultList;
  }

  static Pipeline initPipeline()
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException {
    Properties properties = new Properties();
    properties.load(new FileReader("metamaplite.properties"));
    for (Map.Entry<Object,Object> entry: properties.entrySet()) {
      System.out.println(entry.getKey() + " -> " + entry.getValue());
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

  public List<String> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<String> lineList = new ArrayList<String>();
    String line;
    while ((line = br.readLine()) != null) {
      lineList.add(line);
    }
    br.close();
    return lineList;
  }

  /**
   *
   * @param args - Arguments passed from the command line
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   ParseException , InvocationTargetException {
    if (args.length > 0) {
      Pipeline pipeline = initPipeline();
      List<String> documentList = pipeline.loadFile(args[0]);
      
      /*CHEMDNER style documents*/
      for (String doc: documentList) {
	String[] docFields = doc.split("\\|");
	String docId = docFields[0];
	String docBody = docFields[1];
	String[] bodyFields = docBody.split("\t");
	String docTitle = bodyFields[0];
	String docAbstract = bodyFields[1];

	pipeline.processText(docTitle);
	pipeline.processText(docAbstract);
      }
    } else {
      System.err.println("usage: filename");
    }   
  }
    
}
