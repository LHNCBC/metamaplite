
//
package irutils;

import java.util.Map;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import gov.nih.nlm.nls.utils.StringUtils;

/**
 *
 */

public class Config {

  /** Load list of tables and their configurations. 
   * @param configFilename configuration filename
   * @return map of key value pairs of configuration.
   * @throws NumberFormatException
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static Map<String,String []> loadConfig(String configFilename) 
    throws NumberFormatException, FileNotFoundException, IOException
  {
    Map<String,String []> tableMap = new HashMap<String, String []>();
    String line;

    BufferedReader reader = 
      new BufferedReader(new FileReader( configFilename ));
    // if ( (line = reader.readLine()) != null ) {
    //   int numTables = Integer.parseInt(StringUtils.getToken(line, " ", 1));
    // } else {
    //   // should throw an exception here...
    //   System.err.println("invalid config file, first line: \"NUM_TABLES: <n>\" missing");
    // }
	
    while ( (line = reader.readLine()) != null )
      {
	if ( line.trim().length() > 0 && line.charAt(0) != '#' ) {
	  String[] fields = line.split("\\|");
	  if (fields.length > 5) {
	    String tablename = fields[1];
	    tableMap.put(tablename, fields);
	  }
	}
      }
    reader.close();
    return tableMap;
  }

  /**
   * Save list of tables and their configurations to file.
   *
   * @param configFilename configuration filename
   * @param tableConfig cmap of key value pairs of configuration.
   * @throws IOException
   */
  public static void saveConfig(String configFilename, Map<String,String[]> tableConfig)
    throws IOException
  {
    PrintWriter out = new PrintWriter(new FileWriter(configFilename));
    for (Map.Entry<String,String[]> entry: tableConfig.entrySet()) {
      out.println(StringUtils.join(entry.getValue(), "|")) ;
    }
    out.close();
  }
}
