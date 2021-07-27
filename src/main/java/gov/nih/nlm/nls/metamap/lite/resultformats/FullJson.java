package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Serialize contents of FullJson format in JSON.
 *
 * This basically serializes Entity, Ev, and ConceptInfo objects in
 * result to JSON.  Mainly for debugging.
 *
 * Created: Fri Feb 21 03:07:22 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class FullJson implements ResultFormatter {

  public JSONObject conceptInfoToJson(ConceptInfo ci) {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("cui", ci.getCUI());
    jsonObj.put("preferredname", ci.getPreferredName());
    jsonObj.put("conceptstring", ci.getConceptString());
    JSONArray semanticTypeArray = new JSONArray();
    for (String semtype: ci.getSemanticTypeSet()) {
      semanticTypeArray.put(semtype);
    }
    jsonObj.put("semantictypes", semanticTypeArray);

    JSONArray sourceArray = new JSONArray();
    for (String sourceAbbrev: ci.getSourceSet()) {
      sourceArray.put(sourceAbbrev);
    }
    jsonObj.put("sources", sourceArray);
    return jsonObj;
  }
  
  public JSONObject evToJson(Ev ev) {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", ev.getId());
    jsonObj.put("matchedtext", ev.getMatchedText());
    jsonObj.put("score", ev.getScore());
    jsonObj.put("start", ev.getStart());
    jsonObj.put("length", ev.getLength());
    JSONObject conceptInfoJsonObj = conceptInfoToJson(ev.getConceptInfo());
    jsonObj.put("conceptinfo", conceptInfoJsonObj);
    return jsonObj;
  }
  
  public JSONObject entityToJson(Entity entity) {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("docid", entity.getDocid());
    jsonObj.put("id", entity.getId());
    jsonObj.put("matchedtext", entity.getMatchedText());
    jsonObj.put("fieldid", entity.getFieldId());
    jsonObj.put("start", entity.getStart());
    jsonObj.put("length", entity.getLength());
    jsonObj.put("negated", entity.isNegated());
    JSONArray evArray = new JSONArray();
    for (Ev ev: entity.getEvSet()) {
      evArray.put(evToJson(ev));
    }
    jsonObj.put("evlist", evArray);
    return jsonObj;
  }
  
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    JSONArray entityArray = new JSONArray();
    for (Entity entity: entityList) {
      entityArray.put(entityToJson(entity));
    }
    writer.println(entityArray.toString());
  }
  
  public String entityListFormatToString(List<Entity> entityList) {
    JSONArray entityArray = new JSONArray();
    for (Entity entity: entityList) {
      entityArray.put(entityToJson(entity));
    }
    return entityArray.toString();
  }

  public void initProperties(Properties properties) { 
  }
}
