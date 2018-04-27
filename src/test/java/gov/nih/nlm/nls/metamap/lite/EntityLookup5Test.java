package gov.nih.nlm.nls.metamap.lite;

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
  Set<Entity> initialEntitySet1 = new HashSet<Entity>();
  Set<Entity> expectedEntitySet1 = new HashSet<Entity>();

  
  /**
   * Test if code that removes subsumed entities works as expected.
   *
   */
  public EntityLookup5Test() {
    // initial entity set
    this.initialEntitySet.add(new Entity("00",             "Sleep Apnea", 12, 11, 0.0, new HashSet<Ev>()));
    this.initialEntitySet.add(new Entity("00", "Obstructive Sleep Apnea",  0, 23, 0.0, new HashSet<Ev>()));
    this.initialEntitySet.add(new Entity("00", "Obstructive",              0, 11, 0.0, new HashSet<Ev>()));
    // expected entity set
    this.expectedEntitySet.add(new Entity("00", "Obstructive Sleep Apnea", 0, 23, 0.0, new HashSet<Ev>()));
    
    this.initialEntitySet1.add(new Entity("00", "blood sugar",              0, 11, 0.0, new HashSet<Ev>()));
    this.initialEntitySet1.add(new Entity("00", "blood sugar level",        0, 17, 0.0, new HashSet<Ev>()));
    this.initialEntitySet1.add(new Entity("00",             "level",       12,  5, 0.0, new HashSet<Ev>()));
    this.initialEntitySet1.add(new Entity("00",       "sugar",              6,  5, 0.0, new HashSet<Ev>()));
    // expected entity set
    this.expectedEntitySet1.add(new Entity("00", "blood sugar level",       0, 17, 0.0, new HashSet<Ev>()));
  }

  @org.junit.Before public void setup() {
  }

  @org.junit.Test public void testRemoveSubsumedEntities() {
    Set<Entity> newEntitySet = EntityLookup5.removeSubsumedEntities(this.initialEntitySet);
    System.out.println("expectedEntitySet: " + this.expectedEntitySet);
    System.out.println("newEntitySet: " + newEntitySet);
    org.junit.Assert.assertTrue(newEntitySet.equals(this.expectedEntitySet));

    Set<Entity> newEntitySet1 = EntityLookup5.removeSubsumedEntities(this.initialEntitySet1);
    System.out.println("expectedEntitySet: " + this.expectedEntitySet1);
    System.out.println("newEntitySet: " + newEntitySet1);
    org.junit.Assert.assertTrue(newEntitySet1.equals(this.expectedEntitySet1));
  }



}
