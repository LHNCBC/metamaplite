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
   * @param <T> element type of set
   * @return true if set1 subset of set2
   */
  public static <T> boolean isSubsetOf(Set<T> set1,Set<T> set2) {
    return set1.containsAll(set2);
  }

  /**
   * Return intersection of two sets, a new set containing the
   * intersection is returned.
   *
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @param <T> element type of set
   * @return a new set containing the intersection of the two supplied sets
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
   * @param <T> element type of set
   * @return a new set containing the union of the two supplied sets
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
   * @param <T> element type of set
   * @return a new set containing the difference of the two supplied sets
   */
  public static <T> Set<T> difference(Set<T> set1,Set<T> set2) {
    Set<T> differenceSet = new HashSet<T>(set1);
    differenceSet.removeAll(set2);
    return differenceSet;
  }

  /**
   * Return symmetric difference of two sets, essentially the
   * disjunctive union.  That is, the union of the sets minus the
   * intersection of the sets.
   *
   * @param set1 a set of type T
   * @param set2 a set of type T
   * @param <T> element type of set
   * @return a new set containing the symmetric difference of the two supplied sets
   */

  public static <T> Set<T> symmetric_difference(Set<T> set1,Set<T> set2) {
    Set<T> differenceSet = union(set1,set2);
    differenceSet.removeAll(intersection(set1,set2));
    return differenceSet;
  }
}

