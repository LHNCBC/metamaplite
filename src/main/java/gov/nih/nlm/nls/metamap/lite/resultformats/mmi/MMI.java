//
package gov.nih.nlm.nls.metamap.lite.resultformats.mmi;

import java.io.PrintWriter;
import java.io.IOException;
import java.text.NumberFormat;
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
import java.util.stream.Collectors;
import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.types.Span;
import gov.nih.nlm.nls.metamap.lite.types.SpanImpl;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Position;
import gov.nih.nlm.nls.metamap.lite.types.PositionImpl;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.types.TriggerInfo;
import gov.nih.nlm.nls.metamap.lite.types.MatchInfo;
import gov.nih.nlm.nls.metamap.lite.metamap.MetaMapIvfIndexes;

import gov.nih.nlm.nls.metamap.mmi.AATF;
import gov.nih.nlm.nls.metamap.mmi.Ranking;
import gov.nih.nlm.nls.metamap.mmi.TermFrequency;
import gov.nih.nlm.nls.metamap.mmi.Tuple;
import gov.nih.nlm.nls.metamap.mmi.Tuple7;


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

  NumberFormat scoreFormat = NumberFormat.getInstance();

  public MMI()
  {
    scoreFormat.setMaximumFractionDigits(2);
    scoreFormat.setMinimumFractionDigits(2);
    scoreFormat.setGroupingUsed(false);
  }

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
      System.out.print("text|" + entityList.get(0).getScore() + "|" + cui + "|" + seqno + "|" + val);
      seqno++;
    }
  }

  /**
   * @param pw printwriter used for output
   * @param entityList entitylist to be rendered for output
   * @deprecated
   */
  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    int seqno = 0;
    Map<String,MatchInfo> cuiEntityMap = genCuiMatchInfoMap(entityList);
    for (Map.Entry<String,MatchInfo> cuiEntity: cuiEntityMap.entrySet()) {
      String cui = cuiEntity.getKey();
      MatchInfo val = cuiEntity.getValue();
      pw.print("text|" + entityList.get(0).getScore() + "|" + val.getPreferredName() + "|" + cui + "|" + seqno + "|" +
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

  /**
   * render tuple without positional information.
   * @param tuple seven item Tuple.
   * @return tuple string representation with positional information
   */
  public String renderTupleInfo(Tuple tuple) {
    return "\"" + tuple.getTerm() + "\"-" +
      tuple.getField() + "-" +
      tuple.getNSent() + "-\"" +
      tuple.getText() + "\"-" +		
      tuple.getLexCat() + "-" +
      tuple.getNeg();
  }
  
  /**
   * render positional information from tuple to string.
   * @param tuple seven item Tuple.
   * @return positional information in formatted string form.
   */
  public String renderPositionInfo(Tuple tuple) {
    return tuple.getPosInfo().stream().map(i -> ((PositionImpl)i).toStringStartLength()).collect(Collectors.joining(","));
  }
  
  /**
   * @param pw printwriter used for output
   * @param docid document identifier
   * @param entityList entitylist to be rendered for output
   */
  public void renderEntityList(PrintWriter pw, String docid, List<Entity> entityList) 
  {
    List<TermFrequency> tfList = this.entityToTermFrequencyInfo(entityList);
    List<AATF> aatfList = Ranking.processTF(tfList, 1000);
    Collections.sort(aatfList);
    for (AATF aatf: aatfList) {
      Set<String> fieldSet =
	aatf.getTuplelist()
	.stream()
	.map(tuple -> tuple.getField())
	.collect(Collectors.toCollection(LinkedHashSet::new));
      pw.println(docid + "|MMI|" +
		 scoreFormat.format(-10000 *aatf.getNegNRank()) + "|" +
		 aatf.getConcept() +"|" +
		 aatf.getCui() +"|" +
		 aatf.getSemanticTypes() +"|" +
		 aatf.getTuplelist().stream().map(i -> this.renderTupleInfo(i)).collect(Collectors.joining(","))  + "|" +
		 fieldSet.stream().map(i -> i).collect(Collectors.joining(";")) + "|" +
		 aatf.getTuplelist().stream().map(i -> this.renderPositionInfo(i)).collect(Collectors.joining(";"))  + "|" +
		 aatf.getTreeCodes().stream().map(i -> i.toString()).collect(Collectors.joining(";")) + "|" );
    }
  }
  
  /**
   * map entities by document id.
   * @param entityList input entitylist 
   * @return map of entities key by document id.
   */
  public static Map<String,List<Entity>> genDocidEntityMap(List<Entity> entityList) {
    Map<String,List<Entity>> docidEntityMap =
      new HashMap<String,List<Entity>>();
    for (Entity entity: entityList) {
      if (docidEntityMap.containsKey(entity.getDocid())) {
	docidEntityMap.get(entity.getDocid()).add(entity);
      } else {
	List<Entity> newEntityList = new ArrayList<Entity>();
	newEntityList.add(entity);
	docidEntityMap.put(entity.getDocid(), newEntityList);
      }
    }
    return docidEntityMap;
  }
  

  /**
   * @param writer printwriter used for output
   * @param entityList entitylist to be rendered for output
   */
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    
    for (Map.Entry<String,List<Entity>> entry: genDocidEntityMap(entityList).entrySet() ) {
      this.renderEntityList(writer, entry.getKey(), entry.getValue());
    }
  }

  /**
   * @param sb string builder used for output
   * @param docid document identifier
   * @param entityList entitylist to be rendered for output
   */
  public void renderEntityList(StringBuilder sb, String docid, List<Entity> entityList)
  {
    List<TermFrequency> tfList = this.entityToTermFrequencyInfo(entityList);
    List<AATF> aatfList = Ranking.processTF(tfList, 1000);
    Collections.sort(aatfList);
    for (AATF aatf: aatfList) {
      Set<String> fieldSet =
	aatf.getTuplelist()
	.stream()
	.map(tuple -> tuple.getField())
	.collect(Collectors.toCollection(LinkedHashSet::new));
      sb.append(docid).append("|MMI|").append(scoreFormat.format(-10000 * aatf.getNegNRank())).append("|")
	.append(aatf.getConcept()).append("|")
	.append(aatf.getCui()).append("|")
	.append(aatf.getSemanticTypes()).append("|")
	.append(aatf.getTuplelist().stream().map(i -> this.renderTupleInfo(i)).collect(Collectors.joining(","))).append("|")
	.append(fieldSet.stream().map(i -> i).collect(Collectors.joining(";"))).append("|")
	.append(aatf.getTuplelist().stream().map(i -> this.renderPositionInfo(i)).collect(Collectors.joining(";"))).append("|")
	.append(aatf.getTreeCodes().stream().map(i -> i.toString()).collect(Collectors.joining(";"))).append("\n");
    }
  }

  public String entityListFormatToString(List<Entity> entityList)
  {
    int seqno = 0;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String,List<Entity>> entry: genDocidEntityMap(entityList).entrySet() ) {
      renderEntityList(sb, entry.getKey(), entry.getValue());
    }
    return sb.toString();
  }

  public List<TermFrequency> entityToTermFrequencyInfo(List<Entity> entityList) {
    Map<String,TermFrequency> termFreqMap = new HashMap<String,TermFrequency>();
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvList()) {
	String cui = ev.getConceptInfo().getCUI();
	String conceptString = ev.getConceptInfo().getConceptString();
	String tfKey = cui;
	if (termFreqMap.containsKey(tfKey)) {
	  TermFrequency tf = termFreqMap.get(tfKey);
	  List<Position> posInfo = new ArrayList<Position>();
	  int start = ev.getStart();
	  int end = ev.getStart() + ev.getLength();
	  posInfo.add(new PositionImpl(start, end));
	  Tuple tuple = new Tuple7(ev.getConceptInfo().getConceptString(),
				   entity.getFieldId() == null ? "text" : entity.getFieldId(), // section/location field needs to be added to Entity or Ev (or both)
				   entity.getSentenceNumber(), // sentence number needs to be added to Entity or Ev (or both)
				   ev.getMatchedText(), // text?
				   entity.getLexicalCategory(), // lexical category needs to be added to Entity or Ev (or both)
				   entity.isNegated() ? 1 : 0, // neg?
				   posInfo);
	  tf.getTupleSet().add(tuple);
	  tf.setFrequencyCount(tf.getFrequencyCount() + 1);
	} else {
	  List<Position> posInfo = new ArrayList<Position>();
	  int start = ev.getStart();
	  int end = ev.getStart() + ev.getLength();
	  posInfo.add(new PositionImpl(start, end));
	  Set<Tuple> tupleSet = new LinkedHashSet<Tuple>();
	  Tuple tuple = new Tuple7(ev.getConceptInfo().getConceptString(),
				   entity.getFieldId() == null ? "text" : entity.getFieldId(), // section/location field needs to be added to Entity or Ev (or both)
				   entity.getSentenceNumber(), // sentence number needs to be added to Entity or Ev (or both)
				   ev.getMatchedText(), // text?
				   entity.getLexicalCategory(), // lexical category needs to be added to Entity or Ev (or both)
				   entity.isNegated() ? 1 : 0, // neg?
				   posInfo); 
	  tupleSet.add(tuple);
	  String preferredName = ev.getConceptInfo().getPreferredName();
	  termFreqMap.put(tfKey,
			  new TermFrequency(preferredName,
					    new ArrayList<String>(ev.getConceptInfo().getSemanticTypeSet()),
					    tupleSet, 
					    entity.getFieldId() == null ? false :
					    (entity.getFieldId().equals("title") ||
					     entity.getFieldId().equals("TI") )
					    , 
					    cui,
					    1,
					    ev.getScore(),
					    getTreecodes(preferredName)));
	}
      }
    }
    // List<TermFrequency> tfList = new ArrayList<TermFrequency>();
    return new ArrayList<TermFrequency>(termFreqMap.values());
  }

  public MetaMapIvfIndexes mmIndexes;

  public List<String> getTreecodes(String term) {
    try {
      List<String> treecodeList = new ArrayList<String>();
      for (String hit :this.mmIndexes.meshTcRelaxedIndex.lookup(term,0)) {
	String[] fields = hit.split("\\|");
	treecodeList.add(fields[1]);
      }
      return treecodeList;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public void initProperties(Properties properties) {
    try {
      this.mmIndexes = new MetaMapIvfIndexes(properties);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
