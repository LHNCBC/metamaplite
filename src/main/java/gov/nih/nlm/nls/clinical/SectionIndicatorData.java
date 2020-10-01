
//
package gov.nih.nlm.nls.clinical;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import java.io.Console;
import au.com.bytecode.opencsv.CSVReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */

public class SectionIndicatorData {
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(SectionIndicator.class);

  /** header pattern */
  static Pattern headerPattern = Pattern.compile("\\d+\\t[\\|]+\\t\\d+\\t[\\|]+\\t\\d+\\t[\\|]+\\t[\\w\\_\\-]+\\t[\\|]+\\t\\d+\\-\\d+\\-\\d+ \\d+:\\d+:\\d+\\.\\d+\\t[\\|]+\\t\\t[\\|]+\\t\\t[\\|]+\\t");

  List<SectionIndicator> indicatorList = new ArrayList<SectionIndicator>();
  Map<String,List<SectionIndicator>> docTypeIndicatorMap = new HashMap<String,List<SectionIndicator>>();
  public List<SectionIndicator> getIndicatorList() { return this.indicatorList; }
  public Map<String,List<SectionIndicator>> getIndicatorMap() { return this.docTypeIndicatorMap; }
  IndicatorNote indicatorRegExpFormat = new IndicatorNote();


  public void loadRecords(String filename)
    throws FileNotFoundException, IOException
  {
    // BufferedReader br = new BufferedReader(new FileReader(filename));
    // String line;
    // while ((line = br.readLine()) != null) {
    //   newList.add(new SectionIndicator(line));
      
    // }
    // br.close();

    CSVReader reader = new CSVReader(new FileReader(filename));
    String [] nextLine;
    while ((nextLine = reader.readNext()) != null) {
      // nextLine[] is an array of values from the line
      //System.out.println(nextLine[0] + nextLine[1] + "etc...");
      SectionIndicator indicator = new SectionIndicator(nextLine);
      this.indicatorList.add(indicator);
      if (this.docTypeIndicatorMap.containsKey(indicator.getDocType())) {
	List<SectionIndicator> docTypeIndicatorList = this.docTypeIndicatorMap.get(indicator.getDocType());
	docTypeIndicatorList.add(indicator);
      } else {
	List<SectionIndicator> docTypeIndicatorList = new ArrayList<SectionIndicator>();
	docTypeIndicatorList.add(indicator);
	this.docTypeIndicatorMap.put(indicator.getDocType(), docTypeIndicatorList);
      }
    }
  }

  public static String loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    File inputFile  = new File(inputFilename);
    BufferedReader br = new BufferedReader(new FileReader(inputFile));
    long fileLen = inputFile.length();
    char[] buf = new char[(int)fileLen];
    br.read(buf,0, (int)fileLen);
    br.close();
    String text = new String(buf);
    return text;
  }

  public static String fill(String targetString) {
    char[] fillArray = new char[targetString.length()];
    Arrays.fill(fillArray, 'X');
    return new String(fillArray);
  }

  static class Extent {
    int start;
    int end;
    Extent(int start, int end) { this.start = start; this.end = end; }
    int getStart() { return this.start; }
    int getEnd() { return this.end; }
  }


  public String generateExpression(String indicatorString, 
				   String note) {
    String expFormat = this.indicatorRegExpFormat.getFormatForNote(note);
    String expression = String.format(expFormat, indicatorString);
    return expression;
  }

  public String removeNonParsableStrings(String docType, String aPassage) {
    Console console = System.console();
    // Matcher rmatcher = Pattern.compile(deIdentifierPattern).matcher(passage);
    // rmatcher.replaceAll();
    console.printf("removeNonParsableStrings:\n");
    List<Extent> matchList = new ArrayList<Extent>();
    String passage = aPassage;
    for (SectionIndicator indicator: this.docTypeIndicatorMap.get(docType)) {
      console.printf("Indicator: %40s, Notes: %s\n", indicator.getIndicatorString(), indicator.getNotes());
      String ignoreString = indicator.getIndicatorString();
      String ignoreExpression = ignoreString;
      Pattern ignorePattern = Pattern.compile(ignoreExpression);
      Matcher ignoreStringMatcher = ignorePattern.matcher(aPassage.toLowerCase());
      while (ignoreStringMatcher.find()) {
	console.printf("I found the text" +
		       " \"%s\" starting at " +
		       "index %d and ending at index %d.%n",
		       ignoreStringMatcher.group(),
		       ignoreStringMatcher.start(),
		       ignoreStringMatcher.end());
	console.printf("found text: \"%s\"%n",
		       aPassage.substring(ignoreStringMatcher.start(), ignoreStringMatcher.end()));
      }
    }
    Matcher headerMatcher = headerPattern.matcher(aPassage);
    // System.out.println("headerMatcher: " + headerMatcher);
    while (headerMatcher.find()) {
      console.printf("I found the text" +
		     " \"%s\" starting at " +
		     "index %d and ending at index %d.%n",
		     headerMatcher.group(),
		     headerMatcher.start(),
		     headerMatcher.end());
    }
    return passage;
  }

  /**
   *
   * @param args - Arguments passed from the command line
   * @throws FileNotFoundException File Not Found Exception
   * @throws IOException IO Exception
   **/
  public static void main(String[] args) 
    throws FileNotFoundException, IOException
  {
    if (args.length > 0) {
      SectionIndicatorData indicatorData = new SectionIndicatorData();
      indicatorData.loadRecords(args[0]);
      List<SectionIndicator> sectionRecordList = indicatorData.getIndicatorList();
      System.out.println("size of section record list: " + sectionRecordList.size());
      for (SectionIndicator record: sectionRecordList) {
	System.out.print("DOC_TYPE: " + record.getDocType());
	System.out.print("\tNEW_TAG: " + record.getNewTag());
	System.out.print("\t\tINDICATOR_STRING: " + record.getIndicatorString());
	System.out.println("\t\tNOTES: " + record.getNotes());
      }
      if (args.length > 1) {
	String docType = args[1];
	String text = loadFile(args[2]);
	System.out.println("Original File:\n_______________________________________________\n" +
			   text +"\n_______________________________________________\n");
	String filteredText = indicatorData.removeNonParsableStrings(docType, text);



	System.out.println("Processed File:\n______________________________________________\n" + 
			   filteredText +"\n_______________________________________________\n");
      }
    } else {
      System.out.println("Usage: SectionIndicator indicator doctype file [inputfile]");
    }
  }
  
}
