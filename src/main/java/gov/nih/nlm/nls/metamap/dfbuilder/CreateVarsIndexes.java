package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.Charset;

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
 * CreateVarsIndexes - create variant indexes
 *
 *
 * Created: Thu Mar 16 17:08:45 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class CreateVarsIndexes {

  Charset charset = Charset.forName("utf-8");

  /**
   * Creates a new <code>CreateVarsIndexes</code> instance.
   *
   */
  public CreateVarsIndexes() {

  }
  
  static void createTables(String varsfilename, String ivfDir)
    throws Exception
  {
    System.out.println("Creating tables from:\nvars: " + varsfilename );
    String varsTableFilename = ivfDir + "/tables/vars.txt";
    if (Files.notExists(Paths.get(varsTableFilename))) {
      Files.copy(Paths.get(varsfilename), Paths.get(varsTableFilename),
		 StandardCopyOption.COPY_ATTRIBUTES);
    } else {
      System.out.println("Warning: " + varsTableFilename + " already exists, using existing file.");
    }
  }

  static Map<String,String[]> generateTableConfig(String ivfDir)
  {
    Map<String,String[]> tableConfig = new HashMap<String,String []>();
    tableConfig.put("vars",
		    "vars.txt|vars|7|0,2|term|tcat|word|wcat|varlevel|history||TXT|TXT|TXT|TXT|TXT|TXT|TXT".split("\\|"));
    return tableConfig;
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
      }
    } else {
      // create ivf directory and sub-directories
      ivfDir.mkdir();
      tablesDir.mkdir();
      indicesDir.mkdir();
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
   * <p>
   * usage: CreateIndexes {varsfile} {ivfdir}
   * @param args argument vector.
   * @throws FileNotFoundException file not found exception
   * @throws IOException IO exception
   * @throws BSPIndexCreateException index create exception 
   * @throws BSPIndexInvalidException invalid index exception
   * @throws ClassNotFoundException class not found exception
   * @throws Exception any exception
   */
  public static void main(String[] args)
    throws java.io.FileNotFoundException,
	   java.io.IOException, BSPIndexCreateException, BSPIndexInvalidException, 
	   ClassNotFoundException, Exception
  {
    if (args.length > 1) {
      String varsFile = args[0];
      String ivfDir = args[1];
      prepareDirectories(ivfDir);
      createTables(varsFile, ivfDir);
      String configFilename = ivfDir + "/tables/ifconfig";
      Map<String,String[]> tableConfig;
      if ((new File(configFilename)).exists()) {
	tableConfig = Config.loadConfig(configFilename);
	tableConfig.put("vars",
			"vars.txt|vars|7|0,2|term|tcat|word|wcat|varlevel|history||TXT|TXT|TXT|TXT|TXT|TXT|TXT".split("\\|"));
      } else {
	tableConfig = generateTableConfig(ivfDir);
      }
      Config.saveConfig(configFilename, tableConfig);
      createIndex(ivfDir, tableConfig, "vars", Charset.forName("utf-8"));
    } else {
      System.out.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.CreateVarsIndexes <vars-txt-fn> <ivfdir>");
    }
  }
}
