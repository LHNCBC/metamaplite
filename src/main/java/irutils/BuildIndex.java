package irutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
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
import irutils.MappedMultiKeyIndexGeneration;
import irutils.MappedMultiKeyIndex;
import irutils.MappedMultiKeyIndexDiskBasedGeneration;
import irutils.MultiKeyIndex;
import irutils.MultiKeyIndex.Record;
import irutils.MultiKeyIndex.Extent;
import irutils.Config;
import gov.nih.nlm.nls.utils.StringUtils;

/**
 * BuildIndex build a single index
 *
 *
 * Created: Tue Apr  4 11:30:50 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class BuildIndex {

  /**
   * Creates a new <code>BuildIndex</code> instance.
   *
   */
  public BuildIndex() {

  }

  static void prepareDirectories(String ivfdir)
  {
    File ivfDir = new File(ivfdir);
    File tablesDir = new File(ivfdir + "/tables");
    File indicesDir = new File(ivfdir + "/indices");
    File cuiconceptDir = new File(ivfdir + "/indices/cuiconcept");
    File cuisourceinfoDir = new File(ivfdir + "/indices/cuisourceinfo");
    File cuistDir = new File(ivfdir + "/indices/cuist");
    System.out.println("Preparing directories\n workingDir: " + ivfDir +
		       "\n tablesDir: " + tablesDir +
		       "\n indicesDir: " + indicesDir + "\n");
    if (ivfDir.exists()) {
      // if ivfdir exists then...
      // check if tables directory exists
      if (! tablesDir.exists()) {
	tablesDir.mkdir();
      }
      // check if indices directories exist
      if (! indicesDir.exists()) {
	indicesDir.mkdir();
	cuiconceptDir.mkdir();
	cuisourceinfoDir.mkdir();
	cuistDir.mkdir();
      } else {
	if (! cuiconceptDir.exists()) {
	  cuiconceptDir.mkdir();
	  cuisourceinfoDir.mkdir();
	  cuistDir.mkdir();
	}
      }
    } else {
      // create ivf directory and sub-directories
      ivfDir.mkdir();
      tablesDir.mkdir();
      indicesDir.mkdir();
      cuiconceptDir.mkdir();
      cuisourceinfoDir.mkdir();
      cuistDir.mkdir();
    }
  }

  static void createIndex(String ivfDir, Map<String,String[]> tableConfig, String indexName, Charset charset)
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
    
    System.out.println("loading table for " + indexName + " from file: " + tableFilename + ".");
    List<Record> recordTable = MultiKeyIndex.loadTable(workingDir + "/tables/" + tableFilename, charset);
    MappedMultiKeyIndexDiskBasedGeneration instance = new MappedMultiKeyIndexDiskBasedGeneration();
    System.out.println("writing partitions for columns " +
		       MappedMultiKeyIndexDiskBasedGeneration.renderColumns(columns) ); 
    Set<String> columnLengthKeys =
      MappedMultiKeyIndexDiskBasedGeneration.writeTemporaryPartitionsTables(workingDir, indexName, recordTable, columns, charset);
    System.out.println("writing final index");
    MappedMultiKeyIndexDiskBasedGeneration.writeFinalIndex(workingDir, indexName, columnLengthKeys, charset);
  }

  /**
   * main program 
   *
   * usage: java gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex ivfdir indexname
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
    Charset charset = Charset.forName("utf-8");
    if (args.length > 1) {
      String ivfDir = args[0];
      String indexname = args[1];
      prepareDirectories(ivfDir);
      Map<String,String[]> tableConfig;
      String configFilename = ivfDir + "/tables/ifconfig";
      if ((new File(configFilename)).exists()) {
	tableConfig = Config.loadConfig(configFilename);
	if (tableConfig.containsKey(indexname)) {
	  System.out.println("Building index " + indexname + ":" + StringUtils.join(tableConfig.get(indexname), " "));
	  createIndex(ivfDir, tableConfig, indexname, charset);
	} else {
	  System.err.println("Error: An entry for index named: " + indexname + " is not present in " + configFilename);
	}
      }
    } else {
      System.err.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex <ivfdir> <indexname>");
    }
  }

}

