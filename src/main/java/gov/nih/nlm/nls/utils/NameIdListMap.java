package gov.nih.nlm.nls.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * NameIdMap - class for handling maps indexing ids by name.
 *
 * Created: Fri Apr 17 16:19:29 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class NameIdListMap {

  static Charset charset = Charset.forName("utf-8");

  /**
   * Creates a new <code>NameIdListMap</code> instance.
   *
   */
  public NameIdListMap() {

  }

  public NameIdListMap(Charset aCharset) {
    charset = aCharset;
  }

  /**
   * Load file of id, name pairs in to map keyed by name.  A name can
   * refer to multiple ids.
   *
   * <pre>
   * id0|name
   * id1|name
   * </pre>
   * @param filename
   * @return map of ids keyed by name/term 
   */
  public static Map<String,List<String>> loadNameIdListMap(String filename) {
    try {
      Map<String,List<String>> nameIdListMap = new HashMap<String,List<String>>();
      BufferedReader br = new BufferedReader
	(new InputStreamReader
	 (new FileInputStream(filename), charset));
      String line;
      while ((line = br.readLine()) != null) {
	String[] fields = line.split("\\|");
	String key = fields[1].toLowerCase();
	String id = fields[0];
	if (nameIdListMap.containsKey(key)) {
	  nameIdListMap.get(key).add(id);
	} else {
	  List<String> idList = new ArrayList<String>();
	  idList.add(id);
	  nameIdListMap.put(key, idList);
	}
      }
      br.close();
      return nameIdListMap;
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
