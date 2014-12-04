//
package gov.nih.nlm.nls.metamap.lite.lucene;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import gov.nih.nlm.nls.metamap.prefix.CharUtils;


/**
 * Index representation for MetaMap Lite; the representation consists
 * index, index reader, and index searcher.
 */

public class SearchIndex {
  static Version version = Version.LATEST;
  static Analyzer analyzer = new EnglishAnalyzer();
  Directory index;
  DirectoryReader ireader;
  IndexSearcher isearcher;

  /**
   * Instantiate lucene index reader for index at indexDirectoryname
   * @param indexDirectoryName path of index to opened.
   */
  public SearchIndex(String indexDirectoryName)
    throws IOException, ParseException
  {
    // Directory index = getMemoryIndex();
    // To store an index on disk, use this instead:
    // Directory index = getDiskIndex("/rhome/wjrogers/lucenedb/strict");
    this.index = FSDirectory.open(new File(indexDirectoryName));
    // Now search the index:
    this.ireader = DirectoryReader.open(index);
    this.isearcher = new IndexSearcher(ireader);
  }

  public static Directory openIndex(String dbDirname) 
    throws IOException
  {
    return FSDirectory.open(new File(dbDirname));
  }

  public static DirectoryReader openDirectoryReader(Directory index)
    throws IOException
  {
    return DirectoryReader.open(index);
  }

  public static IndexSearcher newIndexSearcher(DirectoryReader indexReader) {
    return new IndexSearcher(indexReader);
  }

  /** Create a query parser for specified fieldname
   * @param fieldname name for field to search.
   */
  public static QueryParser newQueryParser(String fieldname) {
    return new QueryParser(Version.LATEST, fieldname, analyzer);
  }

  /**
   * Lookup term using supplied query parser and index searcher.
   * @param term          target term
   * @param queryParser   query parser for field
   * @param indexSearcher index searcher
   * @param resultLength expected size of query result.
   */
  public static List<Document> lookup(String term,
				      QueryParser queryParser,
				      IndexSearcher indexSearcher,
				      int resultLength)
    throws IOException, ParseException
  {
    List<Document> documentList = new ArrayList<Document>();
    try {
      if (CharUtils.isAlphaNumeric(term.charAt(0))) {
	Query query = queryParser.parse(term.toLowerCase());
	ScoreDoc[] hits = indexSearcher.search(query, null, resultLength).scoreDocs;
	// Iterate through the results:
	for (int i = 0; i < hits.length; i++) {
	  documentList.add(indexSearcher.doc(hits[i].doc));
	}
      }
    } catch (ParseException pe) {
      System.err.println("term causing error is: " + term);
      throw pe;
    }
    return documentList;
  }

  /**
   * Lookup term using index reader instantiated by class instance
   * using supplied query parser.
   * @param term         target term
   * @param queryParser  query parser for field
   * @param resultLength expected size of query result.
   */
  public List<Document> lookup(String term,
			       QueryParser queryParser,
			       int resultLength)
    throws IOException, ParseException
  {
    return SearchIndex.lookup(term, queryParser, this.isearcher, resultLength);
  }
}
