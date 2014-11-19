//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public class ERTokenImpl implements Token, PosToken, ClassifiedToken, ERToken
{
  String text;
  int position;
  String tokenClass;
  String partOfSpeech;
  List<Entity> entityList = new ArrayList<Entity>();

  public ERTokenImpl(String tokenText, int position, String tokenClass) {
    this.text = tokenText;
    this.position = position;
    this.tokenClass = tokenClass;
  }

  public ERTokenImpl(String tokenText, int position, String tokenClass, List<Entity> entityList) {
    this.text = tokenText;
    this.position = position;
    this.tokenClass = tokenClass;
    this.entityList = entityList;
  }

  @Override
  public String getText() {
    return this.text;
  }
  
  @Override
  public int getPosition() {
    return this.position;
  }

  @Override
  public String getTokenClass() {
    return this.tokenClass;
  }

  @Override
  public String getPartOfSpeech() {
    return this.partOfSpeech;
  }

  @Override
  public List<Entity> getEntityList() {
    return this.entityList;
  }

  public void setEntityList(List<Entity> entityList) {
    this.entityList = entityList;
  }
  public void addEntity(Entity entity) {
    this.entityList.add(entity);
  }
  public String toString() {
    return this.text + "," + this.position;
  }
}
