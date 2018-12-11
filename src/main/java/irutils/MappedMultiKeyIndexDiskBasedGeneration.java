
//
package irutils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;

import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import irutils.MultiKeyIndex.Record;
import irutils.MultiKeyIndex.Extent;

/**
 * 
 */

public class MappedMultiKeyIndexDiskBasedGeneration {
  /*
    Memory based temporary inverted file
    two tables;
    <ol>
    <li>digest -&gt; postings record map
    <li>column -&gt; termlength -&gt; term -&gt; digest map 
    </ol>
  */
  
  /** encoding utf-8? */
  Charset charset = Charset.forName("utf-8");
  /** map of stats maps for each partition, partitionName -&gt; StatsMap */
  Map<String,Map<String,String>> mapOfStatMaps = new HashMap<String,Map<String,String>>();

  public MappedMultiKeyIndexDiskBasedGeneration() { }
  public MappedMultiKeyIndexDiskBasedGeneration(Charset charset) { this.charset = charset; }


  /** start a new digest list for term using map from column -&gt; termLength 
   * @param newMap string -&gt; digest -list map
   * @param term   term to be indexed
   * @param digest digest of postings for term
   */
  public static void addNewDigestList(Map<String,List<String>> newMap, String term, String digest) {
    List<String> newList = new ArrayList<String>();
    newList.add(digest);
    newMap.put(term, newList);
  }

  /** column -&gt; termlength -&gt; term -&gt; digest map */
  Map<Integer,Map<Integer,Map<String,List<String>>>> columnLengthTermDigestMap;
  /** digest -&gt; single posting map */
  Map<String,String> digestPostingMap;

  /**
   * Write term dictionary as a set of temporary partition tables
   * grouped by termlength to be used later to create final partition
   * files.
   *
   * @param workingDir working directory
   * @param indexName short name of index
   * @param recordTable list of record instances
   * @param columns  which columns of records to use as keys.
   * @return set of column "-" termLength keys.
   */
  public static Set<String> writeTemporaryPartitionsTables(String workingDir, String indexName,
							   List<Record> recordTable, int[] columns, Charset charset)
    throws IOException {
    Map<String,PrintWriter> columnLengthTempTableFileMap =  new HashMap<String,PrintWriter>();
    for (Record record: recordTable) {
      String[] fields = record.getFields();
      // String digest = record.getDigest();
      for (int column: columns) {
	String term = fields[column].toLowerCase();
	/// int termLength = term.length();
	// byte length of utf-8 or utf-16 string
	int bytelength = term.getBytes(charset).length;
	String tempPartitionFilename =
	  MultiKeyIndex.partitionPath(workingDir, indexName,
				      Integer.toString(column), Integer.toString(bytelength), "-temp-partition-table");
	Path indexPath = Paths.get(workingDir + "/indices/" + indexName);
	if (Files.notExists(indexPath)) {
	  Path newDir = Files.createDirectory(indexPath);
	  System.out.println("newDir: " + newDir);
	}
	String partitionKey = Integer.toString(column) + "-" + Integer.toString(bytelength);
	PrintWriter pw;
	if (columnLengthTempTableFileMap.containsKey(partitionKey)) {
	  pw = columnLengthTempTableFileMap.get(partitionKey);
	} else {
	  pw = new PrintWriter(new FileWriter(tempPartitionFilename));
	  columnLengthTempTableFileMap.put(partitionKey, pw);
	}
	pw.println(record.getLine());
      }
    }
    // close all partitions
    for (PrintWriter pw: columnLengthTempTableFileMap.values()) {
      pw.close();
    }
    return columnLengthTempTableFileMap.keySet();
  }

 /**
   * Generate term dictionary for column and term length
   * @param recordTable list of record instances
   * @param column table column to read key 
   * @return map of term to digest lists
   */
  public static Map<String,List<String>> generateTermDigestMap(Map<String,String> digestPostingMap,
							       List<Record> recordTable,
							       int column, Charset charset)
  {
    System.out.println("generating term -> digest map and digest -> posting map...");
    Map<String,List<String>> termDigestMap = new TreeMap<String,List<String>>();
    for (Record record: recordTable) {
      String[] fields = record.getFields();
      String digest = record.getDigest(charset);
      if (! digestPostingMap.containsKey(digest)) {
	digestPostingMap.put(digest, record.getLine());
      }
      String term = fields[column].toLowerCase();
      if (termDigestMap.containsKey(term)) {
	  termDigestMap.get(term).add(digest);
      } else {
	List<String> newList = new ArrayList<String>();
	newList.add(digest);    
	termDigestMap.put(term, newList);
      }
    }
    return termDigestMap;
  }

  /**
   * Write postings to posting pool file while filling digest -&gt; posting extent map.
   * @param postingsRaf postings random access file 
   * @param termDigestMap term to digest list map
   * @param digestPostingMap digest to posting content map
   * @param digestExtentMap digest to postings extent map
   * @throws IOException 
   */
  public static void writePostings(RandomAccessFile postingsRaf,
				   Map<String, List<String>> termDigestMap,
				   Map<String,String> digestPostingMap,
				   Map<String,Extent> digestExtentMap,
				   Charset charset)
    throws IOException {
    System.out.println("writing postings...");
    for (Map.Entry<String,List<String>> termDigestEntry: termDigestMap.entrySet()) {
      for (String digest: termDigestEntry.getValue()) {
	// convert posting string to bytes
	byte[] byteData = digestPostingMap.get(digest).getBytes(charset);
	long start = postingsRaf.getFilePointer();
	postingsRaf.write(byteData);
	long end = postingsRaf.getFilePointer();
	digestExtentMap.put(digest, new Extent(start, byteData.length));
      }
    }
  }

  /**
   * Write dictionary and extent list file
   * @param workingDir working directory
   * @param indexName short name of index
   * @param termDigestMap term to digest list map
   * @param digestExtentMap digest to postings extent map
   * @throws IOException 
   */
    public static void writeDictionaryAndExtents(String workingDir, String indexName, String columnLengthKey,
						 Map<String, List<String>> termDigestMap,
						 Map<String,Extent> digestExtentMap,
						 Charset charset)
      throws FileNotFoundException, IOException, NoSuchAlgorithmException
  {
      System.out.println("writing dictionary and extents (TBI)...");
      String[] fields = columnLengthKey.split("-");
      int column = Integer.parseInt(fields[0]);
      int termLength = Integer.parseInt(fields[1]);
      RandomAccessFile termDictionaryRaf = 
	  new RandomAccessFile(MultiKeyIndex.partitionPath
			       (workingDir, indexName,
				Integer.toString(column), Integer.toString(termLength),
				"-term-dictionary"), "rw");
      RandomAccessFile extentsRaf = 
	  new RandomAccessFile(MultiKeyIndex.partitionPath
			       (workingDir, indexName,
				Integer.toString(column), Integer.toString(termLength),
				"-postings-offsets"), "rw");


      int recordnumber = termDigestMap.size();
      long datalength = 16;
      long recordlength = termLength + datalength;
      
      for (Entry<String,List<String>> termEntry: termDigestMap.entrySet()) {
	
	byte[] byteData = termEntry.getKey().getBytes(charset);
	  List<String> digestList = termEntry.getValue();
	  long extentListOffset = extentsRaf.getFilePointer();
	  // write extents
	  for (String digest: digestList) {
	    Extent extent = digestExtentMap.get(digest);
	    extentsRaf.writeLong(extent.getStart());
	    extentsRaf.writeLong(extent.getLength());
	  }
	  // write dictionary
	  
	  long dictEntryStart = termDictionaryRaf.getFilePointer();
	  // write encoded UTF-8 string 
	  termDictionaryRaf.write(byteData);		  // term
	  
	  long dictEntryDataStart = termDictionaryRaf.getFilePointer();
	  termDictionaryRaf.writeLong(digestList.size()); // number of postings
	  termDictionaryRaf.writeLong(extentListOffset);  // offset to begining of extent list
	  datalength = termDictionaryRaf.getFilePointer() - dictEntryDataStart;
	  recordlength = termDictionaryRaf.getFilePointer() - dictEntryStart;
      }
      termDictionaryRaf.close();
      extentsRaf.close();
      BufferedWriter bw =
	new BufferedWriter
	(new FileWriter
	 (workingDir + "/indices/" + indexName + "/" + indexName + "-" + 
	  Integer.toString(column) + "-" + Integer.toString(termLength) + "-term-dictionary-stats.txt"));
      bw.write("termlength|" + termLength + "\n");
      bw.write("reclength|"  + recordlength + "\n");
      bw.write("datalength|" + datalength + "\n");
      bw.write("recordnum|"  + recordnumber + "\n");
      bw.close();
    }
  
  /**
   * Write final index using temporary index files
   *
   * @param workingDir working directory
   * @param indexName short name of index
   * @param columnLengthSet set of column "-" term length keys
   */
  public static void writeFinalIndex(String workingDir, String indexName, Set<String> columnLengthSet, Charset charset)
    throws FileNotFoundException, IOException, NoSuchAlgorithmException
  {
    // digest to diskfile extent map (offset, length in diskfile)
    Map<String,Extent> digestExtentMap = new HashMap<String,Extent>();
    // digest to postings content map
    Map<String,String> digestPostingMap = new HashMap<String,String>();

    // open postings file
     RandomAccessFile postingsRaf = new RandomAccessFile
       (workingDir + "/indices/" + indexName + "/postings", "rw");
    
    for (String columnLengthKey: columnLengthSet) {
      System.out.println("Processing table " + columnLengthKey);
      String[] fields = columnLengthKey.split("-");
      int column = Integer.parseInt(fields[0]);
      int termLength = Integer.parseInt(fields[1]);
      Path tempPartitionPath = Paths.get(workingDir + "/indices/" + indexName + "/" + indexName +
					 "-" + columnLengthKey + "-temp-partition-table");
      List<Record> recordTable = MultiKeyIndex.loadTable(tempPartitionPath.toString(), charset);
      Map<String, List<String>> termDigestMap = generateTermDigestMap(digestPostingMap, recordTable, column, charset);
      writePostings(postingsRaf, termDigestMap, digestPostingMap, digestExtentMap, charset);
      writeDictionaryAndExtents(workingDir, indexName, columnLengthKey, termDigestMap, digestExtentMap, charset);
    }
    postingsRaf.close();
  }

  /**
   * Generate in-memory term dictionary
   * @param recordTable list of record instances
   * @param columns  which columns of records to use as keys.
   */
  public void generateMaps(List<Record> recordTable, int[] columns) {
    // create in-memory representation of file maps
    this.columnLengthTermDigestMap =  new HashMap<Integer,Map<Integer,Map<String,List<String>>>>();
    this.digestPostingMap = new HashMap<String,String>();

    for (int column: columns) {
      this.columnLengthTermDigestMap.put(column, new HashMap<Integer,Map<String,List<String>>>());
    }
    for (Record record: recordTable) {
      String[] fields = record.getFields();
      String digest = record.getDigest(this.charset);
      this.digestPostingMap.put(digest, record.getLine()); // store hash -> postings
      for (int column: columns) {
	String term = fields[column].toLowerCase();
	// byte length of utf-8 string
	int bytelength = term.getBytes(this.charset).length;
	if (this.columnLengthTermDigestMap.get(column).containsKey(bytelength)) {
	  if (this.columnLengthTermDigestMap.get(column).get(bytelength).containsKey(term)) {
	    // store column -> term -> hash list
	    this.columnLengthTermDigestMap.get(column).get(bytelength).get(term).add(digest);
	  } else {
	    Map<String,List<String>> termDigestMap = this.columnLengthTermDigestMap.get(column).get(new Integer(bytelength));
            List<String> newList = new ArrayList<String>();
            newList.add(digest);    
            termDigestMap.put(term, newList);
	  }
	} else {
	  Map<String,List<String>> newTermDigestMap = new TreeMap<String,List<String>>();
	  List<String> newList = new ArrayList<String>();
          newList.add(digest);    
          newTermDigestMap.put(term, newList);
          this.columnLengthTermDigestMap.get(column).put(bytelength, newTermDigestMap);
	}
      }
    }
  }


  /**
   * Write postings to posting pool file while filling digest -&gt; posting extent map that is returned at end of processing.
   * @param workingdir working directory
   * @return map of string -&gt; start, offset pairs (extents)
   * @throws IOException 
   */
  public Map<String, Extent> writePostings(String workingdir, String indexname) 
    throws IOException { 
    Map<String, Extent> digestExtentMap = new TreeMap<String, Extent>();

    // get final length of file
    int length = 0;
    for (Map.Entry<String,String> digestEntry: this.digestPostingMap.entrySet()) {
      length = length + digestEntry.getValue().getBytes(this.charset).length;
    }

    // map file.
    MappedByteBuffer raf = new RandomAccessFile
      (workingdir + "/indices/" + indexname + "/postings", "rw")
      .getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);

    // write postings
    for (Map.Entry<String,String> digestEntry: this.digestPostingMap.entrySet()) {
      // convert posting string to bytes
      byte[] byteData = digestEntry.getValue().getBytes(this.charset);
      long start = raf.position();
      raf.put(byteData);
      long end = raf.position();
      if ((end - start) != byteData.length) {
	System.out.println("Warning: extent: (" + end + " - " + start + ") = " + (end - start) +
			   " does not equal byteData length: " + Integer.toString(byteData.length));
      }
      digestExtentMap.put(digestEntry.getKey(), new Extent(start, byteData.length));
    }
    return digestExtentMap;
  }

  /**
   * Using column length term digest map and digest extent Map, create
   * partitions consisting of two files: a dictionary containing term,
   * num-of-postings, and pointer to extent list and extext list pool
   * containing offset length pairs, one for each posting.
   *
   * @param workingDir working directory
   * @param indexname name of index
   * @param digestExtentMap map of digest -&gt; start length pairs (extents)
   * @throws IOException
   * @throws FileNotFoundException
   */
  public void writePartitions(String workingDir, String indexname, Map<String, Extent> digestExtentMap) 
    throws FileNotFoundException, IOException
  {
    for (Integer column: this.columnLengthTermDigestMap.keySet()) {
      for (Integer termLength: this.columnLengthTermDigestMap.get(column).keySet()) {
	RandomAccessFile termDictionaryRaf = 
	  new RandomAccessFile(MultiKeyIndex.partitionPath(workingDir, indexname,
					     column.toString(), termLength.toString(), "-term-dictionary"), "rw");
	RandomAccessFile extentsRaf = 
	  new RandomAccessFile(MultiKeyIndex.partitionPath(workingDir, indexname,
					     column.toString(), termLength.toString(), "-postings-offsets"), "rw");
	int recordnumber = this.columnLengthTermDigestMap.get(column).get(termLength).size();
	long datalength = 16;
	long recordlength = termLength.intValue() + datalength;
	for (Entry<String,List<String>> termEntry: this.columnLengthTermDigestMap.get(column).get(termLength).entrySet()) {
	  byte[] byteData = termEntry.getKey().getBytes(this.charset);
	  List<String> digestList = termEntry.getValue();
	  long extentListOffset = extentsRaf.getFilePointer();
	  // write extents
	  for (String digest: digestList) {
	    Extent extent = digestExtentMap.get(digest);
	    extentsRaf.writeLong(extent.getStart());
	    extentsRaf.writeLong(extent.getLength());
	  }
	  // write dictionary
	  
	  long dictEntryStart = termDictionaryRaf.getFilePointer();
	  termDictionaryRaf.write(byteData);		  // term
	  
	  long dictEntryDataStart = termDictionaryRaf.getFilePointer();
	  termDictionaryRaf.writeLong(digestList.size()); // number of postings
	  termDictionaryRaf.writeLong(extentListOffset);  // offset to begining of extent list
	  datalength = termDictionaryRaf.getFilePointer() - dictEntryDataStart;
	  recordlength = termDictionaryRaf.getFilePointer() - dictEntryStart;
	}
	termDictionaryRaf.close();
	extentsRaf.close();
	BufferedWriter bw =
	  new BufferedWriter
	  (new FileWriter
	   (workingDir + "/indices/" + indexname + "/" + indexname + "-" + 
	    column.toString() + "-" + termLength.toString() + "-term-dictionary-stats.txt"));
	bw.write("termlength|" + termLength + "\n");
	bw.write("reclength|"  + recordlength + "\n");
	bw.write("datalength|" + datalength + "\n");
	bw.write("recordnum|"  + recordnumber + "\n");
	bw.close();
      }
    }
  }


  public List<String> lookup(String workingDir, String indexname,  String term, int column)
    throws IOException, FileNotFoundException
  {
    List<String> resultList = new ArrayList<String>();
    // byte length of utf-8 string
    int bytelength = term.getBytes(this.charset).length;
    String termLengthString = Integer.toString(bytelength);
    String columnString = Integer.toString(column);
      RandomAccessFile termDictionaryRaf = 
	new RandomAccessFile(MultiKeyIndex.partitionPath(workingDir, indexname,
					   columnString, termLengthString, "-term-dictionary"), "r");
    RandomAccessFile extentsRaf = 
      new RandomAccessFile(MultiKeyIndex.partitionPath(workingDir, indexname,
					 columnString, termLengthString, "-postings-offsets"), "r");
    RandomAccessFile postingsRaf = 
      new RandomAccessFile(workingDir + "/indices/" + indexname + "/postings", "r");

    Map<String,String> statsMap;
    String statMapKey = columnString + "|" + termLengthString;
    if (this.mapOfStatMaps.containsKey(statMapKey)) {
      statsMap = this.mapOfStatMaps.get(statMapKey);
    } else {
      statsMap = MultiKeyIndex.readStatsFile
	(MultiKeyIndex.partitionPath
	 (workingDir, indexname,
	  columnString, termLengthString, "-term-dictionary-stats.txt"));
      this.mapOfStatMaps.put(statMapKey, statsMap);
    }


    int datalength = Integer.parseInt(statsMap.get("datalength"));
    int recordnum = Integer.parseInt(statsMap.get("recordnum"));
    
    DictionaryEntry entry = 
      MultiKeyIndex.dictionaryBinarySearch(termDictionaryRaf, term, 
			     bytelength, datalength, recordnum );
    if (entry != null) {
      MultiKeyIndex.readPostings(extentsRaf, postingsRaf, resultList, entry, charset);
    }
    termDictionaryRaf.close();
    extentsRaf.close();
    postingsRaf.close();
    return resultList;
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

  public static void usage() {
    System.out.println("Usage: build workingdir indexname");
    System.out.println("       lookup workingdir indexname term");
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
    Charset charset = Charset.forName("utf-8");
    if (args.length > 2) {
      String option = args[0];
      String workingDir = args[1];
      String indexName = args[2];
      System.out.println("option: " + option);
      System.out.println("workingDir: " + workingDir);
      System.out.println("indexname: " + indexName);

      Map<String,String []> tableConfig = Config.loadConfig(workingDir + "/tables/ifconfig");
      String[] tableFields = tableConfig.get(indexName);
      if (option.equals("build")) {
	if (tableFields != null) {
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
	  System.out.println("writing partitions for columns " + renderColumns(columns) ); 
	  Set<String> columnLengthKeys = writeTemporaryPartitionsTables(workingDir, indexName, recordTable, columns, charset);
	  System.out.println("writing final index");
	  writeFinalIndex(workingDir, indexName, columnLengthKeys, charset);
	  // System.out.println("Generating maps for columns " + renderColumns(columns) );
	  // instance.generateMaps(recordTable, columns);
	  // Map<String,Extent> digestExtentMap = instance.writePostings(workingDir, indexName);
	  // instance.writePartitions(workingDir, indexName, digestExtentMap);
	} else {
	  System.out.println("table entry for index " + indexName + " is not present in configuration file: ifconfig.");
	}
      } else if (option.equals("lookup")) {
	if (args.length > 5) {
	  String column = args[3];
	  System.out.println("column: " + column);
	  StringBuilder termBuf = new StringBuilder();
	  for (int i = 4; i < args.length; i++) {
	    termBuf.append(args[i]).append(" ");
	  }
	  String term = termBuf.toString().trim();
	  System.out.println("term: " + term);
	  MappedMultiKeyIndexDiskBasedGeneration instance = new MappedMultiKeyIndexDiskBasedGeneration();
	  List<String> resultList = instance.lookup(workingDir, indexName, term.toLowerCase(), Integer.parseInt(column));
	  for (String result: resultList) {
	    System.out.println(result);
	  }
	} else {
	  System.out.println("missing arguments for lookup.");
	  usage();
	}
      } else {
	System.out.println("Unknown option.");
	usage();
      }
    } else {
      usage();
    }
  }
}
