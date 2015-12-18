//
package gov.nih.nlm.nls.metamap.lite.metamap;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Properties;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import gov.nih.nlm.nls.metamap.lite.lucene.SearchIndex;

/**
 * Tables from MetaMap imported into lucene core indices.
 */

public class MetaMapIndexes {
  /** cui -&gt; concept index (actually bi-directional) */
  public SearchIndex cuiConceptIndex;
  /** first words of one wide index */
  public SearchIndex firstWordsOfOneWideIndex;
  /** cui -&gt; source info index */
  public SearchIndex cuiSourceInfoIndex;
  /** cui -&gt; semantic type index */
  public SearchIndex cuiSemanticTypeIndex;
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
    this.cuiConceptIndex =
      new SearchIndex(System.getProperty("metamaplite.cuiconceptindex",
					 "/net/indlx1/export/home/wjrogers/Projects/metamaplite/data/lucenedb/strict/cuiconcept"));
    this.firstWordsOfOneWideIndex =
      new SearchIndex(System.getProperty("metamaplite.firstwordsofonewideindex",
					 "/net/indlx1/export/home/wjrogers/Projects/metamaplite/data/lucenedb/strict/first_words_of_one_WIDE"));
    this.cuiSourceInfoIndex =
      new SearchIndex(System.getProperty("metamaplite.cuisourceinfoindex",
					 "/net/indlx1/export/home/wjrogers/Projects/metamaplite/data/lucenedb/strict/cui_sourceinfo"));
    this.cuiSemanticTypeIndex =
      new SearchIndex(System.getProperty("metamaplite.cuisemantictypeindex",
					 "/net/indlx1/export/home/wjrogers/Projects/metamaplite/data/lucenedb/strict/cui_st"));
    this.varsIndex =
      new SearchIndex(System.getProperty("metamaplite.varsindex",
					 "/net/indlx1/export/home/wjrogers/Projects/metamaplite/data/lucenedb/strict/vars"));

    // query parsers for various fields
    this.conceptQueryParser = SearchIndex.newQueryParser("concept");
    this.strQueryParser = SearchIndex.newQueryParser("str");
    this.nmstrQueryParser = SearchIndex.newQueryParser("nmstr");
    this.cuiQueryParser = SearchIndex.newQueryParser("cui");
    this.varQueryParser = SearchIndex.newQueryParser("var");
  }

  public MetaMapIndexes(Properties properties) 
    throws IOException, FileNotFoundException, ParseException
  {
    // indexes
    this.cuiConceptIndex =
      new SearchIndex(properties.getProperty("metamaplite.cuiconceptindex",
					     "/nfsvol/nlsaux15/lucenedb/strict/cuiconcept"));
    this.firstWordsOfOneWideIndex =
      new SearchIndex(properties.getProperty("metamaplite.firstwordsofonewideindex",
					     "/nfsvol/nlsaux15/lucenedb/strict/first_words_of_one_WIDE"));
    this.cuiSourceInfoIndex =
      new SearchIndex(properties.getProperty("metamaplite.cuisourceinfoindex",
					     "/nfsvol/nlsaux15/lucenedb/strict/cui_sourceinfo"));
    this.cuiSemanticTypeIndex =
      new SearchIndex(properties.getProperty("metamaplite.cuisemantictypeindex",
					     "/nfsvol/nlsaux15/lucenedb/strict/cui_st"));
    this.varsIndex =
      new SearchIndex(properties.getProperty("metamaplite.varsindex",
					     "/nfsvol/nlsaux15/lucenedb/strict/vars"));

    // query parsers for various fields
    this.conceptQueryParser = SearchIndex.newQueryParser("concept");
    this.strQueryParser = SearchIndex.newQueryParser("str");
    this.nmstrQueryParser = SearchIndex.newQueryParser("nmstr");
    this.cuiQueryParser = SearchIndex.newQueryParser("cui");
    this.varQueryParser = SearchIndex.newQueryParser("var");
  }

}
