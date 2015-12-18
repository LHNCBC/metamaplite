package irutils;

/**
 * BSPIndexCreateException.java
 *
 *
 * Created: Fri Jul 20 10:51:39 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: BSPIndexCreateException.java,v 1.4 2001/09/07 13:32:20 wrogers Exp $
 */

public class BSPIndexCreateException extends Exception {
  /**
   * Instantiate new index creation exception.
   */
  public BSPIndexCreateException () {
    super("BSPIndexCreateException");
  }
  /**
   * Instantiate new index creation exception.
   * @param message additional information about exception.
   */
  public BSPIndexCreateException (String message) {
    super(message);
  }
}// BSPIndexCreateException
