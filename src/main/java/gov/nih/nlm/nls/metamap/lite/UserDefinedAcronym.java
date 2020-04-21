package gov.nih.nlm.nls.metamap.lite;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.dictionary.DictionaryLookup;

/**
 * Describe class UserDefinedAcronyms here.
 *
 *
 * Created: Thu May 10 10:39:14 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class UserDefinedAcronym<T> {
  String shortForm;
  String longForm;
  T info;

  /**
   * Creates a new <code>UserDefinedAcronyms</code> instance.
   * @param shortForm abbrevation or acronym
   * @param longForm expanded form of abbrevation or acronym
   * @param info information associated with short form/long form pair (concept, etc.)
   */
  public UserDefinedAcronym(String shortForm, String longForm, T info) {
    this.shortForm = shortForm;
    this.longForm = longForm;
    this.info = info;

  }

  public String getShortForm() {
    return this.shortForm;
  }

  public String getLongForm() {
    return this.longForm;
  }

  public T getInfo() {
     return this.info;
  }
  
  public String toString() {
    return this.shortForm + "|" + this.longForm + "|" + this.info;
  }

  public static <T> Map<String,UserDefinedAcronym<T>> loadUDAList(String filename, DictionaryLookup<T> lookupClass) {
    try {
      Map<String,UserDefinedAcronym<T>> udaMap = new HashMap<String,UserDefinedAcronym<T>>();
      BufferedReader br = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = br.readLine()) != null) {
	String[] fields = line.split("\\|");
	if (fields.length > 1) {
	  T ti = lookupClass.lookup(fields[1].toLowerCase());
	  if (! ((Set<ConceptInfo>)((TermInfo)ti).getDictionaryInfo()).isEmpty()) {
	    udaMap.put
	      (fields[0],
	       new UserDefinedAcronym<T> (fields[0], fields[1], ti));
	  } else {
	    udaMap.put
	      (fields[0],
	       new UserDefinedAcronym<T> (fields[0], fields[1],
					  lookupClass.lookup
					  (NormalizedStringCache.normalizeString
					   (fields[1]))));
	  }
	}
      }
      br.close();
      return udaMap;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /** Convert uda map to ua map 
   *@param udaMap uda Map with TermInfo
   * @return uda map just mapping short forms to long forms (no TermInfo)
   */
  public static Map<String,String> udasToUA(Map<String,UserDefinedAcronym<TermInfo>> udaMap) {
    Map<String,String> uaMap = new HashMap<String,String>(); // short form -> long form

    for (Map.Entry<String,UserDefinedAcronym<TermInfo>> entry: udaMap.entrySet()) {
      uaMap.put(entry.getKey(), entry.getValue().getLongForm());
    }
    return uaMap;
  }

  public static Set<Entity> generateEntities(String docid,
					     Map<String,UserDefinedAcronym<TermInfo>> udaMap) {
    Set<Entity> entitySet = new HashSet<Entity>();
    for (Map.Entry<String,UserDefinedAcronym<TermInfo>> entry: udaMap.entrySet()) {
      UserDefinedAcronym<TermInfo> uda = entry.getValue();
      TermInfo termInfo = uda.getInfo();
      Set<ConceptInfo> conceptInfoSet = (Set<ConceptInfo>)termInfo.getDictionaryInfo();
      Set<Ev> evSet = new HashSet<Ev>();
      for (ConceptInfo conceptInfo: conceptInfoSet) {
	Ev ev = new Ev(conceptInfo, uda.getShortForm(), conceptInfo.getConceptString(), 0, 0, 100.0, "");
      }
      entitySet.add(new Entity("UDA", docid, uda.getShortForm(), 0, 0, 100.0, evSet));
    }
    return entitySet;
  }

  public static Set<Entity> generateEntities(String docid,
					     Map<String,UserDefinedAcronym<TermInfo>> udaMap,
					     List<ERToken> tokenList)
  {
    Set<Entity> entitySet = new HashSet<Entity>();
    for (ERToken token: tokenList) {
      if (udaMap.containsKey(token.getText())) {
	UserDefinedAcronym<TermInfo> uda = udaMap.get(token.getText());
	TermInfo termInfo = uda.getInfo();
	Set<ConceptInfo> conceptInfoSet = (Set<ConceptInfo>)termInfo.getDictionaryInfo();
	Set<Ev> evSet = new HashSet<Ev>();
	for (ConceptInfo conceptInfo: conceptInfoSet) {
	  Ev ev = new Ev(conceptInfo, 
			 uda.getShortForm(), 
			 conceptInfo.getConceptString(), 
			 token.getOffset(), 
			 token.getText().length(), 
			 100.0,
			 "");
	  evSet.add(ev);
	}
	entitySet.add(new Entity("UDA",
				 docid, 
				 uda.getShortForm(),
				 token.getOffset(), 
				 token.getText().length(), 
				 100.0,
				 evSet));
      }
    }
    return entitySet;
  }
}
