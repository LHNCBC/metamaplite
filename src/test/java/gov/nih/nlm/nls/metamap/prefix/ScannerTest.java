
//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

import bioc.BioCSentence;



/**
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 0.1
 */
@RunWith(JUnit4.class)
public class ScannerTest {
  String sentence = "Cervical cancer starts in the cells of the cervix, which is the lower, narrow end of the uterus.";
  List<ERToken> tokenlist;
  List<ClassifiedToken> classTokenlist;
  List<Token> offsetTokenlist;

  @org.junit.Before public void setup() {
    this.tokenlist = Scanner.analyzeText(sentence);
    this.classTokenlist = new ArrayList<ClassifiedToken>();
    this.offsetTokenlist = new ArrayList<Token>();
  }

  @org.junit.Test public void testAddOffsets() {
    // assertEquals(addOffsets(this.tokenlist, 0), )
  }
  
  @org.junit.Test public void testAnalyzeText() {
    
  }
  @org.junit.Test public void testAnalyzeSentenceText() {
    
  }
  @org.junit.Test public void testRemoveWhiteSpaceTokens() {
    
  }
  @org.junit.Test public void testAnalyzeTextNoWS() {
    
  }
  
}
