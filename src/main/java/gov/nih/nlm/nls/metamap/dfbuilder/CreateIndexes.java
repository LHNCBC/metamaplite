
//
package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
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
 *
 */

public class CreateIndexes {

  String indexPath;
  String tablePath;


  static void createTables(String mrconsofilename, String mrstyfilename, String ivfDir)
    throws Exception
  {
    System.out.println("Creating tables from:\nmrconso: " + mrconsofilename +
		       "\nmrsty: " + mrstyfilename);
    String cuiConceptFilename = ivfDir + "/tables/cuiconcept.txt";
    ExtractMrconsoPreferredNames.createTable(mrconsofilename, cuiConceptFilename,
					     "ENG", true, "RRF");
    String cuiSourceInfoFilename = ivfDir + "/tables/cuisourceinfo.txt";
    ExtractMrconsoSources.createTable(mrconsofilename, cuiSourceInfoFilename,
				      true, true, true, "RRF");
    String cuiSemanticTypesFilename = ivfDir + "/tables/cuist.txt";
    ExtractMrstySemanticTypes.createTable(mrstyfilename, cuiSemanticTypesFilename,
					  true, "RRF", null);
  }

  static Map<String,String[]> generateTableConfig(String ivfDir)
  {
    Map<String,String[]> tableConfig = new HashMap<String,String []>();
    String configFilename = ivfDir + "/tables/ifconfig";
    tableConfig.put("cuiconcept",
		    "cuiconcept.txt|cuiconcept|2|0,1|cui|concept|TXT|TXT".split("\\|"));
    tableConfig.put("cuisourceinfo",
		    "cuisourceinfo.txt|cuisourceinfo|6|0,1,3|cui|sui|i|str|src|tty|TXT|TXT|INT|TXT|TXT|TXT".split("\\|"));
    tableConfig.put("cuist",
		    "cuist.txt|cuist|2|0|cui|st|TXT|TXT".split("\\|"));
    return tableConfig;
  }

  static String join(String[] stringArray, String delimiter) {
    StringBuilder sb = new StringBuilder();
    sb.append(stringArray[0]);
    for (int i=1; i<stringArray.length; i++) {
      sb.append(delimiter).append(stringArray[i]);
    }
    return sb.toString();
  }
  
  static void saveTableConfig(String configFilename, Map<String,String[]> tableConfig)
    throws IOException
  {
    PrintWriter out = new PrintWriter(new FileWriter(configFilename));
    for (Map.Entry<String,String[]> entry: tableConfig.entrySet()) {
      out.println(join(entry.getValue(), "|")) ;
    }
    out.close();
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

  static void createIndices(String ivfDir, Map<String,String[]> tableConfig)
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
      
      System.out.println("loading table for " + indexName + " from file: " + tableFilename + ".");
      List<MultiKeyIndex.Record> recordTable = MultiKeyIndex.loadTable(workingDir + "/tables/" + tableFilename);
      System.out.println("Generating index for " + indexName);
      MultiKeyIndexGeneration instance = new MultiKeyIndexGeneration();
      System.out.println("Generating maps for columns " +
			 MultiKeyIndexGeneration.renderColumns(columns) ); 
      instance.generateMaps(recordTable, columns);
      Map<String,Extent> digestExtentMap = instance.writePostings(workingDir, indexName);
      instance.writePartitions(workingDir, indexName, digestExtentMap);
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
    if (args.length > 2) {
      String mrconsoFile = args[0];
      String mrstyFile = args[1];
      String ivfDir = args[2];
      prepareDirectories(ivfDir);
      createTables(mrconsoFile, mrstyFile, ivfDir);
      Map<String,String[]> tableConfig = generateTableConfig(ivfDir);
      saveTableConfig(ivfDir + "/tables/ifconfig", tableConfig);
      createIndices(ivfDir, tableConfig);
    } else {
      System.out.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>");
    }
  }
}
