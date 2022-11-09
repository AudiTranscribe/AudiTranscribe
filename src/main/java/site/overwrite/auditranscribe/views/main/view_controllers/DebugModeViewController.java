/*
 * DebugModeViewController.java
 * Description: View controller that shows the debug view.
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

package site.overwrite.auditranscribe.views.main.view_controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.Theme;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DebugModeViewController extends ClassWithLogging implements Initializable {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ListView<String> debugList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    // Public methods

    /**
     * Method that shows the debug view.
     *
     * @param owner Owner of the debug view window.
     * @return The debug view controller.
     */
    public static DebugModeViewController showDebugView(Window owner) {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/main/debug-mode-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            DebugModeViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage debugStage = new Stage();
            debugStage.initStyle(StageStyle.UTILITY);
            debugStage.initModality(Modality.NONE);
            debugStage.initOwner(owner);
            debugStage.setTitle("Debug View");
            debugStage.setScene(scene);

            debugStage.setMinWidth(controller.rootPane.getPrefWidth());
            debugStage.setMaxWidth(controller.rootPane.getPrefWidth());

            // Show the stage
            debugStage.show();

            // Return the controller for further processing
            return controller;
        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that sets the content of the list.
     *
     * @param listContent Variable-value mappings.
     */
    public void setListContent(Map<String, String> listContent) {
        // Convert the map into strings to display
        List<String> displayStrings = new ArrayList<>();
        for (String key : listContent.keySet()) {
            displayStrings.add(key + ": " + listContent.get(key));
        }

        // Update the list view
        Platform.runLater(() -> debugList.setItems(FXCollections.observableList(displayStrings)));
    }

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
    }
}
