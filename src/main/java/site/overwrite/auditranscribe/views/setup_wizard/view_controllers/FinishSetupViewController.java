/*
 * FinishSetupViewController.java
 * Description: View controller for the view that signals the end of the setup process.
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

package site.overwrite.auditranscribe.views.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller for the view that signals the end of the setup process.
 */
public class FinishSetupViewController extends AbstractSetupViewController {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button closeButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
        log(Level.INFO, "AudiTranscribe setup complete");
    }
}
