
//
package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.PrintWriter;
import java.util.List;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public interface ResultFormatter {
  void entityListFormatter(PrintWriter writer,
			   List<Entity> entityList);
}
