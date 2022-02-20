/*
 * MainViewController.java
 *
 * Created on 2022-02-09
 * Updated on 2022-02-12
 *
 * Description: Contains the main view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    @FXML
    private WebView webView;  // What actually displays the request

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get the `webEngine` of the `webView` instance
        WebEngine webEngine = webView.getEngine();

        // Load the URL using the web engine
        webEngine.load("https://www.iana.org/domains/reserved");
        System.out.println("Loaded webpage.");
    }
}
