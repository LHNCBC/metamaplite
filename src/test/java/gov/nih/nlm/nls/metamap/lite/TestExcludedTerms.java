package gov.nih.nlm.nls.metamap.lite;

import java.util.HashSet;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Describe class TestExcludedTerms here.
 *
 *
 * Created: Thu Apr 16 09:38:26 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TestExcludedTerms extends TestCase {

  SpecialTerms inst = new SpecialTerms();

  /**
   * Creates a new <code>TestExcludedTerms</code> instance.
   *
   */
  public TestExcludedTerms() {

  }

  @org.junit.Before public void setup() {
    this.inst.specialTerms.add("C1031756:idea");
    this.inst.specialTerms.add("C1171947:commit");
    this.inst.specialTerms.add("C1332799:bars");
    this.inst.specialTerms.add("C1410088:still");
    this.inst.specialTerms.add("C1413126:cars");
    this.inst.specialTerms.add("C1419257:ran");
    this.inst.specialTerms.add("C1420522:fact");
    this.inst.specialTerms.add("C1420811:trade");
  }
  
  @org.junit.Test public void test0() {
    this.setup();
    assertTrue(this.inst.isExcluded("C1413126", "cars") &&
	       this.inst.isExcluded("C1420811", "trade"));
  }

  @org.junit.Test public void test1() {
    this.setup();
    assertFalse(this.inst.isExcluded("C1420811", "cars") ||
		this.inst.isExcluded("C1413126", "trade"));
  }
}
