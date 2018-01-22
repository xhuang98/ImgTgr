package Model;

/**
 * {@code InvalidDirectoryException} is an Exception to be thrown when a specified directory isn't valid.
 */
public class InvalidDirectoryException extends Exception {
  /**
   * Exception thrown when Directory isn't valid.
   *
   * @param message Message to be returned.
   */
  public InvalidDirectoryException(String message) {
    super(message);
  }
}
