//
package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

public class SpanEntityMapAndTokenLength {
  Map<String,Entity> spanEntityMap;
  int length;
  public SpanEntityMapAndTokenLength(Map<String,Entity> spanEntityMap, int length) {
    this.spanEntityMap = spanEntityMap;
    this.length = length;
  }
  public Map<String,Entity> getSpanEntityMap() {
    return this.spanEntityMap;
  }
  public int getLength() {
    return this.length;
  }
  public List<Entity> getEntityList() {
    return new ArrayList<Entity>(this.spanEntityMap.values());
  }
  public int size() { return this.spanEntityMap.size(); }
}

