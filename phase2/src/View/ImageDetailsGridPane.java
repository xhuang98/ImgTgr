package View;

import Model.Image;
import Model.Tag;
import Model.TagNamingException;
import Control.Controller;
import Control.ControlImage;
import Control.ControlTag;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class ImageDetailsGridPane extends GridPane {

  /** The interface's {@code Controller}. */
  private Controller controller;

  /** The interface's {@code ControlImage} */
  private ControlImage imageControl;

  /** The interface's {@code ControlTag} */
  private ControlTag tagControl;

  /** List of {@code Image(s)} with the same {@code Tag}. */
  private ListView<Image> imageList;

  /** The Stage for viewing name history. */
  private Stage stageNameHistory = new Stage();

  /** The Stage for moving {@code Image}. */
  private static Stage stageMoveImage = new Stage();

  /** Keeps track of the selected Tag. */
  private Tag selectedTag = null;

  /** The ListView of all {@code Tag(s)}. */
  private ListView<Tag> allTags = new ListView<>();

  /** Indicates that the {@code Tag} already exists. */
  private Text tagExistsWarning = new Text("This tag already exists!");

  /** Indicates that the input is not valid. */
  private Text invalidTagWarning = new Text("This is not a valid tag!");

  /** Index of the old version to change to. Default value is -1. */
  private int versionIndex = -1;

  /** Display of {@code Image}. */
  private javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();

  /** Keeps track of current absolute path. */
  private static Text absolutePathText = null;

  Text instructionAddExisting = new Text("All tags");

  javafx.scene.image.Image imageToShow;

  public InputStream file;

  /**
   * Creates ImageDetailsGridPane.
   *
   * @param control Controller to be used.
   * @param imageList List of {@code Image(s)} to be stored.
   */
  protected ImageDetailsGridPane(Controller control, ListView<Image> imageList) {
    this.controller = control;
    this.imageList = imageList;
    imageControl = control.getImageControl();
    tagControl = control.getTagControl();
    initiate();
  }

  /** Initiates the pane. */
  private void initiate() {
    tagExistsWarning.setFill(Color.RED.darker());
    invalidTagWarning.setFill(Color.RED.darker());
    setAlignment(Pos.BASELINE_LEFT);
    setPadding(new Insets(0, 10, 0, 10));
    setVgap(5);
    setHgap(5);
    for (int i = 0; i < 6; i++) {
      RowConstraints mediumRow = new RowConstraints(30);
      RowConstraints longRow = new RowConstraints(115);
      if (i == 2 || i == 4) {
        getRowConstraints().add(longRow);
      } else {
        getRowConstraints().add(mediumRow);
      }
    }
    RowConstraints shortRow = new RowConstraints(15);
    getRowConstraints().add(shortRow);
    getRowConstraints().add(shortRow);

    ColumnConstraints wideColumn = new ColumnConstraints(200);
    ColumnConstraints thinColumn = new ColumnConstraints(100);
    getColumnConstraints().add(wideColumn);
    getColumnConstraints().add(thinColumn);
    // Existing tags
    for (Tag tag : tagControl.getAllTags()) {
      allTags.getItems().add(tag);
    }
    allTags.setOnMouseClicked(
        event -> {
          selectedTag = allTags.getSelectionModel().getSelectedItem();
          if (selectedTag != null) {
            constructImageList(imageList, selectedTag);
          }
        });
    // Add non-existing tags to allTags
    Text instructionAddNew = new Text("Add here:");
    TextField newTagInput = new TextField();
    Button addNonExistingTag = new Button("Add");
    addNonExistingTag.setOnAction(
        event -> {
          String newTagString = newTagInput.getText();
          // update to tag manager
          // update tags display
          Tag updated = null;
          boolean valid = true;
          try {
            updated = tagControl.addTag(newTagString);
            Interface.save();
          } catch (TagNamingException ex) {
            if (this.getChildren().contains(tagExistsWarning)) {
              this.getChildren().remove(tagExistsWarning);
            }
            if (!this.getChildren().contains(invalidTagWarning)) {
              add(invalidTagWarning, 0, 7);
            }
            valid = false;
          }
          if (updated != null) {
            addToAllTags(updated);
          } else if (valid) {
            if (this.getChildren().contains(invalidTagWarning)) {
              this.getChildren().remove(invalidTagWarning);
            }
            if (!this.getChildren().contains(tagExistsWarning)) {
              add(tagExistsWarning, 0, 7);
            }
          }
        });
    // Delete Existing Tag
    Button deleteTag = new Button("Delete");
    deleteTag.setOnAction(
        event -> {
          deleteTag();
          selectedTag = null;
          imageList.getItems().clear();
          Interface.save();
        });
    // Untag all images
    Button untagAllImages = new Button("Untag all");
    untagAllImages.setOnAction(
        event -> {
          untagAllImages();
          selectedTag = null;
          Interface.save();
        });
    // VBox for Delete and Untag all
    VBox deleteUntag = new VBox();
    deleteUntag.getChildren().add(deleteTag);
    deleteUntag.getChildren().add(untagAllImages);
    deleteUntag.setSpacing(20);
    add(instructionAddExisting, 0, 3);
    add(allTags, 0, 4);
    add(instructionAddNew, 0, 5);
    add(newTagInput, 0, 6);
    add(addNonExistingTag, 1, 6);
    add(deleteUntag, 1, 4);
  }

  /** Refreshes the pane when no {@code Image} is selected. */
  protected void refresh() {
    for (Tag tag : tagControl.getAllTags()) {
      if (!allTags.getItems().contains(tag)) {
        allTags.getItems().add(tag);
      }
    }
  }

  /**
   * Refreshes the pane to contain information of {@code Image}.
   *
   * @param image The desired {@code Image}.
   */
  protected void refresh(Image image) {
    if (image != null) {
      Stack<Button> buttons = new Stack<>();
      absolutePathText = new Text(imageControl.getImageFile(image).getAbsolutePath());
      absolutePathText.setFont(new Font(8));
      showImageView(image);
      Text imageName = new Text(imageControl.getImageName(image));
      imageName.setFont(Font.font("System", 14));
      ListView<Tag> imageTagList = createTagList(image);

      // Name History
      Button name_history = new Button("Name History");
      buttons.push(name_history);
      name_history.setOnAction(event -> showHistory(image));
      // Log
      Button logs = new Button("Log");
      buttons.push(logs);
      logs.setOnAction(event -> showLogs(image));
      // Move file
      Button move = new Button("Move image");
      buttons.push(move);
      move.setOnAction(event -> constructMoveWindow(image));
      // Open folder
      Button openFolder = new Button("Open Folder");
      buttons.push(openFolder);
      openFolder.setOnAction(event -> openFolder(image));
      // View Image
      Button openImage = new Button("Open Image");
      buttons.push(openImage);
      openImage.setOnAction(event -> openImage(image));
      // Remove all tags from Image
      Button removeAllTags = new Button("Remove all");
      buttons.push(removeAllTags);
      removeAllTags.setOnAction(
          event -> {
            removeAllTags(image, imageTagList);
            updateImageList(image, imageTagList);
          });
      // Remove Tag from Image
      Button removeTag = new Button("Remove");
      buttons.push(removeTag);
      removeTag.setOnAction(
          event -> {
            removeTag(image);
            updateImageList(image, imageTagList);
          });
      // Add Existing Tag
      Button addExistingTag = new Button("Add");
      buttons.push(addExistingTag);
      addExistingTag.setOnAction(event -> addExistingTag(image, imageTagList));
      // Untag all images
      Button untagAllImages = new Button("Untag all");
      buttons.push(untagAllImages);
      untagAllImages.setOnAction(
          event -> {
            untagAllImages();
            updateImageList(image, imageTagList);
          });
      // Delete Existing Tag
      Button deleteTag = new Button("Delete");
      buttons.push(deleteTag);
      deleteTag.setOnAction(
          event -> {
            deleteTag();
            updateImageList(image, imageTagList);
          });
      // Add non-existing tags
      TextField newTagInput = new TextField();
      Button addNewTag = new Button("Add");
      buttons.push(addNewTag);
      addNewTag.setOnAction(event -> addNewTag(image, imageTagList, newTagInput.getText()));

      // Left click to see images with the same tag. Right click to choose to remove tag
      imageTagList.setOnMouseClicked(
          event -> {
            MouseButton button = event.getButton();
            selectedTag = imageTagList.getSelectionModel().getSelectedItem();
            if (selectedTag != null) {
              // Right click on tag
              if (button == MouseButton.SECONDARY) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem delete = new MenuItem("Remove tag");
                contextMenu.getItems().addAll(delete);
                contextMenu.show(imageTagList, event.getScreenX(), event.getScreenY());
                contextMenu.addEventFilter(
                    MouseEvent.MOUSE_PRESSED,
                    mouseEvent -> {
                      tagControl.untag(selectedTag, image);
                      updateImageList(image, imageTagList);
                    });
              }
              // Left click on tag
              else if (button == MouseButton.PRIMARY) {
                // Update right pane
                leftClickOnTag();
              }
            }
          });

      // Select and add existing tags
      showExistingTags();
      // Construct grid pane
      constructGrid(imageName, imageTagList, buttons, newTagInput);
      // Right click to delete tag.
      allTags.setOnMouseClicked(
          event -> {
            MouseButton button = event.getButton();
            selectedTag = allTags.getSelectionModel().getSelectedItem();
            if (selectedTag != null) {
              // Right click on tag
              if (button == MouseButton.SECONDARY) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem delete = new MenuItem("Delete tag");
                contextMenu.getItems().addAll(delete);
                contextMenu.show(allTags, event.getScreenX(), event.getScreenY());
                contextMenu.addEventFilter(
                    MouseEvent.MOUSE_PRESSED,
                    mouseEvent -> {
                      deleteTag();
                      updateImageList(image, imageTagList);
                    });
              }
              // Left click on tag
              else if (button == MouseButton.PRIMARY) {
                // Update right pane
                leftClickOnTag();
              }
            }
          });
    }
  }

  /**
   * Shows history of given {@code Image}.
   *
   * @param image {@code Image} whose history is shown.
   */
  private void showHistory(Image image) {
    VBox root = constructNameHistoryDisplay(image);
    stageNameHistory.setTitle("Name History");
    stageNameHistory.setScene(new Scene(root, 400, 400));
    stageNameHistory.show();
  }

  /**
   * Shows logs of given {@code Image}.
   *
   * @param image {@code Image} whose logs are shown.
   */
  private void showLogs(Image image) {
    ArrayList<String> rootArray = imageControl.getLog(image);
    ListView<String> root = controller.convertArrayListToListView(rootArray);
    if (root != null) {
      Stage stage = new Stage();
      stage.setTitle("Log");
      stage.setScene(new Scene(root, 450, 450));
      stage.show();
    }
  }

  /**
   * Opens folder containing given {@code Image}.
   *
   * @param image {@code Image} whose folder is opened.
   */
  private void openFolder(Image image) {
    try {
      imageControl.openImageFolder(image);
    } catch (IOException exception) {
      Interface.displayAlert(
          "Error opening folder.", "We were unable to open this folder, please try again.");
    }
  }

  private void showImageView(Image image){
    try {
      file = new FileInputStream(controller.getImageFile(image));
      imageToShow =
              new javafx.scene.image.Image(file, 200, 200, true, true);
      file.close();
      imageView.setImage(imageToShow);
    } catch (IOException ex) {
      Interface.displayAlert("Oops! Can't display image!", ex.getMessage());
      if(imageToShow != null){
        imageToShow = null;
      }
    }
  }

  /**
   * Shows given {@code Image} by opening its file.
   *
   * @param image {@code Image} to be opened.
   */
  private void openImage(Image image) {
    try {
      imageControl.openImage(image);
    } catch (IOException exception) {
      Interface.displayAlert(
          "Error opening Image.", "we were unable to open this image, please try again.");
    }
  }

  /**
   * Creates a list of {@code Tag(s)} associated to given {@code Image}.
   *
   * @param image {@code Image} from which {@code Tag} list will be created.
   * @return ListView of {@code Tag(s)}.
   */
  private ListView<Tag> createTagList(Image image) {
    ListView<Tag> imageTagList = new ListView<>();
    for (Tag tag : image.getCurrentTags()) {
      imageTagList.getItems().add(tag);
    }

    return imageTagList;
  }

  /** Gives options when left clicking on {@code Image}. */
  private void leftClickOnTag() {
    imageList.getItems().clear();
    constructImageList(imageList, selectedTag);
  }

  /** Shows all existing {@code Tag(s)}. */
  private void showExistingTags() {
    for (Tag tag : tagControl.getAllTags()) {
      if (!allTags.getItems().contains(tag)) {
        allTags.getItems().add(tag);
      }
    }
  }

  /**
   * Adds existing {@code Tag} to given {@code Image}.
   *
   * @param image {@code Image} to which selected {@code Tag} is added.
   * @param imageTagList List of {@code Tag(s)} of {@code Image}.
   */
  private void addExistingTag(Image image, ListView<Tag> imageTagList) {
    if (selectedTag != null) {
      Tag updated = null;
      try {
        updated = tagControl.addTag(tagControl.getTagName(selectedTag), image);
        Interface.save();
        absolutePathText.setText(imageControl.getImageFile(image).getAbsolutePath());
      } catch (TagNamingException exception) {
        Interface.displayAlert("Tag Naming Error.", exception.toString());
      }
      if (updated != null) {
        imageTagList.getItems().add(updated);
      }
    }
    selectedTag = null;
  }

  /**
   * Removes currently selected {@code Tag} from {@code Image} and updates imageTagList.
   *
   * @param image {@code Image} from which the {@code Tag} will be removed.
   */
  private void removeTag(Image image) {
    if (selectedTag != null) {
      tagControl.untag(selectedTag, image);
    }
  }

  /**
   * Removes all {@code Tag(s)} from the selected {@code Image}. Updates {@code Image('s)} {@code
   * Tag} list.
   *
   * @param image {@code Image} whose {@code Tag(s)} will be removed.
   * @param imageTagList ImageTagList to update.
   */
  private void removeAllTags(Image image, ListView<Tag> imageTagList) {
    imageControl.removeAllTags(image);
    imageTagList.getItems().clear();
  }

  /** Untags all {@Image(s)} of selected {@code Image}. */
  private void untagAllImages() {
    if (selectedTag != null) {
      tagControl.untagAll(selectedTag);
    }
  }

  /**
   * Deletes currently selected {@code Tag} permanently, first removing it from all tagged {@code
   * Image(s)}.
   */
  private void deleteTag() {
    if (selectedTag != null) {
      tagControl.deleteTag(selectedTag);
      allTags.getItems().remove(selectedTag);
    }
  }

  /**
   * Updates {@code Tag} lists and resets selected {@code Tag} after deleting or removing a tag.
   *
   * @param image {@code Image} to be updated.
   * @param imageTagList List of {@code Tag(s)} of {@code Image}.
   */
  private void updateImageList(Image image, ListView<Tag> imageTagList) {
    imageTagList.getItems().remove(selectedTag);
    selectedTag = null;
    imageList.getItems().clear();
    Interface.save();
    absolutePathText.setText(imageControl.getImageFile(image).getAbsolutePath());
  }

  /**
   * Creates new {@code Tag} and adds it to selected {@code Image}.
   *
   * @param image {@code Image} to which new {@code Tag} is added.
   * @param imageTagList List of {@code Tag(s)}.
   * @param newTag Name of new {@code Tag} to be created.
   */
  private void addNewTag(Image image, ListView<Tag> imageTagList, String newTag) {
    // String newTagString = newTagInput.getText();
    // update to tag manager
    // update tags display
    Tag updated = null;
    boolean valid = true;
    try {
      updated = tagControl.addTag(newTag, image);
      Interface.save();
      absolutePathText.setText(imageControl.getImageFile(image).getAbsolutePath());
    } catch (TagNamingException ex) {
      if (this.getChildren().contains(tagExistsWarning)) {
        this.getChildren().remove(tagExistsWarning);
      }
      if (!this.getChildren().contains(invalidTagWarning)) {
        add(invalidTagWarning, 0, 7);
      }
      valid = false;
    }
    if (updated != null) {
      addToAllTags(updated);
      imageTagList.getItems().add(updated);
    } else if (valid) {
      if (this.getChildren().contains(invalidTagWarning)) {
        this.getChildren().remove(invalidTagWarning);
      }
      if (!this.getChildren().contains(tagExistsWarning)) {
        add(tagExistsWarning, 0, 7);
      }
    }
  }

  /**
   * Constructs GridPane for application.
   *
   * @param imageName Name of selected {@code Image}.
   * @param imageTagList List of {@code Tag(s)} currently available.
   * @param buttons Buttons to be displayed.
   * @param tagInput Input of tag.
   */
  private void constructGrid(
      Text imageName, ListView<Tag> imageTagList, Stack<Button> buttons, TextField tagInput) {
    HBox nameHBox = new HBox();
    HBox moveLog = new HBox();
    VBox addDeleteBox = new VBox();
    VBox removeBox = new VBox();

    // addNewTag
    add(buttons.pop(), 1, 6);
    // addExistingTag, untagAll and deleteTag
    for (int nBoxes = 0; nBoxes < 3; nBoxes++) {
      addDeleteBox.getChildren().add(buttons.pop());
    }
    addDeleteBox.setSpacing(20);
    add(addDeleteBox, 1, 4);
    // RemoveTag
    removeBox.getChildren().add(buttons.pop());
    removeBox.getChildren().add(buttons.pop());
    removeBox.setPadding(new Insets(50, 0, 0, 0));
    removeBox.setSpacing(15);
    add(removeBox, 1, 2);
    // View Image
    add(buttons.pop(), 1, 1);
    // OpenFolder
    add(buttons.pop(), 1, 0);
    // Move
    moveLog.getChildren().add(buttons.pop());
    // Logs
    moveLog.getChildren().add(buttons.pop());
    // Name
    nameHBox.getChildren().add(imageName);
    // Name History
    nameHBox.getChildren().add(buttons.pop());
    nameHBox.setSpacing(10);
    moveLog.setSpacing(5);

    Text instructionAddNew = new Text("Can't find it? Add here:");

    add(moveLog, 0, 0);
    add(nameHBox, 0, 1);
    add(imageView, 3, 1);
    add(imageTagList, 0, 2);
    add(instructionAddExisting, 0, 3);
    add(allTags, 0, 4);
    add(instructionAddNew, 0, 5);
    add(tagInput, 0, 6);
    add(absolutePathText, 0, 8);
  }

  /**
   * Returns a VBox display of the name history of an {@code Image} and a Button for going back to a
   * previous version.
   *
   * @param image The desired {@code Image}.
   * @return The desired display.
   */
  private VBox constructNameHistoryDisplay(Image image) {
    VBox vBox = new VBox();
    ArrayList<String> listNameHistoryArray = imageControl.getNameHistory(image);
    ListView<String> listNameHistory = controller.convertArrayListToListView(listNameHistoryArray);
    listNameHistory.setOnMouseClicked(
        event -> versionIndex = listNameHistory.getSelectionModel().getSelectedIndex());
    Button changeToBtn = new Button("Change to");
    changeToBtn.setMinHeight(30);
    changeToBtn.setOnAction(
        event -> {
          if (versionIndex == imageControl.getLifeTimeTagsLength(image) - 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Newest version!");
            alert.showAndWait();
            versionIndex = -1;
          } else if (versionIndex != -1) {
            imageControl.changeToOldVersion(image, versionIndex);
            getChildren().clear();
            refresh(image);
            stageNameHistory.close();
            Interface.save();
            absolutePathText.setText(imageControl.getImageFile(image).getAbsolutePath());
          } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Please select version!");
            alert.showAndWait();
          }
        });
    vBox.getChildren().add(listNameHistory);
    vBox.getChildren().add(changeToBtn);
    return vBox;
  }

  /**
   * Refreshes the right pane according to the desired {@code Tag}.
   *
   * @param imageList The ListView that shows {@code Image(s)} that has the desired {@code Tag}.
   * @param tag The desired {@code Tag}.
   */
  private void constructImageList(ListView<Image> imageList, Tag tag) {
    imageList.getItems().clear();
    for (Image image : tagControl.getTaggedImages(tag)) {
      imageList.getItems().add(image);
    }
  }

  /**
   * Adds {@code Tag} to the ListView of all {@code Tag(s)} if the {@code Tag} is not in the
   * ListView.
   *
   * @param tag The {@code Tag} to be added.
   */
  private void addToAllTags(Tag tag) {
    if (!allTags.getItems().contains(tag)) {
      allTags.getItems().add(tag);
    }
  }

  /**
   * Shows a Stage with the MoveImageDisplay about the {@code Image}.
   *
   * @param image The desired {@code Image}.
   */
  private void constructMoveWindow(Image image) {
    MoveImageDisplay moveImageDisplay = new MoveImageDisplay(image, imageControl, stageMoveImage);
    stageMoveImage.setTitle("Move to");
    stageMoveImage.setScene(new Scene(moveImageDisplay, 300, 350));
    stageMoveImage.show();
  }

  /**
   * Gets current absolute path.
   *
   * @return Text of current absolute path.
   */
  protected static Text getAbsolutePathText() {
    return absolutePathText;
  }
}
