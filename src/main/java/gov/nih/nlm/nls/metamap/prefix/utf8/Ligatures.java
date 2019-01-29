package gov.nih.nlm.nls.metamap.prefix.utf8;

import java.util.Map;
import java.util.HashMap;

/**
 * Maps for converting UTF-8  Ligature characters to expanded equivalents in ASCII.
 *
 * Created: Tue Jan  8 09:10:46 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Ligatures {

  static Map<Character,String> characterToStringMap = new HashMap<Character,String>();
  // encodings are actually in UTF-16, Java's native internal character encoding format
  static {

    characterToStringMap.put('\u0152',"OE"); // LATIN CAPITAL LIGATURE OE
    characterToStringMap.put('\u0198',"AE"); // LATIN CAPITAL LIGATURE AE
    characterToStringMap.put('\u0339',"oe"); // LATIN SMALLAPITAL LIGATURE OE
    characterToStringMap.put('\u0339',"oe"); // LATIN SMALLAPITAL LIGATURE OE
    characterToStringMap.put('\uFB00',"fi"); // LATIN SMALL LIGATURE FF
    characterToStringMap.put('\uFB01',"fi"); // LATIN SMALL LIGATURE FI
    characterToStringMap.put('\uFB02',"fl"); // LATIN SMALL LIGATURE FL
    characterToStringMap.put('\uFB03',"ffi"); // LATIN SMALL LIGATURE FFI
    characterToStringMap.put('\uFB04',"ffl"); // LATIN SMALL LIGATURE FFL

  }
  /**
   * Creates a new <code>Ligatures</code> instance.
   *
   */
  public Ligatures() {

  }

}
