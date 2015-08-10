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
 *
 */

public class SearchCUIConcept {
  static Version version = Version.LATEST;
  static Analyzer analyzer = new EnglishAnalyzer();
  Directory index;
  DirectoryReader ireader;
  IndexSearcher isearcher;
  QueryParser parser;


  public SearchCUIConcept()
    throws IOException, ParseException
  {
    // Directory index = getMemoryIndex();
    // To store an index on disk, use this instead:
    // Directory index = getDiskIndex("/rhome/wjrogers/lucenedb/example");
    this.index = FSDirectory.open(new File("/rhome/wjrogers/lucenedb/cuiconcept"));
    // Now search the index:
    this.ireader = DirectoryReader.open(index);
    this.isearcher = new IndexSearcher(ireader);
    // Parse a simple query that searches for "":
    this.parser = new QueryParser(Version.LATEST, "conceptname", analyzer);
  }

  public List<StringTriple> getConceptListForTerm(String term)
    throws IOException, ParseException
  {
    List<StringTriple> conceptList = new ArrayList<StringTriple>();
    // Term queryTerm = new Term("concept", term);
    // Query query = new TermQuery(queryTerm);
    if (CharUtils.isAlphaNumeric(term.charAt(0))) {
      Query query = this.parser.parse(term);
      ScoreDoc[] hits = this.isearcher.search(query, null, 1000).scoreDocs;
      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
	Document hitDoc = this.isearcher.doc(hits[i].doc);
	if (term.toLowerCase().equals(hitDoc.get("concept").toLowerCase())) {
	  conceptList.add(new StringTriple(term, hitDoc.get("cui"), hitDoc.get("concept")));
	}
      }
    }
    return conceptList;
  }

  /** Get concept list for term matching head of term in database.
   */
  public List<StringTriple> getConceptListForTermMatchingHead(String term)
    throws IOException, ParseException
  {
    List<StringTriple> conceptList = new ArrayList<StringTriple>();
    // Term queryTerm = new Term("conceptname", term);
    // Query query = new TermQuery(queryTerm);
    if (CharUtils.isAlphaNumeric(term.charAt(0))) {
      Query query = this.parser.parse(term);
      ScoreDoc[] hits = this.isearcher.search(query, null, 1000).scoreDocs;
      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
	Document hitDoc = this.isearcher.doc(hits[i].doc);
	conceptList.add(new StringTriple(term, hitDoc.get("cui"), hitDoc.get("conceptname")));
      }
    }
    return conceptList;
  }

  public List<StringTriple> listConcepts(String[] termlist) // this is subject to change
    throws IOException, ParseException
  {
    List<StringTriple> conceptList = new ArrayList<StringTriple>();
    for (String term: termlist) {
      // Query query = this.parser.parse(term);

      Term queryTerm = new Term("conceptname", term.toLowerCase());
      Query query = new TermQuery(queryTerm);

      ScoreDoc[] hits = this.isearcher.search(query, null, 1000).scoreDocs;
      //junit: assertEquals(1, hits.length);
      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
	Document hitDoc = this.isearcher.doc(hits[i].doc);
	if (term.toLowerCase().equals(hitDoc.get("concept").toLowerCase())) {
	  conceptList.add(new StringTriple(term, hitDoc.get("cui"), hitDoc.get("concept")));
	}
      }
    }
    return conceptList;
  }

  public void cleanup()
    throws IOException, ParseException
  {
    this.ireader.close();
    this.index.close();
  }


  public static void main(String[] args)
    throws IOException, ParseException
  {
    SearchCUIConcept inst = new SearchCUIConcept();
    Query query = inst.parser.parse(args[0]);
    ScoreDoc[] hits = inst.isearcher.search(query, null, 1000).scoreDocs;
    //junit: assertEquals(1, hits.length);
    // Iterate through the results:
    for (int i = 0; i < hits.length; i++) {
      Document hitDoc = inst.isearcher.doc(hits[i].doc);
      //junit:   assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
      System.out.println("cui: " + hitDoc.get("cui"));
      System.out.println("conceptname: " + hitDoc.get("conceptname"));
      System.out.println("----");
    }
    inst.cleanup();
  }
}
