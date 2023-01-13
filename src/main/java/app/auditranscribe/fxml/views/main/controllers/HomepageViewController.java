/*
 * HomepageViewController.java
 * Description: Controller for the homepage.
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

package app.auditranscribe.fxml.views.main.controllers;

import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.generic.tuples.Quadruple;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.data_files.data_encapsulators.SettingsData;
import app.auditranscribe.io.db.ProjectsDB;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.utils.GUIUtils;
import app.auditranscribe.utils.MiscUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

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

/**
 * Controller for the homepage.
 */
public class HomepageViewController extends AbstractViewController {
    // Attributes
    private ProjectsDB projectsDB;

    private FilteredList<Quadruple<Long, String, String, String>> filteredList;  // List of project records

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    // Menu bar items
    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newProjectMenuItem, openProjectMenuItem, preferencesMenuItem, docsMenuItem, aboutMenuItem;

    // Sidebar
    @FXML
    private Label versionLabel;

    @FXML
    private Button newProjectButton, openProjectButton, preferencesButton;

    // Projects list
    @FXML
    private SVGPath searchImage;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<Quadruple<Long, String, String, String>> projectsListView;

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

//                // Set the scene switching state and data
//                sceneSwitchingState = SceneSwitchingState.OPEN_PROJECT;
//                sceneSwitchingData.file = file;

                log("Opening project: '" + selectedItem.value1() + "'");  // Value 1 is project name

                // Close this stage
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });

        refreshProjectsListView();

        // Add methods to menu items
        newProjectMenuItem.setOnAction(this::handleNewProject);
        openProjectMenuItem.setOnAction(this::handleOpenProject);
//        preferencesMenuItem.setOnAction(event -> PreferencesViewController.showPreferencesWindow());
        preferencesMenuItem.setOnAction(event -> log("preferencesMenuItem"));  // todo remove
        docsMenuItem.setOnAction(event -> GUIUtils.openURLInBrowser("https://docs.auditranscribe.app/"));
//        aboutMenuItem.setOnAction(event -> AboutViewController.showAboutWindow());
        aboutMenuItem.setOnAction(event -> log("aboutMenuItem"));  // todo remove

        // Report that the homepage is ready to be shown
        log("Homepage ready to be shown");
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        updateThemeCSS(rootPane);

        // Set graphics
        IconHelper.setSVGPath(searchImage, 20, "search-line");
        IconHelper.setSVGOnButton(preferencesButton, 15, 30, "cog-solid");
    }

    /**
     * Method that sets the version on the version label.
     *
     * @param version Current version of AudiTranscribe.
     */
    public void setVersionLabel(String version) {
        versionLabel.setText(version);
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
        // Todo add
        log("handleNewProject");
//        // Get the scene switching data
//        Pair<Boolean, SceneSwitchingData> pair = ProjectSetupViewController.showProjectSetupView();
//        boolean shouldProceed = pair.value0();
//        sceneSwitchingData = pair.value1();
//
//        // Specify the scene switching state
//        if (shouldProceed) {
//            // Signal the creation of a new project
//            sceneSwitchingState = SceneSwitchingState.NEW_PROJECT;
//
//            // Close this stage
//            ((Stage) rootPane.getScene().getWindow()).close();
//        }
    }

    /**
     * Helper method that helps open an existing project.
     *
     * @param actionEvent Event that triggered this function.
     */
    private void handleOpenProject(ActionEvent actionEvent) {
        // Todo add
        log("handleOpenProject");
//        // Get the current window
//        Window window = rootPane.getScene().getWindow();
//
//        // Get user to select an AUDT file
//        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
//                "AudiTranscribe files (*.audt)", "*.audt"
//        );
//        File file = GUIUtils.openFileDialog(window, extFilter);
//
//        // Verify that the user actually chose a file
//        if (file == null) {
//            Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
//        } else {
//            // Set the scene switching state and data
//            sceneSwitchingState = SceneSwitchingState.OPEN_PROJECT;
//            sceneSwitchingData.file = file;
//
//            // Close this stage
//            ((Stage) window).close();
//        }
    }

    // Helper classes
    static class CustomListCell extends ListCell<Quadruple<Long, String, String, String>> {
        // Constants
        final int BUTTON_AND_RECTANGLE_SIDE_LENGTH = 40;

        // Attributes
        ProjectsDB db;
        ListView<?> projectsListView;

        // FXML elements
        HBox content;

        StackPane shortNameDisplayArea;
        Rectangle shortNameRectangle;
        Label shortNameLabel;

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

            // Create all labels
            nameLabel = new Label();
            lastModifiedTimeLabel = new Label();
            filepathLabel = new Label();
            shortNameLabel = new Label();

            // Set CSS classes on text labels
            nameLabel.getStyleClass().add("project-name-label");
            lastModifiedTimeLabel.getStyleClass().add("last-opened-date-label");
            filepathLabel.getStyleClass().add("filepath-label");

            shortNameLabel.getStyleClass().add("short-name-label");

            // Create the short name display area
            shortNameRectangle = new Rectangle();
            shortNameRectangle.getStyleClass().add("short-name-rectangle");

            shortNameRectangle.setFill(Color.TRANSPARENT);
            shortNameRectangle.setWidth(BUTTON_AND_RECTANGLE_SIDE_LENGTH);
            shortNameRectangle.setHeight(BUTTON_AND_RECTANGLE_SIDE_LENGTH);

            shortNameDisplayArea = new StackPane();
            shortNameDisplayArea.getChildren().addAll(shortNameRectangle, shortNameLabel);

            // Set the removal button's style and method
            removeButton = new Button();
            removeButton.getStyleClass().add("image-button");
            removeButton.getStyleClass().add("remove-project-button");

            IconHelper.setSVGOnButton(
                    removeButton, 20, BUTTON_AND_RECTANGLE_SIDE_LENGTH, "window-close-line"
            );

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
                        HomepageViewController.class.getName()
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
                    new Insets(0, 5, 0, 20)
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
                        "[" + MiscUtils.formatDate(new Date(object.value0()), "yyyy-MM-dd HH:mm") + "]"
                );
                nameLabel.setText(object.value1());
                filepathLabel.setText(object.value2());
                shortNameLabel.setText(object.value3());

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
            // Sort in descending order
            long cmp = o1.value0() - o2.value0();
            if (cmp > 0) {
                return -1;
            } else if (cmp < 0) {
                return 1;
            }
            return 0;
        }
    }
}
