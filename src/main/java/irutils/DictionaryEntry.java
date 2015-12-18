package irutils;

 /**
 * DictionaryEntry.java
 *
 *
 * Created: Mon Aug 27 13:07:37 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: DictionaryEntry.java,v 1.2 2001/09/07 13:32:20 wrogers Exp $
 */

public class DictionaryEntry
{

  // Organization of dictionary one record:
  //  +------------------------+--------------------+-------------------+
  //  | term                   | number of postings |     address       |
  //  +------------------------+--------------------+-------------------+
  //  |<<--- term length ---->>|<<--- 8 bytes ---->>|<<--- 8 bytes --->>|
  //  |<<------------------------- record length --------------------->>|
  //
  //  Term Length, # of postings And addr are the same for all records in a partition.

  String term;
  long numberOfPostings;
  long address;
  
  public DictionaryEntry (String term, long numberOfPostings, long address)
  {
    this.term = term;
    this.numberOfPostings = numberOfPostings;
    this.address = address;
  }
  
  public String getTerm()
  {
    return this.term;
  }

  public long getNumberOfPostings()
  {
    return this.numberOfPostings;
  }
  public long getAddress()
  {
    return this.address;
  }
  public String toString() {
    return term + "\t" + numberOfPostings + "\t" + address;
  }
}// DictionaryEntry



