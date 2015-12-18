package irutils;

/**
 * BSPIndexInvalidException.java
 *
 *
 * Invalidd: Fri Jul 20 10:51:39 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: BSPIndexInvalidException.java,v 1.2 2001/09/07 13:32:20 wrogers Exp $
 */

public class BSPIndexInvalidException extends Exception {
  /**
   * Instantiate new index validation exception.
   */
  public BSPIndexInvalidException () {
    super("BSPIndexInvalidException");
  }
  /**
   * Instantiate new index validation exception.
   * @param message additional information about exception.
   */
  public BSPIndexInvalidException (String message) {
    super(message);
  }
}// BSPIndexInvalidException
