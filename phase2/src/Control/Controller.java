package Control;

import Model.*;
import Model.Container;
import Model.Image;
import javafx.scene.control.ListView;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@code Controller} allows the frontend (View) to interact with the backend. */
public class Controller implements Serializable {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** Path to the save.ser file. */
  private File savePath;

  /** All {@code Image} that have been changed but not saved. */
  private ArrayList<Image> changedImages = new ArrayList<>();

  /** {@code Container} stores all other information, acts as model. */
  private Container container;

  /** Application's {@code ControlImage}. */
  private ControlImage imageControl;

  /** Application's {@code ControlTag}. */
  private ControlTag tagControl;

  /**
   * On construction; if a previously saved {@code Controller} exists, load it.
   *
   * @throws ClassNotFoundException if a class in the serialized version is not present in the
   *     current version.
   * @throws IOException if there is a problem reading or writing the save.
   */
  public Controller() throws ClassNotFoundException, IOException {
    savePath = new File("./save.ser").getAbsoluteFile();
    if (savePath.exists()) {
      read();
    } else {
      this.container = new Container();
    }
    imageControl = new ControlImage(this);
    tagControl = new ControlTag(this);
  }

  /**
   * Gets all {@code ImageManagers} contained in the {@code Container}. Each {@code ImageManager}
   * corresponds to a directory.
   *
   * @return List of all current {@code ImageManagers}.
   */
  public List<ImageManager> getImageManagers() {
    return container.getImageManagers();
  }

  /**
   * Changes all FileNames of any {@code changedImages} and writes to serialized file.
   *
   * @throws IOException if there is a problem writing to the file.
   */
  public void save() throws IOException {
    for (Image changedImage : changedImages) {
      imageControl.changeFileName(changedImage);
    }
    write();
  }

  /**
   * Retrieves any {@code Image} that may have changed.
   *
   * @return a list of {@code Image} that has been modified.
   */
  protected ArrayList<Image> getChangedImages() {
    return changedImages;
  }

  /**
   * Reads from a serialized file found at {@code savePath}.
   *
   * @throws ClassNotFoundException if a class is missing that is in serialized.
   * @throws IOException if the file cannot be found or read.
   */
  private void read() throws ClassNotFoundException, IOException {
    try (InputStream file = new FileInputStream(savePath);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer)) {
      container = (Container) input.readObject();
    } catch (ClassNotFoundException | IOException ex) {
      LOGGER.log(Level.WARNING, ex.getMessage());
      // pass on the exception for Interface or Terminal to handle
      throw (ex);
    }
  }

  /**
   * Writes all classes that implement {@code Serialized} to the file at {@code savePath}.
   *
   * @throws IOException if the file cannot be found or written.
   */
  private void write() throws IOException {
    try (OutputStream file = new FileOutputStream(savePath);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer)) {
      output.writeObject(container);
    } catch (IOException ex) {
      LOGGER.log(Level.WARNING, ex.getMessage());
      // pass on the exception for Interface or Terminal ot handle
      throw (ex);
    }
  }

  /**
   * Finds and returns the {@code Image} with desired directory and name. If it doesn't exist,
   * return {@code null}.
   *
   * @param imgPath Absolute path of desired {@code Image}.
   * @param imgName Name of {@code Image}.
   */
  public Image getImage(Path imgPath, String imgName) {
    for (ImageManager im : container.getImageManagers()) {
      if (im.getDirectory().equals(imgPath)) {
        for (Image image : im.getImages()) {
          if (image.getName().equals(imgName)) {
            return image;
          }
        }
      }
    }
    return null;
  }

  /**
   * Sets the last {@code File} that the user chose.
   *
   * @param folder A {@code File} of directory.
   */
  public void setDirectory(File folder) {
    container.setDirectory(folder);
  }

  /**
   * Gets the last {@code File} that the user chose. If the user hadn't chosen a {@code File}
   * before, then returns null.
   *
   * @return A {@code File} of the last chosen directory.
   */
  public File getDirectory() {
    return container.getDirectory();
  }

  /**
   * Retrieves application's Model.
   *
   * @return {@code Container}.
   */
  public Container getContainer() {
    return container;
  }

  /**
   * Retrieves application's {@code ControlImage}.
   *
   * @return {@code imageControl}.
   */
  public ControlImage getImageControl() {
    return imageControl;
  }

  /**
   * Retrieves application's {@code ControlTag}.
   *
   * @return {@code tagControl}.
   */
  public ControlTag getTagControl() {
    return tagControl;
  }

  /**
   * Gets the {@code File} of {@code Image}.
   *
   * @param image whose {@code File} we want.
   * @return {@code File}.
   */
  public File getImageFile(Image image) {
    return image.getImageFile();
  }

  /**
   * Turns a regular {@code List} into a {@code ListView} to be used with JavaFX.
   *
   * @param listToConvert original list.
   * @return Copy of the list as a {@code ListView}.
   */
  public ListView<String> convertArrayListToListView(ArrayList<String> listToConvert) {
    ListView<String> listToReturn = new ListView<>();
    for (String s : listToConvert) {
      listToReturn.getItems().add(s);
    }
    return listToReturn;
  }
}
