
//
package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.security.NoSuchAlgorithmException;

import irutils.BSPIndexCreateException;
import irutils.BSPIndexInvalidException;
import irutils.MappedMultiKeyIndexGeneration;
import irutils.MappedMultiKeyIndex;
import irutils.MultiKeyIndexGeneration;
import irutils.MultiKeyIndex;
import irutils.MultiKeyIndex.Record;
import irutils.MultiKeyIndex.Extent;

/**
 * A program which generates three tables: cuiconcept.txt,
 * cuisourceinfo.txt, and cuist.txt and their associated inverted file
 * indexes.
 * <p>
 * Each file and its contents:
 * <dl>
 * <dt>cuiconcept.txt</dt>
 * <dd> contains a cui to preferred name mapping. </dd>
 * <dt>cuisourceinfo.txt</dt>
 * <dd> contains a cui to UMLS source mapping. </dd>
 * <dt>cuist.txt</dt>
 * <dd> contains a cui to semantic type mapping. </dd>
 * </dl>
 * Optional files
 * <dl>
 * <dt>mesh_tc_relaxed.txt</dt>
 * <dd> term to MeSH treecode file. </dd>
 * <dt>vars.txt</dt>
 * <dd> term to term variants file. </dd>
 * </dl>
 *
 */

public class CreateIndexes {

  String indexPath;
  String tablePath;


  /**
   *  Does file exist and is not empty?
   * @param filename name of file to be checked.
   * @return true if file exists and is not empty, false otherwise. 
   */
  public static boolean fileExistsAndIsNotEmpty(String filename) {
    File file = new File(filename);
    return (file.exists() && (file.length() > 0));
  }

  /** 
   * Skip message.
   * @param filename name of file
   */
  public static void skipMessage(String filename) {
    System.out.println("The table file " + filename + 
		       " exists and has a length greater than zero, skipping table generation for this file");
  }

  /**
   * Create MetaMapLite Tables from MRCONSO.RRF and MRSTY.RRF UMLS files
   *
   * @param mrconsofilename MRCONSO filename
   * @param mrstyfilename MRSTY filename
   * @param ivfDir inverted file directory
   * @throws Exception general exception
   */
  static void createTables(String mrconsofilename,
			   String mrstyfilename,
			   String mrsatfilename,
			   String ivfDir)
    throws Exception
  {
    System.out.println("Creating tables from:\nmrconso: " + mrconsofilename +
		       "\nmrsty: " + mrstyfilename);
    String cuiConceptFilename = ivfDir + "/tables/cuiconcept.txt";
    if (fileExistsAndIsNotEmpty(cuiConceptFilename)) {
      skipMessage(cuiConceptFilename);
    } else {
      ExtractMrconsoPreferredNames.createTable(mrconsofilename, cuiConceptFilename,
					       "ENG", true, "RRF");
    }
    String cuiSourceInfoFilename = ivfDir + "/tables/cuisourceinfo.txt";
    if (fileExistsAndIsNotEmpty(cuiSourceInfoFilename)) {
      skipMessage(cuiSourceInfoFilename);
    } else {
      ExtractMrconsoSources.createTable(mrconsofilename, cuiSourceInfoFilename,
					true, true, true, "RRF");
    }
    String cuiSemanticTypesFilename = ivfDir + "/tables/cuist.txt";
    if (fileExistsAndIsNotEmpty(cuiSemanticTypesFilename)) {
      skipMessage(cuiSemanticTypesFilename);
    } else {
      ExtractMrstySemanticTypes.createTable(mrstyfilename, cuiSemanticTypesFilename,
					    true, "RRF", null);
    }
    String meshTreecodesFilename = ivfDir + "/tables/mesh_tc_relaxed.txt";
    if (fileExistsAndIsNotEmpty(meshTreecodesFilename)) {
      skipMessage(meshTreecodesFilename);
    } else {
      ExtractTreecodes.process(mrconsofilename, mrsatfilename, meshTreecodesFilename);
    }

    System.gc();

    // add variant generation
    String varsfilename = ivfDir + "/tables/vars.txt";
    if (fileExistsAndIsNotEmpty(varsfilename)) {
      skipMessage(varsfilename);
    } else {
      GenerateVariants inst = new GenerateVariants();
      inst.process(mrconsofilename, varsfilename);
    }
  }

   /**
   * Generate table configuration
   *
   * @param ivfDir inverted file directory
   * @return map of dbname to associated configuration fields
   */
  public static Map<String,String[]> generateTableConfig(String ivfDir)
  {
    Map<String,String[]> tableConfig = new HashMap<String,String []>();
    String configFilename = ivfDir + "/tables/ifconfig";
    tableConfig.put("cuiconcept",
		    "cuiconcept.txt|cuiconcept|2|0,1|cui|concept|TXT|TXT".split("\\|"));
    tableConfig.put("cuisourceinfo",
		    "cuisourceinfo.txt|cuisourceinfo|6|0,1,3|cui|sui|i|str|src|tty|TXT|TXT|INT|TXT|TXT|TXT".split("\\|"));
    tableConfig.put("cuist",
		    "cuist.txt|cuist|2|0|cui|st|TXT|TXT".split("\\|"));
    tableConfig.put("meshtcrelaxed",
		    "mesh_tc_relaxed.txt|meshtcrelaxed|2|0,1|mesh|tc|TXT|TXT".split("\\|"));
    tableConfig.put("vars",
		    "vars.txt|vars|7|0,2|term|tcat|word|wcat|varlevel|history||TXT|TXT|TXT|TXT|TXT|TXT|TXT".split("\\|"));
    return tableConfig;
  }

  /**
   * Join elements of stringarray into string.
   * @param stringArray string array
   * @param joinstring string to separate components by in composed string.
   * @return joined string representation of string array.
   */
  static String join(String[] stringArray, String joinstring) {
    StringBuilder sb = new StringBuilder();
    sb.append(stringArray[0]);
    for (int i=1; i<stringArray.length; i++) {
      sb.append(joinstring).append(stringArray[i]);
    }
    return sb.toString();
  }

   /**
   * Save table configuration file
   *
   * @param configFilename name of configuration file
   * @param tableConfig map of dbname to associated configuration fields
   * @throws IOException i/o exception
   */
  static void saveTableConfig(String configFilename, Map<String,String[]> tableConfig)
    throws IOException
  {
    PrintWriter out = new PrintWriter(new FileWriter(configFilename));
    for (Map.Entry<String,String[]> entry: tableConfig.entrySet()) {
      out.println(join(entry.getValue(), "|")) ;
    }
    out.close();
  }

  /**
   * Prepare directories
   * @param ivfdir inverted file directory
   */
  static void prepareDirectories(String ivfdir)
  {
    File ivfDir = new File(ivfdir);
    File tablesDir = new File(ivfdir + "/tables");
    File indicesDir = new File(ivfdir + "/indices");
    File cuiconceptDir = new File(ivfdir + "/indices/cuiconcept");
    File cuisourceinfoDir = new File(ivfdir + "/indices/cuisourceinfo");
    File cuistDir = new File(ivfdir + "/indices/cuist");
    File meshtcrelaxedDir = new File(ivfdir + "/indices/meshtcrelaxed");
    File varsDir = new File(ivfdir + "/indices/vars");
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
      cuiconceptDir.mkdir();
      cuisourceinfoDir.mkdir();
      cuistDir.mkdir();
      meshtcrelaxedDir.mkdir();
      varsDir.mkdir();
    } else {
      // create ivf directory and sub-directories
      ivfDir.mkdir();
      tablesDir.mkdir();
      indicesDir.mkdir();

      cuiconceptDir.mkdir();
      cuisourceinfoDir.mkdir();
      cuistDir.mkdir();
      meshtcrelaxedDir.mkdir();
      varsDir.mkdir();

    }
  }

   /**
   * Create Indices
   *
   * @param ivfDir inverted file directory
   * @param tableConfig map of dbname to associated configuration fields
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   * @throws NoSuchAlgorithmException no such algorithm exception

   */
  static void createIndices(String ivfDir, Map<String,String[]> tableConfig, Charset charset)
    throws FileNotFoundException, IOException, NoSuchAlgorithmException
  {
    for (Map.Entry<String,String[]> entry: tableConfig.entrySet()) {
      String workingDir = ivfDir;
      String indexName = entry.getKey();
      String[] tableFields = entry.getValue();
      String tableFilename = tableFields[0];
      // get specified columns from table entry
      String[] columnStrings = tableFields[3].split(",");
      int columns[] = new int[columnStrings.length];
      for (int i = 0; i < columnStrings.length; i++) {
	columns[i] = Integer.parseInt(columnStrings[i]);
      }
      
      String absTableFilename = workingDir + "/tables/" + tableFilename;
      if (new File(absTableFilename).exists()) {
	System.out.println("loading table for " + indexName + " from file: " + tableFilename + ".");
	List<MultiKeyIndex.Record> recordTable = MultiKeyIndex.loadTable(absTableFilename, charset);
	System.out.println("Generating index for " + indexName);
	MultiKeyIndexGeneration instance = new MultiKeyIndexGeneration();
	System.out.println("Generating maps for columns " +
			   MultiKeyIndexGeneration.renderColumns(columns) ); 
	instance.generateMaps(recordTable, columns);
	Map<String,Extent> digestExtentMap = instance.writePostings(workingDir, indexName);
	instance.writePartitions(workingDir, indexName, digestExtentMap);
	System.gc();
      } else {
	System.out.println("warning: table for " + indexName + " from file: " + tableFilename + " is not present, skipping table.");
      }
    }
  }

  /**
   * main program 
   * <p>
   * usage: CreateIndexes {mrconsofile} {mrstyfile} {ivfdir}
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
    Charset charset = Charset.forName("utf-8");
    if (args.length > 2) {
      String mrconsoFile = args[0];
      String mrstyFile = args[1];
      String mrsatFile = args[2];
      String ivfDir = args[3];
      prepareDirectories(ivfDir);
      createTables(mrconsoFile, mrstyFile, mrsatFile, ivfDir);
      Map<String,String[]> tableConfig = generateTableConfig(ivfDir);
      saveTableConfig(ivfDir + "/tables/ifconfig", tableConfig);
      createIndices(ivfDir, tableConfig, charset);
    } else {
      System.out.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>");
    }
  }
}
