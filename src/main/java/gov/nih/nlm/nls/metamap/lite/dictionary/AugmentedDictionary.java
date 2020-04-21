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
  MMLDictionaryLookup persistantLookup;
  Map<String,List<String>> strCuiListMap;
  final static Set<String> customSourceSet = new HashSet<String>();
  final static Set<String> customSemanticTypeSet = new HashSet<String>();
  static {
    customSourceSet.add("userdefined");
    customSemanticTypeSet.add("unknown");
  }
  
  public AugmentedDictionary( MMLDictionaryLookup persistantLookup,
			      Map<String,List<String>> strCuiListMap) {
    this.persistantLookup = persistantLookup;
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
	ConceptInfo conceptInfo = new ConceptInfo(cui,
						  originalTerm,
						  normTerm,
						  customSourceSet, // custom vocabulary
						  customSemanticTypeSet);
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
	// term with this cui is not in persistant dictionary then create a new entry
	if (! cuiConceptInfoMap.containsKey(cui)) {
	  ConceptInfo conceptInfo = new ConceptInfo(cui,
						    originalTerm,
						    normTerm,
						    customSourceSet, // custom vocabulary
						    customSemanticTypeSet);
	  ciSet.add(conceptInfo);
	}
      }
    }
    return ciSet;
  }

  // DictionaryLookup signature (what is T)
  public TermInfo lookup(String term) {
    // lookup term in persistant dictionary first
    TermInfo persistantTermInfo = (TermInfo)this.persistantLookup.lookup(term);
    TermInfo termInfo;
    if (((Set<ConceptInfo>)persistantTermInfo.getDictionaryInfo()).isEmpty()) {
      Set<ConceptInfo> ciSet = createCustomConceptInfoSet(term,
							  persistantTermInfo.getOriginalTerm(), 
							  persistantTermInfo.getNormTerm());
      termInfo = new TermInfoImpl(persistantTermInfo.getOriginalTerm(), 
				  persistantTermInfo.getNormTerm(),
				  ciSet,
				  persistantTermInfo.getTokenList());
    } else {
      Map<String,ConceptInfo> cuiConceptInfoMap = new HashMap<String,ConceptInfo>();
      for (ConceptInfo ci: (Set<ConceptInfo>)persistantTermInfo.getDictionaryInfo()) {
	cuiConceptInfoMap.put(ci.getCUI(), ci);
      }
      Set<ConceptInfo> ciSet =
	createCustomConceptInfoSet(term,
				   persistantTermInfo.getOriginalTerm(), 
				   persistantTermInfo.getNormTerm(),
				   cuiConceptInfoMap);
      if (! ciSet.isEmpty()) {
	Set<ConceptInfo> pciSet =
	  (Set<ConceptInfo>)persistantTermInfo.getDictionaryInfo();
	pciSet.addAll(ciSet);
	termInfo = new TermInfoImpl(persistantTermInfo.getOriginalTerm(), 
				    persistantTermInfo.getNormTerm(),
				    ciSet,
				    persistantTermInfo.getTokenList());
	  
      } else {
	termInfo = persistantTermInfo;
      }
    }
    return termInfo;
  }

  // VariantLookup signature
  public int lookupVariant(String term, String word) {
    return this.persistantLookup.lookupVariant(term, word);
  }
  public int lookupVariant(String term) {
    return this.persistantLookup.lookupVariant(term);
  }

  // MMLInstantiate signature
  public void init(Properties properties) {
    this.persistantLookup.init(properties);
  }

  // MMLValidate signature
  public boolean verifyImplementation(String directoryPath) {
    return this.persistantLookup.verifyImplementation(directoryPath);
  }

  // PreferredNameLookup signature
  public String getPreferredName(String cui) {
    return this.persistantLookup.getPreferredName(cui);
  }

  // SemanticTypeLookup signature
  public Set<String> getSemanticTypeSet(String cui) {
    return this.persistantLookup.getSemanticTypeSet(cui);
  }

  // SourceLookup signature
  public Set<String> getSourceSet(String cui) {
    return this.persistantLookup.getSourceSet(cui);
  }
}
