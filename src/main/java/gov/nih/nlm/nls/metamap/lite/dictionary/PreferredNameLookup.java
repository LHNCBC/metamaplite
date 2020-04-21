package gov.nih.nlm.nls.metamap.lite.dictionary;

/**
 * Interface PreferredNameLookup - must be implemented by classes that
 * support lookup of preferred names in knowledge source dictionaries.
 *
 * Created: Tue Jun 11 12:28:27 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface PreferredNameLookup {
  String getPreferredName(String cui);
}
