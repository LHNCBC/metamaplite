package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;

/**
 * CUI List Output 
 *
 * Output is UMLS Concept Unique Indentifiers (CUIs), one CUI per line.
 */

public class CuiList implements ResultFormatter {

  public static String entityToString(Entity entity){
    StringBuilder sb = new StringBuilder();
    for (Ev ev: entity.getEvSet()) {
      sb.append(ev.getConceptInfo().getCUI()).append('\n');
    }
    return sb.toString();
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

  public String entityListFormatToString(List<Entity> entityList) {
    return null;
  }

  public void initProperties(Properties properties) {
  }

}
