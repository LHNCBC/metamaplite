package gov.nih.nlm.nls.ner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.ChemDNERSLDI;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.document.NCBICorpusDocument;
import gov.nih.nlm.nls.metamap.document.SingleLineInput;
import gov.nih.nlm.nls.metamap.document.SingleLineDelimitedInputWithID;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoader;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoaderImpl;
import gov.nih.nlm.nls.metamap.document.BioCDocumentLoaderRegistry;
import gov.nih.nlm.nls.metamap.document.PubTator;
import gov.nih.nlm.nls.metamap.document.PubMedXMLDocument;
import gov.nih.nlm.nls.metamap.document.MedlineDocument;

import gov.nih.nlm.nls.metamap.lite.resultformats.mmi.MMI;
import gov.nih.nlm.nls.metamap.lite.resultformats.Brat;
import gov.nih.nlm.nls.metamap.lite.resultformats.CuiList;
import gov.nih.nlm.nls.metamap.lite.resultformats.BcEvaluate;
import gov.nih.nlm.nls.metamap.lite.resultformats.FullJson;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatterRegistry;

import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.lite.EntityLookupBioC;
import gov.nih.nlm.nls.metamap.lite.EntityLookup4;

import gov.nih.nlm.nls.metamap.lite.MarkAbbreviations;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;
import gov.nih.nlm.nls.metamap.lite.OpenNLPChunker;
import gov.nih.nlm.nls.metamap.lite.ChunkerMethod;

import gov.nih.nlm.nls.metamap.lite.types.MMLDocument;
import gov.nih.nlm.nls.metamap.lite.types.MMLPassage;
import gov.nih.nlm.nls.metamap.lite.types.MMLSentence;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.Phrase;

import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.types.Sentence;

import net.sourceforge.argparse4j.inf.Namespace;


/**
 * Process files, documents, sentences, etc.
 *
 * Created: Fri Jun  7 16:29:25 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Process {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(Process.class);
  Properties properties;
  BioCDocumentLoader docLoader = new FreeText();
  boolean detectNegationsFlag = false;
  boolean verbose = false;

  Set<String> semanticGroup = new HashSet<String>(); // initially empty
  Set<String> sourceSet = new HashSet<String>(); // initially empty

  String outputExtension = ".out";
  String outputFormatOption = System.getProperty("metamaplite.outputformat","mmi");
  enum SegmentatonType {
    SENTENCES,
    BLANKLINES,
    LINES
  };

  SegmentatonType segmentationMethod = SegmentatonType.SENTENCES;
  SentenceExtractor sentenceExtractor;
  EntityLookup entityLookup;
  EntityLookupBioC entityLookupBioC;
  
  /** end of citation output marker */
  public static String eotString = "<<< EOT >>>";

  /**
   * Creates a new <code>Process</code> instance.
   * @param properties application properties instance
   * @throws ClassNotFoundException Class not found exception
   * @throws InstantiationException problem with instance creation
   * @throws NoSuchMethodException instance or class method not found
   * @throws IllegalAccessException illegal access of instance 
   * @throws IOException input/output exception
   * @throws Exception general exception
   */
  public Process(Properties properties)
    throws ClassNotFoundException, InstantiationException, 
	   NoSuchMethodException, IllegalAccessException,
	   IOException, Exception
  {
    this.properties = properties;
    // this shoud be instantiate using reflection or JNDI
    this.entityLookup = new EntityLookup4(properties);
    if (entityLookup instanceof EntityLookupBioC) {
      this.entityLookupBioC = new EntityLookup4(properties);
    }
    String documentInputOption = properties.getProperty("metamaplite.document.inputtype","freetext");
    registerFormats();
    if (BioCDocumentLoaderRegistry.contains(documentInputOption)) {
      this.docLoader = BioCDocumentLoaderRegistry.get(documentInputOption);
      if (this.docLoader == null) {
	throw new Exception("Fatal: Document loader for input option \"" +
			    documentInputOption +
			    "\" is not instantiated check configuration or properties");
      }
    } else {
      logger.error("Document loader for input option \"" +
		   documentInputOption + "\" is not available.");

      throw new Exception("Fatal: Document loader for input option \"" +
			  documentInputOption + "\" is not available. Check properties or configuration.");
    }
    this.properties = properties;

    this.sentenceExtractor = new OpenNLPSentenceExtractor(properties);
    this.setSemanticGroup(properties.getProperty("metamaplite.semanticgroup", "all").split(","));
    this.setSourceSet(properties.getProperty("metamaplite.sourceset","all").split(","));
    this.detectNegationsFlag = 
      Boolean.parseBoolean(properties.getProperty("metamaplite.detect.negations", "true"));
    this.setSegmentationMethod
      (properties.getProperty("metamaplite.segmentation.method","SENTENCE"));

    this.outputFormatOption = properties.getProperty("metamaplite.outputformat","mmi");
    this.outputExtension = ".out";
    if (properties.getProperty("metamaplite.outputformat").equals("mmi")) {
      properties.setProperty("metamaplite.outputextension", ".mmi");
    }
    if (properties.containsKey("metamaplite.outputextension")) {
      this.outputExtension = properties.getProperty("metamaplite.outputextension");
    }
    this.verbose = Boolean.parseBoolean(properties.getProperty("metamaplite.verbose", "false"));
  }

  /**
   * Set list of semantic types concepts must belong to be retrieved.
   * @param semanticTypeList list of semantic type strings
   */
  public void setSemanticGroup(String[] semanticTypeList) {
    this.semanticGroup = new HashSet<String>(Arrays.asList(semanticTypeList));
  }

  /**
   * Set list of sources concepts must belong to be retrieved.
   * @param sourceList list of source strings
   */
  public void setSourceSet(String[] sourceList) {
    this.sourceSet = new HashSet<String>(Arrays.asList(sourceList));
  }

  /**
   * Register input and output formats
   * @throws ClassNotFoundException class not found exception
   * @throws InstantiationException problem instantiating class exception
   * @throws NoSuchMethodException exception if method does not exist
   * @throws IllegalAccessException Illegal memory access exception
   */
  void registerFormats()
    throws ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException
  {
    BioCDocumentLoaderRegistry.register("bioc","For BioC XML documents.", new BioCDocumentLoaderImpl());
    BioCDocumentLoaderRegistry.register
      ("freetext", "For freetext documents that are grammatically well behaved.", new FreeText());
    BioCDocumentLoaderRegistry.register("chemdner", "ChemDNER format document sets",new ChemDNER());
    BioCDocumentLoaderRegistry.register
      ("chemdnersldi","ChemDNER single line delimited with id format document sets",new ChemDNERSLDI());
    BioCDocumentLoaderRegistry.register
      ("ncbicorpus","NCBI Disease Corpus format document sets",new NCBICorpusDocument());
    BioCDocumentLoaderRegistry.register("sli","Single Line Input document sets",new SingleLineInput());
    BioCDocumentLoaderRegistry.register("sldi","Single Line Input document sets",new SingleLineInput());
    BioCDocumentLoaderRegistry.register
      ("sldiwi","Single Line Input document sets with id",new SingleLineDelimitedInputWithID());
    BioCDocumentLoaderRegistry.register("pubmed","PubMed XML Abstract",new PubMedXMLDocument());
    BioCDocumentLoaderRegistry.register("pubtator","PubTator format",new PubTator());
    BioCDocumentLoaderRegistry.register("medline","Medline format", new MedlineDocument());

    ResultFormatterRegistry.register("bc","BioCreative Evaluation Format", new BcEvaluate());
    ResultFormatterRegistry.register("bc-evaluate", "BioCreative Evaluation Format", new BcEvaluate());
    ResultFormatterRegistry.register("bioc", "BioCreative Evaluation Format", new BcEvaluate());
    ResultFormatterRegistry.register("cdi", "BioCreative Evaluation Format", new BcEvaluate());
    ResultFormatterRegistry.register("brat", "BRAT Annotation format (.ann)",  new Brat());
    ResultFormatterRegistry.register("json", "JSON format (.json)", new FullJson());
    ResultFormatterRegistry.register("fulljson", "JSON format (.json)", new FullJson());
    ResultFormatterRegistry.register("mmi", "Fielded MetaMap Indexing-like Output", new MMI());
    ResultFormatterRegistry.register("cuilist", "UMLS CUI List Output", new CuiList());
    /** augment or override any built-in formats with ones specified by property file. */
    BioCDocumentLoaderRegistry.register(this.properties);
    ResultFormatterRegistry.register(this.properties);
  }

  /**
   * Set seqmentation method used by passage segmenter.
   * segmentation methods:
   * <dl>
   * <dt><tt>SENTENCES</tt>  <dd>seqment text using sentence breaker.
   * <dt><tt>BLANKLINES</tt> <dd>seqment text using blank lines as delimitor
   * <dt><tt>LINES</tt>      <dd>seqment text using newlines as delimitor
   * </dl>
   * @param typeName name of segmentation method to use
   */
  public void setSegmentationMethod(String typeName) {
    if (typeName.equals("SENTENCES")) {
      this.segmentationMethod = SegmentatonType.SENTENCES;
    } else if (typeName.equals("BLANKLINES")) {
      this.segmentationMethod = SegmentatonType.BLANKLINES;
    } else if (typeName.equals("LINES")) {
      this.segmentationMethod = SegmentatonType.LINES;
    }
  }

  List<Entity> processText(String docid, String text) {
    return null;
  }

  public MMLSentence processSentence(BioCSentence sentence) {
    String docid = sentence.getInfon("docid");
    if (docid == null) { docid = "00000000.tx"; }
    List<Entity> entityList = processText(docid, sentence.getText());

    return new MMLSentence(sentence, entityList);
  }

  public MMLPassage processPassage(BioCPassage passage)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    BioCPassage passage0;
    List<BioCSentence> sentenceList;
    int offset = 0;
    int passageOffset = 0;
    String text;
    String[] segmentList;
    
    logger.debug("enter processPassage");
    logger.debug(passage.getText());
    switch (segmentationMethod) {
    case SENTENCES:
      passage0 = this.sentenceExtractor.createSentences(passage);
      passage0.setInfons(passage.getInfons()); // copy docid and section info
      break;
    case BLANKLINES:
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      passageOffset = passage.getOffset();
      text = passage.getText();
      segmentList = text.split("\n\n");
      for (String segment: segmentList) {
	BioCSentence sentence = new BioCSentence();
	offset = text.indexOf(segment, offset);
	sentence.setOffset(offset);
	sentence.setText(segment);
	sentence.setInfons(passage.getInfons());
	sentenceList.add(sentence);
	passage.addSentence(sentence);
	offset = segment.length(); 
      }
      passage0 = passage;
      break;
    case LINES:
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      passageOffset = passage.getOffset();
      text = passage.getText();
      segmentList = text.split("\n");
      for (String segment: segmentList) {
	offset = text.indexOf(segment, offset);
	if (segment.trim().length() > 0) {
	  BioCSentence sentence = new BioCSentence();
	  sentence.setOffset(offset);
	  sentence.setText(segment);
	  sentence.setInfons(passage.getInfons());
	  sentenceList.add(sentence);
	  passage.addSentence(sentence);
	}
	offset = segment.length(); // preserve offsets even for blank lines.
      }
      passage0 = passage;
      break;
    default:
      // copy entire text of passage into one sentence
      sentenceList = new ArrayList<BioCSentence>();
      offset = passage.getOffset();
      BioCSentence sentence = new BioCSentence();
      sentence.setText(passage.getText());
      sentence.setOffset(offset);
      sentence.setInfons(passage.getInfons());
      sentenceList.add(sentence);
      passage.addSentence(sentence);
      passage0 = passage;
      break;
    }
    // List<Entity> entityList = new ArrayList<Entity>();
    // for (BioCSentence sentence: passage0.getSentences()) {
    //   MMLSentence annotatedSentence = processSentence(sentence);
    //   entityList.addAll(annotatedSentence.getEntities());
    // }
    String docid = passage0.getInfons().get("docid");
    List<Entity> entityList = this.entityLookupBioC.processPassage(docid,
							       passage0,
							       false,
							       semanticGroup,
							       sourceSet);
    return new MMLPassage(passage0, entityList);
  }

  public MMLDocument processDocument(BioCDocument document) 
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    List<Entity> entityList = new ArrayList<Entity>();
    List<BioCPassage> passageList = new ArrayList<BioCPassage>();
    // add docid to passage info namespace (infons)
    Map<String,String> docInfoMap = document.getInfons();
    if (docInfoMap == null) {
      docInfoMap = new HashMap<String,String>();
      document.setInfons(docInfoMap);
    }
    docInfoMap.put("docid", document.getID());
    for (BioCPassage passage: document.getPassages()) {
      Map<String,String> passageInfons = passage.getInfons();
      if (! passageInfons.containsKey("docid")) {
	passageInfons.put("docid", document.getID());
      }
      MMLPassage newPassage = processPassage(passage);
      passageList.add(newPassage);
      entityList.addAll(newPassage.getEntityList());
    }
    MMLDocument newDocument = new MMLDocument(document, entityList);
    return newDocument;
  }  

  public List<MMLDocument> processDocumentList(List<BioCDocument> documentList) 
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    List<MMLDocument> mmlDocumentList = new ArrayList<MMLDocument>();
    for (BioCDocument document: documentList) {
      mmlDocumentList.add(this.processDocument(document));
    }
    return mmlDocumentList;
  }

  public List<Sentence> getSentenceList(List<BioCDocument> documentList) {
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (BioCDocument document: documentList) {
      for (BioCPassage passage: document.getPassages()) {
	sentenceList.addAll(this.sentenceExtractor.createSentenceList(passage.getText(), passage.getOffset()));
      }
    }
    return sentenceList;
  }

  /** list phrases of sentences using document list 
   * @param documentList list of BioC documents
   * @return formatted output string
   */
  public String renderChunks(List<BioCDocument> documentList)
  {
    SentenceAnnotator sentenceAnnotator = new OpenNLPPoSTagger(this.properties);
    ChunkerMethod chunkerMethod = new OpenNLPChunker(properties);
    StringBuilder sb = new StringBuilder();
    for (Sentence sent: this.getSentenceList(documentList)) {
      List<ERToken> sentenceTokenList = sentenceAnnotator.addPartOfSpeech(sent);
      sb.append(sent.getOffset() + "|" + sent.getText().length() + "|" + sent.getText()).append("\n");
      sb.append("--tokenlist--").append("\n");
      List<ERToken> minimalSentenceTokenList = new ArrayList<ERToken>();
      for (ERToken token: sentenceTokenList) {
	if (! token.getTokenClass().equals("ws")) { // only keep non-ws tokens
	  minimalSentenceTokenList.add(token);
	}
      }
      sentenceAnnotator.addPartOfSpeech(minimalSentenceTokenList);
      for (ERToken token: minimalSentenceTokenList) {
	sb.append(token.getText() + "(" + token.getPartOfSpeech() + "),");
      }
      sb.append("--").append("\n");
      sb.append("--phraselist--").append("\n");
      logger.debug("minimalSentenceTokenList: " + minimalSentenceTokenList);
      List<Phrase> phraseList = chunkerMethod.applyChunker(minimalSentenceTokenList);
      for (Phrase phrase: phraseList) {
	sb.append("phrase: " + phrase.toString()).append("\n");
      }
      sb.append("-----------").append("\n");
      sb.append("\n");
    }
    return sb.toString();
  }

  public List<AbbrInfo> getAcronymList(List<BioCDocument> documentList) {
    ExtractAbbrev extractAbbr = new ExtractAbbrev();
    List <AbbrInfo> infos = new ArrayList<AbbrInfo>();
    for (BioCDocument document: documentList) {
      for (BioCPassage passage: document.getPassages()) {
	for (Sentence sentence: this.sentenceExtractor.createSentenceList(passage.getText())) {
	  infos.addAll(extractAbbr.extractAbbrPairsString(sentence.getText()));
	}
      }
    }
    return infos;
  }

  /** list acronyms and abbreviations of sentences using document list 
   * @param documentList list of BioC documents
   * @return formatted output string
   */
  public String renderAcronyms(List<BioCDocument> documentList)
  {
    StringBuilder sb = new StringBuilder();
    for (AbbrInfo acronym: this.getAcronymList(documentList)) {
      sb.append(acronym.shortForm + "|" + acronym.shortFormIndex + "|" +
		acronym.longForm + "|" + acronym.longFormIndex ).append("\n");
    }

    return sb.toString();
  }
  /** list sentences using document list 
   * @param documentList list of BioC documents
   * @return formatted output string
   */
  public String renderSentences(List<BioCDocument> documentList)
  {
    StringBuilder sb = new StringBuilder();
    for (Sentence sent: this.getSentenceList(documentList)) {
      sb.append(sent.getOffset()).append("|").append(sent.getText().length())
	.append("|").append(sent.getText()).append("\n");
    }
    return sb.toString();
  }

  /** list sentences with part of speech tags using document list 
   * @param documentList list of BioC documents
   * @return formatted output string
   */
  public String renderSentencesWithPosTags(List<BioCDocument> documentList)
  {
    SentenceAnnotator sentenceAnnotator = new OpenNLPPoSTagger(this.properties);
    if (this.verbose)
      System.err.println("outputing results to Standard Output");
    logger.debug("outputing results to Standard Output");
    StringBuilder sb = new StringBuilder();
    for (Sentence sent: this.getSentenceList(documentList)) {
      if (this.verbose) {
	System.err.println("sentence: " + sent);
      }
      List<ERToken> tokenList = sentenceAnnotator.addPartOfSpeech(sent);
     
      sb.append(sent.getOffset()).append("|").append(sent.getText().length()).append("|").append(sent.getText()).append("|");
      for (ERToken token: tokenList) {
	sb.append(token.getText()).append("(").append(token.getPartOfSpeech()).append("),");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public String renderDocumentEntities(List<BioCDocument> documentList)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    StringBuilder sb = new StringBuilder();
    for (MMLDocument mmlDoc: this.processDocumentList(documentList)) {
      List<Entity> entityList = mmlDoc.getEntityList();
      // output results to string builder
      sb.append(this.renderEntities(entityList, outputFormatOption));
    }
    return sb.toString();
  }

  public String renderEntities(List<Entity> entityList,
			       String outputFormatOption)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    logger.debug("outputing results to output." );
    // format output
    ResultFormatter formatter = ResultFormatterRegistry.get(outputFormatOption);
    if (formatter != null) {
      formatter.initProperties(this.properties);
      return formatter.entityListFormatToString(entityList);
    } else {
      System.err.println("! Couldn't find formatter for output format option: " + outputFormatOption);
    }
    return entityList.stream().map( n -> n.toString() ).collect( Collectors.joining( ";" ) );	
  }
  

  public void listEntities(PrintWriter pw,
			   List<Entity> entityList,
			   String outputFormatOption)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    logger.debug("outputing results to output." );
    // format output
    ResultFormatter formatter = ResultFormatterRegistry.get(outputFormatOption);
    if (formatter != null) {
      formatter.initProperties(this.properties);
      formatter.entityListFormatter(pw, entityList);
    } else {
      System.err.println("! Couldn't find formatter for output format option: " + outputFormatOption);
    }
    pw.flush();
  }

  enum RenderMethod { ACRONYMS, CHUNKS, SENTENCES, SENTENCES_POSTAGS, ENTITIES };
  RenderMethod getRenderMethod(Namespace ns) {
    if (Boolean.parseBoolean(ns.getString("list_acronyms"))) {
      return RenderMethod.ACRONYMS;
    } else if (Boolean.parseBoolean(ns.getString("list_chunks"))) {
      return RenderMethod.CHUNKS;
    } else if (Boolean.parseBoolean(ns.getString("list_sentences"))) {
      return RenderMethod.SENTENCES;
    } else if (Boolean.parseBoolean(ns.getString("list_sentences_postags"))) {
      return RenderMethod.SENTENCES_POSTAGS;
    } else {
      return RenderMethod.ENTITIES;
    }
  }
  RenderMethod getRenderMethod(Properties properties) {
    if (Boolean.parseBoolean(properties.getProperty("metamaplite.list.acronyms", "false"))) {
      return RenderMethod.ACRONYMS;
    } else if (Boolean.parseBoolean(properties.getProperty("metamaplite.list.chunks"))) {
      return RenderMethod.CHUNKS;
    } else if (Boolean.parseBoolean(properties.getProperty("metamaplite.list.sentences"))) {
      return RenderMethod.SENTENCES;
    } else if (Boolean.parseBoolean(properties.getProperty("metamaplite.list.sentences.with.postags"))) {
      return RenderMethod.SENTENCES_POSTAGS;
    } else {
      return RenderMethod.ENTITIES;
    }
  }

  public void processDocuments(RenderMethod method,
			       String inputFilename, String printFilename,
			       boolean overwrite, boolean fromScheduler)
    throws IOException, IllegalAccessException,
	   InvocationTargetException, Exception
  {
    if (this.verbose) {
      System.out.printf(" overwrite = %s\n", overwrite);
      System.out.printf(" processing %s\n", inputFilename);
    }

    Path path = Paths.get(inputFilename);
    List<BioCDocument> documentList = this.docLoader.loadFileAsBioCDocumentList(path.toString());
    String result = "no output...";
    switch (method) {
    case ACRONYMS:
      outputExtension = ".acronyms";
      result = this.renderAcronyms(documentList);
      break;
    case CHUNKS:
      outputExtension = ".chunks";
      result = this.renderChunks(documentList);
      break;
    case SENTENCES:
      outputExtension = ".sentences";
      result = this.renderSentences(documentList);
      break;
    case SENTENCES_POSTAGS:
      outputExtension = ".sentences_postags";
      result = this.renderSentencesWithPosTags(documentList);
      break;
    default:
      result = this.renderDocumentEntities(documentList);
      break;
    }
    /* if not scheduler filename then filename must be direved from
     * basename of inputfilename */
    String outputFilename;
    if (fromScheduler) {
      outputFilename = printFilename;
    } else {
      outputFilename = printFilename + outputExtension;
    }
    File outputFile = new File(outputFilename);
    if (outputFile.exists() && (overwrite == false)) {
      throw new RuntimeException("File " + outputFile.getPath() + " exists aborting, use --overwrite to overwrite output files.");
    }
    // output results for file
    PrintWriter pw = new PrintWriter(new OutputStreamWriter
				     (new FileOutputStream(outputFile),
				      Charset.forName("utf-8")));
    pw.print(result);
    if (fromScheduler) {
      pw.println(eotString); // should this be in Prolog format? Will 'EOT' suffice?
      pw.flush();
    }
    pw.close();
  }


  public void process(RenderMethod method, List<String> filenameList,
		      boolean inputFromStdin, boolean overwrite,
		      boolean fromScheduler)
    throws IllegalAccessException,
	   InvocationTargetException, IOException, Exception
  {
    if (inputFromStdin) {
      List<BioCDocument> documentList =
	this.docLoader.readAsBioCDocumentList(new InputStreamReader(System.in,
								    Charset.forName("utf-8")));
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,
							      Charset.forName("utf-8")));

      switch (method) {
      case ACRONYMS:
	pw.println(this.renderAcronyms(documentList));
	break;
      case CHUNKS:
	pw.println(this.renderChunks(documentList));
	break;
      case SENTENCES:
	pw.println(this.renderSentences(documentList));
	break;
      case SENTENCES_POSTAGS:
	pw.println(this.renderSentencesWithPosTags(documentList));
	break;
      default:			// entities
	pw.println(this.renderDocumentEntities(documentList));
	break;
      }
      pw.flush();
    } else if (fromScheduler) {
      String inputFilename = filenameList.get(0);
      String outputFilename = filenameList.get(1);
      processDocuments(method, inputFilename, outputFilename, overwrite, fromScheduler);      
    } else {
      // input files 
      for (String name : filenameList) {
	Path path = Paths.get(name);
	String filename = path.toString();
	String basename = "output";
	// create output filename
	if (filename.lastIndexOf(".") >= 0) {
	  basename = filename.substring(0,filename.lastIndexOf(".")); //
	} else {
	  basename = filename;
	}
	processDocuments(method, filename, basename, overwrite, fromScheduler);
      }
    }
  }

  public void process(Namespace ns)
    throws IllegalAccessException, InvocationTargetException, IOException, Exception
  {
    Map<String,Object> nsAttrs = ns.getAttrs();
    if (this.verbose) {
      System.out.println("ns: " + ns);
    }
    if (ns.getBoolean("list_formats")) {
      registerFormats();
      System.out.println("input document formats:");
      for (String name: BioCDocumentLoaderRegistry.listNameSet()) {
	String description = BioCDocumentLoaderRegistry.getDescription(name);
	System.out.println("  " + name + ": " + description);
      }
      System.out.println("output document formats:");
      for (String name: ResultFormatterRegistry.listNameSet()) {
	String description = ResultFormatterRegistry.getDescription(name);
	System.out.println("  " + name + ": " + description);
      }
    } else {
      boolean inputFromStdin = Boolean.parseBoolean(ns.getString("pipe"));
      List<String> filenameList = ns.<String> getList("file");
      boolean fromScheduler = ns.getBoolean("scheduler");
      boolean overwrite = ns.getBoolean("overwrite");
      RenderMethod method = getRenderMethod(ns);
      process(method, filenameList, inputFromStdin, overwrite, fromScheduler);
    }
  }

  public void process(Properties properties, List<String> filenameList,
		      boolean inputFromStdin, boolean overwrite,
		      boolean fromScheduler)
    throws IllegalAccessException,
	   InvocationTargetException, IOException, Exception
  {
    RenderMethod method = getRenderMethod(properties);
    process(method, filenameList, inputFromStdin, overwrite, fromScheduler);
  }

}
