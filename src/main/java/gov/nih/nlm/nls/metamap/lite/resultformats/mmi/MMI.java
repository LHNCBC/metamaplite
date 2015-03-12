
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
 * Fielded MetaMap Indexing (MMI) Output
 *
 * Sample record (pipe separated):
 * <pre>
 * id|MMI|score|preferredname|cui|semtypelist|triggerinfo|location|posinfo|treecodes
 * </pre>
 * Triggerinfo (dash separated):  (ex:  "UMLS concept-loc-locPos-text-Part of Speech-Negation Flag". )
 * <pre>
 *  UMLS Concept (Preferred or Synonym Text)
 *  <ul>
 *  <li>loc - Location in the text if identifiable. ti - Title, ab - Abstract, and tx - Free Text 
 *  <li>locPos - Number of the utterance within the loc starting with one (1). For example, "ti-1" denotes first utterance in title. 
 *  <li>text - The actual text mapped to this UMLS concept identification.
 *  <li>Part of Speech - N/A
 *  <li>Negation Flag
 * </pre>
 */

public class MMI implements ResultFormatter {

  public static String triggerInfoToString(Entity entity, Ev ev) {
    return "\"" + ev.getConceptInfo().getPreferredName() + "\"-tx-" + 
      entity.getLocationPosition() + "-\"" + 
      ev.getMatchedText() + "\"-unknown-" + 
      ((entity.isNegated()) ? "1" : "0") ;
  }

  public static String entityToString(Entity entity){
    StringBuilder sb = new StringBuilder();
    for (Ev ev: entity.getEvList()) {
      sb.append(entity.getDocid()).append("|")
	.append(entity.getScore()).append("|")
	.append(ev.getConceptInfo().getPreferredName()).append("|")
	.append(ev.getConceptInfo().getCUI())
	.append("|[")
	.append(Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)",""))
	.append("]|")
	.append(triggerInfoToString(entity, ev)).append("|")
	.append("tx").append("|")
	.append(entity.getStart()).append(":").append(entity.getLength()).append("|")
      	;
    }
    return sb.toString();
  }

  public static String entityListToString(List<Entity> entityList) {
    StringBuilder sb = new StringBuilder();
    for (Entity entity: entityList) {
      sb.append(entityToString(entity)).append("\n");
    }
    return sb.toString();
  }

  public static void displayEntityList(List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    for (Entity entity: entityList) {
      System.out.println(entityToString(entity));
    }
    System.out.println("-==-");
  }

  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    for (Entity entity: entityList) {
      pw.println(entityToString(entity));
    }
    System.out.println("-==-");
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    displayEntityList(writer, entityList);
  }
}
