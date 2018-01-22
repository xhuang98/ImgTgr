package View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * {@code Run} starts the program. Allows the user to choose whether they would like to use the
 * terminal or GUI.
 */
public class Run {

  /** Determines whether the main method will continue to run. */
  private static boolean run = true;

  /**
   * Depending on user input will start the program via GUI or terminal.
   *
   * @param args input arguments.
   * @throws IOException throws IOException if the given file does not exist or is moved.
   */
  public static void main(String[] args) throws IOException {
    System.out.println("Use '-exit' to exit at any time.");
    System.out.print("Would you like to use the GUI? Y/N: ");
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    String in = "";
    while (run) {
      in = input.readLine();
      if (in.matches("^\\s*Y\\s*$")) {
        run = false;
        Interface.main(args);
      } else if (in.matches("^\\s*N\\s*$")) {
        run = false;
        Terminal.main(args);
      } else if (in.matches("^\\s*-exit\\s*$")) {
        run = false;
      } else {
        System.out.print("Please enter a valid input.\n>>> ");
      }
    }
  }
}
