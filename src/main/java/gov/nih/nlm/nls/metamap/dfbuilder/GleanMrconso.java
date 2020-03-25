package gov.nih.nlm.nls.metamap.dfbuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import gov.nih.nlm.nls.nlp.nlsstrings.MetamapTokenization;

/**
 * GleanMrconso -
 * Implement the words file generation functionality of
 * the original Prolog version of glean_mrconso using MRCONSO.RRF
 * present in Prolog and Java versions of glean_mrconso.
 *
 * The original program, glean_mrconso.pl, also gleaned strings, and
 * concepts.  This extra functionality has been ommited (for now).
 *
 * Created: Tue Mar 10 09:57:37 2020
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class GleanMrconso {

  /**
   * Creates a new <code>GleanMrconso</code> instance.
   */
  public GleanMrconso() {
  }

  public static class WordInfo {
    String cui;
    String sui;
    List<String> words;
    public WordInfo(String cui, String sui, String str) {
      this.cui = cui;
      this.sui = sui;
      this.words = MetamapTokenization.tokenizeTextMM(str);
      
    }
    public String getCui() { return this.cui; }
    public String getSui() { return this.sui; }
    public List<String> getWords() { return this.words; }
  }

  /**
   * Convert mrconso record to word-string information: a map
   * with :cui, :sui, and :words derived from :str field MRCONSO record.
   * @param line line mrconso 
   */
  public WordInfo mrconsoRecordToWordInfo(String line) {
    String[] fields = line.split("\\|");
    return new WordInfo(fields[0], fields[5], fields[14]);
  }

  public void writeWords(PrintWriter pw, List<String> wordlist, String sui, String cui) {
    int i  = 0;
    for (String word: wordlist) {
      pw.write(i + "|" + wordlist.size() + "|" + word + "|" + sui + "|" + cui + "\n");
      i++;
    }
  }

  /**
   * Read MRCONSO files and write generated Term/Treecode file.
   * @param mrconsoFilename UMLS MRCONSO table filename
   * @param wordsFilename name of words output file
   * lines of form:
   *   term|term-categories|term-variant|variant-categories|varlevel|history|
   */
  public void process(String mrconsoFilename, String wordsFilename) {
    Charset charset = Charset.forName("utf-8");
    try {
      BufferedReader br =
	new BufferedReader(new InputStreamReader
			   (new FileInputStream(mrconsoFilename),
			    charset));
      PrintWriter pw =
	new PrintWriter(new OutputStreamWriter
			(new FileOutputStream(wordsFilename),
			 charset));
      String line;
      while ((line = br.readLine()) != null) {
	WordInfo wordInfo = mrconsoRecordToWordInfo(line);
	writeWords(pw, wordInfo.getWords(), wordInfo.getSui(), wordInfo.getCui());
      }
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }    
  }

  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
   */
  public static final void main(final String[] args) {
    if (args.length > 1) {
      String mrconsofn = args[0];
      String wordsfn = args[1];
      GleanMrconso inst = new GleanMrconso();
      inst.process(mrconsofn, wordsfn);
    } else {
      System.out.println("gov.nih.nlm.nls.metamap.dfbuilder.GleanMrconso mrconsofile wordsfile");
    }
  }
}
