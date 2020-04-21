package gov.nih.nlm.nls.metamap.lite.dictionary;

/**
 * Describe interface VariantLookup here.
 *
 *
 * Created: Wed Mar 22 16:37:39 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface VariantLookup {
  int lookupVariant(String term, String word);
  int lookupVariant(String term);
}
