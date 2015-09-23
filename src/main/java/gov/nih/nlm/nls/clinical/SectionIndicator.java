
//
package gov.nih.nlm.nls.clinical;

/**
 *
 */

public class SectionIndicator {
  String docType;
  String newTag;
  String indicatorString;
  String notes;

  public SectionIndicator(String recordString) {
    String[] fields = recordString.split("\\,");
    if (fields.length > 3) {
      this.docType         = fields[0];
      this.newTag          = fields[1];
      this.indicatorString = fields[2];
      this.notes           = fields[3].trim();
    }
  }

  public SectionIndicator(String[] fields) {
    if (fields.length > 3) {
      this.docType         = fields[0];
      this.newTag          = fields[1];
      this.indicatorString = fields[2];
      this.notes           = fields[3].trim();
    }
  }

  public String getDocType() { return this.docType; }
  public String getNewTag() { return this.newTag; }
  public String getIndicatorString() { return this.indicatorString; }
  public String getNotes() { return this.notes; }

}
