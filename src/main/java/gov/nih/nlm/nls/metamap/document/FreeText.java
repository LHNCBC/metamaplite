
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Unstructured text.
 */

public class FreeText implements Document {
  String text;
  public FreeText(String text) { this.text = text; }
  public String getText() {
    return this.text;
  }

  public static String loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }


}
