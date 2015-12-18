
//
package irutils;

import java.util.Map;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import utils.StringUtils;

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
    if ( (line = reader.readLine()) != null ) {
      int numTables = Integer.parseInt(StringUtils.getToken(line, " ", 1));
    } else {
      // should throw an exception here...
      System.err.println("invalid config file, first line: \"NUM_TABLES: <n>\" missing");
    }
	
    while ( (line = reader.readLine()) != null )
      {
	if ( line.length() > 0 && line.charAt(0) != '#' ) {
	  String[] fields = line.split("\\|");
	  String tablename = fields[1];
	  tableMap.put(tablename, fields);
	}
      }
    reader.close();
    return tableMap;
  }
  
}
