package examples;

/**
 * Stream Sentences, one sentence per line, as Single Line Input with
 * ID streaming class using tab as delimiter.
 *
 * An attempt to streamline StreamSpacyNLP (without the use of BioC or
 * MetaMapLite instances)
 * 
 * Each line contains the passage id, the sentence id, the sentence
 * text and optionally previously added entity annotations which are
 * discarded.
 *
 * passage/sentence/entity input:
 * <pre>
 * passage_id sentence_id sentence entitytext:char_start-char_end:entitytype ...
 * </pre>
 * 
 * The sentence segmenter, abbreviation detector, and negation
 * detector are not used.
 *
 * usage: 
 * <pre>
 * program [options] infile outfile
 * </pre>
 * For Example:
 * <pre>
 * java -Xmx6g -cp target/metamaplite-3.6.2rc5-standalone.jar examples.StreamPassageSentences  collection.tsv collection.umls.tsv
 * </pre>
 * Created: Tue Jun  9 09:48:20 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import gov.nih.nlm.nls.metamap.lite.Normalization;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.lite.TermInfoImpl;
import gov.nih.nlm.nls.metamap.lite.IVFLookup;
import gov.nih.nlm.nls.metamap.lite.FindLongestMatch;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.MMLEntity;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.utils.StringUtils;

public class StreamPassageSentences {
  Properties myProperties;
  MetaMapIvfIndexes mmIndexes;
  Set<String> stopwords = new HashSet<String>();
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  DictionaryLookup<TermInfo> dictionaryLookup;
  SentenceAnnotator posTagger;

  Set<String> loadTermSet(Set termset,
			  String inputFilename,
			  Charset charset) {
    try {
      BufferedReader br =
	new BufferedReader
	(new InputStreamReader
	 (new FileInputStream(inputFilename), charset));
      String term;
      while ((term = br.readLine()) != null) {
	termset.add(term);
      }
      return termset;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static Properties getDefaultConfiguration() {
    String modelsDirectory = System.getProperty("opennlp.models.directory", "data/models");
    String indexDirectory = System.getProperty("metamaplite.index.directory", "data/ivf/strict");
    Properties defaultConfiguration = new Properties();
    defaultConfiguration.setProperty("metamaplite.excluded.termsfile",
				     System.getProperty("metamaplite.excluded.termsfile",
							"data/specialterms.txt"));
    defaultConfiguration.setProperty("metamaplite.semanticgroup", "all");
    defaultConfiguration.setProperty("metamaplite.sourceset", "all");
    defaultConfiguration.setProperty("metamaplite.segmentation.method", "SENTENCES");

    defaultConfiguration.setProperty("opennlp.models.directory", modelsDirectory);
    defaultConfiguration.setProperty("opennlp.en-sent.bin.path", 
				     modelsDirectory + "/en-sent.bin");
    defaultConfiguration.setProperty("opennlp.en-token.bin.path",
				     modelsDirectory + "/en-token.bin");
    defaultConfiguration.setProperty("opennlp.en-pos.bin.path",
				     modelsDirectory + "/en-pos-maxent.bin");
    defaultConfiguration.setProperty("opennlp.en-chunker.bin.path",
				     modelsDirectory + "/en-chunker.bin");

    defaultConfiguration.setProperty("metamaplite.index.directory", indexDirectory);
    defaultConfiguration.setProperty("metamaplite.ivf.cuiconceptindex", 
				     indexDirectory + "/strict/indices/cuiconcept");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisourceinfoindex", 
				     indexDirectory + "/strict/indices/cuisourceinfo");
    defaultConfiguration.setProperty("metamaplite.ivf.cuisemantictypeindex", 
				     indexDirectory + "/strict/indices/cuist");
    defaultConfiguration.setProperty("metamaplite.ivf.varsindex", 
				     indexDirectory + "/strict/indices/varss");
    defaultConfiguration.setProperty("metamaplite.ivf.meshtcrelaxedindex", 
				     indexDirectory + "/strict/indices/meshtcrelaxed");

    defaultConfiguration.setProperty("metamaplite.negation.detector",
				     "gov.nih.nlm.nls.metamap.lite.NegEx");
    defaultConfiguration.setProperty("metamaplite.disable.chunker","true");
    return defaultConfiguration;
  }

  public static void expandModelsDir(Properties properties, String modelsDir) {
    if (modelsDir != null) {
      properties.setProperty("opennlp.en-sent.bin.path", modelsDir + "/en-sent.bin");
      properties.setProperty("opennlp.en-token.bin.path", modelsDir + "/en-token.bin");
      properties.setProperty("opennlp.en-pos.bin.path", modelsDir + "/en-pos-maxent.bin");
      properties.setProperty("opennlp.en-chunker.bin.path", modelsDir + "/en-chunker.bin");
    }
  }
  public static void expandModelsDir(Properties properties) {
    String modelsDir = properties.getProperty("opennlp.models.directory");
    expandModelsDir(properties, modelsDir);
  }
  public static void expandIndexDir(Properties properties, String indexDirName) {
    if (indexDirName != null) {
      properties.setProperty("metamaplite.ivf.cuiconceptindex", indexDirName + "/indices/cuiconcept");
      properties.setProperty("metamaplite.ivf.firstwordsofonewideindex", indexDirName + "/indices/first_words_of_one_WIDE");
      properties.setProperty("metamaplite.ivf.cuisourceinfoindex", indexDirName + "/indices/cuisourceinfo");
      properties.setProperty("metamaplite.ivf.cuisemantictypeindex", indexDirName + "/indices/cuist");
      properties.setProperty("metamaplite.ivf.varsindex", indexDirName + "/indices/vars");
      properties.setProperty("metamaplite.ivf.meshtcrelaxedindex", indexDirName + "/indices/meshtcrelaxed");
    }
  }
  public static void expandIndexDir(Properties properties) {
    String indexDirName = properties.getProperty("metamaplite.index.directory");
    expandIndexDir(properties, indexDirName);
  }

  public static class MorphLookup implements DictionaryLookup<TermInfo> {
    DictionaryLookup<TermInfo<Set<ConceptInfo>>> dbLookup;
      
    public MorphLookup(DictionaryLookup dbLookupImpl) {
      this.dbLookup = dbLookupImpl;
    }
    
    public TermInfo lookup(String term) {
      Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
      String normTerm = Normalization.normalizeUtf8AsciiString(term);
      TermInfo<Set<ConceptInfo>> termInfo = (TermInfo<Set<ConceptInfo>>)this.dbLookup.lookup(term);
      conceptInfoSet.addAll((Set<ConceptInfo>)termInfo.getDictionaryInfo());
      termInfo = this.dbLookup.lookup(normTerm);
      conceptInfoSet.addAll((Set<ConceptInfo>)termInfo.getDictionaryInfo());
      return new TermInfoImpl<Set<ConceptInfo>>(term, normTerm, conceptInfoSet);
    }
  }
  
  /**
   * Creates a new <code>StreamPassageSentences</code> instance.
   *
   * @param metaMapLiteRootDir root of metamaplite installation
   * @param datasetDir directory containing specific dataset usually under data/ivf      
   * @param stopwordsFilename filename of stopwords file.
   */
  public StreamPassageSentences(String metaMapLiteRootDir,
		   String datasetDir,
		   String stopwordsFilename) {
    try {
      loadTermSet(this.stopwords, stopwordsFilename,
		  Charset.forName("utf-8"));
      this.stopwords.add("and");
      this.stopwords.add("but");
      this.stopwords.add("can");
      this.stopwords.add("end");
      this.stopwords.add("follow");
      this.stopwords.add("have");
      this.stopwords.add("his");
      this.stopwords.add("this");
      this.stopwords.add("was");
      this.stopwords.add("while");
      
      this.myProperties = this.getDefaultConfiguration();
      this.myProperties.setProperty("opennlp.models.directory",
				    metaMapLiteRootDir + "/data/models");
      expandModelsDir(this.myProperties);
      this.myProperties.setProperty("metamaplite.index.directory", datasetDir);      
      expandIndexDir(this.myProperties);
      this.myProperties.setProperty("metamaplite.excluded.termsfile",
				    metaMapLiteRootDir + "/data/specialterms.txt");
      // Loading properties file in "config", overriding previously
      // defined properties.
      // FileReader fr = new FileReader("config/metamaplite.properties");
      // myProperties.load(fr);
      // fr.close();
      // this.mmIndexes = new MetaMapIvfIndexes(this.myProperties);
      this.myProperties.setProperty("metamaplite.postaglist",
				    "CD,FW,RB,NN,NNS,NNP,NNPS,JJ,JJR,JJS,LS");
      this.myProperties.setProperty("metamaplite.enable.scoring","false");
      this.allowedPartOfSpeechSet.add("CD"); // cardinal number (need this for chemicals)
      this.allowedPartOfSpeechSet.add("FW"); // foreign word
      this.allowedPartOfSpeechSet.add("RB"); // should this be here?
      // this.allowedPartOfSpeechSet.add("IN"); // preposition, subordinating conjunction	(in, of, like) ?what?
      this.allowedPartOfSpeechSet.add("NN");
      this.allowedPartOfSpeechSet.add("NNS");
      this.allowedPartOfSpeechSet.add("NNP");
      this.allowedPartOfSpeechSet.add("NNPS");
      this.allowedPartOfSpeechSet.add("JJ");
      this.allowedPartOfSpeechSet.add("JJR");
      this.allowedPartOfSpeechSet.add("JJS");
      this.allowedPartOfSpeechSet.add("LS"); // list item marker (need this for chemicals)

      this.dictionaryLookup = new MorphLookup(new IVFLookup(this.myProperties));
      // this.dictionaryLookup = new IVFLookup(this.myProperties);

      this.posTagger = new OpenNLPPoSTagger(this.myProperties);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class PassageSentenceText  {
    String passageId;
    String sentenceId;
    String text;
    public PassageSentenceText(String passageId,
			       String sentenceId,
			       String text) {
      this.passageId = passageId;
      this.sentenceId = sentenceId;
      this.text = text;
    }
    public String getPassageId() { return this.passageId; }
    public String getSentenceId() { return this.sentenceId; }
    public String getText() { return this.text; }
  }

  /**
   * Parse record with id passage and entities (probably from spacy);
   * entities are discarded.
   * 
   * @param record passageid/sentid/sent/entities record
   * @param delimiterRegex delimiter regular expression for String.split
   */
  public static PassageSentenceText parsePassageSentenceEntityRecord(String record,
								     String delimiterRegex) {
    try {
      String fields[] = record.split(delimiterRegex);
      return new PassageSentenceText(fields[0], fields[1], fields[2]);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      throw new RuntimeException("Record: " + record, aioobe);
    }
  }

  public static class Entity {
    String text;
    int start;
    int length;
    Collection<ConceptInfo> conceptInfoList;
    
    public Entity(String text,
		  int start,
		  int length,
		  Collection<ConceptInfo> conceptInfoList)
    { 
      this.text = text;           
      this.start = start;          
      this.length = length;         
      this.conceptInfoList = conceptInfoList;
    }
    public String getText() { return this.text; }
    public int getStart() { return this.start; }
    public int getLength() { return length; }
    public Collection<ConceptInfo> getConceptInfoList() {
      return conceptInfoList;
    }
  }
  
  List<Entity> processText(String text)
  {
    List<Entity> entityList = new ArrayList<Entity>();
    DictionaryLookup<TermInfo> lookupImpl;
    List<ERToken> tokenList = Scanner.analyzeText(text);
    posTagger.addPartOfSpeech(tokenList); // this should probably be functional
    List<TermInfo> termInfoList = 
      FindLongestMatch.findLongestMatch(tokenList,
					this.allowedPartOfSpeechSet,
					this.dictionaryLookup);
    for (TermInfo termInfo: termInfoList) {
      if (((Collection<ConceptInfo>)termInfo.getDictionaryInfo()).size() > 0) {
	String term = termInfo.getOriginalTerm();
	PosToken firstToken = (PosToken)termInfo.getTokenList().get(0);
	int start = firstToken.getOffset();
	entityList.add(new Entity(term,
	 			  start,
	 			  start + term.length(),
	 			  (Collection<ConceptInfo>)termInfo.getDictionaryInfo()));
	// System.out.println(termInfo);
      }
    }
    return entityList;
  }

  public void stream(String inputFile, String outputFile, Charset charset)
  {
    Set<String> allowedPOS = new HashSet<String>();
    for (String partOfSpeech:
	   System.getProperty("metamaplite.postaglist",
			      "CD,FW,RB,NN,NNS,NNP,NNPS,JJ,JJR,JJS,LS").split(",")) {
      allowedPOS.add(partOfSpeech);
    }
    String delimiterRegex =  "\t";
    try {
      BufferedReader br =
	new BufferedReader
	(new InputStreamReader
	 (new FileInputStream(inputFile), charset));
      PrintWriter pw =
	new PrintWriter
	(new OutputStreamWriter
	 (new FileOutputStream(outputFile), charset));
      String line;
      while ((line = br.readLine()) != null) {
	PassageSentenceText document =
	  parsePassageSentenceEntityRecord(line, delimiterRegex);

	List<Entity> entityList = processText(document.getText());	  
	pw.print(document.getPassageId() + "\t" + document.getSentenceId() + "\t" + document.getText());
	for (Entity entity: entityList) {
	  if (! this.stopwords.contains(entity.getText().toLowerCase())) {
	    pw.print("\t" + entity.getText() + ":" +
		     entity.getStart() + "-"
		     + (entity.getStart() + entity.getLength()) + ":");
	    // entity.getEvList().get(0).getPartOfSpeech() + ":" +

	    // pw.print(Arrays.stream
	    // 	     (entity.getEvList().toArray()).map
	    // 	     (ev -> ev.getConceptInfo().getCUI()).collect
	    // 	     (Collectors.joining(",")));
	    List<String> cuilist = new ArrayList<String>();
	    for (ConceptInfo ci: entity.getConceptInfoList()) {
	      cuilist.add(ci.getCUI());
	    }
	    pw.print(StringUtils.join(cuilist, ","));
	  }
	}
	pw.println();
      }
      pw.close();
      br.close();
      System.out.println("done writing: " + outputFile + ".");
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Main program
   *
   * @param args command line arugments
   */
  public static void main(String[] args) {
    if (args.length > 1) {
      String metaMapLiteRoot = System.getProperty("metamaplite.root.dir", ".");
      String datasetDir =  System.getProperty("metamaplite.dataset.dir",
					      "data/ivf/strict");
      String stopwordsFilename = System.getProperty("metamaplite.stopwords.file",
						    "data/stopwords.txt");
      StreamPassageSentences inst =
	new StreamPassageSentences(metaMapLiteRoot, datasetDir, stopwordsFilename);
      inst.stream(args[0], args[1], Charset.forName("utf-8"));
    }
  }
}
