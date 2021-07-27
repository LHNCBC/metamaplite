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
 * Serialize contents of Full format in JSON.
 *
 * Created: Fri Feb 21 03:07:22 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Json implements ResultFormatter {

  /**
   * Convert ConceptInfo instance to JSON objects
   *
   * @param ci ConceptInfo instance to converted
   * @return nested Json object containing content of ConceptInfo instance.
   */
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
  
  /**
   * Convert Ev instance to JSON objects
   *
   * @param ev Ev instance to converted
   * @return nested Json object containing content of Ev instance.
   */
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
  
  /**
   * Convert Entity instance to JSON objects
   *
   * @param entity Entity instance to converted
   * @return nested Json object containing content of Entity instance.
   */
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
  
  /**
   * Print JSON object array representing EntityList instance
   *
   * @param writer PrintWriter instance to write JSON representation to.`
   * @param entityList Entity instance to converted
   */
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    JSONArray entityArray = new JSONArray();
    for (Entity entity: entityList) {
      entityArray.put(entityToJson(entity));
    }
    writer.println(entityArray.toString());
  }
  
  /**
   * Print JSON object array representing EntityList instance to String
   *
   * @param entityList Entity instance to converted
   * @return String containing nested Json array object containing content of EntityList.
   */
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
