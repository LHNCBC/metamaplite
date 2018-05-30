//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;

import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

/**
 * TokenListUtilsTest - test createSubLists and createSubListsOpt
 * 
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 0.1
 */
@RunWith(JUnit4.class)
public class TokenListUtilsTest {
  String text0 = "There was no sign of pneumonia.";
  String text1 = "3. APC and APC2 may therefore have comparable functions in development and cancer.";
  List<? extends Token> tokenList0;
  List<? extends Token> tokenList1;

  @org.junit.Before public void setup() {
    this.tokenList0 = Scanner.addOffsets
      (Scanner.classifyTokenList
       (new ArrayList<Token>(Tokenize.mmPosTokenize(text0,0))));
    this.tokenList1 = Scanner.addOffsets
      (Scanner.classifyTokenList
       (new ArrayList<Token>(Tokenize.mmPosTokenize(text1,0))));
  }

  @org.junit.Test public void testCreateSubLists0() {
    List<List<? extends Token>> listOfTokenLists0 = TokenListUtils.createSubLists(this.tokenList0);
    System.out.println("listOfTokenLists0.get(0): " + listOfTokenLists0.get(0));
    System.out.println("listOfTokenLists0.get(0): " + listOfTokenLists0.get(listOfTokenLists0.size() - 1));
    System.out.println("listOfTokenLists0.get(0).get(0).getText(): " + 
		       listOfTokenLists0.get(0).get(0).getText());
    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2).size(): " +
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2).size());
    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2): " + 
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2));

    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText(): " + 
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText());
    org.junit.Assert.assertTrue(listOfTokenLists0.get(0).size() > 0 &&
				listOfTokenLists0.get(0).get(0).getText().equals("There") &&
				listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText().equals("pneumonia"));
  }

  @org.junit.Test public void testCreateSubLists1() {
    List<List<? extends Token>> listOfTokenLists1 = TokenListUtils.createSubLists(this.tokenList1);
    System.out.println("listOfTokenLists1.get(0): " + listOfTokenLists1.get(0));
    System.out.println("listOfTokenLists1.get(0): " + listOfTokenLists1.get(listOfTokenLists1.size() - 1));
    System.out.println("listOfTokenLists1.get(0).get(0).getText(): " + 
		       listOfTokenLists1.get(0).get(0).getText());
    System.out.println("listOfTokenLists1.get(listOfTokenLists1.size() - 2).size(): " +
		       listOfTokenLists1.get(listOfTokenLists1.size() - 2).size());
    System.out.println("listOfTokenLists1.get(listOfTokenLists1.size() - 2): " + 
		       listOfTokenLists1.get(listOfTokenLists1.size() - 2));
    System.out.println("listOfTokenLists1.get(listOfTokenLists1.size() - 2).get(0).getText(): " + 
		       listOfTokenLists1.get(listOfTokenLists1.size() - 2).get(0).getText());
    org.junit.Assert.assertTrue(listOfTokenLists1.get(0).size() > 0 &&
				listOfTokenLists1.get(0).get(0).getText().equals("3") &&
				(listOfTokenLists1.get(listOfTokenLists1.size() - 2).size() == 1) &&
				listOfTokenLists1.get(listOfTokenLists1.size() - 2).get(0).getText().equals("cancer"));
  }
  

  @org.junit.Test public void testCreateSubListsOpt0() {
    List<List<? extends Token>> listOfTokenLists0 = TokenListUtils.createSubListsOpt(this.tokenList0);
    System.out.println("listOfTokenLists0.get(0): " + listOfTokenLists0.get(0));
    System.out.println("listOfTokenLists0.get(0): " + listOfTokenLists0.get(listOfTokenLists0.size() - 1));
    System.out.println("listOfTokenLists0.get(0).get(0).getText(): " + 
		       listOfTokenLists0.get(0).get(0).getText());
    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2).size(): " +
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2).size());
    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2): " + 
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2));

    System.out.println("listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText(): " + 
		       listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText());
    int i = 0;
    for (List<? extends Token> tokenList: listOfTokenLists0) {
      System.out.println(i + ": " + tokenList);
      i++;
    }
    org.junit.Assert.assertTrue(listOfTokenLists0.get(0).size() > 0 &&
				listOfTokenLists0.get(0).get(0).getText().equals("There") &&
				listOfTokenLists0.get(listOfTokenLists0.size() - 2).get(0).getText().equals("pneumonia"));
  }
}
