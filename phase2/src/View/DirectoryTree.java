package View;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;

/** {@code DirectoryTree} keeps a copy of the current directory as a Tree. */
public class DirectoryTree extends TreeView<File> implements Serializable {

  /** Root of this directory. */
  private File rootDirectory;

  /** The node of the root. */
  private static TreeItem<File> root;

  /**
   * Sets the the root File of the directory.
   *
   * @param root File that denotes the root of directory.
   */
  protected void setRootDirectory(File root) {
    rootDirectory = root;
  }

  /** Refreshes the {@code DirectoryTree} by scanning for any new or deleted nodes. */
  protected void refreshTree() {
    if (rootDirectory != null) {
      root = getNodesForDirectory(rootDirectory);
      setRoot(root);
      setCellFactory(
          (t) ->
              new TreeCell<File>() {
                // Updates colour of the tree.
                @Override
                protected void updateItem(File item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty || item == null) {
                    setText(null);
                  } else {
                    setText(item.getName());
                    if (!item.getName().matches("^.*(\\.gif|\\.jpg|\\.tiff|\\.jpeg|\\.png)+$")) {
                      setTextFill(Color.GREY);
                    } else {
                      setTextFill(Color.BLACK);
                    }
                  }
                }
              });
    }
  }

  /**
   * Creates a Tree representation of the given directory.
   *
   * @param directory to turn into a tree.
   * @return the top node in the tree.
   */
  private TreeItem<File> getNodesForDirectory(File directory) {
    TreeItem<File> root = new TreeItem<>(directory);
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory()) {
            root.getChildren().add(getNodesForDirectory(f));
          } else if (f.getName().matches("^.*(\\.gif|\\.jpg|\\.tiff|\\.jpeg|\\.png)+$")) {
            root.getChildren().add(new TreeItem<>(f));
          }
        }
      }
    }
    return root;
  }

  /**
   * Expands this {@code DirectoryTree('s)} starting from root.
   *
   * @param root Root of the {@code DirectoryTree}.
   */
  protected void expandTree(TreeItem<File> root) {
    if (root != null && !root.isLeaf()) {
      root.setExpanded(true);
      for (TreeItem<File> child : root.getChildren()) {
        expandTree(child);
      }
    }
  }
}
