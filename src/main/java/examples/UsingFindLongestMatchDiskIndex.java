package examples;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import irutils.MappedMultiKeyIndex;
import irutils.MappedMultiKeyIndexLookup;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;

import gov.nih.nlm.nls.metamap.lite.FindLongestMatch;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.Normalization;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.lite.TermFilter;
import gov.nih.nlm.nls.metamap.lite.types.MMLEntity;

/**
 * UsingFindLongestMatchDiskIndex - An example of using
 * FindLongestMatch.findLongestMatch method with disk-based inverted
 * file dataset.
 *
 * Created: Fri Jan 16 16:29:45 2026
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class UsingFindLongestMatchDiskIndex {

  /** class used to determine if head token should be used for
   * dictionary lookup. */
  TermFilter termFilter;
  MappedMultiKeyIndex index;
  MappedMultiKeyIndexLookup indexLookup;

  /**
   * Creates a new <code>UsingFindLongestMatchDiskIndex</code> instance.
   *
   */
  public UsingFindLongestMatchDiskIndex(String indexdir) {
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

    String indexName = "cuisourceinfo";
    try {
      this.index = new MappedMultiKeyIndex(indexdir, indexName);
      this.indexLookup = new MappedMultiKeyIndexLookup(this.index);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static class MappedMultKeyTermInfo implements TermInfo<List<String>> {
    String originalTerm;
    String normTerm;
    List<? extends Token> tokenList;
    /** concept name */
    List<String> dictionaryInfo;
    
    public MappedMultKeyTermInfo(String originalTerm, 
				 String normTerm,
				 List<String> resultList,
				 List<? extends Token> tokenSubList) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = resultList;
      this.tokenList = tokenSubList;
    }
    public MappedMultKeyTermInfo(String originalTerm, 
				 String normTerm,
				 List<String> resultList) {
      this.originalTerm = originalTerm;
      this.normTerm = normTerm;
      this.dictionaryInfo = resultList;
    }
    /** normalized form of term */
    public String getNormTerm() { return this.normTerm; }
    /** original term */
    public String getOriginalTerm() { return this.originalTerm; }
    /** dictionary info */
    public List<String> getDictionaryInfo() { return this.dictionaryInfo; }
    public List<? extends Token> getTokenList() { return this.tokenList; }
    public String toString() {
      return this.originalTerm + "|" + this.normTerm + "|" + this.dictionaryInfo;
    }      
  }

  public static class DiskIndexLookup implements DictionaryLookup<TermInfo<List<String>>> {
    MappedMultiKeyIndexLookup lookupInstance;
    public DiskIndexLookup(MappedMultiKeyIndexLookup instance) {
      this.lookupInstance = instance;
    }

    public TermInfo<List<String>> lookup(String originalTerm) {
      Set<String> resultSet = new HashSet<String>();
      String normTerm = Normalization.normalizeLiteString(originalTerm);
      try {
	List<String> resultList = this.lookupInstance.lookup(originalTerm, 3);
	if (resultList.size() > 0) {
	  resultSet.addAll(resultList);
	}
	List<String> normResultList = this.lookupInstance.lookup(normTerm, 3);
	if (normResultList.size() > 0) {
	  resultSet.addAll(normResultList);
	}
	if (resultSet.size() > 0) {
	  return new MappedMultKeyTermInfo(originalTerm,
					   normTerm,
					   new ArrayList<String>(resultSet));
	}
      } catch (Exception e) {
	throw new RuntimeException(e);
      }
      return null;
    }
  }

  public static String readLines(Reader inputReader)
			throws IOException {
    BufferedReader br;
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader) inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    StringBuilder sb = new StringBuilder();
    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  /** 
   * Add part of speech to tokens in tokenlist.
   * @param tokenList input token list
   */
  public void addPartOfSpeech(POSTaggerME posTagger, List<ERToken> tokenList) {


    // Copy token textstrings to string list token list discarding
    // text strings of whitespace tokens.
    List<String> textList = new ArrayList<String>();
    for (ERToken token: tokenList) {
      if (! token.getTokenClass().equals("ws")) {
	textList.add(token.getText());
      }
    }

    // Convert list of text strings to an array and call tagger, then
    // copy parts-of-speech back into token list, skipping whitespce
    // tokens.
    if (posTagger != null) {
      String tags[] = posTagger.tag(textList.toArray(new String[0]));
      int tags_i = 0;
      for (ERToken token: tokenList) {
	if (! token.getTokenClass().equals("ws")) {
	  token.setPartOfSpeech(tags[tags_i]);
	  tags_i++;
	} else {
	  token.setPartOfSpeech("WS");
	}
      }
    } else {
      //logger.error("OpenNLPPosTagger:addPartOfSpeech: posTagger is null!");
      System.err.println("OpenNLPPosTagger:addPartOfSpeech: posTagger is null!");
    }
  }


  public void process(String text) {
    List<MMLEntity<TermInfo<List<String>>>> entityList =
      new ArrayList<MMLEntity<TermInfo<List<String>>>>();
    // tokenize removing whitespace tokensp

    DictionaryLookup<TermInfo<List<String>>> lookupImpl = new DiskIndexLookup(this.indexLookup);
    String propertiesFilename = "config/metamaplite.properties";
    Properties properties = new Properties();
    try {
      BufferedReader reader = new BufferedReader
	(new InputStreamReader
	 (new FileInputStream(propertiesFilename),
	  Charset.forName("utf-8")));
      properties.load(reader);
      reader.close();
    } catch(Exception e) {
      System.err.println("Could not load configuration file from classpath: " + propertiesFilename);
    }


    InputStream modelIn = null;
    InputStream posModelIn = null;
    try {
      // initialize sentence detector/seqmenter
      String modelFilename = properties.getProperty("en-sent.bin.path",
						    "data/models/en-sent.bin");
      modelIn = new FileInputStream(modelFilename);
      SentenceModel sentenceModel = new SentenceModel(modelIn);
      SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);

      // initialize part of speech tagger
      String posModelFname = properties.getProperty("en-pos.bin.path",
						    "data/models/en-pos-maxent.bin");

      posModelIn = new FileInputStream(posModelFname);
      POSModel posModel = new POSModel(posModelIn);
      POSTaggerME posTagger = new POSTaggerME(posModel);

      String[] sentenceArray = sentenceDetector.sentDetect((String)text);
      for (String sentenceText: sentenceArray) {
	if (sentenceText instanceof String) {
	  List<ERToken> tokenlist = Scanner.analyzeText(sentenceText);
	  addPartOfSpeech(posTagger, tokenlist);
	  entityList.addAll(FindLongestMatch.findLongestMatch(tokenlist,
							      this.termFilter,
							      null, // contextInfo is null
							      lookupImpl));
	}
      }
      for (MMLEntity<TermInfo<List<String>>> t: entityList) {
	System.out.println(t);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException ioe) {
	  ioe.printStackTrace();
	}
      }
      if (posModelIn != null) {
	try {
	  posModelIn.close();
	} catch (IOException ioe) {
	  ioe.printStackTrace();
	}
      }
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
    if (args.length > 0) {
      String indexdir = args[0];
      String filename = args[1];
      System.out.println("indexdir: " + indexdir);
      System.out.println("filename: " + filename);
      System.out.flush();
      UsingFindLongestMatchDiskIndex inst = new UsingFindLongestMatchDiskIndex(indexdir);
      String inputText =
	readLines(new InputStreamReader(new FileInputStream(filename),
					Charset.forName("utf-8")));
      inst.process(inputText);
    } else {
      System.err.println("usage: examples.UsingFindLongestMatchDiskIndex indexdir filename");
      System.err.println("  indexdir: inverted file index directory");
      System.err.println("  filename: file to be processed");
    }
  }
}
