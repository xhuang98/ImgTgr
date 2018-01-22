package Control;

import Model.*;

import java.io.Serializable;
import java.util.ArrayList;

/** {@code ControlTag} serves as a {@code Controller} for {@code Tag}. */
public class ControlTag implements Serializable {

  /** Application's {@code ControlImage}. */
  private ControlImage imageControl;

  /** Application's {@code TagManager}. */
  private TagManager tagManager;

  /**
   * Creates new {@code ControlTag} and associates to a {@code Controller}.
   *
   * @param control {@code Controller} to be associated to.
   */
  public ControlTag(Controller control) {
    tagManager = control.getContainer().getTagManager();
    imageControl = control.getImageControl();
  }

  /**
   * Creates a {@code Tag} given a {@code String} if it doesn't exist and adds the new {@code Tag}
   * to {@code Image}. If a {@code Tag} with the name already exists, adds the {@code Tag} to {@code
   * Image} returns the {@code Tag} if it is added successfully, else returns null.
   *
   * @param tagName name of new {@code Tag}.
   * @param image to add {@code Tag} to.
   * @throws TagNamingException if the input {@code Tag} name does not follow standards.
   */
  public Tag addTag(String tagName, Image image) throws TagNamingException {
    Tag sameTag = null;
    for (Tag t : tagManager.getTags()) {
      if (tagName.equals(t.getName())) {
        sameTag = t;
      }
    }
    if (sameTag == null) {
      Tag newTag = new Tag(tagName);
      tagManager.addTag(newTag);
      imageControl.updateImage(image);
      return image.addTag(newTag);
    } else {
      imageControl.updateImage(image);
      return image.addTag(sameTag);
    }
  }

  /**
   * Creates new {@code Tag} from {@code tagName} given. If {@code Tag} with same name already
   * exists, no new {@code Tag} is created.
   *
   * @param tagName Name of tag to be created.
   * @return Tag if new {@code Tag} has been created. {@code Null} otherwise.
   * @throws TagNamingException when {@code Tag} isn't named properly (i.e. contains @).
   */
  public Tag addTag(String tagName) throws TagNamingException {
    Tag sameTag = null;
    for (Tag t : tagManager.getTags()) {
      if (tagName.equals(t.getName())) {
        sameTag = t;
      }
    }
    if (sameTag == null) {
      Tag newTag = new Tag(tagName);
      tagManager.addTag(newTag);
      return newTag;
    } else {
      return null;
    }
  }

  /**
   * Removes {@code Tag} from selected {@code Image}. {@code Tag} will still be available as a
   * {@code Tag} in {@code TagManager}.
   *
   * @param tag to remove.
   * @param image from which to remove {@code Tag} from.
   */
  public void untag(Tag tag, Image image) {
    image.removeTag(tag);
    imageControl.updateImage(image);
  }

  /**
   * Removes {@code Tag} from selected {@code Image}. {@code Tag} will still be available as a
   * {@code Tag} in {@code TagManager}.
   *
   * @param tag name of {@code Tag} to remove.
   * @param image rom which to remove {@code Tag} from.
   */
  public void untag(String tag, Image image) {
    for (Tag existing : tagManager.getTags()) {
      if (existing.getName().equals(tag)) {
        untag(existing, image);
        return;
      }
    }
  }

  /**
   * Removes given {@code Tag} from all {@code Image(s)} tagged with it.
   *
   * @param tag to be removed {@code Image(s)}.
   */
  public void untagAll(Tag tag) {
    for (Image image : tag.getTaggedImages()) {
      imageControl.updateImage(image);
    }
    tag.untagAllImages();
  }

  /**
   * Permanently deletes {@code Tag}, first removing it from all associated {@code Image}.
   *
   * @param tag to be removed from the program.
   */
  public void deleteTag(Tag tag) {
    for (Image image : tag.getTaggedImages()) {
      imageControl.updateImage(image);
    }
    tagManager.deleteTag(tag);
  }

  /**
   * Retrieves all instances of {@code Image} that contain {@code Tag}.
   *
   * @param tag to search.
   * @return List of all {@code Image} with {@code Tag}.
   */
  public ArrayList<Image> getTaggedImages(Tag tag) {
    return tag.getTaggedImages();
  }

  /**
   * Retrieves all {@code Tag} currently available.
   *
   * @return List of all tags.
   */
  public ArrayList<Tag> getAllTags() {
    return tagManager.getTags();
  }

  /**
   * Retrieves the name of given {@code Tag}.
   *
   * @param tag whose name is retrieved.
   * @return name of {@code Tag}.
   */
  public String getTagName(Tag tag) {
    return tag.getName();
  }
}
