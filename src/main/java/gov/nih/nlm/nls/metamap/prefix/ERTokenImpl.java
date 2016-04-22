//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public class ERTokenImpl extends PosTokenImpl implements Token, PosToken, ClassifiedToken, ERToken
{
  String tokenClass;
  String partOfSpeech;
  List<Entity> entityList = new ArrayList<Entity>();

  public ERTokenImpl(String tokenText, int offset, String tokenClass) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.partOfSpeech = "";
  }

  public ERTokenImpl(String tokenText, int offset, String tokenClass, String partOfSpeech) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.partOfSpeech = partOfSpeech;
  }

  public ERTokenImpl(String tokenText, int offset, String tokenClass, List<Entity> entityList) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.entityList = entityList;
    this.partOfSpeech = "";
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
  public void setPartOfSpeech(String partOfSpeech) {
    this.partOfSpeech = partOfSpeech;
  }
  
  public String toString() {
    return this.tokenText + "|" + this.tokenClass + "|" + this.offset + "|" + this.partOfSpeech;
  }
}
