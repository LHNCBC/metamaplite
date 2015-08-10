//
package gov.nih.nlm.nls.metamap.lite.lucene;

import java.util.Map;
import java.util.List;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Load cui -> concept table into Lucene index.
 * <p>
 * Example of use:
 * <pre>
 * examples.LoadCuiConcept /rhome/wjrogers/lucenedb/cuiconcept \
 *   /nfsvol/nls/specialist/module/db_access/data.Base.2014AA/MetaWordIndex/model.strict/cui_concept.txt
 * </pre>
 */

public class LoadCUIConcept {

  static Version version = Version.LATEST;
  static Analyzer analyzer = new EnglishAnalyzer();

  public static void loadTable(String databasedirname, String tableFilename) 
    throws IOException, FileNotFoundException, ParseException
  {

    // Directory index = getMemoryIndex();
    // To store an index on disk, use this instead:
    // Directory index = getDiskIndex("/rhome/wjrogers/lucenedb/example");
    Directory index = FSDirectory.open(new File(databasedirname));
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
    IndexWriter iwriter = new IndexWriter(index, config);

    BufferedReader br = new BufferedReader(new FileReader( tableFilename ));
    int count = 0;
    String line;
    while ((line = br.readLine()) != null) {
      Document doc = new Document();
      String[] fields = line.split("\\|");
      String cui = fields[0];
      String conceptName = fields[1];
      doc.add(new Field("cui", cui, TextField.TYPE_STORED));
      doc.add(new Field("conceptname", conceptName, TextField.TYPE_STORED));
      iwriter.addDocument(doc);
      count++;
    }
    iwriter.close();
    br.close();
    System.out.println("inserted " + count + " documents.");
  }

  /**
   *
   * @param args - Arguments passed from the command line
   **/
  public static void main(String[] args)
    throws IOException, ParseException
  {
    if (args.length > 1) {
      loadTable(args[0], args[1]);
    } else {
      System.err.println("usage: examples.LoadCuiConcept dbdirname cui-concept-tablename");
    }
  }
}
