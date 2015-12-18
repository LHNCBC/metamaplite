package irutils;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * MappedFileBinarySearch.java
 *
 *
 * Created: Wed Jul 25 11:15:59 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: MappedFileBinarySearch.java,v 1.1 2008/04/25 14:57:52 wrogers Exp $
 */

public final class MappedFileBinarySearch extends Object
{

  /**
   *  MappedFile based binary search implementation
   *
   * @param byteBuf    in memory byte buffer
   * @param word       search word
   * @param wordlen    wordlength
   * @param numrecs    number of records in table
   * @param datalen    length of associated data
   * @return byte array containing binary data
   *          associated with search word or null if term not found.
   */
  public static byte[] binarySearch(ByteBuffer byteBuf, String word, int wordlen, int numrecs, int datalen)
    throws IOException
  {
    // d1 or i1 if double then bytelen is 8 else int of bytelen 4.
    int low = 0;
    int high = numrecs;
    int cond;
    int mid;
    byte[] wordbuf = new byte[wordlen];
    String tstword;
    byte[] data = new byte[datalen];

    while ( low < high )
      {
	mid = low + (high- low) / 2;
	byteBuf.position(mid * (wordlen+datalen));
	byteBuf.get(wordbuf);
	tstword = new String(wordbuf);
	cond = word.compareTo(tstword);
	if (cond < 0) {
	  high = mid;
	} else if (cond > 0) {
	  low = mid + 1;
	} else {
	  byteBuf.get(data);
	  return data;
	}
      }
    return null;
  }

  /**
   *  MappedFile based binary search implementation
   *
   * @param byteBuf       file pointer for binary search table
   * @param word       search word
   * @param wordlen    wordlength
   * @param numrecs    number of records in table
   * @return int containing address of posting, -1 if not found.
   */
  public static int intBinarySearch(ByteBuffer byteBuf, String word, int wordlen, int numrecs)
    throws IOException
  {
    // d1 or i1 if double then bytelen is 8 else int of bytelen 4.
    int datalen = 4;
    int low = 0;
    int high = numrecs;
    int cond;
    int mid;
    byte[] wordbuf = new byte[wordlen];
    String tstword;

    while ( low < high )
      {
	mid = low + (high- low) / 2;
	byteBuf.position(mid * (wordlen+datalen));
	byteBuf.get(wordbuf);
	tstword = new String(wordbuf);
	cond = word.compareTo(tstword);
	if (cond < 0) {
	  high = mid;
	} else if (cond > 0) {
	  low = mid + 1;
	} else {
	  return byteBuf.getInt();
	}
      }
    return -1;
  }

  /**
   *  MappedFile based binary search implementation
   *
   * @param byteBuf       file pointer for binary search table
   * @param word       search word
   * @param wordlen    wordlength
   * @param numrecs    number of records in table
   * @return int containing address of posting, -1 if not found.
   */
  public static DictionaryEntry
    dictionaryBinarySearch(ByteBuffer byteBuf, String word, 
			   int wordlen, int numrecs)
    throws IOException
  {
    int datalen = 8; // postings (integer[4 bytes]) + address (integer[4 bytes])
    int low = 0;
    int high = numrecs;
    int cond = -1;
    int mid;
    byte[] tstwordbytes = new byte[wordlen];
    byte[] wordbytes = word.getBytes();

    while ( low < high )
      {
	mid = low + (high- low) / 2;
	byteBuf.position(mid * (wordlen+datalen));
	byteBuf.get(tstwordbytes);
	// System.out.println(" byte array comparison, both byte arrays must be equal.");
	int i = 0;
	while (i < wordlen) {
	  if ( wordbytes[i] != tstwordbytes[i] ) {
	    cond = wordbytes[i] - tstwordbytes[i];
	    break;
	  } else {
	    cond = 0;
	  }
	  i++;
	}
	if (cond < 0) {
	  high = mid;
	} else if (cond > 0) {
	  low = mid + 1;
	} else {
	  int count = byteBuf.getInt();
	  int address = byteBuf.getInt();
	  return new DictionaryEntry(word, count, address);
	}
      }
    return null;
  }
} // MappedFileBinarySearch
