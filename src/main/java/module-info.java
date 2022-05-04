module site.overwrite.auditranscribe {
    requires java.desktop;

    requires javafx.controls;
    requires javafx.swing;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires commons.exec;
    requires org.apache.commons.compress;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires java.logging;
    requires javafx.media;

    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.views;
    exports site.overwrite.auditranscribe.audio;
    exports site.overwrite.auditranscribe.audio.filters;
    opens site.overwrite.auditranscribe to javafx.fxml;
    opens site.overwrite.auditranscribe.views to javafx.fxml;
}
