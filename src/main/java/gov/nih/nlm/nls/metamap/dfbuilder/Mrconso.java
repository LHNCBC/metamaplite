package gov.nih.nlm.nls.metamap.dfbuilder;

/**
 * Representation of one MRCONSO Rich Release Format Record
 *
 * Created: Wed Nov 27 10:51:29 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */

public class Mrconso {
  String cui;
  String lat;
  String ts;
  String lui;
  String stt;
  String sui;
  String ispref;
  String aui;
  String saui;
  String scui;
  String sdui;
  String sab;
  String tty;
  String code;
  String str;
  String srl;
  String suppress;
  String cvf;


  /** 
   * Create <code>Mrconso</code> instance 
   *
   * @param cui Unique identifier for concept 
   * @param lat Language of term
   * @param ts  Term status
   * @param lui Unique identifier for term
   * @param stt String type
   * @param sui Unique identifier for string
   * @param ispref Atom status - preferred (Y) or not (N) for this string within this concept
   * @param aui  Unique identifier for atom - variable length field, 8 or 9 characters
   * @param saui Source asserted atom identifier [optional]
   * @param scui Source asserted concept identifier [optional]
   * @param sdui Source asserted descriptor identifier [optional]
   * @param sab Abbreviated source name (SAB).
   * @param tty Abbreviation for term type in source vocabulary
   * @param code Abbreviation for term type in source vocabulary
   * @param str String
   * @param srl Source restriction level
   * @param suppress Suppressible flag. Values = O, E, Y, or N
   * @param cvf Content View Flag.
   */
  public Mrconso(String cui, String lat, String ts, String lui, String stt,
	  String sui, String ispref, String aui, String saui, String scui,
	  String sdui, String sab, String tty, String code, String str,
	  String srl, String suppress, String cvf)
  {
    this.cui = cui;
    this.lat = lat;
    this.ts = ts;
    this.lui = lui;
    this.stt = stt;
    this.sui = sui;
    this.ispref = ispref;
    this.aui = aui;
    this.saui = saui;
    this.scui = scui;
    this.sdui = sdui;
    this.sab = sab;
    this.tty = tty;
    this.code = code;
    this.str = str;
    this.srl = srl;
    this.suppress = suppress;
    this.cvf = cvf;
  }

  /** 
   * Create <code>Mrconso</code> instance from fieldlist created from
   * String.split of MRCONSO record. 
   *
   * @param fieldlist array of field from String.split of MRCONSO record.
   */
  public Mrconso(String[] fieldlist) {
      this(fieldlist[0], fieldlist[1], fieldlist[2], fieldlist[3],
	   fieldlist[4], fieldlist[5], fieldlist[6], fieldlist[7],
	   fieldlist[8], fieldlist[9], fieldlist[10], fieldlist[11],
	   fieldlist[12], fieldlist[13], fieldlist[14], fieldlist[15],
	   fieldlist[16], "");
  }

  public String getCui() {
    return this.cui;
  }

  public String getLat() {
    return this.lat;
  }

  public String getTs() {
    return this.ts ;
  }

  public String getLui() {
    return this.lui;
  }

  public String getStt() {
    return this.stt;
  }

  public String getSui() {
    return this.sui;
  }

  public String getIspref() {
    return this.ispref;
  }

  public String getAui() {
    return this.aui;
  }

  public String getSaui() {
    return this.saui;
  }

  public String getScui() {
    return this.scui;
  }

  public String getSdui() {
    return this.sdui;
  }

  public String getSab() {
    return this.sab;
  }

  public String getTty() {
    return this.tty;
  }

  public String getCode() {
    return this.code;
  }

  public String getStr() {
    return this.str;
  }

  public String getSrl() {
    return this.srl;
  }

  public String getSuppress() {
    return this.suppress;
  }

  public String getCvf() {
    return this.cvf;
  }

  
  
}
