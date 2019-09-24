package gov.nih.nlm.nls.metamap.lite.dictionary;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * MMLDictionaryLookupRegistry - Registry of MMLDictionaryLookup
 * implementations
 *
 * Created: Tue Jun 11 09:13:33 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MMLDictionaryLookupRegistry {

  /** Map of Dictionary Lookup Implementation by name */
  Map<String,MMLDictionaryLookup> implementationMap = new HashMap<String,MMLDictionaryLookup>();

  /** Map of Dictionary Lookup Implementation Descriptions by format name */
  Map<String,String> descriptionMap = new HashMap<String,String>();
  
  /**
   * Creates a new <code>MMLDictionaryLookupRegistry</code> instance.
   *
   */
  public MMLDictionaryLookupRegistry() {
    
  }

  public void put(String name, MMLDictionaryLookup instance) {
    this.implementationMap.put(name, instance);
  }

  public MMLDictionaryLookup get(String name) {
    return this.implementationMap.get(name);
  }
  
  public Map.Entry<String,MMLDictionaryLookup>
    determineImplementation(String directoryPath)
  {
    Map.Entry<String,MMLDictionaryLookup> implEntry = null;
    for (Map.Entry<String,MMLDictionaryLookup> entry: this.implementationMap.entrySet()) {
      if (entry.getValue().verifyImplementation(directoryPath)) {
	implEntry = entry;
      }
    }
    return implEntry;
  }
}
