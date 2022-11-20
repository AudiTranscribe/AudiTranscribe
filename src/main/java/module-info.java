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

module app.auditranscribe {
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

    exports app.auditranscribe;
    exports app.auditranscribe.audio;
    exports app.auditranscribe.audio.exceptions;
    exports app.auditranscribe.audio.filters;
    exports app.auditranscribe.audio.window_functions;
    exports app.auditranscribe.generic.exceptions;
    exports app.auditranscribe.generic.tuples;
    exports app.auditranscribe.io.audt_file;
    exports app.auditranscribe.io.audt_file.base;
    exports app.auditranscribe.io.audt_file.base.data_encapsulators;
    exports app.auditranscribe.io.data_files;
    exports app.auditranscribe.io.data_files.data_encapsulators;
    exports app.auditranscribe.io.data_files.file_classes;
    exports app.auditranscribe.io.exceptions;
    exports app.auditranscribe.misc;
    exports app.auditranscribe.misc.icon;
    exports app.auditranscribe.music;
    exports app.auditranscribe.music.exceptions;
    exports app.auditranscribe.music.notes;
    exports app.auditranscribe.network;
    exports app.auditranscribe.network.exceptions;
    exports app.auditranscribe.spectrogram;
    exports app.auditranscribe.utils;
    exports app.auditranscribe.views.main;
    exports app.auditranscribe.views.main.scene_switching;
    exports app.auditranscribe.views.main.view_controllers;
    exports app.auditranscribe.views.setup_wizard;
    exports app.auditranscribe.views.setup_wizard.download_handlers;
    exports app.auditranscribe.views.setup_wizard.view_controllers;

    opens app.auditranscribe to javafx.fxml;
    opens app.auditranscribe.misc to javafx.fxml;
    opens app.auditranscribe.misc.icon to com.google.gson, javafx.fxml;
    opens app.auditranscribe.views.main to javafx.fxml;
    opens app.auditranscribe.views.main.scene_switching to javafx.fxml;
    opens app.auditranscribe.views.main.view_controllers to javafx.fxml;
    opens app.auditranscribe.views.setup_wizard to javafx.fxml;
    opens app.auditranscribe.views.setup_wizard.view_controllers to javafx.fxml;
}
