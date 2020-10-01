
// EntityLookup from Left to Right, Longest Match
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioc.BioCAnnotation;
import bioc.BioCPassage;
import bioc.BioCSentence;
import bioc.BioCLocation;
import bioc.BioCRelation;
import bioc.BioCNode;
import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;
import gov.nih.nlm.nls.metamap.lite.context.ContextWrapper;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.MatchInfo;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.TokenListUtils;
import gov.nih.nlm.nls.metamap.prefix.CharUtils;
import gov.nih.nlm.nls.metamap.lite.BioCEntityLookup;
import gov.nih.nlm.nls.metamap.lite.BioCSentenceEntityAnnotator;
import gov.nih.nlm.nls.metamap.lite.BioCSentenceEntityAnnotatorImpl;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;

/**
 *
 */

public class BioCLRLongestMatchLookup implements BioCEntityLookup
{
  private static final Logger logger = LoggerFactory.getLogger(EntityLookup4.class);

  public MetaMapIvfIndexes mmIndexes;
  Set<String> allowedPartOfSpeechSet = new HashSet<String>();
  
  /** string column for cuisourceinfo index*/
  int strColumn = 3;		
  /** cui column for semantic type and cuisourceinfo index */
  int cuiColumn = 0;		
  SpecialTerms excludedTerms = new SpecialTerms();
  int MAX_TOKEN_SIZE =
    Integer.parseInt(System.getProperty("metamaplite.entitylookup3.maxtokensize","15"));

  SentenceAnnotator sentenceAnnotator;
  NegationDetector negationDetector;
  boolean addPartOfSpeechTagsFlag =
    Boolean.parseBoolean(System.getProperty("metamaplite.enable.postagging","true"));

  BioCSentenceEntityAnnotator sentenceEntityAnnotator = new BioCSentenceEntityAnnotatorImpl();
  
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

  /** Constructor 
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public BioCLRLongestMatchLookup() 
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes();
    this.defaultAllowedPartOfSpeech();
    this.negationDetector = new ContextWrapper();
    this.sentenceEntityAnnotator = new BioCSentenceEntityAnnotatorImpl();
  }

  /** 
   * Constructor
   * @param properties metamaplite properties instance.
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public BioCLRLongestMatchLookup(Properties properties)
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes(properties);

    addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));
    if (addPartOfSpeechTagsFlag) {
      this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
    }

    // Instantiate user-specified negation detector if present,
    // otherwise use ConText.
    try {
      this.negationDetector =
	(NegationDetector)
	Class.forName
	(properties.getProperty
	 ("metamaplite.negation.detector",
	  "gov.nih.nlm.nls.metamap.lite.context.ContextWrapper")).newInstance();
      this.negationDetector.initProperties(properties);
      // this.negationDetector = new ContextWrapper();
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException(cnfe);
    } catch (InstantiationException ie) {
      throw new RuntimeException(ie);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }

    this.defaultAllowedPartOfSpeech();
    if (properties.containsKey("metamaplite.excluded.termsfile")) {
      this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
    } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
      this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
    }
    MAX_TOKEN_SIZE =
      Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
					      Integer.toString(MAX_TOKEN_SIZE)));
    this.sentenceEntityAnnotator = new BioCSentenceEntityAnnotatorImpl();
  }

  /** 
   * Constructor
   * @param properties metamaplite properties instance.
   * @param sentenceAnnotator an OpenNLP sentence annotator instance
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public BioCLRLongestMatchLookup(Properties properties, SentenceAnnotator sentenceAnnotator)
    throws IOException, FileNotFoundException
  {
    this.mmIndexes = new MetaMapIvfIndexes(properties);

    addPartOfSpeechTagsFlag =
      Boolean.parseBoolean(properties.getProperty("metamaplite.enable.postagging",
						  Boolean.toString(addPartOfSpeechTagsFlag)));
    if (addPartOfSpeechTagsFlag) {
      this.sentenceAnnotator = new OpenNLPPoSTagger(properties);
    }

    // Instantiate user-specified negation detector if present,
    // otherwise use ConText.
    try {
      this.negationDetector =
	(NegationDetector)
	Class.forName
	(properties.getProperty
	 ("metamaplite.negation.detector",
	  "gov.nih.nlm.nls.metamap.lite.context.ContextWrapper")).newInstance();
      this.negationDetector.initProperties(properties);
      // this.negationDetector = new ContextWrapper();
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException(cnfe);
    } catch (InstantiationException ie) {
      throw new RuntimeException(ie);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }

    this.defaultAllowedPartOfSpeech();
    if (properties.containsKey("metamaplite.excluded.termsfile")) {
      this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
    } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
      this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
    }
    MAX_TOKEN_SIZE =
      Integer.parseInt(properties.getProperty("metamaplite.entitylookup3.maxtokensize",
					      Integer.toString(MAX_TOKEN_SIZE)));

  }

  /** cache of string -&gt; concept and attributes */
  public static Map<String,Set<ConceptInfo>> termConceptCache = new HashMap<String,Set<ConceptInfo>>();

  public void cacheConcept(String term, ConceptInfo concept) {
    synchronized (termConceptCache) {
      if (termConceptCache.containsKey(term)) {
	synchronized (termConceptCache.get(term)) {
	  termConceptCache.get(term).add(concept);
	}
      } else {
	Set<ConceptInfo> newConceptSet = new HashSet<ConceptInfo>();
	newConceptSet.add(concept);
	synchronized (termConceptCache) {
	  termConceptCache.put(term, newConceptSet);
	}
      }
    }
  }

  public static Map<String,String> cuiPreferredNameCache =  new HashMap<String,String>();
  
  public void cachePreferredTerm(String cui, String preferredTerm) {
    synchronized (cuiPreferredNameCache) {
      cuiPreferredNameCache.put(cui, preferredTerm);
    }
  }

  /**
   * Find preferred name for cui (concept unique identifier)
   * @param cui target cui
   * @return preferredname for cui or null if not found
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public String findPreferredName(String cui)
    throws FileNotFoundException, IOException
  {
    if (cuiPreferredNameCache.containsKey(cui)) {
      return cuiPreferredNameCache.get(cui);
    } else {
      List<String> hitList = 
	this.mmIndexes.cuiConceptIndex.lookup(cui, 0);
      if (hitList.size() > 0) {
	String[] fields = hitList.get(0).split("\\|");
	this.cachePreferredTerm(cui, fields[1]);
	return fields[1];
      }
    }
    return null;
  }

  /**
   * Get source vocabulary abbreviations for cui (concept unique identifier)
   * @param cui target cui
   * @return set of source vocabulary abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException IO Exception
   * @throws IOException IO Exception
   */
  public Set<String> getSourceSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> sourceSet = new HashSet<String>();
    List<String> hitList = 
      this.mmIndexes.cuiSourceInfoIndex.lookup(cui, 0);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      sourceSet.add(fields[4]);
    }
    return sourceSet;
  }

  /**
   * Get semantic type set for cui (concept unique identifier)
   * @param cui target cui
   * @return set of semantic type abbreviations a for cui or empty set if none found.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   */
  public Set<String> getSemanticTypeSet(String cui)
    throws FileNotFoundException, IOException
  {
    Set<String> semanticTypeSet = new HashSet<String>();
    List<String> hitList = 
      this.mmIndexes.cuiSemanticTypeIndex.lookup(cui, this.cuiColumn);
    for (String hit: hitList) {
      String[] fields = hit.split("\\|");
      semanticTypeSet.add(fields[1]);
    }
    return semanticTypeSet;
  }

  /**
   * Given the string:
   *   "cancer of the lung" -&gt; "cancer, lung" -&gt; "lung cancer"
   * <pre>
   * what it does:
   *  1. replace "of the" with comma (",")
   *  2. inversion
   * </pre>
   * TBD: should be updated for other relevant prepositions.
   *
   * @param inputtext input text
   * @return string with preposition "of the" removed and the term inverted.
   */
  public static String transformPreposition(String inputtext) {
    if (inputtext.indexOf(" of the") > 0) {
      return NormalizedStringCache.normalizeString(inputtext.replaceAll(" of the", ","));
    } 
    return inputtext;
  }

  public class SpanEntityMapAndTokenLength {
    Map<String,Entity> spanEntityMap;
    int length;
    public SpanEntityMapAndTokenLength(    Map<String,Entity> spanEntityMap, int length) {
      this.spanEntityMap = spanEntityMap;
      this.length = length;
    }
    public Map<String,Entity> getSpanEntityMap() {
      return this.spanEntityMap;
    }
    public int getLength() {
      return this.length;
    }
    public List<Entity> getEntityList() {
      return new ArrayList<Entity>(this.spanEntityMap.values());
    }
  }


  public class SpanInfo {
    int start;
    int length;
    int getStart()  { return this.start; }
    int getLength() { return this.length; }
  }

  public void addEvSetToSpanMap(Map<String,Entity> spanMap, Set<Ev> evSet, 
				String docid, String matchedText, 
				int offset, int length) {
    String span = offset + ":" + length;
    if (spanMap.containsKey(span)) {
      Entity entity = spanMap.get(span);
      Set<String> currentCuiSet = new HashSet<String>();
      for (Ev currentEv: entity.getEvSet()) {
	currentCuiSet.add(currentEv.getConceptInfo().getCUI());
      }
      for (Ev newEv: evSet) {
	if (! currentCuiSet.contains(newEv.getConceptInfo().getCUI())) {
	  entity.addEv(newEv);
	}
      }
    } else {
      Entity entity = new Entity(docid, matchedText, offset, length, 0.0, evSet);
      spanMap.put(span, entity);
    }
  }

  boolean isCuiInSemanticTypeRestrictSet(String cui, Set<String> semanticTypeRestrictSet)
  {
    if (semanticTypeRestrictSet.isEmpty() || semanticTypeRestrictSet.contains("all"))
      return true;
    try {
      boolean inSet = false;
      for (String semtype: getSemanticTypeSet(cui)) {
	inSet = inSet || semanticTypeRestrictSet.contains(semtype);
      }
      return inSet;
    } catch (FileNotFoundException fnfe) {
      return false;
    } catch (IOException ioe) {
      return false;
    }
  }


  boolean isCuiInSourceRestrictSet(String cui, Set<String> sourceRestrictSet)
  {
    if (sourceRestrictSet.isEmpty() || sourceRestrictSet.contains("all"))
      return true;
    try {
      boolean inSet = false;
      for (String semtype: getSourceSet(cui)) {
	inSet = inSet || sourceRestrictSet.contains(semtype);
      }
      return inSet;
    } catch (FileNotFoundException fnfe) {
      return false;
    } catch (IOException ioe) {
      return false;
    }
  }

  public static boolean isLikelyMatch(String term, String normTerm, String docStr) {
    if (term.length() < 5) {
      return term.equals(docStr);
    } else {
      return normTerm.equals(NormalizedStringCache.normalizeString(docStr));
    }
  }

  /** 
   * Create sequence of sublists of tokenlist always starting from
   * the head each sublist smaller than the previous.
   * @param listOfTokenLists list of tokenlists each a subset of original tokenlist.
   * @param tokenList original tokenlist.
   */
  public static void applyHeadSubTokenListsOpt
    (List<List<BioCAnnotation>> listOfTokenLists,
     List<BioCAnnotation> tokenList) {
    for (int i = tokenList.size(); i > 0; i--) { 
      List<BioCAnnotation> tokenSubList = tokenList.subList(0, i);
      listOfTokenLists.add(tokenSubList);
    }
  }
  
  /**
   * Generate token sublists from original list using .
   * @param tokenList original tokenlist.
   * @return list of lists of BioC annotations
   */
  public static List<List<BioCAnnotation>> createSubListsOpt(List<BioCAnnotation> tokenList) {
    List<List<BioCAnnotation>> listOfTokenLists = new ArrayList<List<BioCAnnotation>>();
    for (int i=0; i<tokenList.size(); i++) {
      applyHeadSubTokenListsOpt
	(listOfTokenLists, tokenList.subList(i,tokenList.size()));
    }
    return listOfTokenLists;
  }

  
  /** 
   * Process sentence containing token and part-of-speech (postag)
   * annotations finding entities. Return set of entities.
   *
   * @param docid document id of parent BioCDocument of sentence
   * @param tokenizedSentence with token and part-of-speech annotations.
   * @return set of entities corresponding to sentence.
   */
  public Set<Entity> listEntities(String docid, BioCSentence tokenizedSentence)
  {
    try {
      logger.debug("listEntities");
      String normTerm ="";
      int longestMatchedTokenLength = 0;
      // span -> entity list map
      Map<String,Entity> spanMap = new HashMap<String,Entity>();
      List<BioCAnnotation> tokenAnnotationList =
	BioCUtilities.keepTokenAnnotations(tokenizedSentence.getAnnotations());
      List<List<BioCAnnotation>> listOfTokenSubLists =
	createSubListsOpt(tokenAnnotationList);
      for (List<BioCAnnotation> tokenSubList: listOfTokenSubLists) {
	List<String> tokenTextSubList = new ArrayList<String>();
	for (BioCAnnotation token: tokenSubList) {
	  tokenTextSubList.add(token.getText());
	}
	BioCAnnotation firstToken = tokenSubList.get(0);
	BioCAnnotation lastToken = tokenSubList.get(tokenSubList.size() - 1);
	if ((! firstToken.getText().toLowerCase().equals("other")) &&
	    this.allowedPartOfSpeechSet.contains(firstToken.getInfon("postag"))) {
	  int termLength = (tokenSubList.size() > 1) ?
	    (lastToken.getLocations().get(0).getOffset() +
	     lastToken.getText().length()) - firstToken.getLocations().get(0).getOffset() : 
	    firstToken.getText().length();
	  String originalTerm = StringUtils.join(tokenTextSubList, "");
	  if ((originalTerm.length() > 2) &&
	      (CharUtils.isAlphaNumeric(originalTerm.charAt(originalTerm.length() - 1)))) {
	    String term = originalTerm;
	    String query = term;
	    normTerm = NormalizedStringCache.normalizeString(term);
	    int offset = tokenSubList.get(0).getLocations().get(0).getOffset();
	    if (CharUtils.isAlpha(term.charAt(0))) {
	      Set<Ev> evSet = new HashSet<Ev>();
	      Integer tokenListLength = new Integer(tokenSubList.size());
	      if (termConceptCache.containsKey(normTerm)) {
		for (ConceptInfo concept: termConceptCache.get(normTerm)) {
		  String cui = concept.getCUI();
		  Ev ev = new Ev(concept,
				 originalTerm,
				 normTerm,
				 tokenSubList.get(0).getLocations().get(0).getOffset(),
				 termLength,
				 0.0,
				 tokenSubList.get(0).getInfon("postag"));
		  if (! evSet.contains(ev)) {
		    logger.debug("add ev: " + ev);
		    evSet.add(ev);
		  }
		}
	      } else {
		// if not in cache then lookup term 
		for (String doc: this.mmIndexes.cuiSourceInfoIndex.lookup(normTerm, 3)) {
		  String[] fields = doc.split("\\|");
		  String cui = fields[0];
		  String docStr = fields[3];
		
		  // If term is not in excluded term list and term or
		  // normalized form of term matches lookup string or
		  // normalized form of lookup string then get
		  // information about lookup string.
		  if ((! excludedTerms.isExcluded(cui,normTerm)) && isLikelyMatch(term,normTerm,docStr)) {
		    ConceptInfo concept = new ConceptInfo(cui, 
							  this.findPreferredName(cui),
							  this.getSourceSet(cui),
							  this.getSemanticTypeSet(cui));
		    this.cacheConcept(normTerm, concept);
		    cui = concept.getCUI();
		    Ev ev = new Ev(concept,
				   originalTerm,
				   docStr,
				   offset,
				   termLength,
				   0.0,
				   tokenSubList.get(0).getInfon("postag"));
		    if (! evSet.contains(ev)) {
		      logger.debug("add ev: " + ev);
		      evSet.add(ev);
		    } 
		  } /*if term equals doc string */
		} /* for doc in documentList */
	      } /* if term in concept cache */
	      if (evSet.size() > 0) {
		this.addEvSetToSpanMap(spanMap, evSet, 
				       docid,
				       originalTerm,
				       offset, termLength);
		longestMatchedTokenLength = Math.max(longestMatchedTokenLength,tokenTextSubList.size());
	      }
	    } /* if term alphabetic */
	  } /* if term length > 0 */
	} /* first token has allowed partOfSpeech */
      } /* for token-sublist in list-of-token-sublists */

      SpanEntityMapAndTokenLength spanEntityMapAndTokenLength =
	new SpanEntityMapAndTokenLength(spanMap, longestMatchedTokenLength);
      Set<Entity> entitySet = new HashSet<Entity>();
      for (Entity entity: spanEntityMapAndTokenLength.getEntityList()) {
	if (entity.getEvList().size() > 0) {
	  entitySet.add(entity);
	}
      }
      return entitySet;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static Set<Entity> removeSubsumingEntities(Set<Entity> entitySet) {
    Map <Integer,Entity> startMap = new HashMap<Integer,Entity>();
    logger.debug("-input entity set spans-");
    for (Entity entity: entitySet) {
      Integer key = new Integer(entity.getStart());
      if (startMap.containsKey(key)) {
	if (startMap.get(key).getLength() < entity.getLength()) {
	  // replace entity with larger span
	  startMap.put(key,entity);
	}
      } else {
	startMap.put(key, entity);
      }
    }
    logger.debug("-shorter entities with same start have been removed-");
    Map <Integer,Entity> endMap = new HashMap<Integer,Entity>();
    for (Entity entity: startMap.values()) {
      Integer key = new Integer(entity.getStart() + entity.getLength());
      if (endMap.containsKey(key)) {
	if (endMap.get(key).getStart() > entity.getStart()) {
	  // replace entity with larger span
	  endMap.put(key,entity);
	}
      } else {
	endMap.put(key, entity);
      }
    }

    logger.debug("-final entity set spans-");
    Set<Entity> newEntitySet = new HashSet<Entity>();
    for (Entity entity: endMap.values()) {
      newEntitySet.add(entity);
    }
    return newEntitySet;
  }


  public void filterEntityEvListBySemanticType(Entity entity, Set<String> semanticTypeRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      if (isCuiInSemanticTypeRestrictSet(cui, semanticTypeRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }

  public void filterEntityEvListBySource(Entity entity, Set<String> sourceRestrictSet)
  {
    Set<Ev> newEvSet = new HashSet<Ev>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      if (isCuiInSourceRestrictSet(cui, sourceRestrictSet)) {
	newEvSet.add(ev);
      }
    }
    entity.setEvSet(newEvSet);
  }

  public Set<Entity> filterEntitiesBySemanticTypeAndSource(Set<Entity> entitySet1,
							   Set<String> semTypeRestrictSet,
							   Set<String> sourceRestrictSet) {
    Set<Entity> entitySet = new HashSet<Entity>();
    for (Entity entity: entitySet1) {
      filterEntityEvListBySemanticType(entity, semTypeRestrictSet);
      filterEntityEvListBySource(entity, sourceRestrictSet);
      if (entity.getEvList().size() > 0) {
	entitySet.add(entity);
      }
    }
    return entitySet;
  }

  /** 
   * Process sentence containing token and part-of-speech (postag)
   * annotations finding entities. Return sentence with entity and
   * concept annotations with linking relations.  Token and
   * part-of-speech annotations are removed.
   *
   * @param docid document id of parent BioCDocument of sentence
   * @param tokenizedSentence with token and part-of-speech annotations.
   * @return BioCSentence with entity and concept annotations with linking relations.
   */
  public BioCSentence findLongestMatches(String docid, BioCSentence tokenizedSentence)
  {
    Set<Entity> entitySet = listEntities(docid, tokenizedSentence);
    BioCSentence annotatedSentence = sentenceEntityAnnotator.annotateSentence(tokenizedSentence, entitySet);
    return annotatedSentence;
  }

  public BioCSentence findLongestMatches(String docid,
					 BioCSentence sentence,
					 Set<String> semTypeRestrictSet,
					 Set<String> sourceRestrictSet)
  {
    Set<Entity> entitySet0 = listEntities(docid, sentence);
    Set<Entity> entitySet1 = removeSubsumingEntities(entitySet0);
    Set<Entity> entitySet = filterEntitiesBySemanticTypeAndSource(entitySet0, semTypeRestrictSet,sourceRestrictSet);
    BioCSentence annotatedSentence = sentenceEntityAnnotator.annotateSentence(sentence, entitySet);
    return annotatedSentence;
  }
}













