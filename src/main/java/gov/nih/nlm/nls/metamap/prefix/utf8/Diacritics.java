package gov.nih.nlm.nls.metamap.prefix.utf8;

import java.util.Map;
import java.util.HashMap;

/**
 * Maps for converting UTF-8  Diacritic characters to expanded equivalents in ASCII.
 *
 * Created: Tue Nov 26 09:30:24 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Diacritics {
  static Map<String,String> stringToStringMap = new HashMap<String,String>();
  // encodings are actually in UTF-16, Java's native internal character encoding format
  /**
   * <ul>
   *   <li> a -&gt; a</li>
   *   <li> &amp;#771; -&gt; &#771;</li>
   *   <li> a&amp;#771; -&gt; a&#771;</li>
   * </ul>
   * Note character encodings in example are in decimal, encoding in source are hexadecimal
   */
  static {

    stringToStringMap.put("a\u0300","a"); // a grave accent
    stringToStringMap.put("a\u0301","a"); // a acute accent
    stringToStringMap.put("a\u0302","a"); // a circumflex accent
    stringToStringMap.put("a\u0303","a"); // a tilde
    stringToStringMap.put("a\u0304","a"); // a macron
    stringToStringMap.put("a\u0305","a"); // a overline
    stringToStringMap.put("a\u0306","a"); // a breve
    stringToStringMap.put("a\u0307","a"); // a dot above
    stringToStringMap.put("a\u0308","a"); // a 
    stringToStringMap.put("a\u0309","a"); // a 
    stringToStringMap.put("a\u030A","a"); // a 
    stringToStringMap.put("a\u030B","a"); // a 
    stringToStringMap.put("a\u030C","a"); // a caron
    stringToStringMap.put("a\u0301","a"); // a tilde
    stringToStringMap.put("a\u0301","a"); // a tilde
    stringToStringMap.put("\u0198","AE"); // 
    stringToStringMap.put("\u0339","oe"); // 
    stringToStringMap.put("\u0339","oe"); // 
    stringToStringMap.put("\uFB00","fi"); // 
    stringToStringMap.put("\uFB01","fi"); // 
    stringToStringMap.put("\uFB02","fl"); // 
    stringToStringMap.put("\uFB03","ffi"); //
    stringToStringMap.put("\uFB04" ,"ffl"); //

  }

  /**
   * Creates a new <code>Diacritics</code> instance.
   *
   */
  public Diacritics() {

  }

}
