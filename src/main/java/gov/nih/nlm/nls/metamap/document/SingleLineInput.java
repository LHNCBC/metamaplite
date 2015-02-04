
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 *
 */

public class SingleLineInput {
  public static List<String> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<String> lineList = new ArrayList<String>();
    String line;
    while ((line = br.readLine()) != null) {
      lineList.add(line);
    }
    br.close();
    return lineList;
  }


  public static List<BioCDocument> bioCLoadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFilename));
    List<BioCDocument> docList = new ArrayList<BioCDocument>();
    int i = 0;
    String line;
    while ((line = br.readLine()) != null) {
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      BioCDocument doc = new BioCDocument();
      doc.setID(".tx");
      BioCPassage passage = new BioCPassage();
      formatter.format("%08d.TX", i);
      passage.putInfon("docid", formatter.toString());
      passage.setText(line);
      passage.setOffset(0);
      doc.addPassage(passage);
      doc.putInfon("docid", formatter.toString());
      docList.add(doc);
      i++;
    }
    br.close();
    return docList;
  }
}
