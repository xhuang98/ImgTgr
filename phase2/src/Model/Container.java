package Model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Container} stores data for this project. {@code Container} follows a singleton design
 * pattern, so only one instance of it exists in the application.
 */
public class Container implements Serializable {

  /** Contains all instances of ImageManager, each represents a separate directory. */
  private List<ImageManager> imageManagers = new ArrayList<>();

  /** Universal {@code TagManager}. */
  private TagManager tagManager = new TagManager();

  /** Keeps track of the last Folder that the user chose. */
  private File directory;

  /** Creates new, empty {@code Container}. */
  public Container() {
  }

  /**
   * Adds a new {@code ImageManager} to the collections of {@code ImageManager}.
   *
   * @param im {@code ImageManager} representing a directory.
   */
  public void addImageManager(ImageManager im) {
    imageManagers.add(im);
  }

  /**
   * Gets the entire list of {@code ImageManager(s)}.
   *
   * @return list of {@code ImageManager(s)}.
   */
  public List<ImageManager> getImageManagers() {
    return imageManagers;
  }

  /**
   * Gets the {@code Container('s)} {@code TagManager}.
   *
   * @return single {@code TagManager}.
   */
  public TagManager getTagManager() {
    return tagManager;
  }

  /**
   * Gets the last directory that the user chose. If the user hadn't chosen a directory before, then
   * returns null.
   *
   * @return Last chosen directory or null.
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Sets the last directory that the user chose.
   *
   * @param folder A directory.
   */
  public void setDirectory(File folder) {
    directory = folder;
  }
}
