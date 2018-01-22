package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * {@code TagManager} stores all {@code Tag(s)} used in Model, a {@code Tag} can be added, removed,
 * or retrieved. {@code TagManager} follows a singleton design pattern, so only one instance of it
 * exists in the application.
 */
public class TagManager implements Serializable {

  /** All {@code Tag(s)} that have been created in the application. */
  private ArrayList<Tag> tags = new ArrayList<>();

  /** Privately creates new {@code TagManager}. */
  public TagManager() {}

  /**
   * Creates new {@code Tag} and adds it to list of {@code Tag(s)}.
   *
   * @param newTag {@code Tag} to be added to tags.
   */
  public void addTag(Tag newTag) {
    if (!tags.contains(newTag)) {
      tags.add(newTag);
    }
  }

  /**
   * Deletes a {@code Tag} permanently, first removing it from all tagged images.
   *
   * @param tagToDelete {@code Tag} to be deleted.
   */
  public void deleteTag(Tag tagToDelete) {
    Iterator<Image> imageIterator = tagToDelete.getTaggedImages().iterator();
    while (imageIterator.hasNext()) {
      Image image = imageIterator.next();
      image.removeTag(tagToDelete, false, false);
    }
    tags.remove(tagToDelete);
  }

  /**
   * Retrieves all currently stored {@code Tag(s)}.
   *
   * @return list of {@code Tag(s)}.
   */
  public ArrayList<Tag> getTags() {
    return tags;
  }
}
