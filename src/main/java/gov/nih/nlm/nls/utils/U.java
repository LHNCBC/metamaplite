package gov.nih.nlm.nls.utils;

import java.text.*;
import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.lang.Process;

/* ================================================|U.java |==== */
/**
* U is a helper static class to make newlines, path separators, and the
* like as short and unobtrusive a call in the source code as possible.
* 
* NewLines are different on different platforms and the call to deterine
* them is obtrusive.  This static call should make it easier to do
* and in doing so, should eliminate the temptation to just code a "\n"
* and hope that this does not get run on an NT. 
*
* Concatinating strings together looks so easy when the + symbol is
* used, but it is really inefficient. Several concat methods are
* here to put strings together efficiently.
*
*       Fri Jun 06 10:46:45 EDT 2000,     divita     Initial Version
*
* @version $Id: U.java,v 1.28 2007/08/06 21:03:09 divita Exp $
*/
/* ================================================|NL.java |==== */
/* ---------------------------------|License Endowment|----
   ___LICENSE_ENDOWMENT___
   ---------------------------------|License Endowment|---- */


public class U 
{
  
  // ====================
  // Public Declarations
  // ====================
  
  public static final String NL = System.getProperty("line.separator").toString(); 
  public static final String FS = System.getProperty("file.separator").toString();  
  public static final String PS = System.getProperty("path.separator").toString(); 
  
  public static final String JV = System.getProperty("java.version").toString();
  public static final String HR = "====================================================================";

  private static BufferedReader standardInput = null;
  private static Runtime runtime = null; 
  
  //  ================================================|Public Method Header|====
  /**
   * Method openStandardInput
   * 
   * @return BufferedReader  
   * 
   */
   /* ================================================|Public Method Header|==== */
  public static BufferedReader openStandardInput()
  { 
    
    if ( standardInput == null ) { 
      
      // ---------------------------------------
      // Open the standard input to get messages
      // ---------------------------------------
      try {
	standardInput = new BufferedReader(new InputStreamReader(System.in));
      } catch ( RuntimeException e3 ) {
	System.err.println("Not able to open the standard input for reading");
      }
    }
    
    return ( standardInput );
    
    
  } // ***End static void openStandardInput()
    
    
// ================================================|Date Diff Header |====
/**
 * Given two different milliseconds (long) compkute the hours, minutes, and 
 * seconds and then print that into a string to be returned. 
 *
 *  Example call
 * 
 *	formatDiff(begin, end);
 *
 * @param begin         Time in milliseconds when activity began
 * @param end           Time in milliseconds when activity ended
 * @return String
 *	
 *
*/
// ================================================|Usage Header |====
  public static final String formatDiff(long begin, long end)
  {
    String     returnValue = null;
    int hours, minutes, milliseconds;
    long diff;
    double seconds;
    DecimalFormat df = new DecimalFormat ( "#0.00" );

    diff = end - begin;
    seconds = (diff * 1.0) / 1000.0;
    hours = (int) (seconds / 3600);
    seconds -= (hours * 3600) * 1.0;
    minutes = (int) (seconds / 60);
    seconds -= (minutes * 60) * 1.0;
    returnValue = hours + ":" + minutes + ":" + 
                  df.format(seconds);

    return( returnValue );
  } // ***End formatDifff


  //  ================================================|Public Method Header|====
 /**
   * Method isWindows returns true if this platform is a windows platform
   *
   * @return boolean r
   * 
   */
   /* ================================================|Public Method Header|==== */
  public static final boolean isWindows() { 

    boolean returnCode = false;
    String osName = System.getProperty("os.name");
    
    if ( osName.toLowerCase().indexOf("windows") > -1 ) 
      returnCode = true;
    
    
    return ( returnCode );
  } 

  //  ================================================|Public Method Header|====
 /**
   * Method run 
   *
   * @param pCommand command text
   * @return  String Anything that comes back from that command
   */
  /* ================================================|Public Method Header|==== */
  public static String run(String pCommand ) { 

    Process p = null; 
    StringBuffer buff = new StringBuffer();
    StringBuffer errorBuff = new StringBuffer();

    try {
      if ( runtime == null )
	runtime = Runtime.getRuntime();
      
      if ( runtime != null ) 
        {
          p = runtime.exec( pCommand.trim() );

          BufferedReader outputStream = new BufferedReader(
                                                           new InputStreamReader( p.getInputStream()));
          
          String line = null;
          while ( (line = outputStream.readLine()) != null ) {
            buff.append( line );
            buff.append( U.NL );
          } 
          outputStream.close();
          
          outputStream =
            new BufferedReader( new InputStreamReader( p.getErrorStream()));
          
          line = null;
          while ( (line = outputStream.readLine()) != null ) {
            errorBuff.append( line );
            errorBuff.append( U.NL );
            buff.append( line );
            buff.append( U.NL );
          } 
          outputStream.close();
	  // ------------------------------------------------------
	  // I descided to have the error put on the string instead
	  // rather than spew out an error
	  // ------------------------------------------------------
          // if ( errorBuff.length() > 0 ) {
            // System.err.println("? Error: " + errorBuff );
          // }
        } else {
          System.err.print("runtime is null, aborting...");
          System.exit(0);
        }
    } catch ( IOException e2 ) { 
      System.err.println("Problem with getting output from the command " 
                         + U.NL +
                         pCommand + U.NL +
                         e2.toString());
      System.err.flush();
    } catch ( RuntimeException e ) { 
      System.err.println("Problem running the command |"  +
			 pCommand + "| " + e.getMessage() + "|" + e.toString() );  
      e.printStackTrace(System.err);
      System.err.flush();
  
    }
    p.destroy();
    p = null;

    return (buff.toString());
  }  
  
  // ================================================|Public Method Header|====
  /**
   * Method isNumber 
   *
   * @param pValue input text
   * 
   * @return boolean true if the string contains only numerals and appropriate punctuation
   */
  /* ================================================|Public Method Header|==== */
  public static boolean isNumber(String pValue) { 
    
    boolean returnValue = false;
    
    char[] buff = pValue.toCharArray(); 

    for ( int i = 0; i < buff.length; i++ ) {

      if (( buff[i] >= '0') && ( buff[i] <= '9' ) ) 
	returnValue = true;

      // -------------------------------------
      // This is cluge to easily capture 1,000
      //  but not 10, 
      // -------------------------------------
      else if ( buff[i] == ',' ) {
	if ( i == buff.length - 1 )
	  returnValue = false;
      } else {
	returnValue = false;
	break;
      }
      
    }

    return ( returnValue );
     
  }  

  // ================================================|Public Method Header|====
  /**
   * Method isRealNumber 
   *
   * @param pValue input text
   * 
   * @return boolean true if the string contains only numerals and appropriate punctuation
   */
  /* ================================================|Public Method Header|==== */
  public static boolean isRealNumber(String pValue) { 
    
    boolean returnValue = false;
    boolean periodSeen = false;

    char[] buff = pValue.toCharArray(); 

    for ( int i = 0; i < buff.length; i++ ) {

      if (( buff[i] >= '0') && ( buff[i] <= '9' ) ) 
	returnValue = true;

      // -------------------------------------
      // This is cluge to easily capture 1,000
      //  but not 10, 
      // -------------------------------------
      else if ( buff[i] == ',' ) {
	if ( i == buff.length - 1 )
	  returnValue = false;
	
      } else if ( buff[i] == '.' )  {
	if ( periodSeen == false )
	  periodSeen = true;
	else {
	  returnValue = false;
	}

      } else {
	returnValue = false;
	break;
      }
      
    }
    if ((returnValue == true ) && (periodSeen == false ))
      returnValue = false;

    return ( returnValue );
     
  }  

  // ================================================|Public Method Header|====
  /**
   * Method isPunctuation
   *
   * @param pValue input text
   * 
   * @return boolean true if the string contains only punctuation
   */
  /* ================================================|Public Method Header|==== */
  public static boolean isPunctuation(String pValue) { 
    
    boolean returnValue = false;
    
    char[] buff = pValue.toCharArray(); 

    for ( int i = 0; i < buff.length; i++ ) {

      if ((( buff[i] >= '0') && ( buff[i] <= '9' ) ) ||
	  (( buff[i] >= 'A') && ( buff[i] <= 'z' ) ) ) {
	returnValue = false;
	break;
      } else {
	returnValue = true;
      }
    }

    return ( returnValue );
     
  }  

  // ================================================|Public Method Header|====
  /**
   * Method containsNumber 
   *
   * @param pValue text string
   * 
   * @return boolean true if the string contains numerals
   */
  /* ================================================|Public Method Header|==== */
  public static boolean containsNumber(String pValue) { 
    
    boolean returnValue = false;
    
    char[] buff = pValue.toCharArray(); 

    for ( int i = 0; i < buff.length; i++ ) {

      if (( buff[i] >= '0') && ( buff[i] <= '9' ) ) {
	returnValue = true;
	break;
      }
      
    }

    return ( returnValue );
     
  }  


// ================================================|Public Method Header|====
/**
 * Method getParentDirectoryName returns the name of the parent directory.
 * Not the path, just the name of that directory. 
 * 
 * @return String
 * 
*/
// ================================================|Public Method Header|====
public static String getParentDirectoryName()
  { 
    String     returnValue = null;

    try {
      File currentDir = new File(System.getProperty("user.dir") );

      try {
	File parentDir = currentDir.getParentFile();
    
	try {
	  returnValue = parentDir.getName();
	} catch (Exception e3 ) { e3.printStackTrace(); }
      } catch (Exception e2 ) { e2.printStackTrace(); }
    } catch (Exception e1 ) { e1.printStackTrace(); }


    return( returnValue );

  }
// ================================================|Public Method Header|====
/**
 * Method square squares the input number
 * 
 * @param pVal input number
 * @return double: square of input number
 * 
*/
// ================================================|Public Method Header|====
  public static double square( double pVal )
  {
    return ( pVal * pVal );
  }  
  

// ================================================|Public Method Header|====
/**
 * copyFile copies the contents of file A to file B 
 * 
 * @param pSourceFile source file
 * @param pTargetFile target file
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static void copyFile( String pSourceFile, String pTargetFile ) throws Exception
  {

    try {
      File inputFile = new File(pSourceFile );
      File outputFile = new File(pTargetFile );
      
      FileReader in = new FileReader(inputFile);
      FileWriter out = new FileWriter(outputFile);
      int c;
      
      while ((c = in.read()) != -1)
	out.write(c);
      
      in.close();
      out.close();
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new Exception ( "Not able to copy the file" + e.toString());
    }
  }  
 
// ================================================|Public Method Header|====
/**
 * copyDir  Copies all files under srcDir to dstDir.
 * If dstDir does not exist, it will be created.
 * 
 * @param srcDir source directory
 * @param dstDir destination directory
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
  public static void copyDir(File srcDir, File dstDir) throws Exception {
    if (srcDir.isDirectory()) {
      if (!dstDir.exists()) {
	dstDir.mkdir();
      }
      
      String[] children = srcDir.list();
      for (int i=0; i<children.length; i++) {
	copyDir(new File(srcDir, children[i]), new File(dstDir, children[i]));
      }
    } else {
      // This method is implemented in e1071 Copying a File
      copyFile(srcDir.getAbsolutePath(), dstDir.getAbsolutePath());
    }
  } // *** end copyDir 

// ================================================|Public Method Header|====
/**
 * copyDir  Copies all files under srcDir to dstDir.
 * If dstDir does not exist, it will be created.
 * 
 * @param pSrcDir source directory
 * @param pDstDir destination directory
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
  public static void copyDir(String pSrcDir, String pDstDir) throws Exception {

    File srcDir = new File(pSrcDir);
    File dstDir = new File(pDstDir); 
    copyDir ( srcDir, dstDir );
    srcDir = null;
    dstDir = null;

  } // *** end copyDir 
// ================================================|Public Method Header|====
/**
 * renameFile changes the name of the file t
 * 
 * @param pSourceFile source file
 * @param pTargetFile target file
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static void renameFile( String pSourceFile, String pTargetFile ) throws Exception
  {

    try {
      File sourceFile = new File( pSourceFile); 
      File targetFile = new File( pTargetFile); 
      sourceFile.renameTo( targetFile );
    } catch ( Exception e) {
      e.printStackTrace();
      throw new Exception ( "Not able to rename the file" + e.toString());
    }
  }  
  
// ================================================|Public Method Header|====
/**
 * concat concatinates two strings together efficiently 
 * 
 * @param pStringA first string 
 * @param pStringB second string
 * @return concatenation of first and second strings 
 * @exception Exception general exception
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB ) throws Exception
  {

    String[] strings = new String[2];
    strings[0] = pStringA;
    strings[1] = pStringB;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  
// ================================================|Public Method Header|====
/**
 * concat concatinates three strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC ) throws Exception
  {

    String[] strings = new String[3];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates four strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD ) throws Exception
  {

    String[] strings = new String[4];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates five strings together efficiently 
 * 
 * @param pStringA first string
 * @param pStringB second string
 * @param pStringC third string
 * @param pStringD fourth string
 * @param pStringE five string
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE ) throws Exception
  {

    String[] strings = new String[5];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates six strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF ) throws Exception
  {

    String[] strings = new String[6];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates seven strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG ) throws Exception
  {

    String[] strings = new String[7];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates eight strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH ) throws Exception
  {

    String[] strings = new String[8];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  


// ================================================|Public Method Header|====
/**
 * concat concatinates nine strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI ) throws Exception
  {

    String[] strings = new String[9];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates ten strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ ) throws Exception
  {

    String[] strings = new String[10];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates 11 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK ) throws Exception
  {

    String[] strings = new String[11];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates 12 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @param pStringL
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK,
			     String pStringL ) throws Exception
  {

    String[] strings = new String[12];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;
    strings[11] = pStringL;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates 13 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @param pStringL
 * @param pStringM
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK,
			     String pStringL,
			     String pStringM ) throws Exception
  {

    String[] strings = new String[13];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;
    strings[11] = pStringL;
    strings[12] = pStringM;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates 14 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @param pStringL
 * @param pStringM
 * @param pStringN
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK,
			     String pStringL,
			     String pStringM,
			     String pStringN ) throws Exception
  {

    String[] strings = new String[14];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;
    strings[11] = pStringL;
    strings[12] = pStringM;
    strings[13] = pStringN;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  




// ================================================|Public Method Header|====
/**
 * concat concatinates 15 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @param pStringL
 * @param pStringM
 * @param pStringN
 * @param pStringO
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK,
			     String pStringL,
			     String pStringM,
			     String pStringN,
			     String pStringO ) throws Exception
  {

    String[] strings = new String[15];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;
    strings[11] = pStringL;
    strings[12] = pStringM;
    strings[13] = pStringN;
    strings[14] = pStringO;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  

// ================================================|Public Method Header|====
/**
 * concat concatinates 16 strings together efficiently 
 * 
 * @param pStringA
 * @param pStringB
 * @param pStringC
 * @param pStringD
 * @param pStringE
 * @param pStringF
 * @param pStringG
 * @param pStringH
 * @param pStringI
 * @param pStringJ
 * @param pStringK
 * @param pStringL
 * @param pStringM
 * @param pStringN
 * @param pStringO
 * @param pStringP
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String pStringA, 
			     String pStringB,
			     String pStringC,
			     String pStringD,
			     String pStringE,
			     String pStringF,
			     String pStringG,
			     String pStringH,
			     String pStringI,
			     String pStringJ,
			     String pStringK,
			     String pStringL,
			     String pStringM,
			     String pStringN,
			     String pStringO,
			     String pStringP ) throws Exception
  {

    String[] strings = new String[16];
    strings[0] = pStringA;
    strings[1] = pStringB;
    strings[2] = pStringC;
    strings[3] = pStringD;
    strings[4] = pStringE;
    strings[5] = pStringF;
    strings[6] = pStringG;
    strings[7] = pStringH;
    strings[8] = pStringI;
    strings[9] = pStringJ;
    strings[10] = pStringK;
    strings[11] = pStringL;
    strings[12] = pStringM;
    strings[13] = pStringN;
    strings[14] = pStringO;
    strings[16] = pStringP;

    String returnValue = concat( strings );

    strings = null;

    return( returnValue );
  }  


// ================================================|Public Method Header|====
/**
 * concat concatinates strings together efficiently 
 * 
 * @param pStrings
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static String concat( String[] pStrings ) throws Exception
  {
    StringBuffer buff = new StringBuffer();
    String returnValue = null;

    for ( int i = 0; i < pStrings.length; i++ ) {
      if ( pStrings[i] != null )
	buff.append( pStrings[i]);
    }
    returnValue = buff.toString();
    buff = null;

    return ( returnValue );
  }

// ================================================|Public Method Header|====
/**
 * pad returns a string with spaces appended to pad it so the string returned
 * is a fixed width of x chars.
 * 
 * @param pString
 * @param pNumChars
 * @return String
 * 
*/
// ================================================|Public Method Header|====
public static String pad( String pString, int pNumChars ) 
  {
    //      4       12 
    //    |Blue____________
    //    |          111111
    //    |0123456789012345
  
    String returnValue = "";

    try {
      StringBuffer buff = new StringBuffer();
      int stringLength = 0;
      
      if ( pString != null ) {
	stringLength = pString.length();
	
	if ( pNumChars > stringLength ) {
	  buff.append( pString );
	  int addSpaces = pNumChars - stringLength ;
	  
	  for ( int i = 0; i < addSpaces; i++ )
	    buff.append(" ");
	  
	} else if (pNumChars ==  stringLength ) {
	  buff.append( pString );
	} else {
	  buff.append( pString.substring(0,pNumChars ) );
	}
      } else {
	for ( int i = 0; i < pNumChars; i++ )
	  buff.append(" ");
      }
      returnValue = buff.toString();

    } catch (Exception e ) {}
    
    return ( returnValue );
  }

// ================================================|Public Method Header|====
/**
 * pad returns a string with spaces appended to pad it so the string returned
 * is a fixed width of x chars.
 * 
 * @param pNum
 * @param pNumChars
 * @return String
 * 
*/
// ================================================|Public Method Header|====
public static String pad( int pNum, int pNumChars ) 
  {
    //      4       12 
    //    |Blue____________
    //    |          111111
    //    |0123456789012345
    
    String returnValue = "";
  
    try {
      StringBuffer buff = new StringBuffer();
      int stringLength = 0;
      
      // ------------------------------
      // Convert the number to a string 
      // ------------------------------
      String pString = String.valueOf( pNum );
      
      
      if ( pString != null ) {
	stringLength = pString.length();
	
	if ( pNumChars > stringLength ) {
	  int addSpaces = pNumChars - stringLength ;
	  
	  for ( int i = 0; i < addSpaces; i++ )
	    buff.append(" ");
	  
	  buff.append( pString );
	  
	} else if (pNumChars ==  stringLength ) {
	  buff.append( pString );
	} else {
	  buff.append( pString.substring(0,pNumChars ) );
	}
      } else {
	for ( int i = 0; i < pNumChars; i++ )
	  buff.append(" ");
      }

      returnValue = buff.toString();
    } catch (Exception e ) {}

    
    return ( returnValue  );
  }
  
// ================================================|Public Method Header|====
/**
 * pad returns a string with spaces appended to pad it so the string returned
 * is a fixed width of x chars.
 * 
 * @param pNum
 * @param pNumChars
 * @return String
 * 
*/
// ================================================|Public Method Header|====
public static String pad( double pNum, int pNumChars ) 
  {
    //      4       12 
    //    |Blue____________
    //    |          111111
    //    |0123456789012345

    String returnValue = "";
  
    try {
      StringBuffer buff = new StringBuffer();
      int stringLength = 0;
      
      // ------------------------------
      // Convert the number to a string 
      // ------------------------------
      String pString = String.valueOf( pNum );
      

      if ( pString != null ) {
	stringLength = pString.length();
	
	if ( pNumChars > stringLength ) {
	  int addSpaces = pNumChars - stringLength ;
	  
	  for ( int i = 0; i < addSpaces; i++ )
	    buff.append(" ");
	  
	  buff.append( pString );
	  
	} else if (pNumChars ==  stringLength ) {
	  buff.append( pString );
	} else {
	  buff.append( pString.substring(0,pNumChars ) );
	}
      } else {
	for ( int i = 0; i < pNumChars; i++ )
	  buff.append(" ");
      }
      returnValue = buff.toString();
    } catch (Exception e ) {}

    return ( returnValue );
  }


// ================================================|Public Method Header|====
/**
 * ListToPipedString returns a piped delimited, trimmed string given a List 
 * of String 
 * 
 * @param pRows 
 * @return String
 * 
*/
// ================================================|Public Method Header|====
  public static String listToPipedString( List<String> pRows ) {

    StringBuffer buff = new StringBuffer();
    for (Iterator<String> i = pRows.iterator(); i.hasNext(); ) {
      buff.append(((String) i.next()).trim());
      if (i.hasNext() )
	buff.append("|");
    }
    return ( buff.toString() );

  }

// ================================================|Public Method Header|====
/**
 * formatPercent just multiplies the number by 100 to make it 
 * a percent. 
 * 
 * @param pNum
 * @return String
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
  public static String formatPercent( double pNum) throws Exception {

    String buff =  String.valueOf(pNum * 100.00 )   ;
    int point = -1;
    String returnValue = null;
    if (((point =  buff.indexOf(".")) > -1 ) && (buff.substring( point).length() > 2 ) )
      returnValue = buff.substring(0,point + 3 );
    else
      returnValue = buff;
    return ( returnValue );
  }
// ================================================|Public Method Header|====
/**
 * getClassLoader retreives the class loader that loaded this software 
 *  
 * @return ClassLoader 
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static ClassLoader getClassLoader( Class aClass ) throws Exception
  {
    
    ClassLoader loader = null; 
    if ( aClass != null )
      loader = aClass.getClassLoader();
    
    if( loader != null ) {
      return loader;
    } else {
      return ClassLoader.getSystemClassLoader();  
    }

  } // *** getClassLoader

// ================================================|Public Method Header|====
/**
 * getFileFromJar retreives the file by looking for it in the jar file. 
 * Ideally, the pFileName should be the full pathname of the file as found
 * in the jar file.
 *
 * This assumes that the current classloader is the System class loader 
 * and the System class loader includes the classpath to the jar file.
 *  
 * @return InputStream   
 * @exception Exception 
 * 
*/
// ================================================|Public Method Header|====
public static InputStream getFileFromJar( String pFileName ) throws Exception
  {
    
    InputStream is = null;
    
    if ( pFileName != null ) 
      is = ClassLoader.getSystemClassLoader().getSystemResourceAsStream( pFileName );

    return ( is ); 

  } // *** getFileFromJar 

// ================================================|Public Method Header|====
/**
 * isCaseSignificant returns true if the term passed in contains 
 * an upper-case character. 
 * 
 * @return param pTerm term to be tested.
 * @return boolean   
 * 
*/
// ================================================|Public Method Header|====
public static boolean isCaseSignificant( String pTerm) 
  {
    
    boolean returnValue = false;
    char[] buff = pTerm.toCharArray();

    for (int i = 0; i < buff.length; i++ ) 
      if ( Character.getType( buff[i]) == Character.UPPERCASE_LETTER ) {
	returnValue = true;
	break;
      }
    

    return ( returnValue );

  } // *** isCaseSignificant  
  
  
} // End of the Class U
