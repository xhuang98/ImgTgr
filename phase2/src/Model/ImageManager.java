package Model;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * {@code ImageManager} keeps a collection of all {@code Image(s)} that have been read into the
 * application.
 */
public class ImageManager implements Serializable {

  /** Top level name of this directory; */
  private String name;

  /** Path to open the directory of ImageManger. */
  private String directory;

  /** All {@code Image(s)} from a particular directory. */
  private ArrayList<Image> images;

  /**
   * Creates new {@code ImageManager}. Directory initialized that of specified file's path. Name
   * initialized to directory's name.
   *
   * @param directory File's whose directory the {@code ImageManager} is set to.
   */
  public ImageManager(File directory) {
    this.directory = directory.toPath().toString();
    this.name = directory.getName();
    images = new ArrayList<>();
  }

  /**
   * Creates a new {@code Image} from File and adds it to the rest of stored {@code Image(s)}. If
   * {@code Image} already exists in {@code ImageManager}, replace old tags with new.
   *
   * @param image File to be converted to {@code Image}.
   */
  public void addImage(File image, ArrayList<Tag> existingTags) {
    Image newImage;
    if (existingTags.size() == 0) {
      newImage = new Image(image);
    } else {
      newImage = new Image(image, existingTags);
    }
    newImage.setManager(this);
    boolean foundSame = false;
    for (Image savedImage : images) {
      if (savedImage.equals(newImage)) {
        foundSame = true;
        newImage = null;
      }
    }
    if (!foundSame) {
      images.add(newImage);
    }
  }

  /**
   * Adds an {@code Image} to {@code ImageManager}.
   *
   * @param image {@code Image} to be added.
   */
  public void addImage(Image image) {
    images.add(image);
  }

  /**
   * Removes an {@code Image} from {@code ImageManager}.
   *
   * @param image {@code Image} to be removed.
   */
  public void removeImage(Image image) {
    images.remove(image);
  }

  /**
   * Returns the directory.
   *
   * @return Directory of {@code ImageManager}.
   */
  public Path getDirectory() {
    return new File(directory).toPath();
  }

  /**
   * Returns list of {@code Image(s)} in this {@code ImageManager}.
   *
   * @return {@code Image(s)} associated to current {@code ImageManager}.
   */
  public ArrayList<Image> getImages() {
    return images;
  }

  /**
   * Returns the directory's top level name.
   *
   * @return name of directory.
   */
  @Override
  public String toString() {
    return name;
  }
}
