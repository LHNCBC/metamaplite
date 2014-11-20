
//
package gov.nih.nlm.nls.metamap.lite;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.types.Annotation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */

public class SentenceExtractor 
{
  private static final Logger logger = LogManager.getLogger(SentenceExtractor.class);

  public static SentenceModel sentenceModel;
  public static SentenceDetectorME sentenceDetector;

  static {
    InputStream modelIn = null;
    try {
      modelIn = new FileInputStream(System.getProperty("en-sent.bin.path", "en-sent.bin"));
      sentenceModel = new SentenceModel(modelIn);
      sentenceDetector = new SentenceDetectorME(sentenceModel);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException ioe) {
	  ioe.printStackTrace();
	} 
      }
    }
  }

  static class SentenceImpl implements Sentence {
    String text;
    int offset;
    SentenceImpl(String text, int offset) {
      this.text = text;
      this.offset = offset;
    }
    @Override
    public int getOffset() {
      return this.offset;
    }
    
    @Override
    public String getText() {
      return this.text;
    }
    
    @Override
    public List<Annotation> getAnnotations() {
      // TODO: Stub
      return null;
    }
  }
  
  public static List<Sentence> createSentenceList(String text) {
    logger.debug("createSentenceList");
    int offset = 0;
    String[] sentenceArray = sentenceDetector.sentDetect(text);
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (String sentenceText: sentenceArray) {
      sentenceList.add(new SentenceImpl(sentenceText, offset));
      offset = offset + sentenceText.length();
    }
    return sentenceList;
  }
}
