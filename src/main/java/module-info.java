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
    requires java.desktop;
    requires java.logging;
    requires java.sql;

    // General dependencies
    requires com.google.gson;
    requires org.apache.commons.compress;
    requires org.xerial.sqlitejdbc;

    // JavaFX-related dependencies
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // Exports
    exports app.auditranscribe;
    exports app.auditranscribe.fxml;
    exports app.auditranscribe.fxml.views;
    exports app.auditranscribe.fxml.views.main;
    exports app.auditranscribe.generic;
    exports app.auditranscribe.generic.tuples;
    exports app.auditranscribe.io.data_files;
    exports app.auditranscribe.io.data_files.data_encapsulators;
    exports app.auditranscribe.io.data_files.file_classes;
    exports app.auditranscribe.io.exceptions;
    exports app.auditranscribe.misc;
    exports app.auditranscribe.signal;
    exports app.auditranscribe.signal.feature_extraction;
    exports app.auditranscribe.signal.onset_detection;
    exports app.auditranscribe.signal.representations;
    exports app.auditranscribe.signal.resampling_filters;
    exports app.auditranscribe.signal.time_domain_processing;
    exports app.auditranscribe.signal.windowing;

    // Opens
    opens app.auditranscribe to javafx.fxml;
    opens app.auditranscribe.fxml to com.google.gson, javafx.fxml;
    opens app.auditranscribe.fxml.views.main.controllers to javafx.fxml;
}