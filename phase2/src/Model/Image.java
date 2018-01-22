package Model;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code Image} represents an image in the directory. It retains the file path to the image,
 * current {@code Tag(s)}, and previous {@code Tag(s)}.
 */
public class Image implements Serializable {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** Name of the {@code Image}. Does not include {@code Tag(s)}. */
  private String name;

  /** Path to directory in which {@code Image} is stored. */
  private String directory;

  /** The type of extension {@code Image} uses: .gif, .jpg, .tiff, .png, .jpeg. */
  private String extension;

  /** The {@code Tag(s)} currently assigned to {@code Image}. */
  private ArrayList<Tag> currentTags = new ArrayList<>();

  /**
   * History of {@code Tag(s)} assigned to {@code Image} over its lifetime. First String in the Pair
   * is the time of name change, second String is the new name.
   */
  private ArrayList<Pair<String, ArrayList<Tag>>> lifeTimeTags = new ArrayList<>();

  /** {@code Image('s)} file; only modified when method renameFile is called. */
  private File imageFile;

  /** {@code ImageManager} where this {@code Image} will be stored. */
  private ImageManager manager;

  /**
   * Creates a new {@code Image} from a File object, breaking the file name into its name and
   * extension, and storing the Path to the directory in which the image resides.
   *
   * @param image File to be stored as {@code Image}.
   */
  public Image(File image) {
    breakName(image);
    Path filePath = image.getAbsoluteFile().toPath();
    directory = filePath.getParent().toString();
    updateLifeTimeTags();
    imageFile = image;
  }

  /**
   * Creates a new {@code Image} from a File object, breaking the file name into its name and
   * extension, and storing the Path to the directory in which the image resides. Adds on additional
   * {@code Tag(s)} found in existingTags.
   *
   * @param image File to be stored as {@code Image}.
   * @param existingTags {@code Tag(s)} to be added to {@code Image}.
   */
  public Image(File image, ArrayList<Tag> existingTags) {
    breakName(image);
    Path filePath = image.getAbsoluteFile().toPath();
    directory = filePath.getParent().toString();
    for (Tag tag : existingTags) {
      if (!currentTags.contains(tag)) {
        currentTags.add(tag);
        tag.tagImage(this);
      }
    }
    updateLifeTimeTags();
    imageFile = image;
  }

  /**
   * Separates the name of File into its name and its extension.
   *
   * @param image File with name.
   */
  private void breakName(File image) {
    String[] parts = image.getName().split("\\.");
    name = parts[0].split("\\s@")[0];
    extension = parts[1];
  }

  /**
   * Assigns a new {@code Tag} to this {@code Image} if it is not already assigned. Returns a
   * boolean indicating whether the {@code Tag} is added.
   *
   * @param newTag {@code Tag} to assign to {@code Image}.
   * @return {@code Tag} if {@code Tag} was new, null otherwise.
   */
  public Tag addTag(Tag newTag) {
    if (!currentTags.contains(newTag)) {
      currentTags.add(newTag);
      newTag.tagImage(this);
      updateLifeTimeTags();
      LOGGER.log(Level.CONFIG, "Added " + newTag.toString() + " to " + this.name);
      return newTag;
    }
    return null;
  }

  /**
   * Removes {@code Tag} from currentTags of {@code Image}. {@code Tag} is retained in lifeTimeTags.
   * Log of {@code Image} is updated if specified.
   *
   * @param rmTag {@code Tag} to remove from {@code Image}.
   * @param updateLog Whether log of {@code Image} should be updated.
   */
  public void removeTag(Tag rmTag, boolean updateLog, boolean updateTaggedImages) {
    if (currentTags.contains(rmTag)) {
      currentTags.remove(rmTag);
      if (updateTaggedImages) {
        rmTag.untagImage(this);
      }
      if (updateLog) {
        updateLifeTimeTags();
      }
      LOGGER.log(Level.CONFIG, "Removed " + rmTag.toString() + " from " + this.name);
    }
  }

  /**
   * Removes {@code Tag} from currentTags of {@code Image}. {@code Tag} is retained in lifeTimeTags.
   * Log is updated by default.
   *
   * @param rmTag {@code Tag} to be removed.
   */
  public void removeTag(Tag rmTag) {
    removeTag(rmTag, true, true);
  }

  /** Removes all {@code Tag(s)} from this {@code Image}. Updates log only once. */
  public void removeAllTags() {
    for (Tag tag : currentTags) {
      tag.untagImage(this);
    }
    currentTags.clear();
    updateLifeTimeTags();
  }

  /** Renames file according to updated name. */
  public void renameFile() {
    Path source = imageFile.toPath();
    try {
      Files.move(source, source.resolveSibling(this.toString()));
    } catch (Exception ex) {
      //TODO: log exception?
    }
  }

  /** Updates the log lifeTimeTags with current time, date and {@code Tag(s)}. */
  private void updateLifeTimeTags() {
    Pair<String, ArrayList<Tag>> logToAdd =
        new Pair<>(LocalDateTime.now().toString(), new ArrayList<>(currentTags));
    lifeTimeTags.add(logToAdd);
  }

  /**
   * Resets {@code Tag(s)} of {@code Image} to those at given index in the log.
   *
   * @param log Index of {@code Tag} list to revert to in lifeTimeTags.
   */
  public void resetTags(int log) {
    ArrayList<Tag> newCurrentTags = new ArrayList<>();
    for (Object tag : lifeTimeTags.get(log).getValue()) {
      if (tag instanceof Tag) {
        newCurrentTags.add((Tag) tag);
      }
    }
    currentTags = newCurrentTags;
    updateLifeTimeTags();
  }

  /**
   * Sets {@code Image('s)} {@code ImageManager}. Used when moving {@code Image}.
   *
   * @param manager new {@code ImageManager} for this {@code Image}.
   */
  public void setManager(ImageManager manager) {
    this.manager = manager;
  }

  /**
   * Sets {@code Image('s)} file. Used when {@code Image} is moved or renamed.
   *
   * @param imageFile File to be set to.
   */
  public void setImageFile(File imageFile) {
    this.imageFile = imageFile;
  }

  /**
   * Set the directory of this {@code Image}. Used when moving the {@code Image}.
   *
   * @param directory new directory the {@code Image} belongs to.
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Retrieve's history of names that the {@code Image} has previously had.
   *
   * @return List of previous names
   */
  public ArrayList<String> getNameHistory() {
    ArrayList<String> nameHistory = new ArrayList<>();
    for (Pair<String, ArrayList<Tag>> entry : lifeTimeTags) {
      nameHistory.add(entry.getKey() + ":   " + getNameFromTags(entry.getValue()));
    }
    return nameHistory;
  }

  /**
   * Retrieves the name of an {@code Image} with a given set of {@code Tag(s)}.
   *
   * @param tags List of {@code Tag(s)} to be associated with image's name.
   * @return Name of image with given {@code Tag(s)}.
   */
  private String getNameFromTags(ArrayList<Tag> tags) {
    StringBuilder newName = new StringBuilder().append(name);
    for (Tag tag : tags) {
      newName.append(" ");
      newName.append(tag.toString());
    }
    return newName.toString() + "." + extension;
  }

  /**
   * Retrieves the log as a list of name changes with an associated time.
   *
   * @return Log of {@code Image('s)} name changes.
   */
  public ArrayList<String> getLog() {
    ArrayList<String> list = new ArrayList<>();
    int previousIndex = 0;
    int nextIndex = 1;
    while (nextIndex < lifeTimeTags.size()) {
      String oldName = getNameFromTags(lifeTimeTags.get(previousIndex).getValue());
      String time = lifeTimeTags.get(nextIndex).getKey();
      String newName = getNameFromTags(lifeTimeTags.get(nextIndex).getValue());
      list.add(time + ":   " + oldName + " -> " + newName);
      previousIndex++;
      nextIndex++;
    }
    return list;
  }

  /**
   * Retrieves {@code Image('s)} name.
   *
   * @return {@code Image('s)} name.
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves {@code Image('s)} {@code ImageManager}.
   *
   * @return {@code Image('s)} {@code ImageManager}.
   */
  public ImageManager getManager() {
    return manager;
  }

  /**
   * Gets all {@code Tag(s)} ever assigned to this {@code Image} during its lifetime.
   *
   * @return list of all {@code Tag(s)} ever assigned.
   */
  public ArrayList<Pair<String, ArrayList<Tag>>> getLifeTimeTags() {
    return lifeTimeTags;
  }

  /**
   * Gets the directory in which this {@code Image} is stored.
   *
   * @return Path to this {@code Image}.
   */
  public Path getDirectory() {
    return Paths.get(directory);
  }

  /**
   * Gets all {@code Tag} currently associated with this {@code Image}.
   *
   * @return list of current {@code Tag(s)}.
   */
  public ArrayList<Tag> getCurrentTags() {
    return currentTags;
  }

  /**
   * Returns the File of the {@code Image}.
   *
   * @return {@code Image('s)} File.
   */
  public File getImageFile() {
    return imageFile;
  }

  /**
   * Constructs this {@code Image} display name. The display name starts with name, followed by all
   * {@code Tag(s)} from currentTags, and ends with extension.
   *
   * @return name + {@code Tag(s)} from currentTags + extension.
   */
  @Override
  public String toString() {
    StringBuilder builtName = new StringBuilder();
    builtName.append(name);
    for (Tag tag : currentTags) {
      builtName.append(" ");
      builtName.append(tag.toString());
    }
    builtName.append(".");
    builtName.append(extension);
    return builtName.toString();
  }

  /**
   * Returns true if Object that is an {@code Image} and has the same name and directory as this
   * {@code Image}. Otherwise returns false.
   *
   * @param that Object to be compared to.
   */
  @Override
  public boolean equals(Object that) {
    return (that instanceof Image
        && ((Image) that).name.equals(this.name)
        && ((Image) that).directory.equals(this.directory));
  }
}
