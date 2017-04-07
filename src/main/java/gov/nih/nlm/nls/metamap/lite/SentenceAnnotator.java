
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Properties;

import gov.nih.nlm.nls.types.Annotation;
import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

import bioc.BioCSentence;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

/**
 *
 */

public interface SentenceAnnotator {
  
  /**
   * List part of speech tags corresponding to to oridinal position of tokens.
   * @param tokenList input token list
   * @return list string of part of speech corresponding to oridinal position of tokens.
   */
  List<String> listPartOfSpeech(List<? extends Token> tokenList);

  /** 
   * Add part of speech to tokens in tokenlist.
   * @param tokenList input token list
   */
  void addPartOfSpeech(List<ERToken> tokenList);

  /**
   * Add part of speech annotations to sentence that already contains
   * token annotations (annotation type is "token").
   * @param sentence bioC sentence containing annotations of type "token".
   */
  void addPartOfSpeech(BioCSentence sentence);

  List<ERToken> addPartOfSpeech(Sentence sentence);

}
