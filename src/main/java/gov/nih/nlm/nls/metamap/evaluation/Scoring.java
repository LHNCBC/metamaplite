package gov.nih.nlm.nls.metamap.evaluation;

import java.util.List;
import gov.nih.nlm.nls.metamap.lite.VariantLookup;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

/**
 * Describe class Scoring here.
 *
 *
 * Created: Wed Mar 22 14:47:30 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Scoring {

  /**
   * Creates a new <code>Scoring</code> instance.
   *
   */
  public Scoring() {

  }


  /**
   * Method setCentrality 
   * 
   * The centrality value is simply 1 if the string involves the head of the
   * phrase and 0 otherwise.  This routine inspects the "isHead" and sets
   * the centrality accordingly.
   * @param isHead does string involve head of phrase.
   * @return 1 if the string involves the head of the
   */
 int computeCentrality(boolean isHead) {
   // The system currently does not use a parser of any kind so doesn't
   // have a concept of phrases.
   return isHead ? 1 : 0;
  }

  /**
   * The variation value estimates how much the variants in the Metathesaurus
   * string differ from the corresponding words in the phrase.  It is computed
   * by first determining the variation distance for each variant in the 
   * Metathesaurus string.  This distance is the sum of the distance values
   * for each step taken during variant generation.  V=4/(D+4)  The final
   * variation value for the candidate is the average fo the values for each
   * of the variants.
   * @param term target term
   * @param mstring metathesaurus string
   * @param tokenList tokenlist of target term
   * @param lookupInstance 
   */
  double computeVariation(String term,
			  String mstring,
			  List<ERToken> tokenList,
			  VariantLookup lookupInstance) {
    int varlevel = lookupInstance.lookupVariant(term, mstring);
    for (ERToken token: tokenList) {
      
    }
    
    return 0.0;
  }

  /**
   * The coverage value indicates how much of the phrase string and the 
   * Metathesaurus string are involved in the match.  In order to compute the 
   * value, the number of words participating in the match is computed for both 
   * the phrase and the Metathesaurus string.  These numbers are called the 
   * phrase span and Metathesaurus span, respectively.  NOTE: Gaps are ignored in
   * this calculation.  The coverage value for the phrase is the phrase span
   * divided by the length of the phrase.  Similarly, the coverage value for the
   * Metathesaurus string is the Metathesaurus span divided by the length of the
   * string. The final coverage value is the weighted average of the values for
   * the phrase and the Metathesaurus string where the Metathesaurus string is
   * given twice the weight as the phrase.
   *
   * @param sizeOfPhrase     Number of words in parent phrase.
   * @param numPhraseSpan    position of phrase span
   * @param numCandidateSpan position of candidate span
   * @param numWords         Number of words in term
   */ 
  double computeCoverage(int sizeOfPhrase,
			 int numPhraseSpan,
			 int numCandidateSpan,
			 int numWords) {
    return 
      (((numPhraseSpan * 1.0) / (sizeOfPhrase * 1.0)) + 
       (2 * ((numCandidateSpan * 1.0) / (numWords * 1.0)))) / 3.0;
  }

  double computeInvolvement(int sizeOfPhrase,
			    int numPhraseSpan,
			    int numCandidateSpan,
			    int numWords) {
    return 
      ((numPhraseSpan * 1.0) / (sizeOfPhrase * 1.0)) + 
      ((numCandidateSpan * 1.0) / (numWords * 1.0)) / 2.0;
  }

  /**
   * The cohesiveness value is similar to the coverage value but emphasizes the
   * importance of connected components.  A connected component is a maximal
   * sequence of contiguous words participating in the match.  The connected
   * components for both the phrase and the Metathesaurus string are computed.
   * The cohesiveness value for the phrase is the sum of the squares of the
   * connected phrase component sizes divided by the square of the length of the
   * string.  A similar cohesiveness value is computed for the Metathesaurus
   * string.  The final cohesiveness value is the weighted average of the phrase
   * and Metathesaurus string values where the Metathesaurus string is again
   * given twice the weight as the phrase.
   *
   * taken from MMTx 
   * @param sizeOfPhrase   Number of words in parent phrase.
   * @param numWords         Number of words in term
   */
  double conputeCohesiveness(int sizeOfPhrase,
			 int numWords) {



    
    return 0.0;
  }


}
