package gov.nih.nlm.nls.metamap.lite;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

/**
 * Describe class VariantLookupIVF here.
 *
 *
 * Created: Fri Mar 24 16:37:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class VariantLookupIVF implements VariantLookup {
  public MetaMapIvfIndexes mmIndexes;

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   */
  public VariantLookupIVF(MetaMapIvfIndexes mmIndexes) {
    this.mmIndexes = mmIndexes;
  }


  /**
   * Get variant records for term
   * @param term user supplied term
   * @param list of variant records with matching term
   */
  public List<String[]> getVariantsForTerm(String term)
    throws IOException {
    List<String[]> variantList = new ArrayList<String[]>();
    if (this.mmIndexes.varsIndex != null) {
      List<String> hitList = this.mmIndexes.varsIndex.lookup(term, 1);
      for (String hit: hitList) {
	String[] fields = hit.split("\\|");
	variantList.add(fields);
      }
    }
    return variantList;
  }

  /**
   * Get variant records for word
   * @param word user supplied word
   * @param list of variant records with matching word
   */
  public List<String[]> getVariantsForWord(String word)
    throws IOException {
    List<String[]> variantList = new ArrayList<String[]>();
    if (this.mmIndexes.varsIndex != null) {
      List<String> hitList = this.mmIndexes.varsIndex.lookup(word, 2);
      for (String hit: hitList) {
	String[] fields = hit.split("\\|");
	variantList.add(fields);
      }
    }
    return variantList;
  }

  public int lookupVariant(String term, String word)
  {
    /* lookup term variants */
    /* if word is in variant list return varlevel */
    int variance = 99;
    try {
      for (String[] varFields: this.getVariantsForTerm(term)) {
	if (varFields[2].equals(word)) {
	  variance = Integer.parseInt(varFields[4]); // use varlevel field
	}
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return variance;
  }
}

