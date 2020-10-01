
//
package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import bioc.BioCDocument;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import java.util.regex.Pattern;

import bioc.BioCDocument;
import bioc.BioCPassage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nlm.nls.clinical.SectionIndicator;

/**
 *
 */

public class SemEvalDocument implements BioCDocumentLoader {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(FreeText.class);
  
  static List<SectionIndicator> indicatorList = new ArrayList<SectionIndicator>();

  /** ignore entire line for these strings: */
  static List<String> ignoreEntireLineStringList = 
    Arrays.asList("Admission Date:",
		  "Discharge Date:",
		  "Date of Birth:",
		  "Service:",
		  "CONDITION ON DISCHARGE:",
		  "D:",
		  "T:",
		  "JOB#:",
		  "Signed electronically by: " );
  static Set<String> ignoreEntireLineStringSet = new HashSet<String>(ignoreEntireLineStringList);


  /** List of strings to omit from consideration when reconizing entities: */
  static List<String> ignoreStringList =
    Arrays.asList(
		  "(End of Report)",
		  "ADMISSION DIAGNOSIS:",
		  "ALLERGIES:",
		  "Admission Date:",
		  "Allergies:",
		  "Attending:",
		  "Brief Hospital Course:",
		  "CLINICAL DETAILS:",
		  "CONDITION AT DISCHARGE:",
		  "CONDITION ON DISCHARGE:",
		  "CV:",
		  "Chest:",
		  "Chief Complaint:",
		  "Completed by:",
		  "D:",
		  "DISCHARGE DIAGNOSES:",
		  "DISCHARGE DIAGNOSIS:",
		  "DISCHARGE DIET:",
		  "DISCHARGE DISPOSITION:",
		  "DISCHARGE INSTRUCTIONS/FOLLOWUP:",
		  "DISCHARGE MEDICATIONS:",
		  "DISCHARGE STATUS:",
		  "Date of Birth:",
		  "Dictated By:",
		  "Discharge Condition:",
		  "Discharge Date:",
		  "Discharge Diagnosis:",
		  "Discharge Disposition:",
		  "Discharge Instructions:",
		  "Discharge Medications:",
		  "FINDINGS:",
		  "FOLLOW-UP INSTRUCTIONS:",
		  "Family History:",
		  "Findings:",
		  "Followup Instructions:",
		  "GI/Abd:",
		  "Gen:",
		  "HISTORY OF PRESENT ILLNESS:",
		  "HISTORY OF THE PRESENT ILLNESS:",
		  "HOME MEDICATIONS",
		  "HOSPITAL COURSE:",
		  "Head:",
		  "Height: (in)",
		  "History of Present Illness:",
		  "Indication:",
		  "JOB#:",
		  "MEDICATIONS ON ADMISSION:",
		  "MEDICATIONS ON DISCHARGE:",
		  "MEDICATIONS:",
		  "Major Surgical or Invasive Procedure:",
		  "Medications on Admission:",
		  "PAST MEDICAL HISTORY:",
		  "PERTINENT LABORATORY VALUES ON DISCHARGE:",
		  "PERTINENT LABORATORY VALUES ON PRESENTATION:",
		  "PHYSICAL EXAMINATION ON DISCHARGE:",
		  "PHYSICAL EXAMINATION ON PRESENTATION:",
		  "PHYSICAL EXAMINATION:",
		  "PROCEDURE:",
		  "Past Medical History",
		  "Past Medical History:",
		  "Pertinent Results:",
		  "Physical Exam:",
		  "Physical Examination:",
		  "Please follow-up with Dr.",
		  "RADIOLOGY/IMAGING:",
		  "REASON FOR THIS EXAMINATION:",
		  "Reason:",
		  "Service:",
		  "Signed electronically by: ",
		  "Signed electronically by:",
		  "Skin:",
		  "Social History:",
		  "T:",
		  "UNDERLYING MEDICAL CONDITION:",
		  "Weight (lb):",
		  "You should contact your MD if you experience:"
		  );

  /** Set of strings to ignore */
  static Set<String> ignoreStringSet = new HashSet<String>(ignoreStringList);

  /* "16139	||||	98	||||	15836	||||	DISCHARGE_SUMMARY	||||	2015-03-24 00:00:00.0	||||		||||		||||		||||	" */

  /** header pattern */
  static Pattern headerPattern = Pattern.compile("\\d+\\t[\\|]+\\t\\d+\\t[\\|]+\\t\\d+\\t[\\|]+\\t[\\w\\_\\-]+\\t[\\|]+\\t\\d+\\-\\d+\\-\\d+ \\d+:\\d+:\\d+\\.\\d+\\t[\\|]+\\t\\t[\\|]+\\t\\t[\\|]+\\t");

  /** de-identified text pattern */
  static Pattern deIdentifierPattern = Pattern.compile("\\[\\*\\*.\\*\\*\\]");

  public static String fill(String targetString) {
    char[] fillArray = new char[targetString.length()];
    Arrays.fill(fillArray, 'X');
    return new String(fillArray);
  }

  public static List<String> segmentPassages(String text) {
    List<String> passageList = new ArrayList<String>();
    StringBuilder passageSb = new StringBuilder();
    for (String line: text.split("\n")) {
      if (line.trim().length() > 0) {
	passageSb.append(line).append("\n");
      } else {
	passageList.add(passageSb.toString());
	passageSb.setLength(0);
      }
    }
    if (passageSb.toString().length() > 0) {
      passageList.add(passageSb.toString());
    }
    return passageList;
  }

  public static String removeNonParsableStrings(String aPassage) {
    Console console = System.console();
    // Matcher rmatcher = Pattern.compile(deIdentifierPattern).matcher(passage);
    // rmatcher.replaceAll();

    String passage = aPassage;
    for (String ignoreString: ignoreStringList) {
      String ignorePattern = "^" + ignoreString;
      String newPassage = passage.replaceAll(ignorePattern, fill(ignoreString));
      passage = newPassage;
    }
    Matcher headerMatcher = headerPattern.matcher(aPassage);
    System.out.println("headerMatcher: " + headerMatcher);
    while (headerMatcher.find()) {
      logger.info("I found the text" +
		  " \"%s\" starting at " +
		  "index %d and ending at index %d.%n",
		  headerMatcher.group(),
		  headerMatcher.start(),
		  headerMatcher.end());
    }

    return passage;
  }
  
  public static boolean isParsable(String passage) {
    // What's parsable?
    // Prerequites: Remove any known non-parsable patterns
    if (passage.trim().length() == 0) { return false; }
    return true;
  }

  public static String sanitizeDocument(String inputDocumentText) {
    // 1. divide document into passages
    // 2. determine if each passage is parsable.
    //

    return removeNonParsableStrings(inputDocumentText);
  }
  
  public static String read(Reader inputReader)
    throws IOException {
    BufferedReader br;
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    StringBuilder sb = new StringBuilder();
    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  public static String loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    File inputFile  = new File(inputFilename);
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), charset));
    long fileLen = inputFile.length();
    char[] buf = new char[(int)fileLen];
    br.read(buf,0, (int)fileLen);
    br.close();
    String text = new String(buf);
    return text;
  }

  public static BioCDocument instantiateBioCDocument(String docText) {
    BioCDocument document = new BioCDocument();
    logger.debug(docText);
    BioCPassage passage = new BioCPassage();
    passage.setOffset(0);
    passage.setText(docText);
    passage.putInfon("docid", "00000000.tx");
    passage.putInfon("freetext", "freetext");
    document.addPassage(passage);
    document.setID("00000000.tx");
    return document;
  }

  @Override
  public BioCDocument loadFileAsBioCDocument(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    BioCDocument document = instantiateBioCDocument(inputtext);
    return document;
  }

  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename) 
    throws FileNotFoundException, IOException
  {
    List<BioCDocument> docList = new ArrayList<BioCDocument>();
    docList.add(loadFileAsBioCDocument(filename));
    return docList;
  }

  public BioCDocument readAsBioCDocument(Reader inputReader) 
    throws IOException
  {
    String inputtext = FreeText.read(inputReader);
    BioCDocument document = instantiateBioCDocument(inputtext);
    return document;
  }

  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    List<BioCDocument> docList = new ArrayList<BioCDocument>();
    docList.add(readAsBioCDocument(reader));
    return docList;
  }

  /**
   *
   * @param args - Arguments passed from the command line
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public static void main(String[] args) 
    throws FileNotFoundException, IOException

  {
    if (args.length > 0) {
      for (String passage: segmentPassages(loadFile(args[0]))) {
	System.out.println("          passage: " + passage);
	System.out.println("sanitized passage: " + removeNonParsableStrings(passage));
      }
    }
  }
}
