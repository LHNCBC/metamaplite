
package gov.nih.nlm.nls.clinical;

import java.util.Map;
import java.util.HashMap;

//
/**
 *
 */

public class IndicatorNote {
  Map<String,String> patternFormatByNoteMap = new HashMap<String,String>();

  public IndicatorNote() {
    this.patternFormatByNoteMap.put("Beginning, followed by ''is'', ''of'', '':'', '' -'' ", "\n%s[:\\-]*");
    this.patternFormatByNoteMap.put("Beginning, on a line by itself, followed by '':'' or '' -''", "\n%s[:\\-]*");
    this.patternFormatByNoteMap.put("ONLY AT END OF DOCUMENT and followed by '':''", "%s");
    this.patternFormatByNoteMap.put("ONLY IF IN FIRST HALF OF DOCUMENT and followed by '':''", "%s");
    this.patternFormatByNoteMap.put("On a line by itself or followed by '':'','' -''", "%s");
    this.patternFormatByNoteMap.put("On a line by itself, followed by '':'' or '' -''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'' or '' -''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'' or \"-\", or preceded by ''[#].''", "%s");
    this.patternFormatByNoteMap.put("followed by '':''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', '' -'' or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', '' -''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''-'' or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''-''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''-'', or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''-'',''to'',''are to''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''-'',''to'',''are to'', ''allergy'' may be preceded by ''penicillin''", "%s[:\\-]*");
    this.patternFormatByNoteMap.put("followed by '':'', ''are as follows''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', ''are as follows'', \"-\"", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', '-\"", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', \"-\" or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', \"-\", ''are as follows''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'', \"-\", \"are\", \"include\"", "%s");
    this.patternFormatByNoteMap.put("followed by '':'','' -''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'','';'' or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'','';'', \"-\" or on a line by itself", "%s");
    this.patternFormatByNoteMap.put("followed by '':'',''is'',''was''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'',\"-\",''is'',''was''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'';phrases ending with ''include'' may or may not be followed by '':''", "%s");
    this.patternFormatByNoteMap.put("followed by '':'';phrases ending with ''include'' may or may not be followed by '':\"", "%s");
    this.patternFormatByNoteMap.put("followed by \":\" or \"-\"", "%s");
    this.patternFormatByNoteMap.put("followed by \":\"", "%s");
    this.patternFormatByNoteMap.put("followed by \":\", \"are\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or  '':''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or  '':'', '' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'',  \"-\", should not be preceded by ''special''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', '' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', '' -'',''was significant for'', ''was positive for''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', '-\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', \"-\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', \"-\", \"is\", \"was\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', \"-\",", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'', should not be preceded by ''special''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or '':'',\"-\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or \":\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or \":\", \"is\", \"was\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself or followed by '':'','' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, '':'', '' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':'' or '' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':'', \"-\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':'', \"-\", \"to\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':'',''-'',''of'',''is'' (of and is only if the word family starts a on a line by itself),''shows''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':'',''-'',''of'',''to'',''is'',''shows''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or by '':''; should not be followed by ''by''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or followed by '':'','' -''", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or followed by \":\" or \"-\"", "%s");
    this.patternFormatByNoteMap.put("on a line by itself, or followed by \":\"", "%s");
  }			     

  public String getFormatForNote(String note) { return this.patternFormatByNoteMap.get(note); }
}

