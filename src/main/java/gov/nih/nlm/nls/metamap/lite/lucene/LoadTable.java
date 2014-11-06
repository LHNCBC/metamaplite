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

public class LoadTable {

  static Version version = Version.LATEST;
  static Analyzer analyzer = new EnglishAnalyzer();

  public static void loadTable(String databasedirname, String tableFilename, List<String> fieldnames) 
    throws IOException, FileNotFoundException, ParseException
  {

    // Directory index = getMemoryIndex();
    // To store an index on disk, use this instead:
    // Directory index = getDiskIndex("/rhome/wjrogers/lucenedb/example");
    Directory index = FSDirectory.open(new File(databasedirname));
    IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
    IndexWriter iwriter = new IndexWriter(index, config);

    BufferedReader br = new BufferedReader(new FileReader( tableFilename ));
    int count = 0;
    String line;
    while ((line = br.readLine()) != null) {
      Document doc = new Document();
      String[] fields = line.split("\\|");
      for (int i = 0; i < Math.min(fields.length,fieldnames.size()); i++) {
	doc.add(new Field(fieldnames.get(i), fields[i], TextField.TYPE_STORED));
      }
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
    if (args.length > 3) {
      List<String> fieldnames = new ArrayList<String>();
      for (int i = 2; i<args.length; i++) {
	fieldnames.add(args[i]);
      }
      loadTable(args[0], args[1], fieldnames);
    } else {
      System.err.println("usage: examples.LoadTable dbdirname cui-concept-tablename fields");
    }
  }
}
