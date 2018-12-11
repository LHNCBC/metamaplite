//
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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.Charset;

/**
 * 
 */

public class MultiKeyIndex {

  Charset charset = Charset.forName("utf-8");
  String indexname;
  String indexDirectoryName;
  RandomAccessFile postingsRaf;
  /** random access file name cache as Map, filename -&gt; random access file. */
  Map<String,RandomAccessFile> rafCache = new HashMap<String,RandomAccessFile>(); 
  /** map of stats maps for each partition, partitionName -&gt; StatsMap */
  Map<String,Map<String,String>> MapOfStatMaps = new HashMap<String,Map<String,String>>();

  public MultiKeyIndex(String indexDirectoryName)
    throws FileNotFoundException
  {
    this.indexDirectoryName = indexDirectoryName;
    String[] fields = indexDirectoryName.split("/");
    this.indexname = fields[fields.length - 1];
    this.postingsRaf = 
      new RandomAccessFile(indexDirectoryName + "/postings", "r");
  }

  public MultiKeyIndex(String indexDirectoryName, Charset charset)
    throws FileNotFoundException
  {
    this.indexDirectoryName = indexDirectoryName;
    String[] fields = indexDirectoryName.split("/");
    this.indexname = fields[fields.length - 1];
    this.postingsRaf = 
      new RandomAccessFile(indexDirectoryName + "/postings", "r");
    this.charset = charset;
  }

  public MultiKeyIndex(String workingDirectoryName, String indexname)
    throws FileNotFoundException
  {
    this.indexDirectoryName = workingDirectoryName +  "/indices/" + indexname ;
    this.indexname = indexname;
    this.postingsRaf = 
      new RandomAccessFile(this.indexDirectoryName + "/postings", "r");
  }

  public MultiKeyIndex(String workingDirectoryName, String indexname, Charset charset)
    throws FileNotFoundException
  {
    this.indexDirectoryName = workingDirectoryName +  "/indices/" + indexname ;
    this.indexname = indexname;
    this.postingsRaf = 
      new RandomAccessFile(this.indexDirectoryName + "/postings", "r");
    this.charset = charset;
  }

  public RandomAccessFile openRandomAccessFile(String filename) 
    throws FileNotFoundException
  {
    if (rafCache.containsKey(filename)) {
      return rafCache.get(filename);
    } else {
      RandomAccessFile raf = new RandomAccessFile(filename, "r");
      rafCache.put(filename, raf);
      return raf;
    }
  }

  /**
   * Generate path for partition
   *
   * @param workingDir working directory path
   * @param indexname  name of index
   * @param columnString key column of table
   * @param termLengthString length of indexed term
   * @param suffix filename suffix
   * @return path of partition file.
   */
  public static String partitionPath(String workingDir, String indexname, 
			      String columnString, String termLengthString, String suffix) {
    return workingDir + "/indices/" + indexname + "/" + indexname + "-" + 
      columnString + "-" + termLengthString + suffix;
  }


  public static String partitionPath(String indexDirectoryName, 
				     String columnString, String termLengthString, String suffix) {
    String[] fields = indexDirectoryName.split("/");
    String indexname = fields[fields.length - 1];
    return indexDirectoryName + "/" + indexname + "-" + 
      columnString + "-" + termLengthString + suffix;
  }


  public RandomAccessFile openTermDictionaryFile(String columnString, String termLengthString)
    throws IOException
  {
    return openRandomAccessFile(partitionPath
				(this.indexDirectoryName,
				 columnString, termLengthString, "-term-dictionary"));
  }

  public RandomAccessFile openExtentsFile(String columnString, String termLengthString)
    throws IOException
  {
    return openRandomAccessFile(partitionPath
				(this.indexDirectoryName,
				 columnString, termLengthString, "-postings-offsets"));
  }

  public RandomAccessFile getPostingsFile() {
    return this.postingsRaf;
  }
 
  public Map<String,String> readStatsFile(String columnString, String termLengthString)
    throws IOException
  {
    return readStatsFile(MultiKeyIndex.partitionPath
			 (this.indexDirectoryName,
			  columnString, termLengthString, "-term-dictionary-stats.txt"));
  }

  public List<String> lookup(int column, String term)
    throws IOException, FileNotFoundException
  {
    List<String> resultList = new ArrayList<String>();
    // byte length of utf-8 string
    int bytelength = term.getBytes(this.charset).length;

    String termLengthString = Integer.toString(bytelength);
    String columnString = Integer.toString(column);

    RandomAccessFile termDictionaryRaf = this.openTermDictionaryFile(columnString, termLengthString);
    RandomAccessFile extentsRaf = this.openExtentsFile(columnString, termLengthString);
    RandomAccessFile postingsRaf = this.getPostingsFile();
    Map<String,String> statsMap = this.readStatsFile(columnString, termLengthString);

    int datalength = Integer.parseInt(statsMap.get("datalength"));
    int recordnum = Integer.parseInt(statsMap.get("recordnum"));
    
    DictionaryEntry entry = 
      dictionaryBinarySearch(termDictionaryRaf, term, 
					   bytelength, datalength, recordnum );
    if (entry != null) {
      readPostings(extentsRaf, postingsRaf, resultList, entry, charset);
    } else {
      resultList.add("\"" + term + "\" entry is " + entry);
    }
    termDictionaryRaf.close();
    extentsRaf.close();
    postingsRaf.close();
    return resultList;
  }

  public static String sha1(String input, Charset charset) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(input.getBytes(charset));
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  
  public static class Record {
    /** input line*/
    String line;
    Record(String line) {
      this.line = line;
    }
    /** @return input line*/
    String getLine() { return this.line; }
    /** @return line separated into fields */
    String [] getFields() { return this.line.split("\\|"); }
    /** @return checksum digest of line (currently sha1) */
    String getDigest(Charset charset)
    {
      try {
	return sha1(this.line, charset);
      } catch (NoSuchAlgorithmException nsae) {
	throw new RuntimeException(nsae);
      }
    }
  }

  /**
   * Load Table
   * @param tablefilename name of file containing table of records with pipe-separated fields.
   * @return list of records instances.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws NoSuchAlgorithmException 
   */
  public static List<Record> loadTable(String tablefilename, Charset charset) 
    throws FileNotFoundException, IOException, NoSuchAlgorithmException {
    List<Record> newList = new ArrayList<Record>();
    BufferedReader br =
      new BufferedReader(new InputStreamReader(new FileInputStream(tablefilename), charset));
    String line;
    while ((line = br.readLine()) != null) {
      newList.add(new Record(line));
    }
    return newList;
  }

  /** container for start offset and length of a posting. */
  public static class Extent {
    /** address of posting */
    long start;
    /** length of posting */
    long length;
    Extent(long start, long length) { this.start = start; this.length = length; }
    long getStart() { return this.start; }
    long getLength() { return this.length; }
  }
  
  /**
   *  Disk based binary search implementation
   *
   * @param bsfp       file pointer for binary search table
   * @param word       search word
   * @param wordlen    wordlength
   * @param numrecs    number of records in table
   * @return long containing address of posting, -1 if not found.
   */
  public static DictionaryEntry
    dictionaryBinarySearch(RandomAccessFile bsfp, String word, 
			   int wordlen, long datalen, long numrecs)
    throws IOException
  {
    long low = 0;
    long high = numrecs;
    long cond;
    long mid;
    byte[] wordbuf = new byte[wordlen];
    String tstword;

    // System.out.println("wordlen: " + wordlen + ", datalen: " + datalen + ", numrecs: " + numrecs);
    while ( low < high )
      {
	mid = low + (high- low) / 2;
	bsfp.seek(mid * (wordlen+datalen));
	bsfp.read(wordbuf);
	tstword = new String(wordbuf);
	// System.out.println("index: " + mid + ", address: " + (mid * (wordlen+datalen)) + ", tstword: " + tstword + ", word: " + word);
	cond = word.compareTo(tstword);
	if (cond < 0) {
	  high = mid;
	} else if (cond > 0) {
	  low = mid + 1;
	} else {
	  long count = bsfp.readLong();
	  long address = bsfp.readLong();
	  return new DictionaryEntry(tstword, count, address);
	}
      }
    return null;
  }

  public static Map<String,String> readStatsFile(String filename)
    throws IOException, FileNotFoundException
  {
    Map<String,String> newMap = new HashMap<String,String>();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\\|");
      newMap.put(fields[0], fields[1]);
    }
    br.close();
    return newMap;
  }

  public static void readPostings(RandomAccessFile extentsRaf, RandomAccessFile postingsRaf, 
				  List<String> newList, DictionaryEntry entry, Charset charset) 
    throws IOException
  {
    extentsRaf.seek(entry.getAddress());
    for (int i = 0; i < entry.getNumberOfPostings(); i++) {
      long offset = extentsRaf.readLong();
      long length = extentsRaf.readLong();
      postingsRaf.seek(offset);
      // read encoded UTF-8 string
      byte[] byteData = new byte[(int)length];
      postingsRaf.read(byteData);
      newList.add(new String(byteData, charset));
    }
  }
}
