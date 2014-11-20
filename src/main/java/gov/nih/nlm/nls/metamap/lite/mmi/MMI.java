
//
package gov.nih.nlm.nls.metamap.lite.mmi;

import java.util.Arrays;
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
	.append(Arrays.toString(entity.getMatchedWordSet().toArray()).replaceAll("(^\\[)|(\\]$)", ""))
	.append("|")
	.append(Arrays.toString(entity.getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)", ""))
	.append("|")
	.append(Arrays.toString(entity.getSourceSet().toArray()).replaceAll("(^\\[)|(\\]$)", ""))
	.append("|").append(entity.getStart()).append(":").append(entity.getEnd()).append("|");
      System.out.println(sb);
    }
    System.out.println("-==-");
  }

}
