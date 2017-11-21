package gov.nih.nlm.nls.tools.standoff;

/**
 * Normalization annotations
 *
 * Normalization annotations are supported as of v1.3. Each
 * normalization annotation has a unique ID and is defined by
 * reference to the ID of the annotation that the normalization
 * attaches to and a RID:EID pair identifying the external resource
 * (RID) and the entry within that resource (EID). Additionally, each
 * normalization annotation has the type Reference (no other values
 * for the type are currently defined) and a human-readable string
 * value for the entry referred to.
 *
 * The following example shows a normalization annotation attached to
 * the text-bound annotation "T1" (not shown) and associates it with
 * the Wikipedia entry with the Wikipedia ID "534366" ("Barack
 * Obama").
 *
 * <pre>
 * N1	Reference T1 Wikipedia:534366 Barack Obama
 * </pre>
 *
 * (Note that the association of the EID values such as "Wikipedia" or
 * "GO" with the relevant external resources is not represented in the
 * standoff but controlled by the tools.conf configuration file.)
 *
 * (As for text-bound annotations, the ID and the text are separated
 * by TAB characters, and other fields (here, "Reference", "T1" and
 * "Wikipedia:534366") by SPACE.
 *
 * See also, "brat standoff format" (http://brat.nlplab.org/standoff.html)
 *
 * Created: Thu Nov  9 09:25:15 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class NormalizationAnnotation implements Annotation {

  String id;
  String target;
  String rid;
  String eid;
  String text;

  /**
   * Creates a new <code>NormalizationAnnotation</code> instance.
   *
   */
  public NormalizationAnnotation(String id, String target, String rid, String eid, String text) {
    this.id = id.trim(); 
    this.target = target.trim();
    this.rid = rid.trim();
    this.eid = eid.trim();
    this.text = text.trim();
  }
  public boolean equals(Object obj) {
    return  (this.rid.equals(((NormalizationAnnotation)obj).rid) &&
	     this.eid.equals(((NormalizationAnnotation)obj).eid) &&
	     this.text.equals(((NormalizationAnnotation)obj).text));
  }
  public int hashCode() {
    return (this.rid + this.eid + this.text).hashCode();
  }
  public String getId() { return this.id; }
  public void setId(String id) { this.id = id; }
  public void setTarget(String target) { this.target = target; }
  public String getRid()  { return this.rid; }
  public String getEid()  { return this.eid; }
  public String getText() { return this.text; }
  public String toString() {
    return this.id + "\tReference " + this.target + " " + this.rid + ":" + this.eid + "\t" + this.text;
  }
}

