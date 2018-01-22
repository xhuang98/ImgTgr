package Control;

import Model.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/** Traverses a {@code File} structure and extracts relevant files to create Objects with. */
public class Read {

  /**
   * Traverses a directory to determine whether a file is an image. If the file is an image it will
   * be stored in {@code ImageManager} where an {@code Image} will be created.
   *
   * @param path of the directory to traverse.
   * @param container in which to store new {@code ImageManagers}.
   * @return true if {@code Path} is an image file, false otherwise.
   */
  public static boolean traverse(Path path, Container container) {
    File dir = path.toFile();
    if (dir.isDirectory()) {
      ImageManager im = new ImageManager(dir);
      container.addImageManager(im);
      File[] allFiles = dir.listFiles();
      if (allFiles != null) {
        for (File file : allFiles) {
          if (traverse(file.toPath(), container)) {
            im.addImage(file, updateTagManager(file, container.getTagManager()));
          }
        }
      }
      return false;
    } else {
      return isImage(path);
    }
  }

  /**
   * Checks whether {@code Path} ends with a particular extension that would signify it's an image.
   *
   * @param name Check the extension of this {@code Path}.
   * @return true iff this {@code Path} ends in one of the determined image extensions, false
   *     otherwise.
   */
  private static boolean isImage(Path name) {
    return name.toString().matches("^.*(\\.gif|\\.jpg|\\.tiff|\\.jpeg|\\.png)+$");
  }

  /**
   * Checks if the file name contains Tags, updates the {@code TagManager} and returns an {@code
   * ArrayList} of {@code Tag} included in the name.
   *
   * @param image to be checked.
   * @param tagManager to be updated.
   * @return Tags included in the image's name.
   */
  private static ArrayList<Tag> updateTagManager(File image, TagManager tagManager) {
    ArrayList<Tag> tagsFound = new ArrayList<>();
    String[] nameAndTag = image.getName().split("\\.")[0].split("\\s@");
    if (nameAndTag.length > 1) {
      for (String tagName : Arrays.copyOfRange(nameAndTag, 1, nameAndTag.length)) {
        try {
          Tag newTag = new Tag(tagName);
          ArrayList<Tag> managerTags = tagManager.getTags();
          if (managerTags.contains(newTag)) {
            newTag = managerTags.get(managerTags.indexOf(newTag));
          } else {
            tagManager.addTag(newTag);
          }
          tagsFound.add(newTag);
        } catch (TagNamingException exception) {
          // Do not take in illegal tags
        }
      }
    }
    return tagsFound;
  }
}
