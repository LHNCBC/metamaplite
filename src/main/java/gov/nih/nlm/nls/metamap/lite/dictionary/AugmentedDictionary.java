package gov.nih.nlm.nls.metamap.lite.dictionary;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.lite.TermInfoImpl;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;

/**
 * Describe class AugmentedDictionary here.
 *
 *
 * Created: Fri Apr 17 18:15:08 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class AugmentedDictionary
  implements MMLDictionaryLookup<TermInfo>
{
  MMLDictionaryLookup persistentLookup;
  Map<String,List<String>> strCuiListMap;
  final static Set<String> customSourceSet = new HashSet<String>();
  final static Set<String> customSemanticTypeSet = new HashSet<String>();
  static {
    customSourceSet.add("USERDEFINED");
    customSemanticTypeSet.add("unknown");
  }
  
  public AugmentedDictionary( MMLDictionaryLookup persistentLookup,
			      Map<String,List<String>> strCuiListMap) {
    this.persistentLookup = persistentLookup;
    this.strCuiListMap = strCuiListMap;
  }

  public static Set<String> getCustomSemanticTypeSet() { return customSemanticTypeSet; }
  public static Set<String> getCustomSourceSet() { return customSourceSet; }

  public Set<ConceptInfo> createCustomConceptInfoSet(String term,
						     String originalTerm,
						     String normTerm) {
    Set<ConceptInfo> ciSet = new HashSet<ConceptInfo>();
    if (this.strCuiListMap.containsKey(term)) {
      for (String cui: strCuiListMap.get(term)) {
	String prefName = this.persistentLookup.getPreferredName(cui);
	if (prefName == null) {
	  prefName = originalTerm;
	}
	ConceptInfo conceptInfo = new ConceptInfo(cui,
						  prefName,
						  normTerm,
						  this.getSourceSet(cui), // custom vocabulary
						  this.getSemanticTypeSet(cui));
	ciSet.add(conceptInfo);
      }
    }
    return ciSet;
  }

  public Set<ConceptInfo> createCustomConceptInfoSet(String term,
						     String originalTerm,
						     String normTerm,
						     Map<String,ConceptInfo> cuiConceptInfoMap) {
    Set<ConceptInfo> ciSet = new HashSet<ConceptInfo>();
    if (this.strCuiListMap.containsKey(term)) {
      for (String cui: strCuiListMap.get(term)) {
	// term with this cui is not in persistent dictionary then create a new entry
	if (! cuiConceptInfoMap.containsKey(cui)) {
	  String prefName = this.persistentLookup.getPreferredName(cui);
	  if (prefName == null) {
	    prefName = originalTerm;
	  }
	  ConceptInfo conceptInfo = new ConceptInfo(cui,
						    prefName,
						    normTerm,
						    this.getSourceSet(cui), // custom vocabulary
						    this.getSemanticTypeSet(cui));
	  ciSet.add(conceptInfo);
	}
      }
    }
    return ciSet;
  }

  // DictionaryLookup signature (what is T)
  public TermInfo lookup(String term) {
    // lookup term in persistent dictionary first
    TermInfo persistentTermInfo = (TermInfo)this.persistentLookup.lookup(term);
    TermInfo termInfo;
    if (((Set<ConceptInfo>)persistentTermInfo.getDictionaryInfo()).isEmpty()) {
      Set<ConceptInfo> ciSet = createCustomConceptInfoSet(term,
							  persistentTermInfo.getOriginalTerm(), 
							  persistentTermInfo.getNormTerm());
      termInfo = new TermInfoImpl(persistentTermInfo.getOriginalTerm(), 
				  persistentTermInfo.getNormTerm(),
				  ciSet,
				  persistentTermInfo.getTokenList());
    } else {
      Map<String,ConceptInfo> cuiConceptInfoMap = new HashMap<String,ConceptInfo>();
      for (ConceptInfo ci: (Set<ConceptInfo>)persistentTermInfo.getDictionaryInfo()) {
	cuiConceptInfoMap.put(ci.getCUI(), ci);
      }
      Set<ConceptInfo> ciSet =
	createCustomConceptInfoSet(term,
				   persistentTermInfo.getOriginalTerm(), 
				   persistentTermInfo.getNormTerm(),
				   cuiConceptInfoMap);
      if (! ciSet.isEmpty()) {
	Set<ConceptInfo> pciSet =
	  (Set<ConceptInfo>)persistentTermInfo.getDictionaryInfo();
	pciSet.addAll(ciSet);
	termInfo = new TermInfoImpl(persistentTermInfo.getOriginalTerm(), 
				    persistentTermInfo.getNormTerm(),
				    ciSet,
				    persistentTermInfo.getTokenList());
	  
      } else {
	termInfo = persistentTermInfo;
      }
    }
    return termInfo;
  }

  // VariantLookup signature
  public int lookupVariant(String term, String word) {
    return this.persistentLookup.lookupVariant(term, word);
  }
  public int lookupVariant(String term) {
    return this.persistentLookup.lookupVariant(term);
  }

  // MMLInstantiate signature
  public void init(Properties properties) {
    this.persistentLookup.init(properties);
  }

  // MMLValidate signature
  public boolean verifyImplementation(String directoryPath) {
    return this.persistentLookup.verifyImplementation(directoryPath);
  }

  // PreferredNameLookup signature
  public String getPreferredName(String cui) {
    return this.persistentLookup.getPreferredName(cui);
  }

  // SemanticTypeLookup signature
  public Set<String> getSemanticTypeSet(String cui) {
    Set<String> semTypeSet = this.persistentLookup.getSemanticTypeSet(cui);
    if (semTypeSet.isEmpty()) {
      return customSemanticTypeSet;
    } else {
      return semTypeSet;
    }
  }

  // SourceLookup signature
  public Set<String> getSourceSet(String cui) {
    Set<String> sourceSet = this.persistentLookup.getSourceSet(cui);
    if (sourceSet.isEmpty()) {
      return customSourceSet;
    } else {
      sourceSet.addAll(customSourceSet);
      return sourceSet;
    }
  }
}
