
//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public interface ERToken extends Token, PosToken, ClassifiedToken
{
  String getPartOfSpeech();  
  List<Entity> getEntityList();
  void addEntity(Entity entity);
  void setPartOfSpeech(String partOfSpeech);  
}
