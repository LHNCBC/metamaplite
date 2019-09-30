package gov.nih.nlm.nls.metamap.lite;

/**
 * MMLDictionaryLookup  - Interface for implementations of DictionaryLookup with
 * instantiation, validation, and other operationss for MetaMapLite.
 *
 * Created: Tue Jun 11 12:29:37 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface MMLDictionaryLookup<T>
  extends DictionaryLookup<T>, MMLInstantiate, MMLValidate, PreferredNameLookup, VariantLookup
{
}
