package gov.nih.nlm.nls.metamap.lite;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Properties;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.PosToken;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.TokenListUtils;

import gov.nih.nlm.nls.metamap.lite.types.MMLEntity;
import gov.nih.nlm.nls.metamap.lite.types.Span;
import gov.nih.nlm.nls.metamap.lite.types.SpanImpl;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;

import gov.nih.nlm.nls.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import gov.nih.nlm.nls.metamap.TermConcept

/**
 * Describe class FindLongestMatch here.
 *
 *
 * Created: Wed Mar 14 12:45:39 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class FindLongestMatch {
  private static final Logger logger =
    LoggerFactory.getLogger(FindLongestMatch.class);

  /**
   * Creates a new <code>FindLongestMatch</code> instance.
   * @param properties metamaplite configuration properties instance
   */
  public FindLongestMatch(Properties properties)
  {
  }

  /**
   * Given Example:
   * <pre>
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
   * </pre>
   * 
   * Check the following token sublists:
   * <pre>
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity"
   *   "Papillary Thyroid Carcinoma is a Unique Clinical"
   *   "Papillary Thyroid Carcinoma is a Unique"
   *   "Papillary Thyroid Carcinoma is a"
   *   "Papillary Thyroid Carcinoma is"
   *   "Papillary Thyroid Carcinoma"
   *   "Papillary Thyroid"
   *   "Papillary"
   *             "Thyroid Carcinoma is a Unique Clinical Entity"
   *             "Thyroid Carcinoma is a Unique Clinical"
   *             "Thyroid Carcinoma is a Unique"
   *             "Thyroid Carcinoma is a"
   *             "Thyroid Carcinoma is"
   *             "Thyroid Carcinoma"
   *             "Thyroid"
   *    ...
   * </pre>
   * @param tokenList tokenlist of document
   * @param allowedPartOfSpeechSet term head must be in allowed part of speech set
   * @param lookupImpl dictionary lookup class
   * @return list of term info instances
   */
  public static List<TermInfo> findLongestMatch(List<ERToken> tokenList,
					 Set<String> allowedPartOfSpeechSet,
					 DictionaryLookup<TermInfo> lookupImpl)
  {
    logger.debug("findLongestMatch");
    List<TermInfo> termInfoList = new ArrayList<TermInfo>();
    List<List<? extends Token>> listOfTokenSubLists = TokenListUtils.createSubListsOpt(tokenList);
    for (List<? extends Token> tokenSubList: listOfTokenSubLists) {
      List<String> tokenTextSubList = new ArrayList<String>();
      for (Token token: tokenSubList) {
       	tokenTextSubList.add(token.getText());
      }
      ERToken firstToken = (ERToken)tokenSubList.get(0);
      ERToken lastToken = (ERToken)tokenSubList.get(tokenSubList.size() - 1);
      if ((! firstToken.getText().toLowerCase().equals("other")) &&
       	  allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech())) {
       	int termLength = (tokenSubList.size() > 1) ?
       	  (lastToken.getOffset() + lastToken.getText().length()) - firstToken.getOffset() : 
       	  firstToken.getText().length();
       	String originalTerm = StringUtils.join(tokenTextSubList, "");
	if ((originalTerm.length() > 2) &&
	    (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	  int offset = ((PosToken)tokenSubList.get(0)).getOffset();
	  // term must begin with alphabetic character.
	  if (CharUtils.isAlpha(originalTerm.charAt(0))) {
	    TermInfo termInfo = lookupImpl.lookup(originalTerm);
	    if (termInfo != null) {
	      termInfoList.add(termInfo);
	    }
	  }
	}
      }
    }
    return termInfoList;
  }


  /**
   * Given Example:
   * <pre>
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
   * </pre>
   * 
   * Check the following:
   * <pre>
   *   "Papillary Thyroid Carcinoma is a Unique Clinical Entity"
   *   "Papillary Thyroid Carcinoma is a Unique Clinical"
   *   "Papillary Thyroid Carcinoma is a Unique"
   *   "Papillary Thyroid Carcinoma is a"
   *   "Papillary Thyroid Carcinoma is"
   *   "Papillary Thyroid Carcinoma"
   *   "Papillary Thyroid"
   *   "Papillary"
   *             "Thyroid Carcinoma is a Unique Clinical Entity"
   *             "Thyroid Carcinoma is a Unique Clinical"
   *             "Thyroid Carcinoma is a Unique"
   *             "Thyroid Carcinoma is a"
   *             "Thyroid Carcinoma is"
   *             "Thyroid Carcinoma"
   *             "Thyroid"
   *    ...
   * </pre>
   * @param tokenList tokenlist of document
   * @param termFilter filter terms by criteria, for example: part of speech, phrase type, etc.
   * @param contextInfo additional context about tokenlist
   * @param lookupImpl dictionary lookup class
   * @return list MML Entity instances
   */
  public static List<MMLEntity<TermInfo>> findLongestMatch(List<ERToken> tokenList,
						TermFilter termFilter,
						Map<String,Object> contextInfo,
						DictionaryLookup<TermInfo> lookupImpl)
  {
    logger.debug("findLongestMatch");
    List<MMLEntity<TermInfo>> entityList = new ArrayList<MMLEntity<TermInfo>>();
    List<List<? extends Token>> listOfTokenSubLists = TokenListUtils.createSubListsOpt(tokenList);
    for (List<? extends Token> tokenSubList: listOfTokenSubLists) {
      List<String> tokenTextSubList = new ArrayList<String>();
      for (Token token: tokenSubList) {
       	tokenTextSubList.add(token.getText());
      }
      ERToken firstToken = (ERToken)tokenSubList.get(0);
      ERToken lastToken = (ERToken)tokenSubList.get(tokenSubList.size() - 1);
      if (termFilter.filterToken(firstToken, contextInfo)) {
       	int termLength = (tokenSubList.size() > 1) ?
       	  (lastToken.getOffset() + lastToken.getText().length()) - firstToken.getOffset() : 
       	  firstToken.getText().length();
       	String originalTerm = StringUtils.join(tokenTextSubList, "");
	if ((originalTerm.length() > 2) &&
	    (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	  int offset = firstToken.getOffset();
	  int endOffset = lastToken.getOffset() + lastToken.getText().length();
	  // term must begin with alphabetic character.
	  if (CharUtils.isAlpha(originalTerm.charAt(0))) {
	    TermInfo termInfo = lookupImpl.lookup(originalTerm);
	    if (termInfo != null) {
	      List<Span> spanList = new ArrayList<Span>();
	      spanList.add(new SpanImpl(offset, endOffset));
	      entityList.add(new MMLEntity<TermInfo>(spanList, termInfo));
	    }
	  }
	}
      }
    }
    return entityList;
  }

  /** An example Term Filter class */
  public static class SampleTermFilter implements TermFilter {
    /** Set of parts-of-speech allowed, usually a set of Penn Treebank tags. */
    Set<String> allowedPartOfSpeechSet;

    /**
     * Instantiate a SampleTermFilter instance with set of allowed parts-of-speech.
     * @param allowedPartOfSpeechSet allowed parts-of-speech.
     */
    public SampleTermFilter(Set<String> allowedPartOfSpeechSet) {
      this.allowedPartOfSpeechSet = allowedPartOfSpeechSet;
    }

    /**
     * If token is in allowed part of speech set and is not "other"
     * then return true.  otherwise return false.
     * @param firstToken token to be tested
     * @param contextInfo contextual Information associated with token (future).
     * @return True if token meets criteria, false otherwise.
    */
    public boolean filterToken(ERToken firstToken, Map<String,Object> contextInfo) {
      return (! firstToken.getText().toLowerCase().equals("other")) &&
	allowedPartOfSpeechSet.contains(firstToken.getPartOfSpeech());
    }
  }
}
