package gov.nih.nlm.nls.metamap.lite;

import bioc.BioCSentence;
import bioc.BioCPassage;
import java.util.Set;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import java.util.List;
import java.util.ArrayList;

/**
 * Describe class HelperMethods here.
 *
 *
 * Created: Fri May 25 08:30:02 2018
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class HelperMethods {

  /**
   * Creates a new <code>HelperMethods</code> instance.
   *
   */
  public HelperMethods() {

  }

  public static void displayEntitySet(String entitySetName, Set<Entity> entitySet) {
    for (Entity entity: entitySet) {
      System.out.println(entitySetName + ": " + entity);
    }
  }

  /** 
   * Segment passage sentences by lines.
   * @param passage clinical text passage
   * @return passage with sentences seqmented by lines
   */
  public static BioCPassage segmentLines(BioCPassage passage) {
    BioCPassage passage0 = new BioCPassage(passage);
    List <BioCSentence> sentenceList = new ArrayList<BioCSentence>();
    int offset = passage0.getOffset();
    int passageOffset = passage0.getOffset();
    String text = passage0.getText();
    String[] segmentList = text.split("\n");
    for (String segment: segmentList) {
      offset = text.indexOf(segment, offset);
      if (segment.trim().length() > 0) {
	BioCSentence sentence = new BioCSentence();
	sentence.setOffset(offset);
	sentence.setText(segment);
	sentence.setInfons(passage.getInfons());
	sentenceList.add(sentence);
	passage0.addSentence(sentence);
      }
      offset = segment.length(); // preserve offsets even for blank lines.
    }
    return passage0;
  }
}

