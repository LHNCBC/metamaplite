package gov.nih.nlm.nls.metamap.mmi;

import java.util.List;
import gov.nih.nlm.nls.metamap.lite.types.Position;

/**
 * A marker interface for tuples.   Can I use PersistantHashMap instead?
 *
 * <p>
 * Created: Wed Mar  8 09:02:38 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface Tuple {
  String getTerm();
  void setTerm(final String term);
  String getField();
  void setField(final String field);
  int getNSent();
  void setNSent(final int nSent);
  String getText();
  void setText(final String text);
  String getLexCat();
  void setLexCat(final String LexCat);
  int getNeg();
  void setNeg(final int Neg);
  List<Position> getPosInfo();
  void setPosInfo(final List<Position> PosInfo);
}
