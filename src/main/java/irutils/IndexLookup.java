package irutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.security.NoSuchAlgorithmException;

import irutils.BSPIndexCreateException;
import irutils.BSPIndexInvalidException;
import irutils.MappedMultiKeyIndex;
import irutils.MappedMultiKeyIndexLookup;
import irutils.MultiKeyIndex;
import irutils.MultiKeyIndex.Record;
import irutils.MultiKeyIndex.Extent;
import irutils.Config;
import gov.nih.nlm.nls.utils.StringUtils;

/**
 * IndexLookup - lookup a term in an existing ivf index.
 *
 *
 * Created: Wed Apr  5 16:20:17 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class IndexLookup {

  /**
   * Creates a new <code>IndexLookup</code> instance.
   *
   */
  public IndexLookup() {
  }

  /**
   *
   *
   * @param ivfDir inverted file 
   * @param tableConfig map of indexes and their configuration
   * @param indexName  name of index to search 
   * @param column column to search
   * @param query query terms
   * @return results of lookup one result per string in list.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws NoSuchAlgorithmException no such algorithm exception 
   */
  static List<String> lookup(String ivfDir, Map<String,String[]> tableConfig, String indexName, int column, String query)
    throws FileNotFoundException, IOException, NoSuchAlgorithmException
  {
    String[] tableFields = tableConfig.get(indexName);
    String workingDir = ivfDir;
    String tableFilename = tableFields[0];
    // get specified columns from table entry
    String[] columnStrings = tableFields[3].split(",");
    int columns[] = new int[columnStrings.length];
    for (int i = 0; i < columnStrings.length; i++) {
      columns[i] = Integer.parseInt(columnStrings[i]);
    }
    MappedMultiKeyIndexLookup index = new MappedMultiKeyIndexLookup(new MappedMultiKeyIndex(ivfDir + "/indices/" + indexName));
    return index.lookup(query, column);
  }
  

  /**
   * main program 
   *
   * usage: java gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex ivfdir indexname column query terms
   *
   *
   * @param args argument vector.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws BSPIndexCreateException index create exception 
   * @throws BSPIndexInvalidException invalid index exception
   * @throws ClassNotFoundException class not found exception
   * @throws Exception any exception
   */
  public static final void main(final String[] args)
        throws java.io.FileNotFoundException,
	   java.io.IOException, BSPIndexCreateException, BSPIndexInvalidException, 
	   ClassNotFoundException, Exception
  {
    if (args.length > 2) {
      String ivfDir = args[0];
      String indexname = args[1];
      int column = Integer.parseInt(args[2]);
      StringBuilder sb = new StringBuilder();
      for (int i = 3; i < args.length; i++) {
	sb.append(args[i])
	  .append(" ");
      }
      String query = sb.toString().trim();
      Map<String,String[]> tableConfig;
      String configFilename = ivfDir + "/tables/ifconfig";
      if ((new File(configFilename)).exists()) {
	tableConfig = Config.loadConfig(configFilename);
	if (tableConfig.containsKey(indexname)) {
	  int i = 0;
	  for (String hit: lookup(ivfDir, tableConfig, indexname, column, query)) {
	    System.out.println(i + ": " + hit);
	    i++;
	  }
	} else {
	  System.err.println("Error: An entry for index named: " + indexname + " is not present in " + configFilename);
	}
      } else {
	System.err.println("Error: " + configFilename + " does not exist.");
      }
    } else {
      System.err.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.IndexLookup <ivfdir> <indexname> <column> <query terms>");
    }
  }

}
