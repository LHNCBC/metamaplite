package gov.nih.nlm.nls.tools;

import java.util.Set;
import java.util.HashSet;


/**
 * Standard set operations: isSubsetOf, intersection, union,
 * difference.
 */

public class SetOps {

  /**
   * Return is set1 a subset set2
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @return true if set1 subset of set2
   */
  public static <T> boolean isSubsetOf(Set<T> set1,Set<T> set2) {
    return set1.containsAll(set2);
  }

  /**
   * Return intersection of two sets
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @return set intersection of the two supplied sets
   */
  public static <T> Set<T> intersection(Set<T> set1,Set<T> set2) {
    Set<T> intersectionSet = new HashSet<T>(set1);
    intersectionSet.retainAll(set2);
    return intersectionSet;
  }

  /**
   * Return union of two sets
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @return set union of the two supplied sets
   */
  public static <T> Set<T> union(Set<T> set1,Set<T> set2) {
    Set<T> unionSet = new HashSet<T>(set1);
    unionSet.addAll(set2);
    return unionSet;
  }
  
  /**
   * Return difference of two sets
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @return set difference of the two supplied sets
   */
  public static <T> Set<T> difference(Set<T> set1,Set<T> set2) {
    Set<T> differenceSet = new HashSet<T>(set1);
    differenceSet.removeAll(set2);
    return differenceSet;
  }
}

