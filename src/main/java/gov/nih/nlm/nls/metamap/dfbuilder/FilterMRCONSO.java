package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.*;
import java.util.*;
import java.text.*;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.impl.Arguments;

import gov.nih.nlm.nls.utils.StringUtils;
import gov.nih.nlm.nls.metamap.lite.ChunkerMethod;
import gov.nih.nlm.nls.metamap.lite.OpenNLPChunker;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.Phrase;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;
import gov.nih.nlm.nls.types.Sentence;

/**
 * This utility creates less redundant, versions of (English) mrcon by
 * filtering mrconso (essentially mrcon with source information).
 * <p>
 *  Basic (lexical) filtering consists of removing strings for a concept
 *  which are effectively the same as another string for the concept.
 *  The normalization process consists of the following steps:
 * <ol>
 *    <li> removal of (left []) parentheticals; </li>
 *    <li> removal of multiple meaning designators (<n>);</li>
 *    <li> NOS normalization;</li>
 *    <li> syntactic uninversion;</li>
 *    <li> conversion to lowercase;</li>
 *    <li> replacement of hyphens with spaces; and</li>
 *    <li> stripping of possessives.</li>
 * </ol>
 *  Some right parentheticals used to be stripped, but no longer are.
 *  Lexical Filtering Examples:
 *  The concept "Abdomen" has strings "ABDOMEN" and "Abdomen, NOS".
 *  Similarly, the concept "Lung Cancer" has string "Cancer, Lung".
 *  And the concept "1,4-alpha-Glucan Branching Enzyme" has a string
 *  "1,4 alpha Glucan Branching Enzyme".
 * </p>
 * <p>
 *  Moderate filtering additionally involves filtering out terms by
 *  type. The following cause filtering:
 * <ol>
 *   <li> TS (Term Status) of "s" (suppressible synonym)
 *                       or "p" (suppressible preferred name)</li>
 *   <li> TTY (Term Type) of "AA", "AB", "CO", "CS", "DFA", "HX", "LN",
 *      "LO", "LX", "OA", "PS", "UCN", "UPC", "USN", "USY", "VAB" and
 *      "XX".</li>
 * </ol>
 * </p>
 * <p>
 *  Finally, strict filtering parses the strings and filters out
 *  any with more than one MSU (minimal syntactic unit), i.e.,
 *  more than one phrase. (Alternative criteria may be considered.)
 * </p>
 * <p>
 * <pre>
 * Usage: FilterMRCONSO [&lt;options&gt;] &lt;infile&gt; &lt;outfile&gt; &lt;restfile&gt;
 *
 * &lt;infile&gt; should normally be mrconso.eng.0 or the like,
 * &lt;outfile&gt; is &lt;infile&gt; filtered,
 * &lt;restfile&gt; is the remainder.
 *
 * FilterMRCONSO options:
 *           -h --help              
 *           -i --info                 (not implemented)
 *           -s --strict_filtering
 *           -m --moderate_filtering
 *           -N --silent
 *           -p --progress_bar_interval &lt;integer&gt; (not implemented)
 *           -t --total_lines &lt;integer&gt;
 *           -w --warnings
 *           -x --dump_syntax_only     (not implemented)
 * </pre>
 * </p>
 * <p>
 * Translated from Alan Aronson's original prolog version: filter_mrconso.pl.
 * </p>
 * <p>
 * Created: Fri Oct  5 09:23:48 2001
 * </p>
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: FilterMRCONSO.java,v 1.15 2006/08/23 17:51:16 wrogers Exp $
 */
public class FilterMRCONSO {
  /** program usage message */
  private static final String usageMsg = 
    "Usage: FilterMRCONSO [<options>] <infile> <outfile> <restfile>\n" +
    "\n" +
    "  <infile> should normally be mrconso.eng.0 or the like,\n" +
    "  <outfile> is <infile> filtered,\n" +
    "  <restfile> is the remainder.\n" +
    "\n" +
    "  FilterMRCONSO options:\n" +
    "            -s --strict_filtering\n" +
    "            -m --moderate_filtering\n" +
    "            -x --dump_syntax_only\n" +
    "            -h --help\n" +
    "            -i --info\n" +
    "            -w --warnings\n" +
    "\n";

  /** use 8bit Unicode for character set */
  static String charset="UTF-8";
  /** message log */
  static PrintWriter log = new PrintWriter(new OutputStreamWriter(System.out));
  
  // Instance variables

  /** excluded term type map */
  Set<String> excludedTermType = new HashSet<String>(25);
  /** true if applying strict filtering (default is relaxed) */
  boolean strictFiltering = false;
  /** true if applying moderate filtering */
  boolean moderateFiltering = false;
  /** true if dumping syntax only */
  boolean dumpSyntaxOnly = false;
  /** true if you wish warnings to be displayed */
  boolean displayWarnings = false;
  /** if true display verbose messages: system property "filter.mrconso.verbose" */
  boolean verbose = Boolean.parseBoolean
    (System.getProperty("filter.mrconso.verbose", "false"));
  String currentCUI = null;
  /** group of lines associated with a concept. */
  List<ConceptRecord> conceptGroup = new ArrayList<ConceptRecord>();
  /** type counts */
  Map<String,Counter> typeCounts = new HashMap<String,Counter>();
  /** unfiltered record count */
  int recordCount = 0;
  
  ChunkerMethod chunkerMethod;
  SentenceAnnotator sentenceAnnotator;
  SentenceExtractor sentenceExtractor;

  /** are we threaded? */
  boolean threaded = false;
  /** Knowledge Source year to use for term types, defaults to current release year. */
  String ksYear = null;

  /**
   * @param strictFiltering true if applying strict filtering (default is relaxed)
   * @param moderateFiltering true if applying moderate filtering 
   * @param dumpSyntaxOnly true if dumping syntax only
   * @param displayWarnings true if you wish warnings to be displayed
   */
  public FilterMRCONSO (boolean strictFiltering, boolean moderateFiltering, 
			boolean dumpSyntaxOnly, boolean displayWarnings,
                        String ksYear)
    throws Exception
  {
    boolean ksYearSelected = false;

    this.strictFiltering = strictFiltering;
    this.moderateFiltering = moderateFiltering;
    this.dumpSyntaxOnly = dumpSyntaxOnly;
    this.displayWarnings = displayWarnings;
    if (ksYear != null) {
      this.ksYear = ksYear;
      ksYearSelected = true;
    }
    if (this.strictFiltering || this.dumpSyntaxOnly)
      {
	this.sentenceAnnotator = new OpenNLPPoSTagger();
	this.sentenceExtractor = new OpenNLPSentenceExtractor();
	this.chunkerMethod = new OpenNLPChunker();

        String fttFilename = 
	    "/data/dfbuilder/" + this.ksYear +"/filteredtermtypes.txt";

        File fttFile = new File( fttFilename );
        if (fttFile.exists()) {
          System.out.println("loading term types for filters from file: " + fttFile.getCanonicalPath());
            BufferedReader fttRdr = new BufferedReader(new FileReader(fttFile));
            String line = null;
            while ((line = fttRdr.readLine()) != null) {
                String termType = line.trim();
                this.excludedTermType.add(termType);
            }
            fttRdr.close();
        } else {
            if (ksYearSelected == true) {
              System.out.println("filtered term type file not found, file: " + fttFilename);
              System.out.println("Using default list of term types to filter.");
            }
            /** initialize excluded term type set to default if no term type file */
            this.excludedTermType.add("AA");
            this.excludedTermType.add("AB");
            this.excludedTermType.add("CDD");
            this.excludedTermType.add("CO");
            this.excludedTermType.add("CS");
            this.excludedTermType.add("DFA");
            this.excludedTermType.add("HX");
            this.excludedTermType.add("LPDN");
            this.excludedTermType.add("LN");
            this.excludedTermType.add("LO");
            this.excludedTermType.add("LX");
            this.excludedTermType.add("OA");
            this.excludedTermType.add("PS");
            this.excludedTermType.add("UCN");
            this.excludedTermType.add("USN");
            this.excludedTermType.add("USY");
            this.excludedTermType.add("VAB");
            this.excludedTermType.add("XX");
        }
      }
  }

  /**
   * Generate message about options currently enforce.
   * @return string containing message about which options are set.
   */
  public String getOptionsMessage()
  {
    StringBuffer sb = new StringBuffer();
    if (this.strictFiltering ||
	this.moderateFiltering ||
	this.dumpSyntaxOnly ||
	this.displayWarnings ||
	this.verbose)
      {
	sb.append("Control options:\n").append("  "); 
	if (this.strictFiltering) sb.append("strict_filtering\n");
	if (this.moderateFiltering) sb.append("moderate_filtering\n");
	if (this.dumpSyntaxOnly) sb.append("dump_syntax_only\n");
	if (this.displayWarnings) sb.append("display_warnings\n");
	if (this.verbose) sb.append("verbose\n");
      }
    return sb.toString();
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
   * Set verbose state: true or false
   * @param verbosity set to true for verbose messages
   */
  public void setVerbose(boolean verbosity) {
    this.verbose = verbosity;
  }

  /**
   * @param type string naming which type should be updated.
   */
  void updateTypeCount(String type)
  {
    if (this.typeCounts.containsKey(type))
      {
	Counter counter = (Counter)this.typeCounts.get(type);
	counter.increment();
      }
    else
      {
	this.typeCounts.put(type, new Counter(1));
      }
  }

  void resetConceptGroup()
  {
    // remove all elements
    this.conceptGroup.removeAll(this.conceptGroup);
  }
  /**
   * @param cui concept identifier.
   * @param tokens tokenized version of concept record.
   * @param clsTokens tokenization of cui, lui, and sui.
   * @param nmstr normalized version of STR field in concept record.
   */
  void addConceptRecord(String cui, List<String> tokens, List<String> clsTokens, String nmstr)
  {
    this.conceptGroup.add(new ConceptRecord(cui, tokens, clsTokens, nmstr));
  }

  public List<Sentence> getSentenceList(String nmstr)
  {
    return this.sentenceExtractor.createSentenceList(nmstr);
  }

  /**
   * Generate phrase list from sentence using OpenNLP phrase chunker.
   *
   * @param sentence string containing sentence to be chunked into phrases
   * @return list of phrase instances
   */
  List<Phrase> getPhraseList(String sentence) {
    List<ERToken> sentenceTokenList = Scanner.analyzeText(sentence);
    List<ERToken> minimalSentenceTokenList = new ArrayList<ERToken>();
    for (ERToken token: sentenceTokenList) {
      if (! token.getTokenClass().equals("ws")) { // only keep non-ws tokens
	minimalSentenceTokenList.add(token);
      }
    }
    return this.chunkerMethod.applyChunker(minimalSentenceTokenList);
  }

  /**
   * Is this phrase a prepositional phrase? 
   * @param phrase phrase to be evaluated.
   * @return true if it is a prepositional phrase.
   */
  public boolean isPrepPhrase(Phrase phrase)
  {
    return phrase.getTag() == "PP";
  }

  /**
   * Is this phrase an "of" prepositional phrase, i.e.
   * "of <some term>"?
   * @param phrase phrase to be evaluated.
   * @return true if it is a prepositional phrase starting with "of".
   */
  public boolean isOfPhrase(Phrase phrase)
  {
      return (phrase.getTag() == "PP") && (phrase.getPhrase().get(0).getText().toLowerCase() == "of");
  }

  /**
   * Returns true if list of phrases are all of the form: "of <text>".
   * @param phrases list of phrases
   * @return true if all phrases are "of" phrases.
   */
  public boolean areOfPhrases(List<Phrase> phrases)
  {
    if (verbose) { log.println("areOfPhrase(" + phrases + ")"); }
    if (phrases.size() == 0) 
      {
	return true;
      }
    if (isOfPhrase(phrases.get(0)) &&
	areOfPhrases(phrases.subList(1, phrases.size())) )
      {
	if (this.verbose) log.println("areOfPhrases -> true");
	return true;
      }
    if (this.verbose) log.println("areOfPhrases -> false");
    return false;
  }
  
  /**
   * Corresponds to predicate: <tt>is_syntactically_simple/2(+Phrases,
   * +Nomsu)</tt>
   *
   * is_syntactically_simple/2 succeeds if either Nomsu (the number of minimal
   * syntactic units) is 1 or Phrases is of the form 'a &lt;prep&gt; b' or
   * 'a &lt;prep&gt; b of c of ...'. 
   *
   * @param phrases list of phrases
   * @param noMSU number of minimal syntactic units.
   * @return true if syntactically simple, false if otherwise.
   */  
  public boolean isSyntacticallySimple(List<Phrase> phrases, int noMSU)
  {
    if (verbose) {
      log.println("isSyntacticallySimple( {" + phrases + "}, noMSU=" + noMSU + ")");
    }
    if (noMSU <= 2) {
      if (this.verbose) {
	log.println("isSyntacticallySimple -> true (#msu=1)"); log.flush();
      }
      return true;
    }
    if ( phrases.size() > 2 &&
         areOfPhrases(phrases.subList(1, phrases.size())) )
      { 
	if (this.verbose) log.println("isSyntacticallySimple -> true");
	return true;
      }
    if (this.verbose) log.println("isSyntacticallySimple -> false");
    return false;
  }

  /**
   * Glom Noun Phrase + Prepositional Phrase into a single composite
   * phrase.
   *
   * @param phraseList list of phrases to be modified.
   * @return modified list of phrases
   */
  public List<Phrase> glomNounPhrasePrepPhrase(List<Phrase> phraseList) {
    if (phraseList.size() > 0) {
      List<Phrase> newPhraseList = new ArrayList<Phrase>();
      Phrase first = phraseList.get(0);
      for (Phrase phrase: phraseList.subList(1, phraseList.size())) {
	
      }
    }
    return phraseList;
  }

  /**
   * Extract phrases from input text
   *
   * @param inputtext input text
   * @return list of phrases
   */
    public List<Phrase> getPhrases(String inputtext) {
	List<Phrase> phraseList;
	List<ERToken> sentenceTokenList = Scanner.analyzeText(inputtext);
	List<ERToken> minimalSentenceTokenList = new ArrayList<ERToken>();
	for (ERToken token: sentenceTokenList) {
	    if (! token.getTokenClass().equals("ws")) { // only keep non-ws tokens
		minimalSentenceTokenList.add(token);
	    }
	}
	this.sentenceAnnotator.addPartOfSpeech(minimalSentenceTokenList);
	phraseList = this.chunkerMethod.applyChunker(minimalSentenceTokenList);
	return phraseList;
    }

  /**
   * filter syntactically, used by strict model only.
   *
   * @param conceptGroup list of records for this concept.
   * @param restFile stream to place removed (filtered) records in.
   * @return filtered list of records for this concept.
   */
  List<ConceptRecord> filterSyntactically(List<ConceptRecord> conceptGroup, PrintWriter restFile)
    throws Exception
  {
    List<ConceptRecord> filteredGroup = new ArrayList<ConceptRecord>();
    for (ConceptRecord record: conceptGroup) {
      List<Phrase> phrases = getPhrases(record.getNMSTR());
      if (this.verbose) {
	log.println("phrases: " + phrases);
      }
      int noMSU = phrases.size();
      if (this.verbose) 
	{
	  log.println( record.getNMSTR() + ": # of phrases: " + phrases.size() );
	  log.flush();
	}
      if (this.verbose) displayList(log, "phrase list:", phrases);	
      if (isSyntacticallySimple(phrases, noMSU))
	{
	  filteredGroup.add(record);
	}
      else 
	{
	  restFile.println("synt|" + record);
	}
    }
    return filteredGroup;
  }

  /**
   * Find records with metathesaurus strings lexically equivalent to
   * the supplied record's and return them as a list.
   *
   * @param cRecord one record in a concept
   * @param conceptGroup the group of record representing one concept
   * @return list of records lexically equivalent to cRecord.
   */
  List<ConceptRecord> nmstrDuplicateFound(ConceptRecord cRecord, List<ConceptRecord> conceptGroup)
  {
    String nmstr = cRecord.getNMSTR();
    List<ConceptRecord> foundList = new ArrayList<ConceptRecord>();
    for (ConceptRecord record: conceptGroup) {
      if (record.getNMSTR().equals(nmstr)) {
	foundList.add(record);
      }
    }
    return foundList;
  }

  /**
   * filter records whose string's normalized form duplicates previous
   * string's normalized form.
   * @param conceptGroup of records associated with a concept.
   * @param restFile file that filtered records are written to.
   * @return filtered concept group
   */
  List<ConceptRecord> filterNmStrDups(List<ConceptRecord> conceptGroup, PrintWriter restFile)
  {
    List<ConceptRecord> finalConceptGroup = new ArrayList<ConceptRecord>();
    while (conceptGroup.size() > 0)
      {	
	ConceptRecord record = (ConceptRecord)conceptGroup.get(0);
	conceptGroup.remove(record);
	List<ConceptRecord> dupList = 
	  nmstrDuplicateFound(record, conceptGroup);
	// if current record is not in found list, another record was
	// chosen over it, then add it to the final concept group.
	if (dupList.indexOf(record) == -1) {
	  finalConceptGroup.add(record);
	}
	for (ConceptRecord dupRecord: dupList)
	  {
	    restFile.println("norm|" + dupRecord);
	    finalConceptGroup.remove(dupRecord);
	  }
      }
    return finalConceptGroup;
  }

  /**
   * Filter concept records in group by type.
   *
   * @param conceptGroup list of records for this concept.
   * @param restFile stream to place removed (filtered) records in.
   * @return filtered list of records for this concept.
   */
  List<ConceptRecord> filterByType(List<ConceptRecord> conceptGroup, PrintWriter restFile)
  {
    List<ConceptRecord> filteredGroup = new ArrayList<ConceptRecord>();
    for (ConceptRecord record: conceptGroup) {
      String tty = record.getTTY();
      if (record.getTS().equals("s"))
	{
	  this.updateTypeCount("s");
	  restFile.println(record);
	} 
	else if (this.excludedTermType.contains(tty))
	  {
	    this.updateTypeCount(tty);
	    restFile.println("typePS|" + record);
	  }
	else 
	  {
	    filteredGroup.add(record);
	  }
      }
    return filteredGroup;
  }

  /**
   * get record with preferred status, record is removed from concept group
   *
   * @param conceptGroup list of records for this concept.
   * @return record with preferred status
   */
  ConceptRecord findPreferred(List<ConceptRecord> conceptGroup)
  {
    ConceptRecord preferred = null;
    for (ConceptRecord record: conceptGroup) {
      if (record.getTS().equals("P") && record.getSTT().equals("PF"))
	{
	  preferred = record;
	}
      if (record.getSAB() == "MSH2001") break;
    }
    return preferred;
  }

  /**
   * Display of objects (usually concept records) to print writer out with message prefixed to output.
   *
   * @param out output print writer.
   * @param message message to prefix output.
   * @param objectList list of concept records to be displayed.
   */
  void displayList(PrintWriter out, String message, List objectList)
  {
    out.println(message);
    out.println("list size: " + objectList.size());
    for (Object element: objectList) {
      out.println(" " + element);
    }
    out.println("end of list");
  }

  /**
   * Determine if the preferred record for the concept is 
   * present in the supplied concept group (a list).
   *
   * @param conceptGroup list of records for a particular concept.
   * @return true if a preferred record was not found.
   */
  boolean preferredRecordNotPresent(List<ConceptRecord> conceptGroup)
  {
    Iterator iter = conceptGroup.iterator();
    while (iter.hasNext())
      {
	ConceptRecord record = (ConceptRecord)iter.next();
	if (record.getTS().equals("P") &&
	    record.getSTT().equals("PF")) {
	  if (verbose) { log.println("preferred record found: " + record); }
	  return false;
	}
      }
    return true;
  }

  /**
   * Apply filter rules to records by concept and write results of
   * filtering.
   *
   * @param outFile  output file for retained records.
   * @param restFile output file for discarded records.
   * @param conceptGroup0 list of records for a particular concept.
   */
  void filterAndWrite(PrintWriter outFile,
		      PrintWriter restFile,
		      List<ConceptRecord> conceptGroup0)
    throws Exception
  {
    try {
      if (this.verbose) { 
	displayList(log, "initial group", conceptGroup0);
      }

      // find the preferred name, if any (it may have been suppressed.)
      ConceptRecord preferred = this.findPreferred(conceptGroup0);
      // first do type filtering
      List<ConceptRecord> conceptGroup1 = null;
      if ( this.strictFiltering || this.moderateFiltering || this.dumpSyntaxOnly )
	{
	  conceptGroup1 = this.filterByType(conceptGroup0, restFile);
	  if (this.verbose) {
	    displayList(log, "group after Filtering by Type" , conceptGroup);
	  }
	} 
      else 
	{
	  conceptGroup1 = conceptGroup0;
	}
      // the do lexical filtering
      List<ConceptRecord> conceptGroup2 = this.filterNmStrDups(conceptGroup1, restFile);
      if (this.verbose) {
	displayList(log, "group after Normalized String Filtering" , conceptGroup2);
      }
      List<ConceptRecord> conceptGroup3 = null;
      if ( this.strictFiltering || this.dumpSyntaxOnly )
	{
	  conceptGroup3 = this.filterSyntactically(conceptGroup2, restFile);
	  if (this.verbose) {
	    displayList(log, "group after Syntactic Filtering" , conceptGroup2);
	  }
	}
      else 
	{
	  conceptGroup3 = conceptGroup2;
	}
    
      this.recordCount = this.recordCount + conceptGroup3.size();
      // if relaxed filtering and preferred record has
      // been removed then reinsert it.
      if ((! (this.moderateFiltering || this.strictFiltering)) && 
	  preferred != null &&
	  conceptGroup3.indexOf(preferred) == -1 &&
	  preferredRecordNotPresent(conceptGroup3))
	{
	  conceptGroup3.add(0, preferred);
	}
      if (this.verbose) {
	displayList(log, "final group" , conceptGroup3); 
      }
      // output unfiltered records.
      Iterator iterator = conceptGroup3.iterator();
      while (iterator.hasNext())
	{
	  outFile.println("y|" + iterator.next());
	}
    } catch (RuntimeException exception) {
      exception.printStackTrace(System.err);
      System.err.println("conceptGroup0: " + conceptGroup0);
      if (this.isThreaded() == false) {
	System.exit(-1);
      } else {
	throw exception;
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println("conceptGroup0: " + conceptGroup0);
      if (this.isThreaded() == false) {
	System.exit(-1);
      } else {
	throw exception;
      }
    }
  }

  /**
   * process a line.
   * @param line     mrconso record.
   * @param outFile  output file for filtered mrconso.
   * @param restFile rest file for what was filtered out.
   */
  public void processLine(String line, PrintWriter outFile, PrintWriter restFile)
    throws Exception
  {
    try {
      // Old ORF constructed MRCONSO format:
      // List<String> tokens = StringUtils.split(line, "|");
      // String cls = (String)tokens.get(0);
      // List<String> clsTokens = StringUtils.split(cls, ":");
      // String cui = (String)clsTokens.get(0);

      // Rich Release Format:
      List<String> tokens = StringUtils.split(line, "|");
      List<String> clsTokens = new ArrayList<String>();
      clsTokens.add(tokens.get(0));
      clsTokens.add(tokens.get(3));
      clsTokens.add(tokens.get(5));      
      String cui = (String)tokens.get(0);

      if (this.currentCUI == null) {
	this.currentCUI = cui;
      }
      if (! this.currentCUI.equals(cui))  {
	this.currentCUI = cui;
	this.filterAndWrite(outFile, restFile, this.conceptGroup);
	this.resetConceptGroup();
      }
      if (tokens.size() >= 16) {
	this.addConceptRecord(cui, tokens, clsTokens, 
			      MWIUtilities.normalizeMetaString
			      ((String)tokens.get(14)));
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println("line: " + line);
      throw exception;
    }
  }

  /**
   * Finish processing any remaining records. Flush any buffered
   * output.
   * @param outFile  Output filtered mrconso file
   * @param restFile Rest file of discards
   */
  public void finishProcessing(PrintWriter outFile, PrintWriter restFile)
    throws Exception
  {
    this.filterAndWrite(outFile, restFile, this.conceptGroup);
    this.resetConceptGroup();
    outFile.flush();
    restFile.flush();
  }

  /**
   * Process input mrconso file and output filtered mrconso and rest
   * file of discarded records.
   * @param infile   Input mrconso file 
   * @param outfile  Output filtered mrconso file
   * @param restfile Rest file of discards
   */
  public void processInput(BufferedReader infile, 
			   PrintWriter outfile, PrintWriter restfile)
    throws IOException, Exception
  {
    String line = null;
    while ((line = infile.readLine()) != null)
      {
	this.processLine(line, outfile, restfile);
      }
    this.finishProcessing(outfile, restfile);
  }

  /**
   * main program
   * @param args command line arguments
   */
  public static void main(String[] args)
    throws Exception
  {
    boolean strictFiltering = false;
    boolean moderateFiltering = false;
    boolean dumpSyntaxOnly = false;
    boolean displayWarnings = false;
    boolean verbose = false;

    String inFilename = null;
    String outFilename = null;
    String restFilename = null;
    String ksYear = null;

    System.out.println("\nFilter mrconso (Java Prototype)\n");

    ArgumentParser parser = ArgumentParsers.newFor("FilterMRCONSO").build()
      .defaultHelp(true)
      .description("Filter MRCONSO.");

    parser.addArgument("-x", "--dump_syntax_only")
      .help("dump syntax only.")
      .action(storeTrue());

    parser.addArgument("-E", "--end_of_processing")
      .help("end of processing")
      .action(storeTrue());

    parser.addArgument("-m", "--moderate_filtering")
      .help("moderate filtering")
      .action(storeTrue());

    parser.addArgument("-p", "--progress_bar_interval")
      .setDefault("1000")
      .help("interval for progress bar");

    parser.addArgument("-N", "--silent")
      .help("silence output")
      .action(storeTrue());

    parser.addArgument("-t", "--total_lines")
      .setDefault("1000")
      .help("total lines of input?");

    parser.addArgument("-s", "--strict_filtering")
      .help("strict filtering")
      .action(storeTrue());

    parser.addArgument("-v", "--verbose")
      .help("verbose output")
      .action(storeTrue());

    parser.addArgument("-w", "--warnings")
      .help("display warnings")
      .action(storeTrue());

    parser.addArgument("-Z", "--mm_data_year")
      .help("knowledge source data year");

    parser.addArgument("inputfile")
      .help("input MRCONSO file");

    parser.addArgument("outputfile")
      .help("output MRCONSO file(s)");

    parser.addArgument("restfile")
      .help("rest file(s)");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    strictFiltering = Boolean.parseBoolean(ns.getString("strict_filtering"));
    moderateFiltering = Boolean.parseBoolean(ns.getString("moderate_filtering"));    
    inFilename = ns.getString("inputfile");
    outFilename = ns.getString("outputfile");
    if (ns.getString("restfile") != null) {
      restFilename = ns.getString("restfile");
    } else {
      restFilename = outFilename + ".rest";
    }
    if (ns.getString("mm_data_year") != null) {
      ksYear = ns.getString("mm_data_year");
    }
    FilterMRCONSO filter =
      new FilterMRCONSO(strictFiltering, moderateFiltering, 
			dumpSyntaxOnly, displayWarnings, 
                        ksYear);
    if (verbose) {
      filter.setVerbose(verbose);
    }
    filter.setThreaded(false);
    System.out.println(filter.getOptionsMessage());
    System.out.println("Processing " + inFilename + " --> " +
		       outFilename + " and " + restFilename + ".");
    try {
      BufferedReader infile = new BufferedReader
	(new InputStreamReader(new FileInputStream(inFilename), charset));
            PrintWriter outfile = new PrintWriter
	(new BufferedWriter
	 (new OutputStreamWriter
	  (new FileOutputStream(outFilename), charset)));
      PrintWriter restfile = new PrintWriter
	(new BufferedWriter
	 (new OutputStreamWriter
	  (new FileOutputStream(restFilename), charset)));
      filter.processInput(infile, outfile, restfile);
      outfile.close();
      restfile.close();
      
      if (filter.typeCounts.size() > 0) {
	log.println("Type counts causing filtering:");
	for (Map.Entry entry: filter.typeCounts.entrySet()) {
	  log.println(entry.getKey() + " " + entry.getValue());
	}
      }
      log.println();
      log.println(filter.recordCount + " unfiltered records" );
      log.close();
    } catch (Exception exception) {
      System.err.println("Exception: " + exception.getMessage());
      exception.printStackTrace(System.err);
      System.exit(-1);
    }
    System.exit(0);
  } 

  /**
   * This class represents one record in MRCONSO.
   */
  class ConceptRecord
  {
    private String cui;
    private List<String> tokens;
    private List<String> clsTokens;
    private String normalizedString;
    
    /**
     * @param cui concept identifier.
     * @param tokens tokenized version of concept record.
     * @param clsTokens tokenization of cui, lui, and sui.
     * @param normalizedString normalized version of STR field in concept record.
     */
    public ConceptRecord(String cui, 
			 List<String> tokens, List<String> clsTokens,
			 String normalizedString) {
      this.cui = cui;
      this.tokens = tokens;
      this.clsTokens = clsTokens;
      this.normalizedString = normalizedString;
    }

    /**
     * Get term status of record.
     * @return term status.
     */
    public String getTS()
    {
      return (String)this.tokens.get(2);
    }

    /**
     * Get string type.
     * @return string type.
     */
    public String getSTT()
    {
      return (String)this.tokens.get(4);
    }

    /**
     * Get term string (STR).
     * @return term string.
     */
    public String getSTR()
    {
      return (String)this.tokens.get(14);
    }

    /**
     * Get source abbreviation.
     * @return source abbreviation.
     */
    public String getSAB()
    {
      return (String)this.tokens.get(11);
    }

    /**
     * Get term type for source.
     * @return term type for source.
     */
    public String getTTY()
    {
      return (String)this.tokens.get(12);
    }

    /**
     * Get normalized form of term string (STR).
     * @return return normalized form of term string
     */
    public String getNMSTR()
    {
      return this.normalizedString;
    }

    /**
     * Get MRCONSO record in pipe '|' separated form.
     * @return piped delimited fields of record.
     */
    public String toString()
    {
      return StringUtils.join(tokens, "|");
    }

    /**
     * Get MRCONSO record in pipe '|' separated form.
     * @return piped delimited fields of record.
     */
    public String pipedOutput()
    {
      return StringUtils.join(tokens, "|");
    }

    /**
     * get normalized string, term status, string type, and term string.
     * @return normalized string + TS + STT + STR
     */
    public String info()
    {
      return "{ NMSTR = " + this.normalizedString +
	", rec = " + this.tokens.get(2) + 
	"|" + this.tokens.get(4) + 
	"|" + this.tokens.get(14) + 
	" }";
    }
  }
  

  /**
   * A mutable counter (unlike java.lang.Integer).
   */
  class Counter
  {
    DecimalFormat decimalFormat = new DecimalFormat("       #");
    private int value;
    /**
     * @param initialValue initial value of counter.
     */
    public Counter(int initialValue)
    {
      this.value = initialValue;
    }
    /** increment counter */     
    public void increment()
    {
      this.value++;
    }
    /**
     * Increment counter by supplied delta value.
     * @param delta amount to increment by, 
     */     
    public void increment(int delta)
    {
       this.value =+ delta;
    }
    /**
     * Get current value of counter.
     * @return current value of counter.
     */
    public int getValue()
    {
      return this.value;
    }
    /**
     * return value of counter in decimal string format.
     * @return current value of counter in decimal format.
     */
    public String toString()
    {
      return this.decimalFormat.format(this.value);
    }
  }

}// FilterMRCONSO
