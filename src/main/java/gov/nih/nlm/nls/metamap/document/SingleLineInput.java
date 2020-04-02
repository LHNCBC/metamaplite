
//
package gov.nih.nlm.nls.metamap.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import java.util.List;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 *
 */

public class SingleLineInput implements BioCDocumentLoader {

  Charset charset = Charset.forName("utf-8");

  public static List<String> read(Reader inputReader)
    throws IOException {
    BufferedReader br;
    List<String> lineList = new ArrayList<String>();
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    while ((line = br.readLine()) != null) {
      lineList.add(line);
    }
    return lineList;
  }

  public static List<String> loadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
    List<String> lineList = new ArrayList<String>();
    String line;
    while ((line = br.readLine()) != null) {
      lineList.add(line);
    }
    br.close();
    return lineList;
  }

  public static BioCDocument instantiateBioCDocument(String docText) 
  {
      BioCDocument doc = new BioCDocument();
      doc.setID("00000.txt");
      BioCPassage passage = new BioCPassage();
      passage.setText(docText);
      passage.setOffset(0);
      // passage.putInfon("docid", formatter.toString());
      doc.addPassage(passage);
      return doc;
  }

  public static BioCDocument instantiateBioCDocument(String docText, String inputFilename) 
  {
      BioCDocument doc = new BioCDocument();
      String[] pathArray = inputFilename.split("/");
      String basename = pathArray[pathArray.length - 1];
      doc.setID(basename);
      BioCPassage passage = new BioCPassage();
      passage.putInfon("docid", basename);
      passage.putInfon("inputformat", "sli");
      passage.putInfon("section","text");
      passage.setText(docText);
      passage.setOffset(0);
      // passage.putInfon("docid", formatter.toString());
      doc.addPassage(passage);
      return doc;
  }

  public static List<BioCDocument> bioCLoadFile(Reader inputReader)
    throws IOException {
    BufferedReader br;
    List<BioCDocument> docList = new ArrayList<BioCDocument>();
    int i = 0;
    String line;
    if (inputReader instanceof BufferedReader) {
      br = (BufferedReader)inputReader;
    } else {
      br = new BufferedReader(inputReader);
    }
    while ((line = br.readLine()) != null) {
      BioCDocument doc = instantiateBioCDocument(line); 
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      formatter.format("%08d.TX", i);
      doc.putInfon("docid", formatter.toString());
      docList.add(doc);
      formatter.close();
      i++;
    }
    return docList;
  }
  
  public static List<BioCDocument> bioCLoadFile(String inputFilename)
    throws FileNotFoundException, IOException
  {
    Charset charset = Charset.forName("utf-8");
BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
    List<BioCDocument> docList = new ArrayList<BioCDocument>();
    int i = 0;
    String line;
    while ((line = br.readLine()) != null) {
      BioCDocument doc = instantiateBioCDocument(line, inputFilename); 
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      formatter.format("%08d.TX", i);
      doc.putInfon("docid", formatter.toString());
      docList.add(doc);
      formatter.close();
      i++;
    }
    br.close();
    return docList;
  }

  @Override  
  public BioCDocument loadFileAsBioCDocument(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    BioCDocument document = instantiateBioCDocument(inputtext, filename);
    return document;
  }

 @Override  
 public List<BioCDocument> loadFileAsBioCDocumentList(String filename) 
    throws FileNotFoundException, IOException
  {
    return bioCLoadFile(filename);
  }

  /** read freetext into BioC Document instance */
  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException {
    return bioCLoadFile(reader);
  }
}
