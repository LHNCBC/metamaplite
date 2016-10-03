package irutils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.nio.MappedByteBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import irutils.MultiKeyIndex.Record;


/**
 * Usage:
 * <pre>
 * MappedMultiKeyIndexLookup cuiSourceInfoIndex =
 *   new MappedMultiKeyIndexLookup
 *      (new MappedMultiKeyIndex
 *       (properties.getProperty("metamaplite.ivf.cuisourceinfoindex", 
 *           root + "/cuisourceinfo")));
 * List&lt;String&gt; result = cuiSourceInfoIndex.lookup("dipalmitoylphosphatidylcholine",3);
 * for (String record: result) {
 *    System.out.println(record);
 * }
 * </pre>
 * output:
 * <pre>
 *  C0000039|S0033298|7|Dipalmitoylphosphatidylcholine|LNC|CN
 *  C0000039|S0033298|6|Dipalmitoylphosphatidylcholine|SNOMEDCT_US|OAP
 *  C0000039|S0033298|5|Dipalmitoylphosphatidylcholine|NDFRT|SY
 *  C0000039|S0033298|4|Dipalmitoylphosphatidylcholine|SNMI|PT
 * </pre>
 */

public class MappedMultiKeyIndexLookup {

  MappedMultiKeyIndex index;

  public MappedMultiKeyIndexLookup(String indexDirectoryName)
    throws FileNotFoundException, IOException
  {
    this.index = new MappedMultiKeyIndex(indexDirectoryName);
  }

  public MappedMultiKeyIndexLookup(MappedMultiKeyIndex index) {
    this.index = index;
  }

  public List<String> lookup(String term, int column)
    throws IOException, FileNotFoundException
  {
    List<String> resultList = new ArrayList<String>();
    String termLengthString = Integer.toString(term.length());
    String columnString = Integer.toString(column);

    MappedByteBuffer termDictionaryRaf = this.index.getTermDictionaryFile(columnString, termLengthString);
    if (termDictionaryRaf != null) {
      MappedByteBuffer extentsRaf = this.index.getExtentsFile(columnString, termLengthString);
      MappedByteBuffer postingsRaf = this.index.getPostingsFile();
      Map<String,String> statsMap = this.index.getStatsMap(columnString, termLengthString);
      int datalength = Integer.parseInt(statsMap.get("datalength"));
      int recordnum = Integer.parseInt(statsMap.get("recordnum"));
    
      DictionaryEntry entry = 
	MappedMultiKeyIndex.dictionaryBinarySearch(termDictionaryRaf, term.toLowerCase(), 
						   term.length(), datalength, recordnum );
      if (entry != null) {
	MappedMultiKeyIndex.readPostings(extentsRaf, postingsRaf, resultList, entry);
      }
    }
    return resultList;
  }

  /**
   * The main program
   * @param args Arguments passed from the command line
   * @throws IOException
   * @throws FileNotFoundException
   * @throws NoSuchAlgorithmException
   **/
  public static void main(String[] args)
    throws FileNotFoundException, IOException, NoSuchAlgorithmException
  {
    if (args.length > 4) {
      String option = args[0];
      String workingDir = args[1];
      String indexName = args[2];
      String column = args[3];

      Map<String,String []> tableConfig = Config.loadConfig(workingDir + "/tables/ifconfig");
      String[] tableFields = tableConfig.get(indexName);
      if (option.equals("lookup")) {
	StringBuilder termBuf = new StringBuilder();
	for (int i = 4; i < args.length; i++) {
	  termBuf.append(args[i]).append(" ");
	}
	String term = termBuf.toString().trim();
	System.out.println("option: " + option);
	System.out.println("workingDir: " + workingDir);
	System.out.println("indexname: " + indexName);
	System.out.println("column: " + column);
	System.out.println("term: " + term);
	MappedMultiKeyIndex index = new MappedMultiKeyIndex(workingDir, indexName);
	MappedMultiKeyIndexLookup instance = new MappedMultiKeyIndexLookup(index);
	List<String> resultList = instance.lookup( term, Integer.parseInt(column));
	for (String result: resultList) {
	  System.out.println(result);
	}
	
      } else {
	System.out.println("Unknown option.");
	System.out.println("Usage: build workingdir indexname");
	System.out.println("       lookup workingdir indexname term");
      }
    }
  }
}
