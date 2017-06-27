package gov.nih.nlm.nls.metamap.mmi;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.metamap.prefix.Tokenize;

/**
 * MMI Ranking functions
 * <p>
 * Created: Wed Mar  8 08:56:56 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Ranking {

  // processing_parameters
  static final double nc  =   0; // character normalization index
  static final double nf  =  -5; // frequency normalization index
  static final double nm  =   0; // MeSH normalization index
  static final double nmm = -10; // MetaMap normalization index
  static final double nw  =   0; // word normalization index
  static final double nz  =   0; // final normalization index
  static final double wc  =   0; // character count weight
  static final double wd  =   1; // default tree depth
  static final double wm  =  14; // MeSH tree depth weight
  static final double wmm =   1; // MetaMap weight
  static final double ww  =   0; // word count weight

  
  public static int computeTreeDepth(String treecode) {
    String[] fields = treecode.split("\\.");
    return fields.length;
  }

  public static List<Integer> computeTreeDepths(List<String> treecodeList) {
    List<Integer> depths = new ArrayList<Integer>();
    for (String treecode: treecodeList) {
      depths.add(computeTreeDepth(treecode));
    }
    return depths;
  }

  public static int sumIntList(List<Integer> valueList) {
    int sum = 0;
    for (Integer value: valueList) {
      sum = value.intValue() + sum;
    }
    return sum;
  }
  public static double sumDoubleList(List<Double> valueList) {
    double sum = 0.0;
    for (Double value: valueList) {
      sum = value.doubleValue() + sum;
    }
    return sum;
  }

  public static double computeTreeDepthSpecificity(List<String> treecodeList, double wd) {
    return Math.max(wd, sumIntList(computeTreeDepths(treecodeList)));
  }

  public static double setValue1(double value) {
    if (value > 1.) return 1.0;
    else if (value < 0.0) return 0.0;
    else return value;
  }

  public static double normalizeValue(double n, double value) {
    if (n == 0.0) {
      return value;
    } else if (n > 0.0) {
      double value1 = setValue1(value);
      double en = Math.exp(n);
      double a = en + 1;
      double b = en - 1;
      double c = (n * -1) * value1;
      double ec = Math.exp(c);
      return (a / b) * ((1 - ec) / (1 + ec));
    } else if (n < 0.0) {
      double value1 = setValue1(value);
      double m = -n;
      double em = Math.exp(m);
      double a = em + 1;
      double b = em - 1;
      double c = (a + (b * value1)) / (a - (b * value1));
      double lc = Math.log(c);
      return (lc / m);
    }
    return value;
  }

  public static String[] tokenizeTextMore(String text) {
    return Tokenize.mmTokenize(text, 2);
  }

  public static int MMI_TREE_DEPTH_SPECIFICITY_DIVISOR = 8;
  public static int MMI_WORD_SPECIFICITY_DIVISOR = 26;
  public static int MMI_CHARACTER_SPECIFICITY_DIVISOR = 102;
  
  public static List<Double> computeSpecificities(String concept, double mmValue,
					   List<String> treecodes, 
					   double wd, double nmm, double nm,
					   double nw, double nc) {
    double mmSpec = mmValue / 1000;
    double nmmSpec = normalizeValue(nmm, mmSpec);
    double mValue = computeTreeDepthSpecificity(treecodes, wd);
    // TreeDepthSpecificityDivisor used to be hard-coded as 9
    double mSpec = mValue / MMI_TREE_DEPTH_SPECIFICITY_DIVISOR;
    double nmSpec = normalizeValue(nm, mSpec);
    String[] conceptWords = tokenizeTextMore(concept);
    int wValue = conceptWords.length;
    // WordSpecificityDivisor used to be hard-coded as 26
    double wSpec =  wValue / MMI_WORD_SPECIFICITY_DIVISOR;
    double nwSpec = normalizeValue(nw, wSpec);
    // Character specificity
    int cValue = concept.length();
    // CharacterSpecificityDivisor used to be hard-coded as 102
    int cSpec = cValue / MMI_CHARACTER_SPECIFICITY_DIVISOR;
    double ncSpec = normalizeValue(nc, cSpec);

    // Java needs a better shortcut for these arrays
    List<Double> specificities = new ArrayList<Double>();
    specificities.add(nmmSpec);
    specificities.add(nmSpec);
    specificities.add(nwSpec);
    specificities.add(ncSpec);
    return specificities;
  }

  public static double setAATFRank(boolean titleFlag, double spec, double nFreq) {
    if (titleFlag)
      return spec;
    else
      return nFreq * spec;
  }

  public static List<Double> computeProducts(List<Double> valueList0, List<Double> valueList1) {
    List<Double> productList = new ArrayList<Double>();
    for (int i = 0; i < Math.min(valueList0.size(), valueList1.size()); i++) {
      productList.add(valueList0.get(i).doubleValue() * valueList1.get(i).doubleValue());
    }
    return productList;
  }

  public static double computeWeightedValue(List<Double> frequencyList, List<Double> valueList) {
    List<Double> freqValueList = computeProducts(frequencyList, valueList);
    double sum = sumDoubleList(freqValueList);
    double n =  sumDoubleList(frequencyList);
    return sum / n;
  }
  
  public static List<AATF> processTF1(List<TermFrequency> tfList, double maxFreq, 
	     double nc, double nf, double nm, double nmm,
	     double nw, double nz, double wc, double wd,
	     double wm, double wmm, double ww) {
    List<AATF> aatfList = new ArrayList<AATF>();
    for (TermFrequency tf: tfList) {
      double freq = tf.getFrequencyCount() / maxFreq;
      double nFreq = normalizeValue(nf, freq);
      List<Double> specificities = computeSpecificities(tf.getMetaConcept(),
						       tf.getAverageValue(),
						       tf.getTreecodes(),
						       wd, nmm, nm, nw, nc);
      List<Double> frequencies = new ArrayList<Double>();
      frequencies.add(wmm);
      frequencies.add(wm);
      frequencies.add(ww);
      frequencies.add(wc);
      double spec = computeWeightedValue(frequencies, specificities);
      double rank = setAATFRank(tf.getTitleFlag(), spec, nFreq);
      double normalizedRank = normalizeValue(nz, rank);
      double negNRank = -1 * normalizedRank;
      aatfList.add(new AATF(negNRank, tf.getMetaConcept(),
			    tf.getSemanticTypes(), tf.getCui(),
			    tf.getTupleList(), tf.getTreecodes()));
    }
    return aatfList;
  }
  
  public static List<AATF> processTF(List<TermFrequency> tfInfo, double maxFreq) {
    return processTF1(tfInfo, maxFreq, nc, nf, nm, nmm, nw, nz, wc, wd, wm, wmm, ww);
  }
    
}
