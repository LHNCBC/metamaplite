//
package gov.nih.nlm.nls.metamap.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import java.util.List;
import java.util.ArrayList;

import bioc.BioCDocument;
import bioc.BioCPassage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unstructured text.
 */

public class FreeText implements BioCDocumentLoader
{
  /** log4j logger instance */
  private static final Logger logger = LoggerFactory.getLogger(FreeText.class);

  String text;
  public FreeText() { }
  public FreeText(String text) { this.text = text; }
  public String getText() {
    return this.text;
  }
  public void setText(String text) { this.text = text; }

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

  public static String loadFile(String inputFilename, Charset charset)
    throws FileNotFoundException, IOException
  {
    File inputFile  = new File(inputFilename);
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), charset));
    long fileLen = inputFile.length();
    char[] buf = new char[(int)fileLen];
    br.read(buf,0, (int)fileLen);
    br.close();
    String text = new String(buf);
    return text;
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

  public static BioCDocument instantiateBioCDocument(String docText) 
  {
    BioCDocument document = new BioCDocument();
    logger.debug(docText);
    BioCPassage passage = new BioCPassage();
    passage.setOffset(0);
    passage.setText(docText);
    passage.putInfon("docid", "00000000.tx");
    passage.putInfon("inputformat", "freetext");
    passage.putInfon("section", "text");
    document.addPassage(passage);
    document.setID("00000000.tx");
    return document;
  }

  public static BioCDocument instantiateBioCDocument(String docText, String inputFilename) 
  {
    BioCDocument document = new BioCDocument();
    String[] pathArray = inputFilename.split("/");
    String basename = pathArray[pathArray.length - 1];
    document.setID(basename);
    logger.debug(docText);
    BioCPassage passage = new BioCPassage();
    passage.setOffset(0);
    passage.setText(docText);
    passage.putInfon("docid", basename);
    passage.putInfon("inputformat", "freetext");
    document.addPassage(passage);
    document.setID(inputFilename);
    return document;
  }

  /** Read free text document from reader.
   * @param reader reader for input file
   * @return list of BioC document instances.
   * @throws IOException i/o exception
   */
  public static List<BioCDocument> readFreeText(Reader reader) 
    throws IOException
  {
    String inputtext = FreeText.read(reader);
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    BioCDocument document;
    if (reader.equals(new InputStreamReader(System.in))) {
      document = instantiateBioCDocument(inputtext, "stdin");
    } else {
      document = instantiateBioCDocument(inputtext);
    }
    documentList.add(document);
    return documentList;
  }

  public static List<BioCDocument> loadFreeTextFile(String filename) 
    throws FileNotFoundException, IOException
  {
    String inputtext = FreeText.loadFile(filename);
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    BioCDocument document = instantiateBioCDocument(inputtext, filename);
    documentList.add(document);
    return documentList;
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
    return loadFreeTextFile(filename);
  }

  /** read freetext into BioC Document instance */
  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException {
    return readFreeText(reader);
  }
}
