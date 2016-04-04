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
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.NegExKeyMap;
// import explore.negex;

/**
 * Given tokenlist, and EntityList:
 * <ul>
 * <li> remove non-useful tokens: pn and ws tokens 
 * <li> get string from tokens
 * <li> get negation phrase list 
 * <li> generate matchin negation phrase for sentence if present.
 * </ul>
 */

public class NegEx implements NegationDetector {

  /** Entity must be within this number of tokens of negation phrase to be considered negated. */
  int tokenWindow = 6;
  /** Entity must have at least one of the semantic types in semanticTypeSet to be considered negated. */
  Set<String> semanticTypeSet = new HashSet<String>
    (Arrays.asList("acab","anab","biof","cgab","comd","dsyn","emod","fndg",
		   "inpo","lbtr","menp","mobd","neop","patf","phsf","sosy"));
  public NegEx() { }
  public NegEx(Properties properties) {
    this.tokenWindow = Integer.parseInt
      (properties.getProperty("metamaplite.negex.tokenwindowsize", "6"));
    if (properties.getProperty("metamaplite.negex.semantic.type.set") != null) {
      this.semanticTypeSet = new HashSet<String>
	(Arrays.asList(properties.getProperty("metamaplite.negex.semantic.type.set").split(",")));
    }
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
      if ((! token.getTokenClass().equals("pn")) ||
	  (! token.getTokenClass().equals("ws"))) {
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
    int tokenPosition = -1;
    int i = 0;
    for (ERToken token: tokenlist) {
      if (entity.getOffset() == token.getOffset()) {
	tokenPosition = i;
      }
      i++;
    }
    return tokenPosition;
  }

  boolean NoConjunctionBetweenNegaAndEntity(int entityTokenPosition,
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

  boolean NoConjunctionBetweenNegbAndEntity(int entityTokenPosition,
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

  public void markNegatedEntities(List<ERToken> filteredTokenlist,
				  List<NegPhraseInfo> negationPhraseList,
				  List<NegPhraseInfo> conjPhraseList,
				  Collection<Entity> entityColl) {
    for (Entity entity: entityColl) {
      int entityOffset = entity.getStart();
      for (NegPhraseInfo info: negationPhraseList) {
	System.out.println("negation info: " + info);
	if (info.getType().equals("nega") || info.getType().equals("pnega")) {
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
		  if (NoConjunctionBetweenNegaAndEntity(entityTokenPosition,
							conjPhraseList,
							negPhraseTokenPosition.intValue())) {
		    entity.setNegated(true);
		  }
		}
	      }
	    }
	  }
	} else if (info.getType().equals("negb") || info.getType().equals("pnegb")) {
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
		  if (NoConjunctionBetweenNegbAndEntity(entityTokenPosition,
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
	for (String semtype: ev.getConceptInfo().getSemanticTypeSet()) {
	  if (semanticTypeSet.contains(semtype)) {
	    inSet = true;
	  }
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
	  (! token.getTokenClass().equals("pd"))) {
	newTokenlist.add(token);
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
    List<ERToken> filteredTokenList = filterTokenList(tokenList);
    Collection<Entity> filteredEntityColl = filterEntityCollection(entityColl);
    List<String> tokenStringList = extractStringsFromTokenlist(filteredTokenList);
    List<NegPhraseInfo> negationPhraseList0 =
      getNegationPhraseList(tokenStringList, NegExKeyMap.negationPhraseTypeMap);
    List<NegPhraseInfo> conjPhraseList = listConjPhrases(negationPhraseList0);
    List<NegPhraseInfo> negationPhraseList = keepLongestNegationPhrases(negationPhraseList0);
    markNegatedEntities(filteredTokenList, negationPhraseList, conjPhraseList, filteredEntityColl);
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
