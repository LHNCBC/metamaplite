
//
package gov.nih.nlm.nls.metamap.lite;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.types.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioc.BioCPassage;
import bioc.BioCSentence;

/**
 *
 */

public class OpenNLPSentenceExtractor implements SentenceExtractor
{
  private static final Logger logger = LoggerFactory.getLogger(OpenNLPSentenceExtractor.class);

  SentenceModel sentenceModel;
  SentenceDetectorME sentenceDetector;

  public OpenNLPSentenceExtractor() {
    if (new File(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin")).exists()) {
      this.setModel(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    }
  }

  public OpenNLPSentenceExtractor(Properties properties) { 
    if (new File(properties.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin")).exists()) {
      this.setModel(properties.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    }
  }
  
  public void setModel(String modelFilename)
  {
    InputStream modelIn = null;
    try {
      modelIn = new FileInputStream(modelFilename);
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

  public static class SentenceImpl implements Sentence {
    Map<String, String> infons;
    String text;
    int offset;
    List<Annotation> annotationList = null;
    SentenceImpl(String id, String text, int offset) {
      this.text = text;
      this.offset = offset;
    }
    @Override
    public Map<String, String> getInfons() {
      return this.infons;
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
      return this.annotationList;
    }

    public void addAnnotation(Annotation annotation) {
      if (this.annotationList == null) {
	this.annotationList = new ArrayList<Annotation>();
      }
      this.annotationList.add(annotation);
    }
  }
  
  public List<Sentence> createSentenceList(String text) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    int offset = 0;
    Span[] sentencePosArray = sentenceDetector.sentPosDetect(text);
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (Span sentenceSpan: sentencePosArray) {
      String sentenceText = sentenceSpan.getCoveredText(text).toString();
      sentenceList.add(new SentenceImpl("", sentenceText, offset + sentenceSpan.getStart()));
      sentenceCount++;
    }
    return sentenceList;
  }

  public List<Sentence> createSentenceList(String text, int offset) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    Span[] sentencePosArray = sentenceDetector.sentPosDetect(text);
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (Span sentenceSpan: sentencePosArray) {
      String sentenceText = sentenceSpan.getCoveredText(text).toString();
      sentenceList.add(new SentenceImpl("", sentenceText, offset + sentenceSpan.getStart()));
      sentenceCount++;
    }
    return sentenceList;
  }

  public int addBioCSentence(BioCPassage passage,
				    String sentenceText, int offset, Map<String,String> infoNS) {
    BioCSentence sentence = new BioCSentence();
    sentence.setText(sentenceText);
    sentence.setOffset(offset);
    sentence.setInfons(infoNS);
    passage.addSentence(sentence);
    offset = offset + sentenceText.length() + 1;
    return offset;
  }

  public BioCPassage createSentences(BioCPassage passage) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    int offset = passage.getOffset();
    String text = passage.getText();
    Span[] sentencePosArray = sentenceDetector.sentPosDetect(passage.getText());
    for (Span sentenceSpan: sentencePosArray) {
      // If sentence contains a semi-colon then split the sentence
      // into two utterances and add each to the passage, preserving
      // whitespace.
      String sentenceText = sentenceSpan.getCoveredText(text).toString();
      int semicolonIndex = sentenceText.indexOf(";");
      offset = sentenceSpan.getStart();
      if (semicolonIndex > 3) {
	String[] utteranceArray = sentenceText.split(";");
	for (String utteranceText: utteranceArray) {
	  offset = addBioCSentence(passage, utteranceText, offset, passage.getInfons());
	  sentenceCount++;
	}
      } else {
	offset = addBioCSentence(passage, sentenceText, offset, passage.getInfons());
	sentenceCount++;
      }
    }
    return passage;
  }
}
