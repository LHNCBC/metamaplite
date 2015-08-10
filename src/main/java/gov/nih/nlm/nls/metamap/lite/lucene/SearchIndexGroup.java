//
package gov.nih.nlm.nls.metamap.lite.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;

/**
 *
 */

public class SearchIndexGroup {

  List<SearchIndex> indexGroup;

  public SearchIndexGroup(List<SearchIndex> group) { this.indexGroup = group; }
  public void addIndex(SearchIndex index) { this.indexGroup.add(index); }
  public List<Document> lookup(String term,
				      QueryParser queryParser,
				      int resultLength)
    throws IOException, ParseException
  {
    List<Document> hitList = new ArrayList<Document>();
    for (SearchIndex index: indexGroup) {
      hitList.addAll(index.lookup(term, queryParser, resultLength));
    }
    return hitList;
  }
}
