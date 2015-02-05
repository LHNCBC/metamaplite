
//
package gov.nih.nlm.nls.metamap.lite.metamap;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public class MetaMapEvaluation {

  public MetaMapIndexes mmIndexes;
  
  public MetaMapEvaluation(MetaMapIndexes indexes) {
    this.mmIndexes = indexes;
  }

  /** only possible to calculate if you have parsed the sentence into phrases. */
  public double calculateCentrality() {
    return 1.0;
  }

  public int getVariantDistance(String word, String variant)
    throws IOException, ParseException
  {
    List<Document> hitList = this.mmIndexes.varsIndex.lookup(variant,
							     this.mmIndexes.varQueryParser,
							     100);
    int distance = 4;		// maximum distance: doesn't match at all.
    for (Document hit: hitList) {
      if ((word.equals(hit.get("word"))) &&
	  (variant.equals(hit.get("var")))) {
	distance = Integer.parseInt(hit.get("dist"));
      }
    }
    return distance;
  }

  public double calculateVariation(String textstring, String word)
    throws FileNotFoundException, IOException, ParseException
  {
    return 4/(this.getVariantDistance(word, textstring) + 4);
  }

  /**
   * Currently:
   *   wl: word token length
   *   vl: variant token length
   *   ml: number of metawords (candidates?)
   *    C: coverage
   *
   *       C = ((wl / vl) + ml) / 3.0"
   */
  public double calculateCoverage(String[] tokenList, String word, int nMetaWords)
  {
    // System.out.println("word: " + word + ", " +
    //                    "tokenList: " + Tokenize.getTextFromTokenList(tokenList));
    String[] wordTokenList = Tokenize.mmTokenize(word, 2);
    return ((wordTokenList.length/tokenList.length) + nMetaWords)/3.0;
  }

  /**
   */
  public double calculateCohesiveness() {

    return 1.0;
  }

  /**
   * Score formula:
   *   score = 1000 * ((centrality + variation + 2*coverage + 2*cohesiveness)/6)
   * in this case centrality and cohesiveness are 1.0:
   *   score = 1000 * ((1.0 + variation + 2*coverage + 2.0)/6)
   */
  public double calculateScore(String textstring,
			       String preferredName,
			       String cui,
			       String[] inputTextTokenList,
			       Collection<Entity> candidateCollection)
    throws FileNotFoundException, IOException, ParseException
  {
    // System.out.println("textstring: " + textstring);
    String word = preferredName;
    return 1000 * ( 1.0 +
		    (this.calculateVariation(textstring, word) + 
		     (2 * calculateCoverage(inputTextTokenList, word, candidateCollection.size())) +
		     2.0)/6.0);
  }
}
