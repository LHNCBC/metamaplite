package gov.nih.nlm.nls.metamap.lite;

import java.util.Set;
import java.util.HashSet;
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

  public SpecialTerms(String filename) 
    throws FileNotFoundException, IOException
  {
    this.specialTerms = loadSpecialTerms(filename);
  }

  public SpecialTerms(InputStream stream) 
    throws FileNotFoundException, IOException
  {
    this.specialTerms = loadSpecialTerms(stream);
  }

  public SpecialTerms(Set<String> specialTermSet) 
  {
    this.specialTerms = specialTermSet;
  }

  /**
   * Input file format:
   * <pre>
   * cui|term
   * </pre>
   * @param filename 
   * @return 
   */
  public Set<String> loadSpecialTerms(String filename)
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

  public Set<String> loadSpecialTerms(InputStream stream)
    throws FileNotFoundException, IOException
  {
    Set<String> specialTerms = new HashSet<String>();
    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
     while ((line = br.readLine()) != null) {
      this.specialTerms.add(line.trim());
    }
    br.close();
    return specialTerms;
  }  

  public String makeKey(String cui, String term) {
    return cui + ":" + term;
  }

  public boolean isSpecial(String cui, String term) {
    return this.specialTerms.contains(makeKey(cui,term));
  }

  public boolean isExcluded(String cui, String term) {
    return this.specialTerms.contains(makeKey(cui,term));
  }
}


