package View;

import Control.ControlImage;
import Model.Image;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

/**
 * {@code MoveImageDisplay} is a pane that displays a directory tree and a button for moving the
 * {@code Image}.
 */
public class MoveImageDisplay extends VBox {

  /** Desired path. */
  private Path desiredPath;

  /** Interface's {@code ControlImage}. */
  private ControlImage imageControl;

  /** Stage to be displayed on. */
  private Stage stageMoveImage;

  /** Text of absolute path. */
  private Text absolutePathText = ImageDetailsGridPane.getAbsolutePathText();

  /**
   * Creates {@code MoveImageDisplay} to move an {@code Image('s)} display.
   *
   * @param image {@code Image} to be displayed.
   * @param imageControl Interface's {@code ControlImage}.
   * @param stageMoveImage Stage to be displayed on.
   */
  public MoveImageDisplay(Image image, ControlImage imageControl, Stage stageMoveImage) {
    this.stageMoveImage = stageMoveImage;
    this.imageControl = imageControl;
    construct(image);
  }

  /**
   * Constructs a display from {@code Image} selected.
   *
   * @param image {@code Image} to be displayed.
   */
  private void construct(Image image) {
    TreeView<File> tree = new TreeView<>();
    tree.setRoot(Interface.getDirectoryTree().getRoot());
    tree.setCellFactory(
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
                  if (item.getName().matches("^.*(\\.gif|\\.jpg|\\.tiff|\\.jpeg|\\.png)+$")) {
                    setTextFill(Color.GREY.darker());
                  } else {
                    setTextFill(Color.BLACK);
                  }
                }
              }
            });
    tree.setOnMouseClicked(event -> createDirectory(tree));

    Button select = new Button("Select");
    select.setOnAction(event -> moveImage(image));
    select.setMinHeight(30);

    this.getChildren().add(tree);
    this.getChildren().add(select);
  }

  /**
   * Creates a directory from a tree.
   *
   * @param tree Tree from which to create tree.
   */
  private void createDirectory(TreeView<File> tree) {
      TreeItem<File> clickedFile = tree.getSelectionModel().getSelectedItem();
      if (clickedFile!= null) {
      desiredPath = clickedFile.getValue().toPath();
    } 
  }

  /**
   * Moves an {@Image}.
   *
   * @param image {@Image} to be moved.
   */
  private void moveImage(Image image) {
    try {
      imageControl.move(image, desiredPath);
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Success!");
      alert.setContentText("Moved " + image + " to " + desiredPath);
      alert.showAndWait();
      Interface.refreshTree();
      desiredPath = null;
      stageMoveImage.close();
      Interface.save();
      absolutePathText.setText(imageControl.getImageFile(image).getAbsolutePath());
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("Failed!");
      alert.setContentText(e.toString());
      alert.showAndWait();
    }
  }
}
