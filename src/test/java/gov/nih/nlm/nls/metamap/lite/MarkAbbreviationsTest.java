package gov.nih.nlm.nls.metamap.lite;

import org.junit.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Describe class MarkAbbreviationsTest here.
 *
 *
 * Created: Tue Jan 31 09:22:43 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MarkAbbreviationsTest extends TestCase {

  public MarkAbbreviationsTest() {

  }

  /** 
   * Test code with presence of short form followed by short form in parenthesis.
   * <p>
   * Example:
   * <pre>
   *   Heart Rate (HR)
   * </pre>
   */
  @Test
  public void testLongFormShortForm()
  {
    /* initialize input strings */
    /* initialize found entities */
    /* initialize entities expected after processing */
  }

  /** 
   * Test code with presence of long form followed by short form in parenthesis.
   * <p>
   * Example:
   * <pre>
   *   HR (Heart Rate)
   * </pre>
   */
  @Test
  public void testShortFormLongForm()
  {
    /* initialize input strings */
    /* initialize found entities */
    /* initialize entities expected after processing */
  }

}

