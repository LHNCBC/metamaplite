package gov.nih.nlm.nls.metamap.dfbuilder;

/**
 * Describe class Mrsat here.
 *
 *
 * Created: Wed Nov 27 12:22:47 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Mrsat {

  String cui;
  String lui;
  String sui;
  String metaui;
  String stype;
  String code;
  String atui;
  String satui;
  String atn;
  String sab;
  String atv;
  String suppress;
  String cvf;
    
  /**
   * Creates a new <code>Mrsat</code> instance.
   *
   * @param cui Unique identifier for concept 
   * @param lui Unique identifier for term
   * @param sui Unique identifier for string
   * @param metaui .
   * @param stype .
   * @param code .
   * @param atui .
   * @param satui .
   * @param atn .
   * @param sab .
   * @param atv .
   * @param suppress .
   * @param cvf .
   */
  public Mrsat(String cui, String lui, String sui, String metaui,
	       String stype, String code, String atui, String satui,
	       String atn, String sab, String atv, String suppress, String cvf)

  {
    this.cui = cui;
    this.lui = lui;
    this.sui = sui;
    this.metaui = metaui;
    this.stype = stype;
    this.code = code;
    this.atui = atui;
    this.satui = satui;
    this.atn = atn;
    this.sab = sab;
    this.atv = atv;
    this.suppress = suppress;
    this.cvf = cvf;
  }

  /**
   * Creates a new <code>Mrsat</code> instance from fieldlist created from
   * String.split of MRSAT record. 
   *
   * @param fieldlist array of field from String.split of MRSAT record.
   */
  public Mrsat(String[] fieldlist) {
    // :cui :lui :sui :metaui :stype :code :atui :satui :atn :sab :atv :suppress :cvf
    this(fieldlist[0], fieldlist[1], fieldlist[2], fieldlist[3],
	 fieldlist[4], fieldlist[5], fieldlist[6], fieldlist[7],
	 fieldlist[8], fieldlist[9], fieldlist[10], fieldlist[11],
	 "");
  }

  public String getCui() {
    return this.cui;
  }

  public String getLui() {
    return this.lui;
  }

  public String getSui() {
    return this.sui;
  }

  public String getMetaui() {
    return this.metaui;
  }

  public String getStype() {
    return this.stype;
  }

  public String getCode() {
    return this.code;
  }

  public String getAtui() {
    return this.atui;
  }

  public String getSatui() {
    return this.satui;
  }

  public String getAtn() {
    return this.atn;
  }

  public String getSab() {
    return this.sab;
  }

  public String getAtv() {
    return this.atv;
  }

  public String getSuppress() {
    return this.suppress;
  }

  public String getCvf() {
    return this.cvf;
  }

}
