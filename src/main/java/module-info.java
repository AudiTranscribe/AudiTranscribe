module site.overwrite.auditranscribe {
    requires java.desktop;

    requires commons.exec;
    requires com.dlsc.formsfx;
    requires com.google.gson;
    requires ffmpeg;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;

    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.audio;
    exports site.overwrite.auditranscribe.audio.filters;
    exports site.overwrite.auditranscribe.io.data_encapsulators;
    exports site.overwrite.auditranscribe.views;

    opens site.overwrite.auditranscribe to javafx.fxml;
    opens site.overwrite.auditranscribe.views to javafx.fxml;
}
