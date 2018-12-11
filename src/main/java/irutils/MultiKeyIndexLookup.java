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
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import irutils.MultiKeyIndex.Record;

/**
 * 
 */

public class MultiKeyIndexLookup {

  MultiKeyIndex index;
  Charset charset = Charset.forName("utf-8");

  public MultiKeyIndexLookup(String indexDirectoryName)
    throws FileNotFoundException
  {
    this.index = new MultiKeyIndex(indexDirectoryName);
  }

  public MultiKeyIndexLookup(MultiKeyIndex index) {
    this.index = index;
  }

  public MultiKeyIndexLookup(String indexDirectoryName, Charset charset)
    throws FileNotFoundException
  {
    this.index = new MultiKeyIndex(indexDirectoryName);
    this.charset = charset;
  }

  public MultiKeyIndexLookup(MultiKeyIndex index, Charset charset) {
    this.index = index;
    this.charset = charset;
  }

  public List<String> lookup(String term, int column)
    throws IOException, FileNotFoundException
  {
    List<String> resultList = new ArrayList<String>();
    // byte length of utf-8 string
    int bytelength = term.getBytes(this.charset).length;

    String termLengthString = Integer.toString(bytelength);
    String columnString = Integer.toString(column);

    RandomAccessFile termDictionaryRaf = this.index.openTermDictionaryFile(columnString, termLengthString);
    RandomAccessFile extentsRaf = this.index.openExtentsFile(columnString, termLengthString);
    RandomAccessFile postingsRaf = this.index.getPostingsFile();
    Map<String,String> statsMap = this.index.readStatsFile(columnString, termLengthString);

    int datalength = Integer.parseInt(statsMap.get("datalength"));
    int recordnum = Integer.parseInt(statsMap.get("recordnum"));
    
    DictionaryEntry entry = 
      MultiKeyIndex.dictionaryBinarySearch(termDictionaryRaf, term.toLowerCase(), 
					   bytelength, datalength, recordnum );
    if (entry != null) {
      MultiKeyIndex.readPostings(extentsRaf, postingsRaf, resultList, entry, this.charset);
    }
    termDictionaryRaf.close();
    extentsRaf.close();
    postingsRaf.close();
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
	MultiKeyIndex index = new MultiKeyIndex(workingDir, indexName);
	MultiKeyIndexLookup instance = new MultiKeyIndexLookup(index);
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
