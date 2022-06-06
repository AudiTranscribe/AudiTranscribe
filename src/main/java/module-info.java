module site.overwrite.auditranscribe {
    requires java.desktop;
    requires java.sql;

    requires commons.exec;
    requires com.dlsc.formsfx;
    requires com.google.gson;
    requires ffmpeg;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;
    requires java.logging;
    requires javatuples;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.audio;
    exports site.overwrite.auditranscribe.audio.ffmpeg;
    exports site.overwrite.auditranscribe.audio.filters;
    exports site.overwrite.auditranscribe.audio.window_functions;
    exports site.overwrite.auditranscribe.exceptions;
    exports site.overwrite.auditranscribe.io.audt_file.data_encapsulators;
    exports site.overwrite.auditranscribe.io.json_files;
    exports site.overwrite.auditranscribe.io.json_files.data_encapsulators;
    exports site.overwrite.auditranscribe.io.json_files.file_classes;
    exports site.overwrite.auditranscribe.misc;
    exports site.overwrite.auditranscribe.spectrogram;
    exports site.overwrite.auditranscribe.utils;
    exports site.overwrite.auditranscribe.views;
    exports site.overwrite.auditranscribe.views.helpers;

    opens site.overwrite.auditranscribe to javafx.fxml;
    opens site.overwrite.auditranscribe.misc to javafx.fxml;
    opens site.overwrite.auditranscribe.views to javafx.fxml;
    opens site.overwrite.auditranscribe.views.helpers to javafx.fxml;
}
