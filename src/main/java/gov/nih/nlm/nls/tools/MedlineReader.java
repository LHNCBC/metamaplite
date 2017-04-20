package gov.nih.nlm.nls.tools;

import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Interface for reading Medline citations
 *
 * Created: Mon Dec 10 10:11:35 2012
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface MedlineReader {

  /**
   * Extract content of individual citations to map of citations by pmid.
   * @param inputFilename filename referencing XML PUBMED citations file.
   * @param citationMap map of citations keyed by pmid.
   * @param emptyCitationSet set of pmids of empty citations.
   */
  void readCitations(String inputFilename, 
		     Map<String,Citation>citationMap,
		     Set<String>emptyCitationSet);
  /**
   * Extract content of individual citations to map of citations by pmid.
   * @param inputFilename filename referencing XML PUBMED citations file.
   * @return map of citations keyed by pmid.
   */
  Map<String,Citation> readCitations(String inputFilename);

  /**
   * Extract content of individual citations to map of citations by pmid.
   * @param inputFilename filename referencing XML PUBMED citations file.
   * @return map of citations keyed by pmid.
   */
  Map<String,Citation> makeCitationMap(String inputFilename);

  /**
   * Extract content of individual citations to map of citations by pmid.
   * @param inputFilename filename referencing XML PUBMED citations file.
   * @return list of citations
   */
  List<Citation> makeCitationList(String inputFilename);
}
