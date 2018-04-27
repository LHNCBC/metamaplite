package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;

/**
 * Describe class TokenListMatch here.
 *
 *
 * Created: Mon Feb  4 14:12:16 2013
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TokenListMatch {

  /**
   * Return true if the tokenlists are the same length and tokens in
   * the two tokenlists equal.
   * @param tokenlist0 first tokenlist
   * @param tokenlist1 second tokenlist
   * @param compCase compare string including case.
   * @return true if tokenlist match.
   */
  public static boolean tokenMatch( List<Token> tokenlist0,  List<Token> tokenlist1,
				    boolean compCase )
  {
    if (tokenlist0.size() != tokenlist1.size()) { return false; }
    boolean status = true;
    for (int i = 0; i < tokenlist0.size(); i++) {
      if (compCase) {
	if (! tokenlist0.get(i).getText().equals
	    (tokenlist1.get(i).getText())) {
	  status = false;
	}
      } else {
	if (! tokenlist0.get(i).getText().toLowerCase().equals
	    (tokenlist1.get(i).getText().toLowerCase())) {
	  status = false;
	}
      }
    }
    return status;
  }

  public static boolean tokenMatch( String[] tokenlist0,  String[] tokenlist1,
				    boolean compUpperCase )
  {
    if (tokenlist0.length != tokenlist1.length) { return false; }
    boolean status = true;
    for (int i = 0; i < tokenlist0.length; i++) {
      if (compUpperCase) {
	if (! tokenlist0[i].equals(tokenlist1[i])) {
	  status = false;
	}
      } else {
	if (! tokenlist0[i].toLowerCase().equals(tokenlist1[i].toLowerCase())) {
	  status = false;
	}
      }
    }
    return status;
  }
}

