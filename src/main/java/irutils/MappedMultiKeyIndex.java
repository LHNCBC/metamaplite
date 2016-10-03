package irutils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Dictionary and Postings (example records)
 * <p>
 * cuisourceinfo-3-30-term-dictionary-stats.txt:
 * <pre>
 *  termlength|30
 *  reclength|46
 *  datalength|16
 *  recordnum|76361
 * </pre>
 * <p>
 * cuisourceinfo-3-30-partition:
 * <pre>
 *  |            term              | # of postings | address |
 *  +------------------------------+---------------+---------+
 *  |dipalmitoylphosphatidylcholine|       4       | FFF4556 | 
 * </pre>
  * <p>
 * cuisourceinfo-3-30-partition-offsets
 * <pre>
 *  | address | start |  len  |
 *  +---------+-------+-------+
 *  | FFF4556 |   58  |   57  |
 *  |   ...   |  176  |   66  |
 *  |   ...   |  279  |   59  |
 *  |   ...   |    0  |   58  |
 * </pre> 
 * <p>
 * postings
 * <pre>
 *  address | data 
 *  --------+-------------------------------------------------------------------
 *        0 | C0000039|S0033298|4|Dipalmitoylphosphatidylcholine|SNMI|PT
 *       58 | C0000039|S0033298|7|Dipalmitoylphosphatidylcholine|LNC|CN
 *      176 | C0000039|S0033298|6|Dipalmitoylphosphatidylcholine|SNOMEDCT_US|OAP
 *      279 | C0000039|S0033298|5|Dipalmitoylphosphatidylcholine|NDFRT|SY
 * </pre>
 */

public class MappedMultiKeyIndex {

  String indexname;
  String indexDirectoryName;
  MappedByteBuffer postingsRaf = null;
  /** random access file name cache as Map, filename -&gt; random access file. */
  Map<String,MappedByteBuffer> byteBufCache = new HashMap<String,MappedByteBuffer>(); 
  /** map of term dictionary byte buffers for each partition, partitionName -&gt; StatsMap */
  Map<String,MappedByteBuffer> mapOfTermDictionaryRafs = new HashMap<String,MappedByteBuffer>();
  /** map of extents byte buffers for each partition, partitionName -&gt; StatsMap */
  Map<String,MappedByteBuffer> mapOfExtentsRafs = new HashMap<String,MappedByteBuffer>();
  /** map of stats maps for each partition, partitionName -&gt; StatsMap */
  Map<String,Map<String,String>> mapOfStatMaps = new HashMap<String,Map<String,String>>();

  /**
   * Open index using basename as name of index.
   * @param indexDirectoryName name of directory containing index.
   */
  public MappedMultiKeyIndex(String indexDirectoryName)
    throws FileNotFoundException, IOException
  {
    this.indexDirectoryName = indexDirectoryName;
    String[] fields = indexDirectoryName.split("/");
    this.indexname = fields[fields.length - 1];
    FileInputStream postingsInputStream =
      new FileInputStream(new File (indexDirectoryName + "/postings"));
    FileChannel postingsFileChannel = postingsInputStream.getChannel();
    int sz = (int)postingsFileChannel.size();
    this.postingsRaf = 
      postingsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, sz);
    postingsInputStream.close();
  }

  /**
   * Open index using basename and use supplied name as name of index.
   * @param workingDirectoryName name of directory containing index.
   * @param indexname name of index.
   */
  public MappedMultiKeyIndex(String workingDirectoryName, String indexname)
    throws FileNotFoundException, IOException
  {
    this.indexDirectoryName = workingDirectoryName +  "/indices/" + indexname ;
    this.indexname = indexname;

    FileInputStream postingsInputStream =
      new FileInputStream(new File (indexDirectoryName + "/postings"));
    FileChannel postingsFileChannel = postingsInputStream.getChannel();
    int sz = (int)postingsFileChannel.size();
    this.postingsRaf = 
      postingsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, sz);
    postingsInputStream.close();
  }

  /**
   * Map file to MemoryMappedByteBuffer.
   *
   * @param filename of file to be mapped
   * @return memory mapped buffer of file.
   */
  public MappedByteBuffer openMappedByteBuffer(String filename) 
    throws FileNotFoundException, IOException
  {
    if (byteBufCache.containsKey(filename)) {
      return byteBufCache.get(filename);
    } else {
      MappedByteBuffer byteBuffer = null;
      File file = new File (filename);
      if (file.exists()) {
	FileChannel fileChannel = 
	  (new FileInputStream(file)).getChannel();
	int sz = (int)fileChannel.size();
	byteBuffer = 
	  fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, sz);
	byteBufCache.put(filename, byteBuffer);
	fileChannel.close();
      } else {
	/* file doesn't exist put null bytebuffer pointer in cache anyway. */
	byteBufCache.put(filename, byteBuffer);
      }
      return byteBuffer;
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

  /**
   * Generate path for partition
   *
   * @param indexDirectoryName  name of index
   * @param columnString key column of table
   * @param termLengthString length of indexed term
   * @param suffix filename suffix
   * @return path of partition file.
   */
  public static String partitionPath(String indexDirectoryName, 
				     String columnString, String termLengthString, String suffix) {
    String[] fields = indexDirectoryName.split("/");
    String indexname = fields[fields.length - 1];
    return indexDirectoryName + "/" + indexname + "-" + 
      columnString + "-" + termLengthString + suffix;
  }

  public MappedByteBuffer openTermDictionaryFile(String columnString, String termLengthString)
    throws IOException
  {
    MappedByteBuffer termDictionaryByteBuffer = openMappedByteBuffer
      (partitionPath
       (this.indexDirectoryName,
	columnString, termLengthString, "-term-dictionary"));
    return termDictionaryByteBuffer;
  }

  public MappedByteBuffer getTermDictionaryFile(String columnString, String termLengthString)
    throws IOException
  {
    MappedByteBuffer termDictionaryByteBuffer;
    String partitionKey = columnString + "|" + termLengthString;
    if (this.mapOfTermDictionaryRafs.containsKey(partitionKey)) {
       termDictionaryByteBuffer = this.mapOfTermDictionaryRafs.get(partitionKey);
    } else {
      termDictionaryByteBuffer = this.openTermDictionaryFile(columnString, termLengthString);
      this.mapOfTermDictionaryRafs.put(partitionKey, termDictionaryByteBuffer);
    }
    return termDictionaryByteBuffer;
  }

  public MappedByteBuffer openExtentsFile(String columnString, String termLengthString)
    throws IOException
  {
    return openMappedByteBuffer(partitionPath
				(this.indexDirectoryName,
				 columnString, termLengthString, "-postings-offsets"));
  }

  public MappedByteBuffer getExtentsFile(String columnString, String termLengthString)
    throws IOException
  {
    MappedByteBuffer extentsByteBuffer;
    String partitionKey = columnString + "|" + termLengthString;
    if (this.mapOfExtentsRafs.containsKey(partitionKey)) {
       extentsByteBuffer = this.mapOfExtentsRafs.get(partitionKey);
    } else {
      extentsByteBuffer = this.openExtentsFile(columnString, termLengthString);
      this.mapOfExtentsRafs.put(partitionKey, extentsByteBuffer);
    }
    return extentsByteBuffer;
  }

  public MappedByteBuffer getPostingsFile() {
    return this.postingsRaf;
  }

  public Map<String,String> readStatsFile(String columnString, String termLengthString)
    throws IOException
  {
    return readStatsFile(MultiKeyIndex.partitionPath
			 (this.indexDirectoryName,
			  columnString, termLengthString, "-term-dictionary-stats.txt"));
  }

  /**
   * @param columnString table column index to use
   * @param termLengthString length of search term
   * @return Map of statistics keyed by type of statistic.
   */
  public Map<String,String> getStatsMap(String columnString, String termLengthString)
    throws FileNotFoundException, IOException
  {
    Map<String,String> statsMap;
    String partitionKey = columnString + "|" + termLengthString;
    if (this.mapOfStatMaps.containsKey(partitionKey)) {
      statsMap = this.mapOfStatMaps.get(partitionKey);
    } else {
      statsMap = this.readStatsFile(columnString, termLengthString);
      this.mapOfStatMaps.put(partitionKey, statsMap);
    }
    return statsMap;
  }

  /**
   * Lookup term in index for specified table column.
   * @param column  table column index to use
   * @param term search term
   * @return list of results matching term.
   */
  public List<String> lookup(int column, String term)
    throws IOException, FileNotFoundException
  {
    List<String> resultList = new ArrayList<String>();
    String termLengthString = Integer.toString(term.length());
    String columnString = Integer.toString(column);
    String partitionKey = columnString + "|" + termLengthString;
    MappedByteBuffer termDictionaryRaf = this.getTermDictionaryFile(columnString, termLengthString);
    MappedByteBuffer extentsRaf = this.getExtentsFile(columnString, termLengthString);
    Map<String,String> statsMap = this.getStatsMap(columnString, termLengthString);
    int datalength = Integer.parseInt(statsMap.get("datalength"));
    int recordnum = Integer.parseInt(statsMap.get("recordnum"));
    
    DictionaryEntry entry = 
      dictionaryBinarySearch(termDictionaryRaf, term, 
					   term.length(), datalength, recordnum );
    if (entry != null) {
      readPostings(extentsRaf, this.postingsRaf, resultList, entry);
    } 
    return resultList;
  }

  /**
   * Generate SHA1 digest of input string.
   * @param input input string
   * @return SHA1 digest generated from input string.
   */
  public static String sha1(String input) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(input.getBytes());
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  
  public static class Record {
    /** input line*/
    String line;
    /** line separated into fields */
    String [] fields;
    /** checksum digest of line (currently sha1) */
    String digest;
    Record(String line, String [] fields, String digest) {
      this.line = line; this.fields = fields; this.digest = digest;
    }
    String getLine() { return this.line; }
    String [] getFields() { return this.fields; }
    String getDigest() { return this.digest; }
  }

  /**
   * Load Table
   * @param tablefilename name of file containing table of records with pipe-separated fields.
   * @return list of records instances.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws NoSuchAlgorithmException 
   */
  public static List<Record> loadTable(String tablefilename) 
    throws FileNotFoundException, IOException, NoSuchAlgorithmException {
    List<Record> newList = new ArrayList<Record>();
    BufferedReader br = new BufferedReader(new FileReader(tablefilename));
    String line;
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\\|");
      String digest = sha1(line);
      newList.add(new Record(line, fields, digest));
    }
    return newList;
  }

  /** container for start offset and length of a posting. */
  public static class Extent {
    long start;
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
    dictionaryBinarySearch(MappedByteBuffer bsfp, String word, 
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
	bsfp.position((int)(mid * (wordlen+datalen)));
	bsfp.get(wordbuf);
	tstword = new String(wordbuf);
	// System.out.println("index: " + mid + ", address: " + (mid * (wordlen+datalen)) + ", tstword: " + tstword + ", word: " + word);
	cond = word.compareTo(tstword);
	if (cond < 0) {
	  high = mid;
	} else if (cond > 0) {
	  low = mid + 1;
	} else {
	  long count = bsfp.getLong();
	  long address = bsfp.getLong();
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

  public static void readPostings(MappedByteBuffer extentsRaf, MappedByteBuffer postingsRaf, 
			   List<String> newList, DictionaryEntry entry) 
    throws IOException
  {
    extentsRaf.position((int)entry.getAddress());
    for (int i = 0; i < entry.getNumberOfPostings(); i++) {
      long offset = extentsRaf.getLong();
      long length = extentsRaf.getLong();
      byte[] buf = new byte[(int)length];
      postingsRaf.position((int)offset);
      postingsRaf.get(buf);
      newList.add(new String(buf));
    }
  }
}
