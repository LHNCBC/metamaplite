package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.text.*;

/**
 * File:     extract_mrconso_sources.pl
 * Module:   Extract Mrconso Sources
 * Author:   Lan (tranlated by Willie Rogers)
 * Purpose:  Extracts source information from mrconso.eng.
 * $Id: ExtractMrconsoSources.java,v 1.4 2005/04/22 14:41:00 wrogers Exp $
 */

/**
 * Generate table consisting records each with a pair containing a
 * concept unique identifier, string unique identifier
 *
 * <table>
 *  <caption>MRCONSO fields used:</caption>
 *  <tr><th>CUI <td>concept unique identifier <td>field 0</tr>
 *  <tr><th>LAT <td>language of term          <td>field 1</tr>
 *  <tr><th>SUI <td>string unique identifier  <td>field 5</tr>
 *  <tr><th>STR <td>string                    <td>field 14</tr>
 *  <tr><th>SAB <td>source abbreviation       <td>field 11</tr>
 *  <tr><th>TTY <td>source term type          <td>field 12</tr>
 * </table>
 *
 * <table>
 *  <caption>Format of cui -&gt; sui, str, sab, tty output file.</caption>
 *  <tr><th>CUI <td>concept unique identifier <td>field 0</tr>
 *  <tr><th>SUI <td>string unique identifier  <td>field 1</tr>
 *  <tr><th>STR <td>string                    <td>field 2</tr>
 *  <tr><th>SAB <td>source abbreviation       <td>field 3</tr>
 *  <tr><th>TTY <td>source term type          <td>field 4</tr>
 * </table>
 */


public class ExtractMrconsoSources
{
  /** program usage message */
  private static final String usageMsg = 
    "Usage: ExtractMrconsoSources [<options>] <infile> <outfile>\n" +
    "\n" +
    "  <infile> should normally be mrconso.eng or the like,\n" +
    "  <outfile>  consists of records of the form\n" +
    "             CUI|I|STR|SAB|TTY\n\n" +
    "             or\n\n" +
    "             CUI|SUI|I|STR|SAB|TTY (-s option)\n" +
    "\n" +
    "  ExtractMrconsoSources options:\n" +
    "  [DEFAULT] -f --first_of_each_source_only\n" +
    "            -s --include_sui_info (turns off --first_of_each_source_only)\n" +
    "            -h --help\n" +
    "            -i --info\n" +
    "            -w --warnings\n" +
    
    "\n";

  /** message log */
  static PrintWriter log = new PrintWriter(new OutputStreamWriter(System.out));
  /** info lines associated with a concept (cui) */
  List<CuiInfo> cuiInfoLines = new ArrayList<CuiInfo>();
  /** map of info lines associated with a concept (cui) */
  Map<String,CuiInfo> cuiInfoMap = null;
  boolean firstOfEachSourceOnly = false;
  boolean includeSuiInfo = false;
  /** true if you wish warnings to be displayed */
  boolean displayWarnings = false;
  /** if true display verbose messages: system property "filter.mrconso.verbose" */
  boolean verbose = 
    System.getProperty("extract.mrconso.sources.verbose", "false").equals("true");
  /** are we threaded? */
  boolean threaded = false;
  /** are we using Rich Release Format (RRF) */
  boolean releaseFormatRRF = true;

  public ExtractMrconsoSources(boolean firstOfEachSourceOnly, 
			       boolean includeSuiInfo,
			       boolean displayWarnings,
			       String releaseFormat)
    throws Exception
  {
    this.firstOfEachSourceOnly = firstOfEachSourceOnly;
    this.includeSuiInfo = includeSuiInfo;
    if (this.includeSuiInfo) {
      this.firstOfEachSourceOnly = false;
    }
    this.displayWarnings = displayWarnings;
    this.releaseFormatRRF = releaseFormat.equals("RRF");
  }

  /**
   * Generate message about options currently enforce.
   * @return string containing message about which options are set.
   */
  public String getOptionsMessage()
  {
    StringBuffer sb = new StringBuffer();
    if (this.firstOfEachSourceOnly || this.includeSuiInfo || this.displayWarnings)
      {
	sb.append("Control options:\n"); 
	if (this.firstOfEachSourceOnly) sb.append(" first_of_each_source_only\n");
	if (this.includeSuiInfo) sb.append(" include_sui_info\n");
	if (this.displayWarnings) sb.append(" display_warnings\n");
	if (this.releaseFormatRRF) sb.append(" RRF format\n");
      }
    return sb.toString();
  }


  /**
   * Process input mrconso file and output cuiinfo -&gt; src file.
   * @param infile   Input mrconso file 
   * @param outfile  Output filtered mrconso file
   * @throws Exception any exception
   * @throws IOException IO exception
   */
  public void processInput(BufferedReader infile, PrintWriter outfile)
    throws IOException, Exception
  {
    String line = null;
    String cui0 = "C......";
    while ((line = infile.readLine()) != null)
      { 
	CuiInfo cuiInfo = this.parseLine(line);
	cuiInfo.line = line;	// add line to cui info
	if (cui0.equals(cuiInfo.cui)) {
	  if (this.firstOfEachSourceOnly) { // test to see if source is already there
	    if (! this.cuiInfoMap.containsKey(cuiInfo.sab)) {
	      this.cuiInfoMap.put(cuiInfo.sab, cuiInfo);
	      this.cuiInfoLines.add(cuiInfo);
	    }
	  } else { // always add source line.
	      this.cuiInfoMap.put(cuiInfo.sab, cuiInfo);
	      this.cuiInfoLines.add(cuiInfo);
	  }
	} else {
	  if (! cui0.equals("C......")) {
	    writeCuiInfo(outfile);
	  }
	  cui0 = cuiInfo.cui;
	  this.cuiInfoMap = new HashMap<String,CuiInfo>(2);
	  this.cuiInfoLines.removeAll(this.cuiInfoLines);
	  this.cuiInfoMap.put(cuiInfo.sab, cuiInfo);
	  this.cuiInfoLines.add(cuiInfo);
	}
      }
     if (! cui0.equals("C......")) {
       writeCuiInfo(outfile);
     }
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
	result.sui = tokens[5];  // set SUI
	result.sab = tokens[11]; // set SAB
	result.tty = tokens[12]; // set TTY
	result.str = tokens[14]; // set STR
      } else {
	String cls = tokens[0];
	String[] clsTokens = cls.split("\\:");
	result.cui = clsTokens[0]; // set CUI
	result.sui = clsTokens[2]; // set SUI
	result.str = tokens[4];    // set STR
	result.sab = tokens[5];    // set SAB
	result.tty = tokens[6];    // set TTY
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println("line: " + line);
      throw exception;
    }
    return result;
  }

  public void writeCuiInfo(PrintWriter outfile)
  {
    /* I so want to use generics here.  (couldn't I just use lisp?) */
    int n = 1;
    Iterator<CuiInfo> cuiInfoIter = this.cuiInfoLines.iterator();
    if (this.includeSuiInfo) {
      while (cuiInfoIter.hasNext()) {
	CuiInfo ci = cuiInfoIter.next();
	outfile.println(ci.cui + "|" + ci.sui + "|" + n + "|" +
			ci.str + "|" + ci.sab + "|" + ci.tty);
	n++;
      }
    } else {
      while (cuiInfoIter.hasNext()) {
	CuiInfo ci = cuiInfoIter.next();
	outfile.println(ci.cui + "|" + n + "|" +
			ci.str + "|" + ci.sab + "|" + ci.tty);
	n++;
      }
    }
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
   * This class represents one record in MRCONSO.
   */
  class CuiInfo
  {
    public String cui;
    public String line;
    public String str;
    public String sab;
    public String tty;
    public String sui;
  }

  public static void createTable(String inFilename, String outFilename,
				 boolean firstOfEachSourceOnly,
				 boolean includeSuiInfo,
				 boolean displayWarnings,
				 String releaseFormat)
    throws Exception
  {
    ExtractMrconsoSources filter = 
      new ExtractMrconsoSources(firstOfEachSourceOnly, includeSuiInfo,
				displayWarnings, releaseFormat);

    filter.setThreaded(false);
    System.out.println(filter.getOptionsMessage());
    System.out.println("Processing " + inFilename + " --> " +
		       outFilename + ".");
    BufferedReader infile =
      new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),
					       Charset.forName("utf-8")));
    PrintWriter outfile = 
      new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFilename),
					     Charset.forName("utf-8")));
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
    boolean includeSuiInfo = true;
    boolean displayWarnings = true;
    String releaseFormat = "RRF";

    System.out.println("\nextract mrconso sources (Java Prototype)\n");
    int i = 0;
    while (i < args.length && args[i].substring(0,1).equals("-"))
      {
	if (args[i].equals("-f") ||
	    args[i].equals("--first_of_each_source_only"))
	  {
	    firstOfEachSourceOnly = ! firstOfEachSourceOnly; i++;
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
      createTable(inFilename, outFilename,
		  firstOfEachSourceOnly, includeSuiInfo,
		  displayWarnings, releaseFormat);
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


