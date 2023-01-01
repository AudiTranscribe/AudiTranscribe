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

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.application.Application;
import javafx.stage.Stage;

@ExcludeFromGeneratedCoverageReport
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Todo: update
        stage.setTitle("Main Application");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
