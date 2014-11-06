
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

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

import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.CharUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.lite.lucene.StringPair;
import gov.nih.nlm.nls.metamap.lite.lucene.StringTriple;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreComparator;
import gov.nih.nlm.nls.metamap.lite.types.Entity.EntityScoreConceptNameComparator;
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapEvaluation;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIndexes;

import gov.nih.nlm.nls.utils.StringUtils;

/**
 *
 */

public class SimplePipeline {
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

  public String[] tokenizeText(String sentence)
  {
    String[] tokenlist = Tokenize.mmTokenize(sentence, 2);
    List<String> filteredTokenList = new ArrayList<String>();
    for (String token: tokenlist) {
      // System.out.print("\"" + token + "\", ");
      if (CharUtils.isAlphaNumeric(token.charAt(0)) || CharUtils.isPunct(token.charAt(0))) {
	filteredTokenList.add(token);
      }
    } // token
    return filteredTokenList.toArray(new String[0]);
  }

  public List<String[]> tokenizeText(String[] sentenceList)
  {
    List<String[]> sentenceTokenArrayList = new ArrayList<String[]>();
    for (String sentence: sentenceList) {
      System.out.println(sentence);
      sentenceTokenArrayList.add(tokenizeText(sentence));
    } // sentence
    return sentenceTokenArrayList;
  }

  String findPreferredName(String cui)
    throws FileNotFoundException, IOException, ParseException
 {
    List<Document> hitList = 
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 1);
    if (hitList.size() > 0) {
      return hitList.get(0).get("str");
    }
    return null;
  }

  String[] removePunctuation(String[] stringArray) {
    List<String> stringList = new ArrayList<String>();
    for (String token: stringArray) {
      if ((token.length() > 1) || (! CharUtils.isPunct(token.charAt(0)))) {
	stringList.add(token);
      }
    }
    return stringList.toArray(new String[0]);
  }


  /**
   * Given Example:
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
   * 
   * Check the following:
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity"
   *   "Papillary Thyroid Carcinoma is a Unique Clinical"
   *   "Papillary Thyroid Carcinoma is a Unique"
   *   "Papillary Thyroid Carcinoma is a"
   *   "Papillary Thyroid Carcinoma is"
   *   "Papillary Thyroid Carcinoma"
   *   "Papillary Thyroid"
   *   "Papillary"
   *
   *  
   *
   */
  public List<Entity> findLongestMatch(List<Document> documentList,
					     String[] tokenArray)
    throws FileNotFoundException, IOException, ParseException
 {
    List<Entity> candidateList = new ArrayList<Entity>();
    for (int i = tokenArray.length; i > 0; i--) { 
      String[] arraySegment = removePunctuation(Arrays.copyOfRange(tokenArray, 0, i));
     
      String term = StringUtils.join(arraySegment, " ");
      for (Document doc: documentList) {
	// System.out.println("term: \"" + term + 
	// 		   "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
	//  		 term.toLowerCase().equals(doc.get("str").toLowerCase()));

	if (term.toLowerCase().equals(doc.get("str").toLowerCase())) {
	  candidateList.add(new Entity(doc.get("cui"), 
				       doc.get("str"), 
				       this.findPreferredName(doc.get("cui")),
				       arraySegment,
				       0.0));
	}
      }
    }
    for (Entity candidate: candidateList) {
      candidate.setScore
	(this.metaMapEvalInst.calculateScore(candidate.getConceptName(),
					 candidate.getPreferredName(),
					 candidate.getCUI(),
					 candidate.getInputTextTokenList(),
					 candidateList));
    }
    return candidateList;
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
    String[] sentenceTokenArray = this.tokenizeText(sentence);
    Set<Entity> entitySet = new HashSet<Entity>();
    String tags[] = this.tagger.tag(sentenceTokenArray);
    for (int i = 0; i<sentenceTokenArray.length; i++) {
      List<Document> hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(sentenceTokenArray[i],
									this.mmIndexes.strQueryParser,
									10);
      if (hitList.size() > 0) {
	for (Entity entity: this.findLongestMatch
	       (hitList,
		Arrays.copyOfRange(sentenceTokenArray, 
				   i, Math.min(i+30,sentenceTokenArray.length)))) {
	  entitySet.add(entity);
	}
      }
    }
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
      Collections.sort(entityList, new EntityScoreConceptNameComparator()); 
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
	String[] docFields = doc.split("\\|");
	String docId = docFields[0];
	String docBody = docFields[1];
	String[] bodyFields = docBody.split("\t");
	String docTitle = bodyFields[0];
	String docAbstract = bodyFields[1];

	List<List<Entity>> titleListOfEntityList = inst.processText(docTitle);
	for (List<Entity> entityList: titleListOfEntityList) {
	  MMI.displayEntityList(entityList);
	}

	List<List<Entity>> listOfEntityList = inst.processText(docAbstract);
	for (List<Entity> entityList: listOfEntityList) {
	  MMI.displayEntityList(entityList);
	}
      }
    } else {
      System.err.println("usage: filename");
    }   
  }
}
