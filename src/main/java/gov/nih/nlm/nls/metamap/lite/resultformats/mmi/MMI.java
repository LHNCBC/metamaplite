
//
package gov.nih.nlm.nls.metamap.lite.resultformats.mmi;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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
 * <p>
 *  UMLS Concept (Preferred or Synonym Text)
 * <ul>
 *  <li>loc - Location in the text if identifiable. ti - Title, ab - Abstract, and tx - Free Text 
 *  <li>locPos - Number of the utterance within the loc starting with one (1). For example, "ti-1" denotes first utterance in title. 
 *  <li>text - The actual text mapped to this UMLS concept identification.
 *  <li>Part of Speech - N/A
 *  <li>Negation Flag
 * </ul>
 */

public class MMI implements ResultFormatter {

  public static String evToTriggerString(Ev ev, Entity entity) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(ev.getConceptInfo().getPreferredName()).append("\"-tx-");
    sb.append(entity.getLocationPosition()).append( "-\"");
    sb.append(ev.getMatchedText()).append( "\"-" + ev.getPartOfSpeech() + "-");
    sb.append(entity.isNegated() ? "1" : "0");
    return sb.toString();
  }

  public static String triggerInfoToString(Entity entity, String cui) {
    Set<String> triggerStringSet = new HashSet<String>();
    for (Ev ev: entity.getEvList()) {
      if (ev.getConceptInfo().getCUI() == cui) {
	triggerStringSet.add(evToTriggerString(ev, entity));
      }
    }
    List<String> triggerStrings = new ArrayList<String>(triggerStringSet);
    StringBuilder sb = new StringBuilder();
    String firstTrigger = triggerStrings.get(0);
    sb.append(firstTrigger);
    for (String trigger: triggerStrings.subList(1, triggerStrings.size())) {
	sb.append(",");
	sb.append(trigger);
    }
    return sb.toString();
  }

  public static String renderPosition(Ev ev) {
    StringBuilder sb = new StringBuilder();
    sb.append(ev.getStart()).append(":").append(ev.getLength());
    return sb.toString();
  }

  public static String positionalInfo(Entity entity, String cui) {
    Set<String> positionStringSet = new HashSet<String>();
    for (Ev ev: entity.getEvSet()) {
      if (ev.getConceptInfo().getCUI() == cui) {
	positionStringSet.add(renderPosition(ev));
      }
    }
    List<String> positionStrings = new ArrayList<String>(positionStringSet);
    StringBuilder sb = new StringBuilder();
    String firstPosition = positionStrings.get(0);
    sb.append(firstPosition);
    for (String position: positionStrings.subList(1, positionStrings.size())) {
	sb.append(",");
	sb.append(position);
    }
    return sb.toString();
  }

  public static String entityToString(Entity entity){
    StringBuilder sb = new StringBuilder();
    for (Ev ev: entity.getEvSet()) {
      sb.append(entity.getDocid()).append("|")
	.append(entity.getScore()).append("|")
	.append(ev.getConceptInfo().getPreferredName()).append("|")
	.append(ev.getConceptInfo().getCUI())
	.append("|[")
	.append(Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)",""))
	.append("]|")
	.append(triggerInfoToString(entity, ev.getConceptInfo().getCUI())).append("|")
	.append("tx").append("|")
	.append(positionalInfo(entity, ev.getConceptInfo().getCUI())).append("|").append('\n');
    }
    return sb.toString();
  }

  public static String entityListToString(List<Entity> entityList) {
    Set<String> stringSet = new HashSet<String>();
    for (Entity entity: entityList) {
      if (entity.getEvSet().size() > 0) {
	stringSet.add(entityToString(entity));
      }
    }
    StringBuilder sb = new StringBuilder();
    for (String resultString: stringSet) {
      sb.append(resultString);
    }
    return sb.toString();
  }

  public static void displayEntityList(List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    Set<String> stringSet = new HashSet<String>();
    for (Entity entity: entityList) {
      if (entity.getEvList().size() > 0) {
	stringSet.add(entityToString(entity));
      }
    }
    StringBuilder sb = new StringBuilder();
    for (String resultString: stringSet) {
      System.out.print(resultString);
    }
  }

  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    Set<String> stringSet = new HashSet<String>();
    for (Entity entity: entityList) {
      if (entity.getEvSet().size() > 0) {
	stringSet.add(entityToString(entity));
      }
    }
    StringBuilder sb = new StringBuilder();
    for (String resultString: stringSet) {
      pw.print(resultString);
    }
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    displayEntityList(writer, entityList);
  }
}
