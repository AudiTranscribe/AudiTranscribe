/*
 * module-info.java
 * Description: Module info file.
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

module AudiTranscribe {
    // Java dependencies
    requires java.logging;
    requires java.desktop;

    // General dependencies
    requires com.google.gson;

    // JavaFX-related dependencies
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // Exports
    exports app.auditranscribe;
    exports app.auditranscribe.signal;
    exports app.auditranscribe.signal.resampling_filters;

    // Opens
    opens app.auditranscribe to javafx.fxml;
    opens app.auditranscribe.fxml to com.google.gson, javafx.fxml;
}