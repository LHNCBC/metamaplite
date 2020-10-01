package gov.nih.nlm.nls.metamap.lite;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
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
   * Creates a new <code>OpenNLPChunker</code> instance.
   *
   */
  public OpenNLPChunker() {
    InputStream modelIn = null;
    ChunkerModel model = null;
    try {
      modelIn = new FileInputStream(System.getProperty("opennlp.en-chunker.bin.path", 
						       "data/models/en-chunker.bin"));
      model = new ChunkerModel(modelIn);
    } catch (IOException e) {
      // Model loading failed, handle the error
      e.printStackTrace();
    } finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException e) {
	}
      }
    }
    this.chunker = new ChunkerME(model);
  }


  /**
   * Creates a new <code>OpenNLPChunker</code> instance.
   *
   */
  public OpenNLPChunker(Properties properties) {
    InputStream modelIn = null;
    ChunkerModel model = null;
    try {
      modelIn = new FileInputStream(properties.getProperty("opennlp.en-chunker.bin.path", 
							   "data/models/en-chunker.bin"));
      model = new ChunkerModel(modelIn);
    } catch (IOException e) {
      // Model loading failed, handle the error
      e.printStackTrace();
    } finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException e) {
	}
      }
    }
    this.chunker = new ChunkerME(model);
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
	      cTokenList = new ArrayList();
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
