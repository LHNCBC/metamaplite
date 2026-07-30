package gov.nih.nlm.nls.metamap.lite.dictionary;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.lite.NormalizedStringCache;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.lite.UserDefinedAcronym;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookup;

/**
 * Describe class UDAPlusDictionaryLookup here.
 *
 *
 * Created: Mon Jun 10 13:31:38 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class UDAPlusDictionaryLookup implements MMLDictionaryLookup<TermInfo> {

  MMLDictionaryLookup<TermInfo> dictionaryLookup;
  /** short form to long form user defined acronym map */
  Map<String,UserDefinedAcronym<TermInfo>> udaMap =
    new HashMap<String,UserDefinedAcronym<TermInfo>>();

  Map<String,String> uaMap = new HashMap<String,String>();

  /**
   * Creates a new <code>UDAPlusDictionaryLookup</code> instance.
   */
  public UDAPlusDictionaryLookup() {
  }

  /**
   * Creates a new <code>UDAPlusDictionaryLookup</code> instance.
   * @param udaFilename user defained acronyms file
   * @param dictionaryLookup dictionary based lookup class instance
   */
  public UDAPlusDictionaryLookup(String udaFilename, MMLDictionaryLookup<TermInfo> dictionaryLookup) {
    this.dictionaryLookup = dictionaryLookup;
    this.udaMap = UserDefinedAcronym.loadUDAList(udaFilename, dictionaryLookup);
    this.uaMap = UserDefinedAcronym.udasToUA(this.udaMap);
  }

  public String getPreferredName(String cui) { return this.dictionaryLookup.getPreferredName(cui); }
  // interface VariantLookup
  public int lookupVariant(String term, String word) {
    return this.dictionaryLookup.lookupVariant(term, word);
  }
  public int lookupVariant(String term) {
    return this.dictionaryLookup.lookupVariant(term);
  }
  // Set<String> getSemanticTypeSet(String) in SemanticTypeLookup
  public Set<String> getSemanticTypeSet(String cui) {
    return this.dictionaryLookup.getSemanticTypeSet(cui);
  }
  // Set<String> getSourceSet(String cui)in SourceLookup 
  public Set<String> getSourceSet(String cui) {
    return this.dictionaryLookup.getSourceSet(cui);
  }

  /**
   * Lookup term in dictionary
   *
   * @param term original term
   * @return a <code>TermInfo</code> value associated with input term.
   */
  public TermInfo lookup(String term) {
    TermInfo terminfo = this.dictionaryLookup.lookup(term);
    return terminfo;
  }
  
  public boolean verifyImplementation(String directoryPath) {
    return this.dictionaryLookup.verifyImplementation(directoryPath);
  }

  public void init(Properties properties) {
    // place properties controlled initialization here
    
  }
}
