package irutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import java.nio.MappedByteBuffer;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.security.NoSuchAlgorithmException;

import irutils.MultiKeyIndex.Extent;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

/**
 * TestMultiKeyIndexGeneration - Test MultiKeyIndexGeneration by creating
 * a table, generating the indexes and testing the indexes using
 * lookup with binary search.
 *
 * Created: Fri Nov 16 16:19:55 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class TestMultiKeyIndexGeneration {

  Path dirPath;
  Charset charset = Charset.forName("utf-8");

  /** test configuration */
  String[] configArray = {
    "cuisourceinfo.txt|cuisourceinfo|6|0,1,3|cui|sui|i|str|src|tty|TXT|TXT|INT|TXT|TXT|TXT"
  };

  /** test table */
  String[] recordArray = {
    "C0000039|S0033298|16|Dipalmitoylphosphatidylcholine|SNOMEDCT_US|OAP",
    "C0000039|S3260062|25|Dipalmitoylphosphatidylcholine (substance)|SNOMEDCT_US|OAF",
    "C0000052|S0575717|8|1,4-alpha-Glucan branching enzyme|SNOMEDCT_US|PT",
    "C0000052|S0604824|17|Branching enzyme|SNOMEDCT_US|SY",
    "C0000052|S0589116|29|Amylo-(1,4,6)-transglycosylase|SNOMEDCT_US|SY",
    "C0000052|S3435526|30|Amylo-(1,4->,6)-transglycosylase|SNOMEDCT_US|IS",
    "C0000052|S3293422|33|1,4-alpha-Glucan branching enzyme (substance)|SNOMEDCT_US|FN",
    "C0000097|S1234728|16|Methylphenyltetrahydropyridine|SNOMEDCT_US|PT",
    "C0000097|S3414899|18|Methylphenyltetrahydropyridine (substance)|SNOMEDCT_US|FN",
    "C0000102|S0575892|21|1-Naththylamine|SNOMEDCT_US|PT",
    "C0000102|S0763349|23|a- Naphthylamine|SNOMEDCT_US|SY",
    "C0000102|S3300955|25|1-Naththylamine (substance)|SNOMEDCT_US|FN",
    "C0000163|S0007806|7|17-Hydroxycorticosteroids|SNOMEDCT_US|SY",
    "C0000163|S0001456|12|17-Hydroxycorticosteroid|SNOMEDCT_US|OAP",
    "C0000163|S0001456|13|17-Hydroxycorticosteroid|SNOMEDCT_US|PT",
    "C0000163|S3359313|17|17-Hydrocorticosteroid|SNOMEDCT_US|SY",
    "C0000163|S3359408|21|17-Hydroxycorticoid|SNOMEDCT_US|SY",
    "C0000163|S0576421|23|17-Hydroxycorticoids, NOS|SNOMEDCT_US|IS",
    "C0000163|S0576430|29|17-Ketogenic steroids|SNOMEDCT_US|IS",
    "C0000163|S0576431|31|17-Ketogenic steroids, NOS|SNOMEDCT_US|IS",
    "C0000163|S3360684|34|17-Ketogenic steroid|SNOMEDCT_US|OAP",
    "C0000163|S0783689|36|17-Oxogenic steroids|SNOMEDCT_US|SY",
    "C0000163|S3361788|37|17-Oxogenic steroid|SNOMEDCT_US|OAS",
    "C0000163|S1257867|38|17KGS - 17-Ketogenic steroids|SNOMEDCT_US|IS",
    "C0000163|S3359510|40|17-Hydroxycorticosteroid -RETIRED-|SNOMEDCT_US|IS",
    "C0000163|S3359510|41|17-Hydroxycorticosteroid -RETIRED-|SNOMEDCT_US|OF",
    "C0000163|S3359458|42|17-Hydroxycorticosteroid (substance)|SNOMEDCT_US|OAF",
    "C0000163|S3359458|43|17-Hydroxycorticosteroid (substance)|SNOMEDCT_US|FN",
    "C0000163|S3360310|44|17-Ketogenic steroid (substance)|SNOMEDCT_US|OAF",
    "C0000167|S0007811|6|17-Ketosteroids|SNOMEDCT_US|OAS",
    "C0000167|S0576432|14|17-Ketosteroids, NOS|SNOMEDCT_US|IS",
    "C0000167|S3361366|18|17-Ketosteroid|SNOMEDCT_US|PT",
    "C0000167|S0007813|20|17-Oxosteroids|SNOMEDCT_US|IS",
    "C0000167|S6688366|24|17-oxosteroids|SNOMEDCT_US|OAS",
    "C0000167|S6688363|26|17-Oxosteroid|SNOMEDCT_US|OAP",
    "C0000167|S6688363|27|17-Oxosteroid|SNOMEDCT_US|SY",
    "C0000167|S3360743|28|17-Ketosteroid (substance)|SNOMEDCT_US|FN",
    "C0000167|S6688358|29|17-Oxosteroid (substance)|SNOMEDCT_US|OAF",
    "C0000167|S3361851|30|17-Oxosteroids (substance)|SNOMEDCT_US|OF",
    "C0000172|S0007830|7|18-Hydroxycorticosterone|SNOMEDCT_US|PT",
    "C0000172|S3323976|20|11-beta,18,21-Trihydroxypregn-4-ene-3,20-dione|SNOMEDCT_US|SY",
    "C1527336|S0404490|40|Sjogren's syndrome|SNOMEDCT_US|SY",
    "C1527336|S0735709|57|Sjogrens syndrome|SNOMEDCT_US|SY",
    "C1527336|S11318750|61|Sjögren syndrome|SNOMEDCT_US|SY",
    "C1527336|S3544925|71|Sjögren's syndrome|SNOMEDCT_US|PT",
    "C1527336|S3544923|79|Sjögren's disease|SNOMEDCT_US|SY",
    "C1527336|S0511229|81|Sjogren's disease|SNOMEDCT_US|IS",
    "C1527336|S0511229|82|Sjogren's disease|SNOMEDCT_US|OAS",
    "C1527336|S0511229|83|Sjogren's disease|SNOMEDCT_US|OAS",
    "C1527336|S3544924|93|Sjögren's syndrome (disorder)|SNOMEDCT_US|FN"
  };

  /**
   * Creates a new <code>TestMultiKeyIndexGeneration</code> instance.
   *
   */
  public TestMultiKeyIndexGeneration() {
    System.setProperty("file.encoding","UTF-8");
  }

  public static String renderColumns(int[] columns) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(columns[0]);
    for (int i = 1; i<columns.length; i++) {
      sb.append(",").append(columns[i]);
    }
    sb.append("]");
    return sb.toString();
  }


  // build index using a nominal termlist
  void build(String workingDir, String indexName)
    throws FileNotFoundException, IOException, NoSuchAlgorithmException {
    Map<String,String []> tableConfig = Config.loadConfig(workingDir + "/tables/ifconfig");
    String[] tableFields = tableConfig.get(indexName);
    if (tableFields != null) {
      String tableFilename = tableFields[0];
      // get specified columns from table entry
      String[] columnStrings = tableFields[3].split(",");
      int columns[] = new int[columnStrings.length];
      for (int i = 0; i < columnStrings.length; i++) {
	columns[i] = Integer.parseInt(columnStrings[i]);
      }
      System.out.println("loading table for " + indexName + " from file: " + tableFilename + ".");
      List<MultiKeyIndex.Record> recordTable = MultiKeyIndex.loadTable(workingDir + "/tables/" + tableFilename, this.charset);
      MultiKeyIndexGeneration instance = new MultiKeyIndexGeneration();
      System.out.println("Generating maps for columns " + renderColumns(columns) ); 
      instance.generateMaps(recordTable, columns);
      Map<String,Extent> digestExtentMap = instance.writePostings(workingDir, indexName);
      instance.writePartitions(workingDir, indexName, digestExtentMap);
    } else {
      System.out.println("table entry for index " + indexName + " is not present in configuration file: ifconfig.");
    } 
  }

  /**
   * Create test table and then build test index on disk
   */
  @org.junit.Before public void setup() {
    try {
      String workingdir = "/tmp/testivf";
      // create working directory in a temporary directory
      this.dirPath = FileSystems.getDefault().getPath(workingdir);
      Path tablePath = FileSystems.getDefault().getPath(workingdir + "/tables");
      Path indexPath = FileSystems.getDefault().getPath(workingdir + "/indices");
      Path cuisrcIndexPath = FileSystems.getDefault().getPath(workingdir + "/indices/cuisourceinfo");
      // delete working directory if it already exists
      if (Files.exists(this.dirPath)) {
	Files.walk(this.dirPath)
	  .sorted(Comparator.reverseOrder())
	  .map(Path::toFile)
	  .peek(System.out::println)
	  .forEach(File::delete);
      }
      if (! Files.exists(dirPath)) {
	Files.createDirectory(dirPath);
      }
      if (! Files.exists(tablePath)) {
	Files.createDirectory(tablePath);
      }
      if (! Files.exists(indexPath)) {
	Files.createDirectory(indexPath);
      }
      if (! Files.exists(cuisrcIndexPath)) {
	Files.createDirectory(cuisrcIndexPath);
      }
      // write table to file
      String tableFilename = workingdir + "/tables/cuisourceinfo.txt";
      PrintWriter tableFile = 
	new PrintWriter
	(new OutputStreamWriter(new FileOutputStream(tableFilename),
				this.charset));
      for (String line: recordArray) {
	tableFile.write(line + "\n");
      }
      tableFile.close();
      // write config to file
      String configFilename = workingdir + "/tables/ifconfig";
      PrintWriter configFile = 
	new PrintWriter(new FileWriter(configFilename));
      for (String line: configArray) {
	configFile.write(line + "\n");
      }
      configFile.close();

      // build index 
      build(workingdir, "cuisourceinfo");

    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (NoSuchAlgorithmException nsae) {
      throw new RuntimeException(nsae);
    }
  }  

  @org.junit.Test public void test1() {
    String workingdir = "/tmp/testivf";
    String indexname = "cuisourceinfo";
    String[] sample_array = new String[]
      { "Sjogren's disease",	
	"Sjögren's disease",
	"Sjögren's syndrome",
	"Sjogren's syndrome",
	"Dipalmitoylphosphatidylcholine",
	"17-Ketosteroids",
	"Sjögren's syndrome (disorder)"
      };
    int column = 3;
    try {
      for (String sample: sample_array) {
	System.out.println("sample: " + sample);
	int termlength = sample.getBytes(this.charset).length;
	System.out.println("TestMultiKeyIndexGeneration: test1: termlength: " + termlength);
	String statsfn =
	  MappedMultiKeyIndex.partitionPath(workingdir, indexname,
					    Integer.toString(column), Integer.toString(termlength), "-term-dictionary-stats.txt");
	Map<String,String> statsMap = MappedMultiKeyIndex.readStatsFile(statsfn);
	int recordnum = Integer.parseInt(statsMap.get("recordnum"));
	System.out.println("TestMultiKeyIndexGeneration: test1: recordnum: " + recordnum);
	int reclength = Integer.parseInt(statsMap.get("reclength"));
	System.out.println("TestMultiKeyIndexGeneration: test1: reclength: " + reclength);
	int datalength = Integer.parseInt(statsMap.get("datalength"));
	System.out.println("TestMultiKeyIndexGeneration: test1: datalength: " + datalength);
	String dirfn = 
	  MappedMultiKeyIndex.partitionPath(workingdir, indexname,
					    Integer.toString(column), Integer.toString(termlength), "-term-dictionary");

	RandomAccessFile dirRaf = new RandomAccessFile(dirfn, "r");
	byte[] buf = new byte[termlength];
	for (int i = 0; i<recordnum; i++) {
	  int byteread = dirRaf.read(buf);
	  String term = new String(buf, this.charset);
	  System.out.println("TestMultiKeyIndexGeneration: test1: term: " + term);
	  dirRaf.skipBytes(datalength);
	}
	System.out.println("---");
      }
      org.junit.Assert.assertTrue(true);	
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @org.junit.Test public void test2() {
    String workingdir = "/tmp/testivf";
    String indexname = "cuisourceinfo";
    int column = 3;
    try {
      MappedMultiKeyIndex index = new MappedMultiKeyIndex(workingdir, indexname);
      MappedMultiKeyIndexLookup instance = new MappedMultiKeyIndexLookup(index);

      for (String term: new String[] { "Sjögren's disease" }) {
	List<String> resultList = instance.lookup(term.toLowerCase(), column);
	System.out.println("TestMultKeyIndexGeneration: test2: result: " + resultList);
	org.junit.Assert.assertTrue(resultList.size() > 0);
	org.junit.Assert.assertTrue(true);	
      }
      for (Map.Entry<String,Map<String,String>> entry: index.mapOfStatMaps.entrySet()) {
	System.out.println("TestMultiKeyIndexGeneration: test2: mapOfStatMaps: " + entry.getKey());
      }
      for (Map.Entry<String,MappedByteBuffer> entry: index.byteBufCache.entrySet()) {
	System.out.println("TestMultiKeyIndexGeneration: test2: byteBufCache: " + entry.getKey());
      }
      for (Map.Entry<String,MappedByteBuffer> entry: index.mapOfTermDictionaryRafs.entrySet()) {
	System.out.println("TestMultiKeyIndexGeneration: test2: mapOftermdictionaryrafs" + entry.getKey());
      }
      
      org.junit.Assert.assertTrue(true);	
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @org.junit.Test public void testBinarySearch() {
    // attempt to find terms using binary search
    String[] termList = new String[] { "Sjögren's disease" };
    String workingdir = "/tmp/testivf";
    String indexname = "cuisourceinfo";
    int column = 3;
    try {
      MappedMultiKeyIndex index = new MappedMultiKeyIndex(workingdir, indexname);
      MappedMultiKeyIndexLookup instance = new MappedMultiKeyIndexLookup(index);
      for (String term: new String[] { "Sjögren's disease" }) {
	List<String> resultList = instance.lookup(term.toLowerCase(), column);
	System.out.println("TestMultKeyIndexGeneration:result: " + resultList);
	org.junit.Assert.assertTrue(resultList.size() > 0);
      }
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  // @org.junit.After public void cleanUp() {
  //   try {
  //     // delete working directory if it exists.
  //     if (Files.exists(this.dirPath)) {
  // 	Files.walk(this.dirPath)
  // 	  .sorted(Comparator.reverseOrder())
  // 	  .map(Path::toFile)
  // 	  .peek(System.out::println)
  // 	  .forEach(File::delete);
  //     }
  //   } catch (FileNotFoundException fnfe) {
  //     throw new RuntimeException(fnfe);
  //   } catch (IOException ioe) {
  //     throw new RuntimeException(ioe);
  //   }
  // }

}
