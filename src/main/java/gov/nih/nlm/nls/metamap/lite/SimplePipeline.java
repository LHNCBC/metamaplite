//
package gov.nih.nlm.nls.metamap.lite;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.TokenListUtils;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreComparator;
// import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreConceptNameComparator;
import gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */

public class SimplePipeline {
  private static final Logger logger = LoggerFactory.getLogger(SimplePipeline.class);
  public SentenceModel sentenceModel;
  public SentenceDetectorME sentenceDetector;
  public TokenizerModel model;
  public Tokenizer tokenizer;
  public POSModel posModel;
  public POSTaggerME tagger;
  public MetaMapEvaluation metaMapEvalInst;
  public MetaMapIvfIndexes mmIndexes;


  EntityLookup4 entityLookup;

  public void initSentenceDetector()
    throws IOException, FileNotFoundException
  {
    InputStream modelIn =
      new FileInputStream(System.getProperty("en-sent.bin.path",
					     "data/models/en-sent.bin"));
    try {
      this.sentenceModel = new SentenceModel(modelIn);
      this.sentenceDetector = new SentenceDetectorME(sentenceModel);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException e) {
	}
      }
    }

  }

  public void initTokenizer()
    throws IOException, FileNotFoundException
  {
    InputStream modelIn =
      new FileInputStream(System.getProperty("en-token.bin.path",
					     "data/models/en-token.bin"));

    try {
      this.model = new TokenizerModel(modelIn);
      this.tokenizer = new TokenizerME(model);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException e) {
	}
      }
    }
  }

  public void initPOSTagger()
    throws IOException, FileNotFoundException
  {
    InputStream modelIn = 
      new FileInputStream(System.getProperty("opennlp.en-pos.bin.path",
					     "data/models/en-pos-maxent.bin"));

    try {
      this.posModel = new POSModel(modelIn);
      this.tagger = new POSTaggerME(posModel);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException e) {
	}
      }
    }
  }

  public void init()
    throws IOException, FileNotFoundException
  {
    Properties properties = new Properties();
    this.entityLookup = new EntityLookup4(properties);
    initSentenceDetector();
    initTokenizer();
    initPOSTagger();

    this.mmIndexes = new MetaMapIvfIndexes();
    this.metaMapEvalInst = new MetaMapEvaluation(this.mmIndexes);
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
   * Given a sentence, tokenize it then lookup any concepts that match
   * token extents with in sentence.
   *
   * @param docid document id
   * @param fieldid field id
   * @param sentence sentence to be examined.
   * @return set of entities found in the sentence.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public Set<Entity> processSentence(String docid, String fieldid, String sentence)
    throws FileNotFoundException, IOException
  {
    // Set<Entity> entitySet = EntityLookup1.generateEntitySet(Scanner.analyzeText(sentence));
    Set<Entity> entitySet = this.entityLookup.processSentenceTokenList(docid, fieldid,
    								       Scanner.analyzeText(sentence),
    								       new HashSet<String>(),
    								       new HashSet<String>());
    return entitySet;
  }


  public List<List<Entity>> processText(String docid, String fieldid, String text)
    throws FileNotFoundException, IOException
  {
    List<List<Entity>> listOfEntityList = new ArrayList<List<Entity>>();
    String[] sentenceList = this.sentenceDetector.sentDetect(text);
    for (String sentence: sentenceList) {
      Set<Entity> entitySet = this.processSentence(docid, fieldid, sentence);
      List<Entity> entityList = new ArrayList<Entity>();
      entityList.addAll(entitySet);
      // Collections.sort(entityList, new EntityScoreConceptNameComparator()); 
      listOfEntityList.add(entityList);
    }
    return listOfEntityList;
  }

  public static void main(String[] args)
    throws FileNotFoundException, IOException
  {
    if (args.length > 0) {
      SimplePipeline inst = new SimplePipeline();
      inst.init();
      List<String> documentList = inst.loadFile(args[0]);
      
      /*CHEMDNER style documents*/
      for (String doc: documentList) {
	if (doc.length() > 0) {
	  String[] docFields = doc.split("\\|");

	  String docId = docFields[0];
	  String docBody = docFields[1];

	  List<List<Entity>> titleListOfEntityList = inst.processText(docId, "TEXT", docBody);
	  for (List<Entity> entityList: titleListOfEntityList) {
	    MMI.displayEntityList(entityList);
	  }
	}
      }
    } else {
      System.err.println("usage: filename");
      System.err.println("Note: records in filename should be of the form:\n");
      System.err.println("   docid|document\n");
      System.err.println("Where entire document is one line.");      
    }   
  }
}
