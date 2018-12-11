
//
package gov.nih.nlm.nls.metamap.dfbuilder;

import gov.nih.nlm.nls.metamap.dfbuilder.DefaultSemanticTypesRaw;
import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extract Mrsty Semantic Types
 * Purpose:  Extracts semantic type information from mrsty.eng.
 * <p>
 * MRSTY column
 * <dl>
 * <dt>CUI<dd>Unique indentifier of concept
 * <dt>TUI<dd>Unique indentifier of Semantic Type
 * <dt>STN<dd>Semantic Type tree number.
 * <dt>STY<dd>Semantic Type.
 * <dt>ATUI<dd>Unique indentifier for attribute
 * <dt>CVF<dd>Content View Flag
 * </dl>
 *
 * <table>
 *  <caption>Format of cui -&gt; semantic type output file.</caption>
 *  <tr><th>CUI <td>Concept Unique Identifier <td>field 0</tr>
 *  <tr><th>ST  <td>Semantic Type             <td>field 1</tr>
 * </table>
 */
public class ExtractMrstySemanticTypes {

    /** program usage message */
  private static final String usageMsg = 
    "Usage: ExtractMrstySemanticTypes [<options>] <infile> <outfile>\n" +
    "\n" +
    "  <infile> should normally be mrsty.eng or the like,\n" +
    "  <outfile>  consists of records of the form\n" +
    "             CUI|ST" +
    "\n" +
    "  ExtractMrstySemanticTypes options:\n" +
    "  [DEFAULT] -h --help\n" +
    "            -i --info\n" +
    "            -w --warnings\n" +
    "\n";

  /** message log */
  static PrintWriter log = new PrintWriter(new OutputStreamWriter(System.out));

  /**
   * This class represents one record in MRSTY.
   */
  class CuiInfo
  {
    public String cui;
    public String tui;
    public String stn;
    public String sty;
    public String atui;
    public String cvf;
    public String line;
  }
  
  /** info lines associated with a concept (cui) */
  List<CuiInfo> cuiInfoLines = new ArrayList<CuiInfo>();
  /** map of info lines associated with a concept (cui) */
  Map<String,CuiInfo> cuiInfoMap = null;
  /** true if you wish warnings to be displayed */
  boolean displayWarnings = false;
  /** if true display verbose messages: system property "extract.mrsty.verbose" */
  boolean verbose = 
    System.getProperty("extract.mrstr.verbose", "false").equals("true");
  /** are we threaded? */
  boolean threaded = false;
  /** are we using Rich Release Format (RRF) */
  boolean releaseFormatRRF = true;
 
  /** mapping of TUIs -&gt; semantic type abbreviation for instance */
  Map<String,String> semanticTypeToStAbbrevMap;

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
  
  Map<String,String> loadStRawFile(String stRawFilename)
    throws IOException, FileNotFoundException
  {
    Map<String,String> semTypeToStAbbrevMap = new HashMap<String,String>();
    BufferedReader br = 
      new BufferedReader(new InputStreamReader(new FileInputStream(stRawFilename),
					       Charset.forName("utf-8")));
    String line;
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\\|");
      semTypeToStAbbrevMap.put(fields[0],fields[1]);
    }
    br.close();
    return semTypeToStAbbrevMap;
  }

  public ExtractMrstySemanticTypes(boolean displayWarnings,
				   String releaseFormat,
				   String stRawFilename)
    throws Exception
  {
    this.displayWarnings = displayWarnings;
    this.releaseFormatRRF = releaseFormat.equals("RRF");
    if (stRawFilename != null) {
      this.semanticTypeToStAbbrevMap = loadStRawFile(stRawFilename);
    } else {
      this.semanticTypeToStAbbrevMap = DefaultSemanticTypesRaw.getSemTypeToStAbbrevMap();
    }
  }

  public String getSemanticTypeAbbrev(String semanticTypeName) {
    if (this.semanticTypeToStAbbrevMap.containsKey(semanticTypeName)) {
      return this.semanticTypeToStAbbrevMap.get(semanticTypeName);
    } else {
      return "unk";
    }
  }

  public void writeCuiInfo(PrintWriter outfile, String cui, Set<String> semTypeSet)
  {
    for (String semType: semTypeSet) {
      outfile.println(cui + "|" + semType);
    }
  }

    /**
   * process a line.
   * @param line     mrsty record.
   * @return instance containing cui, tui, stn, sty, atui
   * @throws Exception any exception
     */
  public CuiInfo parseLine(String line)
    throws Exception
  {
    CuiInfo result = new CuiInfo();
    try {
      String[] tokens = line.split("\\|",6);
      if (this.releaseFormatRRF) {
	result.cui = tokens[0];  // set CUI
	result.tui = tokens[1];  // set TUI
	result.stn = tokens[2];	 // set STN
	result.sty = tokens[3];	 // set STY
	result.atui = tokens[4]; // set ATUI
	result.cvf = tokens[5];	 // set cvf
      } else {
	String cls = tokens[0];
	String[] clsTokens = cls.split("\\:");
	result.cui = tokens[0];  // set CUI
	result.tui = tokens[1];  // set TUI
	result.stn = tokens[2];	 // set STN
	result.sty = tokens[3];	 // set STY
	result.atui = tokens[4]; // set ATUI
	result.cvf = tokens[5];	 // set cvf
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println("line: " + line);
      throw exception;
    }
    return result;
  }


  /**
   * Process input mrsty file and output cuiinfo -&gt; src file.
   * @param infile   Input mrsty file 
   * @param outfile  Output filtered mrsty file
   * @throws Exception any exception
   * @throws IOException Input/Output exception
   */
  public void processInput(BufferedReader infile, PrintWriter outfile)
    throws IOException, Exception
  {
    String line = null;
    String cui0 = "C......";
    Set<String> semTypeSet = new TreeSet<String>();

    while ((line = infile.readLine()) != null)
      { 
	CuiInfo cuiInfo = this.parseLine(line);
	cuiInfo.line = line;	// add line to cui info
	if (cui0.equals(cuiInfo.cui)) {
	  semTypeSet.add(this.getSemanticTypeAbbrev(cuiInfo.sty));
	} else {
	  if (! cui0.equals("C......")) {
	    writeCuiInfo(outfile, cui0, semTypeSet);
	  }
	  cui0 = cuiInfo.cui;
	  semTypeSet.clear();
	  semTypeSet.add(this.getSemanticTypeAbbrev(cuiInfo.sty));
	}
      }
     if (! cui0.equals("C......")) {
       writeCuiInfo(outfile, cui0, semTypeSet);
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

  static void createTable(String inFilename, String outFilename,
			  boolean displayWarnings,
			  String releaseFormat,
			  String stRawFilename)
    throws IOException, Exception
  {
    ExtractMrstySemanticTypes filter = 
      new ExtractMrstySemanticTypes(displayWarnings, releaseFormat, stRawFilename);
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
    boolean displayWarnings = false;
    String releaseFormat = "RRF";
    String stRawFilename = null;

    System.out.println("\nextract mrsty sources (Java Prototype)\n");
    int i = 0;
    while (i < args.length && args[i].substring(0,1).equals("-"))
      {
	
	if (args[i].equals("-h") || args[i].equals("--help"))
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
	    releaseFormat = "RRF"; i++;
	  }
	else if (args[i].equals("--orf"))
	  {
	    releaseFormat = "ORF"; i++;
	  }
	else if (args[i].equals("--st_raw_file"))
	  {
	    i++;
	    stRawFilename = args[i];
	    i++;
	  }
      }
    if (i < args.length) {
      inFilename = args[i]; i++;
    } else {
      System.err.println("ERROR: Mandatory argument\n" +
			 "           infile (Input file similar to mrsty.)\n" +
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
      createTable(inFilename, outFilename, displayWarnings,
		  releaseFormat, stRawFilename);
      log.println();
      log.close();
    } catch (Exception exception) {
      System.err.println("Exception: " + exception.getMessage());
      exception.printStackTrace(System.err);
      System.exit(-1);
    }
    System.exit(0);
  }

}
