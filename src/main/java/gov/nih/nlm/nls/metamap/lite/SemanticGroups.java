package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;

//
/**
 *
 */

public class SemanticGroups {

  // disorders semantic group
  static Set<String> disorders;
  static {
    disorders = new HashSet<String>();
    disorders.add("acab");
    disorders.add("anab");
    disorders.add("comd");
    disorders.add("cgab");
    disorders.add("dsyn");
    disorders.add("emod");
    disorders.add("fndg");
    disorders.add("inpo");
    disorders.add("mobd");
    disorders.add("neop");
    disorders.add("patf");
    disorders.add("sosy");
  }


  // disorders semantic group without finding semantic type
  static Set<String> disordersEdited;
  static {
    disordersEdited = new HashSet<String>();
    disordersEdited.add("acab");
    disordersEdited.add("anab");
    disordersEdited.add("comd");
    disordersEdited.add("cgab");
    disordersEdited.add("dsyn");
    disordersEdited.add("emod");
    disordersEdited.add("inpo");
    disordersEdited.add("mobd");
    disordersEdited.add("neop");
    disordersEdited.add("patf");
    disordersEdited.add("sosy");
  }

  public static Set<String> getDisorders() {
    return disorders;
  }

  public static Set<String> getDisordersEdited() {
    return disordersEdited;
  }

}
