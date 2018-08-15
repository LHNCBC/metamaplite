//
package gov.nih.nlm.nls.metamap.lite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import bioc.BioCSentence;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.ERTokenImpl;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.NegExKeyMap;
import gov.nih.nlm.nls.tools.SetOps;
// import explore.negex;

/**
 * Given tokenlist, and EntityList:
 * <ul>
 * <li> remove non-useful tokens: pn and ws tokens 
 * <li> get string from tokens
 * <li> get negation phrase list 
 * <li> determine matching negation phrase for sentence if present.
 * </ul>
 */

public class NegEx implements NegationDetector {

  /** Entity must be within this number of tokens of negation phrase to be considered negated. */
  int tokenWindow = 6;
  /** Entity must have at least one of the semantic types in semanticTypeSet to be considered negated. */
  Set<String> semanticTypeSet = new HashSet<String>
    (Arrays.asList("acab","anab","biof","cgab","comd","dsyn","emod","fndg",
		   "inpo","lbtr","menp","mobd","neop","patf","phsf","sosy"));
  public NegEx() {
    initProperties(System.getProperties());
  }
  public NegEx(Properties properties) {
    initProperties(properties);
  }

  public void initProperties(Properties properties) {
    this.tokenWindow = Integer.parseInt
      (properties.getProperty("metamaplite.negex.tokenwindowsize", "6"));
    if (properties.getProperty("metamaplite.negex.semantic.type.set") != null) {
      this.semanticTypeSet = new HashSet<String>
	(Arrays.asList(properties.getProperty("metamaplite.negex.semantic.type.set").split(",")));
    }
  }

  /**
   * Convert tokenlist to strings, removing punctuation and whitespace
   * tokens.
   * @param tokenList list of Enhanced Tokens
   * @return list of strings from tokenlist
   */
  public List<String> extractStringsFromTokenlist(List<ERToken> tokenList) {
    List<String> stringList = new ArrayList<String>();
    for (ERToken token: tokenList) {
      if (! token.getTokenClass().equals("ws")) {
	stringList.add(token.getText());
      }
    }
    return stringList;
  }

  public List<Integer> findPhrase(List<String> stringlist, List<String> phrase) {
    List<Integer> foundList = new ArrayList<Integer>();
    int slength = stringlist.size();
    int plength = phrase.size();
    int n = 0;
    boolean notfound = (n < slength) &&  ((n + plength) <= slength);
    while (notfound) {
      List<String> window = new ArrayList<String>();
      for (String token: stringlist.subList(n, n + plength)) {
	window.add(token.toLowerCase());
      }
      if (phrase.equals(window)) {
	foundList.add(new Integer(n));
      }
      n++;
      notfound = (n < slength) &&  ((n + plength) <= slength);
    }
    return foundList;
  }

  public List<NegPhraseInfo> keepLongestNegationPhrases(List<NegPhraseInfo> negationPhraseList) {
    Map<Integer,NegPhraseInfo> positionInfoMap = new HashMap<Integer,NegPhraseInfo>();
    for (NegPhraseInfo info: negationPhraseList) {
      for (Integer position: info.getPositionList()) {
	if (positionInfoMap.containsKey(position)) {
	  if (info.getPhrase().size() > 
	      positionInfoMap.get(position).getPhrase().size()) {
	    // if phrase is longer than stored phrase then replace it with longer phrase.
	    positionInfoMap.put(position, info);
	  }
	} else {
	  positionInfoMap.put(position, info);
	}
      }
    }
    return new ArrayList<NegPhraseInfo>(positionInfoMap.values());
  }

  /**
   * @param stringList list of tokenstrings
   * @param negationPhraseTypeMap dictionary of phrases mapped to negation types
   * @return list of negation phrases
   */
  public List<NegPhraseInfo> getNegationPhraseList(List<String> stringList,
					   Map<List<String>,String> negationPhraseTypeMap) {
    List<NegPhraseInfo> negationPhraseList = new ArrayList<NegPhraseInfo>();
    for (Map.Entry<List<String>,String> entry: negationPhraseTypeMap.entrySet()) {
      List<Integer> positionList = findPhrase(stringList, entry.getKey());
      if (positionList.size() > 0) {
	negationPhraseList.add(new NegPhraseInfo(entry.getKey(),
						 entry.getValue(),
						 positionList));
      }
    }
    return negationPhraseList;
  }

  public List<MatchResult> findPhrase(String sentence, Pattern phrasePattern) {
    List<MatchResult> matchList = new ArrayList<MatchResult>();
    Matcher matcher = phrasePattern.matcher(sentence);
    if (matcher.find()) {
      matchList.add(matcher.toMatchResult());
    }
    return matchList;
  }

  public int getEntityTokenPosition(Entity entity, List<ERToken> tokenlist) {
    if ((entity.getOffset() < tokenlist.get(0).getOffset()) ||
	(entity.getOffset() > (tokenlist.get(tokenlist.size() - 1).getOffset() +
			       tokenlist.get(tokenlist.size() - 1).getText().length()))) {
      return -1;
    } else {
      int tokenPosition = -1;
      int i = 0;
      for (ERToken token: tokenlist) {
	if (entity.getOffset() == token.getOffset()) {
	  tokenPosition = i;
	} else if ((entity.getOffset() > token.getOffset()) &&
		   (entity.getOffset() + entity.getMatchedText().length()) <=
		   (token.getOffset() + token.getText().length())) {
	  tokenPosition = i;
	}
	i++;
      }
      return tokenPosition;
    }
  }

  /**
   * Determine if there is not a conjunction in conjPhraseList between
   * negationPhrase and target entity.
   * @param entityTokenPosition text offset of target entity
   * @param conjPhraseList list of conjunctions 
   * @param triggerPosition text offset of negation trigger phrase
   * @return true if there is no conjunction in conjPhraseList between
   * negationPhrase and target entity.
   */
  boolean noConjunctionBetweenNegaAndEntity(int entityTokenPosition,
					    List<NegPhraseInfo> conjPhraseList,
					    int triggerPosition) {
    boolean status = true;
    for (NegPhraseInfo conjPhrase: conjPhraseList) {
      for (Integer conjPosition: conjPhrase.getPositionList()) {
	if ((conjPosition > triggerPosition) && (conjPosition < entityTokenPosition)) {
	  status = false;
	}
      }
    }
    return status;
  }

  /**
   * Determine if there is not a conjunction in conjPhraseList between
   * negationPhrase and target entity.
   * @param entityTokenPosition text offset of target entity
   * @param conjPhraseList list of conjunctions 
   * @param triggerPosition text offset of negation trigger phrase
   * @return true if there is no conjunction in conjPhraseList between
   * negationPhrase and target entity.
   */
  boolean noConjunctionBetweenNegbAndEntity(int entityTokenPosition,
					    List<NegPhraseInfo> conjPhraseList,
					    int triggerPosition) {
    boolean status = true;
    for (NegPhraseInfo conjPhrase: conjPhraseList) {
      for (Integer conjPosition: conjPhrase.getPositionList()) {
	if ((conjPosition < triggerPosition) && (conjPosition > entityTokenPosition)) {
	  status = false;
	}
      }
    }
    return status;
  }

  /**
   * Mark entities in window of negation phrase.  Ignore pseudo negation before (pnega)
   * and after (pnegb).
   *
   * @param filteredTokenlist filtered token list 
   * @param negationPhraseList list of negation phrases
   * @param conjPhraseList list of conjunctions 
   * @param entityColl collection of entity candidates for marking.
   */
  public void markNegatedEntities(List<ERToken> filteredTokenlist,
				  List<NegPhraseInfo> negationPhraseList,
				  List<NegPhraseInfo> conjPhraseList,
				  Collection<Entity> entityColl) {
    for (Entity entity: entityColl) {
      int entityOffset = entity.getStart();
      for (NegPhraseInfo info: negationPhraseList) {
	// System.out.println("negation info: " + info);
	if (info.getType().equals("nega")) {
	  for (Integer negPhraseTokenPosition: info.getPositionList()) {
	    int phraseOffset = filteredTokenlist.get(negPhraseTokenPosition.intValue()).getOffset();
	    if (entityOffset >= phraseOffset) {
	      // If entity and negation phrase distance is within
	      // token window then mark negation as true.
	      int entityTokenPosition = getEntityTokenPosition(entity, filteredTokenlist);
	      if (entityTokenPosition >= 0) {
		int distance = Math.abs(entityTokenPosition - negPhraseTokenPosition.intValue());
		if (distance <= tokenWindow) {
		  // if no conjunction between trigger and target entity, then entity is negated.
		  if (noConjunctionBetweenNegaAndEntity(entityTokenPosition,
							conjPhraseList,
							negPhraseTokenPosition.intValue())) {
		    entity.setNegated(true);
		  }
		}
	      }
	    }
	  }
	} else if (info.getType().equals("negb")) {
	  for (Integer negPhraseTokenPosition: info.getPositionList()) {
	    int phraseOffset = filteredTokenlist.get(negPhraseTokenPosition.intValue()).getOffset();
	    if (entityOffset < phraseOffset) {
	      // If entity and negation phrase distance is within
	      // token window then mark negation as true.
	      int entityTokenPosition = getEntityTokenPosition(entity, filteredTokenlist);
	      if (entityTokenPosition >= 0) {
		int distance = Math.abs(entityTokenPosition - negPhraseTokenPosition.intValue());
		if (distance <= tokenWindow) {
		  // if no conjunction between trigger and target entity, then entity is negated.
		  if (noConjunctionBetweenNegbAndEntity(entityTokenPosition,
							conjPhraseList,
							negPhraseTokenPosition.intValue())) {
		    entity.setNegated(true);
		  }
		}
	      }
	    }
	  } 
	}
      }
    }
  }

  public Collection<Entity> filterEntityCollection(Collection<Entity> entityColl) {
    Collection<Entity> newEntityColl = new ArrayList<Entity>();
    for (Entity entity: entityColl) {
      boolean inSet = false;
      for (Ev ev: entity.getEvList()) {
	if (SetOps.intersection(ev.getConceptInfo().getSemanticTypeSet(),
				semanticTypeSet).size() > 0) {
	  inSet = true;
	}
      }
      if (inSet) {
	newEntityColl.add(entity);
      }
    }
    return newEntityColl;
  }

  public List<ERToken> filterTokenList(List<ERToken> tokenList) {
    List<ERToken> newTokenlist = new ArrayList<ERToken>();
    for (ERToken token: tokenList) {
      if ((! token.getTokenClass().equals("ws")) &&
	  (! token.getTokenClass().equals("pd")) ||
	  NegExKeyMap.negationPhraseTypeMap.containsKey(Arrays.asList(token.getText()))) {
	newTokenlist.add(token);
      } 
    }
    return newTokenlist;
  }

  /* 
   * Determine range of metatoken at head of tokenlist.
   * @param subtokenlist 
   * @return range of metatoken in tokenlist
   */
  public int getRangeOfMetaToken(List<ERToken> subtokenlist) {
    int range = 0;
    while ((range < (subtokenlist.size() - 1)) &&
	   (subtokenlist.get(range+1).getTokenClass() != "ws") &&
	   (subtokenlist.get(range+1).getTokenClass() != "pd")) {
      range++;
    }
    return range;
  }

  /**
   * combine tokens of the form: alpha-token dash-token alpha-token in one token.
   * @param tokenList token list 
   * @return modified token list with metaTokens replacing alpha dash alpha token combinations.
   */
  public List<ERToken> addMetaTokens(List<ERToken> tokenList) {
    List<ERToken> newTokenlist = new ArrayList<ERToken>();
    int i = 0;
    while ((i + 3) < tokenList.size()) {
      if (((i + 3) < tokenList.size()) &&
	  (tokenList.get(i).getTokenClass() != "ws") &&
	  (tokenList.get(i+1).getTokenClass() != "ws") &&
	  (tokenList.get(i+2).getTokenClass() != "ws") &&
	  (tokenList.get(i+2).getTokenClass() != "pd") &&
	  (! NegExKeyMap.negationPhraseTypeMap.containsKey(Arrays.asList(tokenList.get(i+2).getText())))) {
	int range = getRangeOfMetaToken(tokenList.subList(i+2, tokenList.size())) + 3;
	int offset = tokenList.get(i).getOffset();
	String tc = tokenList.get(i).getTokenClass();
	if (range > 0) {
	  int rend = range + i;
	  StringBuilder sb = new StringBuilder();
	  while ((i < rend) && (i < tokenList.size())) {
	    sb.append(tokenList.get(i).getText());
	    i++;
	  }
	  newTokenlist.add(new ERTokenImpl(sb.toString(), offset, tc));
	} else {
	  newTokenlist.add(tokenList.get(i));
	  i++;
	}
      } else {
	newTokenlist.add(tokenList.get(i));
	i++;
      }
    }
    if (i < tokenList.size()) {
      newTokenlist.add(tokenList.get(i));
      if ((i+1) < tokenList.size()) {
	newTokenlist.add(tokenList.get(i+1));
	if ((i+2) < tokenList.size()) {
	  newTokenlist.add(tokenList.get(i+2));
	}
      }
    }
    return newTokenlist;
  }

  public List<NegPhraseInfo> listConjPhrases(List<NegPhraseInfo> negationPhraseList) {
    /* to be implemented */
    List<NegPhraseInfo> conjPhraseList = new ArrayList<NegPhraseInfo>();
    /* collect conj and non-conj phrases in two lists. */
    for (NegPhraseInfo negPhrase: negationPhraseList) {
      if (negPhrase.getType().equals("conj")) {
	conjPhraseList.add(negPhrase);
      } 
    }
    return conjPhraseList;
  }

  /**
   *
   * @param tokenList list of tokens for target sentence
   * @param entityColl list of entities for target sentence
   */
  public void tokenNegex(List<ERToken> tokenList, Collection<Entity> entityColl) {
    List<ERToken> modTokenList = addMetaTokens(tokenList);
    List<ERToken> filteredTokenList = filterTokenList(modTokenList);
    Collection<Entity> filteredEntityColl = filterEntityCollection(entityColl);
    List<String> tokenStringList = extractStringsFromTokenlist(filteredTokenList);
    List<NegPhraseInfo> negationPhraseList0 =
      getNegationPhraseList(tokenStringList, NegExKeyMap.negationPhraseTypeMap);
    if (negationPhraseList0.size() > 0) {
      List<NegPhraseInfo> conjPhraseList = listConjPhrases(negationPhraseList0);
      List<NegPhraseInfo> negationPhraseList = keepLongestNegationPhrases(negationPhraseList0);
      markNegatedEntities(filteredTokenList, negationPhraseList, conjPhraseList, filteredEntityColl);
    }
  }

  /**
   * 
   */
  public void detectNegations(Set<Entity> entitySet, String sentence, List<ERToken> tokenList) {
    tokenNegex(tokenList, entitySet);
  }

  public static void testFindPhrase(NegEx inst) {
    List<String> stringlist =
      Arrays.asList
      ("The", "diagnosis", "suggests", "that", "pneumonia", "can", "be", "ruled", "out");
    List<String> phrase = Arrays.asList("can", "be", "ruled", "out");
    for (Integer pos: inst.findPhrase(stringlist, phrase)) {
      System.out.println("position: " + pos);
    }

    stringlist =
      Arrays.asList("There", "was", "no", "sign", "of", "pneumonia");
    phrase = Arrays.asList("no", "sign", "of");
    for (Integer pos: inst.findPhrase(stringlist, phrase)) {
      System.out.println("position: " + pos);
    }
  }

  /**
   * See gov.nih.nlm.nls.metamap.lite.NegExTest in src/test/java for unit tests.
   * @param args - Arguments passed from the command line
   **/
  public static void main(String[] args) {
    NegEx inst = new NegEx();
    testFindPhrase(inst);
  }
}
