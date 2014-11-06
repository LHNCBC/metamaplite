
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.mmi.MMI;
import gov.nih.nlm.nls.metamap.document.ChemDNER;
import gov.nih.nlm.nls.metamap.document.PubMedDocument;

import gov.nih.nlm.nls.utils.StringUtils;

/**
 *
 */

public class Main {

  public List<String> loadFile(String inputFilename)
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

  public static void main(String[] args)
    throws FileNotFoundException, IOException, ParseException
  {
    if (args.length > 0) {
      SimplePipeline inst = new SimplePipeline();
      inst.init();
      List<String> documentList = inst.loadFile(args[0]);
      
      /*CHEMDNER style documents*/
      for (String docText: documentList) {

	PubMedDocument cDoc = ChemDNER.instantiateSLDIDocument(docText);

	List<List<Entity>> titleListOfEntityList = inst.processText(cDoc.getTitle());
	for (List<Entity> entityList: titleListOfEntityList) {
	  MMI.displayEntityList(entityList);
	}

	List<List<Entity>> listOfEntityList = inst.processText(cDoc.getAbstract());
	for (List<Entity> entityList: listOfEntityList) {
	  MMI.displayEntityList(entityList);
	}
      }
    } else {
      System.err.println("usage: filename");
    }   
  }
}
