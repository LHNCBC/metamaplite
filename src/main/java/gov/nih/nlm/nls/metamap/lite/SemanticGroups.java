package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

//
/**
 * Various Semantic Groups including custom ones, mostly disorders.
 */

public class SemanticGroups {

  // disorders semantic group
  static Set<String> disorders;
  static {
    disorders = new HashSet<String>
      (Arrays.asList
       ("acab", "anab", "comd", "cgab", "dsyn", "emod", "fndg", "inpo",
	"mobd", "neop", "patf", "sosy"));
  }

  // disorders semantic group without finding semantic type
  static Set<String> disordersEdited;
  static {
    disordersEdited = new HashSet<String>
      (Arrays.asList
       ("acab", "anab", "comd", "cgab", "dsyn", "emod", "inpo", "mobd",
	"neop", "patf", "sosy"));
  }

  /** clinical disorders semantic group:
      "acab", "anab", "bact", "cgab", "dsyn", "inpo", "mobd", "neop",
      "patf", "sosy" */

  static Set<String> clinicalDisorders;
  static {
    clinicalDisorders = new HashSet<String>
      (Arrays.asList
       ("acab", "anab", "bact", "cgab", "dsyn", "emod", "inpo", "mobd",
	"neop", "patf", "sosy"));
  }

  static Set<String> literatureDisorders;
  static {
    disordersEdited = new HashSet<String>
      (Arrays.asList
       ("acab", "anab", "cgab", "dsyn", "emod", "inpo", "mobd",
	"neop", "patf", "sosy"));
  }

  public static Set<String> getDisorders() {
    return disorders;
  }
  public static Set<String> getDisordersEdited() {
    return disordersEdited;
  }
  public static Set<String> getLiteratureDisorders() {
    return literatureDisorders;
  }
  public static Set<String> getClinicalDisorders() {
    return clinicalDisorders;
  }

  /**
   * Create semantic type set from string representation.
   * @param semanticTypeSetRepresentation string containing semantic type abbreviations separated by commas.
   * @return set of semantic types
   */
  public static Set<String> createSemanticTypeSet(String semanticTypeSetRepresentation) {
    return new HashSet<String>(Arrays.asList(semanticTypeSetRepresentation.split(",")));
  }

}
