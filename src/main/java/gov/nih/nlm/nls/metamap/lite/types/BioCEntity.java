
//
package gov.nih.nlm.nls.metamap.lite.types;

import java.util.Set;

import bioc.BioCAnnotation;
import bioc.BioCLocation;

/**
 *
 */

public class BioCEntity extends BioCAnnotation {
  Entity entity;
  
  public  BioCEntity(BioCAnnotation annotation) {
    super(annotation);
  }

  public  BioCEntity() {
    super();
  }

  public  BioCEntity(Entity entity) {
    super();
    this.entity = entity;
    this.setID(entity.getCUI());
    this.setText(entity.getMatchedText());
    BioCLocation location = new BioCLocation();
    location.setLength(entity.getLength());
    location.setOffset(entity.getOffset());
    this.addLocation(location);
  }

  public Entity getEntity() { return this.entity; }
  
  public String toString() {
    return super.toString() + "|" + this.entity.toString();
  }
}
