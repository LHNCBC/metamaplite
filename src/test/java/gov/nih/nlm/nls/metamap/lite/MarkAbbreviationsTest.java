package gov.nih.nlm.nls.metamap.lite;


import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;

import bioc.BioCAnnotation;
import bioc.BioCPassage;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.tools.SetOps;

/**
 * Describe class MarkAbbreviationsTest here.
 *
 *
 * Created: Tue Jan 31 09:22:43 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MarkAbbreviationsTest extends TestCase {

  String text0 = "Heart Rate (HR)";
  String text1 = "There was the possibility of suicidal ideations (SI),\n" +
    "homicidal ideations (HI), and audio visual hallucinations (AVH).\n" +
    "\n" +
    "Patient denies current SI/HI/AVH.\n" +
    "denies ever having passive or active SI/intent/plan/attemp.\n" +
    "Denies history of manic episodes, HI or A/VH\n" +
    "Pt reports chronic VH/AH\n" +
    "presented yesterday to local ER after being brought in by police with AH/VH and SI/HI\n";
  
  AbbrConverter abbrConverter = new AbbrConverter();
  ExtractAbbrev extractAbbr = new ExtractAbbrev();

  public MarkAbbreviationsTest() {

  }

  public String getText0() {
    return this.text0;
  }
  public String getText1() {
    return this.text1;
  }
  public AbbrConverter getAbbrConverter() {
    return this.abbrConverter;
  }


  @org.junit.Before public void setup() {
  }

  /** 
   * Test code with presence of short form followed by short form in parenthesis.
   * <p>
   * Example:
   * <pre>
   *   Heart Rate (HR)
   * </pre>
   */
  @Test
  public void testLongFormShortForm()
  {
    /* initialize input strings */
    String input = "Heart Rate (HR)";
    BioCPassage inputPassage = new BioCPassage();
    inputPassage.setText(input);
    inputPassage.setOffset(0);
    BioCPassage passage = abbrConverter.getPassage(inputPassage);
    
    /* initialize found entities */
    Set<Entity> initialEntitySet = new HashSet<Entity>();
    initialEntitySet.add(new Entity("en0","Heart Rate", 0, 10, 0.000000, new HashSet<Ev>()));
    /* initialize entities expected after processing */
    Set<Entity> expectedEntitySet = new HashSet<Entity>();
    expectedEntitySet.add(new Entity("en0","Heart Rate", 0, 10, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","HR", 12, 2, 0.000000, new HashSet<Ev>()));
    /** processing */
    Set<Entity> abbrevEntitySet =
      new HashSet(MarkAbbreviations.markAbbreviations(passage,
						      new ArrayList<Entity>(initialEntitySet)));    
    displayEntitySet("testLongFormShortForm: intersection: ", SetOps.intersection(abbrevEntitySet, expectedEntitySet) );
    assertTrue(abbrevEntitySet.equals(expectedEntitySet));
  }

  public void displayEntitySet(String entitySetName, Set<Entity> entitySet) {
    for (Entity entity: entitySet) {
      System.out.println(entitySetName + ": " + entity);
    }
  }

  /** 
   * Segment passage sentences by lines.
   * @param passage clinical text passage
   * @return passage with sentences seqmented by lines
   */
  public BioCPassage segmentLines(BioCPassage passage) {
    BioCPassage passage0 = new BioCPassage(passage);
    List <BioCSentence> sentenceList = new ArrayList<BioCSentence>();
    int offset = passage0.getOffset();
    int passageOffset = passage0.getOffset();
    String text = passage0.getText();
    String[] segmentList = text.split("\n");
    for (String segment: segmentList) {
      offset = text.indexOf(segment, offset);
      if (segment.trim().length() > 0) {
	BioCSentence sentence = new BioCSentence();
	sentence.setOffset(offset);
	sentence.setText(segment);
	sentence.setInfons(passage.getInfons());
	sentenceList.add(sentence);
	passage0.addSentence(sentence);
      }
      offset = segment.length(); // preserve offsets even for blank lines.
    }
    return passage0;
  }


  /** 
   * Test code with multiple abbreviations.
   * <p>
   * Example:
   * <pre>
   *   Heart Rate (HR)
   * </pre>
   */
  @Test
  public void testComplex()
  {
    /* initialize found entities */
    Set<Entity> initialEntitySet = new HashSet<Entity>();
    initialEntitySet.add(new Entity("en0","suicidal ideations", 29, 18, 0.000000, new HashSet<Ev>()));
    initialEntitySet.add(new Entity("en0","homicidal ideations", 54, 19, 0.000000, new HashSet<Ev>()));
    initialEntitySet.add(new Entity("en0","audio visual hallucinations", 84, 27, 0.000000, new HashSet<Ev>()));
    initialEntitySet.add(new Entity("en0","history of", 221, 10, 0.000000, new HashSet<Ev>()));
    initialEntitySet.add(new Entity("en0","manic", 232, 5, 0.000000, new HashSet<Ev>()));

    /* initialize expected entities */
    Set<Entity> expectedEntitySet = new HashSet<Entity>();
    expectedEntitySet.add(new Entity("en0","suicidal ideations", 29, 18, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","SI", 49, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","homicidal ideations", 54, 19, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","HI", 75, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","audio visual hallucinations", 84, 27, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","AVH", 113, 3, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","SI", 143, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","HI", 146, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","AVH", 149, 3, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","SI", 191, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","history of", 221, 10, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","manic", 232, 5, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","HI", 248, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","SI", 364, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","HI", 367, 2, 0.000000, new HashSet<Ev>()));

    BioCPassage passage0 = new BioCPassage();
    // passage0.setText(text1.replace("\n", " "));
    // BioCPassage passage = this.abbrConverter.getPassage(passage0);
    passage0.setText(text1);
    BioCPassage passage1 = segmentLines(passage0);
    BioCPassage passage = new BioCPassage(passage1);
    for (BioCSentence sentence: passage1.getSentences()) {
      // Find any abbreviations in sentence and add them as annotations referenced by relations.
      BioCSentence newSentence = abbrConverter.getSentence(sentence);
      passage.addSentence(newSentence);
      // Copy any annotations from sentences to passage.
      for (BioCAnnotation note : newSentence.getAnnotations() ) {
	passage.addAnnotation( abbrConverter.getAnnotation(note) );
      }
      // Copy any relations from sentences to passage.
      for (BioCRelation rel : newSentence.getRelations() ) {
	passage.addRelation(rel);
      }
    }
    System.out.println("testComplex: passage: " + passage);
    displayEntitySet("testComplex: initialEntitySet", initialEntitySet);
    displayEntitySet("testComplex: expectedEntitySet", expectedEntitySet);
    Set<Entity> abbrevEntitySet =
      new HashSet(MarkAbbreviations.markAbbreviations(passage,
						      new ArrayList<Entity>(initialEntitySet)));
    displayEntitySet("testComplex: abbrevEntitySet", abbrevEntitySet);

    System.out.println("testComplex: abbrevEntitySet.size() = " + abbrevEntitySet.size());
    System.out.println("testComplex: expectedEntitySet.size() = " + expectedEntitySet.size());
    displayEntitySet("testComplex: intersection: ", SetOps.intersection(abbrevEntitySet, expectedEntitySet) );
    displayEntitySet("testComplex: symmetric difference: ", SetOps.symmetric_difference(abbrevEntitySet, expectedEntitySet) );
    

    // assertTrue(abbrevEntitySet.size() == expectedEntitySet.size());
    assertTrue(abbrevEntitySet.equals(expectedEntitySet));
  }
}
