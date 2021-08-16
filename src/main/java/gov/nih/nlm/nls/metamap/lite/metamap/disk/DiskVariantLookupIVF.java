package gov.nih.nlm.nls.metamap.lite.metamap.disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.FileNotFoundException;

import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfDiskIndexes;
import gov.nih.nlm.nls.metamap.lite.dictionary.VariantLookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe class DiskVariantLookupIVF here.
 *
 *
 * Created: Fri Mar 24 16:37:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class DiskVariantLookupIVF implements VariantLookup {
  private static final Logger logger = LoggerFactory.getLogger(DiskVariantLookupIVF.class);

  public MetaMapIvfDiskIndexes mmIndexes;

  /**
   * Creates a new <code>TermConceptInfoCache</code> instance.
   *
   * @param mmIndexes set of inverted file indexes
   */
  public DiskVariantLookupIVF(MetaMapIvfDiskIndexes mmIndexes) {
    this.mmIndexes = mmIndexes;
  }


  /**
   * Get variant records for term
   * @param term user supplied term
   * @return list of variant records with matching term
   * @throws IOException i/o exception
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
   * @return list of variant records with matching word
   * @throws IOException i/o exception
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

  public int lookupVariant(String term) {
     int variance = 9;		// maximum variance (should this value be larger?)
    try {
      logger.debug("term: " + term);
      for (String[] varFields: this.getVariantsForTerm(term.toLowerCase())) {
	if ((varFields[2].toLowerCase().equals(term.toLowerCase()))) {
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

