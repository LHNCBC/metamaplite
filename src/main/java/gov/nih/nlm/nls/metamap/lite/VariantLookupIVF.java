package gov.nih.nlm.nls.metamap.lite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private static final Logger logger = LogManager.getLogger(VariantLookupIVF.class);

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
      List<String> hitList = this.mmIndexes.varsIndex.lookup(term, 0);
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
    /* if word is in variant list return varlevel (column 4)*/
    int variance = 9;		// maximum variance (should this value be larger?)
    try {
      logger.debug("term: " + term);
      logger.debug("word: " + word);
      for (String[] varFields: this.getVariantsForTerm(term.toLowerCase())) {
	if ((varFields[2].toLowerCase().equals(word.toLowerCase())) ||
	    (varFields[2].toLowerCase().equals(term.toLowerCase()))) {
	  variance = Integer.parseInt(varFields[4]); // use varlevel field
	  logger.debug("*varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
	} else {
	  logger.debug(" varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
	}
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return variance;
  }
}

