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
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
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

  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<String> sourceSet = new HashSet<String>();
    List<Document> hitList = 
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 20);
    for (Document hit: hitList) {
      System.out.println(cui + ": " + hit.get("src"));
      sourceSet.add(hit.get("src"));
    }
    return sourceSet;
  }

  public Set<String> getSemanticTypeSet(String cui)
    throws FileNotFoundException, IOException, ParseException
  {
    Set<String> semanticTypeSet = new HashSet<String>();
    List<Document> hitList = 
      this.mmIndexes.cuiSemanticTypeIndex.lookup(cui, this.mmIndexes.cuiQueryParser, 20);
    for (Document hit: hitList) {
      System.out.println(cui + ": " + hit.get("src"));
      semanticTypeSet.add(hit.get("src"));
    }
    return semanticTypeSet;
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


  /** cache of string -> concept and attributes */
  public static Map<String,List<ConceptInfo>> termConceptCache = new HashMap<String,List<ConceptInfo>>();

  public void cacheConcept(String term, ConceptInfo concept) {
    if (termConceptCache.containsKey(term)) {
      termConceptCache.get(term).add(concept);
    } else {
      List<ConceptInfo> newConceptList = new ArrayList<ConceptInfo>();
      newConceptList.add(concept);
      termConceptCache.put(term, newConceptList);
    }
  }

  public void addEvListToSpanMap(Map<String,Entity> spanMap, List<Ev> evList, 
				 String docid, String matchedText, 
				 int offset, int length) {
    String span = offset + ":" + length;
    if (spanMap.containsKey(span)) {
      Entity entity = spanMap.get(span);
      Set<String> currentCuiSet = new HashSet<String>();
      for (Ev currentEv: entity.getEvList()) {
	currentCuiSet.add(currentEv.getConceptInfo().getCUI());
      }
      for (Ev newEv: evList) {
	if (! currentCuiSet.contains(newEv.getConceptInfo().getCUI())) {
	  entity.addEv(newEv);
	}
      }
    } else {
      Entity entity = new Entity(docid, matchedText, offset, length, 0.0, evList);
      spanMap.put(span, entity);
    }
  }

  public List<Entity> findLongestMatch(String docid,
				       List<Document> documentList,
				       String[] tokenArray)
    throws FileNotFoundException, IOException, ParseException
 {
    // span -> entity list map
    Map<String,Entity> spanMap = new HashMap<String,Entity>();
    int longestMatchedTokenLength = 0;
    for (int i = tokenArray.length; i > 0; i--) { 
      String[] arraySegment = removePunctuation(Arrays.copyOfRange(tokenArray, 0, i));
     
      String term = StringUtils.join(arraySegment, " ");
      String normTerm = MWIUtilities.normalizeAstString(term);
      int termLength = term.length();
      int offset = 0;
      List<Ev> evList = new ArrayList<Ev>();
      if (EntityLookup.termConceptCache.containsKey(normTerm)) {
	for (ConceptInfo concept: EntityLookup.termConceptCache.get(normTerm)) {
	  Ev ev = new Ev(concept,
			 term,
			 offset,
			 termLength,
			 0.0);
	}
      } else {
	for (Document doc: documentList) {
	  // System.out.println("term: \"" + term + 
	  // 		   "\" == triple.get(\"str\"): \"" + doc.get("str") + "\" -> " +
	  //  		 term.toLowerCase().equals(doc.get("str").toLowerCase()));
	  String cui = doc.get("cui");
	  if (term.toLowerCase().equals(doc.get("str").toLowerCase())) {
	    ConceptInfo concept = new ConceptInfo(cui, 
						  this.findPreferredName(cui),
						  this.getSourceSet(cui),
						  this.getSemanticTypeSet(cui));
	    this.cacheConcept(normTerm, concept);
	    Ev ev = new Ev(concept,
			       term,
			       offset,
			       termLength,
			       0.0);
	  } /* if term = lucene document */
	} /* for document in lucene document list*/
      } /* if else */
	this.addEvListToSpanMap(spanMap, evList, 
				docid,
				term,
				offset, termLength);
	longestMatchedTokenLength = Math.max(longestMatchedTokenLength,arraySegment.length);
    }
    // for (Entity candidate: candidateList) {
    //   candidate.setScore
    // 	(this.metaMapEvalInst.calculateScore(candidate.getConceptName(),
    // 					     candidate.getPreferredName(),
    // 					     candidate.getCUI(),
    // 					     candidate.getInputTextTokenList(),
    // 					     candidateList));
    // }
    return new ArrayList<Entity>(spanMap.values());
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
    String[] sentenceTokenArray = this.tokenizeText(sentence);
    Set<Entity> entitySet = new HashSet<Entity>();
    String tags[] = this.tagger.tag(sentenceTokenArray);
    for (int i = 0; i<sentenceTokenArray.length; i++) {
      List<Document> hitList = this.mmIndexes.cuiSourceInfoIndex.lookup(sentenceTokenArray[i],
									this.mmIndexes.strQueryParser,
									10);
      if (hitList.size() > 0) {
	for (Entity entity: this.findLongestMatch
	       (docid,
		hitList,
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
