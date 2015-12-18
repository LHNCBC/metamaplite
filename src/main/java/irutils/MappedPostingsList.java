package irutils;

import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Implementation of an Unmodifiable AbstractList for lists of "IR"
 * document postings.
 *
 *
 * Created: Fri Aug 31 17:22:28 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: MappedPostingsList.java,v 1.1 2008/04/25 14:57:52 wrogers Exp $
 */

public class MappedPostingsList extends AbstractList implements List {

  /** offset in file to beginning of postings list */
  private int address;
  /** number of postings in list */
  private int count;
  /** absolute offset (addresses) in postings file of postings in this
   * list. */
  private int[] offsets;
  /** byte lengths of of postings in this list */
  private int[] lengths;
  /** random access file object for postings file */
  private ByteBuffer buffer;

  /**
   * Constructor.
   * @param postingsBuffer random access file object for postings file 
   * @param postingsAddress offset in file to beginning of postings list
   * @param postingsCount number of postings in list
   */
  public MappedPostingsList (ByteBuffer postingsBuffer, int postingsAddress, int postingsCount)
    throws IOException
  {
    this.address = postingsAddress;
    this.count = postingsCount;
    this.buffer = postingsBuffer;
    // System.out.println("this.file: " + this.file);
    this.offsets = new int[postingsCount];
    this.lengths = new int[postingsCount];
    buffer.position(this.address); int offset = this.address;
    for (int i = 0; i < count; i++)
      {
	this.offsets[i] = offset;
	int postingsLen = this.buffer.getInt();
	this.lengths[i] = postingsLen;
	// System.out.println("postingsLen : " + postingsLen);
	byte[] databuf = new byte[postingsLen];
	this.buffer.get(databuf);
	offset = offset + 4 + postingsLen;
      }
    
  }
  /** @return size of postings list. */
  public int size()
  {
    return this.count;
  }
    @Override
  public void     add(int index, Object element) {  }
    @Override
  public boolean  add(Object o)  { return false; }
    @Override
  public boolean  addAll(int index, Collection c) { return false; }
    @Override
  public void     clear() {}
    @Override
  public boolean  equals(Object o) { return false; }
  /** 
   * get posting at index. 
   *  @return a posting (A String object)
   */
  public Object   get(int index) 
  { 
    try {
      this.buffer.position(this.offsets[index] );
      int postingsLen = this.buffer.getInt(); 
      byte[] databuf = new byte[postingsLen];
      this.buffer.get(databuf);
      return new String(databuf);
    } catch ( Exception exception ) {
      System.err.println("IOException: " + exception.getMessage() );
      return null;
    }
  } 
    @Override
  public int      hashCode() { return 0; }
    @Override
  public int      indexOf(Object o) { return 0;}

  /** 
   * get an iterator over the current posting list.
   * @return iterator over postings list.
   */
    @Override
  public Iterator iterator()
  { 
    try {
      return new MappedPostingsListIterator(this.buffer, 
					    this.address, this.count, this.offsets); 
    } catch ( IOException exception ) { 
      return null; 
    } 
  }
    @Override
  public int      lastIndexOf(Object o) { return 0;  }

  /** 
   * get an list iterator over the current posting list.
   * @return list iterator over postings.
   */
    @Override
  public ListIterator    listIterator() 
  {
    try {
      return new MappedPostingsListIterator(this.buffer, 
				      this.address, this.count, this.offsets); 
    } catch ( IOException exception ) { 
      System.err.println("exception occurred while creating list iterator: " + 
			 exception.getMessage()); 
      return null; 
    } 
  }
  /** 
   * get an list iterator over the current posting list, starting a index.
   * @param index index to start iterating at.
   * @return list iterator over postings.
   */
    @Override
  public ListIterator    listIterator(int index) { 
    try {
      return new MappedPostingsListIterator(this.buffer, 
				      this.address, this.count, this.offsets,
				      index); 
    } catch ( IOException exception ) { 
      System.err.println("exception occurred while creating list iterator: " +
			 exception.getMessage()); 
      return null; 
    } 
  }
    @Override
  public Object	 remove(int index) {
    // not implemented
    return get(index); 
  }
    @Override
  protected void removeRange(int fromIndex, int toIndex) 
  {
    // not implemented
  }
    @Override
  public Object	  set(int index, Object element) 
  {
    // not implemented
    return null;
  }
    @Override
  public List	   subList(int fromIndex, int toIndex) 
  { 
    try {
      return new MappedPostingsList(this.buffer, this.offsets[fromIndex], toIndex - fromIndex);
    } catch (IOException exception) {
      System.err.println("exception occurred while creating sub list: " + 
			 exception.getMessage()); 
      return null;
    }
  }

  private class MappedPostingsListIterator implements Iterator, ListIterator
    {
      /** offset in file to beginning of postings list */
      int address;
      /** number of postings in list */
      int count;
      /** current index in postings */
      int index = 0;
      /** absolute offset (addresses) in postings file of postings in this
       * list. */
      int[] offsets;
      /** random access file object for postings file */
      ByteBuffer buffer;

      public MappedPostingsListIterator(ByteBuffer buffer, 
				  int address, int count, int offsets[])
	throws IOException
      {
	this.count = count;
	this.address = address;
	this.offsets = offsets;
	this.buffer = buffer; 
	this.buffer.position(address);
      }
      public MappedPostingsListIterator(ByteBuffer buffer, 
				  int address, int count, int offsets[],
				  int index)
	throws IOException
      {
	this.count = count;
	this.address = address;
	this.offsets = offsets;
	this.index = index;
	this.buffer = buffer; 
	this.buffer.position(offsets[index]);
      }
      /** non-implementation of interface ListIterator */
      public void add(Object o)
      {
	// not implemented
      }
      
      /** implementation of interface Iterator */
      public boolean hasNext()
      {
	//	System.out.println("MappedPostingsList " + this + " .hasNext() => " +
	// (this.index < this.count) );
	return (this.index < this.count);
      }

      /** implementation of interface ListIterator */
      public boolean hasPrevious()
      {
	return (this.index > 0);
      }

      /** implementation of interface Iterator */
      public Object next()
	throws NoSuchElementException
      {
	if (this.index == this.count)
	  {
	    throw new NoSuchElementException("at end of list.");
	  } 
	this.index++;
	try {
	  int postingsLen = this.buffer.getInt();  
	  byte[] databuf = new byte[postingsLen]; 
	  this.buffer.get(databuf); 
	  return new String(databuf); 
	} catch ( Exception exception ) {
	  System.out.println("exception: " + exception);
	  return null;
	}
      }
      /** implementation of interface ListIterator */
      public int nextIndex()
      {
	return this.index + 1;
      }
      /** implementation of interface ListIterator */
      public Object previous()
	throws NoSuchElementException
      {
	if (this.index == 0)
	  {
	    throw new NoSuchElementException("at beginning of list.");
	  } 
	this.index--;
	try {
	  this.buffer.position( this.offsets[index] );
	  int postingsLen = this.buffer.getInt();  
	  byte[] databuf = new byte[postingsLen]; 
	  this.buffer.get(databuf); 
	  return new String(databuf); 
	} catch ( Exception exception ) {
	  return null;
	}
      }
      /** implementation of interface ListIterator */
      public int previousIndex()
      {
	return this.index - 1;
      }
      /** implementation of interface Iterator */
      public void remove()
      {
	// not implemented
      }
      public void set(Object o)
      {
	// not implemented
      }
    }

}// MappedPostingsList
