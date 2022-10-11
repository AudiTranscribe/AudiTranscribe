/*
 * module-info.java
 * Description: Module info class.
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

module site.overwrite.auditranscribe {
    requires java.desktop;
    requires java.sql;

    requires commons.exec;
    requires com.dlsc.formsfx;
    requires com.google.gson;
    requires org.apache.commons.compress;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.audio;
    exports site.overwrite.auditranscribe.audio.exceptions;
    exports site.overwrite.auditranscribe.audio.filters;
    exports site.overwrite.auditranscribe.audio.window_functions;
    exports site.overwrite.auditranscribe.io.audt_file;
    exports site.overwrite.auditranscribe.io.audt_file.base;
    exports site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators;
    exports site.overwrite.auditranscribe.io.data_files;
    exports site.overwrite.auditranscribe.io.data_files.data_encapsulators;
    exports site.overwrite.auditranscribe.io.data_files.file_classes;
    exports site.overwrite.auditranscribe.io.exceptions;
    exports site.overwrite.auditranscribe.main_views;
    exports site.overwrite.auditranscribe.main_views.scene_switching;
    exports site.overwrite.auditranscribe.misc;
    exports site.overwrite.auditranscribe.generic.exceptions;
    exports site.overwrite.auditranscribe.misc.tuples;
    exports site.overwrite.auditranscribe.music.exceptions;
    exports site.overwrite.auditranscribe.music.notes;
    exports site.overwrite.auditranscribe.network.exceptions;
    exports site.overwrite.auditranscribe.setup_wizard;
    exports site.overwrite.auditranscribe.setup_wizard.helpers;
    exports site.overwrite.auditranscribe.setup_wizard.helpers.data_encapsulators;
    exports site.overwrite.auditranscribe.setup_wizard.view_controllers;
    exports site.overwrite.auditranscribe.spectrogram;
    exports site.overwrite.auditranscribe.utils;

    opens site.overwrite.auditranscribe to javafx.fxml;
    opens site.overwrite.auditranscribe.main_views to javafx.fxml;
    opens site.overwrite.auditranscribe.main_views.scene_switching to javafx.fxml;
    opens site.overwrite.auditranscribe.misc to javafx.fxml;
    opens site.overwrite.auditranscribe.setup_wizard to javafx.fxml;
    opens site.overwrite.auditranscribe.setup_wizard.view_controllers to javafx.fxml;
}
