//
package gov.nih.nlm.nls.metamap.lite.resultformats.mmi;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.types.Span;
import gov.nih.nlm.nls.metamap.lite.types.SpanImpl;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.types.TriggerInfo;
import gov.nih.nlm.nls.metamap.lite.types.MatchInfo;

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
    Set<String> triggerStringSet = new LinkedHashSet<String>();
    for (Ev ev: entity.getEvList()) {
      if (ev.getConceptInfo().getCUI().equals(cui)) {
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
    Set<String> positionStringSet = new LinkedHashSet<String>();
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


  public static Map<String,MatchInfo> genCuiMatchInfoMap(List<Entity> entityList) {
    Map<String,MatchInfo> cuiMatchInfoMap = new HashMap<String,MatchInfo>();
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvSet()) {
	String cui = ev.getConceptInfo().getCUI();
	String preferredName = ev.getConceptInfo().getPreferredName();
	if (cuiMatchInfoMap.containsKey(cui)) {
	  MatchInfo newEntity = cuiMatchInfoMap.get(cui);
	  newEntity.addTriggerInfo(ev.getMatchedText(),
				   ev.getConceptString(),
				   "txt",
				   ev.getPartOfSpeech(),
				   0,
				   entity.isNegated(),
				   new SpanImpl(ev.getStart(), ev.getStart() + ev.getLength()));	  
	} else {
	  Set<String> semanticTypeSet = ev.getConceptInfo().getSemanticTypeSet();
	  MatchInfo newEntity = new MatchInfo(preferredName, semanticTypeSet);
	  newEntity.addTriggerInfo(ev.getMatchedText(),
				   ev.getConceptString(),
				   "txt",
				   ev.getPartOfSpeech(),
				   0,
				   entity.isNegated(),
				   new SpanImpl(ev.getStart(), ev.getStart() + ev.getLength()));
	  cuiMatchInfoMap.put(cui, newEntity);
	}
      }
    }
    return cuiMatchInfoMap;
  }


  /**  
   * Create map cui to entitylist 
   * @param entityList list of entities
   * @return Map of entities keyed by cuis.
   */
  public static Map<String,List<Entity>> genCuiEntityListMap(List<Entity> entityList) {
    Map<String,List<Entity>> cuiEntityListMap = new TreeMap<String,List<Entity>>();
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvSet()) {
	String cui = ev.getConceptInfo().getCUI();
	List<Entity> cuiEntityList;
	if (cuiEntityListMap.containsKey(cui)) {
	  cuiEntityList = cuiEntityListMap.get(cui);
	} else {
	  cuiEntityList = new ArrayList<Entity>();
	  cuiEntityListMap.put(cui, cuiEntityList);
	}
	cuiEntityList.add(entity);
      }
    }
    return cuiEntityListMap;
  }


  public static String entityListToString(List<Entity> entityList) {
    Set<String> stringSet = new LinkedHashSet<String>();
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
  
  public static String entityToString(Entity entity,
				      Collection<String> triggerInfoColl,
				      Collection<String> positionColl) {
    StringBuilder sb = new StringBuilder();
    for (Ev ev: entity.getEvSet()) {
      sb.append(entity.getDocid()).append("|")
	.append(entity.getScore()).append("|")
	.append(ev.getConceptInfo().getPreferredName()).append("|")
	.append(ev.getConceptInfo().getCUI())
	.append("|[")
	.append(Arrays.toString(ev.getConceptInfo().getSemanticTypeSet().toArray()).replaceAll("(^\\[)|(\\]$)",""))
	.append("]|");
      Iterator<String> iter = triggerInfoColl.iterator();
      while ( iter.hasNext() ) {
	sb.append( iter.next());
	if (iter.hasNext()) sb.append(',');
      }
      sb.append("|").append("tx").append("|");
      iter = positionColl.iterator();
      while ( iter.hasNext()) {
	sb.append( iter.next() );
	if (iter.hasNext()) sb.append("|");
      }
      sb.append("|").append('\n');
    }
    return sb.toString();
  }

  public static void displayEntityList(List<Entity> entityList) 
  {
    int seqno = 0;
    Map<String,MatchInfo> cuiEntityMap = genCuiMatchInfoMap(entityList);
    for (Map.Entry<String,MatchInfo> cuiEntity: cuiEntityMap.entrySet()) {
      String cui = cuiEntity.getKey();
      MatchInfo val = cuiEntity.getValue();
      System.out.print("text|0.0|" + cui + "|" + seqno + "|" + val);
      seqno++;
    }
  }

  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    int seqno = 0;
    Map<String,MatchInfo> cuiEntityMap = genCuiMatchInfoMap(entityList);
    for (Map.Entry<String,MatchInfo> cuiEntity: cuiEntityMap.entrySet()) {
      String cui = cuiEntity.getKey();
      MatchInfo val = cuiEntity.getValue();
      pw.print("text|0.0|" + val.getPreferredName() + "|" + cui + "|" + seqno + "|" +
	       StringUtils.join(val.getSemanticTypeSet(), ",") + "|");

      Set<String> spanSet = new LinkedHashSet<String>();
      Set<String> triggerInfoStringList = new LinkedHashSet<String>();
      for (Map.Entry<String,Set<TriggerInfo>> entry: val.getMatchedTextTriggerInfoListMap().entrySet()) {
	String matchedText = entry.getKey();
	for (TriggerInfo triggerInfo: entry.getValue()) {
	  triggerInfoStringList.add(triggerInfo.toString());
	  spanSet.add(triggerInfo.getSpan().toString());
	}
      }
      pw.print(StringUtils.join(triggerInfoStringList, ","));
      pw.print("|" + StringUtils.join(spanSet, ","));
      pw.print("\n");
      seqno++;
    }
  }

  
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    displayEntityList(writer, entityList);
  }

  public String entityListFormatToString(List<Entity> entityList)
  {
    int seqno = 0;
    StringBuilder sb = new StringBuilder();
    Map<String,MatchInfo> cuiEntityMap = genCuiMatchInfoMap(entityList);
    for (Map.Entry<String,MatchInfo> cuiEntity: cuiEntityMap.entrySet()) {
      String cui = cuiEntity.getKey();
      MatchInfo val = cuiEntity.getValue();
      sb.append("text|0.0|").append(val.getPreferredName()).append("|").append(cui).append("|").append(seqno).append("|" +
	       StringUtils.join(val.getSemanticTypeSet(), ",")).append("|");

      Set<String> spanSet = new LinkedHashSet<String>();
      Set<String> triggerInfoStringList = new LinkedHashSet<String>();
      for (Map.Entry<String,Set<TriggerInfo>> entry: val.getMatchedTextTriggerInfoListMap().entrySet()) {
	String matchedText = entry.getKey();
	for (TriggerInfo triggerInfo: entry.getValue()) {
	  triggerInfoStringList.add(triggerInfo.toString());
	  spanSet.add(triggerInfo.getSpan().toString());
	}
      }
      sb.append(StringUtils.join(triggerInfoStringList, ","))
	.append("|" + StringUtils.join(spanSet, ","))
	.append("\n");
      seqno++;
    }
    return sb.toString();
  }

  public void initProperties(Properties properties) {
  }
}
