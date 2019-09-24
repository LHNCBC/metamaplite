package gov.nih.nlm.nls.metamap.lite;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import bioc.BioCAnnotation;
import bioc.BioCPassage;
import bioc.BioCRelation;
import bioc.BioCSentence;
import bioc.tool.AbbrConverter;
import bioc.tool.AbbrInfo;
import bioc.tool.ExtractAbbrev;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.tools.SetOps;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.IVFLookup;
import static org.junit.Assert.assertTrue;

/**
 * Describe class TestUserDefinedAcronyms here.
 *
 *
 * Created: Fri May 18 11:19:36 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class TestUserDefinedAcronyms {

  String text1 = "There was the possibility of suicidal ideations (SI),\n" +
    "homicidal ideations (HI), and audio visual hallucinations (AVH).\n" +
    "\n" +
    "Patient denies current SI/HI/AVH.\n" +
    "denies ever having passive or active SI/intent/plan/attemp.\n" +
    "Denies history of manic episodes, HI or A/VH\n" +
    "Pt reports chronic VH/AH\n" +
    "presented yesterday to local ER after being brought in by police with AH/VH and SI/HI\n";
  
  AbbrConverter abbrConverter = new AbbrConverter();

  /**
   * Creates a new <code>TestUserDefinedAcronyms</code> instance.
   *
   */
  public TestUserDefinedAcronyms() {

  }

  @org.junit.Before public void setup() {
    

  }
  
  @org.junit.Test
    public void test0() {
    /* initialize user defined acronyms */
    Map<String,String> uaMap = new HashMap<String,String>();
    uaMap.put("VH","visual hallucinations");
    uaMap.put("AH","auditory hallucinations");


    Map<String,UserDefinedAcronym<TermInfo>> udaMap =
      new HashMap<String,UserDefinedAcronym<TermInfo>>();
    for (Map.Entry<String,String> entry: uaMap.entrySet()) {
      udaMap.put
	(entry.getKey(),
	 new UserDefinedAcronym<TermInfo>
	 (entry.getKey(),
	  entry.getValue(),
	  new TermInfoImpl<Set<ConceptInfo>>
	  (entry.getValue(),
	   entry.getKey(),
	   new HashSet<ConceptInfo>(),
	   Scanner.analyzeText(entry.getKey()))));
    }
    
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

    expectedEntitySet.add(new Entity("en0","AH", 22, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","AH", 70, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","VH", 19, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","VH", 42, 2, 0.000000, new HashSet<Ev>()));
    expectedEntitySet.add(new Entity("en0","VH", 73, 2, 0.000000, new HashSet<Ev>()));


    Set<Entity> augmentedEntitySet = new HashSet<Entity>();
    BioCPassage passage0 = new BioCPassage();
    // passage0.setText(text1.replace("\n", " "));
    // BioCPassage passage = this.abbrConverter.getPassage(passage0);
    passage0.setText(text1);
    BioCPassage passage1 = HelperMethods.segmentLines(passage0);
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
      List<ERToken> tokenlist = Scanner.analyzeText(newSentence);
      augmentedEntitySet.addAll(UserDefinedAcronym.generateEntities("testdoc", udaMap, tokenlist));
    }

    augmentedEntitySet.addAll(initialEntitySet);
    System.out.println("test0: passage: " + passage);
    HelperMethods.displayEntitySet("test0: initialEntitySet", initialEntitySet);
    HelperMethods.displayEntitySet("test0: augmentedEntitySet", augmentedEntitySet);
    HelperMethods.displayEntitySet("test0: expectedEntitySet", expectedEntitySet);
    Set<Entity> abbrevEntitySet =
      new HashSet(MarkAbbreviations.markAbbreviations(passage,
						      uaMap,
						      new ArrayList<Entity>(augmentedEntitySet)));

    HelperMethods.displayEntitySet("test0: abbrevEntitySet", abbrevEntitySet);

    System.out.println("test0: abbrevEntitySet.size() = " + abbrevEntitySet.size());
    System.out.println("test0: expectedEntitySet.size() = " + expectedEntitySet.size());
    HelperMethods.displayEntitySet("test0: intersection: ", SetOps.intersection(abbrevEntitySet, expectedEntitySet) );
    HelperMethods.displayEntitySet("test0: symmetric difference: ", SetOps.symmetric_difference(abbrevEntitySet, expectedEntitySet));
    

    assertTrue(abbrevEntitySet.size() == expectedEntitySet.size());
    // assertTrue(abbrevEntitySet.equals(expectedEntitySet));
  }
}
