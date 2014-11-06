
//
package gov.nih.nlm.nls.metamap.lite.metamap;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;

/**
 *
 */

public class MetaMapIndexes {
  /** cui -> concept index (actually bi-directional) */
  public SearchIndex cuiConceptIndex;
  /** first words of one wide index */
  public SearchIndex firstWordsOfOneWideIndex;
  /** cui -> source info index */
  public SearchIndex cuiSourceInfoIndex;
  /** variants index */
  public SearchIndex varsIndex;

  public QueryParser conceptQueryParser;
  public QueryParser strQueryParser;
  public QueryParser nmstrQueryParser;
  public QueryParser cuiQueryParser;
  public QueryParser varQueryParser;


  public MetaMapIndexes() 
    throws IOException, FileNotFoundException, ParseException
  {
    // indexes
    this.cuiConceptIndex = new SearchIndex("/rhome/wjrogers/lucenedb/cuiconcept");
    this.firstWordsOfOneWideIndex = new SearchIndex("/rhome/wjrogers/lucenedb/first_words_of_one_WIDE");
    this.cuiSourceInfoIndex = new SearchIndex("/rhome/wjrogers/lucenedb/cui_sourceinfo");
    this.varsIndex = new SearchIndex("/rhome/wjrogers/lucenedb/vars");
    // query parsers for various fields
    this.conceptQueryParser = SearchIndex.newQueryParser("concept");
    this.strQueryParser = SearchIndex.newQueryParser("str");
    this.nmstrQueryParser = SearchIndex.newQueryParser("nmstr");
    this.cuiQueryParser = SearchIndex.newQueryParser("cui");
    this.varQueryParser = SearchIndex.newQueryParser("var");
  }

}
