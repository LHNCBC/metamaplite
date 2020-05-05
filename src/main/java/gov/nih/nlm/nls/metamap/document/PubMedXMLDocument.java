
//
package gov.nih.nlm.nls.metamap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import bioc.BioCDocument;
import bioc.BioCPassage;

/**
 * Convert a PubMed XML Document into a BioC document.
 *
 * Created: Thu Aug 31 14:26:24 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */

public class PubMedXMLDocument 
  implements BioCDocumentLoader
{
  XMLInputFactory inputFactory = XMLInputFactory.newInstance();

  /**
   * <code>loadFileAsBioCDocument</code>- load PubXML document from
   * filename as a BioCDocument
   *
   * @param filename a <code>Filename</code> value
   * @return a <code>BioCDocument</code> value
   * @exception FileNotFoundException if an error occurs
   * @exception IOException if an error occurs
   */
  @Override
  public BioCDocument loadFileAsBioCDocument(String filename)
    throws FileNotFoundException, IOException
  {
    BioCDocument doc = new BioCDocument();
    try {
      InputStream in = new FileInputStream(filename);
      XMLStreamReader streamReader = this.inputFactory.createXMLStreamReader(in);
      while (streamReader.hasNext()) {
	if (streamReader.isStartElement()) {
	  switch (streamReader.getLocalName())
	    {
	    case "PMID": {
	      doc.setID(streamReader.getElementText());
	      break;
	    }
	    case "ArticleTitle": {
	      BioCPassage title = new BioCPassage();
	      title.setText(streamReader.getElementText());
	      title.setOffset(0);
	      title.putInfon("docid", doc.getID());
	      title.putInfon("section","title");
	      doc.addPassage(title);
		break;
	    }
	    case "AbstractText" : {
	      BioCPassage abstractPassage = new BioCPassage();
	      abstractPassage.setText(streamReader.getElementText());
	      abstractPassage.putInfon("docid", doc.getID());
	      abstractPassage.putInfon("section","abstract");
	      abstractPassage.setOffset(0);
	      doc.addPassage(abstractPassage);
	      break;
	    }
	    }
	}
	streamReader.next();
      }
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return doc;
  }

  @Override
  public List<BioCDocument> readAsBioCDocumentList(Reader reader)
    throws IOException
  {
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    try {
      BioCDocument doc = new BioCDocument();
      XMLStreamReader streamReader = this.inputFactory.createXMLStreamReader(reader);
      while (streamReader.hasNext()) {
	if (streamReader.isStartElement()) {
	  switch (streamReader.getLocalName()) {
	  case "PubmedArticle": {
	    doc = new BioCDocument();
	    documentList.add(doc);
	    break;
	  }
	  case "PMID": {
	    doc.setID(streamReader.getElementText());
	    break;
	  }
	  case "ArticleTitle": {
	    BioCPassage title = new BioCPassage();
	    title.setText(streamReader.getElementText());
	    title.setOffset(0);
	    title.putInfon("docid", doc.getID());
	    title.putInfon("section","title");
	    doc.addPassage(title);
	    break;
	  }
	  case "AbstractText" : {
	    BioCPassage abstractPassage = new BioCPassage();
	    abstractPassage.setText(streamReader.getElementText());
	    abstractPassage.putInfon("docid", doc.getID());
	    abstractPassage.putInfon("section","abstract");
	    abstractPassage.setOffset(0);
	    doc.addPassage(abstractPassage);
	    break;
	  }
	  }
	}
	streamReader.next();
      }      
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return documentList;
  }

  /**
   * <code>readAsBioCDocumentList</code> - load a set of PubmedXML
   * documents from filename.
   *
   * @param filename a <code>Filename</code> value
   * @return a <code>List</code> value
   * @exception FileNotFoundException if an error occurs
   * @exception IOException if an error occurs
   */
  @Override
  public List<BioCDocument> loadFileAsBioCDocumentList(String filename)
    throws FileNotFoundException, IOException
  {
    return readAsBioCDocumentList(new FileReader(filename));
  }

}
