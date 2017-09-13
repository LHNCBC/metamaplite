package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
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

  public static Set<String> entityToStringSet(Entity entity){
    Set<String> cuiSet = new TreeSet<String>();
    for (Ev ev: entity.getEvSet()) {
      cuiSet.add(ev.getConceptInfo().getCUI());
    }
    return cuiSet;
  }

  public static void displayEntityList(PrintWriter pw, List<Entity> entityList) 
  {
    Collections.reverse(entityList);
    Set<String> stringSet = new TreeSet<String>();
    for (Entity entity: entityList) {
      if (entity.getEvSet().size() > 0) {
	stringSet.addAll(entityToStringSet(entity));
      }
    }
    for (String resultString: stringSet) {
      pw.print(resultString + "\n");
    }
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    displayEntityList(writer, entityList);
  }

  public String entityListFormatToString(List<Entity> entityList) {
    StringBuilder sb = new StringBuilder();
    Collections.reverse(entityList);
    Set<String> stringSet = new TreeSet<String>();
    for (Entity entity: entityList) {
      if (entity.getEvSet().size() > 0) {
	stringSet.addAll(entityToStringSet(entity));
      }
    }
    for (String resultString: stringSet) {
      sb.append(resultString).append("\n");
    }
    return sb.toString();
  }

  public void initProperties(Properties properties) {
  }

}
