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

    opens site.overwrite.auditranscribe to javafx.fxml;
    exports site.overwrite.auditranscribe;
    exports site.overwrite.auditranscribe.views;
    opens site.overwrite.auditranscribe.views to javafx.fxml;
}