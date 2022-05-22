module site.overwrite.auditranscribe {
    requires java.desktop;
    requires java.sql;

    requires net.harawata.appdirs;
    requires commons.exec;
    requires com.dlsc.formsfx;
    requires com.google.gson;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires javatuples;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.audio;
    exports site.overwrite.auditranscribe.audio.filters;
    exports site.overwrite.auditranscribe.io.audt_file.data_encapsulators;
    exports site.overwrite.auditranscribe.views;
    exports site.overwrite.auditranscribe.views.helpers;

    opens site.overwrite.auditranscribe to javafx.fxml;
    opens site.overwrite.auditranscribe.views to javafx.fxml;
    opens site.overwrite.auditranscribe.views.helpers to javafx.fxml;
}
