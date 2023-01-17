/*
 * MainApplication.java
 * Description: Contains the main application class.
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

package app.auditranscribe;

import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

@ExcludeFromGeneratedCoverageReport
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Todo: update
//        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
//                "fxml/views/main/homepage-view.fxml"
//        ));
//        Scene scene = new Scene(fxmlLoader.load());
//
//        HomepageViewController controller = fxmlLoader.getController();
//        controller.setThemeOnScene();
//
//        stage.setTitle("AudiTranscribe");
//        stage.setScene(scene);
//        stage.show();

        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                "fxml/views/main/transcription-view.fxml"
        ));
        Scene scene = new Scene(fxmlLoader.load());

        AbstractViewController controller = fxmlLoader.getController();
        controller.setThemeOnScene();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        stage.setTitle("AudiTranscribe");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.setMinWidth(screenBounds.getWidth());
        stage.setMinHeight(screenBounds.getHeight());
        stage.setX(0);
        stage.setY(0);

        stage.show();
        controller.finishSetup();
    }

    public static void main(String[] args) {
        launch();
    }
}
