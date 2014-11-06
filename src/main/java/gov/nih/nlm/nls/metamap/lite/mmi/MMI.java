
//
package gov.nih.nlm.nls.metamap.lite.mmi;

import java.util.Collections;
import java.util.List;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public class MMI {

  public static void displayEntityList(List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    for (Entity entity: entityList) {
      StringBuilder sb = new StringBuilder();
      sb.append(entity.getScore()).append("|")
	.append(entity.getPreferredName()).append("|")
	.append(entity.getCUI()).append("|")
	.append(entity.getConceptName()).append("|");
      System.out.println(sb);
    }
    System.out.println("-==-");
  }

}
