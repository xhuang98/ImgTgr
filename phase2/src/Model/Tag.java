package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A {@code Tag} is stored in {@code TagManager} and can be attached to an {@code Image}. */
public class Tag implements Serializable {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** Each {@code Tag} has a name. */
  private String name;

  /** {@code Image(s)} tagged with this {@code Tag}. */
  private ArrayList<Image> taggedImages;

  /**
   * Creates new {@code Tag}.
   *
   * @param name Name of {@code Tag}.
   * @throws TagNamingException when name isn't valid (i.e. contains @).
   */
  public Tag(String name) throws TagNamingException {
    if (name.contains("@") || name.contains(" ") || name.equals("")) {
      throw new TagNamingException("This is not a valid tag name.");
    } else {
      this.name = name;
      taggedImages = new ArrayList<>();
      LOGGER.log(Level.CONFIG, "Tag created: " + this.toString());
    }
  }

  /**
   * Adds {@code Image} to taggedImages in order to keep track of {@code Image(s)} tagged with this
   * {@code Tag}.
   *
   * @param image {@code Image} to {@code Tag}.
   */
  protected void tagImage(Image image) {
    if (!taggedImages.contains(image)) {
      taggedImages.add(image);
    }
  }

  /**
   * Removes the {@code Image} from the list of taggedImages.
   *
   * @param image {@code Image} to remove.
   */
  protected void untagImage(Image image) {
    taggedImages.remove(image);
  }

  /** Removes {@code Tag} from all {@code Image(s)} with instance of this {@code Tag}. */
  public void untagAllImages() {
    for (Image image : taggedImages) {
      image.removeTag(this, false, false);
    }
    taggedImages.clear();
  }

  /**
   * Retrieves name of {@code Tag}.
   *
   * @return name of {@code Tag}.
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves a list of all {@code Image(s)} that are tagged with this {@code Tag}.
   *
   * @return list of {@code Image} with {@code Tag}.
   */
  public ArrayList<Image> getTaggedImages() {
    return taggedImages;
  }

  /**
   * Determine whether two {@code Tag(s)} are equivalent. Returns true if and only if their names
   * are the same.
   *
   * @param object Object to be compared.
   */
  @Override
  public boolean equals(Object object) {
    return object instanceof Tag && ((Tag) object).getName().equals(this.name);
  }

  /**
   * Prepends {@code Tag} name with @.
   *
   * @return name prepended with @.
   */
  @Override
  public String toString() {
    return "@" + name;
  }
}
