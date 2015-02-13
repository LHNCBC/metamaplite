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

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.lite.lucene.StringPair;
import gov.nih.nlm.nls.metamap.lite.lucene.StringTriple;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreComparator;
// import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreConceptNameComparator;
import gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIndexes;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 */

public class SimplePipeline {
  private static final Logger logger = LogManager.getLogger(SimplePipeline.class);
  public SentenceModel sentenceModel;
  public SentenceDetectorME sentenceDetector;
  public TokenizerModel model;
  public Tokenizer tokenizer;
  public POSModel posModel;
  public POSTaggerME tagger;
  public MetaMapEvaluation metaMapEvalInst;
  public MetaMapIndexes mmIndexes;


  public void initSentenceDetector()
    throws IOException, FileNotFoundException
  {
    InputStream modelIn = new FileInputStream(System.getProperty("en-sent.bin.path", "en-sent.bin"));
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
    InputStream modelIn = new FileInputStream(System.getProperty("en-token.bin.path", "en-token.bin"));

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
    InputStream modelIn = new FileInputStream(System.getProperty("en-pos-maxent.bin.path", "en-pos-maxent.bin"));

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
    throws IOException, FileNotFoundException, ParseException
  {
    initSentenceDetector();
    initTokenizer();
    initPOSTagger();

    this.mmIndexes = new MetaMapIndexes();
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
   * @param sentence sentence to be examined.
   * @return set of entities found in the sentence.
   */
  public Set<Entity> processSentence(String sentence)
    throws FileNotFoundException, IOException, ParseException
  {
    String docid = "XXXXXX";
    Set<Entity> entitySet = EntityLookup.generateEntitySet(Scanner.analyzeText(sentence));
    return entitySet;
  }


  public List<List<Entity>> processText(String text)
    throws FileNotFoundException, IOException, ParseException
  {
    List<List<Entity>> listOfEntityList = new ArrayList<List<Entity>>();
    String[] sentenceList = this.sentenceDetector.sentDetect(text);
    for (String sentence: sentenceList) {
      Set<Entity> entitySet = processSentence(sentence);
      List<Entity> entityList = new ArrayList<Entity>();
      entityList.addAll(entitySet);
      // Collections.sort(entityList, new EntityScoreConceptNameComparator()); 
      listOfEntityList.add(entityList);
    }
    return listOfEntityList;
  }

  public static void main(String[] args)
    throws FileNotFoundException, IOException, ParseException
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
	  System.out.println("docid: " + docId);
	  System.out.flush();
	  String docBody = docFields[1];

	  List<List<Entity>> titleListOfEntityList = inst.processText(docBody);
	  for (List<Entity> entityList: titleListOfEntityList) {
	    MMI.displayEntityList(entityList);
	  }
	}
      }
    } else {
      System.err.println("usage: filename");
    }   
  }
}
