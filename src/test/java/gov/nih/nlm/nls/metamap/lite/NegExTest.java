package gov.nih.nlm.nls.metamap.lite;
//
import org.junit.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.List;
import java.util.Arrays;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

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
   * Test findPhrase 
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
  @Test
  public void testFindPhrase1()
  {
    List<String> stringlist = Arrays.asList("There", "was", "no", "sign", "of", "pneumonia");
    List<String> phrase = Arrays.asList("no", "sign", "of");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }
  @Test
  public void testFindPhrase2()
  {
    List<String> stringlist =
      Arrays.asList("The patient denies chest pain and has no shortage of breath".split(" "));
    List<String> phrase = Arrays.asList("denies");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }
  @Test
  public void testFindPhrase3()
  {
    List<String> stringlist =
      Arrays.asList("The patient denies chest pain and has no shortage of breath".split(" "));
    List<String> phrase = Arrays.asList("no");
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(7))));
  }

  public void testFindPhrase4()
  {
    List<String> stringlist =
      Arrays.asList("It is sufficient to rule him out for pneumonia".split(" "));
    List<String> phrase = Arrays.asList("sufficient to rule him out".split(" "));
    List<Integer> positionList = this.inst.findPhrase(stringlist, phrase);
    assertTrue(positionList.equals(Arrays.asList(new Integer(2))));
  }

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

  String metaSubSentence =
    "SMX-NO depleted CYS and GSH in buffer, and to a lesser extent, in cells and plasma.";

  public void testGetRangeOfMetaToken()
  {
    List<ERToken> subTokenList = Scanner.analyzeText(metaSubSentence);
    int range = inst.getRangeOfMetaToken(subTokenList);
    System.out.println("range: " + range);
    assertTrue( range == 0 );
  }


  String testMetaSentence = 
    "Fluorescence HPLC showed that SMX-NHOH and SMX-NO depleted CYS and GSH in buffer, and to a lesser extent, in cells and plasma.";
  public void testAddMetaToken()

  {
    List<ERToken> tokenList = Scanner.analyzeText(testMetaSentence);
    List<ERToken> newTokenList = inst.addMetaTokens(tokenList);
    System.out.println("newTokenList: " + newTokenList);
    assertTrue( newTokenList.get(12).getText().equals("SMX-NO") );
  }

  String testMetaSentence2 = "Nitrosyl hydride ((NO)H).";
  public void testAddMetaToken2()
  {
    List<ERToken> tokenList = Scanner.analyzeText(testMetaSentence2);
    List<ERToken> newTokenList = inst.addMetaTokens(tokenList);
    System.out.println("newTokenList2: " + newTokenList);
    assertTrue( newTokenList.get(4).getText().equals("((NO)H)") );
  }
}

