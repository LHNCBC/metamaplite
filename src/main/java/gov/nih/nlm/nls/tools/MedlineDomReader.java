package gov.nih.nlm.nls.tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Reader using DOM to read PubMed XML containing Medline Citations.
 *
 *
 * Created: Mon Dec 10 10:05:03 2012
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MedlineDomReader implements MedlineReader {

  /**
   * Creates a new <code>MedlineDomReader</code> instance.
   *
   */
  public MedlineDomReader() {

  }

  /** Get pmid for MedlineCitation element
   * @param citation XML DOM node representing a citation.
   * @return string containing article Pub Med identifier (pmid)
   */
  public String getPmid(Node citation)
  {
    if (citation instanceof Element) {
      NodeList pmidlist = ((Element)citation).getElementsByTagName("PMID");
      return pmidlist.item(0).getChildNodes().item(0).getNodeValue();
    }
    return "";
  }

  /** 
   * Get article title for MedlineCitation element
   * @param citation XML DOM node representing a citation.
   * @return string containing article title.
   */
  public String getArticleTitle(Node citation)
  {
    if (citation instanceof Element) {
      NodeList titlelist = ((Element)citation).getElementsByTagName("ArticleTitle");
      return titlelist.item(0).getChildNodes().item(0).getNodeValue();
    }
    return "";
  }

  /**
   * Get article abstract 
   * @param citation XML DOM node representing a citation.
   * @return string containing concatenated text from abstract text sections.
   */
  public String getAbstract(Node citation)
  {
    StringBuilder sb = new StringBuilder();
    if (citation instanceof Element) {
      NodeList abstractlist = ((Element)citation).getElementsByTagName("Abstract");
      if (abstractlist.getLength() > 0) {
	NodeList textlist = ((Element)abstractlist.item(0)).getElementsByTagName("AbstractText");
	if (textlist.getLength() > 0) {
	  if (textlist.item(0).getChildNodes().getLength() > 0) {
	    if (sb.length() > 0) sb.append(" "); // add space between sections of structured abstracts
	    sb.append(textlist.item(0).getChildNodes().item(0).getNodeValue());
	  } else {
	    System.out.println("length of textlist[0]._get_childNodes() is " +
			       textlist.item(0).getChildNodes().getLength());
	  }
	} else {
	  System.out.println("length of textlist[0]._get_childNodes() is " + textlist.getLength());
	}
      }
    }
    return sb.toString();
  }

  /**
   * Get list of publication types for article
   * @param citation XML DOM node representing a citation.
   * @return List of strings containing publication types for article
   */
  public List<String> getPublicationTypeList(Node citation)
  {
    List<String> pubtypelist = new ArrayList<String>();
    if (citation instanceof Element) {
      NodeList journallist = ((Element)citation).getElementsByTagName("PublicationTypeList");
      if (journallist.getLength() > 0) {
	NodeList pubtypenodelist = ((Element)journallist.item(0)).getElementsByTagName("PublicationType");
	for (int i = 0; i < pubtypenodelist.getLength(); i++) {
	  Node pubtypenode = pubtypenodelist.item(i);
	  if (pubtypenode.getChildNodes().getLength() > 0) {
	    pubtypelist.add(pubtypenode.getChildNodes().item(0).getNodeValue());
	  } else {
	    System.out.println("length of pubtypenode.getChildNodes() is " + 
			       pubtypenode.getChildNodes().getLength());
	    return pubtypelist;
	  }
	}
      }
    }
    return pubtypelist;
  }

  /** get journal title
   * @param citation XML DOM node representing a citation.
   * @return content of journal title field
   */
  public String getJournalTitle(Node citation) 
  {
    if (citation instanceof Element) {
      NodeList journallist = ((Element)citation).getElementsByTagName("Journal");
      if (journallist.getLength() > 0) {
	NodeList titlelist = ((Element)journallist.item(0)).getElementsByTagName("Title");
	if (titlelist.getLength() > 0) {
	  if (titlelist.item(0).getChildNodes().getLength() > 0) {
	    return titlelist.item(0).getChildNodes().item(0).getNodeValue();
	  } else {
	    System.out.println("length of titlelist[0]._get_childNodes() is " +
			       titlelist.item(0).getChildNodes().getLength());
	  }
	} else {
	  System.out.println("length of titlelist[0]._get_childNodes() is " 
			     + titlelist.getLength());
	}
      }
    }
    return "";
  }

  /** get medline TA
   * @param citation XML DOM node representing a citation.
   * @return content of TA if it exists.
   */
  public String getMedlineTA(Node citation) 
  {
    if (citation instanceof Element) {
      NodeList journalinfolist = ((Element)citation).getElementsByTagName("MedlineJournalInfo");
      if (journalinfolist.getLength() > 0) {
	NodeList ta_list = ((Element)journalinfolist.item(0)).getElementsByTagName("MedlineTA");
	if (ta_list.getLength() > 0) {
	  if (ta_list.item(0).getChildNodes().getLength() > 0) {
	    return ta_list.item(0).getChildNodes().item(0).getNodeValue();
	  } else {
	    System.out.println("length of ta_list[0]._get_childNodes() is" +
			       ta_list.item(0).getChildNodes().getLength());
	  }
	} else {
	  System.out.println("length of ta_list[0]._get_childNodes() is " + ta_list.getLength());
	}

      }
    }
    return "";
  }

  public String getNlmUniqueId(Node citation)
  {
    if (citation instanceof Element) {
      NodeList journalinfolist = ((Element)citation).getElementsByTagName("MedlineJournalInfo");
      if (journalinfolist.getLength() > 0) {
	NodeList id_list = ((Element)journalinfolist.item(0)).getElementsByTagName("NlmUniqueID");
	if (id_list.getLength() > 0) {
	  if (id_list.item(0).getChildNodes().getLength() > 0) {
	    return id_list.item(0).getChildNodes().item(0).getNodeValue();
	  } else {
	    System.out.println("length of id_list[0]._get_childNodes() is " +
			       id_list.item(0).getChildNodes().getLength());
	  }
	} else {
	  System.out.print("length of id_list[0]._get_childNodes() is" + 
			   id_list.getLength());
	}
      }
    }
    return "";
  }

  /** 
   * Load citations into instance variable citationMap
   * @param inputFilename XML PubMed/MedLine citation file.
   * @param citationMap empty map to be filled with citations keyed by pmid
   * @param emptyCitationSet set of pmids of empty citations
   */
  public void readCitations(String inputFilename, 
			    Map<String,Citation>citationMap,
			    Set<String>emptyCitationSet)
  {
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
      W3CDomHandler domHandler = new W3CDomHandler();
      Document doc = builder.parse(inputFilename); 
      Element root = doc.getDocumentElement();
    
      // traverse through node only operating on MedlineCitation nodes.
      NodeList citationNodeList = root.getElementsByTagName("MedlineCitation");
      for (int i = 0; i < citationNodeList.getLength(); i++) {
	Node citation = citationNodeList.item(i);
	Citation citationObj = new CitationImpl();
	String PMID = this.getPmid(citation);
	citationObj.add("pmid", PMID);
	citationObj.add("articleTitle", this.getArticleTitle(citation));
	String articleAbstract = this.getAbstract(citation);
	citationObj.add("articleAbstract", articleAbstract);
	citationObj.add("journalTitle", this.getJournalTitle(citation));
	// List<String> publicationTypeList = this.getPublicationTypeList(citation);
	citationObj.add("journalAbbrev", this.getMedlineTA(citation));
	citationObj.add("journalID", this.getNlmUniqueId(citation));
	// if (publicationTypeList.contains("Review")) {
	// citationObj.setReviewStatus(true);
	// }
	citationMap.put(PMID, citationObj);
	if (articleAbstract.length() == 0) {
	  emptyCitationSet.add(PMID);
	}
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  
      throw new RuntimeException(e);
    } catch (org.xml.sax.SAXException saxe) {
      throw new RuntimeException(saxe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public Map<String,Citation> readCitations(String inputFilename) {
    return makeCitationMap(inputFilename);
  }

  /** 
   * Load citations into instance variable citationMap
   * @param inputFilename XML PubMed/MedLine citation file.
   * @return map of citations keyed by pmid.
   */
  public Map<String,Citation> makeCitationMap(String inputFilename)
			    
  {
    Map<String,Citation> citationMap = new TreeMap<String,Citation>();
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
      W3CDomHandler domHandler = new W3CDomHandler();
      Document doc = builder.parse(new File(inputFilename)); 
      Element root = doc.getDocumentElement();
    
      // traverse through node only operating on MedlineCitation nodes.
      NodeList citationNodeList = root.getElementsByTagName("MedlineCitation");
      for (int i = 0; i < citationNodeList.getLength(); i++) {
	Node citation = citationNodeList.item(i);
	Citation citationObj = new CitationImpl();
	String PMID = this.getPmid(citation);
	citationObj.add("pmid", PMID);
	citationObj.add("articleTitle", this.getArticleTitle(citation));
	citationObj.add("articleAbstract", this.getAbstract(citation));
	citationObj.add("journalTitle", this.getJournalTitle(citation));
	// List<String> publicationTypeList = this.getPublicationTypeList(citation);
	citationObj.add("journalAbbrev", this.getMedlineTA(citation));
	citationObj.add("journalID", this.getNlmUniqueId(citation));
	// if (publicationTypeList.contains("Review")) {
	// citationObj.setReviewStatus(true);
	// }
	citationMap.put(PMID, citationObj);
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  
      throw new RuntimeException(e);
    } catch (org.xml.sax.SAXException saxe) {
      throw new RuntimeException(saxe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return citationMap;
  }

  /** 
   * Load citations into instance variable citationMap
   * @param inputFilename XML PubMed/MedLine citation file.
   * @return map of citations keyed by pmid.
   */
  public List<Citation> makeCitationList(String inputFilename)
			    
  {
    List<Citation> citationList = new ArrayList<Citation>();
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
      W3CDomHandler domHandler = new W3CDomHandler();
      Document doc = builder.parse(new File(inputFilename)); 
      Element root = doc.getDocumentElement();
    
      // traverse through nodes only operating on MedlineCitation nodes.
      NodeList citationNodeList = root.getElementsByTagName("MedlineCitation");
      for (int i = 0; i < citationNodeList.getLength(); i++) {
	Node citation = citationNodeList.item(i);
	Citation citationObj = new CitationImpl();
	citationObj.add("pmid",this.getPmid(citation));
	citationObj.add("articleTitle", this.getArticleTitle(citation));
	citationObj.add("articleAbstract", this.getAbstract(citation));
	citationObj.add("journalTitle", this.getJournalTitle(citation));
	// List<String> publicationTypeList = this.getPublicationTypeList(citation);
	citationObj.add("journalAbbrev", this.getMedlineTA(citation));
	citationObj.add("journalID", this.getNlmUniqueId(citation));
	// if (publicationTypeList.contains("Review")) {
	// citationObj.setReviewStatus(true);
	// }
	citationList.add(citationObj);
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  
      throw new RuntimeException(e);
    } catch (org.xml.sax.SAXException saxe) {
      throw new RuntimeException(saxe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return citationList;
  }


}
