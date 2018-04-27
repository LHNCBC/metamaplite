package gov.nih.nlm.nls.metamap.lite;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;

import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.metamap.lite.FindLongestMatch;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;

/**
 * FindLongestMatchTest - Test FindLongestMatch method.
 *
 * Test is performed using in-memory dictionary and input text.
 *
 * Created: Thu Apr 20 17:31:52 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class FindLongestMatchTest {
  Map<String,String> dictionary;
  
  List<ERToken> tokenlist0;
  Set<TermInfo> refTermInfoSet0;
  List<ERToken> tokenlist1;
  DictionaryLookup dictionaryLookup;
  /**
   * Test if code that removes subsumed entities works as expected.
   *
   */
  public FindLongestMatchTest() {
  }

  /** Part of speech tags used for term lookup, can be set using
   * property: metamaplite.pos.taglist; the tag list is a set of Penn
   * Treebank part of speech tags separated by commas. */
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  public void defaultAllowedPartOfSpeech() {
    this.allowedPartOfSpeechSet.add("RB"); // should this be here?
    this.allowedPartOfSpeechSet.add("NN");
    this.allowedPartOfSpeechSet.add("NNS");
    this.allowedPartOfSpeechSet.add("NNP");
    this.allowedPartOfSpeechSet.add("NNPS");
    this.allowedPartOfSpeechSet.add("JJ");
    this.allowedPartOfSpeechSet.add("JJR");
    this.allowedPartOfSpeechSet.add("JJS");
    this.allowedPartOfSpeechSet.add(""); // empty if not part-of-speech tagged (accept everything)
  }

  /**
   * Describe class HashMapDictionaryLookup here.
   *
   *
   * Created: Mon Apr 23 08:55:59 2018
   *
   * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
   * @version 1.0
   */
  static class HashMapDictionaryLookup implements DictionaryLookup {
    
    
    Map<String,String> dictionary;
    /**
     * Creates a new <code>HashMapDictionaryLookup</code> instance.
     *
     */
    public HashMapDictionaryLookup(Map<String,String> dictionary) {
      this.dictionary = dictionary;
    }

    // Implementation of gov.nih.nlm.nls.metamap.lite.DictionaryLookup

    /**
     * Describe <code>lookup</code> method here.
     *
     * @param originalTerm a <code>String</code> value
     * @param normTerm a <code>String</code> value
     * @param tokenlist a <code>List</code> value
     * @return a <code>TermInfo</code> value
     */
    public final TermInfo lookup(final String originalTerm, final String normTerm, final List<? extends Token>  tokenlist) {
      String cui = this.dictionary.get(normTerm.toLowerCase());
      if (cui == null) {
	cui = this.dictionary.get(originalTerm.toLowerCase());
      }
      if (cui != null) {
	System.out.println("HashMapDictionary:lookup: cui: " + cui);
	return new TermInfoImpl(originalTerm, normTerm, tokenlist, cui);
      } else {
	return null;
      }
    }
  }

  @org.junit.Before public void setup() {
    // initialize tokenlists and term info sets
    tokenlist0 = Scanner.analyzeText("Papillary Thyroid Carcinoma is a Unique Clinical Entity");

    refTermInfoSet0 = new TreeSet<TermInfo>();
    
    refTermInfoSet0.add(new TermInfoImpl("Papillary Thyroid Carcinoma",
					"papillary thyroid carcinoma",
					tokenlist0.subList(0,5),
					 "C0238463"));
    refTermInfoSet0.add(new TermInfoImpl("Thyroid Carcinoma",
					 "thyroid carcinoma",
					 tokenlist0.subList(2,5),
					 "C0549473"));
    refTermInfoSet0.add(new TermInfoImpl("Thyroid",
					 "thyroid",
					 tokenlist0.subList(2,3),
					 "C0040132"));
    refTermInfoSet0.add(new TermInfoImpl("Carcinoma",
					 "carcinoma",
					 tokenlist0.subList(4,5),
					 "C0007097"));
    refTermInfoSet0.add(new TermInfoImpl("Unique",
					"unique",
					tokenlist0.subList(10,11),
					 "C1710548"));
    refTermInfoSet0.add(new TermInfoImpl("Entity",
					 "entity",
					tokenlist0.subList(14,15),
					 "C1551338"));
    
    tokenlist1 = Scanner.analyzeText("Dimethyl fumarate attenuates 6-OHDA-induced neurotoxicity in SH-SY5Y cells and in animal model of Parkinson's disease by enhancing Nrf2 activity.");

    // initialize dictionary
    this.dictionary = new HashMap<String,String>();
    this.dictionary.put("6-ohda", "C0085196");
    this.dictionary.put("carcinoma", "C0007097");
    this.dictionary.put("dimethyl fumarate","C0058218");
    this.dictionary.put("disease", "C0012634");
    this.dictionary.put("entity", "C1551338");
    this.dictionary.put("papillary thyroid carcinoma", "C0238463");
    this.dictionary.put("parkinson's disease", "C0030567");
    this.dictionary.put("thyroid carcinoma", "C0549473");
    this.dictionary.put("thyroid", "C0040132");
    this.dictionary.put("unique", "C1710548");
    this.dictionary.put("animal model", "C0012644");
    this.dictionary.put("induced", "C0205263");
    this.dictionary.put("neurotoxicity", "C0235032");
    this.dictionaryLookup = new HashMapDictionaryLookup(this.dictionary);
    this.defaultAllowedPartOfSpeech();
  }

  @org.junit.Test public void test0() {
    System.out.println("tokenlist0: " + tokenlist0);
    List<TermInfo> termInfoList = 
      FindLongestMatch.findLongestMatch(this.tokenlist0,
					this.allowedPartOfSpeechSet,
					this.dictionaryLookup);
    System.out.println("FindLongestMatchTest:test0: size of termInfoList: " + termInfoList.size());
    for (TermInfo termInfo: termInfoList) {
      System.out.println("FindLongestMatchTest: " + termInfo.toString());
    }
    Set<TermInfo> termInfoSet = new TreeSet<TermInfo>(termInfoList);
    System.out.println("refTermInfoSet0: " + refTermInfoSet0);
    System.out.println("termInfoSet: " + termInfoSet);
    org.junit.Assert.assertTrue((termInfoList.size() == 6) &&
				refTermInfoSet0.equals(termInfoSet));
  }

  @org.junit.Test public void test1() {
    List<TermInfo> termInfoList = 
      FindLongestMatch.findLongestMatch(this.tokenlist1,
					this.allowedPartOfSpeechSet,
					this.dictionaryLookup);
    System.out.println("FindLongestMatchTest:test1: size of termInfoList: " + termInfoList.size());
    for (TermInfo termInfo: termInfoList) {
      System.out.println("FindLongestMatchTest: " + termInfo.toString());
    }
    org.junit.Assert.assertTrue(termInfoList.size() == 6);
  }
}
