package View;

import Model.*;
import Control.Controller;
import Control.Read;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Application's main Interface, displayed on user's screen. */
public class Interface extends Application {

  /** Provides access to the logger. */
  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());
  /** Controller for the program, all actions should be routed through this. */
  private static Controller controller;
  /** The path of the file initially selected. */
  private static Path rootFilePath;
  /** The directory chosen. */
  private File rootFile;
  /** LeftPane */
  private static DirectoryTree directoryTree;
  /** Image details and operations. */
  private ImageDetailsGridPane imageDetails;
  /** Centre pane */
  private VBox centrePane;
  /** Right pane */
  private VBox rightPane;
  /** List of images with the same tag */
  private static ListView<Image> imageList;
  /** Text displayed next to save button. */
  private static Text lastSavedTime;

  /**
   * Launches program.
   *
   * @param args Arguments.
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Starts the program.
   *
   * @param primaryStage Stage to start at.
   */
  @Override
  public void start(Stage primaryStage) {
    Log.init();
    try {
      controller = new Controller();
      imageList = new ListView<>();
      imageDetails = new ImageDetailsGridPane(controller, imageList);
      directoryTree = new DirectoryTree();
      centrePane = new VBox();
      rightPane = new VBox();
      lastSavedTime = new Text();
    } catch (ClassNotFoundException | IOException exception) {
      displayAlert("Start-up error", exception.getMessage());
      LOGGER.log(Level.WARNING, exception.getMessage());
    }
    rootFile = controller.getDirectory();
    if(rootFile != null) {
      rootFilePath = Paths.get(rootFile.getAbsolutePath());
      directoryTree.setRootDirectory(rootFile);
      refreshTree();
    }

    Button loadBtn = new Button("Load Folder");
    Button expandBtn = new Button("Expand All");

    expandBtn.setOnAction(event -> directoryTree.expandTree(directoryTree.getRoot()));

    // Left: Directory view
    loadBtn.setOnAction(
        event -> {
          displayDirectory(primaryStage);
          if (rootFile != null) {
            controller.setDirectory(rootFile);
          }
          save();
        });

    // Choose directory
    directoryTree.setOnMouseClicked(event -> selectDirectory());

    initiateRightPane();
    initiateCentrePane();
    GridPane buttonGrid = createGrid(loadBtn, expandBtn);
    BorderPane border = alignGrid(buttonGrid);
    setupStage(primaryStage, border);
  }

  /**
   * Creates GridPane for application.
   *
   * @param loadBtn Button to load a directory.
   * @param saveBtn Button to save changes made in application.
   * @return GridPane that was created.
   */
  private GridPane createGrid(Button loadBtn, Button saveBtn) {
    GridPane buttonGrid = new GridPane();
    buttonGrid.setAlignment(Pos.CENTER_LEFT);
    buttonGrid.setPadding(new Insets(20, 10, 20, 10));
    buttonGrid.setHgap(10);
    buttonGrid.add(loadBtn, 0, 0);
    buttonGrid.add(saveBtn, 1, 0);
    buttonGrid.add(lastSavedTime, 2, 0);

    return buttonGrid;
  }

  /**
   * Aligns the GridPane on the screen.
   *
   * @param buttonGrid GridPane to be aligned.
   * @return Border of GridPane.
   */
  private BorderPane alignGrid(GridPane buttonGrid) {
    BorderPane border = new BorderPane();
    border.setTop(buttonGrid);
    border.setLeft(directoryTree);
    border.setCenter(centrePane);
    border.setRight(rightPane);

    return border;
  }

  /**
   * Set up stage for application.
   *
   * @param primaryStage Application's primary stage.
   * @param border GridPane's border.
   */
  private void setupStage(Stage primaryStage, BorderPane border) {
    primaryStage.setScene(new Scene(border, 1070, 600));
    primaryStage.setTitle("ImgTgr");
    primaryStage.show();
  }

  /** Initiates centre pane. */
  private void initiateCentrePane() {
    Text title = new Text("Image-Tag Details");
    title.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
    centrePane.getChildren().add(title);
    centrePane.getChildren().add(imageDetails);
  }

  /** Initiates right pane. */
  private void initiateRightPane() {
    rightPane.setSpacing(5);
    rightPane.getChildren().add(new Text("Click on a tag to see images with this tag!"));
    rightPane.getChildren().add(imageList);
  }

  /** Saves changes, refreshes left pane. */
  protected static void save() {
    try {
      controller.save();
    } catch (IOException exception) {
      displayAlert(
          "Save Error",
          "We encountered an error while saving, please try again. If this error persists, please contact customer support.");
    }
    lastSavedTime.setText("Last saved at " + new Timestamp(System.currentTimeMillis()));
    lastSavedTime.setFill(Color.GRAY);
    directoryTree.refreshTree();
  }

  /**
   * Displays alerts for exceptions.
   *
   * @param header Header for display.
   * @param content Content of display
   */
  protected static void displayAlert(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Displays directories that can be selected.
   *
   * @param primaryStage Application's primary stage.
   */
  private void displayDirectory(Stage primaryStage) {
    // Display directory
    // Code adapted from Stack Overflow answer
    // https://stackoverflow.com/questions/35070310/javafx-representing-directories
    DirectoryChooser dc = new DirectoryChooser();
    dc.setInitialDirectory(new File(System.getProperty("user.home")));
    rootFile = dc.showDialog(primaryStage);
    if (rootFile == null || !rootFile.isDirectory()) {
      displayAlert("Could not open directory.", "The file is invalid.");
    } else {
      rootFilePath = Paths.get(rootFile.getAbsolutePath());
      directoryTree.setRootDirectory(rootFile);
      refreshTree();
      if (rootFilePath != null) {
        Read.traverse(rootFilePath, controller.getContainer());
      }
      save();
      imageDetails.refresh();
    }
  }

  /** Selects directory to work on. */
  private void selectDirectory() {
    String acceptedExtensions = "^.*(\\.gif|\\.jpg|\\.tiff|\\.jpeg|\\.png)+$";
    if (rootFilePath != null) {
      TreeItem<File> clickedFile = directoryTree.getSelectionModel().getSelectedItem();
      if (clickedFile != null) {
        // Clicked on file, check if it's an image. If so, pass it to centre pane.
        String fileName = clickedFile.getValue().getName();
        if (fileName.matches(acceptedExtensions)) {
          String imageName = fileName.split("\\.")[0].split("\\s@")[0];
          Path imageDirectoryPath = clickedFile.getValue().toPath().getParent();
          // Update centre pane
          imageDetails.getChildren().clear();
          imageDetails.refresh(controller.getImage(imageDirectoryPath, imageName));
        }
      }
    }
  }

  /** Refreshes directoryTree . */
  protected static void refreshTree() {
    directoryTree.refreshTree();
  }

  /**
   * Gets DirectoryTree for files.
   *
   * @return directoryTree of files in directory chosen.
   */
  protected static DirectoryTree getDirectoryTree() {
    return directoryTree;
  }
}
