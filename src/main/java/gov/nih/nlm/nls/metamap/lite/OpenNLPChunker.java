package gov.nih.nlm.nls.metamap.lite;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Properties;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.chunker.ChunkerME;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe class OpenNLPChunker here.
 *
 *
 * Created: Mon Mar 27 12:24:14 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class OpenNLPChunker implements ChunkerMethod {
  private static final Logger logger = LoggerFactory.getLogger(OpenNLPPoSTagger.class);

  ChunkerME chunker;

  /** 
   * Instantiate phrase chunker using input stream, most likely from
   * getResourceAsStream from classpath or ServletContext.
   *
   * @param modelIn inputstream of model file
   */
  public OpenNLPChunker(InputStream modelIn) {
    ChunkerModel model = null;
    try {
      model = new ChunkerModel(modelIn);
      this.chunker = new ChunkerME(model);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (Exception e) {
      // Model loading failed, handle the error
      e.printStackTrace();
    } finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException ioe) {
	  System.err.println
	    ("Error when closing Phrase Chunker model input stream: " +
	     ioe);
	}
      }
    }
  }

  public void setModel(String modelFilename) {
    InputStream modelIn = null;
    try {
      // look for model based on parameter modelFilename
      File modelFile = new File(modelFilename);
      if (modelFile.exists()) {
	modelIn = new FileInputStream(modelFile);
      } else {
	// otherwise, look for model on classpath
	ClassLoader loader = OpenNLPPoSTagger.class.getClassLoader();
	for (Enumeration<URL> urlEnum = loader.getResources("en-chunker.bin"); urlEnum
	       .hasMoreElements();) {
	  URL url = urlEnum.nextElement();
	  modelIn = url.openStream();
	  break;
	}
      }
      ChunkerModel model = new ChunkerModel(modelIn);
      this.chunker = new ChunkerME(model);
    } catch (IOException e) {
      // Model loading failed, handle the error
      System.err.println("Error opening Phrase Chunker model file from input stream.");
      logger.error("Error opening Phrase Chunker model file from input stream.");
      logger.error(e.toString());
      e.printStackTrace();
    } catch (Exception e) {
      // Model loading failed, handle the error
      System.err.println("Error opening Phrase Chunker model file from input stream.");
      logger.error("Error opening Phrase Chunker model file from input stream.");
      logger.error(e.toString());
      e.printStackTrace();
    } finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	}
	catch (IOException ioe) {
	  System.err.println
	    ("Error when closing Phrase Chunker model input stream: " +
	     ioe);
	}
      }
    }
  }


  /**
   * Creates a new <code>OpenNLPChunker</code> instance.
   *
   */
  public OpenNLPChunker() {
    this.setModel(System.getProperty("opennlp.en-chunker.bin.path", 
				     "data/models/en-chunker.bin"));
  }

  /**
   * Creates a new <code>OpenNLPChunker</code> instance.
   *
   */
  public OpenNLPChunker(Properties properties) {
    this.setModel(properties.getProperty("opennlp.en-chunker.bin.path", 
					 "data/models/en-chunker.bin"));
  }

  /**
   * @param tokenList token list with whitespace tokens removed.
   * @return list of noun and verb phrase tags
   */
  public String[] applyChunkerV1(List<ERToken> tokenList) {
    List<String> sentenceTokenList = new ArrayList<String>();
    List<String> posTokenList = new ArrayList<String>();
    for (ERToken token: tokenList) {
      sentenceTokenList.add(token.getText());
      posTokenList.add(token.getPartOfSpeech());
    }
    String sentence[] = sentenceTokenList.toArray(new String[1]);
    String pos[] = posTokenList.toArray(new String[1]);
    return this.chunker.chunk(sentence, pos);
  }

  /**
   * @param tokenList token list.
   * @return list of noun and verb phrase tags
   */
  public String[] applyChunkerV2(List<ERToken> tokenList) {
    List<String> sentenceTokenList = new ArrayList<String>();
    List<String> posTokenList = new ArrayList<String>();
    // Remove whitespace tokens when populating sentence and
    // part-of-speech tokenlists
    List<ERToken> noWsSentenceTokenList = new ArrayList<ERToken>();
    for (ERToken token: tokenList) {
      if (! token.getTokenClass().equals("ws")) { // only keep non-ws tokens
	sentenceTokenList.add(token.getText());
	posTokenList.add(token.getPartOfSpeech());
      }
    }
    String sentence[] = sentenceTokenList.toArray(new String[1]);
    String pos[] = posTokenList.toArray(new String[1]);
    return this.chunker.chunk(sentence, pos);
  }

  class PhraseImpl implements Phrase {
    List<ERToken> tokenlist;
    String tag;
    PhraseImpl(List<ERToken> tokenlist, String tag) {
      this.tokenlist = tokenlist;
      this.tag = tag;
    }
    public List<ERToken> getPhrase() { return this.tokenlist; }
    public String getTag() { return this.tag; }
    public String toString() { 
      return this.tokenlist.stream().map(i -> i.toString()).collect(Collectors.joining(", ")) + "/" + this.tag;
    }
  }
  
  /**
   * @param tokenList token list with whitespace tokens removed and
   *                  part of speech tags present.
   * @return list of noun and verb phrase tags
   */
  public List<Phrase> applyChunker(List<ERToken> tokenList) {
    List<Phrase> chunkList = new ArrayList<Phrase>();
    if (tokenList.size() > 0) {
      // Only process chunk list to tokenlist size is greater than zero.
      List<String> sentenceTokenList = new ArrayList<String>();
      List<String> posTokenList = new ArrayList<String>();
      logger.debug("---begin tokenList");
      for (ERToken token: tokenList) {
	  logger.debug(token.toString());
	sentenceTokenList.add(token.getText());
	posTokenList.add(token.getPartOfSpeech());
      }
      logger.debug("---end tokenList");
      String sentence[] = sentenceTokenList.toArray(new String[1]);
      String pos[] = posTokenList.toArray(new String[1]);
      String tag[] = this.chunker.chunk(sentence, pos);

      logger.debug("---begin");
      for (int k = 0; k< tag.length; k++) {
	logger.debug(k + ": " + tag[k] + "|" + sentence[k] + "|" + pos[k]);
      }
      logger.debug("---end");

      int i = 0;
      String phraseTag = "unk";
      List<ERToken> cTokenList = new ArrayList<ERToken>();
      for (String tagChunk: tag) {
	// logger.debug(tagChunk + "-chunk");
	if (i < tokenList.size()) {
	  String fields[] = tagChunk.split("-");
	  if (fields[0].equals("B")) {
	    if (cTokenList.size() > 0) {
	      chunkList.add(new PhraseImpl(cTokenList, phraseTag));
	      cTokenList = new ArrayList<ERToken>();
	    }
	    phraseTag = fields[1];
	    cTokenList.add(tokenList.get(i));
	  } else if (fields[0].equals("I") || fields[0].equals("O")) {
	    cTokenList.add(tokenList.get(i));
	  }
	}
	i++;
      }
      if (cTokenList.size() > 0) {
	chunkList.add(new PhraseImpl(cTokenList, phraseTag));
      }
    }
    return chunkList;
  }

  
}
