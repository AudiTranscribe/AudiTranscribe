/*
 * MainViewController.java
 * Description: Contains the main view's controller class.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.main_views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.generic.tuples.Quadruple;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.io.data_files.data_encapsulators.SettingsData;
import site.overwrite.auditranscribe.io.db.ProjectsDB;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitchingData;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitchingState;
import site.overwrite.auditranscribe.misc.IconHelpers;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class MainViewController extends ClassWithLogging implements Initializable {
    // Attributes
    private ProjectsDB projectsDB;

    private FilteredList<Quadruple<Long, String, String, String>> filteredList;  // List of project records

    private SceneSwitchingState sceneSwitchingState;
    private SceneSwitchingData sceneSwitchingData = new SceneSwitchingData();

    // FXML Elements
    // Menu bar items
    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newProjectMenuItem, openProjectMenuItem, preferencesMenuItem, aboutMenuItem;

    // Main elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label versionLabel;

    @FXML
    private Button newProjectButton, openProjectButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private SVGPath searchImage;

    @FXML
    private ListView<Quadruple<Long, String, String, String>> projectsListView;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make macOS systems use the system menu bar
        if (OSMethods.getOS() == OSType.MAC) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Add methods to buttons
        newProjectButton.setOnAction(this::handleNewProject);

        openProjectButton.setOnAction(this::handleOpenProject);

        // Set the search field method
        searchTextField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredList.setPredicate(projectRecord -> {
                    // If filter text is empty, display all projects
                    if (newValue == null || newValue.isEmpty()) return true;

                    // Attempt to find a match within the *file path*
                    String searchFilter = newValue.toLowerCase();
                    String lowercaseFilepath = projectRecord.value2().toLowerCase();

                    return lowercaseFilepath.contains(searchFilter);
                })
        );

        // Update the projects list view
        projectsListView.setOnMouseClicked(mouseEvent -> {
            // Get the selected item
            Quadruple<Long, String, String, String> selectedItem =
                    projectsListView.getSelectionModel().getSelectedItem();

            // Check if an item was selected
            if (selectedItem != null) {
                // Get the file of the selected item
                String filepath = selectedItem.value2();
                File file = new File(filepath);

                // Set the scene switching state and data
                sceneSwitchingState = SceneSwitchingState.OPEN_PROJECT;
                sceneSwitchingData.file = file;

                // Close this stage
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });

        refreshProjectsListView();

        // Add methods to menu items
        newProjectMenuItem.setOnAction(this::handleNewProject);

        openProjectMenuItem.setOnAction(this::handleOpenProject);

        preferencesMenuItem.setOnAction(actionEvent -> PreferencesViewController.showPreferencesWindow());

        aboutMenuItem.setOnAction(actionEvent -> AboutViewController.showAboutWindow());

        // Report that the main view is ready to be shown
        log(Level.INFO, "Main view ready to be shown");
    }

    // Getter/Setter methods
    public SceneSwitchingState getSceneSwitchingState() {
        if (sceneSwitchingState == null) return SceneSwitchingState.CLOSE_SCENE;
        return sceneSwitchingState;
    }

    public SceneSwitchingData getSceneSwitchingData() {
        return sceneSwitchingData;
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));

        // Set graphics
        IconHelpers.setSVGPath(searchImage, 20,"search-line", theme.shortName);
    }

    /**
     * Method that sets the version on the version label.
     *
     * @param version Current version of AudiTranscribe.
     */
    public void setVersionLabel(String version) {
        versionLabel.setText("Version " + version);
    }

    /**
     * Method to refresh the projects' list view.
     */
    public void refreshProjectsListView() {
        // Get all projects' records
        List<Quadruple<Long, String, String, String>> projects = new ArrayList<>();

        try {
            // Get the projects database
            projectsDB = new ProjectsDB();

            // Get all projects' records
            Map<Integer, Pair<String, String>> projectRecords = projectsDB.getAllProjects();

            // Get all the keys
            Set<Integer> keys = projectRecords.keySet();

            // For each file, get their last accessed time and generate the shortened name
            for (int key : keys) {
                // Get both the filepath and the project name
                Pair<String, String> values = projectRecords.get(key);
                String filepath = values.value0();
                String projectName = values.value1();

                // Add the project to the records
                BasicFileAttributes attributes;
                try {
                    // Get the last modified time of the file
                    attributes = Files.readAttributes(Paths.get(filepath), BasicFileAttributes.class);

                    FileTime lastModifiedTime = attributes.lastModifiedTime();
                    long lastModifiedTimestamp = lastModifiedTime.toMillis();

                    // Get the shortened name of the file name
                    String shortenedName = MiscUtils.getShortenedName(projectName);

                    // Add to the list of projects
                    projects.add(new Quadruple<>(lastModifiedTimestamp, projectName, filepath, shortenedName));
                } catch (NoSuchFileException e) {
                    projectsDB.deleteProjectRecord(key);
                }
            }

            // Sort the list of projects by the last access timestamp
            projects.sort(new SortByTimestamp());

        } catch (SQLException | IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }

        // Convert the `projects` list to an FXML `ObservableList` and a `FilteredList` to allow for searching
        ObservableList<Quadruple<Long, String, String, String>> projectsList = FXCollections.observableList(projects);
        filteredList = new FilteredList<>(projectsList);

        if (projectsList.size() != 0) {
            projectsListView.setBackground(Background.fill(Color.WHITE));
            projectsListView.setItems(new SortedList<>(filteredList));  // Use a sorted list for searching
            projectsListView.setCellFactory(
                    customListCellListView -> new CustomListCell(
                            projectsDB, projectsListView, DataFiles.SETTINGS_DATA_FILE.data
                    )
            );
        } else {
            log(Level.INFO, "No projects found");
            projectsListView.opacityProperty().set(0);
        }
    }

    // Private methods

    /**
     * Helper method that helps open a new project.
     *
     * @param actionEvent Event that triggered this function.
     */
    private void handleNewProject(ActionEvent actionEvent) {
        // Get the scene switching data
        Pair<Boolean, SceneSwitchingData> pair = ProjectSetupViewController.showProjectSetupView();
        boolean shouldProceed = pair.value0();
        sceneSwitchingData = pair.value1();

        // Specify the scene switching state
        if (shouldProceed) {
            // Signal the creation of a new project
            sceneSwitchingState = SceneSwitchingState.NEW_PROJECT;

            // Close this stage
            ((Stage) rootPane.getScene().getWindow()).close();
        }
    }

    /**
     * Helper method that helps open an existing project.
     *
     * @param actionEvent Event that triggered this function.
     */
    private void handleOpenProject(ActionEvent actionEvent) {
        // Get the current window
        Window window = rootPane.getScene().getWindow();

        // Get user to select an AUDT file
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "AudiTranscribe files (*.audt)", "*.audt"
        );
        File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

        // Verify that the user actually chose a file
        if (file == null) {
            Popups.showInformationAlert("Info", "No file selected.");
        } else {
            // Set the scene switching state and data
            sceneSwitchingState = SceneSwitchingState.OPEN_PROJECT;
            sceneSwitchingData.file = file;

            // Close this stage
            ((Stage) window).close();
        }
    }

    // Helper classes
    static class CustomListCell extends ListCell<Quadruple<Long, String, String, String>> {
        // Attributes
        ProjectsDB db;
        ListView<?> projectsListView;

        // FXML elements
        HBox content;

        StackPane shortNameDisplayArea;
        Rectangle shortNameRectangle;
        Text shortNameText;

        Label nameLabel;
        Label lastModifiedTimeLabel;
        Label filepathLabel;

        Button removeButton;

        public CustomListCell(ProjectsDB db, ListView<?> projectsListView, SettingsData settingsData) {
            // Call superclass initialization method
            super();

            // Update attributes
            this.db = db;
            this.projectsListView = projectsListView;

            // Create all labels and texts
            nameLabel = new Label();
            lastModifiedTimeLabel = new Label();
            filepathLabel = new Label();

            shortNameText = new Text();

            // Set CSS classes on text labels
            nameLabel.getStyleClass().add("project-name-label");
            lastModifiedTimeLabel.getStyleClass().add("last-opened-date-label");
            filepathLabel.getStyleClass().add("filepath-label");

            shortNameText.getStyleClass().add("short-name-text");

            // Create the short name display area
            shortNameRectangle = new Rectangle();
            shortNameRectangle.getStyleClass().add("short-name-rectangle");

            shortNameRectangle.setFill(Color.TRANSPARENT);
            shortNameRectangle.setWidth(50);
            shortNameRectangle.setHeight(50);

            shortNameDisplayArea = new StackPane();
            shortNameDisplayArea.getChildren().addAll(shortNameRectangle, shortNameText);

            // Set the removal button's style and method
            SVGPath removeButtonGraphic = new SVGPath();
            IconHelpers.setSVGPath(
                    removeButtonGraphic,
                    20,
                    "window-close-line",
                    Theme.values()[settingsData.themeEnumOrdinal].shortName
            );

            removeButton = new Button();
            removeButton.setGraphic(removeButtonGraphic);
            removeButton.getStyleClass().add("image-button");
            removeButton.getStyleClass().add("remove-project-button");

            removeButton.setOnAction(actionEvent -> {
                // Get the filepath of the project
                String filepath = filepathLabel.getText();

                // Get the primary key from the database
                int pk;
                try {
                    pk = db.getPKOfProjectWithFilepath(filepath);
                } catch (SQLException e) {
                    logException(e);
                    throw new RuntimeException(e);
                }

                // Delete that project's record from the database
                try {
                    db.deleteProjectRecord(pk);
                } catch (SQLException e) {
                    logException(e);
                    throw new RuntimeException(e);
                }

                log(
                        Level.INFO,
                        "Removed '" + nameLabel.getText() + "' with primary key " +
                                pk + " from projects' database",
                        MainViewController.class.getName()
                );

                // Remove this list item from the list view
                SortedList<?> sortedList = (SortedList<?>) getListView().getItems();
                FilteredList<?> filteredList = (FilteredList<?>) sortedList.getSource();
                ObservableList<?> sourceList = filteredList.getSource();
                sourceList.remove(getItem());

                if (sourceList.size() == 0) projectsListView.setBackground(Background.fill(Color.TRANSPARENT));
            });

            // Set the content
            HBox nameAndDateBox = new HBox(nameLabel, lastModifiedTimeLabel);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            HBox.setHgrow(lastModifiedTimeLabel, Priority.ALWAYS);
            nameAndDateBox.setSpacing(10);

            VBox nameDateAndFilepathBox = new VBox(nameAndDateBox, filepathLabel);
            nameDateAndFilepathBox.setSpacing(5);

            content = new HBox(shortNameDisplayArea, nameDateAndFilepathBox, removeButton);
            content.setSpacing(10);
            content.setPadding(
                    // Although FXML file uses 30, use 25 because spacing is 10
                    new Insets(0, 25, 0, 25)
            );
            content.getStyleClass().add("project-list-cell");
        }

        @Override
        protected void updateItem(Quadruple<Long, String, String, String> object, boolean empty) {
            // Call superclass method
            super.updateItem(object, empty);

            // Ensure that the object is not null and non-empty
            if (object != null & !empty) {
                // Convert the timestamp to a date string
                lastModifiedTimeLabel.setText(
                        "[Last modified on " +
                                MiscUtils.formatDate(new Date(object.value0()), "yyyy-MM-dd HH:mm") +
                                "]"
                );
                nameLabel.setText(object.value1());
                filepathLabel.setText(object.value2());
                shortNameText.setText(object.value3());

                // Set the graphic of the list item
                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }

    static class SortByTimestamp implements Comparator<Quadruple<Long, String, String, String>> {
        @Override
        public int compare(
                Quadruple<Long, String, String, String> o1,
                Quadruple<Long, String, String, String> o2
        ) {
            // Convert timestamps from millisecond value to second value
            long timestamp1 = o1.value0() / 1000L;  // Divide by 1000 because 1000 ms = 1s
            long timestamp2 = o2.value0() / 1000L;

            // Now compare the timestamp values
            return (int) -(timestamp1 - timestamp2);  // Sort in descending order
        }
    }
}
