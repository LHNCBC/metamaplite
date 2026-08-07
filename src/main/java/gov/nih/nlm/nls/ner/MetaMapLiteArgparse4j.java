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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import bioc.BioCDocument;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.impl.Arguments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>Using MetaMapLiteArgparse4j from a Java program: don't do it!</h2>
 */
public class MetaMapLiteArgparse4j {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(MetaMapLiteArgparse4j.class);

  static Map<String,String> outputExtensionMap = new HashMap<String,String>();
  static {
    outputExtensionMap.put("bioc",".bioc");
    outputExtensionMap.put("brat",".ann");
    outputExtensionMap.put("mmi",".mmi");
    outputExtensionMap.put("cdi",".cdi");
    outputExtensionMap.put("cuilist",".cuis");
    outputExtensionMap.put("json",".json");
  }

  static Properties setOptionsConfiguation(Namespace ns) {
    Properties optionsConfiguration = new Properties();
    Map<String,Object> nsAttrs = ns.getAttrs();

    for (Map.Entry<String,Object> entry: nsAttrs.entrySet()) {
      if (entry.getKey() != null) {
	if (Boolean.parseBoolean(ns.getString("verbose"))) {
	  System.out.println("setProperty: metamaplite." + entry.getKey() + "-> " + ns.getString(entry.getKey()));
	}
	if ( ns.getString(entry.getKey()) != null ) {
	  optionsConfiguration.setProperty("metamaplite." + entry.getKey(), ns.getString(entry.getKey()));
	}
      }
    }
    if (nsAttrs.containsKey("postagging")) {
      optionsConfiguration.setProperty("metamaplite.enable.postagging", ns.getString("postagging"));
    }
    if (nsAttrs.containsKey("inputformat")) {
      optionsConfiguration.setProperty("metamaplite.document.inputtype", ns.getString("inputformat"));
    }
    if (nsAttrs.containsKey("restrict_to_sts")) {
      optionsConfiguration.setProperty("metamaplite.semanticgroup", ns.getString("restrict_to_sts"));
    }
    if (nsAttrs.containsKey("restrict_to_sources")) {
      optionsConfiguration.setProperty("metamaplite.sourceset", ns.getString("restrict_to_sources"));
    }
    if (nsAttrs.containsKey("indexdir")) {
      optionsConfiguration.setProperty("metamaplite.index.directory", ns.getString("indexdir"));
    }
    if (nsAttrs.containsKey("scheduler")) {
      optionsConfiguration.setProperty("metamaplite.from.scheduler", ns.getString("scheduler"));
    }
    if (ns.getString("uda") != null) {
      optionsConfiguration.setProperty("metamaplite.uda.filename", ns.getString("uda"));
    }
    if (nsAttrs.containsKey("outputformat")) {
      String outputFormat = ns.getString("outputformat");
      optionsConfiguration.setProperty("metamaplite.outputformat", outputFormat);
      optionsConfiguration.setProperty("metamaplite.outputextension",
				       (outputExtensionMap.containsKey(outputFormat) ?
					outputExtensionMap.get(outputFormat) :
					".out"));
    }
    if (nsAttrs.containsKey("output_extension")) {
      if (ns.getString("output_extension") != null) {
	optionsConfiguration.setProperty("metamaplite.outputextension", ns.getString("output_extension"));
      }
    }
    
    optionsConfiguration.setProperty("metamaplite.segmentation.method", ns.getString("segmentation_method"));
    if (nsAttrs.containsKey("dbtype")) {
      optionsConfiguration.setProperty("metamaplite.dbtype", ns.getString("dbtype"));
    }

    if (nsAttrs.containsKey("set_property")) {
      if (ns.getString("set_property") != null) {
	String[] fields = ns.getString("set_property").split("=");
	if (fields.length == 2) {
	  optionsConfiguration.setProperty(fields[0], fields[1]);
	}
      }
    }
    return optionsConfiguration;
  }


  /**
   * MetaMapLiteArgparse4j application commandline.
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
   * @throws Exception general exception
   * @throws ClassNotFoundException class not found exception
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   * @throws IllegalAccessException illegal access of class
   * @throws InstantiationException exception instantiating instance of class
   * @throws InvocationTargetException exception while invoking target class 
   * @throws NoSuchMethodException  no method in class
   */
  public static void main(String[] args)
    throws IOException, FileNotFoundException,
	   ClassNotFoundException, InstantiationException,
	   NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException,
	   Exception
  {
    Properties defaultConfiguration = MetaMapLiteConfiguration.getDefaultConfiguration();
    boolean verbose = false;
    boolean inputFromStdin = false;
    List<String> filenameList = new ArrayList<String>();
    String propertiesFilename = System.getProperty("metamaplite.propertyfile", "config/metamaplite.properties");

    ArgumentParser parser = ArgumentParsers.newFor("MetaMapLiteArgparse4j").build()
      .defaultHelp(true)
      .description("Find concepts in text.");
      
    parser.addArgument("-i", "--inputformat")
      .setDefault("freetext")
      .help("specify parser for input documents");
    parser.addArgument("-o", "--outputformat")
      .setDefault("mmi")
      .help("specify format for output results");
    parser.addArgument("-p", "--pipe")
      .help("pipe documents and results using standard input and output")
      .action(storeTrue());
    parser.addArgument("-e", "--output_extension")
      .help("specify output file extension for output results");

    parser.addArgument("--restrict_to_sts")
      .setDefault("all")
      .help("restrict results to concepts with semantic types");
    parser.addArgument("--restrict_to_sources")
      .setDefault("all")
      .help("restrict results to concepts from vocabulary sourcess");
    parser.addArgument("--segmentation_method")
      .setDefault("SENTENCES")
      .help("set method for text segmentation:SENTENCES, BLANKLINES, or LINES ");

    parser.addArgument("--postagging")
      .setDefault("enable")
      .help("Use part-of-speech tagging: enable or disable.");

    parser.addArgument("--configfile")
      .help("Use configuration file other than distribution file.")
      .setDefault("config/metamaplite.properties");
    parser.addArgument("--indexdir")
      .help("Set directory containing UMLS indexes")
      // .setDefault("data/mapdb/pubchem-mesh");
      .setDefault("data/ivf/strict");
    parser.addArgument("--modelsdir")
      .help("Set OpenNLP model directory")
      .setDefault("data/models");

    parser.addArgument("--specialtermsfile")
      .help("Set location of specialterms file")
      .setDefault("data/specialterms.txt");
      
    parser.addArgument("--filelistfn")
      .help("name of file containing list of files to be processed.");

    parser.addArgument("--filelist")
      .help("comma-separated list of files to be processed.");
    parser.addArgument("--uda")
      .help("user defined acronyms file.");
    parser.addArgument("--set_property")
      .help("set property");

    parser.addArgument("-E","--indication_citation_end")
      .help("emit citation end at end of input.")
      .action(storeTrue());

    parser.addArgument("--overwrite").help("if output files exist, overwrite.")
      .action(storeTrue());

    parser.addArgument("--list_sentences_postags").help("list sentences with part-of-speech tags, skip entity recognition")
      .action(storeTrue());

    parser.addArgument("--list_sentences").help("list sentences, skip entity recognition")
      .action(storeTrue());

    parser.addArgument("--list_acronyms").help("list acronyms, skip entity recognition")
      .action(storeTrue());

    parser.addArgument("--list_chunks").help("list phrase chunks, skip entity recognition")
      .action(storeTrue());

    parser.addArgument("--scheduler").help
      (" use: \"program inputfilename outputfilename\" scheduler convention.")
      .action(storeTrue());

    parser.addArgument("--dbtype").help("database type: ivf or mapdb").setDefault("ivf");

    parser.addArgument("--enable_chunker").setDefault("false").action(storeTrue());

    parser.addArgument("--list_formats").setDefault("false").help("List available input and output formats.").action(storeTrue());

    parser.addArgument("-v", "--verbose").action(Arguments.storeTrue()).help("turn on verbose output.");

    parser.addArgument("file").nargs("*")
      .help("input document file(s)");
      
    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
    
    Map<String,Object> nsAttrs = ns.getAttrs();      
    verbose = Boolean.parseBoolean(ns.getString("verbose"));
    if (verbose) {
      System.out.println(ns);
    }
    Properties optionsConfiguration = setOptionsConfiguation(ns);
    if (verbose) {
      System.out.println(ns);
      for (Map.Entry entry: optionsConfiguration.entrySet()) {
	System.out.println(entry.getKey() + " -> " + entry.getValue());
      }
    }
    Properties properties =
      MetaMapLiteConfiguration.setConfiguration(propertiesFilename,
						defaultConfiguration,
						System.getProperties(),
						optionsConfiguration,
						verbose);
    if (verbose) {
      for (Map.Entry entry: properties.entrySet()) {
	System.out.println(entry.getKey() + " -> " + entry.getValue());
      }
    }
    try {
      Process processInst = new Process(properties);
      processInst.process(ns);
    } catch (Exception e) {
      System.out.println("MetaMapLiteArgparse4j: " + e);
      e.printStackTrace();
    }
  }
}
