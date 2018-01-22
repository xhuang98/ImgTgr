package Control;

import Model.*;
import Model.Image;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@code ControlImage} serves as a {@code Controller} for {@code Image}. */
public class ControlImage implements Serializable {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** Main {@code Controller} to be linked to. */
  private Controller control;

  /**
   * Creates new {@code ControlImage} and associates to a {@code Controller}.
   *
   * @param control {@code Controller} to be associated to.
   */
  public ControlImage(Controller control) {
    this.control = control;
  }

  /**
   * Retrieves the name of given {@code Image}.
   *
   * @param image whose name is retrieved.
   * @return name of {@code Image}.
   */
  public String getImageName(Image image) {
    return image.getName();
  }

  /**
   * Retrieves the number of tags an {@code Image} has had over its life time.
   *
   * @param image to check history of.
   * @return number of {@code Tag} {@code Image} has had.
   */
  public int getLifeTimeTagsLength(Image image) {
    return image.getLifeTimeTags().size();
  }

  /**
   * Moves an {@code Image} to a new directory. Will move an {@code Image} between {@code
   * ImageManagers} and then adjust its file path to reflect this change.
   *
   * @param image to move.
   * @param newDirectoryPath {@code Path} to the new directory.
   * @throws InvalidDirectoryException when the directory does not exist.
   * @throws IOException if the file cannot be moved.
   */
  public void move(Image image, Path newDirectoryPath)
      throws InvalidDirectoryException, IOException {
    File directoryFile = new File(newDirectoryPath.toString());
    if (!directoryFile.isDirectory()) {
      LOGGER.log(Level.WARNING, "Tried to move " + image.getName() + " to invalid directory.");
      throw new InvalidDirectoryException("This is not a valid directory!");
    }

    Path oldPath = image.getDirectory().resolve(image.toString());
    image.setDirectory(newDirectoryPath.toString());

    ImageManager oldManager = image.getManager();
    ImageManager newManager = imExists(newDirectoryPath);
    newManager.addImage(image);
    image.setManager(newManager);
    oldManager.removeImage(image);
    File fileToMoveTo =
        new File(newDirectoryPath + FileSystems.getDefault().getSeparator() + image.toString());
    image.setImageFile(fileToMoveTo);
    Files.move(oldPath, newDirectoryPath.resolve(oldPath.getFileName()));
    updateImage(image);
    LOGGER.log(
        Level.CONFIG,
        "Moved "
            + image.getName()
            + " from "
            + oldManager.toString()
            + " to "
            + newManager.toString());
  }

  /**
   * Determines whether the new directory path has an {@code ImageManager} associated with it. If it
   * does not, creates and returns a new {@code ImageManager}.
   *
   * @param path {@code Path} to validate.
   * @return New or existing {@code ImageManager}.
   */
  private ImageManager imExists(Path path) {
    for (ImageManager manager : control.getContainer().getImageManagers()) {
      if (path.equals(manager.getDirectory())) {
        return manager;
      }
    }
    return new ImageManager(path.toFile());
  }

  /**
   * Retrieves the log for an {@code Image}.
   *
   * @param image whose log is retrieved.
   * @return History of all changes done to {@code Image}.
   */
  public ArrayList<String> getLog(Image image) {
    if (image == null) {
      return null;
    }
    return image.getLog();
  }

  /**
   * Retrieves all name changes that have been done on an {@code Image}.
   *
   * @param image whose history is retrieved.
   * @return History of all the names {@code Image} has had.
   */
  public ArrayList<String> getNameHistory(Image image) {
    return image.getNameHistory();
  }

  /**
   * Changes the file name of {@code Image} according to its {@code Tag(s)}.
   *
   * @param image whose file name is changed.
   */
  protected void changeFileName(Image image) {
    image.renameFile();
    File fileToMoveTo =
        new File(
            image.getDirectory().toString()
                + FileSystems.getDefault().getSeparator()
                + image.toString());
    image.setImageFile(fileToMoveTo);
  }

  /**
   * Resets an {@code Image} to a previous version of itself.
   *
   * @param image to reset.
   * @param index Index of the state in {@code lifeTimeTags} that this {@code Image} should be reset
   *     to.
   */
  public void changeToOldVersion(Image image, int index) {
    image.resetTags(index);
    updateImage(image);
  }

  /**
   * Opens the folder an {@code Image} is stored in, in a system specific file finder.
   *
   * @param img {@code Image} whose folder we want to open.
   * @throws IOException if folder does not exist.
   */
  public void openImageFolder(Image img) throws IOException {
    if (Desktop.isDesktopSupported()) {
      DesktopApi.open(img.getDirectory().toFile());
      LOGGER.log(Level.CONFIG, "Opening " + img.getName() + " in folder.");
    }
  }

  /**
   * Opens an {@code Image} file, in a system specific program.
   *
   * @param img {@code Image} we want to open.
   */
  public void openImage(Image img) throws IOException {
    if (Desktop.isDesktopSupported()) {
      DesktopApi.open(img.getImageFile());
      LOGGER.log(Level.CONFIG, "Opening " + img.getName() + " to view.");
    }
  }

  /**
   * Returns the File of {@code Image}.
   *
   * @param image Desired {@code Image}.
   * @return {@code Image} as {@code File}.
   */
  public File getImageFile(Image image) {
    return image.getImageFile();
  }

  /**
   * Adds {@code Image} to {@code changedImages} list.
   *
   * @param image whose changes need to be saved.
   */
  protected void updateImage(Image image) {
    ArrayList<Image> changedImages = control.getChangedImages();
    if (!changedImages.contains(image)) {
      changedImages.add(image);
    }
  }

  /**
   * Removes all {@code Tag(s)} from given {@code Image}.
   *
   * @param image whose {@code Tag(s)} are removed.
   */
  public void removeAllTags(Image image) {
    image.removeAllTags();
    updateImage(image);
  }
}
