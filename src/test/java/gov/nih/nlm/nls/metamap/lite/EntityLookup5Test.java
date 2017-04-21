package gov.nih.nlm.nis.metamap.lite;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;

import java.util.Set;
import java.util.HashSet;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.EntityLookup5;

/**
 * Describe class EntityLookup5Test here.
 *
 *
 * Created: Thu Apr 20 17:31:52 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class EntityLookup5Test {

  Set<Entity> initialEntitySet = new HashSet<Entity>();
  Set<Entity> expectedEntitySet = new HashSet<Entity>();
  
  /**
   * Creates a new <code>EntityLookup5Test</code> instance.
   *
   */
  public EntityLookup5Test() {
    this.initialEntitySet.add(new Entity("00", "Sleep", 12, 5, 0.0, new HashSet<Ev>()));
    this.initialEntitySet.add(new Entity("00", "Sleep Apnea", 12, 11, 0.0, new HashSet<Ev>()));
    this.initialEntitySet.add(new Entity("00", "Obstructive Sleep Apnea", 0, 23, 0.0, new HashSet<Ev>()));
    this.initialEntitySet.add(new Entity("00", "Obstructive", 0, 11, 0.0, new HashSet<Ev>()));
    this.expectedEntitySet.add(new Entity("00", "Obstructive Sleep Apnea", 0, 23, 0.0, new HashSet<Ev>()));
  }

  @org.junit.Before public void setup() {
  }

  @org.junit.Test public void testRemoveSubSumingEntities() {
    Set<Entity> newEntitySet = EntityLookup5.removeSubsumingEntities(this.initialEntitySet);
    org.junit.Assert.assertTrue(newEntitySet.equals(this.expectedEntitySet));
  }
}
