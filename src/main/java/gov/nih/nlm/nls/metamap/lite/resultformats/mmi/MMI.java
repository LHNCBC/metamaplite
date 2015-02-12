
//
package gov.nih.nlm.nls.metamap.lite.resultformats.mmi;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
/**
 *
 */

public class MMI implements ResultFormatter {

  public static void displayEntityList(List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    for (Entity entity: entityList) {
      StringBuilder sb = new StringBuilder();
      sb.append(entity.getScore()).append("|")
	.append("|").append(entity.getStart()).append(":").append(entity.getLength()).append("|");
      for (Ev ev: entity.getEvList()) {
	sb.append(ev.getConceptInfo().getPreferredName()).append("|")
	  .append(ev.getConceptInfo().getCUI()).append("|")
	  .append(Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)",""))
	  .append("|")
	  .append(Arrays.toString(ev.getConceptInfo().getSourceSet().toArray()).replaceAll("(^\\[)|(\\]$)",""));
      }
      sb.append("|");

      System.out.println(sb);
    }
    System.out.println("-==-");
  }

  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    for (Entity entity: entityList) {
      StringBuilder sb = new StringBuilder();
      sb.append(entity.getScore()).append("|")
	.append("|").append(entity.getStart()).append(":").append(entity.getLength()).append("|");
      for (Ev ev: entity.getEvList()) {
	sb.append(ev.getConceptInfo().getPreferredName()).append("|")
	  .append(ev.getConceptInfo().getCUI()).append("|")
	  .append(Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)",""))
	  .append("|")
	  .append(Arrays.toString(ev.getConceptInfo().getSourceSet().toArray()).replaceAll("(^\\[)|(\\]$)",""));
      }
      sb.append("|");

      pw.println(sb);
    }
    System.out.println("-==-");
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    displayEntityList(writer, entityList);
  }
}
