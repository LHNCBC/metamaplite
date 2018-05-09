package gov.nih.nlm.nls.metamap.lite;
//
import org.junit.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;

/**
 * NegEx unit tests
 */

public class NegExTest extends TestCase {

  NegEx inst = new NegEx();
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public NegExTest( String testName )
  {
    super( testName );
  }

  /**
   * @return the suite of tests being tested
   */
  public static TestSuite suite()
  {
    return new TestSuite( NegExTest.class );
  }


  /**
   * Test findPhrase function for trigger ["can", "be", "ruled", "out"] in sentence at token 5.
   */
  @Test
  public void testFindPhrase0()
  {
    List<String> stringlist = Arrays.asList
      ("The", "diagnosis", "suggests", "that", "pneumonia", "can", "be", "ruled", "out");
    List<String> phrase = Arrays.asList("can", "be", "ruled", "out");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(5))));
  }

  /**
   * Test find phrase function for trigger ["no", "sign", "of"] in sentence at token 2.
   */
  @Test
  public void testFindPhrase1()
  {
    List<String> stringlist = Arrays.asList("There", "was", "no", "sign", "of", "pneumonia");
    List<String> phrase = Arrays.asList("no", "sign", "of");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }

  /**
   * Test find phrase function for trigger "denies" in sentence at token 2.
   */
  @Test
  public void testFindPhrase2()
  {
    List<String> stringlist =
      Arrays.asList("The patient denies chest pain and has no shortage of breath".split(" "));
    List<String> phrase = Arrays.asList("denies");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }

  /**
   * Test find phrase function for trigger "no" in sentence at token 7.
   */
  @Test
  public void testFindPhrase3()
  {
    List<String> stringlist =
      Arrays.asList("The patient denies chest pain and has no shortage of breath".split(" "));
    List<String> phrase = Arrays.asList("no");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(7))));
  }

  /**
   * Test find phrase function for trigger "sufficient to rule him out" in sentence at token 2.
   */
  public void testFindPhrase4()
  {
    List<String> stringlist =
      Arrays.asList("It is sufficient to rule him out for pneumonia".split(" "));
    List<String> phrase = Arrays.asList("sufficient to rule him out".split(" "));
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }

  /**
   * Test find phrase function for trigger "was ruled out" in sentence at token 1.
   */
  public void testFindPhrase5()
  {
    List<String> stringlist =
      Arrays.asList("Pneumonia was ruled out".split(" "));
    List<String> phrase = Arrays.asList("was ruled out".split(" "));
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(1))));
  }

  public void testTokenNegEx()
  {
    assertTrue(true);
  }

  /**
   * test determining range of meta-token.
   */
  public void testGetRangeOfMetaToken()
  {
    String metaSubSentence =
      "SMX-NO depleted CYS and GSH in buffer, and to a lesser extent, in cells and plasma.";
    List<ERToken> subTokenList = Scanner.analyzeText(metaSubSentence);
    int range = this.inst.getRangeOfMetaToken(subTokenList);
    System.out.println("range: " + range);
    assertTrue( range == 2 );
  }


  String testMetaSentence = 
    "Fluorescence HPLC showed that SMX-NHOH and SMX-NO depleted CYS and GSH in buffer, and to a lesser extent, in cells and plasma.";
  public void testAddMetaToken()

  {
    List<ERToken> tokenList = Scanner.analyzeText(testMetaSentence);
    List<ERToken> newTokenList = this.inst.addMetaTokens(tokenList);
    System.out.println("newTokenList: " + newTokenList);
    assertTrue( newTokenList.get(12).getText().equals("SMX-NO") );
  }

  String testMetaSentence2 = "Nitrosyl hydride ((NO)H).";
  public void testAddMetaToken2()
  {
    List<ERToken> tokenList = Scanner.analyzeText(testMetaSentence2);
    List<ERToken> newTokenList = this.inst.addMetaTokens(tokenList);
    System.out.println("newTokenList2: " + newTokenList);
    assertTrue( newTokenList.get(4).getText().equals("((NO)H)") );
  }

  String testMetaSentence3 = "Patient denies current SI/HI/AVH.";
  List<ERToken> testTokenList3 = Scanner.analyzeText(testMetaSentence3);
  
  
  public void testAddMetaToken3()
  {
    List<ERToken> newTokenList = this.inst.addMetaTokens(testTokenList3);
    System.out.println("testAddMetaToken3: newTokenList: " + newTokenList);
    assertTrue( newTokenList.size() == 8 );
  }

  public void testFindPhraseDenies()
  {
    List<String> stringlist = Arrays.asList(Tokenize.mmTokenize(testMetaSentence3, 0));
    List<String> phrase = Arrays.asList("denies");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    System.out.println("testFindPhraseDenies: positionList: " + positionList +
		       " -> (" + stringlist.get(positionList.get(0)) + ")");
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }

  public void testGetEntityTokenPosition0()
  {
    Entity entity = new Entity("s1", "AVH", 29, 3, 100.0, new ArrayList<Ev>());
    System.out.println("testGetEntityTokenPosition0: testTokenList3: " + testTokenList3);
    int position = this.inst.getEntityTokenPosition(entity, testTokenList3);
    System.out.println("testGetEntityTokenPosition0: position: " + position);
    assertTrue(position == 10);		// force it to fail.
  }

  public void testGetEntityTokenPosition1()
  {
    Entity entity0 = new Entity("s1", "SI", 23, 2, 100.0, new ArrayList<Ev>());
    Entity entity1 = new Entity("s1", "HI", 26, 2, 100.0, new ArrayList<Ev>());
    Entity entity2 = new Entity("s1", "AVH", 29, 3, 100.0, new ArrayList<Ev>());
    System.out.println("testGetEntityTokenPosition1: testTokenList3: " + testTokenList3);
    List<ERToken> newTokenList = this.inst.addMetaTokens(testTokenList3);
    System.out.println("testGetEntityTokenPosition1: newTokenList: " + newTokenList);
    int position0 = this.inst.getEntityTokenPosition(entity0, newTokenList);
    System.out.println("testGetEntityTokenPosition1: position0: " + position0 + ", entity0: " + entity0);
    int position1 = this.inst.getEntityTokenPosition(entity1, newTokenList);
    System.out.println("testGetEntityTokenPosition1: position1: " + position1 + ", entity1: " + entity1);
    int position2 = this.inst.getEntityTokenPosition(entity2, newTokenList);
    System.out.println("testGetEntityTokenPosition1: position2: " + position2 + ", entity2: " + entity2);
    assertTrue((position0 == 6) &&
	       (position0 == 6) &&
	       (position0 == 6));
  }

  public void testMarkNegatedEntities()  
  {
    // list of entities
    List<Entity> entityList = new ArrayList<Entity>();
    entityList.add(new Entity("s1", "Patient", 0, 7, 100.0, new ArrayList<Ev>()));
    entityList.add(new Entity("s1", "SI", 23, 2, 100.0, new ArrayList<Ev>()));
    entityList.add(new Entity("s1", "HI", 26, 2, 100.0, new ArrayList<Ev>()));
    entityList.add(new Entity("s1", "AVH", 29, 3, 100.0, new ArrayList<Ev>()));
    
    List<NegPhraseInfo> negationPhraseList = new ArrayList<NegPhraseInfo>();
    List<Integer> posList = Arrays.asList(new Integer("2"));
    negationPhraseList.add(new NegPhraseInfo(Arrays.asList("denies"), "nega", posList));
    
    List<NegPhraseInfo> conjPhraseList = new ArrayList<NegPhraseInfo>(); // empty, no conjunctions

    System.out.println("testMarkNegatedEntities: testTokenList3: " + testTokenList3);
    List<ERToken> newTokenList = this.inst.addMetaTokens(testTokenList3);
    System.out.println("testMarkNegatedEntities: newTokenList: " + newTokenList);

    System.out.println("before:");
    for (Entity entity: entityList) {
      System.out.println(" " + entity);
    }
    this.inst.markNegatedEntities(newTokenList,
				  negationPhraseList,
				  conjPhraseList,
				  entityList);
    System.out.println("after:");
    for (Entity entity: entityList) {
      System.out.println(" " + entity);
    }
    assertTrue(entityList.get(1).isNegated() &&
	       entityList.get(2).isNegated() &&
	       entityList.get(3).isNegated()
	       );
  }
}

