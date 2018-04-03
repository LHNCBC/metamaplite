package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import gov.nih.nlm.nls.nlp.nlsstrings.MWIUtilities;

/**
 * Special terms exclusion list consisting of cui + term 
 *
 * Current implementation is a Set of CUI + "|" + string.
 */
public class SpecialTerms {

  /** instance special terms set */
  Set<String> specialTerms = new HashSet<String>();

  public SpecialTerms() {
  }

  public SpecialTerms(String filename) 
    throws FileNotFoundException, IOException
  {
    File inputFile = new File(filename);
    if (inputFile.exists()) {
      this.specialTerms = loadTerms(new FileReader(inputFile));
    } else {
      System.err.println("warning: special terms file: " + filename + " does not exist.");
    }
  }

  public SpecialTerms(InputStream stream) 
    throws IOException
  {
    this.specialTerms = loadTerms(stream);
  }

  public SpecialTerms(Set<String> specialTermSet) 
  {
    this.specialTerms = specialTermSet;
  }

  public void addTerms(String filename) 
    throws FileNotFoundException, IOException
  {
    File inputFile = new File(filename);
    if (inputFile.exists()) {
      this.specialTerms.addAll( loadTerms(new FileReader(inputFile)) );
    } else {
      System.err.println("warning: special terms file: " + filename + " does not exist.");
    }
  }

    public void addTerms(InputStream stream) 
    throws IOException
  {
    this.specialTerms.addAll( loadTerms(stream) );
  }

  public Set<String> loadTerms(Reader reader)
    throws FileNotFoundException, IOException
  {
    Set<String> termSet = new HashSet<String>();
    String line;
    BufferedReader br = new BufferedReader(reader);
     while ((line = br.readLine()) != null) {
      termSet.add(line.trim());
    }
    br.close();
    return termSet;
  }  

  /**
   * Input file format:
   * <pre>
   * cui|term
   * </pre>
   * @param filename input filename
   * @return set of terms
   * @throws FileNotFoundException file not found exception
   * @throws IOException i/o exception
   */
  public Set<String> loadTerms(String filename)
    throws FileNotFoundException, IOException
  {
    Set<String> termSet = new HashSet<String>();
    String line;
    BufferedReader br = new BufferedReader(new FileReader(filename));
     while ((line = br.readLine()) != null) {
      termSet.add(line.trim());
    }
    br.close();
    return termSet;
  }  

  public Set<String> loadTerms(InputStream stream)
    throws FileNotFoundException, IOException
  {
    Set<String> termSet = new HashSet<String>();
    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
     while ((line = br.readLine()) != null) {
      termSet.add(line.trim());
    }
    br.close();
    return termSet;
  }  

  public String makeKey(String cui, String term) {
    return cui + ":" + term;
  }

  public boolean isSpecial(String cui, String term) {
    return this.specialTerms.contains(makeKey(cui,term));
  }

  public boolean isExcluded(String cui, String term) {
    return this.specialTerms.contains(makeKey(cui,term)) ||
      this.specialTerms.contains(makeKey("*", term));
  }
  public int size() {
    return this.specialTerms.size();
  }
}


