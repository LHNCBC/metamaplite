package gov.nih.nlm.nls.metamap.mmi;

import java.util.List;
import java.util.stream.Collectors;
import gov.nih.nlm.nls.metamap.lite.types.Position;
import gov.nih.nlm.nls.metamap.lite.types.PositionImpl;

/**
 * Container for term, field, sentence no, matching text, lexical
 * category, negation status, and positional information.
 *
 * Created: Wed Mar  8 15:13:14 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Tuple7 implements Tuple {
  private String term;
  private String field = "";
  private int nSent;
  private String text;		
  private String lexCat;
  private int neg;
  private List<Position> posInfo;

  public Tuple7(String term,
		String field,
		int nSent,
		String text,		
		String lexCat,
		int neg,
		List<Position> posInfo)
  {
    this.term = term;
    this.field = field;
    this.nSent = nSent;
    this.text = text;
    this.lexCat = lexCat;
    this.neg = neg;
    this.posInfo = posInfo;
  }

  // tuple7("Epidemic",tx,1,"epidemic",adj,0,[14/8])
  // String,Term;String,Field;int,NSent;String,Text;String,LexCat;int,Neg; String,PosInfo;

  public final String getTerm() {
    return term;
  }

  public final void setTerm(final String term) {
    this.term = term;
  }

  public final String getField() {
    return field;
  }

  public final void setField(final String field) {
    this.field = field;
  }

  public final int getNSent() {
    return nSent;
  }

  public final void setNSent(final int nSent) {
    this.nSent = nSent;
  }

  public final String getText() {
    return text;
  }

  public final void setText(final String text) {
    this.text = text;
  }

  public final String getLexCat() {
    return lexCat;
  }

  public final void setLexCat(final String LexCat) {
    this.lexCat = lexCat;
  }

  public final int getNeg() {
    return neg;
  }

  public final void setNeg(final int Neg) {
    this.neg = neg;
  }

  public final List<Position> getPosInfo() {
    return posInfo;
  }

  public final void setPosInfo(final List<Position> PosInfo) {
    this.posInfo = posInfo;
  }

  public boolean equals(Object obj) {
    return this.term.equals(((Tuple7)obj).getTerm()) &&
      this.posInfo.equals(((Tuple7)obj).getPosInfo());
  }
  public int hashCode() {
    return this.term.hashCode() +
      this.field.hashCode() +
      this.nSent +
      ((this.text != null) ? this.text.hashCode() : 1) +
      this.lexCat.hashCode() +
      this.neg +
      this.posInfo.hashCode();
  }
  public String toString() {
    return this.term + "-" +
      this.field + "-" +
      this.nSent + "-" +
      this.text + "-" +		
      this.lexCat + "-" +
      this.neg + "-" +
      this.posInfo.stream().map(i -> ((PositionImpl)i).toStringStartLength()).collect(Collectors.joining(","));
  }
}
