package View;

import Control.Controller;
import Control.Read;
import Control.ControlImage;
import Control.ControlTag;
import Model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@code Terminal} allows the user to navigate the program via console prompts. */
public class Terminal {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** Provides access to the rest of the program. */
  private static Controller control;

  /** Allows {@code Terminal} to manipulate {@code Image}. */
  private static ControlImage imageControl;

  /** Allows {@code Terminal} to manipulate {@code Tag}. */
  private static ControlTag tagControl;

  /** Currently selected {@code Tag}. */
  private static Tag selectedTag;

  /** Currently selected directory. */
  private static ImageManager selectedDir;

  /** Currently selected {@code Image}. */
  private static Image selectedImg;

  /** Determines whether the user wants to keep running the program; if {@code false} terminates. */
  private static boolean run = true;

  /**
   * Starts up the {@code Model}, will prompt user to enter a directory if this is first time start
   * up. Continues.
   *
   * @param args input.
   * @throws IOException when opening files.
   */
  public static void main(String[] args) throws IOException {
    Log.init();
    LOGGER.log(Level.CONFIG, "Started program in Terminal mode.");
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    showHelp();
    if (startUp()) {
      System.out.print("Enter path of directory you would like to view: ");
      readIn(monitorInput(input.readLine()));
    }
    while (run) {
      System.out.print(">>> ");
      monitorInput(input.readLine());
    }
  }

  /**
   * Creates a new {@code Controller}. If the {@code Controller} loaded a previously serialized
   * version return {@code false}, otherwise return {@code true}.
   *
   * @return {@code true} if the {@code Controller} is brand new, {@code false} if it loaded a
   *     previous version.
   */
  private static boolean startUp() {
    try {
      control = new Controller();
      imageControl = control.getImageControl();
      tagControl = control.getTagControl();
    } catch (ClassNotFoundException | IOException ex) {
      System.out.println("Ooops! Looks like we ran into an error:\n" + ex.getMessage());
    }
    return control.getImageManagers().isEmpty();
  }

  /** Displays the help menu in the console. */
  private static void showHelp() {
    String helpMenu = "Please be aware this terminal uses an auto-save feature.\n";
    helpMenu += "You may type any of the following commands at any time: \n";
    helpMenu += "    -h                 > displays this help menu.\n";
    helpMenu += "    -ld                > list all directories.\n";
    helpMenu += "    -lt                > list all current tags. \n";
    helpMenu += "    -dt                > completely remove a Tag.\n";
    helpMenu += "    -cd                > list the currently selected directory.\n";
    helpMenu += "    -ci                > list the currently selected image.\n";
    helpMenu += "    -n [path to dir]   > add a new directory to the Tagger.\n";
    helpMenu += "    -exit              > exit the program.\n";
    helpMenu += "    ===== Image Functions =====\n";
    helpMenu += "    -ilt               > list all Tags for this image.\n";
    helpMenu += "    -at [name of tag]  > Add a tag to the image.\n";
    helpMenu +=
        "    -rt [name of tag]  > Remove a tag from the image. Do not include @ in the name of the tag.\n";
    helpMenu += "    -mv [dest]         > move a file to destination.\n";
    helpMenu += "    -sh                > show the history of the image.\n";
    helpMenu += "    -of                > open the folder the image is stored in.\n";
    helpMenu += "    -vi                > view the image.\n";
    System.out.println(helpMenu);
  }

  /**
   * Checks whether a command instruction has been input, executes the instruction if true otherwise
   * passes along the input.
   *
   * @param input the {@code String} to monitor for command instructions.
   * @return the original input {@code String}, unaltered.
   */
  private static String monitorInput(String input) {
    if (input.matches("^-.+$")) {
      help(input);
    }
    return input;
  }

  /**
   * Executes the appropriate methods depending on which command instruction is input.
   *
   * @param input contains the instruction to execute.
   */
  private static void help(String input) {
    if (input.matches("^-h$")) {
      showHelp();
    } else if (input.matches("^-ld$")) {
      listDirectories();
    } else if (input.matches("^-lt$")) {
      listTags();
    } else if (input.matches("^-dt$")) {
      deleteTag();
    } else if (input.matches("^-st [0-9]+$")) {
      selectTag(input.split(" ")[1]);
    } else if (input.matches("^-cd$")) {
      currentDirectory();
    } else if (input.matches("^-ci$")) {
      currentImage();
    } else if (input.matches("^-n .*$")) {
      readIn(input.split(" ")[1]);
    } else if (input.matches("^-ilt$")) {
      showImageTags();
    } else if (input.matches("^-at .*$")) {
      addImageTag(input.split(" ")[1]);
    } else if (input.matches("^-rt .*$")) {
      removeImageTag(input.split(" ")[1]);
    } else if (input.matches("^-mv .*$")) {
      moveImage(input.split(" ")[1]);
    } else if (input.matches("^-sh$")) {
      showImageHistory();
    } else if (input.matches("^-of$")) {
      openImageFolder();
    } else if (input.matches("^-vi$")) {
      viewImage();
    } else if (input.matches("^-sd [0-9]+$")) {
      selectDirectory(input.split(" ")[1]);
    } else if (input.matches("^-si [0-9]+$")) {
      selectImage(input.split(" ")[1]);
    } else if (input.matches("^-ri [0-9]+$")) {
      revertImage(input.split(" ")[1]);
    } else if (input.matches("^-exit$")) {
      run = false;
    } else {
      System.out.println(input + " is not a valid command.");
    }
  }

  /** Displays a numbered list of all currently stored directories. */
  private static void listDirectories() {
    System.out.println("You have the following directories:");
    printList(control.getImageManagers());
    System.out.println("Use -sd # to select a directory.");
  }

  /** Displays all {@code Tag currently in this {@code Container. */
  private static void listTags() {
    System.out.println("You have the following tags:");
    printList(tagControl.getAllTags());
  }

  /**
   * Remove a {@code Tag} from {@code TagManager} and all {@code Image} which are tagged with the
   * {@code selectedTag}.
   */
  private static void deleteTag() {
    if (selectedTag == null) {
      System.out.println("Please select a Tag first using '-st #'.");
      listTags();
    } else {
      tagControl.deleteTag(selectedTag);
      selectedTag = null;
      System.out.println("Tag deleted.");
    }
  }

  /**
   * Selects a {@code Tag} provided a number corresponding to a previously output list of all tags.
   *
   * @param selected a number corresponding to the index + 1 of tags in {@code TagManager}.
   */
  private static void selectTag(String selected) {
    int numSelect = new Integer(selected);
    if (numSelect - 1 > tagControl.getAllTags().size() | numSelect < 1) {
      System.out.println("please enter a valid selection: ");
    } else {
      selectedTag = tagControl.getAllTags().get(numSelect - 1);
    }
    System.out.println("Selected: " + selectedTag.toString());
  }

  /** Displays the name of the current directory. */
  private static void currentDirectory() {
    if (selectedDir != null) {
      System.out.println(selectedDir.toString());
    } else {
      System.out.println("Please select a directory first.");
    }
  }

  /** Displays the name of {@code selectedImg}. */
  private static void currentImage() {
    if (imageSelected()) {
      System.out.println((selectedImg.toString()));
    }
  }

  /**
   * Traverses and processes the specified directory.
   *
   * @param path the directory to traverse.
   */
  private static void readIn(String path) {
    Read.traverse(new File(path).toPath(), control.getContainer());
    save();
  }

  /** Displays all of the tags associated with {@code selectedImg}. */
  private static void showImageTags() {
    if (imageSelected()) {
      System.out.println(selectedImg.toString() + " has the following tags:");
      printList(selectedImg.getCurrentTags());
    }
  }

  /**
   * Adds a desired {@code Tag} to {@code selectedImg}.
   *
   * @param tag name of {@code Tag} to add, not including the @.
   */
  private static void addImageTag(String tag) {
    if (imageSelected()) {
      try {
        tagControl.addTag(tag, selectedImg);
      } catch (TagNamingException ex) {
        System.out.println("Ooops! Looks like we ran into an error:\n" + ex.getMessage());
      }
    }
    save();
  }

  /**
   * Removes {@code Tag} from {@code selectedImg}.
   *
   * @param tag to be removed as a {@code String}, not including the @.
   */
  private static void removeImageTag(String tag) {
    if (imageSelected()) {
      tagControl.untag(tag, selectedImg);
    }
    save();
  }

  /**
   * Moves {@code selectedImg} to the desired new location.
   *
   * @param newDir directory to move {@code Image} to.
   */
  private static void moveImage(String newDir) {
    if (imageSelected()) {
      try {
        imageControl.move(selectedImg, new File(newDir).toPath());
      } catch (InvalidDirectoryException | IOException e) {
        System.out.println("There's been an error, please try again.\n" + e.getMessage());
      }
    }
    save();
  }

  /** Displays the log of changes done to {@code selectedImg}. */
  private static void showImageHistory() {
    if (imageSelected()) {
      printList(imageControl.getLog(selectedImg));
    }
    System.out.println("Use '-ri #' to revert the image to a previous version.");
  }

  /**
   * Reverts the {@code selectedImg} back to a previous state.
   *
   * @param selected the state to revert to, is equal to the index + 1.
   */
  private static void revertImage(String selected) {
    if (imageSelected()) {
      int numSelect = new Integer(selected);
      if (numSelect - 1 > imageControl.getLifeTimeTagsLength(selectedImg) | numSelect < 1) {
        System.out.println("Please enter a valid selection :");
        save();
      } else {
        imageControl.changeToOldVersion(selectedImg, numSelect - 1);
      }
    }
  }

  /** Opens the folder containing {@code selectedImg}. */
  private static void openImageFolder() {
    if (imageSelected()) {
      try {
        imageControl.openImageFolder(selectedImg);
      } catch (IOException ex) {
        System.out.println("Ooops! Looks like we ran into an error:\n" + ex.getMessage());
      }
    }
  }

  /** Open and view the {@code selectedImg}. */
  private static void viewImage() {
    if (imageSelected()) {
      try {
        imageControl.openImage(selectedImg);
      } catch (IOException ex) {
        System.out.println("Ooops! Looks like we ran into an error:\n" + ex.getMessage());
      }
    }
  }

  /**
   * Selects a {@code ImageManager} provided a number corresponding to a previously output list of
   * all directories.
   *
   * @param selected a number corresponding to the index + 1 of containers {@code ImageManagers}.
   */
  private static void selectDirectory(String selected) {
    int numSelect = new Integer(selected);
    if (numSelect - 1 > control.getContainer().getImageManagers().size() | numSelect < 1) {
      System.out.println("Please enter a valid selection: ");
    } else {
      selectedDir = control.getImageManagers().get(numSelect - 1);
    }
    System.out.println("Selected directory: " + selectedDir.toString());
    listImages();
  }

  /**
   * Selects a {@code Image} provided a number corresponding to a previously output list of all
   * images in the directory.
   *
   * @param selected a number corresponding to the index + 1 of Image in {@code ImageManager}.
   */
  private static void selectImage(String selected) {
    int numSelect = new Integer(selected);
    if (numSelect - 1 > selectedDir.getImages().size() | numSelect < 1) {
      System.out.println("please enter a valid selection: ");
    } else {
      selectedImg = selectedDir.getImages().get(numSelect - 1);
    }
    System.out.println("Selected: " + selectedImg.toString());
  }

  /** Displays a numbered list of all images in the directory which was selected. */
  private static void listImages() {
    if (selectedDir == null) {
      System.out.println("Please select a directory first.");
    } else {
      System.out.println("You have the following images:");
      printList(selectedDir.getImages());
      System.out.println("Use -si # to select an image.");
    }
  }

  /**
   * Prints out each entry in a given list as a separate row. Each row is numbered starting at 1.
   *
   * @param list the list to be numbered and displayed.
   */
  private static void printList(List list) {
    int count = 1;
    for (Object obj : list) {
      System.out.println(count + " - " + obj.toString());
      count++;
    }
  }

  /**
   * Checks whether an {@code Image} has been selected.
   *
   * @return {@code true} if an {@code Image} has been selected, {@code false} otherwise.
   */
  private static boolean imageSelected() {
    if (selectedImg == null) {
      System.out.println("Please select an Image first.");
      return false;
    }
    return true;
  }

  /** Saves any changes made and catches any exceptions. */
  private static void save() {
    try {
      control.save();
    } catch (IOException ex) {
      System.out.println(
          "Ooops! Looks like we ran into an error while saving:\n" + ex.getMessage());
    }
  }
}
