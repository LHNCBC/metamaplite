package gov.nih.nlm.nls.tools.standoff;

/**
 * Relation annotations
 *
 * Binary relations have a unique ID and are defined by their type (e.g. Origin, Part-of) and their arguments.
 *
 * <pre>
 * R1	Origin Arg1:T3 Arg2:T4
 * </pre>
 *
 * The format is similar to that applied for events, with the
 * exception that the annotation does not identify a specific piece of
 * text expressing the relation ("trigger"): the ID is separated by a
 * TAB character, and the relation type and arguments by SPACE.
 *
 * Relation arguments are commonly identified simply as Arg1 and Arg2,
 * but the system can be configured to use any labels (e.g. Anaphor
 * and Antecedent) in the standoff representation.
 *
 * See also, "brat standoff format" (http://brat.nlplab.org/standoff.html)
 *
 * Created: Thu Nov 9 09:35:23 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class RelationAnnotation {
  String id;
  String type;
  String arg1;
  String arg2;

  public RelationAnnotation(String id, String type, String arg1, String arg2) {
    this.id = id;
    this.type = type;
    this.arg1 = arg1;
    this.arg2 = arg2;
  }
  public String getId() { return this.id; }
  public void setId(String id) { this.id = id; }
  public void setArg1(String target) { this.arg1 = target; }
  public void setArg2(String target) { this.arg2 = target; }
}
