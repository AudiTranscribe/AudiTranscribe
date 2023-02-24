/*
 * DebugViewController.java
 * Description: Controller for the debug view.
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

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOMethods;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the debug view.
 */
public class DebugViewController extends AbstractViewController {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ListView<String> debugList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

    /**
     * Method that shows the debug view.
     *
     * @param owner Owner of the debug view window.
     * @return The debug view controller.
     */
    public static DebugViewController showDebugView(Window owner) {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("fxml/views/main/debug-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            DebugViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage debugStage = new Stage();
            debugStage.initModality(Modality.NONE);
            debugStage.initOwner(owner);
            debugStage.setTitle("Debug View");
            debugStage.setScene(scene);

            debugStage.setMinWidth(controller.rootPane.getPrefWidth());
            debugStage.setMaxWidth(controller.rootPane.getPrefWidth());

            // Show the stage
            debugStage.show();

            // Return focus to owner
            owner.requestFocus();

            // Return the controller for further processing
            return controller;
        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that sets the content of the list of variables.
     *
     * @param listContent List of variable-value pairs.
     */
    public void setListContent(List<Pair<String, String>> listContent) {
        // Convert the map into strings to display
        List<String> displayStrings = new ArrayList<>();
        for (Pair<String, String> entry : listContent) {
            displayStrings.add(entry.value0() + ": " + entry.value1());
        }

        // Update the list view
        Platform.runLater(() -> debugList.setItems(FXCollections.observableList(displayStrings)));
    }

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        // No graphics to set
    }
}
