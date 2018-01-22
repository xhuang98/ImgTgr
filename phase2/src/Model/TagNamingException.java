package Model;

/** TagNamingException is an exception to be thrown when a tag isn't properly named. */
public class TagNamingException extends Exception {

  /**
   * Exception called when Tag isn't properly named (i.e. it contains @).
   *
   * @param message Message to be returned when exception is caught.
   */
  public TagNamingException(String message) {
    super(message);
  }
}
