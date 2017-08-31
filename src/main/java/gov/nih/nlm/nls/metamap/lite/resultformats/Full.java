package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 * Describe class Full here.
 *
 *
 * Created: Fri Mar 10 09:48:22 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Full implements ResultFormatter {
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) { 
    for (Entity entity: entityList) {
      writer.println(entity);
    }
  }
  
  public String entityListFormatToString(List<Entity> entityList) {
    StringBuilder sb = new StringBuilder();
    for (Entity entity: entityList) {
      sb.append(entity.toString()).append("\n");
    }
    return sb.toString();
  }

  public void initProperties(Properties properties) { 
  }
}
