package Model;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Creates a {@code Log} of all activity occurred during the current session and records it in
 * History.log.
 */
public class Log {

  /** Contains the {@code Log} to write to. */
  private static FileHandler fileHandler = null;

  /** Initialize the the {@code Log} file. */
  public static void init() {
    try {
      fileHandler = new FileHandler("History.log", false);
    } catch (IOException ex) {
      ex.getMessage();
    }
    Logger log = Logger.getLogger("");
    fileHandler.setFormatter(new SimpleFormatter());
    log.addHandler(fileHandler);
    log.setLevel(Level.CONFIG);
  }
}
