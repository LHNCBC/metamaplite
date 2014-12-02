
//
package gov.nih.nlm.nls.types;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */

public class PassageImpl 
  implements Passage
{
  Map<String,String> infons = new HashMap<String,String>();
  int offset;
  String text;
  List<Sentence> sentences = new ArrayList<Sentence>();
  List<Annotation> annotations = new ArrayList<Annotation>();

  @Override
  public Map<String,String> getInfons() {
    return infons;
  }

  @Override
  public int getOffset() {
    return this.offset;
  }

  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public List<Sentence> getSentences() {
    return this.sentences;
  }

  @Override
  public List<Annotation> getAnnotations() {

    return this.annotations;
  }

  public void setText(String text) { this.text = text; }
  public void putInfon(String key, String value) { this.infons.put(key,value); }
}
