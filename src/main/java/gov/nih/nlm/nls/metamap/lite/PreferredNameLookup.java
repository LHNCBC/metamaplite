package gov.nih.nlm.nls.metamap.lite;

/**
 * Describe interface PreferredNameLookup here.
 *
 *
 * Created: Tue Jun 11 12:28:27 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface PreferredNameLookup {
  String getPreferredName(String cui);
}
