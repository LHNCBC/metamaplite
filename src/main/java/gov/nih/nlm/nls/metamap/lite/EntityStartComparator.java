package gov.nih.nlm.nls.metamap.lite;

import java.util.Comparator;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

public class EntityStartComparator implements Comparator<Entity> {
  public int compare(Entity o1, Entity o2) { return o1.getStart() - o2.getStart(); }
  public boolean equals(Object obj) { return false; }
  public int hashCode() { return 0; }
}

