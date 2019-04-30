
//
package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.text.*;

/**
 * Generate table consisting records each with a pair containing a
 * concept unique identifier and the preferred name for that concept.
 *
 * <table>
 *  <caption>MRCONSO fields used:</caption>
 *  <tr><th>CUI <td>concept unique identifier <td>field 0</tr>
 *  <tr><th>LAT <td>language of term          <td>field 1</tr>
 *  <tr><th>TS  <td>term status               <td>field 2</tr>
 *  <tr><th>STT <td>string type               <td>field 4</tr>
 *  <tr><th>STR <td>string                    <td>field 14</tr>
 * </table>
 *
 * <table>
 *  <caption>Format of cui -&gt; preferred name output file</caption>
 *  <tr><th>CUI <td>concept unique identifier <td>field 0</tr>
 *  <tr><th>STR <td>preferred namd            <td>field 1</tr>
 * </table>
 */

public class ExtractMrconsoPreferredNames {
  /** program usage message */
  private static final String usageMsg = 
    "Usage: ExtractMrconsoPreferredNames [<options>] <infile> <outfile>\n" +
    "\n" +
    "  <infile> should normally be mrconso.eng or the like,\n" +
    "  <outfile>  consists of records of the form\n" +
    "             CUI|STR\n\n where STR is preferred name" +
    "\n" +
    "  ExtractMrconsoPreferredNames options:\n" +
    "  [DEFAULT] -l --language <language> (default: \"ENG\")\n" +
    "            -h --help\n" +
    "            -i --info\n" +
    "            -w --warnings\n" +
    
    "\n";

  /** message log */
  static PrintWriter log = new PrintWriter(new OutputStreamWriter(System.out));
  /** use only term from this language. */
  String language =
    System.getProperty("extract.mrconso.sources.language", "ENG");
  /** true if you wish warnings to be displayed */
  boolean displayWarnings = false;
  /** if true display verbose messages: system property "filter.mrconso.verbose" */
  boolean verbose = 
    System.getProperty("extract.mrconso.sources.verbose", "false").equals("true");
  /** are we threaded? */
  boolean threaded = false;
  /** are we using Rich Release Format (RRF) */
  boolean releaseFormatRRF = true;

  public ExtractMrconsoPreferredNames(String language, boolean displayWarnings, String releaseFormat) {
    this.language = language;
    this.displayWarnings = displayWarnings;
    this.releaseFormatRRF = ! releaseFormat.equals("ORF");
  }

  /**
   * This class represents one record in MRCONSO.
   */
  class CuiInfo
  {
    public String cui;
    public String lat;
    public String ts;
    public String stt;
    public String str;
    public String line;
  }

  /**
   * Returns false if not being run from a thread, true otherwise.
   * @return true if being run as a thread, false if otherwise.
   */
  public boolean isThreaded()
  {
    return this.threaded;
  }

  /**
   * Set thread state of class. I.E. is this being run from a thread.
   * @param isThreaded set to true if are being run from a thread.
   */
  public void setThreaded(boolean isThreaded)
  {
    this.threaded = isThreaded;
  }

  /**
   * process a line.
   * @param line     mrconso record.
   * @return list containing cui, str, sab, tty
   * @throws Exception any exception
   */
  public CuiInfo parseLine(String line)
    throws Exception
  {
    CuiInfo result = new CuiInfo();
    try {
      String[] tokens = line.split("\\|");
      if (this.releaseFormatRRF) {
	result.cui = tokens[0];  // set CUI
	result.lat = tokens[1];  // set LAT
	result.ts  = tokens[2];  // set TS
	result.stt = tokens[4];  // set STT
	result.str = tokens[14]; // set STR
      } else {
	String cls = tokens[0];
	String[] clsTokens = cls.split("\\:");
	result.cui = clsTokens[0]; // set CUI
	result.lat = tokens[1];  // set LAT
	result.ts  = tokens[2];  // set TS
	result.stt = tokens[3];  // set STT
	result.str = tokens[4];	 // set STR
      }
      result.line = line;	// add line to cui info
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println("line: " + line);
      throw exception;
    }
    return result;
  }

  public void writeCuiInfo(PrintWriter outfile, String cui, String preferredName)
  {
    outfile.println(cui + "|" + preferredName);
  }

  /**
   * Process input mrconso file and output cuiinfo -&gt; src file.
   * @param infile   Input mrconso file 
   * @param outfile  Output filtered mrconso file
   * @throws IOException IO exception
   * @throws Exception any exception
   */
  public void processInput(BufferedReader infile, PrintWriter outfile)
    throws IOException, Exception
  {
    String line = null;
    String cui0 = "C......";
    String preferredName = "X";
    String synonym = "X";
    while ((line = infile.readLine()) != null)
      {
	CuiInfo cuiInfo = this.parseLine(line);
	if (cui0.equals(cuiInfo.cui)) {
	  if (cuiInfo.lat.equals(this.language) &&
	      cuiInfo.ts.equals("P") &&
	      cuiInfo.stt.equals("PF")) {
	    preferredName = cuiInfo.str;
	  } else {
	    synonym = cuiInfo.str;
	  }
	} else {
	  if (! cui0.equals("C......")) {
	    if (preferredName.equals("X")) {
	      preferredName = synonym;
	    }
	    writeCuiInfo(outfile, cui0, preferredName);
	    synonym = "X";
	  }
	  cui0 = cuiInfo.cui;
	  if (cuiInfo.lat.equals(this.language) &&
	      cuiInfo.ts.equals("P") &&
	      cuiInfo.stt.equals("PF")) {
	    preferredName = cuiInfo.str;
	  } else {
	    synonym = cuiInfo.str;
	  }
	}
      }
     if (! cui0.equals("C......")) {
       if (preferredName.equals("X")) {
	 preferredName = synonym;
       }
       writeCuiInfo(outfile, cui0, preferredName);
       synonym = "X";
     }
  }

  /**
   * Generate message about options currently enforce.
   * @return string containing message about which options are set.
   */
  public String getOptionsMessage()
  {
    StringBuffer sb = new StringBuffer();
    if (this.displayWarnings)
      {
	sb.append("Control options:\n"); 
	if (this.displayWarnings) sb.append(" display_warnings\n");
	if (this.releaseFormatRRF) sb.append(" RRF format\n");
      }
    return sb.toString();
  }

  public static void createTable(String inFilename, String outFilename,
				 String language, boolean displayWarnings,
				 String releaseFormat)
    throws Exception
  {
    ExtractMrconsoPreferredNames filter = 
      new ExtractMrconsoPreferredNames(language, displayWarnings, releaseFormat);
    filter.setThreaded(false);
    System.out.println(filter.getOptionsMessage());
    System.out.println("Processing " + inFilename + " --> " +
		       outFilename + ".");
    BufferedReader infile =
      new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),
					       Charset.forName("utf-8")));
      PrintWriter outfile = 
	new PrintWriter(new BufferedWriter(new FileWriter(outFilename)));
      filter.processInput(infile, outfile);
      outfile.close();
      infile.close();
  }
  
  /**
   * main program
   * @param args command line arguments
   * @throws Exception any exception
   */
  public static void main(String[] args)
    throws Exception
  {
    String inFilename = null;
    String outFilename = null;
    boolean firstOfEachSourceOnly = true;
    boolean includeSuiInfo = false;
    boolean displayWarnings = false;
    String releaseFormat = "RRF";
    String language = "ENG";

    System.out.println("\nextract mrconso preferred names (Java Prototype)\n");
    int i = 0;
    while (i < args.length && args[i].substring(0,1).equals("-"))
      {
	if (args[i].equals("-l") ||
	    args[i].equals("--language"))
	  {
	    i++;
	    language = args[i];
	    i++;
	  }
	if (args[i].equals("-s") ||
	    args[i].equals("--include_sui_info"))
	  {
	    includeSuiInfo = ! includeSuiInfo; i++;
	  }
	else if (args[i].equals("-h") || args[i].equals("--help"))
	  {
	    System.err.println(usageMsg); i++;
	  }
	else if (args[i].equals("-i") || args[i].equals("--info"))
	  {
	    i++;
	  }
	else if (args[i].equals("-w") || args[i].equals("--warnings"))
	  {
	    displayWarnings = true; i++;
	  }
	else if (args[i].equals("-r") || args[i].equals("--rrf"))
	  {
	    releaseFormat = "RRF";
	  }
	else if (args[i].equals("--orf"))
	  {
	    releaseFormat = "ORF";
	  }
      }
    if (i < args.length) {
      inFilename = args[i]; i++;
    } else {
      System.err.println("ERROR: Mandatory argument\n" +
			 "           infile (Input file similar to mrconso.)\n" +
			 "       has no value\n");
      System.err.println(usageMsg);
      System.exit(-1);
    }
    if (i < args.length) {
      outFilename = args[i]; i++;
    } else {
      System.err.println("ERROR: Mandatory argument\n" +
			 "            outfile (Output file)\n" +
			 "       has no value.\n");
      System.err.println(usageMsg);
      System.exit(-1);
    }
    
    try {
      createTable(inFilename, outFilename, language, displayWarnings, releaseFormat);
    } catch (Exception exception) {
      System.err.println("Exception: " + exception.getMessage());
      exception.printStackTrace(System.err);
      System.exit(-1);
    }
    log.println();
    log.close();
    System.exit(0);
  }

}
