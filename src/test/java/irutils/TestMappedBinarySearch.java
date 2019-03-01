package irutils;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
  
  
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Before;

/**
 * TestMappedBinarySearch - Test MappedBinarySearch with file
 * containing a list of terms and data built on-the-fly.  Currently
 * tests irutils.MappedFileBinarySearch.dictionaryBinarySearch.
 *
 * Created: Thu Nov 15 10:07:16 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
// @Runwith(JUnit4.class)
public class TestMappedBinarySearch {

  String testdir = "/tmp/binsearchtest";
  Charset charset = Charset.forName("utf-8");

  String[] termList = new String[] { "firewood", "lifespan", "obstructive",
				     "résumé",
				     "sjögren",
				     "sjögren's",
				     "sjögren's diease",
				     "testword", "toroidal" };

  /**
   * Creates a new <code>TestMappedBinarySearch</code> instance.
   *
   */
  public TestMappedBinarySearch() {
  }

  /**
   * @param ras randon access files
   * @param text key text
   * @param count number of postings
   * @param address postings address
   * @throws IOException I/O Exception
   */
  public void writeEntry(RandomAccessFile ras,
			 byte[] bytes,
			 int count,
			 int address) throws IOException {
    ras.write(bytes);
    ras.writeInt(count);
    ras.writeInt(address);
  }

  static class Entry {
    public final byte[] bytes; public final int count; public final int address;
    Entry(byte[] bytes, int count, int address) {
      this.bytes = bytes;
      this.count = count;
      this.address = address;
    }
  }

  Map<Integer,List<Entry>> entryListMap = new HashMap<Integer,List<Entry>>();

  void addEntry(Map<Integer,List<Entry>> entryListMap, String text, int count, int address) {
    byte[] bytes = text.getBytes(this.charset);
    System.out.println("TestMappedBinarySearch: addEntry: text: " + text + ", bytelength: " + bytes.length);
    Integer slotKey = new Integer(bytes.length);
    if (entryListMap.containsKey(slotKey)) {
      entryListMap.get(slotKey).add(new Entry(bytes, count, address));
    } else {
      List<Entry> newList = new ArrayList<Entry>();
      newList.add(new Entry(bytes, count, address));
      entryListMap.put(slotKey, newList);
    }
  }

  /**
   * Build test array on disk, the array file containing rows the
   * consisting of word, count, and offset.
   *
   * <pre>
   * +----------------+-------+---------+
   * |    word        | count | address |
   * +----------------+-------+---------+
   * | "heart attack" |   3   | 456f4   |
   * +----------------+-------+---------+
   * </pre>
   */
  @org.junit.Before public void setup() {
    addEntry(entryListMap, "firewood", 1, 0);
    addEntry(entryListMap, "lifespan", 2, 10);
    addEntry(entryListMap, "obstructive", 4, 5);
    addEntry(entryListMap, "résumé", 2, 20);
    addEntry(entryListMap, "sjögren", 4, 5);
    addEntry(entryListMap, "sjögren's", 4, 5);
    addEntry(entryListMap, "sjögren's diease", 4, 5);
    addEntry(entryListMap, "testword", 1, 0);
    addEntry(entryListMap, "toroidal", 1, 30);
   try {
     for (Map.Entry<Integer,List<Entry>> slotEntry: entryListMap.entrySet()) {
	RandomAccessFile ras = new RandomAccessFile(testdir + slotEntry.getKey(), "rw");
	for (Entry entry: slotEntry.getValue()) {
	  writeEntry(ras, entry.bytes, entry.count, entry.address);
	}
	ras.close();
	PrintWriter statsfp =
	  new PrintWriter(new FileWriter(testdir + slotEntry.getKey() + ".stats"));
	statsfp.println(slotEntry.getValue().size());
	statsfp.close();
      }
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * @param raf random access file
   * @return text key text
   * @throws IOException I/O Exception
   */
  String readEntry(RandomAccessFile raf, int buflen) throws IOException {
    byte[] buf = new byte[buflen];
    raf.read(buf);
    raf.readInt();
    raf.readInt();
    return new String(buf, this.charset);
  }

  RandomAccessFile rafOpenIndex(Map<Integer,RandomAccessFile> indexMap, String text)
    throws FileNotFoundException {
    byte[] bytes = text.getBytes(this.charset);
    System.out.println("TestMappedBinarySearch: rafOpenIndex: text: " + text + ", bytelength: " + bytes.length);
    Integer slotKey = new Integer(bytes.length);
    if (indexMap.containsKey(slotKey)) {
      return indexMap.get(slotKey);
    } else {
      RandomAccessFile raf = new RandomAccessFile(testdir + slotKey, "r");
      indexMap.put(slotKey, raf);
      return raf;
    }
  }
  void rafCloseAll(Map<Integer,RandomAccessFile> indexMap)
    throws IOException {
    for (RandomAccessFile raf: indexMap.values()) {
      raf.close();
    }
  }
  

  @org.junit.Test public void test1() {
    Map<Integer,RandomAccessFile> indexMap = new HashMap<Integer,RandomAccessFile>();
    try {
      for (String target: termList) {
	RandomAccessFile raf = rafOpenIndex(indexMap, target);
	byte[] bytes = target.getBytes(this.charset);
	String lstring = readEntry(raf, bytes.length);
	System.out.println("TestMappedBinarySearch:test1:lstring: \"" + lstring +
			   "\" == target: \"" + target + "\"");
	System.out.flush();
	System.err.println("TestMappedBinarySearch:test1:lstring: \"" + lstring +
			   "\" == target: \"" + target + "\"");
	System.err.flush();
	org.junit.Assert.assertTrue(lstring.equals(target));
      }
      rafCloseAll(indexMap);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  static class MMapIndex {
    public final ByteBuffer byteBuffer;
    public final int arraylen;
    public MMapIndex(ByteBuffer byteBuffer, int arraylen) {
      this.byteBuffer = byteBuffer;
      this.arraylen = arraylen;
    }
  }

  MMapIndex memmapOpenIndex(Map<Integer,MMapIndex> indexMap, String text)
    throws FileNotFoundException, IOException {
    byte[] bytes = text.getBytes(this.charset);
    System.out.println("TestMappedBinarySearch: memmapOpenIndex: text: " + text + ", bytelength: " + bytes.length);
    Integer slotKey = new Integer(bytes.length);
    if (indexMap.containsKey(slotKey)) {
      return indexMap.get(slotKey);
    } else {
      BufferedReader bw = 
	new BufferedReader(new FileReader(testdir + slotKey + ".stats"));
      String line = bw.readLine();
      bw.close();
      int arraylen = Integer.parseInt(line);
      FileChannel fileChannel = (new FileInputStream(testdir + slotKey)).getChannel();
      ByteBuffer byteBuffer =
	fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)fileChannel.size());
      fileChannel.close();
      MMapIndex newIndex = new MMapIndex(byteBuffer, arraylen);
      indexMap.put(slotKey, newIndex);
      return newIndex;
    }
  }

  @org.junit.Test public void test2() {
    Map<Integer,MMapIndex> indexMap = new HashMap<Integer,MMapIndex>();
    try {
      for (String target: termList) {
	MMapIndex index = memmapOpenIndex(indexMap, target);
	ByteBuffer byteBuffer = index.byteBuffer;
	int arraylen = index.arraylen;
	System.out.println("target: " + target);
	byte[] bytes = target.getBytes(this.charset);
	DictionaryEntry entry =
	  MappedFileBinarySearch.dictionaryBinarySearch(byteBuffer, target, bytes.length, arraylen, this.charset);
	if (entry != null) {
	  String lstring = entry.getTerm();
	  org.junit.Assert.assertTrue(lstring.equals(target));
	  System.out.println(entry);
	} else {
	  org.junit.Assert.assertTrue(false);
	}
      }
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }  
}
