package examples;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.resultformats.Brat;
import gov.nih.nlm.nls.metamap.lite.FindLongestMatch;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.Normalization;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.metamap.lite.TermFilter;
import gov.nih.nlm.nls.metamap.lite.types.MMLEntity;

/**
 * UsingFindLongestMatch - An example of using
 * FindLongestMatch.findLongestMatch method with custom in-memory
 * dataset.
 *
 * Created: Mon Dec 17 16:29:45 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class UsingFindLongestMatch {

  /** class used to determine if head token should be used for
   * dictionary lookup. */
  TermFilter termFilter;

  /**
   * Creates a new <code>UsingFindLongestMatch</code> instance.
   *
   */
  public UsingFindLongestMatch() {
    /** Part of speech tags used for term lookup, can be set using
     * property: metamaplite.postaglist; the tag list is a set of Penn
     * Treebank part of speech tags separated by commas. */
    Set<String> allowedPartOfSpeechSet = new HashSet<String>();
    allowedPartOfSpeechSet.add("RB");
    allowedPartOfSpeechSet.add("NN");
    allowedPartOfSpeechSet.add("NNS");
    allowedPartOfSpeechSet.add("NNP");
    allowedPartOfSpeechSet.add("NNPS");
    allowedPartOfSpeechSet.add("JJ");
    allowedPartOfSpeechSet.add("JJR");
    allowedPartOfSpeechSet.add("JJS");
    allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
    this.termFilter = new FindLongestMatch.SampleTermFilter(allowedPartOfSpeechSet);
  }

  public static String read(Reader inputReader)
    throws IOException {
    BufferedReader br;
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    StringBuilder sb = new StringBuilder();
    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  public static class InMemoryTermInfo implements TermInfo {
    String originalTerm;
    String normTerm;
    List<? extends Token> tokenList;
    /** concept name */
    String dictionaryInfo;
    public InMemoryTermInfo(String originalTerm, 
			    String normTerm,
			    String name,
			    List<? extends Token> tokenSubList) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = name;
      this.tokenList = tokenSubList;
    }
    public InMemoryTermInfo(String originalTerm, 
			    String normTerm,
			    String name) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = name;
    }
    /** normalized form of term */
    public String getNormTerm() { return this.normTerm; }
    /** original term */
    public String getOriginalTerm() { return this.originalTerm; }
    /** dictionary info */
    public String getDictionaryInfo() { return this.dictionaryInfo; }
    public List<? extends Token> getTokenList() { return this.tokenList; }
    public String toString() {
      return this.originalTerm + "|" + this.normTerm + "|" + this.dictionaryInfo;
    }      
  }

  /**
   * Describe class InMemoryLookup here.
   *
   *
   * Created: Tue Dec 18 11:39:21 2018
   *
   * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
   * @version 1.0
   */
  public static class InMemoryLookup implements DictionaryLookup<TermInfo> {

    Map<String,String> termMap = new HashMap<String,String>();

    /**
     * Creates a new <code>InMemoryLookup</code> instance.
     *
     */
    public InMemoryLookup() {

      this.termMap.put("baby","Span");
      this.termMap.put("bull","Span");
      this.termMap.put("cat","Span");
      this.termMap.put("flies","Span");
      this.termMap.put("horse","Span");
      this.termMap.put("life","Span");
      this.termMap.put("light","Span");
      this.termMap.put("man","Span");
      this.termMap.put("trees","Span");
      this.termMap.put("zebra","Span");
      this.termMap.put("patients","Span");
      this.termMap.put("medication","Span");
      this.termMap.put("pneumonia","Span");
      this.termMap.put("diabetes","Span");
      this.termMap.put("cancer","Span");
      this.termMap.put("non-Hodgkins lymphoma","Span");
    }
    public TermInfo lookup(String originalTerm) {
      String normTerm = Normalization.normalizeLiteString(originalTerm);
      if (this.termMap.containsKey(originalTerm)) {
	return new InMemoryTermInfo(originalTerm,
				    normTerm,
				    this.termMap.get(originalTerm));
      } else if (this.termMap.containsKey(normTerm)) {
	return new InMemoryTermInfo(originalTerm,
				    normTerm,
				    this.termMap.get(normTerm));
      }
      return null;
    }
  }


  public void process(Properties properties, String text) {
    List<MMLEntity> entityList = new ArrayList<MMLEntity>();
    // tokenize removing whitespace tokens
    DictionaryLookup<TermInfo> lookupImpl = new InMemoryLookup();
    SentenceAnnotator sentenceAnnotator = new OpenNLPPoSTagger(properties);
    SentenceExtractor sentenceExtractor = new OpenNLPSentenceExtractor(properties);
    for (Sentence sent: sentenceExtractor.createSentenceList(text, 0)) {
      List<ERToken> tokenlist = sentenceAnnotator.addPartOfSpeech(sent);
      entityList.addAll(FindLongestMatch.findLongestMatch(tokenlist,
							    this.termFilter,
							    null, // contextInfo is null
							    lookupImpl));
    }
    for (MMLEntity t: entityList) {
      System.out.println(t);
    }
  }

  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
   * @exception IOException i/o exception
   * @throws FileNotFoundException thrown if file is not found
   * @throws IOException i/o exception
   */
  public static final void main(final String[] args)
    throws FileNotFoundException, IOException
  {
    String propertiesFilename = "metamaplite.properties";
    Properties properties = new Properties();
    ClassLoader loader = UsingFindLongestMatch.class.getClassLoader();
    if(loader==null)
      loader = ClassLoader.getSystemClassLoader(); // use system class loader if class loader is null
    java.net.URL url = loader.getResource(propertiesFilename);
    try {
      properties.load(url.openStream());
    } catch(Exception e) {
      System.err.println("Could not load configuration file from classpath: " + propertiesFilename);
    }

    if (args.length > 0) {
      String filename = args[0];
      UsingFindLongestMatch instance = new UsingFindLongestMatch();
      String inputText =
	read(new InputStreamReader(new FileInputStream(filename),
				   Charset.forName("utf-8")));
      instance.process(properties, inputText);
    } else {
      System.err.println("examples.UsingFindLongestMatch filename");
      System.err.println(" file to be processed?");
    }
  }
}
