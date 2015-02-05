//
package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Set;
import java.util.HashSet;

import bioc.BioCAnnotation;
import bioc.BioCLocation;

/**
 *
 */

public class BioCEntity extends BioCAnnotation {
  Set<Entity> entitySet = new HashSet<Entity>();
  
  public  BioCEntity(BioCAnnotation annotation) {
    super(annotation);
  }

  public  BioCEntity() {
    super();
  }

  public  BioCEntity(Entity entity) {
    super();
    this.entitySet.add(entity);
    this.setText(entity.getMatchedText());
    BioCLocation location = new BioCLocation();
    location.setLength(entity.getLength());
    location.setOffset(entity.getOffset());
    this.addLocation(location);
  }

  public void addEntity(Entity entity) { this.entitySet.add(entity); }
  public Set<Entity> getEntitySet() { return this.entitySet; }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getText()).append("|");
    for (BioCLocation location: this.getLocations()) {
      sb.append(location.getOffset()).append("|")
	.append(location.getLength()).append("|");
    }
    sb.append(this.entitySet.toString());
    return sb.toString();
  }
}
