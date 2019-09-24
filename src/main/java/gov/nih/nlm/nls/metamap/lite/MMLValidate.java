package gov.nih.nlm.nls.metamap.lite;

/**
 *  MMLValidate - interface for validating file-based resources used
 *  by class instance.
 *
 * Created: Tue Jun 11 13:11:57 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public interface MMLValidate {
  /**
   * Return true if supplied directory contains valid dictionary
   * lookup implementation.
   * @param directoryPath path of directory containing implementation
   * @return true if valid implementation, false otherwise.
   */
  boolean verifyImplementation(String directoryPath);

}
