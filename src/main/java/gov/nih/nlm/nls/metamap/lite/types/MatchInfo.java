
//
package gov.nih.nlm.nls.metamap.lite.types;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import gov.nih.nlm.nls.metamap.lite.types.Span;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;

/**
 *
 */
public class MatchInfo {
  String preferredName;
  Set<String> semanticTypeSet = new HashSet<String>();
  /** matchedText --&gt; trigger info map */
  Map<String,Set<TriggerInfo>> matchedTextToTriggerInfoListMap = new HashMap<String,Set<TriggerInfo>>();

  public MatchInfo(String preferredName, Set<String> semanticTypeSet) {
    this.preferredName = preferredName;
    this.semanticTypeSet = semanticTypeSet;
  }

  public void addTriggerInfo(String matchText,
			     String conceptString,
			     String location,
			     String partOfSpeech,
			     int sentPos,
			     boolean negationStatus,
			     Span span) {
    if (matchedTextToTriggerInfoListMap.containsKey(matchText)) {
      Set<TriggerInfo> triggerInfoList = matchedTextToTriggerInfoListMap.get(matchText);
      triggerInfoList.add(new TriggerInfo(conceptString,location,
					  matchText,sentPos,
					  partOfSpeech,negationStatus,span));
    } else {
      Set<TriggerInfo> triggerInfoList = new LinkedHashSet<TriggerInfo>();
      triggerInfoList.add(new TriggerInfo(conceptString,location,
					  matchText,sentPos,
					  partOfSpeech,negationStatus,span));
      matchedTextToTriggerInfoListMap.put(matchText, triggerInfoList);
    }
  }
    
  public Map<String,Set<TriggerInfo>> getMatchedTextTriggerInfoListMap() {
    return this.matchedTextToTriggerInfoListMap;
  }
  public String getPreferredName() {
    return this.preferredName;
  }
  public Set<String> getSemanticTypeSet() {
    return this.semanticTypeSet;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("|").append(preferredName);
    return sb.toString();
  }


}
